/* ==================================================================
 * OcppWebSocketHandler.java - 31/01/2020 3:34:19 pm
 *
 * Copyright 2020 SolarNetwork.net Dev Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * ==================================================================
 */

package net.solarnetwork.ocpp.web.json;

import static java.util.Collections.singletonList;
import static net.solarnetwork.util.ObjectUtils.requireNonNullArgument;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Date;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import javax.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.SubProtocolCapable;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.adapter.NativeWebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.solarnetwork.ocpp.domain.Action;
import net.solarnetwork.ocpp.domain.ActionMessage;
import net.solarnetwork.ocpp.domain.BasicActionMessage;
import net.solarnetwork.ocpp.domain.ChargePointIdentity;
import net.solarnetwork.ocpp.domain.ChargePointSessionIdentity;
import net.solarnetwork.ocpp.domain.ErrorCode;
import net.solarnetwork.ocpp.domain.ErrorCodeException;
import net.solarnetwork.ocpp.domain.ErrorHolder;
import net.solarnetwork.ocpp.domain.PendingActionMessage;
import net.solarnetwork.ocpp.domain.SchemaValidationException;
import net.solarnetwork.ocpp.json.ActionPayloadDecoder;
import net.solarnetwork.ocpp.json.CallErrorMessage;
import net.solarnetwork.ocpp.json.CallMessage;
import net.solarnetwork.ocpp.json.CallResultMessage;
import net.solarnetwork.ocpp.json.MessageType;
import net.solarnetwork.ocpp.json.RpcError;
import net.solarnetwork.ocpp.json.WebSocketSubProtocol;
import net.solarnetwork.ocpp.service.ActionMessageProcessor;
import net.solarnetwork.ocpp.service.ActionMessageQueue;
import net.solarnetwork.ocpp.service.ActionMessageResultHandler;
import net.solarnetwork.ocpp.service.ChargePointBroker;
import net.solarnetwork.ocpp.service.ErrorCodeResolver;
import net.solarnetwork.ocpp.service.SimpleActionMessageQueue;
import net.solarnetwork.security.AuthorizationException;
import net.solarnetwork.service.PingTest;
import net.solarnetwork.service.PingTestResult;
import net.solarnetwork.settings.SettingsChangeObserver;
import net.solarnetwork.util.StatTracker;

/**
 * OCPP Charge Point JSON web socket handler.
 *
 * <p>
 * This class is responsible for encoding/decoding the OCPP JSON web socket
 * message protocol. When a Charge Point sends a message to this service, the
 * JSON will be decoded using {@link #getCentralServiceActionPayloadDecoder()},
 * and the resulting payload will be passed to any configured
 * {@link ActionMessageProcessor} instances associated with the message action.
 * The action message processor must eventually call
 * {@link ActionMessageResultHandler#handleActionMessageResult(ActionMessage, Object, Throwable)}
 * with the final result (or error), and that will be encoded into a JSON
 * message and sent back to the originating Charge Point client.
 * </p>
 *
 * <p>
 * This class also implements {@link ChargePointBroker}, so that other classes
 * can push messages to any connected Charge Point client. The
 * {@link #sendMessageToChargePoint(ActionMessage, ActionMessageResultHandler)}
 * will encode a {@link CallMessage} into JSON and sent that as a request to a
 * connected Charge Point matching the message's client ID. When the Charge
 * Point client sends a result (or error) response to the message, it will be
 * passed to the {@link ActionMessageResultHandler} originally provided.
 * </p>
 *
 * @param <C>
 *        the charge point action enumeration to use
 * @param <S>
 *        the central system action enumeration to use
 * @author matt
 * @version 1.10
 */
public class OcppWebSocketHandler<C extends Enum<C> & Action, S extends Enum<S> & Action>
		extends AbstractWebSocketHandler implements WebSocketHandler, SubProtocolCapable,
		SettingsChangeObserver, ChargePointBroker, PingTest {

	/** The default {@code pendingMessageTimeout} property. */
	public static final long DEFAULT_PENDING_MESSAGE_TIMEOUT = TimeUnit.SECONDS.toMillis(120);

	/** The default {@code pingFrequencySecs} property. */
	public static final int DEFAULT_PING_FREQUENCY_SECS = 50;

	/** A class logger. */
	protected final Logger log = LoggerFactory.getLogger(getClass());

	/** An executor. */
	protected final AsyncTaskExecutor executor;

	// A NavigableMap map is used here because the methods afterConnectionEstablished and afterConnectionClosed
	// is not called strictly in order of time, and so a Map<ChargePointIdentity, WebSocketSession> cannot
	// be safely used as multiple WebSocketSessions may exist for the same identity. To work with this,
	// a NavigableMap with ChargePointSessionIdentity keys is used, so we can still quickly find the
	// session(s) associated with a ChargePointIdentity via a tail-submap starting with a "boundary" key
	// having the user and session values as empty strings, which sort before all others.
	private final ConcurrentNavigableMap<ChargePointSessionIdentity, WebSocketSession> clientSessions;

	/** A statistics instance. */
	protected final StatTracker stats;

	private final Class<C> chargePointActionClass;
	private final Class<S> centralSystemActionClass;
	private final ErrorCodeResolver errorCodeResolver;
	private final Map<Action, Set<ActionMessageProcessor<Object, Object>>> processors;
	private final ActionMessageQueue pendingMessages;
	private final ObjectMapper mapper;
	private TaskScheduler taskScheduler;
	private ActionPayloadDecoder centralServiceActionPayloadDecoder;
	private ActionPayloadDecoder chargePointActionPayloadDecoder;
	private long pendingMessageTimeout = DEFAULT_PENDING_MESSAGE_TIMEOUT;
	private int pingFrequencySecs = DEFAULT_PING_FREQUENCY_SECS;
	private CloseStatus shutdownCloseStatus = CloseStatus.SERVICE_RESTARTED;

	private boolean started;
	private Future<?> startupTask;
	private ScheduledFuture<?> pendingTimeoutChore;
	private ScheduledFuture<?> pingChore;

	/**
	 * Constructor.
	 *
	 * <p>
	 * An in-memory queue will be used for pending messages.
	 * </p>
	 *
	 * @param chargePointActionClass
	 *        the charge point action class
	 * @param centralSystemActionClass
	 *        the central system action class
	 * @param errorCodeResolver
	 *        the error code resolver
	 * @param executor
	 *        an executor for tasks
	 * @param mapper
	 *        the object mapper to use
	 */
	public OcppWebSocketHandler(Class<C> chargePointActionClass, Class<S> centralSystemActionClass,
			ErrorCodeResolver errorCodeResolver, AsyncTaskExecutor executor, ObjectMapper mapper) {
		this(defaultStatTracker(), chargePointActionClass, centralSystemActionClass, errorCodeResolver,
				executor, mapper);
	}

	/**
	 * Constructor.
	 *
	 * <p>
	 * An in-memory queue will be used for pending messages.
	 * </p>
	 *
	 * @param stats
	 *        the stats instance
	 * @param chargePointActionClass
	 *        the charge point action class
	 * @param centralSystemActionClass
	 *        the central system action class
	 * @param errorCodeResolver
	 *        the error code resolver
	 * @param executor
	 *        an executor for tasks
	 * @param mapper
	 *        the object mapper to use
	 * @since 1.9
	 */
	public OcppWebSocketHandler(StatTracker stats, Class<C> chargePointActionClass,
			Class<S> centralSystemActionClass, ErrorCodeResolver errorCodeResolver,
			AsyncTaskExecutor executor, ObjectMapper mapper) {
		super();
		this.stats = requireNonNullArgument(stats, "stats");
		this.chargePointActionClass = chargePointActionClass;
		this.centralSystemActionClass = centralSystemActionClass;
		this.errorCodeResolver = errorCodeResolver;
		this.executor = executor;
		this.processors = new ConcurrentHashMap<>(16, 0.9f, 1);
		this.clientSessions = new ConcurrentSkipListMap<>();
		this.pendingMessages = new SimpleActionMessageQueue();
		this.mapper = mapper;
	}

	/**
	 * Constructor.
	 *
	 * @param chargePointActionClass
	 *        the charge point action class
	 * @param centralSystemActionClass
	 *        the central system action class
	 * @param errorCodeResolver
	 *        the error code resolver
	 * @param executor
	 *        an executor for tasks
	 * @param mapper
	 *        the object mapper to use
	 * @param pendingMessageQueue
	 *        a queue to hold pending messages, for individual client IDs
	 * @param centralServiceActionPayloadDecoder
	 *        the action payload decoder to use
	 * @param chargePointActionPayloadDecoder
	 *        for Central Service message the action payload decoder to use for
	 *        Charge Point messages
	 * @throws IllegalArgumentException
	 *         if any parameter is {@literal null}
	 */
	public OcppWebSocketHandler(Class<C> chargePointActionClass, Class<S> centralSystemActionClass,
			ErrorCodeResolver errorCodeResolver, AsyncTaskExecutor executor, ObjectMapper mapper,
			ActionMessageQueue pendingMessageQueue,
			ActionPayloadDecoder centralServiceActionPayloadDecoder,
			ActionPayloadDecoder chargePointActionPayloadDecoder) {
		this(defaultStatTracker(), chargePointActionClass, centralSystemActionClass, errorCodeResolver,
				executor, mapper, pendingMessageQueue, centralServiceActionPayloadDecoder,
				chargePointActionPayloadDecoder);
	}

	/**
	 * Constructor.
	 *
	 * @param stats
	 *        the stats instance
	 * @param chargePointActionClass
	 *        the charge point action class
	 * @param centralSystemActionClass
	 *        the central system action class
	 * @param errorCodeResolver
	 *        the error code resolver
	 * @param executor
	 *        an executor for tasks
	 * @param mapper
	 *        the object mapper to use
	 * @param pendingMessageQueue
	 *        a queue to hold pending messages, for individual client IDs
	 * @param centralServiceActionPayloadDecoder
	 *        the action payload decoder to use
	 * @param chargePointActionPayloadDecoder
	 *        for Central Service message the action payload decoder to use for
	 *        Charge Point messages
	 * @throws IllegalArgumentException
	 *         if any parameter is {@literal null}
	 * @since 1.9
	 */
	public OcppWebSocketHandler(StatTracker stats, Class<C> chargePointActionClass,
			Class<S> centralSystemActionClass, ErrorCodeResolver errorCodeResolver,
			AsyncTaskExecutor executor, ObjectMapper mapper, ActionMessageQueue pendingMessageQueue,
			ActionPayloadDecoder centralServiceActionPayloadDecoder,
			ActionPayloadDecoder chargePointActionPayloadDecoder) {
		super();
		this.stats = requireNonNullArgument(stats, "stats");
		this.chargePointActionClass = requireNonNullArgument(chargePointActionClass,
				"chargePointActionClass");
		this.centralSystemActionClass = requireNonNullArgument(centralSystemActionClass,
				"centralSystemActionClass");
		this.errorCodeResolver = requireNonNullArgument(errorCodeResolver, "errorCodeResolver");
		this.executor = requireNonNullArgument(executor, "executor");
		this.mapper = requireNonNullArgument(mapper, "mapper");
		this.pendingMessages = requireNonNullArgument(pendingMessageQueue, "pendingMessageQueue");
		this.processors = new ConcurrentHashMap<>(16, 0.9f, 1);
		this.clientSessions = new ConcurrentSkipListMap<>();
		setCentralServiceActionPayloadDecoder(centralServiceActionPayloadDecoder);
		setChargePointActionPayloadDecoder(chargePointActionPayloadDecoder);
	}

	private static final StatTracker defaultStatTracker() {
		return new StatTracker("OcppWebSocketHandler", null,
				LoggerFactory.getLogger(OcppWebSocketHandler.class.getName() + ".STATS"), 500);
	}

	/**
	 * Call once this service is configured.
	 */
	public synchronized void startup() {
		startup(true);
	}

	/**
	 * Call once this service is configured.
	 *
	 * @param scheduleJobs
	 *        {@code true} to schedule internal task jobs
	 * @since 1.6
	 */
	public synchronized void startup(boolean scheduleJobs) {
		if ( scheduleJobs ) {
			configurationChanged(null);
		}
		started = true;
	}

	/**
	 * Test if the service has been started.
	 *
	 * @return {@literal true} if the service has been started
	 * @since 1.8
	 */
	public boolean isStarted() {
		return started;
	}

	/**
	 * Call once this service is no longer needed, to free up internal
	 * resources.
	 */
	public synchronized void shutdown() {
		started = false;
		if ( startupTask != null ) {
			startupTask.cancel(true);
		}
		unshceduleChores();
		disconnectClients();
	}

	@Override
	public synchronized void configurationChanged(Map<String, Object> properties) {
		if ( startupTask != null ) {
			return;
		}
		startupTask = executor.submit(new StartupTask());
	}

	private synchronized void scheduleChores() {
		if ( taskScheduler == null ) {
			return;
		}
		if ( pendingTimeoutChore == null ) {
			long freq = Math.max(1000, pendingMessageTimeout / 10);
			pendingTimeoutChore = taskScheduler.scheduleWithFixedDelay(new PendingTimeoutChore(),
					new Date(System.currentTimeMillis() + pendingMessageTimeout), freq);
			log.info("Scheduled pending timeout cleaner task at rate {}s with timeout {}s", freq / 1000,
					pendingMessageTimeout / 1000);
		}
		if ( pingChore == null && pingFrequencySecs > 0 ) {
			long freq = TimeUnit.SECONDS.toMillis(pingFrequencySecs);
			pingChore = taskScheduler.scheduleWithFixedDelay(new PingChore(),
					new Date(System.currentTimeMillis() + freq), freq);
			log.info("Scheduled PING task at rate {}s", pingFrequencySecs);
		}
	}

	private synchronized void unshceduleChores() {
		if ( pendingTimeoutChore != null ) {
			pendingTimeoutChore.cancel(true);
		}
		if ( pingChore != null ) {
			pingChore.cancel(true);
		}
	}

	private class StartupTask implements Runnable {

		@Override
		public void run() {
			try {
				Thread.sleep(2000);
				scheduleChores();
				synchronized ( OcppWebSocketHandler.this ) {
					if ( startupTask == this ) {
						startupTask = null;
					}
				}
			} catch ( InterruptedException e ) {
				// ignore
			}
		}

	}

	private class PendingTimeoutChore implements Runnable {

		@Override
		public void run() {
			log.debug("Looking for expired pending message to clean...");
			final long expiration = System.currentTimeMillis() - pendingMessageTimeout;
			for ( Entry<ChargePointIdentity, Deque<PendingActionMessage>> me : pendingMessages
					.allQueues() ) {
				boolean processNext = false;
				ChargePointIdentity clientId = me.getKey();
				Deque<PendingActionMessage> q = me.getValue();
				PendingActionMessage msg = q.peek();
				if ( msg != null && msg.getDate() < expiration ) {
					log.warn("Cleaning client {} expired pending message {}", clientId, msg);
					synchronized ( q ) {
						q.removeFirstOccurrence(msg);
						processNext = true;
					}
					// let handler know we've timed out
					try {
						msg.getHandler().handleActionMessageResult(msg.getMessage(), null,
								new TimeoutException("Message not handled within configured timeout."));
					} catch ( Throwable t ) {
						// ignore
					}
				}
				if ( processNext ) {
					processNextPendingMessage(q);
				}
			}
		}

	}

	private final class PingTask implements Runnable {

		private final ChargePointIdentity cp;
		private final Session s;

		private PingTask(ChargePointIdentity cp, Session s) {
			super();
			this.cp = cp;
			this.s = s;
		}

		@Override
		public void run() {
			try {
				if ( s.isOpen() ) {
					ByteBuffer msg = ByteBuffer.allocate(0);
					log.trace("Sending PING to charge point {}", cp);
					s.getBasicRemote().sendPing(msg);
				}
			} catch ( IOException | IllegalStateException e ) {
				log.debug("Communication problem sending PING to charge point {}: {}", cp, e);
			} catch ( Exception e ) {
				log.info("Exception sending PING to charge point {}: {}", cp, e);
			}
		}
	}

	private final class PingChore implements Runnable {

		@Override
		public void run() {
			int count = 0;
			for ( WebSocketSession wss : clientSessions.values() ) {
				final Session s;
				if ( wss instanceof NativeWebSocketSession ) {
					s = ((NativeWebSocketSession) wss).getNativeSession(Session.class);
				} else {
					s = null;
				}
				if ( s == null ) {
					continue;
				}
				final ChargePointIdentity ident = clientId(wss);
				if ( ident == null ) {
					continue;
				}
				if ( s.isOpen() ) {
					try {
						executor.execute(new PingTask(ident, s));
						count++;
					} catch ( TaskRejectedException e ) {
						log.warn("Unable to schedule PING task for charge point {}: {}", ident, e);
					}
				}
			}
			if ( count > 0 ) {
				log.info("Scheduled PING frames for {} connected charge points", count);
			}
		}

	}

	/**
	 * Disconnect all connected clients.
	 *
	 * <p>
	 * The {@link #getShutdownCloseStatus()} value will be used.
	 * </p>
	 */
	protected void disconnectClients() {
		log.info("Disconnecting {} connected chargers.", clientSessions.size());
		for ( Iterator<WebSocketSession> itr = clientSessions.values().iterator(); itr.hasNext(); ) {
			WebSocketSession session = itr.next();
			try {
				session.close(shutdownCloseStatus);
			} catch ( IOException e ) {
				// ignore
			} finally {
				itr.remove();
			}
		}
	}

	@Override
	public List<String> getSubProtocols() {
		return singletonList(WebSocketSubProtocol.OCPP_V16.getValue());
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		stats.increment(Stats.ChargePointsConnected);
		if ( !started ) {
			session.close(shutdownCloseStatus);
			return;
		}
		// save client session association
		ChargePointIdentity clientId = clientId(session);
		if ( clientId != null ) {
			clientSessions.put(new ChargePointSessionIdentity(clientId, session.getId()), session);
		}
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		stats.increment(Stats.ChargePointsDisconnected);
		// remove client session association
		ChargePointIdentity clientId = clientId(session);
		if ( clientId != null ) {
			clientSessions.remove(new ChargePointSessionIdentity(clientId, session.getId()));
		}
	}

	/**
	 * Get the charge point identity for a given session.
	 *
	 * @param session
	 *        the session
	 * @return the identity, or {@literal null} if not available
	 */
	protected ChargePointIdentity clientId(WebSocketSession session) {
		Object id = session.getAttributes().get(OcppWebSocketHandshakeInterceptor.CLIENT_ID_ATTR);
		return (id instanceof ChargePointIdentity ? (ChargePointIdentity) id : null);
	}

	/**
	 * Resolve an error code from a {@link RpcError} using the configured
	 * {@link ErrorCodeResolver}.
	 *
	 * @param error
	 *        the error to resolve
	 * @return the code, or {@literal null} it not resolvable
	 */
	protected ErrorCode errorCode(RpcError error) {
		return errorCodeResolver.errorCodeForRpcError(error);
	}

	/**
	 * Processing statistics.
	 */
	public static enum Stats {
		/** Charge points connected. */
		ChargePointsConnected,

		/** Charge points disconnected. */
		ChargePointsDisconnected,

		/** The count of active charge point connections. */
		ChargePointActiveConnections,

		/** Overall messages received. */
		MessagesReceived,

		/** Call messages received. */
		CallMessagesReceived,

		/** Call messages received but not supported. */
		CallMessagesReceivedNotSupported,

		/** Call error messages received. */
		CallErrorMessagesReceived,

		/** Call result messages received. */
		CallResultMessagesReceived,

		/** Call message receive failures. */
		CallMessageReceiveFailures,

		/** Call error message receive failures. */
		CallErrorMessageReceiveFailures,

		/** Call result message receive failures. */
		CallRersultMessageReceiveFailures,

		/** Overall messages sent. */
		MessagesSent,

		/** Call messages sent. */
		CallMessagesSent,

		/** Call error messages sent. */
		CallErrorMessagesSent,

		/** Call result messages sent. */
		CallResultMessagesSent,

		/** Call message send failures. */
		CallMessageSendFailures,

		/** Call error message send failures. */
		CallErrorMessageSendFailures,

		/** Call result message send failures. */
		CallResultMessageSendFailures,
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		stats.increment(Stats.MessagesReceived);
		final ChargePointIdentity clientId = clientId(session);
		log.trace("OCPP {} <<< {}", clientId, message.getPayload());
		JsonNode tree;
		try {
			tree = mapper.readTree(message.getPayload());
		} catch ( JsonProcessingException e ) {
			sendCallError(session, clientId, null, errorCode(RpcError.PayloadProtocolError),
					"Message malformed JSON.", null);
			return;
		}
		if ( tree.isArray() ) {
			JsonNode msgTypeNode = tree.path(0);
			JsonNode messageIdNode = tree.path(1);
			final String messageId = messageIdNode.isTextual() ? messageIdNode.textValue() : "NULL";
			if ( !msgTypeNode.isInt() ) {
				sendCallError(session, clientId, messageId, errorCode(RpcError.MessageSyntaxError),
						"Message type not provided.", null);
				return;
			}
			MessageType msgType;
			try {
				msgType = MessageType.forNumber(msgTypeNode.intValue());
			} catch ( IllegalArgumentException e ) {
				// OCPP spec says messages with unknown types should be ignored
				log.info("OCPP {} <<< Ignoring message with unknown type: {}", clientId,
						message.getPayload());
				return;
			}
			switch (msgType) {
				case Call:
					stats.increment(Stats.CallMessagesReceived);
					handleCallMessage(session, clientId, messageId, message, tree);
					break;

				case CallError:
					stats.increment(Stats.CallErrorMessagesReceived);
					handleCallErrorMessage(session, clientId, messageId, message, tree);
					break;

				case CallResult:
					stats.increment(Stats.CallResultMessagesReceived);
					handleCallResultMessage(session, clientId, messageId, message, tree);
					break;
			}
		}
	}

	/**
	 * Get a charge point action for an action name.
	 *
	 * @param name
	 *        the action name
	 * @return the action, or {@literal null} if not supported
	 */
	protected Action chargePointAction(String name) {
		for ( C action : chargePointActionClass.getEnumConstants() ) {
			if ( name.equals(action.getName()) ) {
				return action;
			}
		}
		return null;
	}

	/**
	 * Get a central system action for an action name.
	 *
	 * @param name
	 *        the action name
	 * @return the action, or {@literal null} if not supported
	 */
	protected Action centralSystemAction(String name) {
		for ( S action : centralSystemActionClass.getEnumConstants() ) {
			if ( name.equals(action.getName()) ) {
				return action;
			}
		}
		return null;
	}

	/**
	 * Process a request from a client Charge Point.
	 *
	 * <p>
	 * The message payload will be decoded using
	 * {@link #getCentralServiceActionPayloadDecoder()} and then passed to any
	 * configured action message processors so the processor's result (or error)
	 * can be returned to the client.
	 * </p>
	 *
	 * @param session
	 *        the session
	 * @param clientId
	 *        the Charge Point client ID
	 * @param messageId
	 *        the message ID
	 * @param message
	 *        the web socket message
	 * @param tree
	 *        the parsed JSON from the message
	 * @return {@literal true} if the message was processed
	 * @see #handleCallMessageResult(CallMessage, CallResultMessage,
	 *      CallErrorMessage)
	 */
	private boolean handleCallMessage(final WebSocketSession session, final ChargePointIdentity clientId,
			final String messageId, final TextMessage message, final JsonNode tree) {
		final JsonNode actionNode = tree.path(2);
		final Action action;
		try {
			action = actionNode.isTextual() ? centralSystemAction(actionNode.textValue()) : null;
			if ( action == null ) {
				if ( actionNode.isTextual() && !actionNode.textValue().isEmpty() ) {
					return sendCallError(session, clientId, messageId,
							errorCode(RpcError.ActionNotImplemented), "Unknown action.", null);
				}
				return sendCallError(session, clientId, messageId,
						errorCode(RpcError.PayloadSyntaxError),
						actionNode.isMissingNode() ? "Missing action." : "Malformed action.", null);
			}
			Object payload;
			try {
				payload = centralServiceActionPayloadDecoder.decodeActionPayload(action, false,
						tree.path(3));
			} catch ( SchemaValidationException e ) {
				return sendCallError(session, clientId, messageId,
						errorCode(RpcError.PayloadTypeConstraintViolation),
						"Schema validation error: " + e.getMessage(), null);
			} catch ( IOException e ) {
				return sendCallError(session, clientId, messageId,
						errorCode(RpcError.PayloadSyntaxError),
						"Error parsing payload: " + e.getMessage(), null);
			}

			pendingMessages.addPendingMessage(
					new PendingActionMessage(
							new BasicActionMessage<Object>(clientId, messageId, action, payload)),
					this::processNextPendingMessage);
			return true;
		} catch ( RuntimeException e ) {
			return sendCallError(session, clientId, messageId, errorCode(RpcError.InternalError),
					"Internal error: " + e.toString(), null);
		}
	}

	@Override
	public Set<ChargePointIdentity> availableChargePointsIds() {
		return clientSessions.keySet().stream().map(ChargePointSessionIdentity::getIdentity)
				.collect(Collectors.toSet());
	}

	@Override
	public boolean isChargePointAvailable(ChargePointIdentity clientId) {
		return clientSessions.ceilingKey(ChargePointSessionIdentity.boundaryKey(clientId)) != null;
	}

	@Override
	public boolean isMessageSupported(ActionMessage<?> message) {
		if ( message == null || !isChargePointAvailable(message.getClientId())
				|| message.getAction() == null ) {
			return false;
		}
		final String action = message.getAction().getName();
		if ( chargePointAction(action) != null ) {
			return true;
		}
		if ( centralSystemAction(action) != null ) {
			return true;
		}
		return false;
	}

	/**
	 * Get the session associated with a client ID.
	 *
	 * <p>
	 * Note that it is possible for multiple sessions to exist for the same
	 * client ID, so this method attempts to return the first open session
	 * available.
	 * </p>
	 *
	 * @param clientId
	 *        the client ID to find the session for
	 * @return the associated session, or {@literal null} if none available
	 */
	protected final WebSocketSession sessionForChargePoint(ChargePointIdentity clientId) {
		if ( clientId == null ) {
			return null;
		}
		Entry<ChargePointSessionIdentity, WebSocketSession> e = clientSessions
				.ceilingEntry(ChargePointSessionIdentity.boundaryKey(clientId));
		if ( e != null ) {
			// verify key for same client
			if ( !e.getKey().getIdentity().equals(clientId) ) {
				return null;
			}
			// verify session is open, and if not see if there is another, open session for same client
			for ( Entry<ChargePointSessionIdentity, WebSocketSession> e2 = clientSessions.higherEntry(
					e.getKey()); e2 != null; e2 = clientSessions.higherEntry(e2.getKey()) ) {
				if ( !e2.getKey().getIdentity().equals(clientId) ) {
					// moved to another client, so stop looking
					break;
				} else if ( e2.getValue().isOpen() ) {
					// found open session, so use that
					e = e2;
					break;
				}
			}
		}
		return (e != null ? e.getValue() : null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T, R> boolean sendMessageToChargePoint(ActionMessage<T> message,
			ActionMessageResultHandler<T, R> resultHandler) {
		final ChargePointIdentity clientId = message.getClientId();
		final WebSocketSession session = sessionForChargePoint(clientId);
		if ( session == null ) {
			log.debug("Client ID [{}] not available; discarding outbound message {}", clientId, message);
			return false;
		}
		// drop generics now for internal processing
		ActionMessage<Object> a = (ActionMessage<Object>) message;
		ActionMessageResultHandler<Object, Object> h = (ActionMessageResultHandler<Object, Object>) resultHandler;
		pendingMessages.addPendingMessage(new PendingActionMessage(a, h),
				this::processNextPendingMessage);
		return true;
	}

	/**
	 * Push a pending message to a Charge Point.
	 *
	 * @param msg
	 *        the pending message to send; this message is expected to have been
	 *        added to the pending message queue already
	 */
	private void sendCall(PendingActionMessage msg) {
		final ActionMessage<Object> message = msg.getMessage();
		final ActionMessageResultHandler<Object, Object> resultHandler = msg.getHandler();
		final WebSocketSession session = sessionForChargePoint(message.getClientId());
		if ( session == null ) {
			log.debug("Web socket not available for CallMessage {}; ignoring", message);
			return;
		}
		boolean sent = sendCall(session, message.getClientId(), message.getMessageId(),
				message.getAction(), message.getMessage());
		if ( !sent ) {
			// if there was an error, then we can immediately remove this message from
			// the pending queue and try the next message, as there won't be any response
			removePendingMessage(msg);
			ErrorCodeException err = new ErrorCodeException(errorCode(RpcError.SecurityError),
					"Client ID missing.");
			try {
				resultHandler.handleActionMessageResult(message, null, err);
			} catch ( Exception e ) {
				log.warn("Error handling OCPP CallError {}: {}", err, e.toString(), e);
				stats.increment(Stats.CallMessageSendFailures);
			} finally {
				processNextPendingMessage(message.getClientId());
			}
		}
	}

	/**
	 * Process the next pending message, if available.
	 *
	 * @param clientId
	 *        the ID of the client to process the next pending message
	 * @see #processNextPendingMessage(Deque)
	 */
	private void processNextPendingMessage(ChargePointIdentity clientId) {
		processNextPendingMessage(pendingMessages.pendingMessageQueue(clientId));
	}

	/**
	 * Process the next pending message, if available.
	 *
	 * @param q
	 *        the queue to process the next pending message
	 * @see #sendCall(PendingActionMessage)
	 * @see #processRequest(PendingActionMessage)
	 */
	private void processNextPendingMessage(Deque<PendingActionMessage> q) {
		PendingActionMessage next = null;
		synchronized ( q ) {
			PendingActionMessage msg = q.peek();
			if ( msg != null && msg.doProcess() ) {
				next = msg;
			}
		}
		if ( next != null ) {
			final PendingActionMessage m = next;
			executor.execute(() -> {
				if ( m.isOutbound() ) {
					sendCall(m);
				} else {
					processRequest(m);
				}
			});
		}
	}

	/**
	 * Process a CallError response to a Call message previously sent to a
	 * client.
	 *
	 * <p>
	 * If there is another message available in the pending message queue, that
	 * message will be sent to the client.
	 * </p>
	 *
	 * @param session
	 *        the session
	 * @param clientId
	 *        the Charge Point client ID
	 * @param messageId
	 *        the message ID
	 * @param message
	 *        the message
	 * @param tree
	 *        the JSON
	 */
	@SuppressWarnings("unchecked")
	private void handleCallErrorMessage(final WebSocketSession session,
			final ChargePointIdentity clientId, final String messageId, final TextMessage message,
			final JsonNode tree) {
		try {
			PendingActionMessage msg = pendingMessages.pollPendingMessage(clientId, messageId);
			if ( msg == null ) {
				log.warn(
						"OCPP {} <<< Original Call message {} not found; ignoring CallError message: {}",
						clientId, messageId, message.getPayload());
				return;
			}

			// stat update
			final long duration = System.currentTimeMillis() - msg.getDate();
			stats.add(actionErrorStatName(msg.getMessage().getAction()), duration);

			ErrorCode errorCode;
			try {
				errorCode = errorCodeResolver.errorCodeForName(tree.path(2).asText());
			} catch ( IllegalArgumentException e ) {
				log.warn("OCPP {} <<< Error code {} not valid; ignoring CallError message: {}", clientId,
						tree.path(2).asText(), message.getPayload());
				return;
			}
			Map<String, ?> details = null;
			try {
				details = mapper.treeToValue(tree.path(4), Map.class);
			} catch ( JsonProcessingException e ) {
				log.warn("OCPP {} <<< Error parsing CallError details object {}, ignoring: {}", clientId,
						tree.path(4), e.toString());
			}
			ErrorCodeException err = new ErrorCodeException(errorCode, details, tree.path(3).asText(),
					null);
			msg.getHandler().handleActionMessageResult(msg.getMessage(), null, err);
		} finally {
			processNextPendingMessage(clientId);
		}
	}

	/**
	 * Get an action statistic name.
	 *
	 * @param action
	 *        the action to get the name for
	 * @return the name to use
	 */
	protected String actionStatName(Action action) {
		return "Action" + (action != null ? action.getName() : "");
	}

	/**
	 * Get an action error statistic name.
	 *
	 * @param action
	 *        the action to get the name for
	 * @return the name to use
	 */
	protected String actionErrorStatName(Action action) {
		return actionStatName(action) + "Error";
	}

	/**
	 * Process a CallResult response to a Call message previously sent to a
	 * client.
	 *
	 *
	 * <p>
	 * If there is another message available in the pending message queue, that
	 * message will be sent to the client.
	 * </p>
	 *
	 * @param session
	 *        the session
	 * @param clientId
	 *        the Charge Point client ID
	 * @param messageId
	 *        the message ID
	 * @param message
	 *        the message
	 * @param tree
	 *        the JSON
	 */
	private void handleCallResultMessage(final WebSocketSession session,
			final ChargePointIdentity clientId, final String messageId, final TextMessage message,
			final JsonNode tree) {
		try {
			PendingActionMessage msg = pendingMessages.pollPendingMessage(clientId, messageId);
			if ( msg == null ) {
				log.warn(
						"OCPP {} <<< Original Call message {} not found; ignoring CallError message: {}",
						clientId, messageId, message.getPayload());
				return;
			}

			// stat update
			final long duration = System.currentTimeMillis() - msg.getDate();
			stats.add(actionStatName(msg.getMessage().getAction()), duration);

			ErrorCodeException err = null;
			Object payload = null;
			try {
				payload = chargePointActionPayloadDecoder
						.decodeActionPayload(msg.getMessage().getAction(), true, tree.path(2));
			} catch ( IOException e ) {
				err = new ErrorCodeException(errorCode(RpcError.PayloadSyntaxError), null,
						"Error parsing payload: " + e.getMessage(), e);
			}

			willProcessCallResponse(msg, payload, err);
			msg.getHandler().handleActionMessageResult(msg.getMessage(), payload, err);
		} finally {
			processNextPendingMessage(clientId);
		}
	}

	private boolean sendCall(final WebSocketSession session, final ChargePointIdentity clientId,
			final String messageId, final net.solarnetwork.ocpp.domain.Action action,
			final Object payload) {
		Object[] msg = new Object[] { MessageType.Call.getNumber(), messageId, action.getName(),
				payload };
		String json = null;
		try {
			json = mapper.writeValueAsString(msg);
			log.trace("OCPP {} >>> {}", clientId, json);
			session.sendMessage(new TextMessage(json));
			stats.increment(Stats.MessagesSent);
			stats.increment(Stats.CallMessagesSent);
			didSendCall(clientId, messageId, action, payload, json, null);
			return true;
		} catch ( IOException e ) {
			log.warn("OCPP {} >>> Communication error sending Call for message ID {}: {}", clientId,
					messageId, e.getMessage());
			stats.increment(Stats.CallMessageSendFailures);
			didSendCall(clientId, messageId, action, payload, json, e);
		}
		return false;
	}

	/**
	 * Extension point for after an OCPP call has been sent.
	 *
	 * @param clientId
	 *        the client ID
	 * @param messageId
	 *        the message ID
	 * @param action
	 *        the action
	 * @param payload
	 *        the payload
	 * @param json
	 *        the full JSON message sent
	 * @param exception
	 *        an exception, if an error occurred
	 * @since 1.4
	 */
	protected void didSendCall(final ChargePointIdentity clientId, final String messageId,
			final net.solarnetwork.ocpp.domain.Action action, final Object payload, final String json,
			final Throwable exception) {
		// extending classes can override
	}

	private boolean sendCallResult(final WebSocketSession session, final ChargePointIdentity clientId,
			final String messageId, final Object payload) {
		Object[] msg = new Object[] { MessageType.CallResult.getNumber(), messageId, payload };
		String json = null;
		try {
			json = mapper.writeValueAsString(msg);
			log.trace("OCPP {} >>> {}", clientId, json);
			session.sendMessage(new TextMessage(json));
			stats.increment(Stats.MessagesSent);
			stats.increment(Stats.CallResultMessagesSent);
			didSendCallResult(clientId, messageId, payload, json, null);
			return true;
		} catch ( IOException e ) {
			log.warn("OCPP {} >>> Communication error sending CallResult for message ID {}: {}",
					clientId, messageId, e.getMessage());
			stats.increment(Stats.CallResultMessageSendFailures);
			didSendCallResult(clientId, messageId, payload, json, e);
		}
		return false;
	}

	/**
	 * Extension point for after an OCPP call result has been sent.
	 *
	 * @param clientId
	 *        the client ID
	 * @param messageId
	 *        the message ID
	 * @param payload
	 *        the payload
	 * @param json
	 *        the full JSON message sent
	 * @param exception
	 *        an exception, if an error occurred
	 * @since 1.4
	 */
	protected void didSendCallResult(final ChargePointIdentity clientId, final String messageId,
			final Object payload, final String json, final Throwable exception) {
		// extending classes can override
	}

	private boolean sendCallError(final WebSocketSession session, final ChargePointIdentity clientId,
			final String messageId, final ErrorCode errorCode, final String errorDescription,
			final Map<String, ?> details) {
		Object[] msg = new Object[] { MessageType.CallError.getNumber(), messageId, errorCode.getName(),
				errorDescription, details != null ? details : Collections.emptyMap() };
		String json = null;
		try {
			json = mapper.writeValueAsString(msg);
			log.trace("OCPP {} >>> {}", clientId, json);
			session.sendMessage(new TextMessage(json));
			stats.increment(Stats.MessagesSent);
			stats.increment(Stats.CallErrorMessagesSent);
			didSendCallError(clientId, messageId, errorCode, errorDescription, details, json, null);
			return true;
		} catch ( IOException e ) {
			log.warn("OCPP {} >>> Communication error sending CallError for message ID {}: {}", clientId,
					messageId, e.getMessage());
			stats.increment(Stats.CallErrorMessageSendFailures);
			didSendCallError(clientId, messageId, errorCode, errorDescription, details, json, e);
		}
		return false;
	}

	/**
	 * Extension point for after an OCPP call error has been sent.
	 *
	 * @param clientId
	 *        the client ID
	 * @param messageId
	 *        the message ID
	 * @param errorCode
	 *        the error code
	 * @param errorDescription
	 *        the error description
	 * @param details
	 *        the error details
	 * @param json
	 *        the full JSON message sent
	 * @param exception
	 *        an exception, if an error occurred
	 * @since 1.4
	 */
	protected void didSendCallError(final ChargePointIdentity clientId, final String messageId,
			final ErrorCode errorCode, final String errorDescription, final Map<String, ?> details,
			final String json, Throwable exception) {
		// extending classes can override
	}

	/**
	 * Process a request from a Charge Point.
	 *
	 * <p>
	 * This method will pass the given pending message's {@link ActionMessage}
	 * to the available {@link ActionMessageProcessor} instances that support
	 * the message's {@link Action}. The first available processor's result (or
	 * error) will be passed back to the Charge Point.
	 * </p>
	 *
	 * @param msg
	 *        the message to process
	 */
	private void processRequest(PendingActionMessage msg) {
		final AtomicBoolean handled = new AtomicBoolean(false);
		try {
			final ActionMessage<Object> message = msg.getMessage();
			final Action action = message.getAction();
			final ChargePointIdentity clientId = message.getClientId();
			final String messageId = message.getMessageId();
			final WebSocketSession session = sessionForChargePoint(clientId);
			if ( session == null ) {
				log.debug("Web socket not available for client {}; ignoring ActionMessage {}", clientId,
						message);
				handled.set(true);
				return;
			}
			willProcessRequest(msg);
			final Set<ActionMessageProcessor<Object, Object>> procs = processors.get(action);
			if ( procs == null ) {
				sendCallError(session, clientId, messageId, errorCode(RpcError.ActionNotImplemented),
						"Action not supported.", null);
				handled.set(true);
				return;
			}
			ActionMessageResultHandler<Object, Object> handler = (am, result, error) -> {
				boolean shouldRespond = handled.compareAndSet(false, true);
				if ( !shouldRespond ) {
					return false;
				}
				try {
					if ( error == null ) {
						sendCallResult(session, clientId, messageId, result);
						final long duration = System.currentTimeMillis() - msg.getDate();
						stats.add(actionStatName(action), duration);
					} else {
						ErrorCode errorCode = null;
						String errorDescription = null;
						Map<String, ?> errorDetails = null;
						if ( error instanceof ErrorHolder ) {
							errorCode = ((ErrorHolder) error).getErrorCode();
							errorDescription = ((ErrorHolder) error).getErrorDescription();
							errorDetails = ((ErrorHolder) error).getErrorDetails();
						}
						if ( errorCode == null ) {
							errorCode = errorCode(RpcError.InternalError);
						}
						sendCallError(session, clientId, messageId, errorCode, errorDescription,
								errorDetails);

						// track stats
						final long duration = System.currentTimeMillis() - msg.getDate();
						stats.add(actionErrorStatName(action), duration);
					}
				} finally {
					removePendingMessage(msg);
				}
				return true;
			};
			boolean processed = false;
			for ( ActionMessageProcessor<Object, Object> p : procs ) {
				try {
					if ( p.isMessageSupported(message) ) {
						processed = true;
						p.processActionMessage(message, handler);
					}
				} catch ( AuthorizationException e ) {
					if ( handled.compareAndSet(false, true) ) {
						sendCallError(session, clientId, messageId, errorCode(RpcError.SecurityError),
								"Authorization error handling action.", null);
					}
				} catch ( Throwable t ) {
					if ( handled.compareAndSet(false, true) ) {
						stats.increment(Stats.CallMessageReceiveFailures);
						sendCallError(session, clientId, messageId, errorCode(RpcError.InternalError),
								"Error handling action.", null);
					}
				}
			}
			if ( !processed ) {
				stats.increment(Stats.CallMessagesReceivedNotSupported);
				sendCallError(session, clientId, messageId, errorCode(RpcError.InternalError),
						"Action not supported.", null);
				handled.set(true);
			}
		} finally {
			if ( handled.get() ) {
				removePendingMessage(msg);
			}
		}
	}

	/**
	 * Extension point for before an action message is to be processed.
	 *
	 * @param msg
	 *        the message
	 */
	protected void willProcessRequest(PendingActionMessage msg) {
		// extending classes can override
	}

	private void removePendingMessage(PendingActionMessage msg) {
		ChargePointIdentity clientId = msg.getMessage().getClientId();
		Deque<PendingActionMessage> q = pendingMessages.pendingMessageQueue(clientId);
		synchronized ( q ) {
			q.removeFirstOccurrence(msg);
			processNextPendingMessage(q);
		}
	}

	/**
	 * Extension point for before a call response is processed.
	 *
	 * @param msg
	 *        the message
	 * @param payload
	 *        the payload
	 * @param exception
	 *        an exception, if an error occurred
	 * @since 1.4
	 */
	protected void willProcessCallResponse(PendingActionMessage msg, final Object payload,
			final Throwable exception) {
		// extending classes can override
	}

	@Override
	public String getPingTestId() {
		return stats.getUid() != null ? stats.getUid() : getClass().getName();
	}

	@Override
	public String getPingTestName() {
		return stats.getDisplayName() != null ? stats.getDisplayName() : getClass().getSimpleName();
	}

	@Override
	public long getPingTestMaximumExecutionMilliseconds() {
		return 1000;
	}

	@Override
	public Result performPingTest() throws Exception {
		Map<String, Number> statMap = stats.allStatistics();
		statMap.put(Stats.ChargePointActiveConnections.name(), (long) availableChargePointsIds().size());
		return new PingTestResult(true, "WebSocket processor " + (isStarted() ? "started" : "stopped."),
				statMap);
	}

	/**
	 * Get the configured action payload decoder for Central Service messages.
	 *
	 * @return the decoder, never {@literal null}
	 */
	public ActionPayloadDecoder getCentralServiceActionPayloadDecoder() {
		return centralServiceActionPayloadDecoder;
	}

	/**
	 * Set the action payload decoder for Central Service messages.
	 *
	 * @param centralServiceActionPayloadDecoder
	 *        the decoder to use
	 * @throws IllegalArgumentException
	 *         if {@code centralServiceActionPayloadDecoder} is {@literal null}
	 */
	public void setCentralServiceActionPayloadDecoder(
			ActionPayloadDecoder centralServiceActionPayloadDecoder) {
		this.centralServiceActionPayloadDecoder = requireNonNullArgument(
				centralServiceActionPayloadDecoder, "centralServiceActionPayloadDecoder");
	}

	/**
	 * Get the configured action payload decoder for Charge Point messages.
	 *
	 * @return the decoder, never {@literal null}
	 */
	public ActionPayloadDecoder getChargePointActionPayloadDecoder() {
		return chargePointActionPayloadDecoder;
	}

	/**
	 * Set the action payload decoder for Charge Point messages.
	 *
	 * @param chargePointActionPayloadDecoder
	 *        the decoder to use
	 * @throws IllegalArgumentException
	 *         if {@code chargePointActionPayloadDecoder} is {@literal null}
	 */
	public void setChargePointActionPayloadDecoder(
			ActionPayloadDecoder chargePointActionPayloadDecoder) {
		this.chargePointActionPayloadDecoder = requireNonNullArgument(chargePointActionPayloadDecoder,
				"chargePointActionPayloadDecoder");
	}

	/**
	 * Add an action message processor.
	 *
	 * <p>
	 * Once added, messages for its supported actions will be routed to it.
	 * </p>
	 *
	 * @param processor
	 *        to processor to add; {@literal null} will be ignored
	 */
	@SuppressWarnings("unchecked")
	public void addActionMessageProcessor(ActionMessageProcessor<?, ?> processor) {
		if ( processor == null ) {
			return;
		}
		for ( Action action : processor.getSupportedActions() ) {
			processors.compute(action, (k, v) -> {
				Set<ActionMessageProcessor<Object, Object>> procs = v;
				if ( procs == null ) {
					procs = new LinkedHashSet<>();
				}
				procs.add((ActionMessageProcessor<Object, Object>) processor);
				return procs;
			});
		}
	}

	/**
	 * Remove an action message processor.
	 *
	 * <p>
	 * Once removed, messages will no longer be routed to it.
	 * </p>
	 *
	 * @param processor
	 *        the processor to remove; {@literal null} will be ignored
	 */
	public void removeActionMessageProcessor(ActionMessageProcessor<?, ?> processor) {
		if ( processor == null ) {
			return;
		}
		for ( Set<ActionMessageProcessor<Object, Object>> procs : processors.values() ) {
			procs.remove(processor);
		}
	}

	/**
	 * Get the object mapper.
	 *
	 * @return the object mapper, never {@literal null}
	 */
	public ObjectMapper getObjectMapper() {
		return mapper;
	}

	/**
	 * Get the task scheduler.
	 *
	 * @return the task scheduler
	 */
	public TaskScheduler getTaskScheduler() {
		return taskScheduler;
	}

	/**
	 * Set the task scheduler.
	 *
	 * <p>
	 * This scheduler is required for automatic maintenance tasks to run. If a
	 * scheduler is not configured, this handler will still function but some
	 * functions like automatically removing unhandled expired tasks will not
	 * occur.
	 * </p>
	 *
	 * @param taskScheduler
	 *        the task scheduler to set
	 */
	public void setTaskScheduler(TaskScheduler taskScheduler) {
		this.taskScheduler = taskScheduler;
	}

	/**
	 * Get the timeout to expire pending messages that have not received a
	 * response.
	 *
	 * @return the timeout, in milliseconds; defaults to
	 *         {@link #DEFAULT_PENDING_MESSAGE_TIMEOUT}
	 */
	public long getPendingMessageTimeout() {
		return pendingMessageTimeout;
	}

	/**
	 * Set the timeout to expire pending messages that have not received a
	 * response.
	 *
	 * @param pendingMessageTimeout
	 *        the timeout to set, in milliseconds
	 */
	public void setPendingMessageTimeout(long pendingMessageTimeout) {
		this.pendingMessageTimeout = pendingMessageTimeout;
	}

	/**
	 * Get the frequency at which to emit PING frames.
	 *
	 * @return the frequency in seconds, or {@literal 0} to disable; defaults to
	 *         {@link #DEFAULT_PING_FREQUENCY_SECS}
	 */
	public int getPingFrequency() {
		return pingFrequencySecs;
	}

	/**
	 * Set the frequency at which to emit PING frames.
	 *
	 * @param pingFrequencySecs
	 *        the frequency in seconds, or {@literal 0} to disable
	 */
	public void setPingFrequency(int pingFrequencySecs) {
		this.pingFrequencySecs = pingFrequencySecs;
	}

	/**
	 * Get the close status to issue to active sessions when {@link #shutdown()}
	 * is invoked.
	 *
	 * @return the close status; defaults to
	 *         {@link CloseStatus#SERVICE_RESTARTED}.
	 * @since 1.6
	 */
	public CloseStatus getShutdownCloseStatus() {
		return shutdownCloseStatus;
	}

	/**
	 * Set the close status to issue to active sessions when {@link #shutdown()}
	 * is invoked.
	 *
	 * @param shutdownCloseStatus
	 *        the status to use
	 * @since 1.6
	 */
	public void setShutdownCloseStatus(CloseStatus shutdownCloseStatus) {
		this.shutdownCloseStatus = shutdownCloseStatus;
	}

}

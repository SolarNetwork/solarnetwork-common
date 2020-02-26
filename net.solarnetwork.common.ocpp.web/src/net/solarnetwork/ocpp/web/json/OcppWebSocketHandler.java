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
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.SubProtocolCapable;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import net.solarnetwork.ocpp.domain.ActionMessage;
import net.solarnetwork.ocpp.domain.BasicActionMessage;
import net.solarnetwork.ocpp.domain.ChargePointIdentity;
import net.solarnetwork.ocpp.domain.PendingActionMessage;
import net.solarnetwork.ocpp.service.ActionMessageProcessor;
import net.solarnetwork.ocpp.service.ActionMessageQueue;
import net.solarnetwork.ocpp.service.ActionMessageResultHandler;
import net.solarnetwork.ocpp.service.ChargePointBroker;
import net.solarnetwork.ocpp.service.SimpleActionMessageQueue;
import net.solarnetwork.settings.SettingsChangeObserver;
import ocpp.domain.Action;
import ocpp.domain.ErrorCode;
import ocpp.domain.ErrorCodeException;
import ocpp.domain.ErrorHolder;
import ocpp.domain.SchemaValidationException;
import ocpp.json.ActionPayloadDecoder;
import ocpp.json.CallErrorMessage;
import ocpp.json.CallMessage;
import ocpp.json.CallResultMessage;
import ocpp.json.MessageType;
import ocpp.json.WebSocketSubProtocol;
import ocpp.v16.ActionErrorCode;
import ocpp.v16.CentralSystemAction;
import ocpp.v16.ChargePointAction;
import ocpp.v16.cp.json.ChargePointActionPayloadDecoder;
import ocpp.v16.cs.json.CentralServiceActionPayloadDecoder;

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
 * @author matt
 * @version 1.0
 */
public class OcppWebSocketHandler extends AbstractWebSocketHandler
		implements WebSocketHandler, SubProtocolCapable, SettingsChangeObserver, ChargePointBroker {

	/** The default {@code pendingMessageTimeout} property. */
	public static final long DEFAULT_PENDING_MESSAGE_TIMEOUT = TimeUnit.SECONDS.toMillis(120);

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final AsyncTaskExecutor executor;
	private final ConcurrentMap<Action, Set<ActionMessageProcessor<Object, Object>>> processors;
	private final ConcurrentMap<ChargePointIdentity, WebSocketSession> clientSessions;
	private final ActionMessageQueue pendingMessages;
	private final ObjectMapper mapper;
	private TaskScheduler taskScheduler;
	private ActionPayloadDecoder centralServiceActionPayloadDecoder;
	private ActionPayloadDecoder chargePointActionPayloadDecoder;
	private long pendingMessageTimeout = DEFAULT_PENDING_MESSAGE_TIMEOUT;

	private Future<?> startupTask;
	private ScheduledFuture<?> pendingTimeoutChore;

	private static ObjectMapper defaultObjectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JaxbAnnotationModule());
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		return mapper;
	}

	/**
	 * Constructor.
	 * 
	 * <p>
	 * Default {@link ObjectMapper} and
	 * {@link CentralServiceActionPayloadDecoder} and
	 * {@link ChargePointActionPayloadDecoder} instances will be created. An
	 * in-memory queue will be used for pending messages.
	 * </p>
	 * 
	 * @param executor
	 *        an executor for tasks
	 */
	public OcppWebSocketHandler(AsyncTaskExecutor executor) {
		super();
		this.executor = executor;
		this.processors = new ConcurrentHashMap<>(16, 0.9f, 1);
		this.clientSessions = new ConcurrentHashMap<>(8, 0.7f, 2);
		this.pendingMessages = new SimpleActionMessageQueue();
		this.mapper = defaultObjectMapper();
		this.centralServiceActionPayloadDecoder = new CentralServiceActionPayloadDecoder(mapper);
		this.chargePointActionPayloadDecoder = new ChargePointActionPayloadDecoder(mapper);
	}

	/**
	 * Constructor.
	 * 
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
	public OcppWebSocketHandler(AsyncTaskExecutor executor, ObjectMapper mapper,
			ActionMessageQueue pendingMessageQueue,
			ActionPayloadDecoder centralServiceActionPayloadDecoder,
			ActionPayloadDecoder chargePointActionPayloadDecoder) {
		super();
		this.processors = new ConcurrentHashMap<>(16, 0.9f, 1);
		this.clientSessions = new ConcurrentHashMap<>(8, 0.7f, 2);
		if ( executor == null ) {
			throw new IllegalArgumentException("The executor parameter must not be null.");
		}
		this.executor = executor;
		if ( mapper == null ) {
			throw new IllegalArgumentException("The mapper parameter must not be null.");
		}
		this.mapper = mapper;
		if ( pendingMessageQueue == null ) {
			throw new IllegalArgumentException("The pendingMessageQueue parameter must not be null.");
		}
		this.pendingMessages = pendingMessageQueue;
		setCentralServiceActionPayloadDecoder(centralServiceActionPayloadDecoder);
		setChargePointActionPayloadDecoder(chargePointActionPayloadDecoder);
	}

	/**
	 * Call once this service is configured.
	 */
	public synchronized void startup() {
		configurationChanged(null);
	}

	/**
	 * Call once this service is no longer needed, to free up internal
	 * resources.
	 */
	public synchronized void shutdown() {
		if ( startupTask != null ) {
			startupTask.cancel(true);
		}
		unshceduleChores();
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
	}

	private synchronized void unshceduleChores() {
		if ( pendingTimeoutChore != null ) {
			pendingTimeoutChore.cancel(true);
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

	@Override
	public List<String> getSubProtocols() {
		return singletonList(WebSocketSubProtocol.OCPP_V16.getValue());
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		// save client session association
		ChargePointIdentity clientId = clientId(session);
		if ( clientId != null ) {
			clientSessions.put(clientId, session);
		}
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		// remove client session association
		ChargePointIdentity clientId = clientId(session);
		if ( clientId != null ) {
			clientSessions.remove(clientId, session);
		}
	}

	private ChargePointIdentity clientId(WebSocketSession session) {
		Object id = session.getAttributes().get(OcppWebSocketHandshakeInterceptor.CLIENT_ID_ATTR);
		return (id instanceof ChargePointIdentity ? (ChargePointIdentity) id : null);
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		final ChargePointIdentity clientId = clientId(session);
		log.trace("OCPP {} <<< {}", clientId, message.getPayload());
		JsonNode tree;
		try {
			tree = mapper.readTree(message.getPayload());
		} catch ( JsonProcessingException e ) {
			sendCallError(session, clientId, null, ActionErrorCode.ProtocolError,
					"Message malformed JSON.", null);
			return;
		}
		if ( tree.isArray() ) {
			JsonNode msgTypeNode = tree.path(0);
			JsonNode messageIdNode = tree.path(1);
			final String messageId = messageIdNode.isTextual() ? messageIdNode.textValue() : "NULL";
			if ( !msgTypeNode.isInt() ) {
				sendCallError(session, clientId, messageId, ActionErrorCode.FormationViolation,
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
					handleCallMessage(session, clientId, messageId, message, tree);
					break;

				case CallError:
					handleCallErrorMessage(session, clientId, messageId, message, tree);
					break;

				case CallResult:
					handleCallResultMessage(session, clientId, messageId, message, tree);
					break;
			}
		}
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
		final CentralSystemAction action;
		try {
			action = actionNode.isTextual() ? CentralSystemAction.valueOf(actionNode.textValue()) : null;
			if ( action == null ) {
				return sendCallError(session, clientId, messageId, ActionErrorCode.FormationViolation,
						actionNode.isMissingNode() ? "Missing action." : "Malformed action.", null);
			}
			Object payload;
			try {
				payload = centralServiceActionPayloadDecoder.decodeActionPayload(action, false,
						tree.path(3));
			} catch ( SchemaValidationException e ) {
				return sendCallError(session, clientId, messageId,
						ActionErrorCode.TypeConstraintViolation,
						"Schema validation error: " + e.getMessage(), null);
			} catch ( IOException e ) {
				return sendCallError(session, clientId, messageId, ActionErrorCode.FormationViolation,
						"Error parsing payload: " + e.getMessage(), null);
			}

			pendingMessages.addPendingMessage(
					new PendingActionMessage(
							new BasicActionMessage<Object>(clientId, messageId, action, payload)),
					this::processNextPendingMessage);
			return true;
		} catch ( IllegalArgumentException e ) {
			return sendCallError(session, clientId, messageId, ActionErrorCode.NotImplemented,
					"Unknown action.", null);
		} catch ( RuntimeException e ) {
			return sendCallError(session, clientId, messageId, ActionErrorCode.InternalError,
					"Internal error: " + e.toString(), null);
		}
	}

	@Override
	public Set<ChargePointIdentity> availableChargePointsIds() {
		return clientSessions.keySet();
	}

	@Override
	public boolean isChargePointAvailable(ChargePointIdentity clientId) {
		return clientSessions.containsKey(clientId);
	}

	@Override
	public boolean isMessageSupported(ActionMessage<?> message) {
		if ( message == null || !isChargePointAvailable(message.getClientId())
				|| message.getAction() == null ) {
			return false;
		}
		final String action = message.getAction().getName();
		try {
			ChargePointAction.valueOf(action);
			return true;
		} catch ( IllegalArgumentException e ) {
			try {
				CentralSystemAction.valueOf(action);
				return true;
			} catch ( IllegalArgumentException e2 ) {
				// ignore
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T, R> boolean sendMessageToChargePoint(ActionMessage<T> message,
			ActionMessageResultHandler<T, R> resultHandler) {
		final ChargePointIdentity clientId = message.getClientId();
		if ( clientId == null || !clientSessions.containsKey(clientId) ) {
			log.debug("Client ID [{}] not available; ignoring message {}", clientId, message);
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
		WebSocketSession session = clientSessions.get(message.getClientId());
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
			ErrorCodeException err = new ErrorCodeException(ActionErrorCode.SecurityError,
					"Client ID missing.");
			try {
				resultHandler.handleActionMessageResult(message, null, err);
			} catch ( Exception e ) {
				log.warn("Error handling OCPP CallError {}: {}", err, e.toString(), e);
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
			ActionErrorCode errorCode;
			try {
				errorCode = ActionErrorCode.valueOf(tree.path(2).asText());
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

			ErrorCodeException err = null;
			Object payload = null;
			try {
				payload = chargePointActionPayloadDecoder
						.decodeActionPayload(msg.getMessage().getAction(), true, tree.path(2));
			} catch ( IOException e ) {
				err = new ErrorCodeException(ActionErrorCode.FormationViolation, null,
						"Error parsing payload: " + e.getMessage(), e);
			}

			msg.getHandler().handleActionMessageResult(msg.getMessage(), payload, err);
		} finally {
			processNextPendingMessage(clientId);
		}
	}

	private boolean sendCall(final WebSocketSession session, final ChargePointIdentity clientId,
			final String messageId, final ocpp.domain.Action action, final Object payload) {
		Object[] msg = new Object[] { MessageType.Call.getNumber(), messageId, action.getName(),
				payload };
		try {
			String json = mapper.writeValueAsString(msg);
			log.trace("OCPP {} >>> {}", clientId, json);
			session.sendMessage(new TextMessage(json));
			return true;
		} catch ( IOException e ) {
			log.warn("OCPP {} >>> Communication error sending Call for message ID {}: {}", clientId,
					messageId, e.getMessage());
		}
		return false;
	}

	private boolean sendCallResult(final WebSocketSession session, final ChargePointIdentity clientId,
			final String messageId, final Object payload) {
		Object[] msg = new Object[] { MessageType.CallResult.getNumber(), messageId, payload };
		try {
			String json = mapper.writeValueAsString(msg);
			log.trace("OCPP {} >>> {}", clientId, json);
			session.sendMessage(new TextMessage(json));
			return true;
		} catch ( IOException e ) {
			log.warn("OCPP {} >>> Communication error sending CallResult for message ID {}: {}",
					clientId, messageId, e.getMessage());
		}
		return false;
	}

	private boolean sendCallError(final WebSocketSession session, final ChargePointIdentity clientId,
			final String messageId, final ErrorCode errorCode, final String errorDescription,
			final Map<String, ?> details) {
		Object[] msg = new Object[] { MessageType.CallError.getNumber(), messageId, errorCode.getName(),
				errorDescription, details != null ? details : Collections.emptyMap() };
		try {
			String json = mapper.writeValueAsString(msg);
			log.trace("OCPP {} >>> {}", clientId, json);
			session.sendMessage(new TextMessage(json));
			return true;
		} catch ( IOException e ) {
			log.warn("OCPP {} >>> Communication error sending CallError for message ID {}: {}", clientId,
					messageId, e.getMessage());
		}
		return false;
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
	 */
	private void processRequest(PendingActionMessage msg) {
		final AtomicBoolean handled = new AtomicBoolean(false);
		try {
			final ActionMessage<Object> message = msg.getMessage();
			final Action action = message.getAction();
			final ChargePointIdentity clientId = message.getClientId();
			final String messageId = message.getMessageId();
			final WebSocketSession session = clientSessions.get(clientId);
			if ( session == null ) {
				log.debug("Web socket not available for client {}; ignoring ActionMessage {}", clientId,
						message);
				handled.set(true);
				return;
			}
			final Set<ActionMessageProcessor<Object, Object>> procs = processors.get(action);
			if ( procs == null ) {
				sendCallError(session, clientId, messageId, ActionErrorCode.NotImplemented,
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
							errorCode = ActionErrorCode.InternalError;
						}
						sendCallError(session, clientId, messageId, errorCode, errorDescription,
								errorDetails);
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
				} catch ( Throwable t ) {
					if ( handled.compareAndSet(false, true) ) {
						sendCallError(session, clientId, messageId, ActionErrorCode.InternalError,
								"Error handling action.", null);
					}
				}
			}
			if ( !processed ) {
				sendCallError(session, clientId, messageId, ActionErrorCode.NotImplemented,
						"Action not supported.", null);
				handled.set(true);
			}
		} finally {
			if ( handled.get() ) {
				removePendingMessage(msg);
			}
		}
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
		if ( centralServiceActionPayloadDecoder == null ) {
			throw new IllegalArgumentException(
					"The centralServiceActionPayloadDecoder parameter must not be null.");
		}
		this.centralServiceActionPayloadDecoder = centralServiceActionPayloadDecoder;
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
		if ( chargePointActionPayloadDecoder == null ) {
			throw new IllegalArgumentException(
					"The chargePointActionPayloadDecoder parameter must not be null.");
		}
		this.chargePointActionPayloadDecoder = chargePointActionPayloadDecoder;
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
					procs = new CopyOnWriteArraySet<>();
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

}

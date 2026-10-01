/* ==================================================================
 * OcppWebSocketHandlerV16Tests.java - 28/10/2020 4:26:33 pm
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

package net.solarnetwork.ocpp.web.jakarta.json.test;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.expect;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import net.solarnetwork.ocpp.domain.ActionMessage;
import net.solarnetwork.ocpp.domain.BasicActionMessage;
import net.solarnetwork.ocpp.domain.ChargePointIdentity;
import net.solarnetwork.ocpp.service.ActionMessageResultHandler;
import net.solarnetwork.ocpp.v16.jakarta.CentralSystemAction;
import net.solarnetwork.ocpp.v16.jakarta.ChargePointAction;
import net.solarnetwork.ocpp.v16.jakarta.ErrorCodeResolver;
import net.solarnetwork.ocpp.v16.jakarta.cp.json.ChargePointActionPayloadDecoder;
import net.solarnetwork.ocpp.v16.jakarta.cs.HeartbeatProcessor;
import net.solarnetwork.ocpp.v16.jakarta.cs.json.CentralServiceActionPayloadDecoder;
import net.solarnetwork.ocpp.web.jakarta.json.OcppWebSocketHandler;
import net.solarnetwork.ocpp.web.jakarta.json.OcppWebSocketHandshakeInterceptor;
import net.solarnetwork.security.AuthorizationException;
import net.solarnetwork.test.CallingThreadExecutorService;
import ocpp.v16.jakarta.cs.HeartbeatRequest;
import ocpp.v16.jakarta.cs.HeartbeatResponse;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.util.StdDateFormat;
import tools.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationModule;

/**
 * Test cases for the {@link OcppWebSocketHandler} class.
 *
 * @author matt
 * @version 1.2
 */
public class OcppWebSocketHandlerV16Tests {

	private WebSocketSession session;
	private OcppWebSocketHandler<ChargePointAction, CentralSystemAction> handler;

	private static ObjectMapper defaultObjectMapper() {
		return JsonMapper.builder().addModule(new JakartaXmlBindAnnotationModule())
				.configure(DateTimeFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS, false)
				.defaultDateFormat(new StdDateFormat().withColonInTimeZone(true))
				.changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(Include.NON_NULL))
				.changeDefaultPropertyInclusion(incl -> incl.withContentInclusion(Include.NON_NULL))
				.build();
	}

	@Before
	public void setup() {
		ObjectMapper mapper = defaultObjectMapper();
		session = EasyMock.createMock(WebSocketSession.class);
		handler = new OcppWebSocketHandler<>(ChargePointAction.class, CentralSystemAction.class,
				new ErrorCodeResolver(), new TaskExecutorAdapter(new CallingThreadExecutorService()),
				mapper);
		handler.setChargePointActionPayloadDecoder(new ChargePointActionPayloadDecoder(mapper));
		handler.setCentralServiceActionPayloadDecoder(new CentralServiceActionPayloadDecoder(mapper));
	}

	private void replayAll() {
		EasyMock.replay(session);
	}

	@After
	public void teardown() {
		EasyMock.verify(session);
	}

	@Test
	public void heartbeatRequest() throws Exception {
		// GIVEN
		final ChargePointIdentity cpIdent = new ChargePointIdentity("foo", "user");
		final Map<String, Object> sessionAttributes = Collections
				.singletonMap(OcppWebSocketHandshakeInterceptor.CLIENT_ID_ATTR, cpIdent);
		final String sessionId = UUID.randomUUID().toString();
		expect(session.getId()).andReturn(sessionId).anyTimes();
		expect(session.getAttributes()).andReturn(sessionAttributes).anyTimes();
		handler.addActionMessageProcessor(new HeartbeatProcessor());

		// send HeartbeatResponse
		Capture<TextMessage> outMessageCaptor = Capture.newInstance();
		session.sendMessage(capture(outMessageCaptor));

		// WHEN
		replayAll();
		handler.startup(false);
		handler.afterConnectionEstablished(session);
		TextMessage msg = new TextMessage("[2,\"1603881305171\",\"Heartbeat\",{}]");
		handler.handleMessage(session, msg);

		// THEN
		TextMessage outMsg = outMessageCaptor.getValue();
		assertThat("Heartbeat response message sent", outMsg, notNullValue());
		assertThat("Heartbeat response message content", outMsg.getPayload()
				.matches("\\[3,\"1603881305171\",\\{\"currentTime\":\"[^\"]+\"\\}\\]"), equalTo(true));
	}

	@Test
	public void orderedProcessors() throws Exception {
		// GIVEN
		final ChargePointIdentity cpIdent = new ChargePointIdentity("foo", "user");
		final Map<String, Object> sessionAttributes = Collections
				.singletonMap(OcppWebSocketHandshakeInterceptor.CLIENT_ID_ATTR, cpIdent);
		final String sessionId = UUID.randomUUID().toString();
		expect(session.getId()).andReturn(sessionId).anyTimes();
		expect(session.getAttributes()).andReturn(sessionAttributes).anyTimes();
		handler.addActionMessageProcessor(new HeartbeatProcessor());
		handler.addActionMessageProcessor(new HeartbeatProcessor() {

			@Override
			public void processActionMessage(ActionMessage<HeartbeatRequest> message,
					ActionMessageResultHandler<HeartbeatRequest, HeartbeatResponse> resultHandler) {
				resultHandler.handleActionMessageResult(message, null, new RuntimeException("Nope!"));
			}

		});

		// send HeartbeatResponse
		Capture<TextMessage> outMessageCaptor = Capture.newInstance();
		session.sendMessage(capture(outMessageCaptor));

		// WHEN
		replayAll();
		handler.startup(false);
		handler.afterConnectionEstablished(session);
		TextMessage msg = new TextMessage("[2,\"1603881305171\",\"Heartbeat\",{}]");
		handler.handleMessage(session, msg);

		// THEN
		TextMessage outMsg = outMessageCaptor.getValue();
		// @formatter:off
		then(outMsg)
			.as("Heartbeat response message sent")
			.isNotNull()
			.extracting(TextMessage::getPayload, STRING)
			.matches("\\[3,\"1603881305171\",\\{\"currentTime\":\"[^\"]+\"\\}\\]")
			;
		// @formatter:on
	}

	@Test
	public void authorizationException() throws Exception {
		final ChargePointIdentity cpIdent = new ChargePointIdentity("foo", "user");
		final Map<String, Object> sessionAttributes = Collections
				.singletonMap(OcppWebSocketHandshakeInterceptor.CLIENT_ID_ATTR, cpIdent);
		final String sessionId = UUID.randomUUID().toString();
		expect(session.getId()).andReturn(sessionId).anyTimes();
		expect(session.getAttributes()).andReturn(sessionAttributes).anyTimes();
		handler.addActionMessageProcessor(new HeartbeatProcessor() {

			@Override
			public void processActionMessage(ActionMessage<HeartbeatRequest> message,
					ActionMessageResultHandler<HeartbeatRequest, HeartbeatResponse> resultHandler) {
				throw new AuthorizationException(AuthorizationException.Reason.ACCESS_DENIED, cpIdent);
			}

		});

		// send HeartbeatResponse
		Capture<TextMessage> outMessageCaptor = Capture.newInstance();
		session.sendMessage(capture(outMessageCaptor));

		// WHEN
		replayAll();
		handler.startup(false);
		handler.afterConnectionEstablished(session);
		TextMessage msg = new TextMessage("[2,\"1603881305171\",\"Heartbeat\",{}]");
		handler.handleMessage(session, msg);

		// THEN
		TextMessage outMsg = outMessageCaptor.getValue();
		assertThat("Heartbeat response message sent", outMsg, notNullValue());
		assertThat("Heartbeat response message content", outMsg.getPayload(), is(equalTo(
				"[4,\"1603881305171\",\"SecurityError\",\"Authorization error handling action.\",{}]")));
	}

	@Test
	public void closeOnShutdown() throws Exception {
		// GIVEN
		final ChargePointIdentity cpIdent = new ChargePointIdentity("foo", "user");
		final Map<String, Object> sessionAttributes = Collections
				.singletonMap(OcppWebSocketHandshakeInterceptor.CLIENT_ID_ATTR, cpIdent);
		final String sessionId = UUID.randomUUID().toString();
		expect(session.getId()).andReturn(sessionId).anyTimes();
		expect(session.getAttributes()).andReturn(sessionAttributes).anyTimes();
		handler.addActionMessageProcessor(new HeartbeatProcessor());

		// send HeartbeatResponse
		Capture<TextMessage> outMessageCaptor = Capture.newInstance();
		session.sendMessage(capture(outMessageCaptor));

		// close on shutdown
		session.close(handler.getShutdownCloseStatus());

		// WHEN
		replayAll();
		handler.startup(false);
		handler.afterConnectionEstablished(session);
		TextMessage msg = new TextMessage("[2,\"1603881305171\",\"Heartbeat\",{}]");
		handler.handleMessage(session, msg);
		handler.shutdown();

		// THEN
		TextMessage outMsg = outMessageCaptor.getValue();
		assertThat("Heartbeat response message sent", outMsg, notNullValue());
		assertThat("Heartbeat response message content", outMsg.getPayload()
				.matches("\\[3,\"1603881305171\",\\{\"currentTime\":\"[^\"]+\"\\}\\]"), equalTo(true));
	}

	@Test
	public void partialMessageRequest() throws Exception {
		// GIVEN
		final ChargePointIdentity cpIdent = new ChargePointIdentity("foo", "user");
		final Map<String, Object> sessionAttributes = new HashMap<>(2);
		sessionAttributes.put(OcppWebSocketHandshakeInterceptor.CLIENT_ID_ATTR, cpIdent);
		final String sessionId = UUID.randomUUID().toString();
		expect(session.getId()).andReturn(sessionId).anyTimes();
		expect(session.getAttributes()).andReturn(sessionAttributes).anyTimes();
		handler.addActionMessageProcessor(new HeartbeatProcessor());
		handler.setPartialMessageMaximumSize(1024);

		// send HeartbeatResponse
		Capture<TextMessage> outMessageCaptor = Capture.newInstance();
		session.sendMessage(capture(outMessageCaptor));

		// WHEN
		replayAll();
		handler.startup(false);
		handler.afterConnectionEstablished(session);
		TextMessage msg1 = new TextMessage("[2,\"1603881305171\",", false);
		TextMessage msg2 = new TextMessage("\"Heartbeat\",", false);
		TextMessage msg3 = new TextMessage("{}]", true);
		handler.handleMessage(session, msg1);
		handler.handleMessage(session, msg2);
		handler.handleMessage(session, msg3);

		// THEN
		TextMessage outMsg = outMessageCaptor.getValue();
		assertThat("Heartbeat response message sent", outMsg, notNullValue());
		assertThat("Heartbeat response message content", outMsg.getPayload()
				.matches("\\[3,\"1603881305171\",\\{\"currentTime\":\"[^\"]+\"\\}\\]"), equalTo(true));
		assertThat("Partial message buffer cleared", sessionAttributes,
				not(hasKey(OcppWebSocketHandler.PARTIAL_MESSAGE_BUFFER_SESSION_KEY)));
	}

	@Test
	public void partialMessageRequest_tooBig() throws Exception {
		// GIVEN
		final ChargePointIdentity cpIdent = new ChargePointIdentity("foo", "user");
		final Map<String, Object> sessionAttributes = new HashMap<>(2);
		sessionAttributes.put(OcppWebSocketHandshakeInterceptor.CLIENT_ID_ATTR, cpIdent);
		final String sessionId = UUID.randomUUID().toString();
		expect(session.getId()).andReturn(sessionId).anyTimes();
		expect(session.getAttributes()).andReturn(sessionAttributes).anyTimes();
		handler.addActionMessageProcessor(new HeartbeatProcessor());
		handler.setPartialMessageMaximumSize(24);

		// send HeartbeatResponse
		Capture<TextMessage> outMessageCaptor = Capture.newInstance();
		session.sendMessage(capture(outMessageCaptor));

		// WHEN
		replayAll();
		handler.startup(false);
		handler.afterConnectionEstablished(session);
		TextMessage msg1 = new TextMessage("[2,\"1603881305171\",", false);
		TextMessage msg2 = new TextMessage("\"Heartbeat\",", false);
		handler.handleMessage(session, msg1);
		handler.handleMessage(session, msg2);

		// THEN
		TextMessage outMsg = outMessageCaptor.getValue();
		assertThat("Heartbeat response message sent", outMsg, notNullValue());
		assertThat("Heartbeat response message content", outMsg.getPayload(), is(equalTo(
				"[4,null,\"ProtocolError\",\"Maximum partial message sequence total allowed size 24 exceeded.\",{}]")));
		assertThat("Partial message buffer cleared", sessionAttributes,
				not(hasKey(OcppWebSocketHandler.PARTIAL_MESSAGE_BUFFER_SESSION_KEY)));
	}

	@Test
	public void partialMessageRequest_tooBig_close() throws Exception {
		// GIVEN
		final ChargePointIdentity cpIdent = new ChargePointIdentity("foo", "user");
		final Map<String, Object> sessionAttributes = new HashMap<>(2);
		sessionAttributes.put(OcppWebSocketHandshakeInterceptor.CLIENT_ID_ATTR, cpIdent);
		final String sessionId = UUID.randomUUID().toString();
		expect(session.getId()).andReturn(sessionId).anyTimes();
		expect(session.getAttributes()).andReturn(sessionAttributes).anyTimes();
		handler.addActionMessageProcessor(new HeartbeatProcessor());
		handler.setPartialMessageMaximumSize(24);
		handler.setPartialMessageMaximumSizeExceededClose(true);

		Capture<CloseStatus> closeStatusCaptor = Capture.newInstance();
		session.close(capture(closeStatusCaptor));

		// WHEN
		replayAll();
		handler.startup(false);
		handler.afterConnectionEstablished(session);
		TextMessage msg1 = new TextMessage("[2,\"1603881305171\",", false);
		TextMessage msg2 = new TextMessage("\"Heartbeat\",", false);
		handler.handleMessage(session, msg1);
		handler.handleMessage(session, msg2);

		// THEN
		CloseStatus status = closeStatusCaptor.getValue();
		assertThat("Session was closed", status, notNullValue());
		assertThat("Close reason was 'too big'", status.getCode(),
				is(equalTo(CloseStatus.TOO_BIG_TO_PROCESS.getCode())));
	}

	private WebSocketSession connectedSession(ChargePointIdentity cpIdent) throws Exception {
		final WebSocketSession s = EasyMock.createMock(WebSocketSession.class);
		final Map<String, Object> attributes = Collections
				.singletonMap(OcppWebSocketHandshakeInterceptor.CLIENT_ID_ATTR, cpIdent);
		expect(s.getId()).andReturn(UUID.randomUUID().toString()).anyTimes();
		expect(s.getAttributes()).andReturn(attributes).anyTimes();
		EasyMock.replay(s);
		handler.afterConnectionEstablished(s);
		return s;
	}

	@Test
	public void isChargePointAvailable_connected() throws Exception {
		// GIVEN
		final ChargePointIdentity cpIdent = new ChargePointIdentity("foo", "user");
		replayAll();
		handler.startup(false);
		connectedSession(cpIdent);

		// WHEN
		boolean result = handler.isChargePointAvailable(cpIdent);

		// THEN
		then(result).as("Connected charger is available").isTrue();
	}

	@Test
	public void isChargePointAvailable_otherChargerConnected() throws Exception {
		// GIVEN
		replayAll();
		handler.startup(false);
		connectedSession(new ChargePointIdentity("foo", "user"));

		// "bar" sorts before "foo", so the connected "foo" session follows its boundary key
		final ChargePointIdentity notConnected = new ChargePointIdentity("bar", "user");

		// WHEN
		boolean result = handler.isChargePointAvailable(notConnected);

		// THEN
		then(result).as("Charger without a session is not available").isFalse();
	}

	@Test
	public void isChargePointAvailable_sameIdentifierOtherUserConnected() throws Exception {
		// GIVEN
		replayAll();
		handler.startup(false);
		connectedSession(new ChargePointIdentity("foo", "user"));

		// "other" sorts before "user", so the connected session follows its boundary key
		final ChargePointIdentity notConnected = new ChargePointIdentity("foo", "other");

		// WHEN
		boolean result = handler.isChargePointAvailable(notConnected);

		// THEN
		then(result).as("Same identifier for a different user is not available").isFalse();
	}

	@Test
	public void isChargePointAvailable_disconnected() throws Exception {
		// GIVEN
		final ChargePointIdentity cpIdent = new ChargePointIdentity("foo", "user");
		replayAll();
		handler.startup(false);
		WebSocketSession s = connectedSession(cpIdent);
		handler.afterConnectionClosed(s, CloseStatus.NORMAL);

		// WHEN
		boolean result = handler.isChargePointAvailable(cpIdent);

		// THEN
		then(result).as("Disconnected charger is not available").isFalse();
	}

	@Test
	public void isMessageSupported_otherChargerConnected() throws Exception {
		// GIVEN
		replayAll();
		handler.startup(false);
		connectedSession(new ChargePointIdentity("foo", "user"));

		final ActionMessage<HeartbeatRequest> msg = new BasicActionMessage<>(
				new ChargePointIdentity("bar", "user"), CentralSystemAction.Heartbeat,
				new HeartbeatRequest());

		// WHEN
		boolean result = handler.isMessageSupported(msg);

		// THEN
		then(result).as("Message for charger without a session is not supported").isFalse();
	}

	@Test
	public void availableChargePointsIds() throws Exception {
		// GIVEN
		final ChargePointIdentity cp1 = new ChargePointIdentity("foo", "user");
		final ChargePointIdentity cp2 = new ChargePointIdentity("bar", "user");
		replayAll();
		handler.startup(false);
		connectedSession(cp1);
		connectedSession(cp1); // second session for same charger
		WebSocketSession s2 = connectedSession(cp2);
		connectedSession(new ChargePointIdentity("bim", "user"));
		handler.afterConnectionClosed(s2, CloseStatus.NORMAL);

		// WHEN
		var result = handler.availableChargePointsIds();

		// THEN
		// @formatter:off
		then(result)
			.as("Each connected charger listed once, disconnected charger excluded")
			.containsExactlyInAnyOrderElementsOf(List.of(cp1, new ChargePointIdentity("bim", "user")))
			;
		// @formatter:on
	}

}

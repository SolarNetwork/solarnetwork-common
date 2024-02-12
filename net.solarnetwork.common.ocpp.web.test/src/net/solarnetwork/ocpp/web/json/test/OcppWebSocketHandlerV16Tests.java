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

package net.solarnetwork.ocpp.web.json.test;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.expect;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import java.util.Collections;
import java.util.Map;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import net.solarnetwork.ocpp.domain.ActionMessage;
import net.solarnetwork.ocpp.domain.ChargePointIdentity;
import net.solarnetwork.ocpp.service.ActionMessageResultHandler;
import net.solarnetwork.ocpp.v16.CentralSystemAction;
import net.solarnetwork.ocpp.v16.ChargePointAction;
import net.solarnetwork.ocpp.v16.ErrorCodeResolver;
import net.solarnetwork.ocpp.v16.cs.HeartbeatProcessor;
import net.solarnetwork.ocpp.web.json.OcppWebSocketHandler;
import net.solarnetwork.ocpp.web.json.OcppWebSocketHandshakeInterceptor;
import net.solarnetwork.security.AuthorizationException;
import net.solarnetwork.test.CallingThreadExecutorService;
import ocpp.v16.cs.HeartbeatRequest;
import ocpp.v16.cs.HeartbeatResponse;

/**
 * Test cases for the {@link OcppWebSocketHandler} class.
 * 
 * @author matt
 * @version 1.0
 */
public class OcppWebSocketHandlerV16Tests {

	private WebSocketSession session;
	private OcppWebSocketHandler<ChargePointAction, CentralSystemAction> handler;

	@Before
	public void setup() {
		session = EasyMock.createMock(WebSocketSession.class);
		handler = new OcppWebSocketHandler<>(ChargePointAction.class, CentralSystemAction.class,
				new ErrorCodeResolver(), new TaskExecutorAdapter(new CallingThreadExecutorService()));
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
		expect(session.getAttributes()).andReturn(sessionAttributes).anyTimes();
		handler.addActionMessageProcessor(new HeartbeatProcessor());

		// send HeartbeatResponse
		Capture<TextMessage> outMessageCaptor = Capture.newInstance();
		session.sendMessage(capture(outMessageCaptor));

		// WHEN
		replayAll();
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
	public void authorizationException() throws Exception {
		final ChargePointIdentity cpIdent = new ChargePointIdentity("foo", "user");
		final Map<String, Object> sessionAttributes = Collections
				.singletonMap(OcppWebSocketHandshakeInterceptor.CLIENT_ID_ATTR, cpIdent);
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
		handler.afterConnectionEstablished(session);
		TextMessage msg = new TextMessage("[2,\"1603881305171\",\"Heartbeat\",{}]");
		handler.handleMessage(session, msg);

		// THEN
		TextMessage outMsg = outMessageCaptor.getValue();
		assertThat("Heartbeat response message sent", outMsg, notNullValue());
		assertThat("Heartbeat response message content", outMsg.getPayload(), is(equalTo(
				"[4,\"1603881305171\",\"SecurityError\",\"Authorization error handling action.\",{}]")));
	}

}

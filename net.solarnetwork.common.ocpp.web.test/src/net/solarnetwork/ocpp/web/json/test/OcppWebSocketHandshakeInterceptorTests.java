/* ==================================================================
 * OcppWebSocketHandshakeInterceptor.java - 31/01/2020 4:49:04 pm
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

import static org.easymock.EasyMock.expect;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import java.net.URI;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.SubProtocolCapable;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketHttpHeaders;
import net.solarnetwork.ocpp.dao.SystemUserDao;
import net.solarnetwork.ocpp.domain.ChargePointIdentity;
import net.solarnetwork.ocpp.domain.SystemUser;
import net.solarnetwork.ocpp.web.json.OcppWebSocketHandshakeInterceptor;
import net.solarnetwork.support.PasswordEncoder;
import ocpp.json.WebSocketSubProtocol;

/**
 * Test cases for the {@link OcppWebSocketHandshakeInterceptor} class.
 * 
 * @author matt
 * @version 1.0
 */
public class OcppWebSocketHandshakeInterceptorTests {

	public static interface WebSocketHandlerAndSubProtocolCapable
			extends WebSocketHandler, SubProtocolCapable {

	}

	private ServerHttpRequest req;
	private ServerHttpResponse res;
	private SystemUserDao systemUserDao;
	private PasswordEncoder passwordEncoder;
	private WebSocketHandlerAndSubProtocolCapable handler;

	@Before
	public void setup() {
		req = EasyMock.createMock(ServerHttpRequest.class);
		res = EasyMock.createMock(ServerHttpResponse.class);
		systemUserDao = EasyMock.createMock(SystemUserDao.class);
		passwordEncoder = EasyMock.createMock(PasswordEncoder.class);
		handler = EasyMock.createMock(WebSocketHandlerAndSubProtocolCapable.class);
	}

	@After
	public void teardown() {
		EasyMock.verify(req, res, handler, systemUserDao, passwordEncoder);
	}

	private void replayAll() {
		EasyMock.replay(req, res, handler, systemUserDao, passwordEncoder);
	}

	@Test
	public void missingClientId() throws Exception {
		// given
		URI uri = URI.create("http://example.com/ocpp/v16");
		expect(req.getURI()).andReturn(uri);
		res.setStatusCode(HttpStatus.NOT_FOUND);

		OcppWebSocketHandshakeInterceptor hi = new OcppWebSocketHandshakeInterceptor(systemUserDao,
				passwordEncoder);

		// when
		replayAll();
		Map<String, Object> attributes = new LinkedHashMap<>(4);
		boolean result = hi.beforeHandshake(req, res, handler, attributes);

		assertThat("Result failed from lack of client ID", result, equalTo(false));
		assertThat("No attributes populated", attributes.keySet(), hasSize(0));
	}

	private void addBasicAuth(HttpHeaders h) {
		h.add(HttpHeaders.AUTHORIZATION, "Basic "
				+ Base64.getEncoder().encodeToString("foo:bar".getBytes(Charset.forName("UTF-8"))));
	}

	private SystemUser testUser() {
		SystemUser user = new SystemUser(1L, Instant.now());
		user.setUsername("foo");
		user.setPassword("bar");
		return user;
	}

	@Test
	public void ok() throws Exception {
		// given
		URI uri = URI.create("http://example.com/ocpp/v16/cs/json/foobar");
		expect(req.getURI()).andReturn(uri);
		expect(handler.getSubProtocols())
				.andReturn(Collections.singletonList(WebSocketSubProtocol.OCPP_V16.getValue()));

		HttpHeaders h = new HttpHeaders();
		h.add(WebSocketHttpHeaders.SEC_WEBSOCKET_PROTOCOL, WebSocketSubProtocol.OCPP_V16.getValue());
		addBasicAuth(h);
		expect(req.getHeaders()).andReturn(h).anyTimes();

		SystemUser user = testUser();
		expect(systemUserDao.getForUsername("foo")).andReturn(user);
		expect(passwordEncoder.matches("bar", "bar")).andReturn(true);

		OcppWebSocketHandshakeInterceptor hi = new OcppWebSocketHandshakeInterceptor(systemUserDao,
				passwordEncoder);

		// when
		replayAll();
		Map<String, Object> attributes = new LinkedHashMap<>(4);
		boolean result = hi.beforeHandshake(req, res, handler, attributes);

		assertThat("Result success", result, equalTo(true));
		assertThat("Client ID attribute populated", attributes,
				hasEntry(OcppWebSocketHandshakeInterceptor.CLIENT_ID_ATTR,
						new ChargePointIdentity("foobar", "foo")));
	}

	@Test
	public void noSubProtocol() throws Exception {
		// given
		URI uri = URI.create("http://example.com/ocpp/v16/cs/json/foobar");
		expect(req.getURI()).andReturn(uri);
		expect(handler.getSubProtocols())
				.andReturn(Collections.singletonList(WebSocketSubProtocol.OCPP_V16.getValue()));

		HttpHeaders h = new HttpHeaders();
		expect(req.getHeaders()).andReturn(h).anyTimes();

		res.setStatusCode(HttpStatus.BAD_REQUEST);

		OcppWebSocketHandshakeInterceptor hi = new OcppWebSocketHandshakeInterceptor(systemUserDao,
				passwordEncoder);

		// when
		replayAll();
		Map<String, Object> attributes = new LinkedHashMap<>(4);
		boolean result = hi.beforeHandshake(req, res, handler, attributes);

		assertThat("Result failed from missing sub-protocol", result, equalTo(false));
	}

	@Test
	public void wrongSubProtocol() throws Exception {
		// given
		URI uri = URI.create("http://example.com/ocpp/v16/cs/json/foobar");
		expect(req.getURI()).andReturn(uri);
		expect(handler.getSubProtocols())
				.andReturn(Collections.singletonList(WebSocketSubProtocol.OCPP_V16.getValue()));

		HttpHeaders h = new HttpHeaders();
		h.add(WebSocketHttpHeaders.SEC_WEBSOCKET_PROTOCOL, WebSocketSubProtocol.OCPP_V15.getValue());
		expect(req.getHeaders()).andReturn(h).anyTimes();

		res.setStatusCode(HttpStatus.BAD_REQUEST);

		OcppWebSocketHandshakeInterceptor hi = new OcppWebSocketHandshakeInterceptor(systemUserDao,
				passwordEncoder);

		// when
		replayAll();
		Map<String, Object> attributes = new LinkedHashMap<>(4);
		boolean result = hi.beforeHandshake(req, res, handler, attributes);

		assertThat("Result failed from missing sub-protocol", result, equalTo(false));
	}

	@Test
	public void userNotFound() throws Exception {
		// given
		URI uri = URI.create("http://example.com/ocpp/v16/cs/json/foobar");
		expect(req.getURI()).andReturn(uri);
		expect(handler.getSubProtocols())
				.andReturn(Collections.singletonList(WebSocketSubProtocol.OCPP_V16.getValue()));

		HttpHeaders h = new HttpHeaders();
		h.add(WebSocketHttpHeaders.SEC_WEBSOCKET_PROTOCOL, WebSocketSubProtocol.OCPP_V16.getValue());
		addBasicAuth(h);
		expect(req.getHeaders()).andReturn(h).anyTimes();

		expect(systemUserDao.getForUsername("foo")).andReturn(null);

		res.setStatusCode(HttpStatus.FORBIDDEN);

		OcppWebSocketHandshakeInterceptor hi = new OcppWebSocketHandshakeInterceptor(systemUserDao,
				passwordEncoder);

		// when
		replayAll();
		Map<String, Object> attributes = new LinkedHashMap<>(4);
		boolean result = hi.beforeHandshake(req, res, handler, attributes);

		assertThat("Result failed from missing user", result, equalTo(false));
	}

	@Test
	public void badPassword() throws Exception {
		// given
		URI uri = URI.create("http://example.com/ocpp/v16/cs/json/foobar");
		expect(req.getURI()).andReturn(uri);
		expect(handler.getSubProtocols())
				.andReturn(Collections.singletonList(WebSocketSubProtocol.OCPP_V16.getValue()));

		HttpHeaders h = new HttpHeaders();
		h.add(WebSocketHttpHeaders.SEC_WEBSOCKET_PROTOCOL, WebSocketSubProtocol.OCPP_V16.getValue());
		addBasicAuth(h);
		expect(req.getHeaders()).andReturn(h).anyTimes();

		SystemUser user = testUser();
		user.setPassword("not bar");
		expect(systemUserDao.getForUsername("foo")).andReturn(user);
		expect(passwordEncoder.matches("bar", "not bar")).andReturn(false);

		res.setStatusCode(HttpStatus.FORBIDDEN);

		OcppWebSocketHandshakeInterceptor hi = new OcppWebSocketHandshakeInterceptor(systemUserDao,
				passwordEncoder);

		// when
		replayAll();
		Map<String, Object> attributes = new LinkedHashMap<>(4);
		boolean result = hi.beforeHandshake(req, res, handler, attributes);

		assertThat("Result failed from bad password", result, equalTo(false));
	}

	@Test
	public void noAuth() throws Exception {
		// given
		URI uri = URI.create("http://example.com/ocpp/v16/cs/json/foobar");
		expect(req.getURI()).andReturn(uri);
		expect(handler.getSubProtocols())
				.andReturn(Collections.singletonList(WebSocketSubProtocol.OCPP_V16.getValue()));

		HttpHeaders h = new HttpHeaders();
		h.add(WebSocketHttpHeaders.SEC_WEBSOCKET_PROTOCOL, WebSocketSubProtocol.OCPP_V16.getValue());
		expect(req.getHeaders()).andReturn(h).anyTimes();

		res.setStatusCode(HttpStatus.FORBIDDEN);

		OcppWebSocketHandshakeInterceptor hi = new OcppWebSocketHandshakeInterceptor(systemUserDao,
				passwordEncoder);

		// when
		replayAll();
		Map<String, Object> attributes = new LinkedHashMap<>(4);
		boolean result = hi.beforeHandshake(req, res, handler, attributes);

		assertThat("Result failed from no Authorization header", result, equalTo(false));
	}

	@Test
	public void notBasicAuth() throws Exception {
		// given
		URI uri = URI.create("http://example.com/ocpp/v16/cs/json/foobar");
		expect(req.getURI()).andReturn(uri);
		expect(handler.getSubProtocols())
				.andReturn(Collections.singletonList(WebSocketSubProtocol.OCPP_V16.getValue()));

		HttpHeaders h = new HttpHeaders();
		h.add(WebSocketHttpHeaders.SEC_WEBSOCKET_PROTOCOL, WebSocketSubProtocol.OCPP_V16.getValue());
		h.add(HttpHeaders.AUTHORIZATION, "Diddly Squat");
		expect(req.getHeaders()).andReturn(h).anyTimes();

		res.setStatusCode(HttpStatus.FORBIDDEN);

		OcppWebSocketHandshakeInterceptor hi = new OcppWebSocketHandshakeInterceptor(systemUserDao,
				passwordEncoder);

		// when
		replayAll();
		Map<String, Object> attributes = new LinkedHashMap<>(4);
		boolean result = hi.beforeHandshake(req, res, handler, attributes);

		assertThat("Result failed from non-Basic auth header", result, equalTo(false));
	}

	@Test
	public void malformedBasicAuth_notBase64() throws Exception {
		// given
		URI uri = URI.create("http://example.com/ocpp/v16/cs/json/foobar");
		expect(req.getURI()).andReturn(uri);
		expect(handler.getSubProtocols())
				.andReturn(Collections.singletonList(WebSocketSubProtocol.OCPP_V16.getValue()));

		HttpHeaders h = new HttpHeaders();
		h.add(WebSocketHttpHeaders.SEC_WEBSOCKET_PROTOCOL, WebSocketSubProtocol.OCPP_V16.getValue());
		h.add(HttpHeaders.AUTHORIZATION, "Basic Oh:no");
		expect(req.getHeaders()).andReturn(h).anyTimes();

		res.setStatusCode(HttpStatus.FORBIDDEN);

		OcppWebSocketHandshakeInterceptor hi = new OcppWebSocketHandshakeInterceptor(systemUserDao,
				passwordEncoder);

		// when
		replayAll();
		Map<String, Object> attributes = new LinkedHashMap<>(4);
		boolean result = hi.beforeHandshake(req, res, handler, attributes);

		assertThat("Result failed from malformed-Basic auth header", result, equalTo(false));
	}

	@Test
	public void malformedBasicAuth_noComponents() throws Exception {
		// given
		URI uri = URI.create("http://example.com/ocpp/v16/cs/json/foobar");
		expect(req.getURI()).andReturn(uri);
		expect(handler.getSubProtocols())
				.andReturn(Collections.singletonList(WebSocketSubProtocol.OCPP_V16.getValue()));

		HttpHeaders h = new HttpHeaders();
		h.add(WebSocketHttpHeaders.SEC_WEBSOCKET_PROTOCOL, WebSocketSubProtocol.OCPP_V16.getValue());
		h.add(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder()
				.encodeToString("user but no pass".getBytes(Charset.forName("UTF-8"))));
		expect(req.getHeaders()).andReturn(h).anyTimes();

		res.setStatusCode(HttpStatus.FORBIDDEN);

		OcppWebSocketHandshakeInterceptor hi = new OcppWebSocketHandshakeInterceptor(systemUserDao,
				passwordEncoder);

		// when
		replayAll();
		Map<String, Object> attributes = new LinkedHashMap<>(4);
		boolean result = hi.beforeHandshake(req, res, handler, attributes);

		assertThat("Result failed from malformed-Basic auth header", result, equalTo(false));
	}

}

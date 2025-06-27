/* ==================================================================
 * ChainHttpRequestCustomizerServiceTests.java - 2/04/2023 12:25:12 pm
 * 
 * Copyright 2023 SolarNetwork.net Dev Team
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

package net.solarnetwork.web.jakarta.service.support.test;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import net.solarnetwork.settings.SettingSpecifier;
import net.solarnetwork.util.ByteList;
import net.solarnetwork.web.jakarta.service.HttpRequestCustomizerService;
import net.solarnetwork.web.jakarta.service.support.AbstractHttpRequestCustomizerService;
import net.solarnetwork.web.jakarta.service.support.ChainHttpRequestCustomizerService;

/**
 * Test cases for the {@link ChainHttpRequestCustomizerService} class.
 * 
 * @author matt
 * @version 1.0
 */
public class ChainHttpRequestCustomizerServiceTests {

	private static final String X_TEST = "x-test";
	private HttpRequest req;

	@Before
	public void setup() {
		req = EasyMock.createMock(HttpRequest.class);
	}

	@After
	public void teardown() {
		EasyMock.verify(req);
	}

	private void replayAll() {
		EasyMock.replay(req);
	}

	@Test
	public void emptyConfiguration() {
		// GIVEN
		ChainHttpRequestCustomizerService s = new ChainHttpRequestCustomizerService(
				Collections.emptyList());

		// WHEN
		replayAll();
		final ByteList body = new ByteList();
		HttpRequest result = s.customize(req, body, null);

		assertThat("Input request returned", result, is(sameInstance(req)));
		assertThat("Body unchanged", body.size(), is(equalTo(0)));
	}

	@Test
	public void noServicesAvailable() {
		// GIVEN
		ChainHttpRequestCustomizerService s = new ChainHttpRequestCustomizerService(
				Collections.emptyList());
		s.setServiceUids(new String[] { "a", "b" });

		// WHEN
		replayAll();
		final ByteList body = new ByteList();
		HttpRequest result = s.customize(req, body, null);

		assertThat("Input request returned", result, is(sameInstance(req)));
		assertThat("Body unchanged", body.size(), is(equalTo(0)));
	}

	private static class TestHttpRequestCustomizerService extends AbstractHttpRequestCustomizerService {

		private TestHttpRequestCustomizerService(String uid) {
			super();
			setUid(uid);
		}

		@Override
		public void configurationChanged(Map<String, Object> properties) {
			// nothing
		}

		@Override
		public String getSettingUid() {
			return "test";
		}

		@Override
		public List<SettingSpecifier> getSettingSpecifiers() {
			return Collections.emptyList();
		}

		@Override
		public HttpRequest customize(HttpRequest request, ByteList body, Map<String, ?> parameters) {
			return request;
		}
	}

	@Test
	public void someServicesAvailable() {
		// GIVEN
		List<HttpRequestCustomizerService> services = new ArrayList<>(1);
		services.add(new TestHttpRequestCustomizerService("a") {

			@Override
			public HttpRequest customize(HttpRequest request, ByteList body, Map<String, ?> parameters) {
				request.getHeaders().set(X_TEST, "a");
				return request;
			}
		});
		ChainHttpRequestCustomizerService s = new ChainHttpRequestCustomizerService(services);
		s.setServiceUids(new String[] { "a", "b" });

		HttpHeaders headers = new HttpHeaders();
		expect(req.getHeaders()).andReturn(headers);

		// WHEN
		replayAll();
		final ByteList body = new ByteList();
		HttpRequest result = s.customize(req, body, null);

		assertThat("Input request returned", result, is(sameInstance(req)));
		assertThat("Body unchanged", body.size(), is(equalTo(0)));
		assertThat("Delegate service executed", headers.getFirst(X_TEST), is(equalTo("a")));
	}

	@Test
	public void servicesExecutedInOrder() {
		// GIVEN
		List<HttpRequestCustomizerService> services = new ArrayList<>(1);
		services.add(new TestHttpRequestCustomizerService("a") {

			@Override
			public HttpRequest customize(HttpRequest request, ByteList body, Map<String, ?> parameters) {
				request.getHeaders().set(X_TEST, "a");
				return request;
			}
		});
		services.add(new TestHttpRequestCustomizerService("b") {

			@Override
			public HttpRequest customize(HttpRequest request, ByteList body, Map<String, ?> parameters) {
				request.getHeaders().add(X_TEST, "b");
				return request;
			}
		});
		ChainHttpRequestCustomizerService s = new ChainHttpRequestCustomizerService(services);
		s.setServiceUids(new String[] { "a", "b" });

		HttpHeaders headers = new HttpHeaders();
		expect(req.getHeaders()).andReturn(headers).times(2);

		// WHEN
		replayAll();
		final ByteList body = new ByteList();
		HttpRequest result = s.customize(req, body, null);

		assertThat("Input request returned", result, is(sameInstance(req)));
		assertThat("Body unchanged", body.size(), is(equalTo(0)));
		assertThat("Delegate service executed", headers.get(X_TEST), contains("a", "b"));
	}

	@Test
	public void nullTerminatesIteration() {
		// GIVEN
		List<HttpRequestCustomizerService> services = new ArrayList<>(1);
		services.add(new TestHttpRequestCustomizerService("a") {

			@Override
			public HttpRequest customize(HttpRequest request, ByteList body, Map<String, ?> parameters) {
				request.getHeaders().set(X_TEST, "a");
				return request;
			}
		});
		services.add(new TestHttpRequestCustomizerService("b") {

			@Override
			public HttpRequest customize(HttpRequest request, ByteList body, Map<String, ?> parameters) {
				return null;
			}
		});
		services.add(new TestHttpRequestCustomizerService("c") {

			@Override
			public HttpRequest customize(HttpRequest request, ByteList body, Map<String, ?> parameters) {
				request.getHeaders().add(X_TEST, "c");
				return request;
			}
		});
		ChainHttpRequestCustomizerService s = new ChainHttpRequestCustomizerService(services);
		s.setServiceUids(new String[] { "a", "b" });

		HttpHeaders headers = new HttpHeaders();
		expect(req.getHeaders()).andReturn(headers);

		// WHEN
		replayAll();
		final ByteList body = new ByteList();
		HttpRequest result = s.customize(req, body, null);

		assertThat("Null request returned", result, is(nullValue()));
		assertThat("Body unchanged", body.size(), is(equalTo(0)));
		assertThat("First delegate service executed", headers.get(X_TEST), contains("a"));
	}

}

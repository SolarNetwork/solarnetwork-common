/* ==================================================================
 * AuthorizationV2RequestInterceptorTests.java - 13/08/2019 10:54:17 am
 * 
 * Copyright 2019 SolarNetwork.net Dev Team
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

package net.solarnetwork.web.jakarta.support.test;

import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import net.solarnetwork.security.Snws2AuthorizationBuilder;
import net.solarnetwork.web.jakarta.security.AuthorizationCredentialsProvider;
import net.solarnetwork.web.jakarta.support.AuthorizationV2RequestInterceptor;
import net.solarnetwork.web.jakarta.support.StaticAuthorizationCredentialsProvider;

/**
 * Test cases for the {@link AuthorizationV2RequestInterceptor} class.
 * 
 * @author matt
 * @version 1.0
 */
public class AuthorizationV2RequestInterceptorTests {

	private static final Charset UTF8 = Charset.forName("UTF-8");
	private static final String TEST_HOST = "localhost";
	private static final String TEST_BASE_URL = "http://" + TEST_HOST;

	private final Logger log = LoggerFactory.getLogger(getClass());

	private AuthorizationCredentialsProvider credentialsProvider;
	private RestTemplate restTemplate;
	private MockRestServiceServer server;

	@Before
	public void setup() {
		restTemplate = new RestTemplate();
		credentialsProvider = new StaticAuthorizationCredentialsProvider(randomUUID().toString(),
				randomUUID().toString());
		setupRestTemplateInterceptors(credentialsProvider);
		server = MockRestServiceServer.createServer(restTemplate);
	}

	private void setupRestTemplateInterceptors(AuthorizationCredentialsProvider credentialsProvider) {
		List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
		if ( restTemplate.getInterceptors() != null ) {
			interceptors.addAll(restTemplate.getInterceptors().stream()
					.filter(o -> !(o instanceof AuthorizationV2RequestInterceptor))
					.collect(Collectors.toList()));
		}
		interceptors.add(0, new AuthorizationV2RequestInterceptor(credentialsProvider));
		restTemplate.setInterceptors(interceptors);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void getSimple() {
		// given
		final String responseBody = "{\"success\":true}";
		HttpHeaders respHeaders = new HttpHeaders();
		respHeaders.setContentLength(responseBody.getBytes(UTF8).length);

		// @formatter:off
	    server.expect(requestTo(startsWith(TEST_BASE_URL + "/foo/bar")))
	        .andExpect(method(HttpMethod.GET))
	        .andExpect(header(HttpHeaders.HOST, "localhost"))
	        .andExpect(header(HttpHeaders.AUTHORIZATION, 
	            startsWith("SNWS2 Credential=" + credentialsProvider.getAuthorizationId() + ",SignedHeaders=accept;date;host,Signature=")))
	        .andRespond(withSuccess(responseBody, APPLICATION_JSON).headers(respHeaders));
	    // @formatter:on

		// when
		ResponseEntity<Map> result = restTemplate.getForEntity(TEST_BASE_URL + "/foo/bar", Map.class);

		// then
		assertThat("Result returned", result, notNullValue());
		assertThat("Result OK", result.getStatusCode(), equalTo(HttpStatus.OK));
		assertThat("Result map", (Map<String, Object>) result.getBody(),
				hasEntry("success", Boolean.TRUE));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void postFormParameters() {
		// given
		final String responseBody = "{\"success\":true}";
		HttpHeaders respHeaders = new HttpHeaders();
		respHeaders.setContentLength(responseBody.getBytes(UTF8).length);

		final long reqDate = System.currentTimeMillis();

		// @formatter:off
		Snws2AuthorizationBuilder auth = new Snws2AuthorizationBuilder(credentialsProvider.getAuthorizationId())
				.method(HttpMethod.POST.toString())
				.host(TEST_HOST)
				.path("/foo/bar")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED + ";charset=UTF-8")
				.date(Instant.ofEpochMilli(reqDate))
				.header("Accept", "application/json, application/*+json")
				.header("Content-Length", "9")
				.parameterMap(Collections.singletonMap("topic", new String[] { "foo" }));
		
		log.debug("Canonical req data:\n{}", auth.computeCanonicalRequestMessage());
		log.debug("Signature data:\n{}", auth.computeSignatureData(Instant.ofEpochMilli(reqDate), auth.computeCanonicalRequestMessage()));
		
		final String authHeader = auth.build(credentialsProvider.getAuthorizationSecret());

	    server.expect(requestTo(startsWith(TEST_BASE_URL + "/foo/bar")))
	        .andExpect(method(HttpMethod.POST))
	        .andExpect(header(HttpHeaders.HOST, "localhost"))
	        .andExpect(header(HttpHeaders.AUTHORIZATION, equalTo(authHeader)))
	        .andRespond(withSuccess(responseBody, APPLICATION_JSON).headers(respHeaders));
	    // @formatter:on

		// when
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.setDate(reqDate);
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>(4);
		params.add("topic", "foo");

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
		ResponseEntity<Map> result = restTemplate.postForEntity(TEST_BASE_URL + "/foo/bar", request,
				Map.class);

		// then
		assertThat("Result returned", result, notNullValue());
		assertThat("Result OK", result.getStatusCode(), equalTo(HttpStatus.OK));
		assertThat("Result map", (Map<String, Object>) result.getBody(),
				hasEntry("success", Boolean.TRUE));
	}

}

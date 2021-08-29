/* ==================================================================
 * AuthenticationDataTokenAuthenticationFilterTests.java - 27/04/2017 12:02:07 PM
 * 
 * Copyright 2017 SolarNetwork.net Dev Team
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

package net.solarnetwork.web.security.test;

import static net.solarnetwork.security.AuthorizationUtils.AUTHORIZATION_DATE_HEADER_FORMATTER;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.same;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.digest.DigestUtils;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import net.solarnetwork.security.Snws2AuthorizationBuilder;
import net.solarnetwork.web.security.AuthenticationDataTokenAuthenticationFilter;

/**
 * Test cases for the {@link AuthenticationDataTokenAuthenticationFilter} class.
 * 
 * @author matt
 * @version 1.1
 */
public class AuthenticationDataTokenAuthenticationFilterTests {

	private static final String HTTP_HEADER_AUTH = "Authorization";
	private static final String TEST_AUTH_TOKEN = "12345678901234567890";
	private static final String TEST_PASSWORD = "lsdjfpse9jfoeijfe09j";

	private FilterChain filterChain;
	private MockHttpServletResponse response;
	private UserDetailsService userDetailsService;
	private AuthenticationDataTokenAuthenticationFilter filter;
	private User userDetails;

	private void setupAuthorizationHeader(MockHttpServletRequest request, String value) {
		request.addHeader(HTTP_HEADER_AUTH, value);
	}

	private void validateAuthentication() {
		assertEquals(HttpServletResponse.SC_OK, response.getStatus());
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		assertNotNull("Authentication available", auth);
		assertEquals("Authentication name", TEST_AUTH_TOKEN, auth.getName());
	}

	private String createAuthorizationHeaderV2Value(String tokenId, String secret,
			MockHttpServletRequest request, Instant date) {
		return createAuthorizationHeaderV2Value(tokenId, secret, request, date, null);
	}

	private String createAuthorizationHeaderV2Value(String tokenId, String secret,
			MockHttpServletRequest request, Instant date, byte[] bodyContent) {
		Snws2AuthorizationBuilder builder = new Snws2AuthorizationBuilder(tokenId);
		builder.date(date).host(request.getRemoteHost()).method(request.getMethod())
				.path(request.getRequestURI());
		if ( request.getParameterMap() != null ) {
			builder.parameterMap(request.getParameterMap());
		}
		Set<String> headersToSign = new HashSet<String>(
				Arrays.asList("content-type", "content-md5", "digest"));
		Enumeration<String> headers = request.getHeaderNames();
		while ( headers.hasMoreElements() ) {
			String header = headers.nextElement().toLowerCase();
			if ( (header.startsWith("x-sn-") || headersToSign.contains(header))
					&& request.getHeader(header) != null ) {
				builder.header(header, request.getHeader(header));
			}
		}
		if ( bodyContent != null ) {
			builder.contentSha256(DigestUtils.sha256(bodyContent));
		}
		return builder.build(secret);
	}

	@Before
	public void setup() {
		filterChain = EasyMock.createMock(FilterChain.class);
		response = new MockHttpServletResponse();
		userDetailsService = EasyMock.createMock(UserDetailsService.class);
		List<GrantedAuthority> roles = new ArrayList<GrantedAuthority>();
		roles.add(new SimpleGrantedAuthority("ROLE_TEST"));
		userDetails = new User(TEST_AUTH_TOKEN, TEST_PASSWORD, roles);
		filter = new AuthenticationDataTokenAuthenticationFilter();
		filter.setUserDetailsService(userDetailsService);
	}

	@Test
	public void noAuthorizationHeader() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/mock/path/here");
		filterChain.doFilter(anyObject(HttpServletRequest.class), same(response));
		replay(filterChain, userDetailsService);
		filter.doFilter(request, response, filterChain);
		verify(filterChain, userDetailsService);
	}

	@Test
	public void invalidScheme() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/mock/path/here");
		filterChain.doFilter(anyObject(HttpServletRequest.class), same(response));
		replay(filterChain, userDetailsService);
		setupAuthorizationHeader(request, "FooScheme ABC:DOEIJLSIEWOSEIHLSISYEOIHEOIJ");
		filter.doFilter(request, response, filterChain);
		verify(filterChain, userDetailsService);

	}

	@Test(expected = BadCredentialsException.class)
	public void missingDateV2() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/mock/path/here");
		replay(filterChain, userDetailsService);
		setupAuthorizationHeader(request, createAuthorizationHeaderV2Value(TEST_AUTH_TOKEN,
				TEST_PASSWORD, request, Instant.now()));
		try {
			filter.doFilter(request, response, filterChain);
		} finally {
			verify(filterChain, userDetailsService);
		}
	}

	@Test(expected = BadCredentialsException.class)
	public void badPasswordV2() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/mock/path/here");
		final Instant now = Instant.now();
		request.addHeader("Date", AUTHORIZATION_DATE_HEADER_FORMATTER.format(now));
		setupAuthorizationHeader(request,
				createAuthorizationHeaderV2Value(TEST_AUTH_TOKEN, "foobar", request, now));
		expect(userDetailsService.loadUserByUsername(TEST_AUTH_TOKEN)).andReturn(userDetails);
		replay(filterChain, userDetailsService);
		try {
			filter.doFilter(request, response, filterChain);
		} finally {
			verify(filterChain, userDetailsService);
		}
	}

	@Test(expected = BadCredentialsException.class)
	public void tooMuchSkewV2() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/mock/path/here");
		final Instant now = Instant.ofEpochMilli(System.currentTimeMillis() - 16L * 60L * 1000L);
		request.addHeader("Date", AUTHORIZATION_DATE_HEADER_FORMATTER.format(now));
		setupAuthorizationHeader(request,
				createAuthorizationHeaderV2Value(TEST_AUTH_TOKEN, TEST_PASSWORD, request, now));
		expect(userDetailsService.loadUserByUsername(TEST_AUTH_TOKEN)).andReturn(userDetails);
		replay(filterChain, userDetailsService);
		try {
			filter.doFilter(request, response, filterChain);
		} finally {
			verify(filterChain, userDetailsService);
		}
	}

	@Test
	public void tokenWithEqualSignV2() throws ServletException, IOException {
		final String tokenId = "2^=3^rz}fgu0twxj;*fb";
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/mock/path/here");
		final Instant now = Instant.now();
		request.addHeader("Date", AUTHORIZATION_DATE_HEADER_FORMATTER.format(now));
		setupAuthorizationHeader(request,
				createAuthorizationHeaderV2Value(tokenId, TEST_PASSWORD, request, now));
		filterChain.doFilter(anyObject(HttpServletRequest.class), same(response));
		expect(userDetailsService.loadUserByUsername(tokenId)).andReturn(userDetails);
		replay(filterChain, userDetailsService);
		filter.doFilter(request, response, filterChain);
		verify(filterChain, userDetailsService);
		assertEquals(HttpServletResponse.SC_OK, response.getStatus());
		validateAuthentication();
	}

	@Test
	public void simplePathWithXDateV2() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/mock/path/here");
		final Instant now = Instant.now();
		request.addHeader("X-SN-Date", AUTHORIZATION_DATE_HEADER_FORMATTER.format(now));
		setupAuthorizationHeader(request,
				createAuthorizationHeaderV2Value(TEST_AUTH_TOKEN, TEST_PASSWORD, request, now));
		filterChain.doFilter(anyObject(HttpServletRequest.class), same(response));
		expect(userDetailsService.loadUserByUsername(TEST_AUTH_TOKEN)).andReturn(userDetails);
		replay(filterChain, userDetailsService);
		filter.doFilter(request, response, filterChain);
		verify(filterChain, userDetailsService);
		assertEquals(HttpServletResponse.SC_OK, response.getStatus());
		validateAuthentication();
	}

	@Test
	public void pathWithQueryParamsV2() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/mock/path/here");
		Map<String, String> params = new HashMap<String, String>();
		params.put("foo", "bar");
		params.put("bar", "foo");
		params.put("zog", "dog");
		request.setParameters(params);
		final Instant now = Instant.now();
		request.addHeader("Date", AUTHORIZATION_DATE_HEADER_FORMATTER.format(now));
		setupAuthorizationHeader(request,
				createAuthorizationHeaderV2Value(TEST_AUTH_TOKEN, TEST_PASSWORD, request, now));
		filterChain.doFilter(anyObject(HttpServletRequest.class), same(response));
		expect(userDetailsService.loadUserByUsername(TEST_AUTH_TOKEN)).andReturn(userDetails);
		replay(filterChain, userDetailsService);
		filter.doFilter(request, response, filterChain);
		assertEquals(HttpServletResponse.SC_OK, response.getStatus());
		validateAuthentication();
		verify(filterChain, userDetailsService);
	}

	@Test
	public void contentTypeV2() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/mock/path/here");
		request.setContentType("application/x-www-form-urlencoded; charset=UTF-8");
		Map<String, String> params = new HashMap<String, String>();
		params.put("foo", "bar");
		params.put("bar", "foo");
		params.put("zog", "dog");
		request.setParameters(params);
		final Instant now = Instant.now();
		request.addHeader("Date", AUTHORIZATION_DATE_HEADER_FORMATTER.format(now));
		setupAuthorizationHeader(request,
				createAuthorizationHeaderV2Value(TEST_AUTH_TOKEN, TEST_PASSWORD, request, now));
		filterChain.doFilter(anyObject(HttpServletRequest.class), same(response));
		expect(userDetailsService.loadUserByUsername(TEST_AUTH_TOKEN)).andReturn(userDetails);
		replay(filterChain, userDetailsService);
		filter.doFilter(request, response, filterChain);
		assertEquals(HttpServletResponse.SC_OK, response.getStatus());
		validateAuthentication();
		verify(filterChain, userDetailsService);
	}

	@Test
	public void contentMD5HexV2() throws ServletException, IOException {
		final String contentType = "application/json; charset=UTF-8";
		final String content = "{\"foo\":\"bar\"}";
		final String contentMD5 = "9bb58f26192e4ba00f01e2e7b136bbd8";
		final byte[] bodyContent = content.getBytes("UTF-8");
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/mock/path/here");
		request.setContentType(contentType);
		request.setContent(bodyContent);
		request.addHeader("Content-MD5", contentMD5);
		final Instant now = Instant.now();
		request.addHeader("Date", AUTHORIZATION_DATE_HEADER_FORMATTER.format(now));
		setupAuthorizationHeader(request, createAuthorizationHeaderV2Value(TEST_AUTH_TOKEN,
				TEST_PASSWORD, request, now, bodyContent));
		filterChain.doFilter(anyObject(HttpServletRequest.class), same(response));
		expect(userDetailsService.loadUserByUsername(TEST_AUTH_TOKEN)).andReturn(userDetails);
		replay(filterChain, userDetailsService);
		filter.doFilter(request, response, filterChain);
		assertEquals(HttpServletResponse.SC_OK, response.getStatus());
		validateAuthentication();
		verify(filterChain, userDetailsService);
	}

	@Test(expected = BadCredentialsException.class)
	public void invalidContentMD5HexV2() throws ServletException, IOException {
		final String contentType = "application/json; charset=UTF-8";
		final String content = "{\"foo\":\"bar\"}";
		final String contentMD5 = "9bb58f26192e4ba00f01e2e7b136bbFF";
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/mock/path/here");
		request.setContentType(contentType);
		request.setContent(content.getBytes("UTF-8"));
		request.addHeader("Content-MD5", contentMD5);
		final Instant now = Instant.now();
		request.addHeader("Date", AUTHORIZATION_DATE_HEADER_FORMATTER.format(now));
		setupAuthorizationHeader(request,
				createAuthorizationHeaderV2Value(TEST_AUTH_TOKEN, TEST_PASSWORD, request, now));
		replay(filterChain, userDetailsService);
		try {
			filter.doFilter(request, response, filterChain);
		} finally {
			verify(filterChain, userDetailsService);
		}
	}

	@Test
	public void contentMD5Base64V2() throws ServletException, IOException {
		final String contentType = "application/json; charset=UTF-8";
		final String content = "{\"foo\":\"bar\"}";
		final String contentMD5 = "m7WPJhkuS6APAeLnsTa72A==";
		final byte[] bodyContent = content.getBytes("UTF-8");
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/mock/path/here");
		request.setContentType(contentType);
		request.setContent(bodyContent);
		request.addHeader("Content-MD5", contentMD5);
		final Instant now = Instant.now();
		request.addHeader("Date", AUTHORIZATION_DATE_HEADER_FORMATTER.format(now));
		setupAuthorizationHeader(request, createAuthorizationHeaderV2Value(TEST_AUTH_TOKEN,
				TEST_PASSWORD, request, now, bodyContent));
		filterChain.doFilter(anyObject(HttpServletRequest.class), same(response));
		expect(userDetailsService.loadUserByUsername(TEST_AUTH_TOKEN)).andReturn(userDetails);
		replay(filterChain, userDetailsService);
		filter.doFilter(request, response, filterChain);
		assertEquals(HttpServletResponse.SC_OK, response.getStatus());
		validateAuthentication();
		verify(filterChain, userDetailsService);
	}

	@Test
	public void digestSHA256V2() throws ServletException, IOException {
		final String contentType = "application/json; charset=UTF-8";
		final String content = "{\"foo\":\"bar\"}";
		final String digestSHA256 = "eji/gfOD9pQzrW6QDTWz4jhVk/dqe3q11DVbi6Qe4ks=";
		final byte[] bodyContent = content.getBytes("UTF-8");
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/mock/path/here");
		request.setContentType(contentType);
		request.setContent(bodyContent);
		request.addHeader("Digest", "sha-256=" + digestSHA256);
		final Instant now = Instant.now();
		request.addHeader("Date", AUTHORIZATION_DATE_HEADER_FORMATTER.format(now));
		setupAuthorizationHeader(request, createAuthorizationHeaderV2Value(TEST_AUTH_TOKEN,
				TEST_PASSWORD, request, now, bodyContent));
		filterChain.doFilter(anyObject(HttpServletRequest.class), same(response));
		expect(userDetailsService.loadUserByUsername(TEST_AUTH_TOKEN)).andReturn(userDetails);
		replay(filterChain, userDetailsService);
		filter.doFilter(request, response, filterChain);
		assertEquals(HttpServletResponse.SC_OK, response.getStatus());
		validateAuthentication();
		verify(filterChain, userDetailsService);
	}

	@Test(expected = BadCredentialsException.class)
	public void invalidDigestSHA256V2() throws ServletException, IOException {
		final String contentType = "application/json; charset=UTF-8";
		final String content = "{\"foo\":\"bar\"}";
		final String digestSHA256 = "Ix2SImWvBXHmqXTuPAaDHz16KeaOlIokZObv6cU+Ie8=";
		final byte[] bodyContent = content.getBytes("UTF-8");
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/mock/path/here");
		request.setContentType(contentType);
		request.setContent(bodyContent);
		request.addHeader("Digest", "sha-256=" + digestSHA256);
		final Instant now = Instant.now();
		request.addHeader("Date", AUTHORIZATION_DATE_HEADER_FORMATTER.format(now));
		setupAuthorizationHeader(request, createAuthorizationHeaderV2Value(TEST_AUTH_TOKEN,
				TEST_PASSWORD, request, now, bodyContent));
		replay(filterChain, userDetailsService);
		try {
			filter.doFilter(request, response, filterChain);
		} finally {
			verify(filterChain, userDetailsService);
		}
	}

}

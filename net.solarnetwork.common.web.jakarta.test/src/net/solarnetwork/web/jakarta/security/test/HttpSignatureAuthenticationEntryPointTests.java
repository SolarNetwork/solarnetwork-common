/* ==================================================================
 * HttpSignatureAuthenticationEntryPointTests.java - 20/09/2026 3:41:09 pm
 *
 * Copyright 2026 SolarNetwork.net Dev Team
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

package net.solarnetwork.web.jakarta.security.test;

import static org.assertj.core.api.BDDAssertions.and;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.ServletException;
import net.solarnetwork.security.http.sig.ContentDigest;
import net.solarnetwork.security.http.sig.HttpSignatureFields;
import net.solarnetwork.test.CommonTestUtils;
import net.solarnetwork.web.jakarta.security.AuthenticationScheme;
import net.solarnetwork.web.jakarta.security.HttpSignatureAuthenticationEntryPoint;
import net.solarnetwork.web.jakarta.security.HttpSignatureSettings;
import net.solarnetwork.web.jakarta.security.WebConstants;

/**
 * Test cases for the {@link HttpSignatureAuthenticationEntryPoint} class.
 *
 * @author matt
 * @version 1.0
 */
@SuppressWarnings("static-access")
public class HttpSignatureAuthenticationEntryPointTests {

	private static final String TEST_PATH = "/api/v1/sec/datum/list";

	private HttpSignatureSettings settings;
	private HttpSignatureAuthenticationEntryPoint entryPoint;
	private MockHttpServletRequest request;
	private MockHttpServletResponse response;
	private AuthenticationException authException;

	@Before
	public void setup() {
		settings = new HttpSignatureSettings();
		entryPoint = new HttpSignatureAuthenticationEntryPoint(settings);
		request = new MockHttpServletRequest("GET", TEST_PATH);
		response = new MockHttpServletResponse();
		authException = new BadCredentialsException(CommonTestUtils.randomString());
	}

	@Test
	public void commence_acceptSignature() throws IOException, ServletException {
		// WHEN
		entryPoint.commence(request, response, authException);

		// THEN
		// @formatter:off
		and.then(response.getHeader(HttpSignatureFields.ACCEPT_SIGNATURE_HEADER))
			.as("A rejected client is told the shape of an acceptable signature,"
					+ " per RFC 9421 5.1")
			.isEqualTo(HttpSignatureAuthenticationEntryPoint.DEFAULT_ACCEPT_SIGNATURE
					.formatted(HttpSignatureSettings.DEFAULT_TAG))
			.as("The configured tag is named, so the client knows how to tag its signature")
			.contains("tag=\"" + HttpSignatureSettings.DEFAULT_TAG + "\"")
			;
		// @formatter:on
	}

	@Test
	public void commence_acceptSignature_configuredTag() throws IOException, ServletException {
		// GIVEN
		final String tag = CommonTestUtils.randomString();
		settings.setTag(tag);

		// WHEN
		entryPoint.commence(request, response, authException);

		// THEN
		// @formatter:off
		and.then(response.getHeader(HttpSignatureFields.ACCEPT_SIGNATURE_HEADER))
			.as("The tag advertised is the configured one")
			.contains("tag=\"" + tag + "\"")
			;
		// @formatter:on
	}

	@Test
	public void commence_schemeDisabled() throws IOException, ServletException {
		// GIVEN
		settings.setEnabled(false);

		// WHEN
		entryPoint.commence(request, response, authException);

		// THEN
		// @formatter:off
		and.then(response.getHeader(HttpSignatureFields.ACCEPT_SIGNATURE_HEADER))
			.as("No signature is advertised when the scheme is disabled")
			.isNull()
			;
		and.then(response.getStatus())
			.as("The standard unauthorized response is still written")
			.isEqualTo(401)
			;
		// @formatter:on
	}

	@Test
	public void commence_acceptSignatureAlreadySet() throws IOException, ServletException {
		// GIVEN
		final String existing = "sig=(\"@method\");created;keyid";
		response.addHeader(HttpSignatureFields.ACCEPT_SIGNATURE_HEADER, existing);

		// WHEN
		entryPoint.commence(request, response, authException);

		// THEN
		// @formatter:off
		and.then(response.getHeaders(HttpSignatureFields.ACCEPT_SIGNATURE_HEADER))
			.as("An Accept-Signature the application already set is left alone")
			.containsExactly(existing)
			;
		// @formatter:on
	}

	@Test
	public void commence_unauthorizedResponse() throws IOException, ServletException {
		// WHEN
		entryPoint.commence(request, response, authException);

		// THEN
		// @formatter:off
		and.then(response.getStatus())
			.as("Status is unauthorized")
			.isEqualTo(401)
			;
		and.then(response.getHeader(HttpHeaders.WWW_AUTHENTICATE))
			.as("The SNWS2 scheme is offered when the request named no scheme")
			.isEqualTo(AuthenticationScheme.V2.getSchemeName())
			;
		and.then(response.getHeader(WebConstants.HEADER_ERROR_MESSAGE))
			.as("The authentication failure message is returned")
			.isEqualTo(authException.getMessage())
			;
		and.then(response.getContentType())
			.as("The response body is JSON")
			.isEqualTo(MediaType.APPLICATION_JSON_VALUE)
			;
		and.then(response.getContentAsString())
			.as("The response body reports the failure")
			.contains("\"success\":false")
			.contains(authException.getMessage())
			;
		// @formatter:on
	}

	@Test
	public void commence_unauthorizedResponse_v1Scheme() throws IOException, ServletException {
		// GIVEN
		request.addHeader(HttpHeaders.AUTHORIZATION,
				AuthenticationScheme.V1.getSchemeName() + " foo:bar");

		// WHEN
		entryPoint.commence(request, response, authException);

		// THEN
		// @formatter:off
		and.then(response.getHeader(HttpHeaders.WWW_AUTHENTICATE))
			.as("The scheme the request used is the one offered back")
			.isEqualTo(AuthenticationScheme.V1.getSchemeName())
			;
		// @formatter:on
	}

	@Test
	public void commence_corsHeaders() throws IOException, ServletException {
		// WHEN
		entryPoint.commence(request, response, authException);

		// THEN
		// @formatter:off
		and.then(response.getHeader("Access-Control-Allow-Origin"))
			.as("Any origin may read the response")
			.isEqualTo("*")
			;
		and.then(response.getHeader("Access-Control-Allow-Headers"))
			.as("The signature fields are allowed, so a browser client can send them")
			.contains(HttpSignatureFields.SIGNATURE_HEADER)
			.contains(HttpSignatureFields.SIGNATURE_INPUT_HEADER)
			.as("The content digest field is allowed too")
			.contains(ContentDigest.CONTENT_DIGEST_HEADER)
			;
		and.then(response.getHeader("Access-Control-Expose-Headers"))
			.as("Accept-Signature is exposed, so a browser client can read what is expected of it")
			.contains(HttpSignatureFields.ACCEPT_SIGNATURE_HEADER)
			;
		// @formatter:on
	}

	@Test
	public void commence_corsHeadersNotOverwritten() throws IOException, ServletException {
		// GIVEN
		response.addHeader("Access-Control-Allow-Origin", "https://example.com");

		// WHEN
		entryPoint.commence(request, response, authException);

		// THEN
		// @formatter:off
		and.then(response.getHeaders("Access-Control-Allow-Origin"))
			.as("A CORS header the application already set is authoritative")
			.containsExactly("https://example.com")
			;
		// @formatter:on
	}

	@Test
	public void commence_handledByResolver() throws IOException, ServletException {
		// GIVEN
		final HandlerExceptionResolver resolver = mock(HandlerExceptionResolver.class);
		entryPoint.setHandlerExceptionResolver(resolver);
		given(resolver.resolveException(any(), any(), any(), any())).willReturn(new ModelAndView());

		// WHEN
		entryPoint.commence(request, response, authException);

		// THEN
		then(resolver).should().resolveException(same(request), same(response), isNull(),
				same(authException));
		// @formatter:off
		and.then(response.getContentAsString())
			.as("A resolver that handles the exception owns the response body")
			.isEmpty()
			;
		and.then(response.getHeader(HttpSignatureFields.ACCEPT_SIGNATURE_HEADER))
			.as("The signature expectations are set before the resolver is consulted")
			.isNotNull()
			;
		// @formatter:on
	}

	@Test
	public void commence_declinedByResolver() throws IOException, ServletException {
		// GIVEN
		final HandlerExceptionResolver resolver = mock(HandlerExceptionResolver.class);
		entryPoint.setHandlerExceptionResolver(resolver);
		given(resolver.resolveException(any(), any(), any(), any())).willReturn(null);

		// WHEN
		entryPoint.commence(request, response, authException);

		// THEN
		then(resolver).should().resolveException(same(request), same(response), isNull(),
				same(authException));
		// @formatter:off
		and.then(response.getContentAsString())
			.as("A resolver that declines the exception leaves the JSON response to be written")
			.contains(authException.getMessage())
			;
		// @formatter:on
	}

}

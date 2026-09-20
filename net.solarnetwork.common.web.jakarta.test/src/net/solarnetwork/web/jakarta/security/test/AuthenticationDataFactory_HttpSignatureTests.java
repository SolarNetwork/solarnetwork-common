/* ==================================================================
 * AuthenticationDataFactory_HttpSignatureTests.java - 20/09/2026 4:05:52 pm
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

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.BDDAssertions.and;
import static org.assertj.core.api.BDDAssertions.from;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import net.solarnetwork.security.Snws2AuthorizationBuilder;
import net.solarnetwork.security.http.sig.HttpSignatureBuilder;
import net.solarnetwork.security.http.sig.HttpSignatureFields;
import net.solarnetwork.security.http.sig.SignatureComponent;
import net.solarnetwork.test.CommonTestUtils;
import net.solarnetwork.web.jakarta.security.AuthenticationData;
import net.solarnetwork.web.jakarta.security.AuthenticationDataFactory;
import net.solarnetwork.web.jakarta.security.AuthenticationDataHttpSignature;
import net.solarnetwork.web.jakarta.security.AuthenticationDataV2;
import net.solarnetwork.web.jakarta.security.HttpSignatureSettings;
import net.solarnetwork.web.jakarta.security.SecurityHttpServletRequestWrapper;

/**
 * Test cases for the RFC 9421 support in the {@link AuthenticationDataFactory}
 * class.
 *
 * @author matt
 * @version 1.0
 */
@SuppressWarnings("static-access")
public class AuthenticationDataFactory_HttpSignatureTests {

	private static final String TEST_HOST = "data.solarnetwork.net";
	private static final String TEST_PATH = "/api/v1/sec/datum/list";
	private static final int MAX_CONTENT_LENGTH = 65536;

	private String tokenId;
	private String tokenSecret;
	private HttpSignatureSettings settings;

	@Before
	public void setup() {
		tokenId = CommonTestUtils.randomString();
		tokenSecret = CommonTestUtils.randomString();
		settings = new HttpSignatureSettings();
	}

	private static MockHttpServletRequest httpRequest() {
		final MockHttpServletRequest request = new MockHttpServletRequest("GET", TEST_PATH);
		request.setScheme("https");
		request.setServerName(TEST_HOST);
		request.setServerPort(443);
		request.addHeader(HttpHeaders.HOST, TEST_HOST);
		return request;
	}

	private void addHttpSignature(MockHttpServletRequest request) {
		// @formatter:off
		final HttpSignatureBuilder builder = new HttpSignatureBuilder(tokenId)
				.method("GET")
				.uri("https://" + TEST_HOST + TEST_PATH)
				.created(Instant.now().truncatedTo(SECONDS))
				.covered(SignatureComponent.METHOD, SignatureComponent.AUTHORITY,
						SignatureComponent.PATH)
				;
		// @formatter:on
		request.addHeader(HttpSignatureFields.SIGNATURE_INPUT_HEADER,
				builder.signatureInputHeaderValue());
		request.addHeader(HttpSignatureFields.SIGNATURE_HEADER,
				builder.signatureHeaderValue(tokenSecret));
	}

	private static SecurityHttpServletRequestWrapper wrap(MockHttpServletRequest request) {
		return new SecurityHttpServletRequestWrapper(request, MAX_CONTENT_LENGTH);
	}

	@Test
	public void isHttpSignatureRequest() {
		// GIVEN
		final MockHttpServletRequest request = httpRequest();
		addHttpSignature(request);

		// THEN
		// @formatter:off
		then(AuthenticationDataFactory.isHttpSignatureRequest(request))
			.as("A Signature-Input field marks an RFC 9421 request, per RFC 9421 appendix A")
			.isTrue()
			;
		// @formatter:on
	}

	@Test
	public void isHttpSignatureRequest_none() {
		// THEN
		// @formatter:off
		then(AuthenticationDataFactory.isHttpSignatureRequest(httpRequest()))
			.as("A request with no Signature-Input field is not an RFC 9421 request")
			.isFalse()
			;
		// @formatter:on
	}

	@Test
	public void resolveHttpSignature() throws IOException {
		// GIVEN
		final MockHttpServletRequest request = httpRequest();
		addHttpSignature(request);

		// WHEN
		final AuthenticationData result = AuthenticationDataFactory
				.authenticationDataForAuthorizationHeader(wrap(request), settings);

		// THEN
		// @formatter:off
		then(result)
			.as("RFC 9421 data resolved from the Signature-Input field")
			.asInstanceOf(type(AuthenticationDataHttpSignature.class))
			.as("Token ID resolved from the signature key ID")
			.returns(tokenId, from(AuthenticationDataHttpSignature::getAuthTokenId))
			;
		and.then(((AuthenticationDataHttpSignature) result).verifySignature(tokenSecret))
			.as("The resolved data verifies against the token secret")
			.isTrue()
			;
		// @formatter:on
	}

	@Test
	public void resolveHttpSignature_defaultSettings() throws IOException {
		// GIVEN
		final MockHttpServletRequest request = httpRequest();
		addHttpSignature(request);

		// WHEN
		final AuthenticationData result = AuthenticationDataFactory
				.authenticationDataForAuthorizationHeader(wrap(request));

		// THEN
		// @formatter:off
		then(result)
			.as("RFC 9421 is accepted by the default profile settings")
			.isInstanceOf(AuthenticationDataHttpSignature.class)
			;
		// @formatter:on
	}

	@Test
	public void resolveHttpSignature_schemeDisabled() throws IOException {
		// GIVEN
		settings.setEnabled(false);
		final MockHttpServletRequest request = httpRequest();
		addHttpSignature(request);

		// WHEN
		final AuthenticationData result = AuthenticationDataFactory
				.authenticationDataForAuthorizationHeader(wrap(request), settings);

		// THEN
		// @formatter:off
		then(result)
			.as("A signed request is ignored when the scheme is disabled")
			.isNull()
			;
		// @formatter:on
	}

	@Test
	public void resolveNothing() throws IOException {
		// WHEN
		final AuthenticationData result = AuthenticationDataFactory
				.authenticationDataForAuthorizationHeader(wrap(httpRequest()), settings);

		// THEN
		// @formatter:off
		then(result)
			.as("A request with no credentials at all resolves nothing")
			.isNull()
			;
		// @formatter:on
	}

	@Test
	public void authorizationHeaderWinsOverHttpSignature() throws IOException {
		// GIVEN
		final Instant now = Instant.now();
		final String authHeader = new Snws2AuthorizationBuilder(tokenId).date(now).host(TEST_HOST)
				.path(TEST_PATH).build(tokenSecret);
		final MockHttpServletRequest request = httpRequest();
		request.addHeader(HttpHeaders.DATE, Date.from(now));
		request.addHeader(HttpHeaders.AUTHORIZATION, authHeader);
		addHttpSignature(request);

		// WHEN
		final AuthenticationData result = AuthenticationDataFactory
				.authenticationDataForAuthorizationHeader(wrap(request), settings);

		// THEN
		// @formatter:off
		then(result)
			.as("An Authorization header is an unambiguous statement of intent, so it wins")
			.isInstanceOf(AuthenticationDataV2.class)
			;
		// @formatter:on
	}

}

/* ==================================================================
 * AuthenticationDataHttpSignatureTests.java - 20/09/2026 3:02:47 pm
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

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.BDDAssertions.and;
import static org.assertj.core.api.BDDAssertions.from;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import org.apache.commons.codec.binary.Hex;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import net.solarnetwork.security.http.sig.ContentDigest;
import net.solarnetwork.security.http.sig.HttpSignatureAlgorithm;
import net.solarnetwork.security.http.sig.HttpSignatureBuilder;
import net.solarnetwork.security.http.sig.HttpSignatureFields;
import net.solarnetwork.security.http.sig.SignatureComponent;
import net.solarnetwork.test.CommonTestUtils;
import net.solarnetwork.web.jakarta.security.AuthenticationDataHttpSignature;
import net.solarnetwork.web.jakarta.security.AuthenticationScheme;
import net.solarnetwork.web.jakarta.security.HttpSignatureSettings;
import net.solarnetwork.web.jakarta.security.SecurityHttpServletRequestWrapper;
import net.solarnetwork.web.jakarta.security.WebConstants;

/**
 * Test cases for the {@link AuthenticationDataHttpSignature} class.
 *
 * @author matt
 * @version 1.0
 */
@SuppressWarnings("static-access")
public class AuthenticationDataHttpSignatureTests {

	private static final String TEST_HOST = "data.solarnetwork.net";
	private static final String TEST_PATH = "/api/v1/sec/datum/list";
	private static final String TEST_CONTENT = "{\"hello\":\"world\"}";
	private static final int MAX_CONTENT_LENGTH = 65536;

	/** A signature value for tests that never get as far as verifying one. */
	private static final String PLACEHOLDER_SIGNATURE = "sig1=:cGxhY2Vob2xkZXI=:";

	private String tokenId;
	private String tokenSecret;
	private HttpSignatureSettings settings;

	@Before
	public void setup() {
		// a token ID must not contain the key ID delimiter
		tokenId = CommonTestUtils.randomString();
		tokenSecret = CommonTestUtils.randomString();
		settings = new HttpSignatureSettings();
	}

	/*- ================================================================
	 * Test support
	 * ================================================================ */

	private static MockHttpServletRequest httpRequest(String method) {
		final MockHttpServletRequest request = new MockHttpServletRequest(method, TEST_PATH);
		request.setScheme("https");
		request.setServerName(TEST_HOST);
		request.setServerPort(443);
		request.addHeader(HttpHeaders.HOST, TEST_HOST);
		return request;
	}

	/**
	 * A builder for a request signed over nothing, for tests that declare their
	 * own coverage.
	 */
	private HttpSignatureBuilder uncoveredSignatureBuilder(String method) {
		// @formatter:off
		return new HttpSignatureBuilder(tokenId)
				.method(method)
				.uri("https://" + TEST_HOST + TEST_PATH)
				.created(Instant.now().truncatedTo(SECONDS))
				;
		// @formatter:on
	}

	/** A builder for a request signed over the minimum the profile demands. */
	private HttpSignatureBuilder signatureBuilder(String method) {
		// @formatter:off
		return uncoveredSignatureBuilder(method)
				.covered(SignatureComponent.METHOD, SignatureComponent.AUTHORITY,
						SignatureComponent.PATH)
				;
		// @formatter:on
	}

	private void addSignature(MockHttpServletRequest request, HttpSignatureBuilder builder) {
		request.addHeader(HttpSignatureFields.SIGNATURE_INPUT_HEADER,
				builder.signatureInputHeaderValue());
		request.addHeader(HttpSignatureFields.SIGNATURE_HEADER,
				builder.signatureHeaderValue(tokenSecret));
	}

	private static void addRawSignature(MockHttpServletRequest request, String signatureInput) {
		request.addHeader(HttpSignatureFields.SIGNATURE_INPUT_HEADER, signatureInput);
		request.addHeader(HttpSignatureFields.SIGNATURE_HEADER, PLACEHOLDER_SIGNATURE);
	}

	/**
	 * A hand-written {@code Signature-Input} value, so odd parameters can be
	 * tested.
	 */
	private static String rawSignatureInput(String parameters) {
		return rawSignatureInput("\"@method\" \"@authority\" \"@path\"", parameters);
	}

	private static String rawSignatureInput(String components, String parameters) {
		return "sig1=(" + components + ");" + parameters;
	}

	private static String keyId(String tokenId, LocalDate signingDate) {
		return tokenId + ":" + DateTimeFormatter.BASIC_ISO_DATE.format(signingDate);
	}

	private AuthenticationDataHttpSignature authData(MockHttpServletRequest request) throws IOException {
		return authData(request, null);
	}

	private AuthenticationDataHttpSignature authData(MockHttpServletRequest request, String explicitHost)
			throws IOException {
		return new AuthenticationDataHttpSignature(
				new SecurityHttpServletRequestWrapper(request, MAX_CONTENT_LENGTH), settings,
				explicitHost);
	}

	private static byte[] sha256(byte[] content) {
		try {
			return MessageDigest.getInstance("SHA-256").digest(content);
		} catch ( NoSuchAlgorithmException e ) {
			throw new IllegalStateException(e);
		}
	}

	/*- ================================================================
	 * Happy paths
	 * ================================================================ */

	@Test
	public void verify_get() throws IOException {
		// GIVEN
		final HttpSignatureBuilder builder = signatureBuilder("GET");
		final MockHttpServletRequest request = httpRequest("GET");
		addSignature(request, builder);

		// WHEN
		final AuthenticationDataHttpSignature result = authData(request);

		// THEN
		// @formatter:off
		then(result)
			.as("Scheme is the RFC 9421 signature scheme")
			.returns(AuthenticationScheme.HttpSignature,
					from(AuthenticationDataHttpSignature::getScheme))
			.as("Date comes from the 'created' signature parameter")
			.returns(builder.parameters().created(), from(AuthenticationDataHttpSignature::getDate))
			.as("Token ID extracted from the 'keyid' signature parameter")
			.returns(tokenId, from(AuthenticationDataHttpSignature::getAuthTokenId))
			.as("Label is the label of the signature used")
			.returns(HttpSignatureBuilder.DEFAULT_LABEL,
					from(AuthenticationDataHttpSignature::getLabel))
			.as("Algorithm defaults to HMAC-SHA256 when the signature declares none")
			.returns(HttpSignatureAlgorithm.HmacSha256,
					from(AuthenticationDataHttpSignature::getAlgorithm))
			.as("Signature data is the computed signature base")
			.returns(builder.signatureBase().value(),
					from(AuthenticationDataHttpSignature::getSignatureData))
			;
		and.then(result.getParameters().components())
			.as("Parameters are the components the signature declared")
			.extracting(SignatureComponent::name)
			.containsExactly(SignatureComponent.METHOD, SignatureComponent.AUTHORITY,
					SignatureComponent.PATH)
			;
		and.then(result.verifySignature(tokenSecret))
			.as("Signature verifies against the token secret it was signed with")
			.isTrue()
			;
		and.then(result.isDateValid(60_000L))
			.as("A signature created now is within the allowed date skew")
			.isTrue()
			;
		// @formatter:on
	}

	@Test
	public void verify_get_withQuery() throws IOException {
		// GIVEN
		final String query = "nodeId=123&sourceId=a%2Fb";
		// @formatter:off
		final HttpSignatureBuilder builder = new HttpSignatureBuilder(tokenId)
				.method("GET")
				.uri("https://" + TEST_HOST + TEST_PATH + "?" + query)
				.created(Instant.now().truncatedTo(SECONDS))
				.covered(SignatureComponent.METHOD, SignatureComponent.AUTHORITY,
						SignatureComponent.PATH, SignatureComponent.QUERY)
				;
		// @formatter:on
		final MockHttpServletRequest request = httpRequest("GET");
		request.setQueryString(query);
		addSignature(request, builder);

		// WHEN
		final AuthenticationDataHttpSignature result = authData(request);

		// THEN
		// @formatter:off
		then(result.verifySignature(tokenSecret))
			.as("Signature over the query verifies")
			.isTrue()
			;
		// @formatter:on
	}

	@Test
	public void verify_get_targetUri() throws IOException {
		// GIVEN
		// @formatter:off
		final HttpSignatureBuilder builder = uncoveredSignatureBuilder("GET")
				.covered(SignatureComponent.METHOD, SignatureComponent.TARGET_URI)
				;
		// @formatter:on
		final MockHttpServletRequest request = httpRequest("GET");
		addSignature(request, builder);

		// WHEN
		final AuthenticationDataHttpSignature result = authData(request);

		// THEN
		// @formatter:off
		then(result.verifySignature(tokenSecret))
			.as("@target-uri covers the authority, path and query in one component")
			.isTrue()
			;
		// @formatter:on
	}

	@Test
	public void verify_post_withContent() throws IOException {
		// GIVEN
		final byte[] content = TEST_CONTENT.getBytes(UTF_8);
		// @formatter:off
		final HttpSignatureBuilder builder = uncoveredSignatureBuilder("POST")
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.contentDigest(content)
				.covered(SignatureComponent.METHOD, SignatureComponent.AUTHORITY,
						SignatureComponent.PATH, "content-type", "content-digest")
				;
		// @formatter:on
		final MockHttpServletRequest request = httpRequest("POST");
		request.setContentType(MediaType.APPLICATION_JSON_VALUE);
		request.setContent(content);
		request.addHeader(ContentDigest.CONTENT_DIGEST_HEADER,
				ContentDigest.fieldValue(ContentDigest.SHA_256, content));
		addSignature(request, builder);

		// WHEN
		final AuthenticationDataHttpSignature result = authData(request);

		// THEN
		// @formatter:off
		then(result.verifySignature(tokenSecret))
			.as("Signature over the request content digest verifies")
			.isTrue()
			;
		// @formatter:on
	}

	@Test
	public void verify_post_sha512ContentDigest() throws IOException {
		// GIVEN
		final byte[] content = TEST_CONTENT.getBytes(UTF_8);
		// @formatter:off
		final HttpSignatureBuilder builder = uncoveredSignatureBuilder("POST")
				.header(ContentDigest.CONTENT_DIGEST_HEADER,
						ContentDigest.fieldValue(ContentDigest.SHA_512, content))
				.covered(SignatureComponent.METHOD, SignatureComponent.AUTHORITY,
						SignatureComponent.PATH, "content-digest")
				;
		// @formatter:on
		final MockHttpServletRequest request = httpRequest("POST");
		request.setContent(content);
		request.addHeader(ContentDigest.CONTENT_DIGEST_HEADER,
				ContentDigest.fieldValue(ContentDigest.SHA_512, content));
		addSignature(request, builder);

		// WHEN
		final AuthenticationDataHttpSignature result = authData(request);

		// THEN
		// @formatter:off
		then(result.verifySignature(tokenSecret))
			.as("A sha-512 Content-Digest is accepted just as sha-256 is")
			.isTrue()
			;
		// @formatter:on
	}

	@Test
	public void verify_post_legacyDigestHeader() throws IOException {
		// GIVEN
		final byte[] content = TEST_CONTENT.getBytes(UTF_8);
		// @formatter:off
		final HttpSignatureBuilder builder = uncoveredSignatureBuilder("POST")
				.contentDigest(content)
				.covered(SignatureComponent.METHOD, SignatureComponent.AUTHORITY,
						SignatureComponent.PATH, "content-digest")
				;
		// @formatter:on
		final MockHttpServletRequest request = httpRequest("POST");
		request.setContent(content);
		request.addHeader(ContentDigest.CONTENT_DIGEST_HEADER,
				ContentDigest.fieldValue(ContentDigest.SHA_256, content));
		request.addHeader("Digest", "sha-256=" + Hex.encodeHexString(sha256(content)));
		addSignature(request, builder);

		// WHEN
		final AuthenticationDataHttpSignature result = authData(request);

		// THEN
		// @formatter:off
		then(result.verifySignature(tokenSecret))
			.as("A legacy Digest header alongside Content-Digest is validated too")
			.isTrue()
			;
		// @formatter:on
	}

	@Test
	public void verify_explicitAlgorithm() throws IOException {
		// GIVEN
		// @formatter:off
		final HttpSignatureBuilder builder = signatureBuilder("GET")
				.algorithm(HttpSignatureAlgorithm.HmacSha256)
				;
		// @formatter:on
		final MockHttpServletRequest request = httpRequest("GET");
		addSignature(request, builder);

		// WHEN
		final AuthenticationDataHttpSignature result = authData(request);

		// THEN
		// @formatter:off
		then(result)
			.as("The declared algorithm is used")
			.returns(HttpSignatureAlgorithm.HmacSha256,
					from(AuthenticationDataHttpSignature::getAlgorithm))
			;
		and.then(result.verifySignature(tokenSecret))
			.as("Signature declaring a supported algorithm verifies")
			.isTrue()
			;
		// @formatter:on
	}

	@Test
	public void verify_directSecretKey() throws IOException {
		// GIVEN
		// @formatter:off
		final HttpSignatureBuilder builder = signatureBuilder("GET")
				.derivedSigningKey(false)
				;
		// @formatter:on
		final MockHttpServletRequest request = httpRequest("GET");
		addSignature(request, builder);

		// WHEN
		final AuthenticationDataHttpSignature result = authData(request);

		// THEN
		// @formatter:off
		then(result)
			.as("Token ID is the whole key ID when no signing date is used")
			.returns(tokenId, from(AuthenticationDataHttpSignature::getAuthTokenId))
			;
		and.then(result.verifySignature(tokenSecret))
			.as("Signature made with the token secret directly verifies")
			.isTrue()
			;
		// @formatter:on
	}

	@Test
	public void verify_explicitHost() throws IOException {
		// GIVEN
		final String publicHost = "public.example.com";
		// @formatter:off
		final HttpSignatureBuilder builder = new HttpSignatureBuilder(tokenId)
				.method("GET")
				.uri("https://" + publicHost + TEST_PATH)
				.created(Instant.now().truncatedTo(SECONDS))
				.covered(SignatureComponent.METHOD, SignatureComponent.AUTHORITY,
						SignatureComponent.PATH)
				;
		// @formatter:on
		final MockHttpServletRequest request = httpRequest("GET");
		addSignature(request, builder);

		// WHEN
		final AuthenticationDataHttpSignature result = authData(request, publicHost);

		// THEN
		// @formatter:off
		then(result.verifySignature(tokenSecret))
			.as("An explicit host lets a signature made against the public host verify behind"
					+ " a proxy")
			.isTrue()
			;
		// @formatter:on
	}

	@Test
	public void signatureDigest() throws IOException {
		// GIVEN
		final HttpSignatureBuilder builder = signatureBuilder("GET");
		final MockHttpServletRequest request = httpRequest("GET");
		addSignature(request, builder);

		// WHEN
		final AuthenticationDataHttpSignature result = authData(request);

		// THEN
		// @formatter:off
		then(result.getSignatureDigest())
			.as("Presented digest is the Base64 signature from the Signature HTTP field")
			.isEqualTo(Base64.getEncoder().encodeToString(builder.sign(tokenSecret)))
			.as("Digest computed from the secret matches the presented one")
			.isEqualTo(result.computeSignatureDigest(tokenSecret))
			;
		// @formatter:on
	}

	@Test
	public void verify_wrongSecret() throws IOException {
		// GIVEN
		final HttpSignatureBuilder builder = signatureBuilder("GET");
		final MockHttpServletRequest request = httpRequest("GET");
		addSignature(request, builder);

		// WHEN
		final AuthenticationDataHttpSignature result = authData(request);

		// THEN
		// @formatter:off
		then(result.verifySignature(CommonTestUtils.randomString()))
			.as("Signature does not verify against a different token secret")
			.isFalse()
			;
		// @formatter:on
	}

	/*- ================================================================
	 * Signature selection
	 * ================================================================ */

	@Test
	public void select_taggedSignature() throws IOException {
		// GIVEN
		// @formatter:off
		final HttpSignatureBuilder wanted = signatureBuilder("GET")
				.label("sn")
				.tag(HttpSignatureSettings.DEFAULT_TAG)
				;
		final HttpSignatureBuilder other = signatureBuilder("GET")
				.label("other")
				.tag("some-other-application")
				;
		// @formatter:on
		final MockHttpServletRequest request = httpRequest("GET");
		addSignature(request, other);
		addSignature(request, wanted);

		// WHEN
		final AuthenticationDataHttpSignature result = authData(request);

		// THEN
		// @formatter:off
		then(result)
			.as("The signature carrying the configured tag is the one authenticated")
			.returns("sn", from(AuthenticationDataHttpSignature::getLabel))
			;
		and.then(result.verifySignature(tokenSecret))
			.as("The selected signature verifies")
			.isTrue()
			;
		// @formatter:on
	}

	@Test
	public void select_soleUntaggedSignature() throws IOException {
		// GIVEN
		final MockHttpServletRequest request = httpRequest("GET");
		addSignature(request, signatureBuilder("GET").label("only"));

		// WHEN
		final AuthenticationDataHttpSignature result = authData(request);

		// THEN
		// @formatter:off
		then(result)
			.as("A lone untagged signature is used when a tag is not required")
			.returns("only", from(AuthenticationDataHttpSignature::getLabel))
			;
		// @formatter:on
	}

	@Test
	public void select_ambiguousTag() {
		// GIVEN
		final MockHttpServletRequest request = httpRequest("GET");
		addSignature(request,
				signatureBuilder("GET").label("one").tag(HttpSignatureSettings.DEFAULT_TAG));
		addSignature(request,
				signatureBuilder("GET").label("two").tag(HttpSignatureSettings.DEFAULT_TAG));

		// THEN
		// @formatter:off
		thenThrownBy(() -> authData(request))
			.as("An ambiguous choice between tagged signatures is refused, per RFC 9421 7.2.6")
			.isInstanceOf(BadCredentialsException.class)
			.hasMessageContaining("exactly one signature may claim the tag")
			;
		// @formatter:on
	}

	@Test
	public void select_requireTag_noneTagged() {
		// GIVEN
		settings.setRequireTag(true);
		final MockHttpServletRequest request = httpRequest("GET");
		addSignature(request, signatureBuilder("GET"));

		// THEN
		// @formatter:off
		thenThrownBy(() -> authData(request))
			.as("An untagged signature is refused when the tag is required")
			.isInstanceOf(BadCredentialsException.class)
			.hasMessageContaining("which is required")
			;
		// @formatter:on
	}

	@Test
	public void select_multipleUntagged() {
		// GIVEN
		final MockHttpServletRequest request = httpRequest("GET");
		addSignature(request, signatureBuilder("GET").label("one"));
		addSignature(request, signatureBuilder("GET").label("two"));

		// THEN
		// @formatter:off
		thenThrownBy(() -> authData(request))
			.as("Without a tag to choose by, more than one signature is ambiguous")
			.isInstanceOf(BadCredentialsException.class)
			.hasMessageContaining("tag the one to authenticate")
			;
		// @formatter:on
	}

	@Test
	public void select_signatureMissingForLabel() {
		// GIVEN
		final MockHttpServletRequest request = httpRequest("GET");
		request.addHeader(HttpSignatureFields.SIGNATURE_INPUT_HEADER,
				signatureBuilder("GET").label("sig1").signatureInputHeaderValue());
		request.addHeader(HttpSignatureFields.SIGNATURE_HEADER,
				signatureBuilder("GET").label("different").signatureHeaderValue(tokenSecret));

		// THEN
		// @formatter:off
		thenThrownBy(() -> authData(request))
			.as("A Signature-Input with no matching Signature member is refused")
			.isInstanceOf(BadCredentialsException.class)
			.hasMessageContaining("is missing from the Signature HTTP field")
			;
		// @formatter:on
	}

	@Test
	public void select_malformedSignatureInput() {
		// GIVEN
		final MockHttpServletRequest request = httpRequest("GET");
		addRawSignature(request, "this is not a structured field dictionary");

		// THEN
		// @formatter:off
		thenThrownBy(() -> authData(request))
			.as("A Signature-Input that cannot be parsed is a credentials problem")
			.isInstanceOf(BadCredentialsException.class)
			;
		// @formatter:on
	}

	@Test
	public void select_noCreated() {
		// GIVEN
		final MockHttpServletRequest request = httpRequest("GET");
		addRawSignature(request, rawSignatureInput("keyid=\"%s\"".formatted(tokenId)));

		// THEN
		// @formatter:off
		thenThrownBy(() -> authData(request))
			.as("A signature with no 'created' parameter has no request date, so is refused")
			.isInstanceOf(BadCredentialsException.class)
			.hasMessageContaining("has no 'created' parameter")
			;
		// @formatter:on
	}

	/*- ================================================================
	 * Signature parameters
	 * ================================================================ */

	@Test
	public void algorithm_unsupported() {
		// GIVEN
		final MockHttpServletRequest request = httpRequest("GET");
		addRawSignature(request, rawSignatureInput("created=%d;alg=\"rsa-pss-sha512\";keyid=\"%s\""
				.formatted(Instant.now().getEpochSecond(), tokenId)));

		// THEN
		// @formatter:off
		thenThrownBy(() -> authData(request))
			.as("An algorithm outside the supported set is refused rather than ignored")
			.isInstanceOf(BadCredentialsException.class)
			.hasMessageContaining("[rsa-pss-sha512] is not supported")
			;
		// @formatter:on
	}

	@Test
	public void keyId_missing() {
		// GIVEN
		final MockHttpServletRequest request = httpRequest("GET");
		addRawSignature(request,
				rawSignatureInput("created=%d".formatted(Instant.now().getEpochSecond())));

		// THEN
		// @formatter:off
		thenThrownBy(() -> authData(request))
			.as("A signature with no 'keyid' parameter names no token, so is refused")
			.isInstanceOf(BadCredentialsException.class)
			.hasMessageContaining("no 'keyid' parameter")
			;
		// @formatter:on
	}

	@Test
	public void keyId_invalidSigningDate() {
		// GIVEN
		final MockHttpServletRequest request = httpRequest("GET");
		addRawSignature(request, rawSignatureInput("created=%d;keyid=\"%s:not-a-date\""
				.formatted(Instant.now().getEpochSecond(), tokenId)));

		// THEN
		// @formatter:off
		thenThrownBy(() -> authData(request))
			.as("A key ID with an unparsable signing date is refused")
			.isInstanceOf(BadCredentialsException.class)
			.hasMessageContaining("is not a valid YYYYMMDD date")
			;
		// @formatter:on
	}

	@Test
	public void keyId_directSecretKeyNotAllowed() {
		// GIVEN
		settings.setAllowDirectSecretKey(false);
		final MockHttpServletRequest request = httpRequest("GET");
		addSignature(request, signatureBuilder("GET").derivedSigningKey(false));

		// THEN
		// @formatter:off
		thenThrownBy(() -> authData(request))
			.as("A bare token ID key is refused when a derived signing key is required")
			.isInstanceOf(BadCredentialsException.class)
			.hasMessageContaining("must be of the form 'tokenId:YYYYMMDD'")
			;
		// @formatter:on
	}

	@Test
	public void keyId_derivedSigningKeyNotAllowed() {
		// GIVEN
		settings.setAllowDerivedSigningKey(false);
		final MockHttpServletRequest request = httpRequest("GET");
		addSignature(request, signatureBuilder("GET"));

		// THEN
		// @formatter:off
		thenThrownBy(() -> authData(request))
			.as("A date-derived key is refused when only the token ID is accepted")
			.isInstanceOf(BadCredentialsException.class)
			.hasMessageContaining("must be the token ID alone")
			;
		// @formatter:on
	}

	@Test
	public void keyId_derivedSigningKey_oldestAllowed() throws IOException {
		// GIVEN
		final Instant created = Instant.now().truncatedTo(SECONDS);
		final LocalDate signingDate = created.atZone(ZoneOffset.UTC).toLocalDate()
				.minusDays(settings.getDerivedSigningKeyMaxDays() - 1L);
		final MockHttpServletRequest request = httpRequest("GET");
		addRawSignature(request, rawSignatureInput("created=%d;keyid=\"%s\""
				.formatted(created.getEpochSecond(), keyId(tokenId, signingDate))));

		// WHEN
		final AuthenticationDataHttpSignature result = authData(request);

		// THEN
		// @formatter:off
		then(result)
			.as("A signing key on its last valid day is accepted")
			.returns(tokenId, from(AuthenticationDataHttpSignature::getAuthTokenId))
			;
		// @formatter:on
	}

	@Test
	public void keyId_derivedSigningKey_tooOld() {
		// GIVEN
		final Instant created = Instant.now().truncatedTo(SECONDS);
		final LocalDate signingDate = created.atZone(ZoneOffset.UTC).toLocalDate()
				.minusDays(settings.getDerivedSigningKeyMaxDays());
		final MockHttpServletRequest request = httpRequest("GET");
		addRawSignature(request, rawSignatureInput("created=%d;keyid=\"%s\""
				.formatted(created.getEpochSecond(), keyId(tokenId, signingDate))));

		// THEN
		// @formatter:off
		thenThrownBy(() -> authData(request))
			.as("A signing key older than the configured lifetime is refused")
			.isInstanceOf(BadCredentialsException.class)
			.hasMessageContaining("is not valid for a signature created on")
			;
		// @formatter:on
	}

	@Test
	public void keyId_derivedSigningKey_oneDayAhead() throws IOException {
		// GIVEN
		final Instant created = Instant.now().truncatedTo(SECONDS);
		final LocalDate signingDate = created.atZone(ZoneOffset.UTC).toLocalDate().plusDays(1);
		final MockHttpServletRequest request = httpRequest("GET");
		addRawSignature(request, rawSignatureInput("created=%d;keyid=\"%s\""
				.formatted(created.getEpochSecond(), keyId(tokenId, signingDate))));

		// WHEN
		final AuthenticationDataHttpSignature result = authData(request);

		// THEN
		// @formatter:off
		then(result)
			.as("A signing key a day ahead is accepted, to allow for clock skew across time zones")
			.returns(tokenId, from(AuthenticationDataHttpSignature::getAuthTokenId))
			;
		// @formatter:on
	}

	@Test
	public void keyId_derivedSigningKey_twoDaysAhead() {
		// GIVEN
		final Instant created = Instant.now().truncatedTo(SECONDS);
		final LocalDate signingDate = created.atZone(ZoneOffset.UTC).toLocalDate().plusDays(2);
		final MockHttpServletRequest request = httpRequest("GET");
		addRawSignature(request, rawSignatureInput("created=%d;keyid=\"%s\""
				.formatted(created.getEpochSecond(), keyId(tokenId, signingDate))));

		// THEN
		// @formatter:off
		thenThrownBy(() -> authData(request))
			.as("A signing key further ahead than clock skew allows is refused")
			.isInstanceOf(BadCredentialsException.class)
			.hasMessageContaining("is not valid for a signature created on")
			;
		// @formatter:on
	}

	@Test
	public void created_notAnInteger() {
		// GIVEN
		final MockHttpServletRequest request = httpRequest("GET");
		addRawSignature(request,
				rawSignatureInput("created=\"yesterday\";keyid=\"%s\"".formatted(tokenId)));

		// THEN
		// @formatter:off
		thenThrownBy(() -> authData(request))
			.as("A 'created' parameter of the wrong structured field type is refused")
			.isInstanceOf(BadCredentialsException.class)
			.hasMessageContaining("must be an Integer")
			;
		// @formatter:on
	}

	@Test
	public void algorithm_notAString() {
		// GIVEN
		final MockHttpServletRequest request = httpRequest("GET");
		addRawSignature(request, rawSignatureInput(
				"created=%d;alg=42;keyid=\"%s\"".formatted(Instant.now().getEpochSecond(), tokenId)));

		// THEN
		// @formatter:off
		thenThrownBy(() -> authData(request))
			.as("An 'alg' parameter of the wrong structured field type is refused")
			.isInstanceOf(BadCredentialsException.class)
			.hasMessageContaining("must be a String")
			;
		// @formatter:on
	}

	@Test
	public void keyId_notAString() {
		// GIVEN
		final MockHttpServletRequest request = httpRequest("GET");
		addRawSignature(request,
				rawSignatureInput("created=%d;keyid=42".formatted(Instant.now().getEpochSecond())));

		// THEN
		// @formatter:off
		thenThrownBy(() -> authData(request))
			.as("A 'keyid' parameter of the wrong structured field type is refused")
			.isInstanceOf(BadCredentialsException.class)
			.hasMessageContaining("must be a String")
			;
		// @formatter:on
	}

	@Test
	public void expires_notAnInteger() {
		// GIVEN
		final MockHttpServletRequest request = httpRequest("GET");
		addRawSignature(request, rawSignatureInput("created=%d;expires=\"soon\";keyid=\"%s\""
				.formatted(Instant.now().getEpochSecond(), tokenId)));

		// THEN
		// @formatter:off
		thenThrownBy(() -> authData(request))
			.as("An 'expires' parameter of the wrong structured field type is refused")
			.isInstanceOf(BadCredentialsException.class)
			.hasMessageContaining("must be an Integer")
			;
		// @formatter:on
	}

	@Test
	public void expires_past() {
		// GIVEN
		final Instant created = Instant.now().truncatedTo(SECONDS).minusSeconds(120);
		final MockHttpServletRequest request = httpRequest("GET");
		addSignature(request, signatureBuilder("GET").created(created).expires(created.plusSeconds(30)));

		// THEN
		// @formatter:off
		thenThrownBy(() -> authData(request))
			.as("A signature past its 'expires' parameter is refused")
			.isInstanceOf(BadCredentialsException.class)
			.hasMessageContaining("The signature expired at")
			;
		// @formatter:on
	}

	@Test
	public void expires_future() throws IOException {
		// GIVEN
		final Instant created = Instant.now().truncatedTo(SECONDS);
		final MockHttpServletRequest request = httpRequest("GET");
		addSignature(request,
				signatureBuilder("GET").created(created).expires(created.plusSeconds(300)));

		// WHEN
		final AuthenticationDataHttpSignature result = authData(request);

		// THEN
		// @formatter:off
		then(result.verifySignature(tokenSecret))
			.as("A signature within its 'expires' parameter verifies")
			.isTrue()
			;
		// @formatter:on
	}

	/*- ================================================================
	 * Coverage floor
	 * ================================================================ */

	@Test
	public void coverage_methodRequired() {
		// GIVEN
		final MockHttpServletRequest request = httpRequest("GET");
		addSignature(request, uncoveredSignatureBuilder("GET").covered(SignatureComponent.AUTHORITY,
				SignatureComponent.PATH));

		// THEN
		// @formatter:off
		thenThrownBy(() -> authData(request))
			.as("A signature that does not cover the method is below the coverage floor")
			.isInstanceOf(BadCredentialsException.class)
			.hasMessageContaining("[@method] component must be covered")
			;
		// @formatter:on
	}

	@Test
	public void coverage_authorityRequired() {
		// GIVEN
		final MockHttpServletRequest request = httpRequest("GET");
		addSignature(request, uncoveredSignatureBuilder("GET").covered(SignatureComponent.METHOD,
				SignatureComponent.PATH));

		// THEN
		// @formatter:off
		thenThrownBy(() -> authData(request))
			.as("A signature that does not cover the authority is below the coverage floor")
			.isInstanceOf(BadCredentialsException.class)
			.hasMessageContaining("[@authority] component must be covered")
			;
		// @formatter:on
	}

	@Test
	public void coverage_pathRequired() {
		// GIVEN
		final MockHttpServletRequest request = httpRequest("GET");
		addSignature(request, uncoveredSignatureBuilder("GET").covered(SignatureComponent.METHOD,
				SignatureComponent.AUTHORITY));

		// THEN
		// @formatter:off
		thenThrownBy(() -> authData(request))
			.as("A signature that does not cover the path is below the coverage floor")
			.isInstanceOf(BadCredentialsException.class)
			.hasMessageContaining("[@path] component must be covered")
			;
		// @formatter:on
	}

	@Test
	public void coverage_queryRequiredWhenPresent() {
		// GIVEN
		final MockHttpServletRequest request = httpRequest("GET");
		request.setQueryString("nodeId=123");
		addSignature(request, signatureBuilder("GET"));

		// THEN
		// @formatter:off
		thenThrownBy(() -> authData(request))
			.as("A request with a query must have it covered")
			.isInstanceOf(BadCredentialsException.class)
			.hasMessageContaining("[@query] component must be covered")
			;
		// @formatter:on
	}

	@Test
	public void coverage_contentDigestRequiredWhenContentPresent() {
		// GIVEN
		final MockHttpServletRequest request = httpRequest("POST");
		request.setContent(TEST_CONTENT.getBytes(UTF_8));
		addSignature(request, signatureBuilder("POST"));

		// THEN
		// @formatter:off
		thenThrownBy(() -> authData(request))
			.as("A request with content must have its digest covered")
			.isInstanceOf(BadCredentialsException.class)
			.hasMessageContaining("[content-digest] component must be covered")
			;
		// @formatter:on
	}

	@Test
	public void coverage_contentTypeRequiredWhenPresent() {
		// GIVEN
		final MockHttpServletRequest request = httpRequest("GET");
		request.setContentType(MediaType.APPLICATION_JSON_VALUE);
		addSignature(request, signatureBuilder("GET"));

		// THEN
		// @formatter:off
		thenThrownBy(() -> authData(request))
			.as("A content type carries request meaning, so must be covered")
			.isInstanceOf(BadCredentialsException.class)
			.hasMessageContaining("[content-type] component must be covered")
			;
		// @formatter:on
	}

	@Test
	public void coverage_solarNetworkHeaderRequired() {
		// GIVEN
		final MockHttpServletRequest request = httpRequest("GET");
		request.addHeader(WebConstants.HEADER_DATE, "Tue, 20 Apr 2021 02:07:55 GMT");
		addSignature(request, signatureBuilder("GET"));

		// THEN
		// @formatter:off
		thenThrownBy(() -> authData(request))
			.as("Every X-SN-* header carries request meaning, so must be covered")
			.isInstanceOf(BadCredentialsException.class)
			.hasMessageContaining("[x-sn-date] component must be covered")
			;
		// @formatter:on
	}

	@Test
	public void coverage_solarNetworkHeaderCovered() throws IOException {
		// GIVEN
		final String dateValue = "Tue, 20 Apr 2021 02:07:55 GMT";
		// @formatter:off
		final HttpSignatureBuilder builder = uncoveredSignatureBuilder("GET")
				.header(WebConstants.HEADER_DATE, dateValue)
				.covered(SignatureComponent.METHOD, SignatureComponent.AUTHORITY,
						SignatureComponent.PATH, WebConstants.HEADER_DATE)
				;
		// @formatter:on
		final MockHttpServletRequest request = httpRequest("GET");
		request.addHeader(WebConstants.HEADER_DATE, dateValue);
		addSignature(request, builder);

		// WHEN
		final AuthenticationDataHttpSignature result = authData(request);

		// THEN
		// @formatter:off
		then(result.verifySignature(tokenSecret))
			.as("A signature covering the X-SN-* headers verifies")
			.isTrue()
			;
		// @formatter:on
	}

	@Test
	public void coverage_componentNotInRequest() {
		// GIVEN
		final MockHttpServletRequest request = httpRequest("GET");
		addRawSignature(request, rawSignatureInput("\"@method\" \"@authority\" \"@path\" \"x-missing\"",
				"created=%d;keyid=\"%s\"".formatted(Instant.now().getEpochSecond(), tokenId)));

		// THEN
		// @formatter:off
		thenThrownBy(() -> authData(request))
			.as("A signature covering a field the request does not carry cannot be verified")
			.isInstanceOf(BadCredentialsException.class)
			.hasMessageContaining("is not present in the request")
			;
		// @formatter:on
	}

	/*- ================================================================
	 * Content digest validation
	 * ================================================================ */

	@Test
	public void contentDigest_mismatch() {
		// GIVEN
		final byte[] content = TEST_CONTENT.getBytes(UTF_8);
		// @formatter:off
		final HttpSignatureBuilder builder = uncoveredSignatureBuilder("POST")
				.contentDigest(content)
				.covered(SignatureComponent.METHOD, SignatureComponent.AUTHORITY,
						SignatureComponent.PATH, "content-digest")
				;
		// @formatter:on
		final MockHttpServletRequest request = httpRequest("POST");
		request.setContent(content);
		request.addHeader(ContentDigest.CONTENT_DIGEST_HEADER,
				ContentDigest.fieldValue(ContentDigest.SHA_256, "something else".getBytes(UTF_8)));
		addSignature(request, builder);

		// THEN
		// @formatter:off
		thenThrownBy(() -> authData(request))
			.as("A Content-Digest that does not match the request content is refused")
			.isInstanceOf(BadCredentialsException.class)
			.hasMessageContaining("does not match the request content")
			;
		// @formatter:on
	}

	@Test
	public void contentDigest_unsupportedAlgorithm() {
		// GIVEN
		final byte[] content = TEST_CONTENT.getBytes(UTF_8);
		final String digest = "md5=:" + Base64.getEncoder().encodeToString(sha256(content)) + ":";
		// @formatter:off
		final HttpSignatureBuilder builder = uncoveredSignatureBuilder("POST")
				.header(ContentDigest.CONTENT_DIGEST_HEADER, digest)
				.covered(SignatureComponent.METHOD, SignatureComponent.AUTHORITY,
						SignatureComponent.PATH, "content-digest")
				;
		// @formatter:on
		final MockHttpServletRequest request = httpRequest("POST");
		request.setContent(content);
		request.addHeader(ContentDigest.CONTENT_DIGEST_HEADER, digest);
		addSignature(request, builder);

		// THEN
		// @formatter:off
		thenThrownBy(() -> authData(request))
			.as("A Content-Digest using no supported algorithm cannot be validated, so is refused")
			.isInstanceOf(BadCredentialsException.class)
			.hasMessageContaining("does not use a supported digest algorithm")
			;
		// @formatter:on
	}

	@Test
	public void contentDigest_malformed() {
		// GIVEN
		final MockHttpServletRequest request = httpRequest("GET");
		request.addHeader(ContentDigest.CONTENT_DIGEST_HEADER, "sha-256=not-a-byte-sequence");
		addSignature(request, signatureBuilder("GET"));

		// THEN
		// @formatter:off
		thenThrownBy(() -> authData(request))
			.as("A Content-Digest that cannot be parsed is a credentials problem")
			.isInstanceOf(BadCredentialsException.class)
			.hasMessageContaining(ContentDigest.CONTENT_DIGEST_HEADER)
			;
		// @formatter:on
	}

}

/* ==================================================================
 * HttpSignatureBuilderTests.java - 19/09/2026 11:22:04 am
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

package net.solarnetwork.security.http.sig.test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.BDDAssertions.then;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import org.junit.Test;
import net.solarnetwork.security.http.sig.HttpSignatureAlgorithm;
import net.solarnetwork.security.http.sig.HttpSignatureBuilder;
import net.solarnetwork.security.http.sig.HttpSignatureVerifier;
import net.solarnetwork.security.http.sig.SignatureBase;
import net.solarnetwork.security.http.sig.SignatureComponent;

/**
 * Test cases for the {@link HttpSignatureBuilder} class.
 *
 * <p>
 * These are the test cases from RFC 9421 appendix B.2, which apply to the
 * following request:
 * </p>
 *
 * <pre>
 * POST /foo?param=Value&amp;Pet=dog HTTP/1.1
 * Host: example.com
 * Date: Tue, 20 Apr 2021 02:07:55 GMT
 * Content-Type: application/json
 * Content-Digest: sha-512=:WZDPaVn/...:
 * Content-Length: 18
 *
 * {"hello": "world"}
 * </pre>
 *
 * @author matt
 * @version 1.0
 */
public class HttpSignatureBuilderTests {

	/** The {@literal test-shared-secret} from RFC 9421 appendix B.1.5. */
	private static final String TEST_SHARED_SECRET = """
			uzvJfB4u3N0Jy4T7NZ75MDVcr8zSTInedJtkgcu46YW4XByzNJjxBdtjUkdJPBtbmHhIDi6pcl8jsasjlTMtDQ==""";

	private static final String TEST_CONTENT = "{\"hello\": \"world\"}";

	private static final String TEST_CONTENT_DIGEST = """
			sha-512=:WZDPaVn/7XgHaAy8pmojAkGWoRx2UFChF41A2svX+TaPm+AbwAgBWnrIiYllu7BNNyealdVLvRwEmTHWXvJwew==:""";

	private static final Instant TEST_CREATED = Instant.ofEpochSecond(1618884473L);

	private static HttpSignatureBuilder testRequestBuilder(String tokenId) {
		// @formatter:off
		return new HttpSignatureBuilder(tokenId)
				.method("POST")
				.uri("https://example.com/foo?param=Value&Pet=dog")
				.header("Date", "Tue, 20 Apr 2021 02:07:55 GMT")
				.header("Content-Type", "application/json")
				.header("Content-Digest", TEST_CONTENT_DIGEST)
				.header("Content-Length", "18")
				.created(TEST_CREATED)
				// the RFC 9421 test cases use a bare key ID, so opt out of the
				// derived signing key SolarNetwork uses by default
				.derivedSigningKey(false)
				;
		// @formatter:on
	}

	@Test
	public void signatureBase_rfc9421_b22_selectiveCoveredComponents() {
		// GIVEN
		// @formatter:off
		final HttpSignatureBuilder builder = testRequestBuilder("test-key-rsa-pss")
				.label("sig-b22")
				.covered(SignatureComponent.of(SignatureComponent.AUTHORITY))
				.covered(SignatureComponent.of("content-digest"))
				.covered(SignatureComponent.of(SignatureComponent.QUERY_PARAM, Map.of("name", "Pet")))
				.tag("header-example")
				;
		// @formatter:on

		// WHEN
		final SignatureBase base = builder.signatureBase();

		// THEN
		final String expected = """
				"@authority": example.com
				"content-digest": %s
				"@query-param";name="Pet": dog
				"@signature-params": ("@authority" "content-digest" "@query-param";name="Pet")\
				;created=1618884473;keyid="test-key-rsa-pss";tag="header-example\""""
				.formatted(TEST_CONTENT_DIGEST);

		// @formatter:off
		then(base.value())
			.as("Signature base matches RFC 9421 appendix B.2.2")
			.isEqualTo(expected)
			;
		then(base.lines())
			.as("Signature base broken into one line per component, plus the parameters line")
			.hasSize(4)
			;
		// @formatter:on
	}

	@Test
	public void signatureBase_rfc9421_b23_fullCoverage() {
		// GIVEN
		// @formatter:off
		final HttpSignatureBuilder builder = testRequestBuilder("test-key-rsa-pss")
				.label("sig-b23")
				.covered("date", SignatureComponent.METHOD, SignatureComponent.PATH,
						SignatureComponent.QUERY, SignatureComponent.AUTHORITY, "content-type",
						"content-digest", "content-length")
				;
		// @formatter:on

		// WHEN
		final SignatureBase base = builder.signatureBase();

		// THEN
		final String expected = """
				"date": Tue, 20 Apr 2021 02:07:55 GMT
				"@method": POST
				"@path": /foo
				"@query": ?param=Value&Pet=dog
				"@authority": example.com
				"content-type": application/json
				"content-digest": %s
				"content-length": 18
				"@signature-params": ("date" "@method" "@path" "@query" "@authority" "content-type"\
				 "content-digest" "content-length");created=1618884473\
				;keyid="test-key-rsa-pss\"""".formatted(TEST_CONTENT_DIGEST);

		// @formatter:off
		then(base.value())
			.as("Signature base matches RFC 9421 appendix B.2.3")
			.isEqualTo(expected)
			;
		// @formatter:on
	}

	@Test
	public void sign_rfc9421_b25_hmacSha256() {
		// GIVEN
		// @formatter:off
		final HttpSignatureBuilder builder = testRequestBuilder("test-shared-secret")
				.label("sig-b25")
				.covered("date", SignatureComponent.AUTHORITY, "content-type")
				;
		// @formatter:on
		final byte[] signingKey = Base64.getDecoder().decode(TEST_SHARED_SECRET);

		// WHEN
		final SignatureBase base = builder.signatureBase();
		final byte[] signature = builder.signWithKey(signingKey);

		// THEN
		final String expectedBase = """
				"date": Tue, 20 Apr 2021 02:07:55 GMT
				"@authority": example.com
				"content-type": application/json
				"@signature-params": ("date" "@authority" "content-type")\
				;created=1618884473;keyid="test-shared-secret\"""";

		// @formatter:off
		then(base.value())
			.as("Signature base matches RFC 9421 appendix B.2.5")
			.isEqualTo(expectedBase)
			;
		then(Base64.getEncoder().encodeToString(signature))
			.as("hmac-sha256 signature matches RFC 9421 appendix B.2.5")
			.isEqualTo("pxcQw6G3AjtMBQjwo8XzkZf/bws5LelbaMk5rGIGtE8=")
			;
		then(HttpSignatureVerifier.verify(HttpSignatureAlgorithm.HmacSha256, signingKey, base,
				signature))
			.as("Signature verifies against the same key and base")
			.isTrue()
			;
		// @formatter:on
	}

	@Test
	public void signatureBase_rfc9421_b26_ed25519() {
		// GIVEN
		// the ed25519 algorithm is not supported yet, but the signature base for this
		// test case is, and it is the part both ends have to agree on
		// @formatter:off
		final HttpSignatureBuilder builder = testRequestBuilder("test-key-ed25519")
				.label("sig-b26")
				.covered("date", SignatureComponent.METHOD, SignatureComponent.PATH,
						SignatureComponent.AUTHORITY, "content-type", "content-length")
				;
		// @formatter:on

		// WHEN
		final SignatureBase base = builder.signatureBase();

		// THEN
		final String expected = """
				"date": Tue, 20 Apr 2021 02:07:55 GMT
				"@method": POST
				"@path": /foo
				"@authority": example.com
				"content-type": application/json
				"content-length": 18
				"@signature-params": ("date" "@method" "@path" "@authority" "content-type"\
				 "content-length");created=1618884473;keyid="test-key-ed25519\"""";

		// @formatter:off
		then(base.value())
			.as("Signature base matches RFC 9421 appendix B.2.6")
			.isEqualTo(expected)
			;
		// @formatter:on
	}

	@Test
	public void signatureInputAndSignatureHeaderValues() {
		// GIVEN
		// @formatter:off
		final HttpSignatureBuilder builder = testRequestBuilder("abc123")
				.label("sn")
				.covered(SignatureComponent.METHOD, SignatureComponent.AUTHORITY,
						SignatureComponent.PATH)
				.algorithm(HttpSignatureAlgorithm.HmacSha256)
				.tag("solarnetwork")
				;
		// @formatter:on

		// WHEN
		final String signatureInput = builder.signatureInputHeaderValue();
		final String signature = builder.signatureHeaderValue("s3cr3t");

		// THEN
		// @formatter:off
		then(signatureInput)
			.as("Signature-Input carries the label, covered components, and parameters in order")
			.isEqualTo("""
					sn=("@method" "@authority" "@path");created=1618884473;alg="hmac-sha256"\
					;keyid="abc123";tag="solarnetwork\"""")
			;
		then(signature)
			.as("Signature carries the label and a Byte Sequence value")
			.startsWith("sn=:")
			.endsWith(":")
			;
		// @formatter:on
	}

	@Test
	public void defaultSigningKey_isDerived() {
		// GIVEN
		final HttpSignatureBuilder builder = new HttpSignatureBuilder("abc123").created(TEST_CREATED);

		// WHEN
		final String keyId = builder.keyId();

		// THEN
		// @formatter:off
		then(keyId)
			.as("A derived signing key is used unless the caller opts out")
			.isEqualTo("abc123:20210420")
			;
		// @formatter:on
	}

	@Test
	public void derivedSigningKey_signingDateFollowsCreated() {
		// GIVEN
		// the signing date comes from the creation time, so the two must be settable
		// in either order
		final HttpSignatureBuilder before = new HttpSignatureBuilder("abc123").derivedSigningKey(true)
				.created(TEST_CREATED);
		final HttpSignatureBuilder after = new HttpSignatureBuilder("abc123").created(TEST_CREATED)
				.derivedSigningKey(true);

		// WHEN
		// THEN
		// @formatter:off
		then(before.keyId())
			.as("Setting the mode before the creation time picks up the creation date")
			.isEqualTo("abc123:20210420")
			.as("Order of configuration does not matter")
			.isEqualTo(after.keyId())
			;
		then(before.computeSigningKey("s3cr3t"))
			.as("The same signing key results either way")
			.isEqualTo(after.computeSigningKey("s3cr3t"))
			;
		// @formatter:on
	}

	@Test
	public void derivedSigningKey_differsFromTokenSecret() {
		// GIVEN
		final String tokenSecret = "s3cr3t";
		// @formatter:off
		final HttpSignatureBuilder direct = testRequestBuilder("abc123")
				.covered(SignatureComponent.METHOD)
				.derivedSigningKey(false)
				;
		final HttpSignatureBuilder derived = testRequestBuilder("abc123")
				.covered(SignatureComponent.METHOD)
				.derivedSigningKey(true)
				;
		// @formatter:on

		// WHEN
		final byte[] directKey = direct.computeSigningKey(tokenSecret);
		final byte[] derivedKey = derived.computeSigningKey(tokenSecret);

		// THEN
		// @formatter:off
		then(directKey)
			.as("Direct signing key is the UTF-8 token secret")
			.isEqualTo(tokenSecret.getBytes(UTF_8))
			;
		then(derivedKey)
			.as("Derived signing key is a HMAC-SHA256 digest")
			.hasSize(32)
			.as("Derived signing key is not the token secret")
			.isNotEqualTo(directKey)
			;
		then(derived.keyId())
			.as("Key ID carries the signing date of the creation time")
			.isEqualTo("abc123:20210420")
			;
		then(direct.keyId())
			.as("Key ID is the token ID alone when the secret is used directly")
			.isEqualTo("abc123")
			;
		// @formatter:on
	}

	@Test
	public void signature_matchesApiExplorerOutput() {
		// GIVEN
		// these values were produced by the SolarNetwork API Explorer, via the
		// solarnetwork-api-core HttpMessageSignatureBuilder, and pin the whole client
		// stack to this server implementation
		final String tokenId = "12345678901234567890";
		final String tokenSecret = "lsdjfpse9jfoeijfe09j";
		// @formatter:off
		final HttpSignatureBuilder builder = new HttpSignatureBuilder(tokenId)
				.label("sn")
				.method("GET")
				.uri("http://localhost:9082/solarquery/api/v1/sec/datum/stream/datum"
						+ "?nodeId=123&sourceId=meter")
				.created(Instant.ofEpochSecond(1789761471L))
				.tag("solarnetwork")
				.derivedSigningKey(false)
				.covered(SignatureComponent.METHOD, SignatureComponent.AUTHORITY,
						SignatureComponent.PATH, SignatureComponent.QUERY)
				;
		// @formatter:on

		// WHEN
		final SignatureBase base = builder.signatureBase();
		final byte[] signature = builder.sign(tokenSecret);

		// THEN
		final String expectedBase = """
				"@method": GET
				"@authority": localhost:9082
				"@path": /solarquery/api/v1/sec/datum/stream/datum
				"@query": ?nodeId=123&sourceId=meter
				"@signature-params": ("@method" "@authority" "@path" "@query")\
				;created=1789761471;keyid="12345678901234567890";tag="solarnetwork\"""";

		// @formatter:off
		then(base.value())
			.as("Signature base matches the API Explorer output")
			.isEqualTo(expectedBase)
			;
		then(Base64.getEncoder().encodeToString(signature))
			.as("Signature matches the API Explorer output")
			.isEqualTo("yt00kJV0rsRBmP7Pu7C8J2f+avzPAAzv1vEGX+SrCd8=")
			;
		// @formatter:on
	}

	@Test
	public void signature_matchesApiExplorerOutput_derivedSigningKey() {
		// GIVEN
		// the same request as above, signed with the derived signing key the API
		// Explorer uses by default
		final String tokenId = "12345678901234567890";
		final String tokenSecret = "lsdjfpse9jfoeijfe09j";
		// @formatter:off
		final HttpSignatureBuilder builder = new HttpSignatureBuilder(tokenId)
				.label("sn")
				.method("GET")
				.uri("http://localhost:9082/solarquery/api/v1/sec/datum/stream/datum"
						+ "?nodeId=123&sourceId=meter")
				.created(Instant.ofEpochSecond(1789761471L))
				.tag("solarnetwork")
				.covered(SignatureComponent.METHOD, SignatureComponent.AUTHORITY,
						SignatureComponent.PATH, SignatureComponent.QUERY)
				;
		// @formatter:on

		// WHEN
		final byte[] signature = builder.sign(tokenSecret);

		// THEN
		// @formatter:off
		then(builder.keyId())
			.as("Key ID carries the signing date")
			.isEqualTo("12345678901234567890:20260918")
			;
		then(Base64.getEncoder().encodeToString(signature))
			.as("Signature matches the API Explorer output")
			.isEqualTo("8gcZ/Rdd932qOF1Cm+hpu7M/hQQ92A3dCffFxtyTSWg=")
			;
		// @formatter:on
	}

	@Test
	public void content_digest_matchesRfc9421TestRequest() {
		// GIVEN
		final HttpSignatureBuilder builder = new HttpSignatureBuilder("abc123");

		// WHEN
		builder.contentDigest(TEST_CONTENT.getBytes(UTF_8));

		// THEN
		// @formatter:off
		then(builder.fieldValues("content-digest"))
			.as("A sha-256 Content-Digest field is set for the content")
			.containsExactly(
					"sha-256=:X48E9qOokqqrvdts8nOJRJN3OWDUoyWxBf7kbu9DBPE=:")
			;
		// @formatter:on
	}

	@Test
	public void content_digest_matchesRfc9421TestRequest_inputStream() throws IOException {
		// GIVEN
		final HttpSignatureBuilder builder = new HttpSignatureBuilder("abc123");

		// WHEN
		try (var input = new ByteArrayInputStream(TEST_CONTENT.getBytes(UTF_8))) {
			builder.contentDigest(input);
		}

		// THEN
		// @formatter:off
		then(builder.fieldValues("content-digest"))
			.as("A sha-256 Content-Digest field is set for the content")
			.containsExactly(
					"sha-256=:X48E9qOokqqrvdts8nOJRJN3OWDUoyWxBf7kbu9DBPE=:")
			;
		// @formatter:on
	}

}

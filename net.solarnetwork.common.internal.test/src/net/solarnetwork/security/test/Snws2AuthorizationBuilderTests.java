/* ==================================================================
 * AuthorizationV2BuilderTests.java - 25/04/2017 2:33:33 PM
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

package net.solarnetwork.security.test;

import static net.solarnetwork.security.AuthorizationUtils.EMPTY_STRING_SHA256_HEX;
import static net.solarnetwork.security.AuthorizationUtils.computeHmacSha256;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import net.solarnetwork.security.AuthorizationUtils;
import net.solarnetwork.security.Snws2AuthorizationBuilder;

/**
 * Test cases for the {@link Snws2AuthorizationBuilder} class.
 *
 * @author matt
 * @version 1.1
 */
public class Snws2AuthorizationBuilderTests {

	private static final String TEST_TOKEN_ID = "test-token-id";
	private static final String TEST_TOKEN_SECRET = "test-token-secret";

	private Instant getTestDate() {
		LocalDateTime date = LocalDateTime.of(2017, 4, 25, 14, 30, 0);
		return date.toInstant(ZoneOffset.UTC);
	}

	@Test
	public void simpleGet() {
		final Instant reqDate = getTestDate();
		Snws2AuthorizationBuilder builder = new Snws2AuthorizationBuilder(TEST_TOKEN_ID);
		builder.date(reqDate).host("localhost").path("/api/test");

		final String canonicalRequestData = builder.computeCanonicalRequestMessage();
		assertThat(canonicalRequestData,
				is("GET\n/api/test\n\ndate:Tue, 25 Apr 2017 14:30:00 GMT\nhost:localhost\ndate;host\n"
						+ AuthorizationUtils.EMPTY_STRING_SHA256_HEX));

		String key = Hex.encodeHexString(builder.computeSigningKey(reqDate, TEST_TOKEN_SECRET));
		assertThat(key, is("bf7885e8bd107a79f5c6e13001a4fa15fbd43221ad39ca47fde96191d302dbf4"));

		// @formatter:off
		String msg = builder.computeSignatureData(reqDate, canonicalRequestData);
		assertThat(msg, is(
				  "SNWS2-HMAC-SHA256\n"
				+ "20170425T143000Z\n"
				+ "1b5cf4c03e8e85f1c08a01ea1d4bd89cc550fc964134b59bb0accf9e5dbb26bf"));
		// @formatter:on

		final String result = builder.build(TEST_TOKEN_SECRET);
		assertThat(result, is(
				"SNWS2 Credential=test-token-id,SignedHeaders=date;host,Signature=4739139d3d370f147b6585795c309b1c6d7d7f59943081f7dd943f689cfa59a3"));
	}

	@Test
	public void xSnDate_manual() {
		final Instant reqDate = getTestDate();
		Snws2AuthorizationBuilder builder = new Snws2AuthorizationBuilder(TEST_TOKEN_ID);
		builder.date(reqDate).host("localhost").path("/api/test").header(
				AuthorizationUtils.SN_DATE_HEADER,
				AuthorizationUtils.AUTHORIZATION_DATE_HEADER_FORMATTER.format(reqDate));

		final String canonicalRequestData = builder.computeCanonicalRequestMessage();
		assertThat(canonicalRequestData, is(
				"GET\n/api/test\n\nhost:localhost\nx-sn-date:Tue, 25 Apr 2017 14:30:00 GMT\nhost;x-sn-date\n"
						+ EMPTY_STRING_SHA256_HEX));

		final String result = builder.build(TEST_TOKEN_SECRET);
		assertThat(result, is(
				"SNWS2 Credential=test-token-id,SignedHeaders=host;x-sn-date,Signature=c14fe9f67560fb9a37d2aa7c40b40c260a5936f999877e2469b8ddb1da7c0eb9"));
	}

	@Test
	public void xSnDate_toggleBefore() {
		final Instant reqDate = getTestDate();
		Snws2AuthorizationBuilder builder = new Snws2AuthorizationBuilder(TEST_TOKEN_ID);
		builder.useSnDate(true).date(reqDate).host("localhost").path("/api/test");

		final String canonicalRequestData = builder.computeCanonicalRequestMessage();
		assertThat(canonicalRequestData, is(
				"GET\n/api/test\n\nhost:localhost\nx-sn-date:Tue, 25 Apr 2017 14:30:00 GMT\nhost;x-sn-date\n"
						+ EMPTY_STRING_SHA256_HEX));

		final String result = builder.build(TEST_TOKEN_SECRET);
		assertThat(result, is(
				"SNWS2 Credential=test-token-id,SignedHeaders=host;x-sn-date,Signature=c14fe9f67560fb9a37d2aa7c40b40c260a5936f999877e2469b8ddb1da7c0eb9"));
	}

	@Test
	public void xSnDate_toggleAfter() {
		final Instant reqDate = getTestDate();
		Snws2AuthorizationBuilder builder = new Snws2AuthorizationBuilder(TEST_TOKEN_ID);
		builder.date(reqDate).useSnDate(true).host("localhost").path("/api/test");

		final String canonicalRequestData = builder.computeCanonicalRequestMessage();
		assertThat(canonicalRequestData, is(
				"GET\n/api/test\n\nhost:localhost\nx-sn-date:Tue, 25 Apr 2017 14:30:00 GMT\nhost;x-sn-date\n"
						+ EMPTY_STRING_SHA256_HEX));

		final String result = builder.build(TEST_TOKEN_SECRET);
		assertThat(result, is(
				"SNWS2 Credential=test-token-id,SignedHeaders=host;x-sn-date,Signature=c14fe9f67560fb9a37d2aa7c40b40c260a5936f999877e2469b8ddb1da7c0eb9"));
	}

	@Test
	public void queryParams() {
		final Instant reqDate = getTestDate();
		final Map<String, String> params = new LinkedHashMap<String, String>();
		params.put("foo", "bar");
		params.put("bim", "bam");

		Snws2AuthorizationBuilder builder = new Snws2AuthorizationBuilder(TEST_TOKEN_ID);
		builder.date(reqDate).host("localhost").path("/api/query").queryParams(params);

		final String canonicalRequestData = builder.computeCanonicalRequestMessage();
		assertThat(canonicalRequestData, is(
				"GET\n/api/query\nbim=bam&foo=bar\ndate:Tue, 25 Apr 2017 14:30:00 GMT\nhost:localhost\ndate;host\n"
						+ EMPTY_STRING_SHA256_HEX));

		final String result = builder.build(TEST_TOKEN_SECRET);
		assertThat(result, is(
				"SNWS2 Credential=test-token-id,SignedHeaders=date;host,Signature=c597ed8061d9d12e12ead3d8d6fc03b28a877e8639548f31556b4760be09a4b8"));
	}

	private static final String JSON_CONTENT_TYPE = "application/json;charset=UTF-8";

	@Test
	public void simplePost() throws Exception {
		final Instant reqDate = getTestDate();
		final String reqBodySha256Hex = "226e49e13d16e5e8aa0d62e58cd63361bf097d3e2b2444aa3044334628a2e8de";
		final byte[] reqBodySha256 = Hex.decodeHex(reqBodySha256Hex.toCharArray());
		final String reqBodySha256Base64 = Base64.encodeBase64String(reqBodySha256);

		Snws2AuthorizationBuilder builder = new Snws2AuthorizationBuilder(TEST_TOKEN_ID);

		// @formatter:off
		builder.date(reqDate).host("localhost").method("POST").path("/api/post")
				.header("Digest", "sha-256=" + reqBodySha256Base64)
				.header("Content-Type", JSON_CONTENT_TYPE)
				.contentSha256(reqBodySha256);
		// @formatter:on

		final String canonicalRequestData = builder.computeCanonicalRequestMessage();
		assertThat(canonicalRequestData, is("POST\n/api/post\n\ncontent-type:" + JSON_CONTENT_TYPE + "\n"
				+ "date:Tue, 25 Apr 2017 14:30:00 GMT\n" + "digest:sha-256=" + reqBodySha256Base64
				+ "\nhost:localhost\ncontent-type;date;digest;host\n" + reqBodySha256Hex));

		final String result = builder.build(TEST_TOKEN_SECRET);
		assertThat(result, is(
				"SNWS2 Credential=test-token-id,SignedHeaders=content-type;date;digest;host,Signature=ad609dd757c1f7f08a519919ab5e109ec61477cf612c6a0d29cac66d54c3987e"));
	}

	@Test
	public void postJsonWithSnDate() throws Exception {
		// @formatter:off
		final String json = "{\"hello\":\"world\"}";             // POST body content
		final Instant reqDate = getTestDate();                   // usually this should just be current date (e.g. `new Date()`)
		final byte[] reqBodySha256 = DigestUtils.sha256(json);   // calculate SHA-256 of POST body
		final String digestHeaderValue = "sha-256="              // the Digest header value we'll sign
				+ Base64.encodeBase64String(reqBodySha256);

		Snws2AuthorizationBuilder builder = new Snws2AuthorizationBuilder(TEST_TOKEN_ID);

		builder.method("POST")                                   // set API endpoint method
				.host("localhost")                               // not typically needed, defaults to data.solarnetwork.net:443
				.path("/api/post")                               // set API endpoint path
				.useSnDate(true)                                 // use X-SN-Date header instead of Date
				.date(reqDate)                                   // set to explicit date to share with X-SN-Date
				.contentType("application/json; charset=UTF-8")  // set body content type of POST data
				.digest(digestHeaderValue)                       // sign Digest header
				.contentSha256(reqBodySha256)                    // inform builder of POST body digest
				.saveSigningKey(TEST_TOKEN_SECRET);              // could call `build(TEST_TOKEN_SECRET)` in real app
		// @formatter:on

		// verify signed header values
		assertThat("Content-Type header", builder.headerValue("Content-Type"),
				is("application/json; charset=UTF-8"));
		assertThat("Digest header", builder.headerValue("Digest"), is(digestHeaderValue));
		assertThat("Host header", builder.headerValue("Host"), is("localhost"));
		assertThat("X-SN-Date header", builder.headerValue(AuthorizationUtils.SN_DATE_HEADER),
				is(AuthorizationUtils.AUTHORIZATION_DATE_HEADER_FORMATTER.format(reqDate)));

		// verify final Authorization header value
		final String result = builder.build();
		assertThat("Signature", result, is(
				"SNWS2 Credential=test-token-id,SignedHeaders=content-type;digest;host;x-sn-date,Signature=17b50462a9db77e569bd74676c550c447be07f605f191a39ca481efaa15e9879"));
	}

	@Test
	public void wikiExampleSigningKey() {
		// GIVEN
		final LocalDateTime date = LocalDateTime.of(2017, 1, 1, 0, 0, 0);
		final String secret = "ABC123";

		// @formatter:off
		Snws2AuthorizationBuilder builder = new Snws2AuthorizationBuilder(TEST_TOKEN_ID)
				.date(date.toInstant(ZoneOffset.UTC))
				.saveSigningKey(secret)
				;
		// @formatter:on

		// WHEN
		String signKeyHex = builder.signingKeyHex();

		// THEN
		String day = AuthorizationUtils.AUTHORIZATION_DATE_FORMATTER.format(date);
		assertThat("Sign date is formatted as YYYYMMDD", day, is("20170101"));
		String expected = Hex.encodeHexString(
				computeHmacSha256(computeHmacSha256("SNWS2" + secret, day), "snws2_request"));
		assertThat("Signing key is HMAC_SHA256(HMAC_SHA256('SNWS2ABC123', '20170101'), 'snws2_request')",
				signKeyHex, is(expected));
		assertThat("Expected is wiki example value", expected,
				is("1f96b28b651285e49d06989aebaee169fa67a5f6a07fb72a8325fce83b425ad6"));
	}

}

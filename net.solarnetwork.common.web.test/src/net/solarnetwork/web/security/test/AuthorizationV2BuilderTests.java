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

package net.solarnetwork.web.security.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import net.solarnetwork.web.security.AuthorizationV2Builder;
import net.solarnetwork.web.security.WebConstants;

/**
 * Test cases for the {@link AuthorizationV2Builder} class.
 * 
 * @author matt
 * @version 1.0
 */
public class AuthorizationV2BuilderTests {

	private static final String TEST_TOKEN_ID = "test-token-id";
	private static final String TEST_TOKEN_SECRET = "test-token-secret";

	private Date getTestDate() {
		Calendar c = new GregorianCalendar(2017, 3, 25, 14, 30, 0);
		c.setTimeZone(TimeZone.getTimeZone("GMT"));
		return c.getTime();
	}

	@Test
	public void simpleGet() {
		final Date reqDate = getTestDate();
		AuthorizationV2Builder builder = new AuthorizationV2Builder(TEST_TOKEN_ID);
		builder.date(reqDate).host("localhost").path("/api/test");

		final String canonicalRequestData = builder.buildCanonicalRequestData();
		Assert.assertEquals(
				"GET\n/api/test\n\ndate:Tue, 25 Apr 2017 14:30:00 GMT\nhost:localhost\ndate;host\n"
						+ WebConstants.EMPTY_STRING_SHA256_HEX,
				canonicalRequestData);

		final String result = builder.build(TEST_TOKEN_SECRET);
		Assert.assertEquals(
				"SNWS2 Credential=test-token-id,SignedHeaders=date;host,Signature=4739139d3d370f147b6585795c309b1c6d7d7f59943081f7dd943f689cfa59a3",
				result);
	}

	@Test
	public void xSnDate() {
		final Date reqDate = getTestDate();
		AuthorizationV2Builder builder = new AuthorizationV2Builder(TEST_TOKEN_ID);
		builder.date(reqDate).host("localhost").path("/api/test").header(WebConstants.HEADER_DATE,
				AuthorizationV2Builder.httpDate(reqDate));

		final String canonicalRequestData = builder.buildCanonicalRequestData();
		Assert.assertEquals(
				"GET\n/api/test\n\nhost:localhost\nx-sn-date:Tue, 25 Apr 2017 14:30:00 GMT\nhost;x-sn-date\n"
						+ WebConstants.EMPTY_STRING_SHA256_HEX,
				canonicalRequestData);

		final String result = builder.build(TEST_TOKEN_SECRET);
		Assert.assertEquals(
				"SNWS2 Credential=test-token-id,SignedHeaders=host;x-sn-date,Signature=c14fe9f67560fb9a37d2aa7c40b40c260a5936f999877e2469b8ddb1da7c0eb9",
				result);
	}

	@Test
	public void queryParams() {
		final Date reqDate = getTestDate();
		final Map<String, String> params = new LinkedHashMap<String, String>();
		params.put("foo", "bar");
		params.put("bim", "bam");

		AuthorizationV2Builder builder = new AuthorizationV2Builder(TEST_TOKEN_ID);
		builder.date(reqDate).host("localhost").path("/api/query").queryParams(params);

		final String canonicalRequestData = builder.buildCanonicalRequestData();
		Assert.assertEquals(
				"GET\n/api/query\nbim=bam&foo=bar\ndate:Tue, 25 Apr 2017 14:30:00 GMT\nhost:localhost\ndate;host\n"
						+ WebConstants.EMPTY_STRING_SHA256_HEX,
				canonicalRequestData);

		final String result = builder.build(TEST_TOKEN_SECRET);
		Assert.assertEquals(
				"SNWS2 Credential=test-token-id,SignedHeaders=date;host,Signature=c597ed8061d9d12e12ead3d8d6fc03b28a877e8639548f31556b4760be09a4b8",
				result);
	}

	@Test
	public void simplePost() throws DecoderException {
		final Date reqDate = getTestDate();
		final String reqBodySha256Hex = "226e49e13d16e5e8aa0d62e58cd63361bf097d3e2b2444aa3044334628a2e8de";
		final byte[] reqBodySha256 = Hex.decodeHex(reqBodySha256Hex.toCharArray());
		final String reqBodySha256Base64 = Base64.encodeBase64String(reqBodySha256);

		AuthorizationV2Builder builder = new AuthorizationV2Builder(TEST_TOKEN_ID);

		// @formatter:off
		builder.date(reqDate).host("localhost").method(HttpMethod.POST).path("/api/post")
				.header("Digest", "sha-256=" + reqBodySha256Base64)
				.header("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE)
				.contentSHA256(reqBodySha256);
		// @formatter:on

		final String canonicalRequestData = builder.buildCanonicalRequestData();
		Assert.assertEquals("POST\n/api/post\n\ncontent-type:application/json;charset=UTF-8\n"
				+ "date:Tue, 25 Apr 2017 14:30:00 GMT\n" + "digest:sha-256=" + reqBodySha256Base64
				+ "\nhost:localhost\ncontent-type;date;digest;host\n" + reqBodySha256Hex,
				canonicalRequestData);

		final String result = builder.build(TEST_TOKEN_SECRET);
		Assert.assertEquals(
				"SNWS2 Credential=test-token-id,SignedHeaders=content-type;date;digest;host,Signature=ad609dd757c1f7f08a519919ab5e109ec61477cf612c6a0d29cac66d54c3987e",
				result);
	}

	@Test
	public void postJsonWithSnDate() throws DecoderException {
		final String json = "{\"hello\":\"world\"}";
		final Date reqDate = getTestDate();
		final byte[] reqBodySha256 = DigestUtils.sha256(json);
		final String reqBodySha256Hex = Hex.encodeHexString(reqBodySha256, true);
		final String reqBodySha256Base64 = Base64.encodeBase64String(reqBodySha256);

		AuthorizationV2Builder builder = new AuthorizationV2Builder(TEST_TOKEN_ID);

		// @formatter:off
		builder.method(HttpMethod.POST)
				.host("localhost")
				.path("/api/post")
				.date(reqDate)
				.header(WebConstants.HEADER_DATE, AuthorizationV2Builder.httpDate(reqDate))
				.header("Digest", "sha-256=" + reqBodySha256Base64)
				.header("Content-Type", "application/json; charset=UTF-8")
				.contentSHA256(reqBodySha256)
				.saveSigningKey(TEST_TOKEN_SECRET);
		// @formatter:on

		final String canonicalRequestData = builder.buildCanonicalRequestData();
		// @formatter:off
		assertThat("Canonical req data", canonicalRequestData, equalTo(
				"POST\n"
				+ "/api/post\n"
				+ "\n"
				+ "content-type:application/json; charset=UTF-8\n"
				+ "digest:sha-256=" + reqBodySha256Base64 +"\n"
				+ "host:localhost\n"
				+ "x-sn-date:Tue, 25 Apr 2017 14:30:00 GMT\n" 
				+ "content-type;digest;host;x-sn-date\n" 
				+ reqBodySha256Hex));
		assertThat("Signing key", builder.signingKeyHex(), 
				equalTo("bf7885e8bd107a79f5c6e13001a4fa15fbd43221ad39ca47fde96191d302dbf4"));
		// @formatter:on

		final String result = builder.build();
		assertThat("Signature", result, equalTo(
				"SNWS2 Credential=test-token-id,SignedHeaders=content-type;digest;host;x-sn-date,Signature=17b50462a9db77e569bd74676c550c447be07f605f191a39ca481efaa15e9879"));
	}

}

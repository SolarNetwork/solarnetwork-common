/* ==================================================================
 * AuthorizationBuilderSNSTests.java - 13/08/2021 4:55:00 PM
 * 
 * Copyright 2021 SolarNetwork.net Dev Team
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

import static net.solarnetwork.security.AuthorizationUtils.computeHmacSha256;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import net.solarnetwork.security.AuthorizationBuilderSNS;

/**
 * Test cases for the {@link AuthorizationBuilderSNS} class.
 * 
 * @author matt
 * @version 1.0
 */
public class AuthorizationBuilderSNSTests {

	@Test
	public void computeSigningKey() {
		// GIVEN
		LocalDateTime date = LocalDateTime.of(2021, 8, 13, 13, 55, 12);

		// WHEN
		byte[] result = AuthorizationBuilderSNS.computeSigningKey(date.toInstant(ZoneOffset.UTC), "bar");

		// THEN
		assertThat("Result available", result, is(notNullValue()));

		byte[] expected = computeHmacSha256(computeHmacSha256("SNSbar", "20210813"), "sns_request");
		assertThat("Key computed", result, is(expected));
	}

	@Test
	public void computeCanonicalRequestMessage_minimal() {
		// GIVEN
		LocalDateTime date = LocalDateTime.of(2021, 8, 13, 13, 55, 12);

		// WHEN
		AuthorizationBuilderSNS b = new AuthorizationBuilderSNS("foo");
		b.verb("SEND");
		b.path("/");
		b.date(date.toInstant(ZoneOffset.UTC));
		String result = b.computeCanonicalRequestMessage();

		// THEN
		// @formatter:off
		assertThat("Message computed", result, is(
		      "SEND\n"
		    + "/\n"
		    + "date:Fri, 13 Aug 2021 13:55:12 GMT\n"
		    + "date\n"
		    + "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
		));
	    // @formatter:on
	}

	@Test
	public void computeCanonicalRequestMessage_multipleHeaders() {
		// GIVEN
		LocalDateTime date = LocalDateTime.of(2021, 8, 13, 13, 55, 12);

		// WHEN
		AuthorizationBuilderSNS b = new AuthorizationBuilderSNS("foo");
		b.verb("SPIFFY");
		b.path("/path/here");
		b.date(date.toInstant(ZoneOffset.UTC));
		b.header("foobar", "12345");
		b.header("spacey", "   a    b     c   ");
		b.header("deja-vu", "one", "two");
		b.header("SHOUTY", "be quiet");
		String result = b.computeCanonicalRequestMessage();

		// THEN
		// @formatter:off
		assertThat("Message computed with sorted normalized headers", result, is(
		      "SPIFFY\n"
		    + "/path/here\n"
		    + "date:Fri, 13 Aug 2021 13:55:12 GMT\n"
		    + "deja-vu:one\n"
		    + "deja-vu:two\n"
		    + "foobar:12345\n"
		    + "shouty:be quiet\n"
		    + "spacey:a b c\n"
		    + "date;deja-vu;foobar;shouty;spacey\n"
		    + "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
		));
	    // @formatter:on
	}

	@Test
	public void computeCanonicalRequestMessage_bodyContent() {
		// GIVEN
		LocalDateTime date = LocalDateTime.of(2021, 8, 13, 13, 55, 12);
		byte[] contentDigest = DigestUtils.sha256("Hello, world.");

		// WHEN
		AuthorizationBuilderSNS b = new AuthorizationBuilderSNS("foo");
		b.verb("GET");
		b.path("/");
		b.date(date.toInstant(ZoneOffset.UTC));
		b.contentSha256(contentDigest);
		String result = b.computeCanonicalRequestMessage();

		// THEN
		// @formatter:off
		assertThat("Message computed with sorted normalized headers", result, is(
		      "GET\n"
		    + "/\n"
		    + "date:Fri, 13 Aug 2021 13:55:12 GMT\n"
		    + "date\n"
		    + "f8c3bf62a9aa3e6fc1619c250e48abe7519373d3edf41be62eb5dc45199af2ef"
		));
	    // @formatter:on
	}

	@Test
	public void computeSignatureData() {
		// GIVEN
		LocalDateTime date = LocalDateTime.of(2021, 8, 13, 13, 55, 12);

		// WHEN
		String result = AuthorizationBuilderSNS.computeSignatureData(date.toInstant(ZoneOffset.UTC),
				"foobar");

		// THEN
		// @formatter:off
		assertThat("Computed signature data", result, is(
				  "SNS-HMAC-SHA256\n"
				+ "20210813135512Z\n"
				+ Hex.encodeHexString(DigestUtils.sha256("foobar"))
		));
		// @formatter:on
	}

	@Test
	public void build_minimal() {
		// GIVEN
		LocalDateTime date = LocalDateTime.of(2021, 8, 13, 13, 55, 12);

		// WHEN
		AuthorizationBuilderSNS b = new AuthorizationBuilderSNS("foo");
		b.verb("SEND");
		b.path("/");
		b.date(date.toInstant(ZoneOffset.UTC));
		String result = b.build("bar");

		// THEN
		assertThat("Authorization computed", result,
				is(String.format("SNS Credential=foo,SignedHeaders=date,Signature=%s",
						"4dea8b3e8e0b43cac894e995716417793ed42e9728eafa6524bd68704bde12f7")));
	}

}

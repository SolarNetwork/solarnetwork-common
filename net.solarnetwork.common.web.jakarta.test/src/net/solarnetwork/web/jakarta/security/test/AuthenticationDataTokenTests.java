/* ==================================================================
 * AuthenticationDataTokenTests.java - 27/04/2017 7:21:16 AM
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

package net.solarnetwork.web.jakarta.security.test;

import static net.solarnetwork.security.AuthorizationUtils.AUTHORIZATION_DATE_HEADER_FORMATTER;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.time.Instant;
import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import jakarta.servlet.http.Cookie;
import net.solarnetwork.security.Snws2AuthorizationBuilder;
import net.solarnetwork.web.jakarta.security.AuthenticationData;
import net.solarnetwork.web.jakarta.security.AuthenticationDataFactory;
import net.solarnetwork.web.jakarta.security.AuthenticationDataToken;
import net.solarnetwork.web.jakarta.security.SecurityException;
import net.solarnetwork.web.jakarta.security.SecurityHttpServletRequestWrapper;

/**
 * Test cases for the {@link AuthenticationDataToken} class.
 * 
 * @author matt
 * @version 2.0
 */
public class AuthenticationDataTokenTests {

	private static final String TEST_TOKEN_IDENTITY = "admin";
	private static final String TEST_SECRET = "secret";
	private static final Instant TEST_DATE = Instant.ofEpochMilli(1493164800000L);
	private static final long TEST_JTW_EXPIRATION = 1493769600L;

	private static final String TEST_JWT_SIG = "6tEdtCDMz6E0XEokeNSMjNYMvMiiuwLzD9FRugg2GFo";
	private static final String TEST_JWT = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImV4cCI6MTQ5Mzc2OTYwMCwiaWF0IjoxNDkzMTY0ODAwfQ."
			+ TEST_JWT_SIG;

	@Test
	public void constructFromAuthenticationData() throws IOException {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/mock/path/here");
		request.addHeader("Date", AUTHORIZATION_DATE_HEADER_FORMATTER.format(TEST_DATE));
		request.addHeader("Authorization", new Snws2AuthorizationBuilder(TEST_TOKEN_IDENTITY)
				.date(TEST_DATE).path(request.getRequestURI()).build(TEST_SECRET));
		AuthenticationData authData = AuthenticationDataFactory.authenticationDataForAuthorizationHeader(
				new SecurityHttpServletRequestWrapper(request, 1024));
		AuthenticationDataToken obj = new AuthenticationDataToken(authData, TEST_SECRET);
		assertEquals("Identity", TEST_TOKEN_IDENTITY, obj.getIdentity());
		assertEquals("Issued", TEST_DATE.toEpochMilli() / 1000, obj.getIssued());
		assertEquals("Expires", TEST_JTW_EXPIRATION, obj.getExpires());
		assertArrayEquals("Signature", Base64.decodeBase64(TEST_JWT_SIG), obj.getSignature());
		obj.verify(TEST_SECRET, 0);
	}

	@Test
	public void constructFromCookie() {
		AuthenticationDataToken obj = new AuthenticationDataToken(new Cookie("foo", TEST_JWT));
		assertEquals("Identity", TEST_TOKEN_IDENTITY, obj.getIdentity());
		assertEquals("Issued", TEST_DATE.toEpochMilli() / 1000, obj.getIssued());
		assertEquals("Expires", TEST_JTW_EXPIRATION, obj.getExpires());
		assertArrayEquals("Signature", Base64.decodeBase64(TEST_JWT_SIG), obj.getSignature());
		obj.verify(TEST_SECRET, TEST_DATE.toEpochMilli());
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructFromCookieMalformedValue() {
		new AuthenticationDataToken(new Cookie("foo", "blah.blah.blah"));
	}

	@Test(expected = SecurityException.class)
	public void invalidPassword() {
		AuthenticationDataToken obj = new AuthenticationDataToken(new Cookie("foo", TEST_JWT));
		obj.verify("foo", 0);
	}

	@Test(expected = SecurityException.class)
	public void expired() {
		AuthenticationDataToken obj = new AuthenticationDataToken(new Cookie("foo", TEST_JWT));
		obj.verify(TEST_SECRET, TEST_JTW_EXPIRATION * 1000 + 1);
	}

}

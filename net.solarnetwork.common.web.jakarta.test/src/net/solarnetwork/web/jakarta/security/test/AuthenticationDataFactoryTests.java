/* ==================================================================
 * AuthenticationDataFactoryTests.java - 15 Sept 2026 7:33:02 am
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import net.solarnetwork.security.Snws2AuthorizationBuilder;
import net.solarnetwork.test.CommonTestUtils;
import net.solarnetwork.web.jakarta.security.AuthenticationData;
import net.solarnetwork.web.jakarta.security.AuthenticationDataFactory;
import net.solarnetwork.web.jakarta.security.AuthenticationDataV2;
import net.solarnetwork.web.jakarta.security.SecurityHttpServletRequestWrapper;

/**
 * Test cases for the {@link AuthenticationDataFactory} class.
 *
 * @author matt
 * @version 1.0
 */
public class AuthenticationDataFactoryTests {

	private static final String TEST_HOST = "host.example.com";
	private static final String TEST_PATH = "/foo/bar";

	@Test
	public void resolveV2() throws IOException {
		// GIVEN
		final String tokenId = CommonTestUtils.randomString();
		final String tokenSec = CommonTestUtils.randomString();
		final Instant now = Instant.now();
		final Snws2AuthorizationBuilder builder = new Snws2AuthorizationBuilder(tokenId);
		final String authHeader = builder.date(now).host(TEST_HOST).path(TEST_PATH).build(tokenSec);

		// WHEN
		MockHttpServletRequest request = new MockHttpServletRequest("GET", TEST_PATH);
		request.addHeader(HttpHeaders.DATE, Date.from(now));
		request.addHeader(HttpHeaders.AUTHORIZATION, authHeader);

		final AuthenticationData result = AuthenticationDataFactory
				.authenticationDataForAuthorizationHeader(
						new SecurityHttpServletRequestWrapper(request, 1024));

		// THEN
		assertThat("SNWS2 data resolved", result, is(instanceOf(AuthenticationDataV2.class)));
	}

	@Test
	public void failV2_trailingCharacters() throws IOException {
		// GIVEN
		final String tokenId = CommonTestUtils.randomString();
		final String tokenSec = CommonTestUtils.randomString();
		final Instant now = Instant.now();
		final Snws2AuthorizationBuilder builder = new Snws2AuthorizationBuilder(tokenId);

		// add a trailing character after SNWS2
		final String authHeader = builder.date(now).host(TEST_HOST).path(TEST_PATH).build(tokenSec)
				.replace("SNWS2", "SNWS2x");

		// WHEN
		MockHttpServletRequest request = new MockHttpServletRequest("GET", TEST_PATH);
		request.addHeader(HttpHeaders.DATE, Date.from(now));
		request.addHeader(HttpHeaders.AUTHORIZATION, authHeader);

		final AuthenticationData result = AuthenticationDataFactory
				.authenticationDataForAuthorizationHeader(
						new SecurityHttpServletRequestWrapper(request, 1024));

		// THEN
		assertThat("SNWS2 data not resolved because prefix does not match", result, is(nullValue()));
	}

}

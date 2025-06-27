/* ==================================================================
 * AuthenticationUtilsTests.java - 3/09/2024 5:41:22 pm
 *
 * Copyright 2024 SolarNetwork.net Dev Team
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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import org.junit.Test;
import net.solarnetwork.web.jakarta.security.AuthenticationUtils;

/**
 * Test cases for the {@link AuthenticationUtils} class.
 *
 * @author matt
 * @version 1.0
 */
public class AuthenticationUtilsTests {

	@Test
	public void httpDate_september() {
		// GIVEN
		ZonedDateTime zdt = LocalDateTime.of(2024, 9, 3, 17, 45).atZone(ZoneId.of("Pacific/Auckland"));

		// WHEN
		String result = AuthenticationUtils.httpDate(Date.from(zdt.toInstant()));

		// THEN
		assertThat("HTTP date generated with 3-character month name", result,
				is(equalTo("Tue, 03 Sep 2024 05:45:00 GMT")));
	}

	@Test
	public void iso8601Date_september() {
		// GIVEN
		ZonedDateTime zdt = LocalDateTime.of(2024, 9, 3, 17, 45).atZone(ZoneId.of("Pacific/Auckland"));

		// WHEN
		String result = AuthenticationUtils.iso8601Date(Date.from(zdt.toInstant()));

		// THEN
		assertThat("ISO 8601 condensed date generated", result, is(equalTo("20240903T054500Z")));
	}

}

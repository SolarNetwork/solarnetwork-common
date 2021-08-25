/* ==================================================================
 * AuthorizationUtilsTests.java - 13/08/2021 4:52:57 PM
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import net.solarnetwork.security.AuthorizationUtils;

/**
 * Test cases for the {@link AuthorizationUtils} class.
 * 
 * @author matt
 * @version 1.0
 */
public class AuthorizationUtilsTests {

	@Test
	public void formatTimestamp_instant() {
		// GIVEN
		LocalDateTime date = LocalDateTime.of(2021, 8, 13, 13, 55, 12,
				(int) TimeUnit.MILLISECONDS.toNanos(123));
		Instant ts = date.toInstant(ZoneOffset.UTC);

		// WHEN
		String result = AuthorizationUtils.AUTHORIZATION_DATE_HEADER_FORMATTER.format(ts);

		// THEN
		assertThat("Instant is formatted", result, is("Fri, 13 Aug 2021 13:55:12 GMT"));
	}

}

/* ==================================================================
 * AuthenticationSchemeTests.java - 15 Sept 2026 7:30:03 am
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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import java.util.regex.Pattern;
import org.junit.Test;
import net.solarnetwork.web.jakarta.security.AuthenticationScheme;

/**
 * Test cases for the {@link AuthenticationScheme} class.
 *
 * @author matt
 * @version 1.0
 */
public class AuthenticationSchemeTests {

	@Test
	public void schemePrefix() {
		for ( AuthenticationScheme s : AuthenticationScheme.values() ) {
			var expected = Pattern.compile("^" + s.getSchemeName() + "\\s+");
			assertThat("Scheme prefix is scheme name with whitespace, anchored to start of string",
					s.getSchemePrefix().pattern(), is(equalTo(expected.pattern())));
		}
	}

}

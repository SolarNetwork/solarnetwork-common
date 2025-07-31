/* ==================================================================
 * SpringSecurityPasswordEncoderTests.java - Mar 19, 2013 10:28:23 AM
 *
 * Copyright 2013 SolarNetwork.net Dev Team
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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import java.security.SecureRandom;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import net.solarnetwork.security.SpringSecurityPasswordEncoder;

/**
 * Test case for the {@link SpringSecurityPasswordEncoder} class.
 *
 * @author matt
 * @version 1.0
 */
public class SpringSecurityPasswordEncoderTests {

	private static final String TEST_PASSWORD = "test.password";

	private SpringSecurityPasswordEncoder testPasswordEncoder;

	@Before
	public void setup() {
		testPasswordEncoder = new SpringSecurityPasswordEncoder(
				Map.of("$2a$", new BCryptPasswordEncoder(12, new SecureRandom())));
	}

	@Test
	public void encodePassword() {
		final String encoded = testPasswordEncoder.encode(TEST_PASSWORD);
		assertThat(encoded, startsWith("$2a$12$"));
		assertThat(encoded.length(), is(equalTo(60)));
	}

	@Test
	public void verifyPassword() {
		final String encoded = testPasswordEncoder.encode(TEST_PASSWORD);
		assertThat(testPasswordEncoder.isPasswordEncrypted(encoded), is(true));
		assertThat(testPasswordEncoder.matches(TEST_PASSWORD, encoded), is(true));
	}

}

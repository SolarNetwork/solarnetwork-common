/* ==================================================================
 * BCPBKDF2PasswordEncoderTests.java - 26/05/2017 10:50:24 AM
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

package net.solarnetwork.pki.bc.test;

import java.util.regex.Pattern;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import net.solarnetwork.pki.bc.BCPBKDF2PasswordEncoder;

/**
 * Test cases for the {@link BCPBKDF2PasswordEncoder} class.
 * 
 * @author matt
 * @version 1.0
 */
public class BCPBKDF2PasswordEncoderTests {

	private static final String PASSWORD = "6022da26f2d4b2a0$131072$"
			+ "4b8518084a659f7562273cfca4efb1850ab76dcdad13c2993bc5c64f0b276984";

	private static final Pattern ENCODING_PATTERN = Pattern
			.compile("\\A([0-9a-fA-F]+)\\$(\\d+)\\$([0-9a-fA-F]+)");

	private BCPBKDF2PasswordEncoder encoder;

	@Before
	public void setup() {
		encoder = new BCPBKDF2PasswordEncoder();
	}

	@Test
	public void testEncode() {
		String encoded = encoder.encode("password");
		Assert.assertEquals("Encoded length", (16 + 1 + 6 + 1 + 64), encoded.length());
		Assert.assertTrue("Encoded format", ENCODING_PATTERN.matcher(encoded).matches());
	}

	@Test
	public void testIsEncrypted() {
		Assert.assertTrue("Password encrypted", encoder.isPasswordEncrypted(PASSWORD));
		Assert.assertTrue("Fake format but valid", encoder.isPasswordEncrypted("aaa$123$abababababab"));
	}

	@Test
	public void testIsNotEncrypted() {
		Assert.assertFalse("Null password", encoder.isPasswordEncrypted(null));
		Assert.assertFalse("Empty password", encoder.isPasswordEncrypted(""));
		Assert.assertFalse("Bad format", encoder.isPasswordEncrypted("foobar"));
	}

	@Test
	public void testMatch() {
		boolean match = encoder.matches("password", PASSWORD);
		Assert.assertTrue("Password matches", match);
	}

	@Test
	public void testNotMatch() {
		boolean match = encoder.matches("foobar", PASSWORD);
		Assert.assertFalse("Password does not match", match);
	}

}

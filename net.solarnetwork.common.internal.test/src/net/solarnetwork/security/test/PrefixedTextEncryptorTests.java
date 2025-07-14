/* ==================================================================
 * PrefixedTextEncryptorTests.java - 8/10/2024 1:15:57 pm
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

package net.solarnetwork.security.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import net.solarnetwork.security.PrefixedTextEncryptor;

/**
 * Test cases for the {@link PrefixedTextEncryptor} class.
 *
 * @author matt
 * @version 1.0
 */
public class PrefixedTextEncryptorTests {

	private String password;
	private String salt;
	private PrefixedTextEncryptor encryptor;

	private static String randomString() {
		return UUID.randomUUID().toString().replace("-", "");
	}

	@Before
	public void setup() {
		password = randomString();
		salt = randomString();

		encryptor = PrefixedTextEncryptor.aesTextEncryptor(password, salt);
	}

	@Test
	public void roundTrip() {
		// GIVEN
		final String plainText = randomString();

		// WHEN
		String encText = encryptor.encrypt(plainText);
		String decText = encryptor.decrypt(encText);

		// THEN
		assertThat("Encrypted text starts with prefix", encText, startsWith(encryptor.getPrefix()));
		assertThat("Decrypted text matches input plain text", decText, is(equalTo(plainText)));
	}

	@Test
	public void reencrypt() {
		// GIVEN
		final String plainText = randomString();

		// WHEN
		String encText = encryptor.encrypt(plainText);

		// try to re-encrypt encrypted text
		String encText2 = encryptor.encrypt(encText);

		// THEN
		assertThat("Re-encrypted text is unchanged from first encryption", encText2,
				is(equalTo(encText)));
	}

	@Test
	public void decrypt_newInstances() {
		// GIVEN
		final String plainText = randomString();

		final var encryptor2 = PrefixedTextEncryptor.aesTextEncryptor(password, salt);

		// WHEN
		String encText = encryptor.encrypt(plainText);

		String encText2 = encryptor2.encrypt(plainText);
		String decText2 = encryptor2.decrypt(encText); // decrypt text encrypted by different instance
		String decText2a = encryptor2.decrypt(encText2); // decrypt text encrypted by same instance

		// THEN
		assertThat("Encryped text same differs between encryptor calls due to random IV", encText2,
				allOf(is(not(equalTo(encText))), startsWith(encryptor.getPrefix())));
		assertThat("Decrypted text encrypted by different instance matches input plain text", decText2,
				is(equalTo(plainText)));
		assertThat("Decrypted text matches input plain text", decText2a, is(equalTo(plainText)));
	}

	@Test
	public void decrypt_notEncrypted() {
		// GIVEN
		final String plainText = randomString();

		// WHEN
		// try to decrypt plain text
		String decText = encryptor.decrypt(plainText);

		// THEN
		assertThat("Decrypted plain text is input plain text", decText, is(equalTo(plainText)));
	}

}

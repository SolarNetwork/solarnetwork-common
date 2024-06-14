/* ==================================================================
 * BCCryptoService.java - 26/05/2017 7:12:03 AM
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

package net.solarnetwork.pki.bc;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import net.solarnetwork.service.PasswordEncoder;

/**
 * A Bouncy Castle implementation of a password encoder using the {@code PBKDF2}
 * key derivation algorithm and the SHA-256 digest.
 *
 * <p>
 * Raw passwords are encoded into strings using the following format:
 * <code>{salt}${iterations}${digest}</code> where <em>salt</em> is the
 * hex-encoded salt used, <em>iterations</em> is the iteration count, and
 * <em>digest</em> is the hex-encoded computed digest.
 * </p>
 *
 * <p>
 * This implementation uses the algorithm as defined by the PKCS 5 V2.0 Scheme 2
 * standard. See <a href="https://tools.ietf.org/html/rfc2898">RFC 2989</a> for
 * more information.
 * </p>
 *
 * @author matt
 * @version 2.0
 */
public class BCPBKDF2PasswordEncoder implements PasswordEncoder {

	/** The default value for the {@code saltLength} property. */
	public static final int DEFAULT_SALT_LENGTH = 8;

	/** The default value for the {@code keyLength} property. */
	public static final int DEFAULT_KEY_LENGTH = 32;

	/** The default value for the {@code iterations} value. */
	public static final int DEFAULT_ITERATIONS = 131072;

	private static final Pattern ENCODING_PATTERN = Pattern
			.compile("(\\A[0-9a-fA-F]+)\\$(\\d+)\\$([0-9a-fA-F]+)");

	private int saltLength = DEFAULT_SALT_LENGTH;
	private int keyLength = DEFAULT_KEY_LENGTH;
	private int iterations = DEFAULT_ITERATIONS;

	private final SecureRandom random = new SecureRandom();

	/**
	 * Constructor.
	 */
	public BCPBKDF2PasswordEncoder() {
		super();
	}

	@Override
	public String encode(CharSequence rawPassword) {
		final int itr = iterations;
		final int keySize = keyLength * 8;
		final byte[] salt = new byte[saltLength];
		random.nextBytes(salt);
		try {
			byte[] dk = derivePBKDF2SHA256Key(rawPassword.toString().getBytes("UTF-8"), salt, keySize,
					itr);
			StringBuilder buf = new StringBuilder();
			buf.append(Hex.encodeHexString(salt));
			buf.append('$');
			buf.append(itr);
			buf.append('$');
			buf.append(Hex.encodeHexString(dk));
			return buf.toString();
		} catch ( UnsupportedEncodingException e ) {
			throw new RuntimeException("Error encoding raw password as UTF-8", e);
		}
	}

	@Override
	public boolean isPasswordEncrypted(CharSequence password) {
		return (password != null && password.length() > 0
				&& ENCODING_PATTERN.matcher(password).matches());
	}

	@Override
	public boolean matches(CharSequence rawPassword, String encodedPassword) {
		if ( encodedPassword == null || encodedPassword.length() == 0 ) {
			return false;
		}

		Matcher matcher = ENCODING_PATTERN.matcher(encodedPassword);
		if ( !matcher.matches() ) {
			return false;
		}

		try {
			byte[] salt = Hex.decodeHex(matcher.group(1).toCharArray());
			int itr = Integer.parseInt(matcher.group(2));
			byte[] digest = Hex.decodeHex(matcher.group(3).toCharArray());
			int keySize = (digest.length * 8);

			byte[] computed = derivePBKDF2SHA256Key(rawPassword.toString().getBytes("UTF-8"), salt,
					keySize, itr);

			return Arrays.equals(computed, digest);
		} catch ( UnsupportedEncodingException e ) {
			throw new RuntimeException("Error decoding password as UTF-8", e);
		} catch ( DecoderException e ) {
			throw new RuntimeException("Error decoding password as hex", e);
		}
	}

	/**
	 * Derive a secure key from a raw password using the PBKDF2 derivation
	 * algorithm and {@literal SHA-256} digest algorithm.
	 *
	 * @param rawPassword
	 *        the raw password to derive the key from
	 * @param salt
	 *        the salt to use
	 * @param keySize
	 *        the desired key size, in <b>bits</b>
	 * @param iterations
	 *        the number of iterations to use
	 * @return the derived key
	 */
	public static final byte[] derivePBKDF2SHA256Key(byte[] rawPassword, byte[] salt, int keySize,
			int iterations) {
		PKCS5S2ParametersGenerator gen = new PKCS5S2ParametersGenerator(new SHA256Digest());
		gen.init(rawPassword, salt, iterations);
		byte[] dk = ((org.bouncycastle.crypto.params.KeyParameter) gen
				.generateDerivedParameters(keySize)).getKey();
		return dk;
	}

	/**
	 * Main entry point.
	 *
	 * <p>
	 * This method expects a plain-text password argument, and will print the
	 * PBKDF2 hashed version of that to the output stream.
	 * </p>
	 *
	 * @param arguments
	 *        the arguments
	 */
	public static final void main(String[] arguments) {
		if ( arguments.length < 1 ) {
			System.err.println("Usage: " + BCPBKDF2PasswordEncoder.class.getName() + " <password>");
			System.exit(1);
		}
		BCPBKDF2PasswordEncoder encoder = new BCPBKDF2PasswordEncoder();
		System.out.println(arguments[0] + " = " + encoder.encode(arguments[0]));
	}

	/**
	 * Set the number of bytes of random salt to use.
	 *
	 * <p>
	 * Defaults to {@link #DEFAULT_SALT_LENGTH}.
	 * </p>
	 *
	 * @param saltLength
	 *        the saltLength to set, in <b>bytes</b>
	 */
	public void setSaltLength(int saltLength) {
		this.saltLength = saltLength;
	}

	/**
	 * Set the number of bytes to use for derived keys.
	 *
	 * <p>
	 * Defaults to {@link #DEFAULT_KEY_LENGTH}.
	 * </p>
	 *
	 * @param keyLength
	 *        the keyLength to set, in <b>bytes</b>
	 */
	public void setKeyLength(int keyLength) {
		this.keyLength = keyLength;
	}

	/**
	 * Set the number of iterations to use when deriving keys.
	 *
	 * <p>
	 * Defaults to {@link BCPBKDF2PasswordEncoder#DEFAULT_ITERATIONS}.
	 * </p>
	 *
	 * @param iterations
	 *        the iterations to set
	 */
	public void setIterations(int iterations) {
		this.iterations = iterations;
	}

}

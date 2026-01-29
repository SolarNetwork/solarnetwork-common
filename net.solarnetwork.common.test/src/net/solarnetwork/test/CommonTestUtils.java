/* ==================================================================
 * CommonTestUtils.java - 30/01/2026 6:22:32 am
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

package net.solarnetwork.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Properties;
import java.util.UUID;
import org.springframework.util.FileCopyUtils;

/**
 * Common test utilities.
 *
 * @author matt
 * @version 1.0
 * @since 2.2
 */
public final class CommonTestUtils {

	/** A random number generator. */
	public static final SecureRandom RNG = new SecureRandom();

	private CommonTestUtils() {
		// not available
	}

	/**
	 * Load test environment properties.
	 *
	 * @return the properties, never {@code null}
	 * @see #loadEnvironmentProperties(ClassLoader)
	 */
	public static Properties loadEnvironmentProperties() {
		return loadEnvironmentProperties(CommonTestUtils.class.getClassLoader());
	}

	/**
	 * Load test environment properties.
	 *
	 * <p>
	 * This will load the properties resource {@code env.properties}. IO
	 * exceptions loading the resource are ignored.
	 * </p>
	 *
	 * @param classLoader
	 *        the class loader to use
	 * @return the properties, never {@code null}
	 */
	public static Properties loadEnvironmentProperties(ClassLoader classLoader) {
		Properties props = new Properties();
		try (InputStream in = classLoader.getResourceAsStream("env.properties")) {
			props.load(in);
		} catch ( IOException e ) {
			// we'll ignore this
		}
		return props;
	}

	/**
	 * Load a UTF-8 string classpath resource.
	 *
	 * @param resource
	 *        the resource to load
	 * @param clazz
	 *        the class from which to load the resource
	 * @return the resource
	 * @throws UncheckedIOException
	 *         if any IO error occurs
	 */
	public static String utf8StringResource(String resource, Class<?> clazz) {
		try {
			return FileCopyUtils.copyToString(
					new InputStreamReader(clazz.getResourceAsStream(resource), StandardCharsets.UTF_8));
		} catch ( IOException e ) {
			throw new UncheckedIOException(e);
		}
	}

	/**
	 * Get a random decimal number in the range {@code [-1000,1000)}.
	 *
	 * @return the random decimal number
	 */
	public static BigDecimal randomDecimal() {
		return new BigDecimal(RNG.nextDouble(-1000.0, 1000.0)).setScale(4, RoundingMode.HALF_UP);
	}

	/**
	 * Get a random positive double number.
	 *
	 * @return the random double number
	 */
	public static Double randomDouble() {
		return RNG.nextDouble(0, Double.MAX_VALUE);
	}

	/**
	 * Get a random positive float number.
	 *
	 * @return the random float number
	 */
	public static Float randomFloat() {
		return RNG.nextFloat(0, Float.MAX_VALUE);
	}

	/**
	 * Get a random string value with a length of {@code 14}.
	 *
	 * @return the string
	 */
	public static String randomString() {
		return UUID.randomUUID().toString().replace("-", "").substring(0, 14);
	}

	/**
	 * Get a random string value of an arbitrary length.
	 *
	 * @param len
	 *        the desired string length
	 * @return the string
	 */
	public static String randomString(int len) {
		StringBuilder buf = new StringBuilder();
		while ( buf.length() < len ) {
			buf.append(UUID.randomUUID().toString().replace("-", ""));
		}
		buf.setLength(len);
		return buf.toString();
	}

	/**
	 * Get a random positive short value.
	 *
	 * @return the short
	 */
	public static Short randomShort() {
		return (short) RNG.nextInt(1, Short.MAX_VALUE);
	}

	/**
	 * Get a random positive integer value.
	 *
	 * @return the integer
	 */
	public static Integer randomInt() {
		return RNG.nextInt(1, Integer.MAX_VALUE);
	}

	/**
	 * Get a random positive long value.
	 *
	 * @return the long
	 */
	public static Long randomLong() {
		return RNG.nextLong(1, Long.MAX_VALUE);
	}

	/**
	 * Get a random boolean value.
	 *
	 * @return the boolean
	 */
	public static boolean randomBoolean() {
		return RNG.nextBoolean();
	}

	/**
	 * Get 16 random bytes.
	 *
	 * @return the random bytes
	 */
	public static byte[] randomBytes() {
		return randomBytes(16);
	}

	/**
	 * Get random bytes.
	 *
	 * @param len
	 *        the desired number of bytes
	 * @return random bytes, of length {@code len}
	 */
	public static byte[] randomBytes(int len) {
		byte[] bytes = new byte[len];
		RNG.nextBytes(bytes);
		return bytes;
	}

}

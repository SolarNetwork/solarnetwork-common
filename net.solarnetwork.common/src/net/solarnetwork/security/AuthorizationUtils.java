/* ==================================================================
 * AuthorizationUtils.java - 13/08/2021 4:49:04 PM
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

package net.solarnetwork.security;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Locale;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * Utilities for authorization.
 * 
 * @author matt
 * @version 1.0
 * @since 1.78
 */
public class AuthorizationUtils {

	private AuthorizationUtils() {
		// can't construct me
	}

	/** The hex-encoded SHA256 value of an empty string. */
	public static final String EMPTY_STRING_SHA256_HEX;

	static {
		EMPTY_STRING_SHA256_HEX = DigestUtils.sha256Hex("".getBytes());
	}

	/**
	 * Date formatter that formats or parses a date without an offset, such as
	 * '20111203'.
	 */
	public static final DateTimeFormatter AUTHORIZATION_DATE_FORMATTER;

	static {
		// @formatter:off
		    AUTHORIZATION_DATE_FORMATTER = new DateTimeFormatterBuilder()
		              .parseCaseInsensitive()
		              .appendValue(ChronoField.YEAR, 4)
		              .appendValue(ChronoField.MONTH_OF_YEAR, 2)
		              .appendValue(ChronoField.DAY_OF_MONTH, 2)
		              .toFormatter();
		    // @formatter:on
	}

	/**
	 * Date formatter that formats or parses timestamp values in the HTTP
	 * {@literal Date} header format, similar to RFC 1123 but with 2-digit day
	 * values used always.
	 * 
	 * <p>
	 * An example of this date format is
	 * {@literal Fri, 13 Aug 2021 13:55:12 GMT}.
	 * </p>
	 */
	public static final DateTimeFormatter AUTHORIZATION_DATE_HEADER_FORMATTER;

	static {
		AUTHORIZATION_DATE_HEADER_FORMATTER = DateTimeFormatter
				.ofPattern("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH).withZone(ZoneId.of("GMT"));
	}

	/**
	 * Date formatter that formats or parses timestamp values in the ISO 8601
	 * condensed timestamp form with second resolution, in the {@code GMT} time
	 * zone.
	 * 
	 * <p>
	 * An example of this date format is {@literal 20210813T135512Z}.
	 * </p>
	 */
	public static final DateTimeFormatter AUTHORIZATION_TIMESTAMP_FORMATTER;

	static {
		AUTHORIZATION_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
				.withZone(ZoneId.of("GMT"));
	}

	/**
	 * Compute a HMAC-SHA256 digest from UTF-8 string values.
	 * 
	 * @param key
	 *        the key to sign the digest with, which is assumed to be a UTF-8
	 *        encoded string.
	 * @param msg
	 *        the message content to sign, which is assumed to be a UTF-8
	 *        encoded string.
	 * @return the computed digest
	 * @throws SecurityException
	 *         if the strings cannot be interpreted as UTF-8 or
	 *         {@code HmacSHA256} is not supported by the runtime security
	 *         provider
	 */
	public static byte[] computeHmacSha256(final String key, final String msg) {
		return computeMacDigest(key, msg, "HmacSHA256");
	}

	/**
	 * Compute a HMAC-SHA256 digest from a byte array password.
	 * 
	 * @param key
	 *        The key to sign the digest with.
	 * @param msg
	 *        the message content to sign, which is assumed to be a UTF-8
	 *        encoded string
	 * @return the computed digest
	 * @throws SecurityException
	 *         if the strings cannot be interpreted as UTF-8 or
	 *         {@literal HmacSHA256} is not supported by the runtime security
	 *         provider
	 */
	public static byte[] computeHmacSha256(final byte[] key, final String msg) {
		try {
			return computeMacDigest(key, msg.getBytes("UTF-8"), "HmacSHA256");
		} catch ( UnsupportedEncodingException e ) {
			throw new SecurityException("Error loading HmacSHA1 crypto function", e);
		}
	}

	/**
	 * Compute a MAC digest from UTF-8 string values.
	 * 
	 * @param secret
	 *        the secret to sign the digest with, which is assumed to be a UTF-8
	 *        encoded string.
	 * @param msg
	 *        the message content to sign, which is assumed to be a UTF-8
	 *        encoded string
	 * @param alg
	 *        the MAC algorithm to use, which must be supported by the runtime
	 *        security provider
	 * @return the computed digest
	 * @throws SecurityException
	 *         if the strings cannot be interpreted as UTF-8 or {@code alg} is
	 *         not supported by the runtime security provider
	 */
	public static byte[] computeMacDigest(final String secret, final String msg, String alg) {
		try {
			return computeMacDigest(secret.getBytes("UTF-8"), msg.getBytes("UTF-8"), alg);
		} catch ( UnsupportedEncodingException e ) {
			throw new SecurityException("Error encoding secret or message for crypto function", e);
		}
	}

	/**
	 * Compute a MAC digest.
	 * 
	 * @param key
	 *        the key to sign the digest with
	 * @param msg
	 *        the message content to sign
	 * @param alg
	 *        the MAC algorithm to use, which must be supported by the runtime
	 *        security provider
	 * @return the computed digest
	 * @throws SecurityException
	 *         if the strings cannot be interpreted as UTF-8 or {@code alg} is
	 *         not supported by the runtime security provider
	 */
	public static byte[] computeMacDigest(final byte[] key, final byte[] msg, String alg) {
		Mac hmacSha1;
		try {
			hmacSha1 = Mac.getInstance(alg);
			hmacSha1.init(new SecretKeySpec(key, alg));
			byte[] result = hmacSha1.doFinal(msg);
			return result;
		} catch ( NoSuchAlgorithmException e ) {
			throw new SecurityException("Error loading " + alg + " crypto function", e);
		} catch ( InvalidKeyException e ) {
			throw new SecurityException("Error loading " + alg + " crypto function", e);
		}
	}

}

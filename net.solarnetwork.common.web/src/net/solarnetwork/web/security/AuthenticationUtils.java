/* ==================================================================
 * AuthenticationUtils.java - 31/10/2017 1:23:52 PM
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

package net.solarnetwork.web.security;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;

/**
 * Utility methods for authentication support.
 * 
 * @author matt
 * @version 1.0
 */
public final class AuthenticationUtils {

	private AuthenticationUtils() {
		// not available
	}

	/**
	 * Get a HTTP formatted date.
	 * 
	 * @param date
	 *        The date to format.
	 * @return The formatted date.
	 */
	public static String httpDate(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		return sdf.format(date);
	}

	/**
	 * Get an ISO8601 formatted date.
	 * 
	 * @param date
	 *        The date to format.
	 * @return The formatted date.
	 */
	public static String iso8601Date(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		return sdf.format(date);
	}

	/**
	 * AWS style implementation of "uri encoding" using UTF-8 encoding.
	 * 
	 * @param input
	 *        The text input to encode.
	 * @return The URI escaped string.
	 */
	public static String uriEncode(CharSequence input) {
		StringBuilder result = new StringBuilder();
		byte[] tmpByteArray = new byte[1];
		for ( int i = 0; i < input.length(); i++ ) {
			char ch = input.charAt(i);
			if ( (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9')
					|| ch == '_' || ch == '-' || ch == '~' || ch == '.' ) {
				result.append(ch);
			} else {
				try {
					byte[] bytes = String.valueOf(ch).getBytes("UTF-8");
					for ( byte b : bytes ) {
						tmpByteArray[0] = b;
						result.append('%').append(Hex.encodeHex(tmpByteArray, false));
					}
				} catch ( UnsupportedEncodingException e ) {
					// ignore, should never be here
				}
			}
		}
		return result.toString();
	}

	/**
	 * Compute a Base64 MAC digest from signature data.
	 * 
	 * @param secretKey
	 *        the secret key
	 * @param data
	 *        the data to sign
	 * @param macAlgorithm
	 *        the MAC algorithm to use
	 * @return The base64 encoded digest.
	 * @throws SecurityException
	 *         if any error occurs
	 */
	public static final byte[] computeMACDigest(final byte[] secretKey, final String data,
			String macAlgorithm) {
		try {
			return computeMACDigest(secretKey, data.getBytes("UTF-8"), macAlgorithm);
		} catch ( UnsupportedEncodingException e ) {
			throw new SecurityException("Error loading " + macAlgorithm + " crypto function", e);
		}
	}

	/**
	 * Compute a Base64 MAC digest from signature data.
	 * 
	 * @param secretKey
	 *        the secret key
	 * @param data
	 *        the data to sign
	 * @param macAlgorithm
	 *        the MAC algorithm to use
	 * @return The base64 encoded digest.
	 * @throws SecurityException
	 *         if any error occurs
	 */
	public static final byte[] computeMACDigest(final String secretKey, final String data,
			String macAlgorithm) {
		try {
			return computeMACDigest(secretKey.getBytes("UTF-8"), data.getBytes("UTF-8"), macAlgorithm);
		} catch ( UnsupportedEncodingException e ) {
			throw new SecurityException("Error loading " + macAlgorithm + " crypto function", e);
		}
	}

	/**
	 * Compute a Base64 MAC digest from signature data.
	 * 
	 * @param secretKey
	 *        the secret key
	 * @param data
	 *        the data to sign
	 * @param macAlgorithm
	 *        the MAC algorithm to use
	 * @return The base64 encoded digest.
	 * @throws SecurityException
	 *         if any error occurs
	 */
	public static final byte[] computeMACDigest(final byte[] secretKey, final byte[] data,
			String macAlgorithm) {
		Mac mac;
		try {
			mac = Mac.getInstance(macAlgorithm);
			mac.init(new SecretKeySpec(secretKey, macAlgorithm));
			byte[] result = mac.doFinal(data);
			return result;
		} catch ( NoSuchAlgorithmException e ) {
			throw new SecurityException("Error loading " + macAlgorithm + " crypto function", e);
		} catch ( InvalidKeyException e ) {
			throw new SecurityException("Error loading " + macAlgorithm + " crypto function", e);
		}
	}

}

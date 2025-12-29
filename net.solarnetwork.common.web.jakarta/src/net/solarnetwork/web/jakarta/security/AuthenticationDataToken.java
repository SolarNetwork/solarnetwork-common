/* ==================================================================
 * AuthenticationDataToken.java - 27/04/2017 7:10:35 AM
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

package net.solarnetwork.web.jakarta.security;

import static java.nio.charset.StandardCharsets.UTF_8;
import static net.solarnetwork.security.AuthorizationUtils.computeHmacSha256;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import org.apache.commons.codec.binary.Base64;
import jakarta.servlet.http.Cookie;
import net.solarnetwork.codec.jackson.JsonUtils;
import tools.jackson.core.JacksonException;

/**
 * Support for JWT encoded authorization data.
 *
 * This class provides support for JWT encoded authorization data, including
 * using HTTP cookies for persistence of the token data.
 *
 * The only supported token type is {@literal jwt}. The only supported signature
 * algorithm is {@code HMAC-SHA256}, which is encoded as the literal
 * {@literal HS256}.
 *
 * @author matt
 * @version 3.1
 */
public class AuthenticationDataToken {

	/** The header key for the token type. */
	public static final String HEADER_TOKEN_TYPE = "typ";

	/** The header key for the signature algorithm type. */
	public static final String HEADER_SIGN_ALG = "alg";

	/** The JWT token type. */
	public static final String TOKEN_TYPE_JWT = "JWT";

	/** The {@literal HMAC-SHA256} signature algorithm type. */
	public static final String SIGN_ALG_HMAC_SHA256 = "HS256";

	/**
	 * The payload key for the token expiration date claim.
	 *
	 * The value associated with this claim is an integer representing seconds
	 * from the Unix epoch.
	 */
	public static final String CLAIM_EXPIRES = "exp";

	/**
	 * The payload key for the token issue date.
	 *
	 * The value associated with this claim is an integer representing seconds
	 * from the Unix epoch.
	 */
	public static final String CLAIM_ISSUED_AT = "iat";

	/**
	 * The payload key for the token subject.
	 *
	 * The value associated with this claim is a string representing a unique
	 * identifier for the bearer of the token, e.g. a token identifier.
	 */
	public static final String CLAIM_SUBJECT = "sub";

	private static final String MESSAGE_KEY = "__msg";
	private static final String SIGNATURE_KEY = "__sig";

	private final String identity;
	private final long expires;
	private final long issued;
	private final String messageData;
	private final byte[] signature;

	/**
	 * Construct from an existing cookie.
	 *
	 * @param cookie
	 *        The cookie to parse.
	 * @throws IllegalArgumentException
	 *         if the cookie cannot be parsed
	 */
	public AuthenticationDataToken(Cookie cookie) {
		super();
		Map<String, Object> tokenData = parseTokenData(cookie.getValue());

		Object o = tokenData.get(CLAIM_SUBJECT);
		if ( o instanceof String ) {
			identity = (String) o;
		} else {
			throw new IllegalArgumentException("Missing 'sub' property from cookie data");
		}

		o = tokenData.get(CLAIM_EXPIRES);
		if ( o instanceof Number ) {
			expires = ((Number) o).longValue();
		} else {
			throw new IllegalArgumentException("Missing 'exp' property from cookie data");
		}

		o = tokenData.get(CLAIM_ISSUED_AT);
		if ( o instanceof Number ) {
			issued = ((Number) o).longValue();
		} else {
			throw new IllegalArgumentException("Missing 'iat' property from cookie data");
		}

		o = tokenData.get(SIGNATURE_KEY);
		if ( o instanceof byte[] ) {
			signature = (byte[]) o;
		} else {
			throw new IllegalArgumentException("Missing signature from cookie data");
		}

		o = tokenData.get(MESSAGE_KEY);
		if ( o instanceof String ) {
			messageData = (String) o;
		} else {
			throw new IllegalArgumentException("Missing message content from cookie data");
		}
	}

	/**
	 * Construct from {@link AuthenticationData}.
	 *
	 * @param data
	 *        The data to use.
	 * @param secret
	 *        The secret to sign the token data with, as a UTF-8 string.
	 * @throws IllegalArgumentException
	 *         if the data is not supported
	 */
	public AuthenticationDataToken(final AuthenticationData data, final String secret) {
		this(data, utf8bytes(secret));
	}

	/**
	 * Construct from {@link AuthenticationData}.
	 *
	 * @param data
	 *        The data to use.
	 * @param secret
	 *        The secret to sign the token data with.
	 * @throws IllegalArgumentException
	 *         if the data is not supported
	 */
	public AuthenticationDataToken(final AuthenticationData data, final byte[] secret) {
		super();

		identity = data.getAuthTokenId();

		// for expires we assume the token is valid for 7 days
		final GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		cal.setTime(Date.from(data.getDate()));

		issued = cal.getTimeInMillis() / 1000;

		cal.add(Calendar.DATE, 7);
		expires = cal.getTimeInMillis() / 1000;

		// compose the header and payload for the JWT
		Map<String, Object> header = new HashMap<String, Object>();
		header.put(HEADER_TOKEN_TYPE, TOKEN_TYPE_JWT);
		header.put(HEADER_SIGN_ALG, SIGN_ALG_HMAC_SHA256);

		Map<String, Object> payload = new HashMap<String, Object>();
		payload.put(CLAIM_SUBJECT, identity);
		payload.put(CLAIM_EXPIRES, Long.valueOf(expires));
		payload.put(CLAIM_ISSUED_AT, Long.valueOf(issued));

		try {
			messageData = Base64
					.encodeBase64URLSafeString(JsonUtils.JSON_OBJECT_MAPPER.writeValueAsBytes(header))
					+ '.' + Base64.encodeBase64URLSafeString(
							JsonUtils.JSON_OBJECT_MAPPER.writeValueAsBytes(payload));
			signature = computeHmacSha256(secret, messageData);
		} catch ( JacksonException e ) {
			throw new IllegalArgumentException("Error encoding message data JSON", e);
		}
	}

	private static final byte[] utf8bytes(String s) {
		try {
			return s.getBytes("UTF-8");
		} catch ( UnsupportedEncodingException e ) {
			// should not get here
			return new byte[0];
		}
	}

	/**
	 * Parse token data into a map.
	 *
	 * @param cookieValue
	 *        the token data value to parse
	 * @return the parsed data
	 */
	public static final Map<String, Object> parseTokenData(final String cookieValue) {
		// split into header/payload/signature
		final String[] components = cookieValue.split("\\.", 3);

		if ( components.length != 3 ) {
			throw new IllegalArgumentException(
					"Malformed token cookie data (missing header/payload/signature structure)");
		}

		final Map<String, Object> result = new HashMap<String, Object>(8);

		final Base64 decoder = new Base64(true);

		Map<String, Object> map = JsonUtils
				.getStringMap(new String(decoder.decode(components[0]), UTF_8));
		if ( map == null || !TOKEN_TYPE_JWT.equals(map.get(HEADER_TOKEN_TYPE)) ) {
			throw new IllegalArgumentException("Unsupported token type");
		}
		if ( !SIGN_ALG_HMAC_SHA256.equals(map.get(HEADER_SIGN_ALG)) ) {
			throw new IllegalArgumentException("Unsupported token sign algorithm");
		}

		map = JsonUtils.getStringMap(new String(decoder.decode(components[1]), UTF_8));

		if ( map.containsKey(CLAIM_SUBJECT) ) {
			result.put(CLAIM_SUBJECT, map.get(CLAIM_SUBJECT));
		}
		if ( map.containsKey(CLAIM_EXPIRES) ) {
			result.put(CLAIM_EXPIRES, map.get(CLAIM_EXPIRES));
		}
		if ( map.containsKey(CLAIM_ISSUED_AT) ) {
			result.put(CLAIM_ISSUED_AT, map.get(CLAIM_ISSUED_AT));
		}

		result.put(SIGNATURE_KEY, decoder.decode(components[2]));
		result.put(MESSAGE_KEY, components[0] + '.' + components[1]);

		return result;
	}

	/**
	 * Verify the token cookie data using a provided signing secret key and the
	 * current date.
	 *
	 * @param secret
	 *        The secret key to compute the signature digest with, as a UTF-8
	 *        encoded string.
	 * @throws SecurityException
	 *         if the computed digest does not match that provided by the token
	 *         data, or the token has expired
	 */
	public void verify(final String secret) {
		verify(secret, System.currentTimeMillis());
	}

	/**
	 * Verify the token cookie data using a provided signing secret key and
	 * date.
	 *
	 * @param secret
	 *        The secret key to compute the signature digest with, as a UTF-8
	 *        encoded string.
	 * @param date
	 *        The date to compare the token expiration with.
	 * @throws SecurityException
	 *         if the computed digest does not match that provided by the token
	 *         data, or the token has expired
	 */
	public void verify(String secret, final long date) {
		verify(utf8bytes(secret), date);
	}

	/**
	 * Verify the token cookie data using a provided signing secret key and the
	 * current date.
	 *
	 * @param secret
	 *        The secret key to compute the signature digest with.
	 * @throws SecurityException
	 *         if the computed digest does not match that provided by the token
	 *         data, or the token has expired
	 */
	public void verify(final byte[] secret) {
		verify(secret, System.currentTimeMillis());
	}

	/**
	 * Verify the token cookie data using a provided signing secret key and
	 * date.
	 *
	 * @param secret
	 *        The secret key to compute the signature digest with.
	 * @param date
	 *        The date to compare the token expiration with.
	 * @throws SecurityException
	 *         if the computed digest does not match that provided by the token
	 *         data, or the token has expired
	 */
	public void verify(byte[] secret, final long date) {
		byte[] computed = computeHmacSha256(secret, messageData);
		if ( !Arrays.equals(signature, computed) ) {
			throw new SecurityException("Signature does not match.");
		}
		if ( expires * 1000 < date ) {
			throw new SecurityException("Token expired");
		}
	}

	/**
	 * Get a value suitable for storing on a {@link Cookie} from the token data.
	 *
	 * @return The cookie value.
	 */
	public String cookieValue() {
		return messageData + '.' + Base64.encodeBase64URLSafeString(signature);
	}

	/**
	 * Get the identity value, e.g. token ID.
	 *
	 * @return the identity
	 */
	public String getIdentity() {
		return identity;
	}

	/**
	 * Get the expiration date, expressed as seconds since the Unix epoch.
	 *
	 * @return the expiration date
	 */
	public long getExpires() {
		return expires;
	}

	/**
	 * Get the issue date, expressed as seconds since the Unix epoch.
	 *
	 * @return the issued date
	 */
	public long getIssued() {
		return issued;
	}

	/**
	 * Get the digest signature bytes.
	 *
	 * @return the signature
	 */
	public byte[] getSignature() {
		return signature;
	}

}

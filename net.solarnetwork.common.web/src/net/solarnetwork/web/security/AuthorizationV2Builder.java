/* ==================================================================
 * AuthorizationV2Builder.java - 25/04/2017 11:49:43 AM
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

import static net.solarnetwork.web.security.AuthenticationUtils.uriEncode;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

/**
 * Builder for HTTP {@code Authorization} header values using the SolarNetwork
 * authentication scheme V2.
 * 
 * This builder can be used to calculate a one-off header value, for example:
 * 
 * <pre>
 * <code>
 * String authHeader = new AuthorizationV2Builder("my-token")
 *     .path("/solarquery/api/v1/pub/...")
 *     .build("my-token-secret");
 * </code>
 * </pre>
 * 
 * Or the builder can be re-used for a given token:
 * 
 * <pre>
 * <code>
 * // create a builder for a token
 * AuthorizationV2Builder builder = new AuthorizationV2Builder("my-token");
 * 
 * // elsewhere, re-use the builder for repeated requests
 * builder.reset()
 *     .path("/solarquery/api/v1/pub/...")
 *     .build("my-token-secret");
 * </code>
 * </pre>
 * 
 * Additionally, a signing key can be generated and re-used for up to 7 days:
 * 
 * <pre>
 * <code>
 * // create a builder for a token
 * AuthorizationV2Builder builder = new AuthorizationV2Builder("my-token")
 *   .saveSigningKey("my-token-secret");
 * 
 * // elsewhere, re-use the builder for repeated requests
 * builder.reset()
 *     .path("/solarquery/api/v1/pub/...")
 *     .build(); // note no argument call here, so previously generated key used
 * </code>
 * </pre>
 * 
 * @author matt
 * @version 1.3
 * @since 1.11
 */
public final class AuthorizationV2Builder {

	private final String tokenId;
	private HttpMethod httpMethod;
	private String requestPath;
	private MultiValueMap<String, String> parameters;
	private HttpHeaders httpHeaders;
	private Date date;
	private byte[] signingKey;
	private Set<String> signedHeaderNames;
	private byte[] contentSHA256;

	/**
	 * Construct with a token ID.
	 * 
	 * The builder will be initialized and then {@link #reset()} will be called
	 * so default values are configured.
	 * 
	 * @param tokenId
	 *        The token ID to use.
	 */
	public AuthorizationV2Builder(String tokenId) {
		super();
		this.tokenId = tokenId;
		reset();
	}

	/**
	 * Set the request date.
	 * 
	 * @param date
	 *        The date to use; typically the current time, e.g.
	 *        {@code new Date()}.
	 * @return The builder.
	 */
	public AuthorizationV2Builder date(Date date) {
		this.date = (date == null ? new Date() : date);
		return this;
	}

	/**
	 * Set the HTTP method
	 * 
	 * @param method
	 *        The method to use.
	 * @return The builder.
	 */
	public AuthorizationV2Builder method(HttpMethod method) {
		this.httpMethod = method;
		return this;
	}

	/**
	 * Set the HTTP host.
	 * 
	 * This is a shortcut for calling {@code HttpHeaders#set(String, String)}
	 * with a {@literal Host} key.
	 * 
	 * @param host
	 *        The host to use.
	 * @return The builder.
	 */
	public AuthorizationV2Builder host(String host) {
		httpHeaders.set(HttpHeaders.HOST, host);
		return this;
	}

	/**
	 * Set the HTTP content type.
	 * 
	 * This is a shortcut for calling {@code HttpHeaders#set(String, String)}
	 * with a {@literal Content-Type} key.
	 * 
	 * @param contentType
	 *        The content type to use.
	 * @return The builder.
	 */
	public AuthorizationV2Builder contentType(String contentType) {
		httpHeaders.set(HttpHeaders.CONTENT_TYPE, contentType);
		return this;
	}

	/**
	 * Set the HTTP body content MD5 digest.
	 * 
	 * This is a shortcut for calling {@code HttpHeaders#set(String, String)}
	 * with a {@literal Content-MD5} key.
	 * 
	 * @param md5
	 *        The content MD5 to use.
	 * @return The builder.
	 */
	public AuthorizationV2Builder contentMD5(String md5) {
		httpHeaders.set("Content-MD5", md5);
		return this;
	}

	/**
	 * Set the HTTP body content digest.
	 * 
	 * This is a shortcut for calling {@code HttpHeaders#set(String, String)}
	 * with a {@literal Digest} key.
	 * 
	 * @param digest
	 *        The digest to use.
	 * @return The builder.
	 */
	public AuthorizationV2Builder digest(String digest) {
		httpHeaders.set("Digest", digest);
		return this;
	}

	/**
	 * Set the HTTP request path.
	 * 
	 * @param path
	 *        The request path to use.
	 * @return The builder.
	 */
	public AuthorizationV2Builder path(String path) {
		this.requestPath = path;
		return this;
	}

	/**
	 * Set the HTTP request body content SHA-256 digest value.
	 * 
	 * <p>
	 * <b>Note</b> if the content is form-encoded parameters set via the
	 * {@link #queryParams(Map)} method, this method should not be called, or
	 * called only with {@link WebConstants#EMPTY_STRING_SHA256_HEX}, as this
	 * type of body is treated as request parameters.
	 * </p>
	 * 
	 * @param digest
	 *        The digest value to use.
	 * @return The builder.
	 */
	public AuthorizationV2Builder contentSHA256(byte[] digest) {
		byte[] copy = null;
		if ( digest != null && digest.length >= 32 ) {
			copy = new byte[32];
			System.arraycopy(digest, 0, copy, 0, 32);
		}
		this.contentSHA256 = copy;
		return this;
	}

	/**
	 * Set the HTTP {@code GET} query parameters, or {@code POST} form-encoded
	 * parameters.
	 * 
	 * @param params
	 *        The parameters to use.
	 * @return The builder.
	 */
	public AuthorizationV2Builder parameterMap(Map<String, String[]> params) {
		MultiValueMap<String, String> map = null;
		if ( params != null ) {
			map = new LinkedMultiValueMap<String, String>(params.size());
			for ( Map.Entry<String, String[]> me : params.entrySet() ) {
				for ( String v : me.getValue() ) {
					map.add(me.getKey(), v);
				}
			}
		}
		this.parameters = map;
		return this;
	}

	/**
	 * Set the HTTP {@code GET} query parameters, or {@code POST} form-encoded
	 * parameters, as a simple {@code Map}.
	 * 
	 * @param params
	 *        The parameters to use.
	 * @return The builder.
	 */
	public AuthorizationV2Builder queryParams(Map<String, String> params) {
		MultiValueMap<String, String> map = null;
		if ( params != null ) {
			map = new LinkedMultiValueMap<String, String>(params.size());
			for ( Map.Entry<String, String> me : params.entrySet() ) {
				map.add(me.getKey(), me.getValue());
			}
		}
		this.parameters = map;
		return this;
	}

	/**
	 * Set the HTTP headers to use with the request.
	 * 
	 * The headers object must include all headers necessary by the
	 * authentication scheme, and any additional headers also configured via
	 * {@link #signedHttpHeaders(Set)}.
	 * 
	 * @param headers
	 *        The HTTP headers to use.
	 * @return The builder.
	 */
	public AuthorizationV2Builder headers(HttpHeaders headers) {
		this.httpHeaders = headers;
		return this;
	}

	/**
	 * Set a HTTP header value.
	 * 
	 * This is a shortcut for calling {@link HttpHeaders#set(String, String)} on
	 * the builder's {@code HttpHeaders} instance.
	 * 
	 * @param headerName
	 *        The header name to set.
	 * @param headerValue
	 *        The header value to set.
	 * @return The builder.
	 */
	public AuthorizationV2Builder header(String headerName, String headerValue) {
		httpHeaders.set(headerName, headerValue);
		return this;
	}

	/**
	 * Set additional HTTP header names to sign with the digest.
	 * 
	 * @param signedHeaderNames
	 *        Additional HTTP header names to include in the computed digest.
	 * @return The builder.
	 */
	public AuthorizationV2Builder signedHttpHeaders(Set<String> signedHeaderNames) {
		this.signedHeaderNames = signedHeaderNames;
		return this;
	}

	/**
	 * Reset all values to their defaults.
	 * 
	 * All properties will be set to {@code null} except the following:
	 * 
	 * <dl>
	 * <dt>date</dt>
	 * <dd>Will be set to the current time.</dd>
	 * 
	 * <dt>headers</dt>
	 * <dd>Will be set to a new instance.</dd>
	 * 
	 * <dt>method</dt>
	 * <dd>Will be set to {@code GET}.</dd>
	 * 
	 * <dt>path</dt>
	 * <dd>Will be set to {@code /}.</dd>
	 * 
	 * <dt>signingKey</dt>
	 * <dd>This value will not be changed.</dd>
	 * </dl>
	 * 
	 * @return The builder.
	 */
	public AuthorizationV2Builder reset() {
		contentSHA256 = null;
		httpHeaders = new HttpHeaders();
		parameters = null;
		signedHeaderNames = null;
		return method(HttpMethod.GET).host("data.solarnetwork.net:443").path("/").date(new Date());
	}

	/**
	 * Compute and cache the signing key.
	 * 
	 * <p>
	 * Signing keys are derived from the token secret and valid for 7 days, so
	 * this method can be used to compute a signing key so that {@link #build()}
	 * can be called later. The signing date will be set to whatever date is
	 * currently configured via {@link #date(Date)}, which defaults to the
	 * current time for newly created builder instances.
	 * </p>
	 * 
	 * @param tokenSecret
	 *        The secret to sign the digest with.
	 * @return The builder.
	 * @throws SecurityException
	 *         if any error occurs computing the key
	 */
	public AuthorizationV2Builder saveSigningKey(String tokenSecret) {
		signingKey = computeSigningKey(tokenSecret);
		return this;
	}

	/**
	 * Set the signing key directly.
	 * 
	 * <p>
	 * Use this method if a signing key has been computed externally. The effect
	 * is the same as in {@link #saveSigningKey(String)} in that the
	 * {@link #build()} method can then be called to compute the authorization
	 * value using this key.
	 * </p>
	 * 
	 * @param key
	 *        The signing key to set.
	 * @return The builder.
	 * @since 1.1
	 */
	public AuthorizationV2Builder signingKey(byte[] key) {
		signingKey = key;
		return this;
	}

	/**
	 * Get the signing key, encoded as hex.
	 * 
	 * @return the computed or saved signing key encoded as hex, or
	 *         {@literal null} if none computed or saved yet
	 * @since 1.1
	 */
	public String signingKeyHex() {
		return Hex.encodeHexString(this.signingKey);
	}

	/**
	 * Compute a HTTP {@code Authorization} header value from the configured
	 * properties on the builder.
	 * 
	 * @param tokenSecret
	 *        The secret to sign the digest with.
	 * @return The HTTP header value.
	 * @throws SecurityException
	 *         if any error occurs computing the header value
	 */
	public String build(final String tokenSecret) {
		final String[] sortedHeaderNames = sortedHeaderNames();
		final byte[] theSigningKey = computeSigningKey(tokenSecret);
		final String signatureData = computeSignatureData(
				computeCanonicalRequestData(sortedHeaderNames));
		final String signature = Hex.encodeHexString(computeHMACSHA256(theSigningKey, signatureData));
		final StringBuilder buf = new StringBuilder(AuthenticationScheme.V2.getSchemeName());
		buf.append(' ');
		buf.append("Credential=").append(tokenId);
		buf.append(",SignedHeaders=").append(StringUtils.arrayToDelimitedString(sortedHeaderNames, ";"));
		buf.append(",Signature=").append(signature);
		return buf.toString();
	}

	/**
	 * Compute a HTTP {@code Authorization} header value from the configured
	 * properties on the builder, using a signing key created from a previous
	 * call to {@link #saveSigningKey(String)}.
	 * 
	 * @return The HTTP header value.
	 * @throws SecurityException
	 *         if any error occurs computing the header value
	 */
	public String build() {
		final String[] sortedHeaderNames = sortedHeaderNames();
		final String signatureData = computeSignatureData(
				computeCanonicalRequestData(sortedHeaderNames));
		final String signature = Hex.encodeHexString(computeHMACSHA256(signingKey, signatureData));
		final StringBuilder buf = new StringBuilder(AuthenticationScheme.V2.getSchemeName());
		buf.append(' ');
		buf.append("Credential=").append(tokenId);
		buf.append(",SignedHeaders=").append(StringUtils.arrayToDelimitedString(sortedHeaderNames, ";"));
		buf.append(",Signature=").append(signature);
		return buf.toString();
	}

	public String buildCanonicalRequestData() {
		return computeCanonicalRequestData(sortedHeaderNames());
	}

	private String[] sortedHeaderNames() {
		Set<String> headerNames = new HashSet<String>(3);
		headerNames.add("Host");
		if ( httpHeaders.containsKey("X-SN-Date") ) {
			headerNames.add("X-SN-Date");
		} else {
			headerNames.add("Date");
		}
		if ( httpHeaders.containsKey("Content-MD5") ) {
			headerNames.add("Content-MD5");
		}
		if ( httpHeaders.containsKey("Content-Type") ) {
			headerNames.add("Content-Type");
		}
		if ( httpHeaders.containsKey("Digest") ) {
			headerNames.add("Digest");
		}
		if ( signedHeaderNames != null ) {
			headerNames.addAll(signedHeaderNames);
		}
		return lowercaseSortedArray(headerNames.toArray(new String[headerNames.size()]));
	}

	/**
	 * Compute a HMAC-SHA256 digest from UTF-8 string values.
	 * 
	 * @param password
	 *        The password to sign the digest with, which is assumed to be a
	 *        UTF-8 encoded string.
	 * @param msg
	 *        The message content to sign, which is assumed to be a UTF-8
	 *        encoded string.
	 * @return The computed digest.
	 * @throws SecurityException
	 *         if the strings cannot be interpreted as UTF-8 or {@code alg} is
	 *         not supported by the runtime security provider
	 */
	public static byte[] computeHMACSHA256(final String password, final String msg) {
		return computeMacDigest(password, msg, "HmacSHA256");
	}

	/**
	 * Compute a HMAC-SHA256 digest from a byte array password.
	 * 
	 * @param password
	 *        The password to sign the digest with.
	 * @param msg
	 *        The message content to sign, which is assumed to be a UTF-8
	 *        encoded string.
	 * @return The computed digest.
	 * @throws SecurityException
	 *         if the strings cannot be interpreted as UTF-8 or {@code alg} is
	 *         not supported by the runtime security provider
	 */
	public static byte[] computeHMACSHA256(final byte[] password, final String msg) {
		try {
			return computeMacDigest(password, msg.getBytes("UTF-8"), "HmacSHA256");
		} catch ( UnsupportedEncodingException e ) {
			throw new SecurityException("Error loading HmacSHA1 crypto function", e);
		}
	}

	/**
	 * Compute a MAC digest from UTF-8 string values.
	 * 
	 * @param password
	 *        The password to sign the digest with, which is assumed to be a
	 *        UTF-8 encoded string.
	 * @param msg
	 *        The message content to sign, which is assumed to be a UTF-8
	 *        encoded string.
	 * @param alg
	 *        The MAC algorithm to use, which must be supported by the runtime
	 *        security provider.
	 * @return The computed digest.
	 * @throws SecurityException
	 *         if the strings cannot be interpreted as UTF-8 or {@code alg} is
	 *         not supported by the runtime security provider
	 */
	public static byte[] computeMacDigest(final String password, final String msg, String alg) {
		try {
			return computeMacDigest(password.getBytes("UTF-8"), msg.getBytes("UTF-8"), alg);
		} catch ( UnsupportedEncodingException e ) {
			throw new SecurityException("Error encoding secret or message for crypto function", e);
		}
	}

	/**
	 * Compute a MAC digest.
	 * 
	 * @param password
	 *        The password to sign the digest with.
	 * @param msg
	 *        The message content to sign.
	 * @param alg
	 *        The MAC algorithm to use, which must be supported by the runtime
	 *        security provider.
	 * @return The computed digest.
	 * @throws SecurityException
	 *         if the strings cannot be interpreted as UTF-8 or {@code alg} is
	 *         not supported by the runtime security provider
	 */
	public static byte[] computeMacDigest(final byte[] password, final byte[] msg, String alg) {
		Mac hmacSha1;
		try {
			hmacSha1 = Mac.getInstance(alg);
			hmacSha1.init(new SecretKeySpec(password, alg));
			byte[] result = hmacSha1.doFinal(msg);
			return result;
		} catch ( NoSuchAlgorithmException e ) {
			throw new SecurityException("Error loading " + alg + " crypto function", e);
		} catch ( InvalidKeyException e ) {
			throw new SecurityException("Error loading " + alg + " crypto function", e);
		}
	}

	/**
	 * Format a date into the HTTP {@code Date} header syntax, in the
	 * {@code GMT} time zone.
	 * 
	 * @param date
	 *        The date to format.
	 * @return The formatted date string.
	 */
	public static String httpDate(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		return sdf.format(date);
	}

	/**
	 * Format a date into the ISO8601 condensed timestamp form, in the
	 * {@code GMT} time zone.
	 * 
	 * @param date
	 *        The date to format.
	 * @return The formatted date string.
	 */
	public static String iso8601Date(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		return sdf.format(date);
	}

	private static String[] lowercaseSortedArray(String[] headerNames) {
		String[] sortedHeaderNames = new String[headerNames.length];
		for ( int i = 0; i < headerNames.length; i++ ) {
			sortedHeaderNames[i] = headerNames[i].toLowerCase();
		}
		Arrays.sort(sortedHeaderNames);
		return sortedHeaderNames;
	}

	private byte[] computeSigningKey(String secretKey) {
		return computeSigningKey(secretKey, this.date);
	}

	/**
	 * Compute a signing key from a secret key and date.
	 * 
	 * @param secretKey
	 *        the secret key to derive the signing key from
	 * @param date
	 *        the signing date
	 * @return the signing key
	 * @since 1.2
	 */
	public static byte[] computeSigningKey(String secretKey, Date date) {
		/*- signing key is like:
		 
		HMACSHA256(HMACSHA256("SNWS2"+secretKey, "20160301"), "snws2_request")
		*/
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		return computeHMACSHA256(
				computeHMACSHA256(AuthenticationScheme.V2.getSchemeName() + secretKey, sdf.format(date)),
				"snws2_request");
	}

	private void appendQueryParameters(StringBuilder buf) {
		Set<String> paramKeys = (parameters != null ? parameters.keySet()
				: Collections.<String> emptySet());
		if ( paramKeys.size() < 1 ) {
			buf.append('\n');
			return;
		}
		String[] keys = paramKeys.toArray(new String[paramKeys.size()]);
		Arrays.sort(keys);
		boolean first = true;
		for ( String key : keys ) {
			for ( String val : parameters.get(key) ) {
				if ( first ) {
					first = false;
				} else {
					buf.append('&');
				}
				buf.append(uriEncode(key)).append('=').append(uriEncode(val));
			}
		}
		buf.append('\n');
	}

	private void appendHeaders(String[] sortedLowercaseHeaderNames, StringBuilder buf) {
		for ( String headerName : sortedLowercaseHeaderNames ) {
			String headerValue;
			if ( "date".equals(headerName) ) {
				headerValue = httpDate(date);
			} else if ( "x-sn-date".equals(headerName) ) {
				headerValue = httpDate(date);
			} else {
				headerValue = httpHeaders.getFirst(headerName);
			}
			buf.append(headerName).append(':').append(headerValue != null ? headerValue.trim() : "")
					.append('\n');
		}
	}

	private static void appendSignedHeaderNames(String[] sortedLowercaseHeaderNames, StringBuilder buf) {
		boolean first = true;
		for ( String headerName : sortedLowercaseHeaderNames ) {
			if ( first ) {
				first = false;
			} else {
				buf.append(';');
			}
			buf.append(headerName);
		}
		buf.append('\n');
	}

	private void appendContentSHA256(StringBuilder buf) {
		byte[] digest = contentSHA256;
		buf.append(digest == null ? WebConstants.EMPTY_STRING_SHA256_HEX : Hex.encodeHexString(digest));
	}

	private String computeCanonicalRequestData(String[] sortedLowercaseHeaderNames) {
		// 1: HTTP verb
		StringBuilder buf = new StringBuilder(httpMethod.toString()).append('\n');

		// 2: Canonical URI
		buf.append(requestPath).append('\n');

		// 3: Canonical query string
		appendQueryParameters(buf);

		// 4: Canonical headers
		appendHeaders(sortedLowercaseHeaderNames, buf);

		// 5: Signed headers
		appendSignedHeaderNames(sortedLowercaseHeaderNames, buf);

		// 6: Content SHA256
		appendContentSHA256(buf);

		return buf.toString();

	}

	private String computeSignatureData(String canonicalRequestData) {
		/*- signature data is like:
		 
		 	SNWS2-HMAC-SHA256\n
		 	20170301T120000Z\n
		 	Hex(SHA256(canonicalRequestData))
		*/
		return "SNWS2-HMAC-SHA256\n" + iso8601Date(date) + "\n"
				+ Hex.encodeHexString(DigestUtils.sha256(canonicalRequestData));
	}

}

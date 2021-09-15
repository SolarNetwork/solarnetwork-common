/* ==================================================================
 * AbstractAuthorizationBuilder.java - 28/08/2021 5:14:56 PM
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

import static net.solarnetwork.security.AuthorizationUtils.AUTHORIZATION_DATE_FORMATTER;
import static net.solarnetwork.security.AuthorizationUtils.AUTHORIZATION_TIMESTAMP_FORMATTER;
import static net.solarnetwork.security.AuthorizationUtils.computeHmacSha256;
import static net.solarnetwork.security.AuthorizationUtils.computeHmacSha256Hex;
import static net.solarnetwork.security.AuthorizationUtils.semiColonDelimitedList;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Base class for SolarNetwork authorization builder support.
 * 
 * <p>
 * This builder supports an HMAC-SHA256 style digest authentication scheme in
 * the spirit of the AWS S3 Signature v4 scheme, but adapted for SolarNetwork.
 * In this scheme, a generated authorization credential takes the form of an
 * HTTP {@literal Authorization} header:
 * </p>
 * 
 * <pre>
 * SCHEME Credential=C,SignedHeaders=H,Signature=S
 * </pre>
 * 
 * <p>
 * The {@literal SCHEME} is provided by {#link {@link #schemeName()}}. The
 * credential {@literal C} is the {@code identifier} provided to the
 * constructor, and represents a login or token ID. The signed headers
 * {@literal H} is a semicolon-delimited list of request header names used when
 * computing the signature {@literal S}. The signature {@literal S} is a
 * hex-encoded HMAC-SHA256 digest.
 * </p>
 * 
 * <p>
 * The <em>signing key</em> used to sign the HMAC-SHA256 signature {@literal S}
 * is a value derived from a provided secret (i.e. password or token secret) by
 * computing a HMAC-SHA256 digest that uses a specific date (usually the current
 * date). See {@link #computeSigningKey(Instant, String)} for more details.
 * </p>
 * 
 * <p>
 * The <em>signing message</em> used in the HMAC-SHA256 signature {@literal S}
 * is a value derived from the configurable properties of this class: a date,
 * verb, path, list of key/value headers, and any other properties defined by
 * implementing subclasses. This message is provided by the
 * {@link #computeCanonicalRequestMessage(String[])} method. See the
 * {@link #computeSignatureData(Instant, String)} method for more details.
 * </p>
 * 
 * @param <T>
 *        the implementation type
 * @author matt
 * @version 1.0
 */
public abstract class AbstractAuthorizationBuilder<T extends AbstractAuthorizationBuilder<T>> {

	/** The authorization header component for the credential (identifier). */
	public static final String AUTHORIZATION_COMPONENT_CREDENTIAL = "Credential";

	/** The authorization header component for the signed header name list. */
	public static final String AUTHORIZATION_COMPONENT_HEADERS = "SignedHeaders";

	/** The authorization header component for the signature. */
	public static final String AUTHORIZATION_COMPONENT_SIGNATURE = "Signature";

	private final String identifier;
	private MultiValueMap<String, String> headers;
	private String verb;
	private String requestPath;
	private Instant date;
	private byte[] signingKey;
	private byte[] contentSha256;

	/**
	 * Construct with a credential.
	 * 
	 * <p>
	 * The builder will be initialized and then {@link #reset()} will be called
	 * so default values are configured.
	 * </p>
	 * 
	 * @param identifier
	 *        the bearer's identifier, such as a token ID or username
	 */
	public AbstractAuthorizationBuilder(String identifier) {
		super();
		this.identifier = identifier;
		reset();
	}

	/**
	 * Reset all values to their defaults.
	 * 
	 * <p>
	 * All properties will be set to {@code null} except the following:
	 * </p>
	 * 
	 * <dl>
	 * <dt>verb</dt>
	 * <dd>will be set to {@literal GET}</dd>
	 * 
	 * <dt>path</dt>
	 * <dd>Will be set to {@literal /}</dd>
	 * 
	 * <dt>date</dt>
	 * <dd>will be set to the current time</dd>
	 * 
	 * <dt>signingKey</dt>
	 * <dd>this value will <b>not</b> be changed</dd>
	 * </dl>
	 * 
	 * @return The builder.
	 */
	public T reset() {
		contentSha256 = null;
		headers = null;
		return verb("GET").path("/").date(null);
	}

	/**
	 * Set the request date.
	 * 
	 * <p>
	 * This will also set the {@literal date} header with the date's formatted
	 * value.
	 * </p>
	 * 
	 * @param date
	 *        the date to use, or {@literal null} for the current system time
	 *        via {@code Instant.now()}; will be truncated to second resolution
	 * @return this builder
	 */
	@SuppressWarnings("unchecked")
	public T date(Instant date) {
		Instant d = (date == null ? Instant.now() : date).truncatedTo(ChronoUnit.SECONDS);
		this.date = d;
		return (T) this;
	}

	/**
	 * Get the date.
	 * 
	 * @return the date
	 */
	public Instant getDate() {
		return date;
	}

	/**
	 * Set the verb.
	 * 
	 * <p>
	 * The meaning of the verb depends on the communication protocol being used,
	 * such as a HTTP method or STOMP command.
	 * </p>
	 * 
	 * @param verb
	 *        the verb
	 * @return this builder
	 * @throws IllegalArgumentException
	 *         if {@code verb} is {@literal null}
	 */
	@SuppressWarnings("unchecked")
	public T verb(String verb) {
		if ( verb == null ) {
			throw new IllegalArgumentException("The verb argument must not be null.");
		}
		this.verb = verb;
		return (T) this;
	}

	/**
	 * Get the verb.
	 * 
	 * @return the verb
	 */
	public String getVerb() {
		return verb;
	}

	/**
	 * Set the request path.
	 * 
	 * @param path
	 *        the request path to use
	 * @return this builder
	 * @throws IllegalArgumentException
	 *         if {@code path} is {@literal null}
	 */
	@SuppressWarnings("unchecked")
	public T path(String path) {
		if ( verb == null ) {
			throw new IllegalArgumentException("The path argument must not be null.");
		}
		this.requestPath = path;
		return (T) this;
	}

	/**
	 * Get the request path.
	 * 
	 * @return the path
	 */
	public String getPath() {
		return requestPath;
	}

	/**
	 * Set the host.
	 * 
	 * <p>
	 * This is a shortcut for calling {@code #header(String, String)} with a
	 * {@literal host} key.
	 * </p>
	 * 
	 * @param host
	 *        the host value
	 * @return this builder
	 */
	public T host(String host) {
		return header("host", host);
	}

	/**
	 * Set the body content SHA-256 digest value.
	 * 
	 * @param digest
	 *        the digest value to use or {@literal null} for none; if provided,
	 *        the array must have a length of {@literal 32} and will be copied
	 * @return this builder
	 */
	@SuppressWarnings("unchecked")
	public T contentSha256(byte[] digest) {
		byte[] copy = null;
		if ( digest != null && digest.length >= 32 ) {
			copy = new byte[32];
			System.arraycopy(digest, 0, copy, 0, 32);
		}
		this.contentSha256 = copy;
		return (T) this;
	}

	/**
	 * Get the content SHA-256 value.
	 * 
	 * @return the content SHA 256
	 */
	public byte[] getContentSha256() {
		return contentSha256;
	}

	/**
	 * Append the content SHA 256 value to a string buffer.
	 * 
	 * @param buf
	 *        the buffer to append to
	 */
	protected void appendContentSha256(StringBuilder buf) {
		final byte[] digest = contentSha256;
		buf.append(digest == null || digest.length < 1 ? AuthorizationUtils.EMPTY_STRING_SHA256_HEX
				: Hex.encodeHexString(digest));
	}

	/**
	 * Append a list of header name and value pairs.
	 * 
	 * @param headerNames
	 *        the header names to append
	 * @param buf
	 *        the buffer to append to
	 */
	protected void appendHeaders(String[] headerNames, StringBuilder buf) {
		final MultiValueMap<String, String> m = getHeaders();
		for ( String h : headerNames ) {
			List<String> vals = (m != null ? m.get(h) : null);
			if ( vals == null || vals.isEmpty() ) {
				buf.append('\n');
			} else {
				for ( String v : vals ) {
					buf.append(h).append(':');
					if ( v != null && !v.isEmpty() ) {
						// header values are trimmed and consecutive spaces collapsed into a single space
						buf.append(v.trim().replaceAll(" {2,}", " "));
					}
					buf.append('\n');
				}
			}
		}

	}

	/**
	 * Set the headers.
	 * 
	 * @param headers
	 *        the headers to use
	 * @return this builder
	 */
	@SuppressWarnings("unchecked")
	public T headers(MultiValueMap<String, String> headers) {
		this.headers = headers;
		return (T) this;
	}

	/**
	 * Get the headers.
	 * 
	 * @return the headers
	 */
	public MultiValueMap<String, String> getHeaders() {
		return headers;
	}

	/**
	 * Set a header value.
	 * 
	 * <p>
	 * Header values are <b>replaced</b> if a given {@code headerName} is passed
	 * more than once.
	 * </p>
	 * 
	 * @param headerName
	 *        the header name to set; this will be stored in lower-case
	 * @param headerValue
	 *        the header value(s) to set
	 * @return this builder
	 * @throws IllegalArgumentException
	 *         if any argument is {@literal null}
	 */
	@SuppressWarnings("unchecked")
	public T header(String headerName, String... headerValue) {
		if ( headerName == null || headerName.isEmpty() || headerValue == null
				|| headerValue.length < 1 ) {
			throw new IllegalArgumentException(
					"The headerName and headerValue arguments must not be null or empty.");
		}
		if ( headers == null ) {
			headers = new LinkedMultiValueMap<>(4);
		}
		headers.put(headerName.toLowerCase(), Arrays.asList(headerValue));
		return (T) this;
	}

	/**
	 * Get the first available header value.
	 * 
	 * @param headerName
	 *        the name of the header to get the first value for
	 * @return the header value, or {@literal null} if the header does not exist
	 */
	public String headerValue(String headerName) {
		List<String> values = headerValues(headerName);
		return (values != null && !values.isEmpty() ? values.get(0) : null);
	}

	/**
	 * Get all available header values.
	 * 
	 * @param headerName
	 *        the name of the header to get the values for
	 * @return the header values, or {@literal null} if the header does not
	 *         exist
	 */
	public List<String> headerValues(String headerName) {
		return (headerName != null && headers != null ? headers.get(headerName.toLowerCase()) : null);
	}

	/**
	 * Compute and cache the signing key.
	 * 
	 * <p>
	 * Signing keys are derived from the a secret value and valid for 7 days, so
	 * this method can be used to compute a signing key so that {@link #build()}
	 * can be called later. The signing date will be set to whatever date is
	 * currently configured via {@link #date(Instant)}, which defaults to the
	 * current time for newly created builder instances.
	 * </p>
	 * 
	 * @param secret
	 *        the secret to sign the digest with
	 * @return this builder
	 * @throws SecurityException
	 *         if any error occurs computing the key
	 */
	@SuppressWarnings("unchecked")
	public T saveSigningKey(String secret) {
		signingKey = computeSigningKey(this.date, secret);
		return (T) this;
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
	 *        the signing key to set
	 * @return this builder
	 */
	@SuppressWarnings("unchecked")
	public T signingKey(byte[] key) {
		signingKey = key;
		return (T) this;
	}

	/**
	 * Get the signing key, encoded as hex.
	 * 
	 * @return the computed or saved signing key encoded as hex, or
	 *         {@literal null} if none computed or saved yet
	 */
	public String signingKeyHex() {
		final byte[] k = this.signingKey;
		return (k != null ? Hex.encodeHexString(k) : null);
	}

	/**
	 * Get the signing key message.
	 * 
	 * @return the signing key message
	 */
	protected abstract String signingKeyMessageLiteral();

	/**
	 * Compute a signing key from a secret key and date.
	 * 
	 * <p>
	 * A signing key is derived from {@code key} and {@code date} using the
	 * following algorithm:
	 * </p>
	 * 
	 * <pre>
	 * <code>
	 * HmacSha256(HmacSha256("SCHEME"+secret, "YYYYMMDD"), "SIGNING_KEY_MESSAGE_LITERAL")
	 * </code>
	 * </pre>
	 * 
	 * <p>
	 * The {@code HmacSha256(key, message)} function computes a HMAC+SHA256
	 * digest value. The {@literal YYYYMMDD} value is {@code date} formatted in
	 * the UTC time zone.
	 * </p>
	 * 
	 * @param date
	 *        the signing date
	 * @param secret
	 *        the secret to derive the signing key from
	 * 
	 * @return the signing key
	 * @throws IllegalArgumentException
	 *         if any argument is {@literal null}
	 */
	public byte[] computeSigningKey(Instant date, String secret) {
		if ( secret == null || date == null ) {
			throw new IllegalArgumentException("The secret and date arguments must not be null.");
		}
		String day = AUTHORIZATION_DATE_FORMATTER.format(date);
		return computeHmacSha256(computeHmacSha256(schemeName() + secret, day),
				signingKeyMessageLiteral());
	}

	/**
	 * Get all configured header names as a sorted array of lower-case values.
	 * 
	 * @return the sorted array, never {@literal null}
	 */
	public String[] sortedHeaderNames() {
		SortedSet<String> headerNames = new TreeSet<String>();
		final MultiValueMap<String, String> h = this.headers;
		int count = 0;
		if ( h != null ) {
			for ( String k : h.keySet() ) {
				if ( k != null ) {
					headerNames.add(k.toLowerCase());
					count++;
				}
			}
		}
		return headerNames.toArray(new String[count]);
	}

	/**
	 * Compute the canonical request message.
	 * 
	 * @return the message content, never {@literal null}
	 * @see #computeCanonicalRequestMessage(String[])
	 */
	public String computeCanonicalRequestMessage() {
		return computeCanonicalRequestMessage(sortedHeaderNames());
	}

	/**
	 * Compute the canonical request message.
	 * 
	 * @param headerNames
	 *        the header names to include in the signature
	 * @return the canonical request message
	 */
	protected abstract String computeCanonicalRequestMessage(String[] headerNames);

	/**
	 * Compute the final signature data.
	 * 
	 * <p>
	 * The message is computed using the following algorithm:
	 * </p>
	 * 
	 * <pre>
	 * <code>
	 * SCHEME_NAME-HMAC-SHA256\n
	 * YYYYMMTDDHHmmssZ\n
	 * Hex(Sha256(canonicalRequestMessage))
	 * </code>
	 * </pre>
	 * 
	 * <p>
	 * The date is formatted using the
	 * {@link AuthorizationUtils#AUTHORIZATION_TIMESTAMP_FORMATTER}.
	 * </p>
	 * 
	 * @param date
	 *        the request date
	 * @param canonicalRequestMessage
	 *        the canonical request message, i.e. from
	 *        {@link #computeCanonicalRequestMessage()}
	 * @return the final data to be signed for the request
	 */
	public String computeSignatureData(Instant date, String canonicalRequestMessage) {
		// @formatter:off
		return schemeName() +"-HMAC-SHA256\n" 
				+ AUTHORIZATION_TIMESTAMP_FORMATTER.format(date) + "\n"
				+ Hex.encodeHexString(DigestUtils.sha256(canonicalRequestMessage));
		// @formatter:on
	}

	/**
	 * Compute an {@literal Authorization} header value from the configured
	 * properties on the builder, using a signing key created from a previous
	 * call to {@link #saveSigningKey(String)} or {@link #signingKey(byte[])}.
	 * 
	 * <p>
	 * The message is formatted using the following structure:
	 * </p>
	 * 
	 * <pre>
	 * <code>
	 * SNS Credential=identifier,SignedHeaders=headerList,Signature=Hex(HmacSha256(signingKey,signatureData))
	 * </code>
	 * </pre>
	 * 
	 * <p>
	 * Where {@code identifier} is the identifier passed to the constructor,
	 * {@code headerList} is a semicolon-delimited list of
	 * {@link #sortedHeaderNames()}, {@code signingKey} is the signing key set
	 * via {@link #signingKey(byte[])} or computed via
	 * {@link #saveSigningKey(String)}, and {@code signatureData} is the
	 * computed signature data via
	 * {@link #computeSignatureData(Instant, String)}.
	 * </p>
	 * 
	 * @return the header value
	 * @throws SecurityException
	 *         if any error occurs computing the header value
	 */
	public String build() {
		return build(signingKey);
	}

	/**
	 * Compute an {@literal Authorization} header value from the configured
	 * properties on the builder, using the provided secret.
	 * 
	 * @param secret
	 *        the secret to sign the digest with; will use the configured date
	 *        to compute the singing key
	 * @return the header value
	 * @throws SecurityException
	 *         if any error occurs computing the header value
	 * @see #build()
	 */
	public String build(final String secret) {
		final byte[] signingKey = computeSigningKey(date, secret);
		return build(signingKey);
	}

	/**
	 * Get the authorization scheme name.
	 * 
	 * @return the scheme name
	 */
	protected abstract String schemeName();

	private String build(byte[] signingKey) {
		final String[] sortedHeaderNames = sortedHeaderNames();
		final String signature = buildSignature(signingKey, sortedHeaderNames);
		final StringBuilder buf = new StringBuilder(schemeName());
		buf.append(' ').append(AUTHORIZATION_COMPONENT_CREDENTIAL).append("=").append(identifier);
		buf.append(",").append(AUTHORIZATION_COMPONENT_HEADERS).append("=")
				.append(semiColonDelimitedList(sortedHeaderNames));
		buf.append(",").append(AUTHORIZATION_COMPONENT_SIGNATURE).append("=").append(signature);
		return buf.toString();
	}

	/**
	 * Compute a signature value from the configured properties on the builder,
	 * using a signing key created from a previous call to
	 * {@link #saveSigningKey(String)} or {@link #signingKey(byte[])}.
	 * 
	 * <p>
	 * <b>Note</b> this method returns just the signature value, not a complete
	 * {@literal Authorization} header value. Use the {@link #build()} method to
	 * generate a complete header value.
	 * </p>
	 * 
	 * <p>
	 * The signature is computed using the following algorithm:
	 * </p>
	 * 
	 * <pre>
	 * <code>
	 * Hex(HmacSha256(signingKey,signatureData))
	 * </code>
	 * </pre>
	 * 
	 * <p>
	 * Where {@code signingKey} is the signing key set via
	 * {@link #signingKey(byte[])} or computed via
	 * {@link #saveSigningKey(String)}, and {@code signatureData} is the
	 * computed signature data via
	 * {@link #computeSignatureData(Instant, String)}.
	 * </p>
	 * 
	 * @return the signature value
	 * @throws SecurityException
	 *         if any error occurs computing the header value
	 */
	public String buildSignature() {
		return buildSignature(signingKey, sortedHeaderNames());
	}

	/**
	 * Compute a signature value from the configured properties on the builder,
	 * using the provided secret.
	 * 
	 * <p>
	 * <b>Note</b> this method returns just the signature value, not a complete
	 * {@literal Authorization} header value. Use the {@link #build(String)}
	 * method to generate a complete header value.
	 * </p>
	 * 
	 * @param secret
	 *        the secret to sign the digest with; will use the configured date
	 *        to compute the singing key
	 * @return the signature value
	 * @throws SecurityException
	 *         if any error occurs computing the header value
	 * @see #buildSignature()
	 */
	public String buildSignature(final String secret) {
		final byte[] signingKey = computeSigningKey(date, secret);
		return buildSignature(signingKey, sortedHeaderNames());
	}

	private String buildSignature(byte[] signingKey, String[] sortedHeaderNames) {
		final String signatureData = computeSignatureData(date,
				computeCanonicalRequestMessage(sortedHeaderNames));
		return computeHmacSha256Hex(signingKey, signatureData);
	}

}

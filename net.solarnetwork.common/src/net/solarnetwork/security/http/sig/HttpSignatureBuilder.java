/* ==================================================================
 * HttpSignatureBuilder.java - 19/09/2026 10:52:40 am
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

package net.solarnetwork.security.http.sig;

import static net.solarnetwork.util.ObjectUtils.requireNonNullArgument;
import java.io.InputStream;
import java.net.URI;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * Builder for RFC 9421 message signatures on a request.
 *
 * <p>
 * This is the signing counterpart to the verification path, in the spirit of
 * {@code Snws2AuthorizationBuilder}: configure the request, then read the
 * {@code Signature-Input} and {@code Signature} field values from it. The
 * intermediate {@link SignatureBase} is deliberately exposed, since it is the
 * value both ends must agree on and therefore the first thing to compare when
 * they do not.
 * </p>
 *
 * <p>
 * This class is <b>not</b> thread safe and should not be used concurrently.
 * </p>
 *
 * @author matt
 * @version 1.1
 */
public final class HttpSignatureBuilder implements SignatureContext {

	/** The default signature label. */
	public static final String DEFAULT_LABEL = "sig1";

	private final FieldCanonicalizer canonicalizer = new FieldCanonicalizer();
	private final Map<String, List<String>> headers = new LinkedHashMap<>(8);
	private final List<SignatureComponent> components = new ArrayList<>(8);

	private String label = DEFAULT_LABEL;
	private String method = "GET";
	private URI uri = URI.create("https://data.solarnetwork.net/");
	private Instant created = Instant.now().truncatedTo(ChronoUnit.SECONDS);
	private @Nullable Instant expires;
	private @Nullable String nonce;
	private @Nullable HttpSignatureAlgorithm algorithm;
	private @Nullable String tag;
	private final String tokenId;
	private boolean derivedSigningKey = true;

	/**
	 * Constructor.
	 *
	 * @param tokenId
	 *        the security token ID
	 * @throws IllegalArgumentException
	 *         if {@code tokenId} is {@code null}
	 */
	public HttpSignatureBuilder(String tokenId) {
		super();
		this.tokenId = requireNonNullArgument(tokenId, "tokenId");
	}

	/**
	 * Set the signature label.
	 *
	 * @param label
	 *        the label to use
	 * @return this builder
	 */
	public HttpSignatureBuilder label(String label) {
		this.label = requireNonNullArgument(label, "label");
		return this;
	}

	/**
	 * Set the request method.
	 *
	 * @param method
	 *        the method
	 * @return this builder
	 */
	public HttpSignatureBuilder method(String method) {
		this.method = requireNonNullArgument(method, "method");
		return this;
	}

	/**
	 * Set the request URI.
	 *
	 * @param uri
	 *        the absolute request URI
	 * @return this builder
	 */
	public HttpSignatureBuilder uri(URI uri) {
		this.uri = requireNonNullArgument(uri, "uri");
		return this;
	}

	/**
	 * Set the request URI.
	 *
	 * @param uri
	 *        the absolute request URI
	 * @return this builder
	 */
	public HttpSignatureBuilder uri(String uri) {
		return uri(URI.create(requireNonNullArgument(uri, "uri")));
	}

	/**
	 * Set an HTTP field value, replacing any existing value.
	 *
	 * @param name
	 *        the field name, which will be lower-cased
	 * @param values
	 *        the field values, one per instance of the field in the message
	 * @return this builder
	 */
	public HttpSignatureBuilder header(String name, String... values) {
		headers.put(requireNonNullArgument(name, "name").toLowerCase(Locale.ROOT),
				Arrays.asList(requireNonNullArgument(values, "values")));
		return this;
	}

	/**
	 * Add a {@code Content-Digest} field for the given content.
	 *
	 * @param content
	 *        the message content
	 * @return this builder
	 */
	public HttpSignatureBuilder contentDigest(byte[] content) {
		return header(ContentDigest.CONTENT_DIGEST_HEADER,
				ContentDigest.fieldValue(ContentDigest.SHA_256, content));
	}

	/**
	 * Add a {@code Content-Digest} field for the given content.
	 *
	 * @param content
	 *        the message content; the stream will not be closed by this method
	 * @return this builder
	 */
	public HttpSignatureBuilder contentDigest(InputStream content) {
		return header(ContentDigest.CONTENT_DIGEST_HEADER,
				ContentDigest.fieldValue(ContentDigest.SHA_256, content));
	}

	/**
	 * Add covered components, in signing order.
	 *
	 * @param names
	 *        the component names, without parameters
	 * @return this builder
	 */
	public HttpSignatureBuilder covered(String... names) {
		for ( String name : requireNonNullArgument(names, "names") ) {
			components.add(SignatureComponent.of(name));
		}
		return this;
	}

	/**
	 * Add a covered component, in signing order.
	 *
	 * @param component
	 *        the component
	 * @return this builder
	 */
	public HttpSignatureBuilder covered(SignatureComponent component) {
		components.add(requireNonNullArgument(component, "component"));
		return this;
	}

	/**
	 * Set the signature creation time.
	 *
	 * @param created
	 *        the creation time; will be truncated to second resolution
	 * @return this builder
	 */
	public HttpSignatureBuilder created(Instant created) {
		this.created = requireNonNullArgument(created, "created").truncatedTo(ChronoUnit.SECONDS);
		return this;
	}

	/**
	 * Set the signature expiration time.
	 *
	 * @param expires
	 *        the expiration time, or {@code null} for none; will be truncated
	 *        to second resolution
	 * @return this builder
	 */
	public HttpSignatureBuilder expires(@Nullable Instant expires) {
		this.expires = (expires != null ? expires.truncatedTo(ChronoUnit.SECONDS) : null);
		return this;
	}

	/**
	 * Set the signature nonce.
	 *
	 * @param nonce
	 *        the nonce, or {@code null} for none
	 * @return this builder
	 */
	public HttpSignatureBuilder nonce(@Nullable String nonce) {
		this.nonce = nonce;
		return this;
	}

	/**
	 * Set the signature algorithm.
	 *
	 * <p>
	 * When set, an {@code alg} signature parameter is included. The parameter
	 * is optional in RFC 9421, and is omitted by default.
	 * </p>
	 *
	 * @param algorithm
	 *        the algorithm, or {@code null} to omit the parameter
	 * @return this builder
	 */
	public HttpSignatureBuilder algorithm(@Nullable HttpSignatureAlgorithm algorithm) {
		this.algorithm = algorithm;
		return this;
	}

	/**
	 * Set the application tag.
	 *
	 * @param tag
	 *        the tag, or {@code null} for none
	 * @return this builder
	 */
	public HttpSignatureBuilder tag(@Nullable String tag) {
		this.tag = tag;
		return this;
	}

	/**
	 * Use a derived signing key, valid for the UTC date of the signature
	 * creation time.
	 *
	 * <p>
	 * This is enabled by default, so that a token secret need not be handed to
	 * whatever signs the request. Disable it to sign with the token secret
	 * itself, which is what an RFC 9421 implementation with no knowledge of
	 * SolarNetwork key derivation can do.
	 * </p>
	 *
	 * @param derived
	 *        {@code true} to derive a signing key from the token secret,
	 *        {@code false} to use the token secret directly
	 * @return this builder
	 */
	public HttpSignatureBuilder derivedSigningKey(boolean derived) {
		this.derivedSigningKey = derived;
		return this;
	}

	/**
	 * Get the key deriver for the configured token and signing key mode.
	 *
	 * <p>
	 * The signing date of a derived key comes from the signature creation time,
	 * and is resolved here rather than when the mode is configured, so that the
	 * two can be set in either order.
	 * </p>
	 *
	 * @return the deriver, never {@code null}
	 */
	private TokenSecretKeyDeriver keyDeriver() {
		return TokenSecretKeyDeriver.forToken(tokenId,
				derivedSigningKey ? created.atZone(ZoneOffset.UTC).toLocalDate() : null);
	}

	/**
	 * Get the {@code keyid} signature parameter value.
	 *
	 * @return the key ID, never {@code null}
	 */
	public String keyId() {
		return keyDeriver().keyId();
	}

	/**
	 * Compute the signing key for a token secret.
	 *
	 * @param tokenSecret
	 *        the token secret
	 * @return the signing key, never {@code null}
	 */
	public byte[] computeSigningKey(String tokenSecret) {
		return keyDeriver().hmacKey(requireNonNullArgument(tokenSecret, "tokenSecret"));
	}

	/**
	 * Get the signature parameters.
	 *
	 * @return the parameters, never {@code null}
	 */
	public SignatureParameters parameters() {
		final Map<String, @Nullable Object> params = new LinkedHashMap<>(6);
		params.put(SignatureParameters.PARAM_CREATED, created);
		params.put(SignatureParameters.PARAM_EXPIRES, expires);
		params.put(SignatureParameters.PARAM_NONCE, nonce);
		final HttpSignatureAlgorithm alg = algorithm;
		params.put(SignatureParameters.PARAM_ALG, alg != null ? alg.getAlgorithmName() : null);
		params.put(SignatureParameters.PARAM_KEYID, keyDeriver().keyId());
		params.put(SignatureParameters.PARAM_TAG, tag);
		return SignatureParameters.of(components, params);
	}

	/**
	 * Compute the signature base.
	 *
	 * @return the signature base, never {@code null}
	 */
	public SignatureBase signatureBase() {
		return SignatureBase.compute(this, parameters(), canonicalizer);
	}

	/**
	 * Compute the signature value, using a token secret.
	 *
	 * @param tokenSecret
	 *        the token secret
	 * @return the signature bytes, never {@code null}
	 */
	public byte[] sign(String tokenSecret) {
		return signWithKey(computeSigningKey(tokenSecret));
	}

	/**
	 * Compute the signature value, using a signing key.
	 *
	 * @param signingKey
	 *        the signing key
	 * @return the signature bytes, never {@code null}
	 */
	public byte[] signWithKey(byte[] signingKey) {
		final HttpSignatureAlgorithm alg = (algorithm != null ? algorithm
				: HttpSignatureAlgorithm.HmacSha256);
		return HttpSignatureVerifier.sign(alg, signingKey, signatureBase());
	}

	/**
	 * Get the {@code Signature-Input} HTTP field value.
	 *
	 * @return the field value, never {@code null}
	 */
	public String signatureInputHeaderValue() {
		return HttpSignatureFields.signatureInputValue(label, parameters());
	}

	/**
	 * Get the {@code Signature} HTTP field value, using a token secret.
	 *
	 * @param tokenSecret
	 *        the token secret
	 * @return the field value, never {@code null}
	 */
	public String signatureHeaderValue(String tokenSecret) {
		return HttpSignatureFields.signatureValue(label, sign(tokenSecret));
	}

	@Override
	public String method() {
		return method;
	}

	@Override
	public String targetUri() {
		final String q = uri.getRawQuery();
		return scheme() + "://" + authority() + path() + (q != null ? "?" + q : "");
	}

	@Override
	public String authority() {
		final String host = requireNonNullArgument(uri.getHost(), "host").toLowerCase(Locale.ROOT);
		final int port = uri.getPort();
		if ( port < 0 || isDefaultPort(scheme(), port) ) {
			return host;
		}
		return host + ":" + port;
	}

	private static boolean isDefaultPort(String scheme, int port) {
		return ("https".equals(scheme) && port == 443) || ("http".equals(scheme) && port == 80);
	}

	@Override
	public String scheme() {
		final String s = uri.getScheme();
		return (s != null ? s.toLowerCase(Locale.ROOT) : "https");
	}

	@Override
	public String requestTarget() {
		final String q = uri.getRawQuery();
		return path() + (q != null ? "?" + q : "");
	}

	@Override
	public String path() {
		final String p = uri.getRawPath();
		return (p == null || p.isEmpty() ? "/" : p);
	}

	@Override
	public String query() {
		final String q = uri.getRawQuery();
		return (q != null ? "?" + q : "?");
	}

	@Override
	public List<String> queryParamValues(String name) {
		return QueryParams.values(uri.getRawQuery(), name);
	}

	@Override
	public List<String> fieldValues(String name) {
		final List<String> values = headers.get(name);
		return (values != null ? values : List.of());
	}

}

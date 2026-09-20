/* ==================================================================
 * AuthenticationDataHttpSignature.java - 19/09/2026 1:12:55 pm
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

package net.solarnetwork.web.jakarta.security;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import net.solarnetwork.security.http.sig.ContentDigest;
import net.solarnetwork.security.http.sig.FieldCanonicalizer;
import net.solarnetwork.security.http.sig.HttpSignatureAlgorithm;
import net.solarnetwork.security.http.sig.HttpSignatureException;
import net.solarnetwork.security.http.sig.HttpSignatureFields;
import net.solarnetwork.security.http.sig.HttpSignatureVerifier;
import net.solarnetwork.security.http.sig.SignatureBase;
import net.solarnetwork.security.http.sig.SignatureComponent;
import net.solarnetwork.security.http.sig.SignatureParameters;
import net.solarnetwork.security.http.sig.TokenSecretKeyDeriver;

/**
 * HTTP Message Signatures authentication, as defined in RFC 9421.
 *
 * <p>
 * RFC 9421 lets a signer cover as much or as little of a request as it likes,
 * which on its own would be weaker than the SNWS2 scheme. This class therefore
 * enforces a coverage floor equivalent to the one SNWS2 builds into its
 * canonical request: the method, authority, path and query must be covered, as
 * must the content digest of any request body, the content type, and every
 * SolarNetwork {@literal X-SN-*} header the request carries.
 * </p>
 *
 * @author matt
 * @version 1.0
 * @since 4.53
 */
public class AuthenticationDataHttpSignature extends AuthenticationData {

	private static final Logger log = LoggerFactory.getLogger(AuthenticationDataHttpSignature.class);

	private static final FieldCanonicalizer CANONICALIZER = new FieldCanonicalizer();

	private static final byte[] EMPTY_SHA256 = emptySha256();

	private static byte[] emptySha256() {
		try {
			return MessageDigest.getInstance("SHA-256").digest(new byte[0]);
		} catch ( NoSuchAlgorithmException e ) {
			throw new IllegalStateException("The SHA-256 digest algorithm is not available.", e);
		}
	}

	private final String label;
	private final SignatureParameters parameters;
	private final SignatureBase signatureBase;
	private final HttpSignatureAlgorithm algorithm;
	private final TokenSecretKeyDeriver keyDeriver;
	private final byte[] signature;

	/**
	 * Constructor.
	 *
	 * @param request
	 *        the HTTP request
	 * @param settings
	 *        the profile settings
	 * @param explicitHost
	 *        a fixed value to use instead of the {@literal Host} HTTP header
	 *        value, or {@code null} to derive the value from the request
	 * @throws IOException
	 *         if any IO error occurs
	 * @throws BadCredentialsException
	 *         if the signature is malformed or does not satisfy the profile
	 */
	public AuthenticationDataHttpSignature(SecurityHttpServletRequestWrapper request,
			HttpSignatureSettings settings, @Nullable String explicitHost) throws IOException {
		this(request, settings, explicitHost, selectSignature(request, settings));
	}

	private AuthenticationDataHttpSignature(SecurityHttpServletRequestWrapper request,
			HttpSignatureSettings settings, @Nullable String explicitHost, SelectedSignature selected)
			throws IOException {
		super(AuthenticationScheme.HttpSignature, selected.created());
		this.label = selected.label();
		this.parameters = selected.parameters();

		this.algorithm = resolveAlgorithm(parameters, settings);
		this.keyDeriver = resolveKeyDeriver(parameters, settings, selected.created());

		validateExpiry(parameters);
		validateCoverage(request, parameters);
		validateContentDigestField(request);
		// also validate a legacy Digest or Content-MD5 header, if the client sent one
		validateContentDigest(request);

		this.signature = selected.signature();
		try {
			this.signatureBase = SignatureBase.compute(
					new HttpServletSignatureContext(request, explicitHost), parameters, CANONICALIZER);
		} catch ( HttpSignatureException e ) {
			throw new BadCredentialsException(e.getMessage(), e);
		}

		log.debug("Signature [{}] base:\n{}", label, signatureBase.value());
	}

	/**
	 * A private carrier, so copying the signature bytes would serve no purpose.
	 */
	@SuppressWarnings("ArrayRecordComponent")
	private record SelectedSignature(String label, SignatureParameters parameters, byte[] signature,
			Instant created) {
	}

	private static SelectedSignature selectSignature(SecurityHttpServletRequestWrapper request,
			HttpSignatureSettings settings) {
		final Map<String, SignatureParameters> inputs;
		final Map<String, byte[]> signatures;
		try {
			inputs = HttpSignatureFields.parseSignatureInput(
					headerValues(request, HttpSignatureFields.SIGNATURE_INPUT_HEADER));
			signatures = HttpSignatureFields
					.parseSignature(headerValues(request, HttpSignatureFields.SIGNATURE_HEADER));
		} catch ( HttpSignatureException e ) {
			throw new BadCredentialsException(e.getMessage(), e);
		}

		final String wantedTag = settings.getTag();
		final List<String> tagged = new ArrayList<>(2);
		for ( Map.Entry<String, SignatureParameters> e : inputs.entrySet() ) {
			if ( wantedTag.equals(e.getValue().tag()) ) {
				tagged.add(e.getKey());
			}
		}

		final String label;
		if ( tagged.size() == 1 ) {
			label = tagged.get(0);
		} else if ( tagged.size() > 1 ) {
			// RFC 9421 7.2.6: an ambiguous choice between signatures is not ours to guess at
			throw new BadCredentialsException("More than one signature is tagged [" + wantedTag
					+ "]; exactly one signature may claim the tag.");
		} else if ( settings.isRequireTag() ) {
			throw new BadCredentialsException(
					"No signature is tagged [" + wantedTag + "], which is required.");
		} else if ( inputs.size() == 1 ) {
			label = inputs.keySet().iterator().next();
		} else {
			throw new BadCredentialsException(
					"The request carries " + inputs.size() + " signatures, none tagged [" + wantedTag
							+ "]; tag the one to authenticate" + " with.");
		}

		final SignatureParameters parameters = inputs.get(label);
		final byte[] signature = signatures.get(label);
		if ( parameters == null || signature == null ) {
			throw new BadCredentialsException("The signature labelled [" + label
					+ "] is missing from the " + HttpSignatureFields.SIGNATURE_HEADER + " HTTP field.");
		}

		final Instant created;
		try {
			created = parameters.created();
		} catch ( HttpSignatureException e ) {
			throw new BadCredentialsException(e.getMessage(), e);
		}
		if ( created == null ) {
			throw new BadCredentialsException(
					"The signature labelled [" + label + "] has no 'created' parameter.");
		}
		return new SelectedSignature(label, parameters, signature, created);
	}

	private static List<String> headerValues(SecurityHttpServletRequestWrapper request, String name) {
		final Enumeration<String> values = request.getHeaders(name);
		if ( values == null ) {
			return List.of();
		}
		final List<String> result = new ArrayList<>(2);
		while ( values.hasMoreElements() ) {
			result.add(values.nextElement());
		}
		return result;
	}

	private static HttpSignatureAlgorithm resolveAlgorithm(SignatureParameters parameters,
			HttpSignatureSettings settings) {
		final String name;
		try {
			name = parameters.alg();
		} catch ( HttpSignatureException e ) {
			throw new BadCredentialsException(e.getMessage(), e);
		}
		if ( name == null ) {
			// alg is optional in RFC 9421: the key material implies the algorithm, and a
			// SolarNetwork token secret is a shared secret
			return HttpSignatureAlgorithm.HmacSha256;
		}
		final HttpSignatureAlgorithm alg = HttpSignatureAlgorithm.forAlgorithmName(name);
		if ( alg == null || !settings.getAlgorithms().contains(alg) ) {
			throw new BadCredentialsException(
					"The signature algorithm [" + name + "] is not supported.");
		}
		return alg;
	}

	private static TokenSecretKeyDeriver resolveKeyDeriver(SignatureParameters parameters,
			HttpSignatureSettings settings, Instant created) {
		final String keyId;
		try {
			keyId = parameters.keyid();
		} catch ( HttpSignatureException e ) {
			throw new BadCredentialsException(e.getMessage(), e);
		}
		if ( keyId == null ) {
			throw new BadCredentialsException("The signature has no 'keyid' parameter.");
		}
		final TokenSecretKeyDeriver deriver;
		try {
			deriver = TokenSecretKeyDeriver.forKeyId(keyId);
		} catch ( HttpSignatureException e ) {
			throw new BadCredentialsException(e.getMessage(), e);
		}
		final LocalDate signingDate = deriver.signingDate();
		if ( signingDate == null ) {
			if ( !settings.isAllowDirectSecretKey() ) {
				throw new BadCredentialsException(
						"A signing key derived from a date is required; the 'keyid' parameter must"
								+ " be of the form 'tokenId:YYYYMMDD'.");
			}
		} else {
			if ( !settings.isAllowDerivedSigningKey() ) {
				throw new BadCredentialsException(
						"A signing key derived from a date is not accepted; the 'keyid' parameter"
								+ " must be the token ID alone.");
			}
			final LocalDate today = created.atZone(ZoneOffset.UTC).toLocalDate();
			final long age = ChronoUnit.DAYS.between(signingDate, today);
			if ( age < -1 || age > settings.getDerivedSigningKeyMaxDays() - 1L ) {
				throw new BadCredentialsException("The signing key date [" + signingDate
						+ "] is not valid for a signature created on [" + today + "].");
			}
		}
		return deriver;
	}

	private static void validateExpiry(SignatureParameters parameters) {
		final Instant expires;
		try {
			expires = parameters.expires();
		} catch ( HttpSignatureException e ) {
			throw new BadCredentialsException(e.getMessage(), e);
		}
		if ( expires != null && expires.isBefore(Instant.now()) ) {
			throw new BadCredentialsException("The signature expired at " + expires + ".");
		}
	}

	private static void validateCoverage(SecurityHttpServletRequestWrapper request,
			SignatureParameters parameters) throws IOException {
		final Set<String> covered = new LinkedHashSet<>(parameters.components().size());
		for ( SignatureComponent component : parameters.components() ) {
			covered.add(component.name());
		}

		final boolean hasTargetUri = covered.contains(SignatureComponent.TARGET_URI);

		requireCovered(covered, SignatureComponent.METHOD);
		if ( !hasTargetUri ) {
			// @target-uri covers the authority, path and query in one component
			requireCovered(covered, SignatureComponent.AUTHORITY);
			requireCovered(covered, SignatureComponent.PATH);
			final String query = request.getQueryString();
			if ( query != null && !query.isEmpty() ) {
				requireCovered(covered, SignatureComponent.QUERY);
			}
		}

		if ( hasContent(request) ) {
			requireCovered(covered, ContentDigest.CONTENT_DIGEST_HEADER.toLowerCase(Locale.ROOT));
		}

		final Enumeration<String> headerNames = request.getHeaderNames();
		final String snPrefix = WebConstants.HEADER_PREFIX.toLowerCase(Locale.ROOT);
		while ( headerNames != null && headerNames.hasMoreElements() ) {
			final String headerName = headerNames.nextElement().toLowerCase(Locale.ROOT);
			// every X-SN-* header, and the content type, carries request meaning, so a
			// signature that leaves them uncovered is not covering the whole request
			if ( headerName.startsWith(snPrefix) || headerName.equals("content-type") ) {
				requireCovered(covered, headerName);
			}
		}
	}

	private static void requireCovered(Set<String> covered, String name) {
		if ( !covered.contains(name) ) {
			throw new BadCredentialsException(
					"The [" + name + "] component must be covered by the signature.");
		}
	}

	private static boolean hasContent(SecurityHttpServletRequestWrapper request) throws IOException {
		// a SecurityException here means the content is over the configured limit, which
		// the filter reports as an upload size problem rather than a credentials one, so
		// let it propagate the way the SNWS2 scheme does
		return !MessageDigest.isEqual(request.getContentSHA256(), EMPTY_SHA256);
	}

	private static void validateContentDigestField(SecurityHttpServletRequestWrapper request)
			throws IOException {
		final List<String> values = headerValues(request, ContentDigest.CONTENT_DIGEST_HEADER);
		if ( values.isEmpty() ) {
			return;
		}
		final Map<String, byte[]> digests;
		try {
			digests = ContentDigest.parse(values);
		} catch ( HttpSignatureException e ) {
			throw new BadCredentialsException(e.getMessage(), e);
		}
		boolean validated = false;
		for ( Map.Entry<String, byte[]> e : digests.entrySet() ) {
			final byte[] computed = switch (e.getKey()) {
				case ContentDigest.SHA_256 -> request.getContentSHA256();
				case ContentDigest.SHA_512 -> request.getContentSHA512();
				default -> null;
			};
			if ( computed == null ) {
				continue;
			}
			if ( !MessageDigest.isEqual(computed, e.getValue()) ) {
				throw new BadCredentialsException("The " + ContentDigest.CONTENT_DIGEST_HEADER + " ["
						+ e.getKey() + "] value does not match the request content.");
			}
			validated = true;
		}
		if ( !validated ) {
			throw new BadCredentialsException("The " + ContentDigest.CONTENT_DIGEST_HEADER
					+ " HTTP field does not use a supported digest algorithm.");
		}
	}

	@Override
	public boolean verifySignature(String secretKey) {
		return HttpSignatureVerifier.verify(algorithm, keyDeriver.hmacKey(secretKey), signatureBase,
				signature);
	}

	@Override
	public String getAuthTokenId() {
		return keyDeriver.tokenId();
	}

	@Override
	public String getSignatureDigest() {
		return Base64.getEncoder().encodeToString(signature);
	}

	@Override
	public String computeSignatureDigest(String secretKey) {
		return Base64.getEncoder().encodeToString(
				HttpSignatureVerifier.sign(algorithm, keyDeriver.hmacKey(secretKey), signatureBase));
	}

	@Override
	public String getSignatureData() {
		return signatureBase.value();
	}

	/**
	 * Get the signature label.
	 *
	 * @return the label of the signature used for authentication
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Get the signature parameters.
	 *
	 * @return the parameters, never {@code null}
	 */
	public SignatureParameters getParameters() {
		return parameters;
	}

	/**
	 * Get the signature algorithm.
	 *
	 * @return the algorithm, never {@code null}
	 */
	public HttpSignatureAlgorithm getAlgorithm() {
		return algorithm;
	}

}

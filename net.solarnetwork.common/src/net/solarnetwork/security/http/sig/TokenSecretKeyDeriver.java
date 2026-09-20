/* ==================================================================
 * TokenSecretKeyDeriver.java - 19/09/2026 10:21:38 am
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

import static java.nio.charset.StandardCharsets.UTF_8;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import org.jspecify.annotations.Nullable;
import net.solarnetwork.security.AuthorizationUtils;

/**
 * Derives the signing key for a SolarNetwork security token from the
 * {@code keyid} signature parameter.
 *
 * <p>
 * Two forms are supported, so that a client can choose between maximum
 * interoperability and the key isolation the SNWS2 scheme provides:
 * </p>
 *
 * <dl>
 * <dt>{@code <tokenId>}</dt>
 * <dd>The signing key is the UTF-8 encoded token secret itself. Any RFC 9421
 * implementation can sign this way, using only the token ID and secret.</dd>
 *
 * <dt>{@code <tokenId>:<YYYYMMDD>}</dt>
 * <dd>The signing key is derived from the token secret and the given UTC date,
 * as {@code HMAC(HMAC("SNWS3"+secret, "YYYYMMDD"), "snws3_request")}. This
 * mirrors the SNWS2 signing key, and has the same benefit: the derived key is
 * only usable for a limited number of days, so it can be given to a signer
 * without disclosing the token secret.</dd>
 * </dl>
 *
 * @author matt
 * @version 1.0
 */
public final class TokenSecretKeyDeriver {

	/** The scheme literal used when deriving a signing key. */
	public static final String SIGNING_KEY_SCHEME = "SNWS3";

	/** The message used to sign the derived signing key. */
	public static final String SIGNING_KEY_MESSAGE = "snws3_request";

	/** The delimiter between the token ID and the signing date in a key ID. */
	public static final char KEY_ID_DELIMITER = ':';

	private static final DateTimeFormatter SIGNING_DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

	private final String tokenId;
	private final @Nullable LocalDate signingDate;
	private final @Nullable String signingDateValue;

	private TokenSecretKeyDeriver(String tokenId, @Nullable LocalDate signingDate,
			@Nullable String signingDateValue) {
		this.tokenId = tokenId;
		this.signingDate = signingDate;
		this.signingDateValue = signingDateValue;
	}

	/**
	 * Parse a key ID signature parameter value.
	 *
	 * @param keyId
	 *        the key ID, from the RFC 9421 {@literal keyid} signature parameter
	 * @return the deriver
	 * @throws HttpSignatureException
	 *         if the key ID is empty, or the signing date is not a valid
	 *         {@code YYYYMMDD} date
	 */
	public static TokenSecretKeyDeriver forKeyId(String keyId) {
		final int idx = keyId.indexOf(KEY_ID_DELIMITER);
		if ( idx < 0 ) {
			if ( keyId.isEmpty() ) {
				throw new HttpSignatureException("The 'keyid' signature parameter is empty.");
			}
			return new TokenSecretKeyDeriver(keyId, null, null);
		}
		final String tokenId = keyId.substring(0, idx);
		if ( tokenId.isEmpty() ) {
			throw new HttpSignatureException(
					"The 'keyid' signature parameter does not start with a token ID.");
		}
		final String dateValue = keyId.substring(idx + 1);
		final LocalDate date;
		try {
			date = LocalDate.parse(dateValue, SIGNING_DATE_FORMATTER);
		} catch ( DateTimeParseException e ) {
			throw new HttpSignatureException("The 'keyid' signature parameter signing date ["
					+ dateValue + "] is not a valid YYYYMMDD date.", e);
		}
		return new TokenSecretKeyDeriver(tokenId, date, dateValue);
	}

	/**
	 * Create a deriver for a token and an optional signing date.
	 *
	 * @param tokenId
	 *        the token ID
	 * @param signingDate
	 *        the signing date, or {@code null} to use the token secret directly
	 * @return the deriver
	 */
	public static TokenSecretKeyDeriver forToken(String tokenId, @Nullable LocalDate signingDate) {
		return new TokenSecretKeyDeriver(tokenId, signingDate,
				signingDate != null ? SIGNING_DATE_FORMATTER.format(signingDate) : null);
	}

	/**
	 * Get the token ID.
	 *
	 * @return the token ID, never {@code null}
	 */
	public String tokenId() {
		return tokenId;
	}

	/**
	 * Get the signing date.
	 *
	 * @return the UTC signing date, or {@code null} if the token secret is used
	 *         directly
	 */
	public @Nullable LocalDate signingDate() {
		return signingDate;
	}

	/**
	 * Test if a derived signing key is used.
	 *
	 * @return {@code true} if a signing date is configured
	 */
	public boolean isDerived() {
		return signingDate != null;
	}

	/**
	 * Get the {@code keyid} signature parameter value this deriver represents.
	 *
	 * @return the key ID, never {@code null}
	 */
	public String keyId() {
		final String d = signingDateValue;
		return (d != null ? tokenId + KEY_ID_DELIMITER + d : tokenId);
	}

	/**
	 * Compute the HMAC signing key for a token secret.
	 *
	 * @param tokenSecret
	 *        the token secret
	 * @return the signing key, never {@code null}
	 */
	public byte[] hmacKey(String tokenSecret) {
		final String d = signingDateValue;
		if ( d == null ) {
			return tokenSecret.getBytes(UTF_8);
		}
		return AuthorizationUtils.computeHmacSha256(
				AuthorizationUtils.computeHmacSha256(SIGNING_KEY_SCHEME + tokenSecret, d),
				SIGNING_KEY_MESSAGE);
	}

	@Override
	public String toString() {
		return keyId();
	}

}

/* ==================================================================
 * HttpSignatureVerifier.java - 19/09/2026 10:30:12 am
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

import java.security.MessageDigest;
import net.solarnetwork.security.AuthorizationUtils;

/**
 * Creates and verifies message signature values, per RFC 9421 section 3.3.
 *
 * @author matt
 * @version 1.0
 */
public final class HttpSignatureVerifier {

	private HttpSignatureVerifier() {
		// not available
	}

	/**
	 * Sign a signature base.
	 *
	 * @param algorithm
	 *        the signature algorithm
	 * @param key
	 *        the signing key
	 * @param base
	 *        the signature base
	 * @return the signature value
	 * @throws HttpSignatureException
	 *         if the signature cannot be computed
	 */
	public static byte[] sign(HttpSignatureAlgorithm algorithm, byte[] key, SignatureBase base) {
		try {
			return AuthorizationUtils.computeMacDigest(key, base.bytes(),
					algorithm.getMacAlgorithm());
		} catch ( net.solarnetwork.security.SecurityException e ) {
			throw new HttpSignatureException(
					"Error computing " + algorithm.getAlgorithmName() + " signature.", e);
		}
	}

	/**
	 * Verify a signature value against a signature base.
	 *
	 * @param algorithm
	 *        the signature algorithm
	 * @param key
	 *        the verification key
	 * @param base
	 *        the signature base
	 * @param signature
	 *        the signature value presented in the message
	 * @return {@code true} if the signature is valid
	 * @throws HttpSignatureException
	 *         if the signature cannot be computed
	 */
	public static boolean verify(HttpSignatureAlgorithm algorithm, byte[] key, SignatureBase base,
			byte[] signature) {
		// compare in constant time to avoid leaking timing information
		return MessageDigest.isEqual(sign(algorithm, key, base), signature);
	}

}

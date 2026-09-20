/* ==================================================================
 * HttpSignatureAlgorithm.java - 19/09/2026 9:05:12 am
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

import org.jspecify.annotations.Nullable;

/**
 * Signature algorithms from the RFC 9421 <i>HTTP Signature Algorithms</i>
 * registry.
 *
 * <p>
 * Only the algorithms SolarNetwork supports are enumerated here. The registry
 * defines others, which this implementation rejects rather than silently
 * ignoring, so that an unsupported {@code alg} signature parameter produces a
 * clear error.
 * </p>
 *
 * @author matt
 * @version 1.0
 */
public enum HttpSignatureAlgorithm {

	/** HMAC using SHA-256, per RFC 9421 section 3.3.3. */
	HmacSha256("hmac-sha256", "HmacSHA256"),

	;

	private final String algorithmName;
	private final String macAlgorithm;

	private HttpSignatureAlgorithm(String algorithmName, String macAlgorithm) {
		this.algorithmName = algorithmName;
		this.macAlgorithm = macAlgorithm;
	}

	/**
	 * Get the registry name of the algorithm.
	 *
	 * <p>
	 * This is the value used for the {@code alg} signature parameter.
	 * </p>
	 *
	 * @return the algorithm name, never {@code null}
	 */
	public String getAlgorithmName() {
		return algorithmName;
	}

	/**
	 * Get the JCA MAC algorithm name.
	 *
	 * @return the JCA algorithm name, never {@code null}
	 */
	public String getMacAlgorithm() {
		return macAlgorithm;
	}

	/**
	 * Test if this algorithm uses a shared secret, rather than a key pair.
	 *
	 * @return {@code true} if the algorithm is symmetric
	 */
	public boolean isSymmetric() {
		return true;
	}

	/**
	 * Get an enum instance for a registry algorithm name.
	 *
	 * @param name
	 *        the algorithm name to look for
	 * @return the matching enum, or {@code null} if {@code name} is
	 *         {@code null} or does not match a supported algorithm
	 */
	public static @Nullable HttpSignatureAlgorithm forAlgorithmName(@Nullable String name) {
		if ( name != null ) {
			for ( HttpSignatureAlgorithm alg : values() ) {
				if ( alg.algorithmName.equals(name) ) {
					return alg;
				}
			}
		}
		return null;
	}

}

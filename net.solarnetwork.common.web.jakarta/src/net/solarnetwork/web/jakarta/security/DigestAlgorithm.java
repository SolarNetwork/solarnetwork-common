/* ==================================================================
 * DigestAlgorithm.java - 1/03/2017 7:24:30 PM
 * 
 * Copyright 2007-2017 SolarNetwork.net Dev Team
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

/**
 * Supported algorithms for the HTTP {@code Digest} header.
 * 
 * @author matt
 * @version 1.1
 * @since 1.11
 */
public enum DigestAlgorithm {

	/** The MD5 algorithm. */
	MD5("md5"),

	/** The SHA1 algorithm. */
	SHA1("sha"),

	/** The SHA-256 algorithm. */
	SHA256("sha-256"),

	/**
	 * The SHA-512 algorithm.
	 *
	 * @since 1.1
	 */
	SHA512("sha-512");

	private String algorithmName;

	private DigestAlgorithm(String name) {
		this.algorithmName = name;
	}

	/**
	 * Get the header algorithm name associated with the digest.
	 * 
	 * @return The name.
	 */
	public String getAlgorithmName() {
		return algorithmName;
	}

	/**
	 * Get a {@code DigestAlgorithm} for a given algorithm name.
	 * 
	 * @param name
	 *        The name to get the instance for.
	 * @return The instance.
	 * @throws IllegalArgumentException
	 *         if the name is not supported
	 */
	public static DigestAlgorithm forAlgorithmName(String name) {
		for ( DigestAlgorithm alg : DigestAlgorithm.values() ) {
			if ( alg.getAlgorithmName().equalsIgnoreCase(name) ) {
				return alg;
			}
		}
		throw new IllegalArgumentException("Algorithm [" + name + "] not supported");
	}

}

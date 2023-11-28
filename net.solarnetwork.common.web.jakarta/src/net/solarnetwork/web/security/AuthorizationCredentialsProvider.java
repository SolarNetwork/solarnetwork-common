/* ==================================================================
 * AuthorizationCredentialsProvider.java - 13/08/2019 10:17:34 am
 * 
 * Copyright 2019 SolarNetwork.net Dev Team
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

import java.time.Instant;

/**
 * API for a provider of authorization credentials.
 * 
 * @author matt
 * @version 1.1
 * @since 1.16
 */
public interface AuthorizationCredentialsProvider {

	/**
	 * Get the authorization identifier.
	 * 
	 * @return the authorization identifier
	 */
	String getAuthorizationId();

	/**
	 * Get the authorization secret.
	 * 
	 * @return the authorization secret
	 */
	String getAuthorizationSecret();

	/**
	 * Get a pre-computed signing key.
	 * 
	 * @return the pre-computed signing key, signed using the
	 *         {@link #getAuthorizationSigningDate()} date, or {@literal null}
	 *         if {@link #getAuthorizationSecret()} should be used
	 * @see #getAuthorizationSigningDate()
	 */
	default byte[] getAuthorizationSigningKey() {
		return null;
	}

	/**
	 * Get the pre-computed signing key sign date.
	 * 
	 * @return the date used to sign {@link #getAuthorizationSigningKey()} if
	 *         that is non-{@literal null}
	 */
	default Instant getAuthorizationSigningDate() {
		return null;
	}

}

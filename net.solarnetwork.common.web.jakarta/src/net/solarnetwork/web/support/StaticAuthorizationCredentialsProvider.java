/* ==================================================================
 * StaticAuthorizationCredentialsProvider.java - 13/08/2019 10:27:53 am
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

package net.solarnetwork.web.support;

import java.time.Instant;
import net.solarnetwork.web.security.AuthorizationCredentialsProvider;

/**
 * A simple statically assigned authorization credentials provider.
 * 
 * @author matt
 * @version 1.1
 * @since 1.16
 */
public class StaticAuthorizationCredentialsProvider implements AuthorizationCredentialsProvider {

	private final String authorizationId;
	private final String authorizationSecret;
	private final byte[] signingKey;
	private final Instant signingDate;

	/**
	 * Constructor.
	 * 
	 * @param authorizationId
	 *        the authorization ID
	 * @param authorizationSecret
	 *        the authorization secret
	 */
	public StaticAuthorizationCredentialsProvider(String authorizationId, String authorizationSecret) {
		super();
		this.authorizationId = authorizationId;
		this.authorizationSecret = authorizationSecret;
		this.signingKey = null;
		this.signingDate = null;
	}

	/**
	 * Constructor for a signing key.
	 * 
	 * @param authorizationId
	 *        the authorization ID
	 * @param signingKey
	 *        the authorization signing key
	 * @param signingDate
	 *        the authorization signing date
	 */
	public StaticAuthorizationCredentialsProvider(String authorizationId, byte[] signingKey,
			Instant signingDate) {
		super();
		this.authorizationId = authorizationId;
		this.authorizationSecret = null;
		this.signingKey = signingKey;
		this.signingDate = signingDate;
	}

	@Override
	public String getAuthorizationId() {
		return authorizationId;
	}

	@Override
	public String getAuthorizationSecret() {
		return authorizationSecret;
	}

	@Override
	public byte[] getAuthorizationSigningKey() {
		return signingKey;
	}

	@Override
	public Instant getAuthorizationSigningDate() {
		return signingDate;
	}

}

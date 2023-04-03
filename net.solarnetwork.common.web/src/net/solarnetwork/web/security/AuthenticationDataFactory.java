/* ==================================================================
 * AuthenticationDataFactory.java - 25/04/2017 11:10:41 AM
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

import java.io.IOException;
import org.springframework.security.authentication.BadCredentialsException;

/**
 * Factory for creating {@code AuthenticationData} instances.
 * 
 * @author matt
 * @version 1.1
 * @since 1.11
 */
public final class AuthenticationDataFactory {

	/**
	 * The system property name of an "explicit" HTTP {@literal Host} header
	 * value to use for authentication signature calculation.
	 * 
	 * <p>
	 * This can be useful when the application performing the authentication
	 * validation sits behind a proxy or load balancer and the requested
	 * {@literal Host} value is different than the value used to generate the
	 * authenciation signature.
	 * </p>
	 * 
	 * @since 1.1
	 */
	public static final String EXPLICIT_HOST_PROP = "sn.web.auth.explicitHost";

	private static final String EXPLICIT_HOST = System.getProperty(EXPLICIT_HOST_PROP, null);

	/**
	 * Constructor.
	 */
	public AuthenticationDataFactory() {
		super();
	}

	/**
	 * Obtain a {@link AuthenticationData} instance from a HTTP request.
	 * 
	 * @param request
	 *        The HTTP request.
	 * @return the authentication data, or {@literal null} if no
	 *         {@code Authorization} header provided on the request or the
	 *         authorization scheme is not supported
	 * @throws IOException
	 *         if any IO error occurs
	 * @throws BadCredentialsException
	 *         if the authorization data is malformed in any way
	 */
	public static AuthenticationData authenticationDataForAuthorizationHeader(
			final SecurityHttpServletRequestWrapper request) throws IOException {
		final String header = request.getHeader("Authorization");

		AuthenticationScheme scheme = null;
		String headerData = null;
		if ( header != null ) {
			for ( AuthenticationScheme aScheme : AuthenticationScheme.values() ) {
				if ( header.startsWith(aScheme.getSchemeName()) ) {
					scheme = aScheme;
					headerData = header.substring(scheme.getSchemeName().length() + 1);
					break;
				}
			}
		}

		if ( scheme == null ) {
			return null;
		}

		AuthenticationData data;
		switch (scheme) {
			case V1:
				data = new AuthenticationDataV1(request, headerData);
				break;

			case V2:
				data = new AuthenticationDataV2(request, headerData, EXPLICIT_HOST);
				break;

			default:
				throw new BadCredentialsException("Authentication scheme not supported.");
		}

		return data;
	}

}

/* ==================================================================
 * AuthenticationDataFactory.java - 19/09/2026 1:38:22 pm
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
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.BadCredentialsException;
import jakarta.servlet.http.HttpServletRequest;
import net.solarnetwork.security.http.sig.HttpSignatureFields;

/**
 * Factory for creating {@code AuthenticationData} instances.
 *
 * @author matt
 * @version 2.0
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
	 * authentication signature.
	 * </p>
	 *
	 * @since 1.1
	 */
	public static final String EXPLICIT_HOST_PROP = "sn.web.auth.explicitHost";

	private static final String EXPLICIT_HOST = System.getProperty(EXPLICIT_HOST_PROP, null);

	private static final HttpSignatureSettings DEFAULT_HTTP_SIGNATURE_SETTINGS = new HttpSignatureSettings();

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
	 * @return the authentication data, or {@code null} if no supported
	 *         credentials are provided on the request
	 * @throws IOException
	 *         if any IO error occurs
	 * @throws BadCredentialsException
	 *         if the authorization data is malformed in any way
	 */
	public static @Nullable AuthenticationData authenticationDataForAuthorizationHeader(
			final SecurityHttpServletRequestWrapper request) throws IOException {
		return authenticationDataForAuthorizationHeader(request, DEFAULT_HTTP_SIGNATURE_SETTINGS);
	}

	/**
	 * Obtain a {@link AuthenticationData} instance from a HTTP request.
	 *
	 * <p>
	 * An {@literal Authorization} header naming a supported scheme takes
	 * precedence, since it is an unambiguous statement of how the client means
	 * to authenticate. Only when no such header is present is the request
	 * inspected for a {@literal Signature-Input} field, which is how RFC 9421
	 * appendix A recommends detecting an HTTP message signature.
	 * </p>
	 *
	 * @param request
	 *        The HTTP request.
	 * @param httpSignatureSettings
	 *        the RFC 9421 profile settings
	 * @return the authentication data, or {@code null} if no supported
	 *         credentials are provided on the request
	 * @throws IOException
	 *         if any IO error occurs
	 * @throws BadCredentialsException
	 *         if the authorization data is malformed in any way
	 * @since 2.0
	 */
	public static @Nullable AuthenticationData authenticationDataForAuthorizationHeader(
			final SecurityHttpServletRequestWrapper request,
			final HttpSignatureSettings httpSignatureSettings) throws IOException {
		final String header = request.getHeader("Authorization");

		AuthenticationScheme scheme = null;
		String headerData = null;
		if ( header != null ) {
			for ( AuthenticationScheme aScheme : AuthenticationScheme.values() ) {
				headerData = aScheme.matchingHeaderData(header);
				if ( headerData != null ) {
					scheme = aScheme;
					break;
				}
			}
		}

		if ( scheme == null ) {
			if ( httpSignatureSettings.isEnabled() && isHttpSignatureRequest(request) ) {
				return new AuthenticationDataHttpSignature(request, httpSignatureSettings,
						EXPLICIT_HOST);
			}
			return null;
		}

		if ( headerData == null ) {
			throw new BadCredentialsException("Authentication info not provided.");
		}

		return switch (scheme) {
			case V1 -> new AuthenticationDataV1(request, headerData);
			case V2 -> new AuthenticationDataV2(request, headerData, EXPLICIT_HOST);
			default -> throw new BadCredentialsException("Authentication scheme not supported.");
		};
	}

	/**
	 * Test if a request carries an RFC 9421 HTTP message signature.
	 *
	 * @param request
	 *        the request
	 * @return {@code true} if the request has a {@literal Signature-Input} HTTP
	 *         field
	 * @since 2.0
	 */
	public static boolean isHttpSignatureRequest(final HttpServletRequest request) {
		return (request.getHeader(HttpSignatureFields.SIGNATURE_INPUT_HEADER) != null);
	}

}

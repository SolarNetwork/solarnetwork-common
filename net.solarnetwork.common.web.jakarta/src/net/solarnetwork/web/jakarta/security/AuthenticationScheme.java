/* ==================================================================
 * AuthenticationScheme.java - 19/09/2026 12:07:41 pm
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

/**
 * Authentication scheme constants.
 *
 * @author matt
 * @version 2.0
 * @since 1.11
 */
public enum AuthenticationScheme {

	/** The original scheme. */
	V1("SolarNetworkWS", true),

	/** The version 2 scheme. */
	V2("SNWS2", true),

	/**
	 * HTTP Message Signatures, as defined in RFC 9421.
	 *
	 * <p>
	 * Unlike the other schemes, this one does not use the
	 * {@literal Authorization} HTTP header at all: a signature is carried in
	 * the {@literal Signature-Input} and {@literal Signature} HTTP fields.
	 * Detection is therefore by the presence of the {@literal Signature-Input}
	 * field, as RFC 9421 appendix A recommends, rather than by an
	 * {@literal Authorization} scheme prefix.
	 * </p>
	 *
	 * @since 2.0
	 */
	HttpSignature("Signature", false);

	private final String schemeName;
	private final Pattern schemePrefix;
	private final boolean authorizationHeader;

	private AuthenticationScheme(String schemeName, boolean authorizationHeader) {
		this.schemeName = schemeName;
		this.schemePrefix = Pattern.compile("^" + schemeName + "\\s+");
		this.authorizationHeader = authorizationHeader;
	}

	/**
	 * Get the scheme name.
	 *
	 * @return the scheme name
	 */
	public String getSchemeName() {
		return schemeName;
	}

	/**
	 * Get the scheme name with whitespace.
	 *
	 * <p>
	 * This is meant to be used when inspecting an authorization header, to
	 * avoid trailing characters from matching.
	 * </p>
	 *
	 * @return the scheme prefix
	 * @since 1.1
	 */
	public final Pattern getSchemePrefix() {
		return schemePrefix;
	}

	/**
	 * Test if this scheme presents its credentials in the
	 * {@literal Authorization} HTTP header.
	 *
	 * @return {@code true} if the scheme uses the {@literal Authorization}
	 *         header
	 * @since 2.0
	 */
	public final boolean isAuthorizationHeader() {
		return authorizationHeader;
	}

	/**
	 * Extract the authentication header data if the scheme prefix matches this
	 * scheme.
	 *
	 * @param authenticationHeader
	 *        the full authentication header to test
	 * @return the associated authentication data (after the scheme prefix) if
	 *         the scheme prefix matches, {@code null} otherwise
	 * @since 1.1
	 */
	public @Nullable String matchingHeaderData(String authenticationHeader) {
		if ( !authorizationHeader ) {
			return null;
		}
		Matcher m = schemePrefix.matcher(authenticationHeader);
		return (m.find() ? authenticationHeader.substring(m.end()) : null);
	}

}

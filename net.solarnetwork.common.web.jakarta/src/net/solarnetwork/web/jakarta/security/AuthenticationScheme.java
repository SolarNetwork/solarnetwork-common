/* ==================================================================
 * AuthenticationScheme.java - 1/03/2017 5:14:54 PM
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

/**
 * Authentication scheme constants.
 *
 * @author matt
 * @version 1.1
 * @since 1.11
 */
public enum AuthenticationScheme {

	/** The original scheme. */
	V1("SolarNetworkWS"),

	/** The version 2 scheme. */
	V2("SNWS2");

	private final String schemeName;
	private final Pattern schemePrefix;

	private AuthenticationScheme(String schemeName) {
		this.schemeName = schemeName;
		this.schemePrefix = Pattern.compile("^" + schemeName + "\\s+");
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
		Matcher m = schemePrefix.matcher(authenticationHeader);
		return (m.find() ? authenticationHeader.substring(m.end()) : null);
	}

}

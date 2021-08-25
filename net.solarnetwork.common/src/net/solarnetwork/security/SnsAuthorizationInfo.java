/* ==================================================================
 * AuthorizationInfo.java - 16/08/2021 8:59:48 AM
 * 
 * Copyright 2021 SolarNetwork.net Dev Team
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

package net.solarnetwork.security;

import static org.springframework.util.StringUtils.delimitedListToStringArray;
import java.util.Map;
import net.solarnetwork.util.StringUtils;

/**
 * SNS authorization info
 * 
 * @author matt
 * @version 1.0
 */
public class SnsAuthorizationInfo {

	private final String scheme;
	private final String identifier;
	private final String[] headerNames;
	private final String signature;

	/**
	 * Parse an authorization header value into a {@link SnsAuthorizationInfo}
	 * instance.
	 * 
	 * @param authorizationHeader
	 *        the header to parse
	 * @return the instance
	 * @throws IllegalArgumentException
	 *         if the header cannot be parsed for any reason
	 */
	public static SnsAuthorizationInfo forAuthorizationHeader(String authorizationHeader) {
		if ( authorizationHeader == null || authorizationHeader.isEmpty() ) {
			throw new IllegalArgumentException("The authorizationHeader must not be null.");
		}
		authorizationHeader = authorizationHeader.trim();
		final int sep = authorizationHeader.indexOf(' ');
		if ( sep < 0 || sep + 1 >= authorizationHeader.length() ) {
			throw new IllegalArgumentException("Invalid authorization header syntax (missing scheme).");
		}
		String scheme = authorizationHeader.substring(0, sep);
		Map<String, String> components = StringUtils
				.commaDelimitedStringToMap(authorizationHeader.substring(sep + 1));
		if ( components == null || components.size() < 3 ) {
			throw new IllegalArgumentException(
					"Invalid authorization header syntax (missing required comopnents).");
		}
		String cred = components.get(SnsAuthorizationBuilder.AUTHORIZATION_COMPONENT_CREDENTIAL);
		if ( cred == null || cred.isEmpty() ) {
			throw new IllegalArgumentException(
					"Invalid authorization header syntax (Credential missing).");
		}
		String headers = components.get(SnsAuthorizationBuilder.AUTHORIZATION_COMPONENT_HEADERS);
		if ( headers == null || headers.isEmpty() ) {
			throw new IllegalArgumentException("Invalid authorization header syntax (Headers missing).");
		}
		String[] headerNames = delimitedListToStringArray(headers, ";");
		if ( headerNames == null || headerNames.length < 1 ) {
			throw new IllegalArgumentException("Invalid authorization header syntax (Headers empty).");
		}
		String sig = components.get(SnsAuthorizationBuilder.AUTHORIZATION_COMPONENT_SIGNATURE);
		if ( sig == null || sig.isEmpty() ) {
			throw new IllegalArgumentException(
					"Invalid authorization header syntax (Signature missing).");
		}
		return new SnsAuthorizationInfo(scheme, cred, headerNames, sig);
	}

	/**
	 * Constructor.
	 * 
	 * @param scheme
	 *        the schema (i.e. {@literal SNS}
	 * @param identifier
	 *        the identifier (credential)
	 * @param headerNames
	 *        the signed header name list
	 * @param signature
	 *        the computed signature, as a hex-encoded string
	 * @throws IllegalArgumentException
	 *         if any argument is {@literal null}
	 */
	public SnsAuthorizationInfo(String scheme, String identifier, String[] headerNames,
			String signature) {
		super();
		if ( scheme == null ) {
			throw new IllegalArgumentException("The scheme argument must not be null.");
		}
		this.scheme = scheme;
		if ( identifier == null ) {
			throw new IllegalArgumentException("The identifier argument must not be null.");
		}
		this.identifier = identifier;
		if ( headerNames == null ) {
			throw new IllegalArgumentException("The headerNames argument must not be null.");
		}
		this.headerNames = headerNames;
		if ( signature == null ) {
			throw new IllegalArgumentException("The signature argument must not be null.");
		}
		this.signature = signature;
	}

	/**
	 * Get the authorization scheme.
	 * 
	 * @return the scheme, never {@literal null}
	 */
	public String getScheme() {
		return scheme;
	}

	/**
	 * Get the identifier (credential).
	 * 
	 * @return the identifier, never {@literal null}
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Get the header name list.
	 * 
	 * @return the header names, never {@literal null}
	 */
	public String[] getHeaderNames() {
		return headerNames;
	}

	/**
	 * Get the signature.
	 * 
	 * @return the signature as a hex-encoded string, never {@literal null}
	 */
	public String getSignature() {
		return signature;
	}

}

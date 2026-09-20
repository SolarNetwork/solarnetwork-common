/* ==================================================================
 * SignatureContext.java - 19/09/2026 9:31:48 am
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

import java.util.List;

/**
 * Provides the message component values needed to build a signature base.
 *
 * <p>
 * This is the boundary between RFC 9421 and the runtime HTTP stack: implement
 * this over a servlet request on the verifying side, or over a request being
 * assembled on the signing side, and the rest of this package works the same
 * way for both.
 * </p>
 *
 * <p>
 * Only request messages are supported. The {@code @status} derived component
 * and the {@code req} component parameter, which apply to response messages,
 * are rejected by {@link SignatureBase}.
 * </p>
 *
 * @author matt
 * @version 1.0
 */
public interface SignatureContext {

	/**
	 * Get the request method, as the {@code @method} component value.
	 *
	 * @return the method, never {@code null}
	 */
	String method();

	/**
	 * Get the full target URI, as the {@code @target-uri} component value.
	 *
	 * @return the target URI, never {@code null}
	 */
	String targetUri();

	/**
	 * Get the authority, as the {@code @authority} component value.
	 *
	 * <p>
	 * The value must be normalized: the host lower-cased, and the default port
	 * for the scheme omitted.
	 * </p>
	 *
	 * @return the authority, never {@code null}
	 */
	String authority();

	/**
	 * Get the scheme, as the {@code @scheme} component value.
	 *
	 * @return the scheme, in lower case, never {@code null}
	 */
	String scheme();

	/**
	 * Get the request target, as the {@code @request-target} component value.
	 *
	 * @return the request target, never {@code null}
	 */
	String requestTarget();

	/**
	 * Get the absolute path, as the {@code @path} component value.
	 *
	 * <p>
	 * An empty path is normalized to a single {@code /} character.
	 * </p>
	 *
	 * @return the path, never {@code null}
	 */
	String path();

	/**
	 * Get the query string, as the {@code @query} component value.
	 *
	 * <p>
	 * The value includes the leading {@code ?} character, and percent-encoded
	 * octets are not decoded. When the request has no query string the value is
	 * a lone {@code ?} character.
	 * </p>
	 *
	 * @return the query, never {@code null}
	 */
	String query();

	/**
	 * Get the values of a single named query parameter, as the
	 * {@code @query-param} component value.
	 *
	 * <p>
	 * The values must be taken from the query string alone, never from a
	 * form-encoded request body, and must be percent-decoded and then re-encoded
	 * per the {@code application/x-www-form-urlencoded} serializing rules.
	 * </p>
	 *
	 * @param name
	 *        the decoded parameter name to look for
	 * @return the values, in query string order; an empty list if the parameter
	 *         is not present, and more than one value if the name occurs more
	 *         than once, both of which are errors for signing purposes
	 */
	List<String> queryParamValues(String name);

	/**
	 * Get the values of an HTTP field.
	 *
	 * <p>
	 * Each instance of the field in the message is returned separately, in
	 * message order, without combination: {@link FieldCanonicalizer} applies
	 * the combination rules, which depend on the component parameters.
	 * </p>
	 *
	 * @param name
	 *        the field name, in lower case
	 * @return the field values; an empty list if the field is not present in
	 *         the message
	 */
	List<String> fieldValues(String name);

}

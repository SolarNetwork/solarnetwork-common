/* ==================================================================
 * QueryParams.java - 19/09/2026 11:05:18 am
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

import static java.nio.charset.StandardCharsets.UTF_8;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * Query string parsing for the {@code @query-param} derived component, per RFC
 * 9421 section 2.2.8.
 *
 * <p>
 * The values come from the query string alone. A servlet container merges query
 * parameters with {@code application/x-www-form-urlencoded} request content, so
 * the raw query string has to be parsed directly rather than going through
 * {@code ServletRequest.getParameterMap()}.
 * </p>
 *
 * @author matt
 * @version 1.0
 */
public final class QueryParams {

	private QueryParams() {
		// not available
	}

	/**
	 * Get the encoded values of a named query parameter.
	 *
	 * @param rawQuery
	 *        the raw query string, without the leading {@code ?} character, or
	 *        {@code null} if the request has no query string
	 * @param name
	 *        the decoded parameter name to look for
	 * @return the encoded values, in query string order, never {@code null}
	 */
	public static List<String> values(@Nullable String rawQuery, String name) {
		if ( rawQuery == null || rawQuery.isEmpty() ) {
			return List.of();
		}
		final List<String> result = new ArrayList<>(2);
		for ( String pair : rawQuery.split("&", -1) ) {
			if ( pair.isEmpty() ) {
				continue;
			}
			final int idx = pair.indexOf('=');
			final String rawName = (idx < 0 ? pair : pair.substring(0, idx));
			if ( !name.equals(decode(rawName)) ) {
				continue;
			}
			final String rawValue = (idx < 0 ? "" : pair.substring(idx + 1));
			result.add(encode(decode(rawValue)));
		}
		return result;
	}

	/**
	 * Decode a query string component.
	 *
	 * <p>
	 * This applies the {@code application/x-www-form-urlencoded} parsing rules:
	 * a {@code +} character means a space, and percent-encoded octets are
	 * decoded as UTF-8.
	 * </p>
	 *
	 * @param value
	 *        the raw value
	 * @return the decoded value
	 */
	public static String decode(String value) {
		return URLDecoder.decode(value, UTF_8);
	}

	/**
	 * Encode a query string component.
	 *
	 * <p>
	 * This applies the "percent-encode after encoding" process with the
	 * {@code application/x-www-form-urlencoded} percent-encode set. Note that
	 * unlike the full form-urlencoded serializer, a space is encoded as
	 * {@code %20} rather than {@code +}, which is what RFC 9421 section 2.2.8
	 * calls for.
	 * </p>
	 *
	 * @param value
	 *        the decoded value
	 * @return the encoded value
	 */
	public static String encode(String value) {
		final byte[] bytes = value.getBytes(UTF_8);
		final StringBuilder buf = new StringBuilder(bytes.length);
		for ( byte b : bytes ) {
			final int c = b & 0xFF;
			if ( (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')
					|| c == '*' || c == '-' || c == '.' || c == '_' ) {
				buf.append((char) c);
			} else {
				buf.append('%');
				buf.append(Character.toUpperCase(Character.forDigit((c >> 4) & 0xF, 16)));
				buf.append(Character.toUpperCase(Character.forDigit(c & 0xF, 16)));
			}
		}
		return buf.toString();
	}

}

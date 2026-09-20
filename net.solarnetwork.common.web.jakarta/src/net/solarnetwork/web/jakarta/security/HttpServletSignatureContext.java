/* ==================================================================
 * HttpServletSignatureContext.java - 19/09/2026 12:45:07 pm
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import org.jspecify.annotations.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import net.solarnetwork.security.http.sig.QueryParams;
import net.solarnetwork.security.http.sig.SignatureContext;

/**
 * A {@link SignatureContext} over a servlet request.
 *
 * <p>
 * The authority and scheme are resolved the same way the SNWS2 scheme resolves
 * the signed {@literal Host} header value, so that a signature created against
 * the public host verifies behind a proxy or load balancer: an explicit host
 * overrides everything, otherwise the {@literal X-Forwarded-Proto} and
 * {@literal X-Forwarded-Port} headers fill in what the {@literal Host} header
 * does not carry. Unlike the SNWS2 {@literal host} value, the result is then
 * normalized as RFC 9421 section 2.2.3 requires, lower-casing the host and
 * omitting the default port for the scheme.
 * </p>
 *
 * @author matt
 * @version 1.0
 * @since 4.53
 */
public class HttpServletSignatureContext implements SignatureContext {

	private static final String HOST_HEADER = "host";
	private static final String FORWARDED_PROTO_HEADER = "X-Forwarded-Proto";
	private static final String FORWARDED_PORT_HEADER = "X-Forwarded-Port";

	private final HttpServletRequest request;
	private final @Nullable String explicitHost;

	/**
	 * Constructor.
	 *
	 * @param request
	 *        the request
	 * @param explicitHost
	 *        a fixed value to use instead of the {@literal Host} HTTP header
	 *        value, or {@code null} to derive the value from the request
	 */
	public HttpServletSignatureContext(HttpServletRequest request, @Nullable String explicitHost) {
		super();
		this.request = request;
		this.explicitHost = explicitHost;
	}

	@Override
	public String method() {
		return request.getMethod();
	}

	@Override
	public String targetUri() {
		final String query = request.getQueryString();
		return scheme() + "://" + authority() + path() + (query != null ? "?" + query : "");
	}

	@Override
	public String authority() {
		final String scheme = scheme();
		String value = explicitHost;
		if ( value == null ) {
			value = header(HOST_HEADER);
			if ( value == null || value.isEmpty() ) {
				// no Host value provided, so fall back to what the container resolved
				final String name = request.getServerName();
				value = (name != null ? name : "");
				final int port = request.getServerPort();
				if ( port > 0 ) {
					value += ":" + port;
				}
			} else if ( value.indexOf(':') < 0 ) {
				// the Host header carries no port, so look for one a proxy may have stripped
				final String port = forwardedPort(scheme);
				if ( port != null ) {
					value += ":" + port;
				}
			}
		}
		return normalizedAuthority(value, scheme);
	}

	private @Nullable String forwardedPort(String scheme) {
		final String port = header(FORWARDED_PORT_HEADER);
		if ( port != null && !port.isEmpty() ) {
			return port;
		}
		return ("https".equals(scheme) ? "443" : null);
	}

	private static String normalizedAuthority(String value, String scheme) {
		// RFC 9421 2.2.3: lower-case the host, and omit the default port
		final String authority = value.trim().toLowerCase(Locale.ROOT);
		final int idx = authority.lastIndexOf(':');
		if ( idx < 0 || idx < authority.lastIndexOf(']') ) {
			// no port, or the colon is within an IPv6 literal
			return authority;
		}
		final String port = authority.substring(idx + 1);
		if ( ("https".equals(scheme) && "443".equals(port))
				|| ("http".equals(scheme) && "80".equals(port)) ) {
			return authority.substring(0, idx);
		}
		return authority;
	}

	@Override
	public String scheme() {
		final String forwarded = header(FORWARDED_PROTO_HEADER);
		if ( forwarded != null && !forwarded.isEmpty() ) {
			return forwarded.trim().toLowerCase(Locale.ROOT);
		}
		final String scheme = request.getScheme();
		return (scheme != null ? scheme.toLowerCase(Locale.ROOT) : "http");
	}

	@Override
	public String requestTarget() {
		final String query = request.getQueryString();
		return path() + (query != null ? "?" + query : "");
	}

	@Override
	public String path() {
		final String uri = request.getRequestURI();
		return (uri == null || uri.isEmpty() ? "/" : uri);
	}

	@Override
	public String query() {
		final String query = request.getQueryString();
		return (query != null ? "?" + query : "?");
	}

	@Override
	public List<String> queryParamValues(String name) {
		return QueryParams.values(request.getQueryString(), name);
	}

	@Override
	public List<String> fieldValues(String name) {
		final Enumeration<String> values = request.getHeaders(name);
		if ( values == null ) {
			return List.of();
		}
		final List<String> result = new ArrayList<>(2);
		while ( values.hasMoreElements() ) {
			result.add(values.nextElement());
		}
		return Collections.unmodifiableList(result);
	}

	private @Nullable String header(String name) {
		return request.getHeader(name);
	}

}

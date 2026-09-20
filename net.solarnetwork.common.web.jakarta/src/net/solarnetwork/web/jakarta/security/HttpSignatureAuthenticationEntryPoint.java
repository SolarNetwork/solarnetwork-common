/* ==================================================================
 * HttpSignatureAuthenticationEntryPoint.java - 19/09/2026 1:52:40 pm
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
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.AuthenticationException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.solarnetwork.security.http.sig.ContentDigest;
import net.solarnetwork.security.http.sig.HttpSignatureFields;

/**
 * Authentication entry point that also handles RFC 9421 HTTP message
 * signatures.
 *
 * <p>
 * On top of the standard behaviour this adds an {@literal Accept-Signature}
 * field, as described in RFC 9421 section 5.1, spelling out the covered
 * components a signature has to carry. A developer whose signature was rejected
 * then has the expected shape of one in the response, rather than having to
 * infer it.
 * </p>
 *
 * @author matt
 * @version 1.0
 * @since 4.53
 */
public class HttpSignatureAuthenticationEntryPoint extends SecurityTokenAuthenticationEntryPoint {

	/**
	 * The {@code Accept-Signature} value describing the minimum covered
	 * components of an acceptable signature.
	 */
	public static final String DEFAULT_ACCEPT_SIGNATURE = """
			sn=("@method" "@authority" "@path" "@query");created;keyid;tag="%s\"""";

	private final HttpSignatureSettings settings;

	/**
	 * Constructor.
	 *
	 * @param settings
	 *        the profile settings
	 */
	public HttpSignatureAuthenticationEntryPoint(HttpSignatureSettings settings) {
		super();
		this.settings = settings;
		setHttpHeaders(defaultHttpHeaders());
	}

	/**
	 * Get the CORS headers to add to a response, if not already set.
	 *
	 * <p>
	 * A preflight request might never reaches this class if the CORS
	 * configuration an application registers with
	 * {@code WebMvcConfigurer.addCorsMappings()} is what answers those, and is
	 * the authoritative list. These values only fill in a response this entry
	 * point writes itself.
	 * </p>
	 *
	 * @return the headers
	 */
	private static Map<String, String> defaultHttpHeaders() {
		final Map<String, String> headers = new LinkedHashMap<>(4);
		headers.put("Access-Control-Allow-Origin", "*");
		headers.put("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, OPTIONS, PATCH");
		// @formatter:off
		headers.put("Access-Control-Allow-Headers",
				HttpHeaders.AUTHORIZATION
				+ ", " + ContentDigest.CONTENT_DIGEST_HEADER
				+ ", Content-MD5"
				+ ", " + HttpHeaders.CONTENT_TYPE
				+ ", Digest "
				+ ", " + HttpSignatureFields.SIGNATURE_HEADER
				+ ", " + HttpSignatureFields.SIGNATURE_INPUT_HEADER
				+ ", " + WebConstants.HEADER_DATE
		);
		headers.put("Access-Control-Expose-Headers",
				HttpSignatureFields.ACCEPT_SIGNATURE_HEADER
				+ ", " + WebConstants.HEADER_DATE
		);
		// @formatter:on
		return headers;
	}

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		if ( settings.isEnabled()
				&& response.getHeader(HttpSignatureFields.ACCEPT_SIGNATURE_HEADER) == null ) {
			response.addHeader(HttpSignatureFields.ACCEPT_SIGNATURE_HEADER,
					DEFAULT_ACCEPT_SIGNATURE.formatted(settings.getTag()));
		}
		super.commence(request, response, authException);
	}

}

/* ==================================================================
 * SecurityTokenAuthenticationEntryPoint.java - 7/09/2023 1:11:19 pm
 * 
 * Copyright 2023 SolarNetwork.net Dev Team
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
import java.util.HashMap;
import java.util.Map;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.solarnetwork.codec.JsonUtils;
import net.solarnetwork.util.ByteUtils;
import net.solarnetwork.web.jakarta.domain.Response;

/**
 * Entry point for SolarNetworkWS authentication.
 * 
 * @author matt
 * @version 1.0
 * @since 3.3
 */
public class SecurityTokenAuthenticationEntryPoint
		implements AuthenticationEntryPoint, Ordered, AccessDeniedHandler {

	private int order = Integer.MAX_VALUE;
	private Map<String, String> httpHeaders = defaultHttpHeaders();
	private HandlerExceptionResolver handlerExceptionResolver;

	private static Map<String, String> defaultHttpHeaders() {
		Map<String, String> headers = new HashMap<String, String>(2);
		headers.put("Access-Control-Allow-Origin", "*");
		headers.put("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, OPTIONS, PATCH");
		headers.put("Access-Control-Allow-Headers",
				"Authorization, Content-MD5, Content-Type, Digest, X-SN-Date");
		return headers;
	}

	/**
	 * Constructor.
	 */
	public SecurityTokenAuthenticationEntryPoint() {
		super();
	}

	@Override
	public int getOrder() {
		return order;
	}

	private static String securityTokenAuthenticationScheme(String authorization) {
		// return V2, unless V1 was used in request
		if ( authorization != null ) {
			int space = authorization.indexOf(' ');
			if ( space > 0 ) {
				String scheme = authorization.substring(0, space);
				if ( AuthenticationScheme.V1.getSchemeName().equals(scheme) ) {
					return scheme;
				}
			}
		}
		return AuthenticationScheme.V2.getSchemeName();
	}

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		final String authHeaderValue = request.getHeader("Authorization");
		final String scheme = securityTokenAuthenticationScheme(authHeaderValue);
		final int statusCode = HttpServletResponse.SC_UNAUTHORIZED;
		if ( response.getHeader("WWW-Authenticate") == null ) {
			response.addHeader("WWW-Authenticate", scheme);
		}
		response.setStatus(statusCode);
		response.addHeader(WebConstants.HEADER_ERROR_MESSAGE, authException.getMessage());
		if ( httpHeaders != null ) {
			for ( Map.Entry<String, String> me : httpHeaders.entrySet() ) {
				if ( response.getHeader(me.getKey()) == null ) {
					response.addHeader(me.getKey(), me.getValue());
				}
			}
		}

		if ( handleWithResolver(request, response, authException) ) {
			return;
		}
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		Response<Void> responseObj = new Response<>(Boolean.FALSE, String.valueOf(statusCode),
				authException.getMessage(), null);
		byte[] responseJson = JsonUtils.getJSONString(responseObj, "{\"success\":false}")
				.getBytes(ByteUtils.UTF8);
		response.setContentLength(responseJson.length);
		response.getOutputStream().write(responseJson);
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException, ServletException {
		if ( handleWithResolver(request, response, accessDeniedException) ) {
			return;
		}
		response.sendError(HttpServletResponse.SC_FORBIDDEN);
	}

	/**
	 * Handle an exception as a transient problem based on resource usage, i.e.
	 * "try again later".
	 * 
	 * @param request
	 *        the request
	 * @param response
	 *        the response
	 * @param exception
	 *        the exception
	 * @throws IOException
	 *         if an error occurs writing the response
	 * @throws ServletException
	 *         if a servlet error occurs
	 */
	public void handleTransientResourceException(HttpServletRequest request,
			HttpServletResponse response, Exception exception) throws IOException, ServletException {
		if ( handleWithResolver(request, response, exception) ) {
			return;
		}
		response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(), "Try again later.");
	}

	private boolean handleWithResolver(HttpServletRequest request, HttpServletResponse response,
			Exception exception) throws ServletException {
		if ( handlerExceptionResolver != null ) {
			try {
				if ( handlerExceptionResolver.resolveException(request, response, null,
						exception) != null ) {
					return true;
				}
			} catch ( RuntimeException e ) {
				throw e;
			} catch ( Exception e ) {
				throw new ServletException(e);
			}
		}
		return false;
	}

	/**
	 * Set the desired order.
	 * 
	 * @param order
	 *        the order to set
	 */
	public void setOrder(int order) {
		this.order = order;
	}

	/**
	 * Get the currently configured HTTP headers that are included in each
	 * response.
	 * 
	 * @return The HTTP headers to include in each response.
	 */
	public Map<String, String> getHttpHeaders() {
		return httpHeaders;
	}

	/**
	 * Set additional HTTP headers to include in the response. By default the
	 * {@code Access-Control-Allow-Origin} header is set to {@code *} and
	 * {@code Access-Control-Allow-Headers} header is set to
	 * {@code Authorization, X-SN-Date}.
	 * 
	 * @param httpHeaders
	 *        The HTTP headers to include in each response.
	 */
	public void setHttpHeaders(Map<String, String> httpHeaders) {
		this.httpHeaders = httpHeaders;
	}

	/**
	 * A {@link HandlerExceptionResolver} to resolve authentication exceptions
	 * with.
	 * 
	 * <p>
	 * This provides a way to render the exceptions as JSON, etc.
	 * </p>
	 * 
	 * @param handlerExceptionResolver
	 *        the resolver to delegate exceptions to
	 */
	public void setHandlerExceptionResolver(HandlerExceptionResolver handlerExceptionResolver) {
		this.handlerExceptionResolver = handlerExceptionResolver;
	}

}

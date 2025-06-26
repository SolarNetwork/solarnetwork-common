/* ==================================================================
 * RequestInfoHandshakeInterceptor.java - 18/11/2017 7:23:33 AM
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

package net.solarnetwork.web.jakarta.support;

import java.net.URI;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

/**
 * {@code HandshakeInterceptor} that populates properties from the connection as
 * message headers.
 * 
 * @author matt
 * @version 1.1
 * @since 1.14
 */
public class RequestInfoHandshakeInterceptor implements HandshakeInterceptor {

	/**
	 * The attribute name for the {@link org.springframework.http.HttpMethod} of
	 * the HTTP request.
	 */
	private static final String REQUEST_METHOD_ATTR = "requestMethod";

	/** The attribute name for the {@link URI} of the HTTP request. */
	public static final String REQUEST_URI_ATTR = "requestUri";

	/** The attribute name for the {@link HttpHeaders} of the HTTP request. */
	public static final String REQUEST_HEADERS = "requestHeaders";

	/**
	 * Constructor.
	 */
	public RequestInfoHandshakeInterceptor() {
		super();
	}

	/**
	 * Populate request information as session attributes.
	 * 
	 * <p>
	 * The following attributes will be populated:
	 * </p>
	 * 
	 * <dl>
	 * <dt>requestUri</dt>
	 * <dd>The {@link URI} of the HTTP request.</dd>
	 * </dl>
	 */
	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
			WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
		attributes.putIfAbsent(REQUEST_URI_ATTR, request.getURI());
		attributes.putIfAbsent(REQUEST_METHOD_ATTR, request.getMethod());
		attributes.putIfAbsent(REQUEST_HEADERS, request.getHeaders());
		return true;
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
			WebSocketHandler wsHandler, Exception exception) {
		// nothing to add
	}

}

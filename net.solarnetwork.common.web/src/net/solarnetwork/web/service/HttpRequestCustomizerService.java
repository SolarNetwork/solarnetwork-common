/* ==================================================================
 * HttpRequestCustomizerService.java - 2/04/2023 6:09:10 am
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

package net.solarnetwork.web.service;

import static net.solarnetwork.util.ObjectUtils.requireNonNullArgument;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.StreamingHttpOutputMessage;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import net.solarnetwork.service.Identifiable;
import net.solarnetwork.util.ByteList;

/**
 * API for a service that can customize HTTP requests, such as populating
 * headers.
 * 
 * <p>
 * This API extends {@link Identifiable}; each implementation must define their
 * own unique identifier so they can be differentiated at runtime.
 * </p>
 * 
 * @author matt
 * @version 1.1
 */
public interface HttpRequestCustomizerService extends Identifiable {

	/**
	 * A standardized group UID for customizer services that perform
	 * authorization.
	 */
	String AUTHORIZATION_GROUP_UID = "Authorization";

	/**
	 * Customize an HTTP request and body.
	 * 
	 * <p>
	 * The {@code body} argument can be manipulated if needed.
	 * </p>
	 * 
	 * @param request
	 *        the request, never {@literal null}
	 * @param body
	 *        the body, never {@literal null}
	 * @param parameters
	 *        optional parameters to pass to the customizer, the meaning of
	 *        which is implementation specific
	 * @return the request to use, which may be {@code request} if that instance
	 *         was modified directly or unchanged, or {@literal null} to prevent
	 *         the request from happening at all
	 */
	HttpRequest customize(HttpRequest request, ByteList body, Map<String, ?> parameters);

	/**
	 * Shortcut to invoke {@link #customize(HttpRequest, ByteList, Map)} with an
	 * empty parameters map.
	 * 
	 * @param request
	 *        the request, never {@literal null}
	 * @param body
	 *        the body, never {@literal null}
	 * @return the request to use, which may be {@code request} if that instance
	 *         was modified directly or unchanged
	 */
	default HttpRequest customize(HttpRequest request, ByteList body) {
		return customize(request, body, Collections.emptyMap());
	}

	/**
	 * Apply this service on a new client HTTP GET request.
	 * 
	 * @param requestFactory
	 *        the request factory to use
	 * @param uri
	 *        the URI to request
	 * @param parameters
	 *        the parameters
	 * @return the client HTTP request, ready to be executed
	 * @throws IllegalArgumentException
	 *         if {@code requestFactory}, {@code uri}, or {@code method} are
	 *         {@literal null}
	 * @throws IOException
	 *         if any IO error occurs
	 * @since 1.1
	 */
	default ClientHttpRequest applyGet(ClientHttpRequestFactory requestFactory, URI uri,
			Map<String, ?> parameters) throws IOException {
		return apply(requestFactory, uri, HttpMethod.GET, null, parameters);
	}

	/**
	 * Apply this service on a new client HTTP request.
	 * 
	 * @param requestFactory
	 *        the request factory to use
	 * @param uri
	 *        the URI to request
	 * @param method
	 *        the HTTP method
	 * @param body
	 *        the HTTP body
	 * @param parameters
	 *        the parameters
	 * @return the client HTTP request, ready to be executed
	 * @throws IllegalArgumentException
	 *         if {@code uri} or {@code method} are {@literal null}, or the
	 *         customized request is changed and {@code requestFactory} is
	 *         {@literal null}
	 * @throws IOException
	 *         if any IO error occurs
	 * @since 1.1
	 */
	default ClientHttpRequest apply(ClientHttpRequestFactory requestFactory, URI uri, HttpMethod method,
			ByteList body, Map<String, ?> parameters) throws IOException {
		final ClientHttpRequest request = requireNonNullArgument(requestFactory, "requestFactory")
				.createRequest(requireNonNullArgument(uri, "uri"),
						requireNonNullArgument(method, "method"));
		return apply(requestFactory, request, body, parameters);
	}

	/**
	 * Apply this service on a new client HTTP request.
	 * 
	 * @param requestFactory
	 *        the request factory to use
	 * @param uri
	 *        the URI to request
	 * @param method
	 *        the HTTP method
	 * @param body
	 *        the HTTP body
	 * @param parameters
	 *        the parameters
	 * @return the client HTTP request, ready to be executed
	 * @throws IllegalArgumentException
	 *         if {@code request}is {@literal null}, or the customized request
	 *         is changed and {@code requestFactory} is {@literal null}
	 * @throws IOException
	 *         if any IO error occurs
	 * @since 1.1
	 */
	default ClientHttpRequest apply(ClientHttpRequestFactory requestFactory, ClientHttpRequest request,
			ByteList body, Map<String, ?> parameters) throws IOException {
		requireNonNullArgument(request, "request");
		final HttpRequest customizedRequest = customize(request, body, parameters);
		if ( customizedRequest != request ) {
			final ClientHttpRequest customizedClientRequest = requireNonNullArgument(requestFactory,
					"requestFactory").createRequest(customizedRequest.getURI(),
							customizedRequest.getMethod());
			customizedRequest.getHeaders()
					.forEach((key, value) -> customizedClientRequest.getHeaders().addAll(key, value));
			if ( body != null && !body.isEmpty() ) {
				if ( customizedClientRequest instanceof StreamingHttpOutputMessage ) {
					StreamingHttpOutputMessage streamingOutputMessage = (StreamingHttpOutputMessage) customizedClientRequest;
					streamingOutputMessage
							.setBody(outputStream -> outputStream.write(body.toArrayValue()));
				} else {
					customizedClientRequest.getBody().write(body.toArrayValue());
				}
			}
			return customizedClientRequest;
		}
		return request;
	}

}

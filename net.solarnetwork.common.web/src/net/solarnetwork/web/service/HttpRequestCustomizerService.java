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

import java.util.Collections;
import java.util.Map;
import org.springframework.http.HttpRequest;
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
 * @version 1.0
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

}

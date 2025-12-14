/* ==================================================================
 * WebTestUtils.java - 15/12/2025 8:39:40 am
 *
 * Copyright 2025 SolarNetwork.net Dev Team
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

package net.solarnetwork.web.jakarta.support.test;

import java.util.Iterator;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * Utilities for testing.
 *
 * @author matt
 * @version 1.0
 */
public final class WebTestUtils {

	private WebTestUtils() {
		// not available
	}

	/**
	 * Get a testing {@link RestTemplate}.
	 *
	 * @return the instance
	 */
	public static RestTemplate testRestTemplate() {
		var restTemplate = new RestTemplate();

		// remove CBOR message converter so tests only dealing with JSON accept headers
		for ( Iterator<HttpMessageConverter<?>> itr = restTemplate.getMessageConverters().iterator(); itr
				.hasNext(); ) {
			var converter = itr.next();
			if ( converter.getClass().getSimpleName().toLowerCase().contains("cbor") ) {
				itr.remove();
			}
		}

		return restTemplate;
	}

}

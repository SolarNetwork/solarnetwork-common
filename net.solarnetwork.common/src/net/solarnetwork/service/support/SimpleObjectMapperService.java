/* ==================================================================
 * SimpleObjectMapperService.java - 24/09/2016 1:23:51 PM
 * 
 * Copyright 2007-2016 SolarNetwork.net Dev Team
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

package net.solarnetwork.service.support;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.solarnetwork.service.ObjectMapperService;

/**
 * Basic implementation of {@link ObjectMapperService}.
 * 
 * @author matt
 * @version 2.0
 */
public class SimpleObjectMapperService implements ObjectMapperService {

	private ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	/**
	 * Set the mapper to use.
	 * 
	 * @param objectMapper
	 *        the mapper to use
	 */
	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	/**
	 * Set the {@link JsonFactory} to use.
	 * 
	 * <p>
	 * This will cause a new {@link ObjectMapper} to be instantiated with the
	 * given factory.
	 * </p>
	 * 
	 * @param factory
	 *        the factory to use
	 * @since 1.1
	 */
	public void setJsonFactory(JsonFactory factory) {
		setObjectMapper(new ObjectMapper(factory));
	}

}

/* ==================================================================
 * JSONStringPropertySerializer.java - Aug 29, 2014 2:51:51 PM
 *
 * Copyright 2007-2014 SolarNetwork.net Dev Team
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

package net.solarnetwork.codec.jackson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.solarnetwork.codec.PropertySerializer;
import tools.jackson.databind.ObjectMapper;

/**
 * Serialize objects to a JSON string value.
 *
 * @author matt
 * @version 1.0
 * @since 4.13
 */
public class JsonStringPropertySerializer implements PropertySerializer {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private ObjectMapper objectMapper;

	/**
	 * Constructor.
	 */
	public JsonStringPropertySerializer() {
		super();
	}

	@Override
	public Object serialize(Object data, String propertyName, Object propertyValue) {
		try {
			return objectMapper.writeValueAsString(propertyValue);
		} catch ( Exception e ) {
			log.error("Exception marshalling {} to JSON", propertyValue, e);
		}
		return null;
	}

	/**
	 * Get the object mapper.
	 *
	 * @return the mapper
	 */
	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	/**
	 * Set the object mapper.
	 *
	 * @param objectMapper
	 *        the mapper to set
	 */
	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

}

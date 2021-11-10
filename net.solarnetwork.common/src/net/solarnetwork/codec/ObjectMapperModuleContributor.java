/* ==================================================================
 * ObjectMapperModuleContributor.java - 24/09/2016 1:05:00 PM
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

package net.solarnetwork.codec;

import java.util.List;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import net.solarnetwork.service.ObjectMapperService;

/**
 * Class for adding a {@link Module} to an existing {@link ObjectMapper}.
 * 
 * This is useful for adding serializers/deserializers to a pre-existing mapper.
 * 
 * @author matt
 * @version 1.1
 */
public class ObjectMapperModuleContributor extends ObjectMapperModuleSupport {

	/**
	 * Setup the {@link Module} and register it with the configured
	 * {@link ObjectMapper}.
	 */
	public void setupModule() {
		SimpleModule module = new SimpleModule(getModuleName(), getModuleVersion());
		if ( getSerializers() != null ) {
			for ( JsonSerializer<?> serializer : getSerializers() ) {
				module.addSerializer(serializer);
			}
		}
		if ( getDeserializers() != null ) {
			for ( JsonDeserializer<?> deserializer : getDeserializers() ) {
				registerDeserializer(module, deserializer);
			}
		}
		if ( getKeyDeserializers() != null ) {
			for ( TypedKeyDeserializer deserializer : getKeyDeserializers() ) {
				module.addKeyDeserializer(deserializer.getClass(), deserializer.getKeyDeserializer());
			}
		}
		if ( getKeySerializers() != null ) {
			for ( JsonSerializer<?> serializer : getKeySerializers() ) {
				registerKeySerializer(module, serializer);
			}
		}
		getObjectMapper().registerModule(module);

		List<Module> otherModules = getModules();
		if ( otherModules != null ) {
			getObjectMapper().registerModules(otherModules);
		}
	}

	/**
	 * Set the {@code ObjectMapper} via a {@link ObjectMapperService}.
	 * 
	 * @param service
	 *        The service to call {@link #getObjectMapper()} on.
	 */
	public void setObjectMapperService(ObjectMapperService service) {
		setObjectMapper(service.getObjectMapper());
	}

}

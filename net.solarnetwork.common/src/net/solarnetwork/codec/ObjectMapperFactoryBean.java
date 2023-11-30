/* ==================================================================
 * ObjectMapperFactoryBean.java - Mar 20, 2013 5:28:37 PM
 * 
 * Copyright 2007-2013 SolarNetwork.net Dev Team
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

import java.util.Collection;
import java.util.List;
import org.springframework.beans.factory.FactoryBean;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Factory for {@link ObjectMapper} that allows configuring an application-wide
 * ObjectMapper.
 * 
 * <p>
 * This factory will generate a {@link Module} and register that with the
 * returned {@link ObjectMapper}.
 * </p>
 * 
 * @author matt
 * @version 1.5
 */
public class ObjectMapperFactoryBean extends ObjectMapperModuleSupport
		implements FactoryBean<ObjectMapper> {

	private JsonInclude.Include serializationInclusion = JsonInclude.Include.NON_NULL;
	private List<Object> featuresToEnable = null;
	private List<Object> featuresToDisable = null;

	/**
	 * Constructor.
	 */
	public ObjectMapperFactoryBean() {
		super();
	}

	@Override
	public ObjectMapper getObject() throws Exception {
		ObjectMapper mapper = getObjectMapper();
		if ( mapper == null ) {
			mapper = new ObjectMapper();
			setObjectMapper(mapper);
		}
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
		if ( serializationInclusion != null ) {
			mapper.setSerializationInclusion(serializationInclusion);
		}
		setupFeatures(mapper, featuresToEnable, true);
		setupFeatures(mapper, featuresToDisable, false);
		mapper.registerModule(module);

		List<Module> otherModules = getModules();
		if ( otherModules != null ) {
			getObjectMapper().registerModules(otherModules);
		}

		return mapper;
	}

	@SuppressWarnings("deprecation")
	private void setupFeatures(final ObjectMapper m, final Collection<?> features, final boolean state) {
		if ( features == null ) {
			return;
		}
		for ( Object o : features ) {
			if ( o instanceof SerializationFeature ) {
				m.configure((SerializationFeature) o, state);
			} else if ( o instanceof DeserializationFeature ) {
				m.configure((DeserializationFeature) o, state);
			} else if ( o instanceof MapperFeature ) {
				m.configure((MapperFeature) o, state);
			}
		}
	}

	@Override
	public Class<?> getObjectType() {
		return ObjectMapper.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	/**
	 * Get the serialization inclusion setting.
	 * 
	 * @return the serialization inclusion
	 * @since 1.2
	 */
	public JsonInclude.Include getSerializationInclusion() {
		return serializationInclusion;
	}

	/**
	 * Set the serialization inclusion to use.
	 * 
	 * @param serializationInclusion
	 *        the inclusion setting
	 * @since 1.2
	 */
	public void setSerializationInclusion(JsonInclude.Include serializationInclusion) {
		this.serializationInclusion = serializationInclusion;
	}

	/**
	 * Get a list of {@link SerializationFeature} or
	 * {@link DeserializationFeature} flags to enable.
	 * 
	 * @return list of features to enable
	 * @since 1.2
	 */
	public List<Object> getFeaturesToEnable() {
		return featuresToEnable;
	}

	/**
	 * Set a list of {@link SerializationFeature} or
	 * {@link DeserializationFeature} flags to enable.
	 * 
	 * @param featuresToEnable
	 *        the list of features to enable
	 * @since 1.2
	 */
	public void setFeaturesToEnable(List<Object> featuresToEnable) {
		this.featuresToEnable = featuresToEnable;
	}

	/**
	 * Get a list of {@link SerializationFeature} or
	 * {@link DeserializationFeature} flags to disable.
	 * 
	 * @return the list of features to disable
	 * @since 1.2
	 */
	public List<Object> getFeaturesToDisable() {
		return featuresToDisable;
	}

	/**
	 * Set a list of {@link SerializationFeature} or
	 * {@link DeserializationFeature} flags to disable.
	 * 
	 * @param featuresToDisable
	 *        the list of features to disable
	 * @since 1.2
	 */
	public void setFeaturesToDisable(List<Object> featuresToDisable) {
		this.featuresToDisable = featuresToDisable;
	}

}

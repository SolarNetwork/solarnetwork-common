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

package net.solarnetwork.util;

import java.util.Collection;
import java.util.List;
import org.springframework.beans.factory.FactoryBean;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
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
 * <p>
 * The configurable properties of this class are:
 * </p>
 * 
 * <dl class="class-properties">
 * <dt>moduleName</dt>
 * <dd>A {@link Module} name to use.</dd>
 * 
 * <dt>moduleVersion</dt>
 * <dd>A {@link Version} to use for the module.</dd>
 * 
 * <dt>serializers</dt>
 * <dd>A list of serializers to register with the module.</dd>
 * 
 * <dt>deserializers</dt>
 * <dd>A list of deserializers to register with the module. Note that these must
 * be subclasses of {@link StdDeserializer}.</dd>
 * 
 * <dt>mapper</dt>
 * <dd>The {@link ObjectMapper} to configure.</dd>
 * 
 * <dt>serializationInclusion</dt>
 * <dd>A serialization inclusion setting to configure.</dd>
 * 
 * <dt>featuresToEnable</dt>
 * <dd>A list of {@link SerializationFeature} or {@link DeserializationFeature}
 * flags to enable.</dd>
 * 
 * <dt>featuresToDisable</dt>
 * <dd>A list of {@link SerializationFeature} or {@link DeserializationFeature}
 * flags to disable.</dd>
 * </dl>
 * 
 * @author matt
 * @version 1.2
 */
public class ObjectMapperFactoryBean implements FactoryBean<ObjectMapper> {

	private ObjectMapper mapper = new ObjectMapper();
	private String moduleName = "SolarNetworkModule";
	private Version moduleVersion = new Version(1, 0, 0, null, null, null);
	private List<JsonSerializer<?>> serializers;
	private List<JsonDeserializer<?>> deserializers;
	private JsonInclude.Include serializationInclusion = JsonInclude.Include.NON_NULL;
	private List<Object> featuresToEnable = null;
	private List<Object> featuresToDisable = null;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void registerStdDeserializer(SimpleModule module, StdDeserializer stdDeserializer) {
		Class deserType = stdDeserializer.handledType();
		module.addDeserializer(deserType, stdDeserializer);
	}

	@Override
	public ObjectMapper getObject() throws Exception {
		if ( mapper == null ) {
			mapper = new ObjectMapper();
		}
		SimpleModule module = new SimpleModule(moduleName, moduleVersion);
		if ( serializers != null ) {
			for ( JsonSerializer<?> serializer : serializers ) {
				module.addSerializer(serializer);
			}
		}
		if ( deserializers != null ) {
			for ( JsonDeserializer<?> deserializer : deserializers ) {
				if ( deserializer instanceof StdDeserializer<?> ) {
					registerStdDeserializer(module, (StdDeserializer<?>) deserializer);
				}
			}
		}
		if ( serializationInclusion != null ) {
			mapper.setSerializationInclusion(serializationInclusion);
		}
		setupFeatures(mapper, featuresToEnable, true);
		setupFeatures(mapper, featuresToDisable, false);
		mapper.registerModule(module);
		return mapper;
	}

	private void setupFeatures(final ObjectMapper m, final Collection<?> features, final boolean state) {
		if ( features == null ) {
			return;
		}
		for ( Object o : features ) {
			if ( o instanceof SerializationFeature ) {
				m.configure((SerializationFeature) o, state);
			} else if ( o instanceof DeserializationFeature ) {
				m.configure((DeserializationFeature) o, state);
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

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public Version getModuleVersion() {
		return moduleVersion;
	}

	public void setModuleVersion(Version moduleVersion) {
		this.moduleVersion = moduleVersion;
	}

	public List<JsonSerializer<?>> getSerializers() {
		return serializers;
	}

	public void setSerializers(List<JsonSerializer<?>> serializers) {
		this.serializers = serializers;
	}

	public ObjectMapper getMapper() {
		return mapper;
	}

	public void setMapper(ObjectMapper obj) {
		this.mapper = obj;
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

	public List<JsonDeserializer<?>> getDeserializers() {
		return deserializers;
	}

	/**
	 * Set a list of {@link JsonDeserializer} objects to configure on the
	 * mapper.
	 * 
	 * @param deserializers
	 *        the deserializers
	 * @since 1.1
	 */
	public void setDeserializers(List<JsonDeserializer<?>> deserializers) {
		this.deserializers = deserializers;
	}

	public List<Object> getFeaturesToEnable() {
		return featuresToEnable;
	}

	/**
	 * Set a list of {@link SerializationFeature} or
	 * {@link DeserializationFeature} flags to enable.
	 * 
	 * @param featuresToEnable
	 * @since 1.2
	 */
	public void setFeaturesToEnable(List<Object> featuresToEnable) {
		this.featuresToEnable = featuresToEnable;
	}

	public List<Object> getFeaturesToDisable() {
		return featuresToDisable;
	}

	/**
	 * Set a list of {@link SerializationFeature} or
	 * {@link DeserializationFeature} flags to disable.
	 * 
	 * @param featuresToDisable
	 * @since 1.2
	 */
	public void setFeaturesToDisable(List<Object> featuresToDisable) {
		this.featuresToDisable = featuresToDisable;
	}

}

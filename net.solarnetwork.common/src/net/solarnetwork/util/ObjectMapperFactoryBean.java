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

import java.util.List;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.Module;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.deser.std.StdDeserializer;
import org.codehaus.jackson.map.module.SimpleModule;
import org.springframework.beans.factory.FactoryBean;

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
 * </dl>
 * 
 * @author matt
 * @version 1.1
 */
public class ObjectMapperFactoryBean implements FactoryBean<ObjectMapper> {

	private ObjectMapper mapper = new ObjectMapper();
	private String moduleName = "SolarNetworkModule";
	private Version moduleVersion = new Version(1, 0, 0, null);
	private List<JsonSerializer<?>> serializers;
	private List<JsonDeserializer<?>> deserializers;
	private JsonSerialize.Inclusion serializationInclusion = JsonSerialize.Inclusion.NON_NULL;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void registerStdDeserializer(SimpleModule module, StdDeserializer stdDeserializer) {
		Class deserType = stdDeserializer.getValueClass();
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
		mapper.registerModule(module);
		return mapper;
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
	 * @since 1.1
	 */
	public JsonSerialize.Inclusion getSerializationInclusion() {
		return serializationInclusion;
	}

	/**
	 * Set the serialization inclusion to use.
	 * 
	 * @param serializationInclusion
	 *        the inclusion setting
	 * @since 1.1
	 */
	public void setSerializationInclusion(JsonSerialize.Inclusion serializationInclusion) {
		this.serializationInclusion = serializationInclusion;
		;
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

}

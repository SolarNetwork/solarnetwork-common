/* ==================================================================
 * ObjectMapperModuleSupport.java - 24/09/2016 12:50:51 PM
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
import org.springframework.util.StringUtils;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import net.solarnetwork.service.support.SimpleObjectMapperService;

/**
 * Helper class for {@link ObjectMapper} configuration.
 * 
 * @author matt
 * @version 1.1
 */
public class ObjectMapperModuleSupport extends SimpleObjectMapperService {

	private String moduleName = "SolarNetworkModule";
	private Version moduleVersion = new Version(1, 0, 0, null, null, null);
	private List<JsonSerializer<?>> serializers;
	private List<JsonDeserializer<?>> deserializers;
	private List<TypedKeyDeserializer> keyDeserializers;
	private List<JsonSerializer<?>> keySerializers;
	private List<Module> modules;

	/**
	 * Constructor.
	 */
	public ObjectMapperModuleSupport() {
		super();
	}

	/**
	 * Helper method for registering {@link JsonDeserializer} instances of
	 * unknown types at runtime, ignoring compiler warnings.
	 * 
	 * @param module
	 *        The module to register with.
	 * @param deserializer
	 *        A {@link JsonDeserializer} instance.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void registerDeserializer(SimpleModule module, JsonDeserializer deserializer) {
		Class deserType = deserializer.handledType();
		module.addDeserializer(deserType, deserializer);
	}

	/**
	 * Helper method for registering key {@link JsonSerializer} instances of
	 * unknown types at runtime, ignoring compiler warnings.
	 * 
	 * @param module
	 *        The module to register with.
	 * @param serializer
	 *        A {@link JsonSerializer} instance.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void registerKeySerializer(SimpleModule module, JsonSerializer serializer) {
		Class serType = serializer.handledType();
		module.addKeySerializer(serType, serializer);
	}

	/**
	 * Get the module name.
	 * 
	 * @return the name
	 */
	public String getModuleName() {
		return moduleName;
	}

	/**
	 * Set the module name.
	 * 
	 * @param moduleName
	 *        The module name to set.
	 */
	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	/**
	 * Get the module version.
	 * 
	 * @return the version
	 */
	public Version getModuleVersion() {
		return moduleVersion;
	}

	/**
	 * Set the module version.
	 * 
	 * @param moduleVersion
	 *        The version to set.
	 * @see #setVersion(String)
	 */
	public void setModuleVersion(Version moduleVersion) {
		this.moduleVersion = moduleVersion;
	}

	/**
	 * Set the module version as a period-delimited version string, e.g.
	 * {@code 1.0.0}.
	 * 
	 * @param versionString
	 *        The version string to set.
	 * @see #setModuleVersion(Version)
	 */
	public void setVersion(String versionString) {
		String[] a = StringUtils.split(versionString, ".");
		int v1 = 1, v2 = 0, v3 = 0;
		String s1 = null, s2 = null, s3 = null;
		int i;
		for ( i = 0; i < 3 && i < a.length; i++ ) {
			int v = Integer.parseInt(a[i]);
			switch (i) {
				case 0:
					v1 = v;
					break;
				case 1:
					v2 = v;
					break;
				case 2:
					v3 = v;
					break;
			}
		}
		for ( ; i < 6 && i < a.length; i++ ) {
			switch (i) {
				case 3:
					s1 = a[i];
					break;
				case 4:
					s2 = a[i];
					break;
				case 5:
					s3 = a[i];
					break;
			}
		}
		setModuleVersion(new Version(v1, v2, v3, s1, s2, s3));
	}

	/**
	 * Get the serializers.
	 * 
	 * @return the serializers
	 */
	public List<JsonSerializer<?>> getSerializers() {
		return serializers;
	}

	/**
	 * Set a list of {@link JsonSerializer} objects to configure on the module.
	 * 
	 * @param serializers
	 *        the serializers
	 */
	public void setSerializers(List<JsonSerializer<?>> serializers) {
		this.serializers = serializers;
	}

	/**
	 * Get a list of {@link JsonSerializer} objects to configure on the module.
	 * 
	 * @return the deserializers
	 */
	public List<JsonDeserializer<?>> getDeserializers() {
		return deserializers;
	}

	/**
	 * Set a list of {@link JsonDeserializer} objects to configure on the
	 * module.
	 * 
	 * @param deserializers
	 *        the deserializers
	 */
	public void setDeserializers(List<JsonDeserializer<?>> deserializers) {
		this.deserializers = deserializers;
	}

	/**
	 * Get the configured list of {@link KeyDeserializer} objects to register.
	 * 
	 * @return List of key deserializers.
	 */
	public List<TypedKeyDeserializer> getKeyDeserializers() {
		return keyDeserializers;
	}

	/**
	 * Set a list of {@link TypedKeyDeserializer} objects.
	 * 
	 * @param keyDeserializers
	 *        The key deserializers.
	 */
	public void setKeyDeserializers(List<TypedKeyDeserializer> keyDeserializers) {
		this.keyDeserializers = keyDeserializers;
	}

	/**
	 * Get a list of {@link TypedKeyDeserializer} objects.
	 * 
	 * @return the serializers
	 */
	public List<JsonSerializer<?>> getKeySerializers() {
		return keySerializers;
	}

	/**
	 * Set a list of key serializers to register.
	 * 
	 * @param keySerializers
	 *        The key serializers to register.
	 */
	public void setKeySerializers(List<JsonSerializer<?>> keySerializers) {
		this.keySerializers = keySerializers;
	}

	/**
	 * Get the list of modules.
	 * 
	 * @return the modules
	 * @since 1.1
	 */
	public List<Module> getModules() {
		return modules;
	}

	/**
	 * Set a list of modules to register.
	 * 
	 * @param modules
	 *        the modules to set
	 * @since 1.1
	 */
	public void setModules(List<Module> modules) {
		this.modules = modules;
	}

}

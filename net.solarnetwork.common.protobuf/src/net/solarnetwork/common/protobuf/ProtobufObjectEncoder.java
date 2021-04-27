/* ==================================================================
 * ProtobufObjectEncoder.java - 26/04/2021 12:02:44 PM
 * 
 * Copyright 2021 SolarNetwork.net Dev Team
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

package net.solarnetwork.common.protobuf;

import java.io.IOException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.protobuf.Message;
import net.solarnetwork.io.ObjectEncoder;
import net.solarnetwork.settings.SettingsChangeObserver;
import net.solarnetwork.support.BasicIdentifiable;
import net.solarnetwork.util.FilterableService;
import net.solarnetwork.util.OptionalService;

/**
 * A {@link ObjectEncoder} service that uses a configurable
 * {@link ProtobufCompilerService} to dynamically encode objects into Protobuf
 * message byte arrays.
 * 
 * @author matt
 * @version 1.0
 */
public abstract class ProtobufObjectEncoder extends BasicIdentifiable
		implements ObjectEncoder, SettingsChangeObserver {

	/** A class-level logger. */
	protected final Logger log = LoggerFactory.getLogger(getClass());

	private OptionalService<ProtobufCompilerService> compilerService;
	private String messageClassName;

	private ClassLoader protoClassLoader;

	@Override
	public void configurationChanged(Map<String, Object> properties) {
		if ( properties == null ) {
			return;
		}
		// reset any cached class loader in case the proto files changed
		synchronized ( this ) {
			protoClassLoader = null;
		}
	}

	@Override
	public byte[] encodeAsBytes(Object obj, Map<String, ?> parameters) throws IOException {
		Map<String, ?> data = convertToMap(obj, parameters);
		if ( data == null ) {
			log.info("Data not available for conversion to Protobuf message {}", messageClassName);
			return null;
		}
		ClassLoader cl = protoClassLoader();
		if ( cl == null ) {
			log.info("ClassLoader not available for conversion to Protobuf message {}",
					messageClassName);
			return null;
		}
		ProtobufMessagePopulator populator = new ProtobufMessagePopulator(cl, messageClassName);
		try {
			populator.setMessageProperties(data, false);
			Message msg = populator.build();
			return msg.toByteArray();
		} catch ( IllegalArgumentException e ) {
			log.warn("Error populating Protobuf message {}: {}", messageClassName, e.toString());
			return null;
		}
	}

	private synchronized ClassLoader protoClassLoader() throws IOException {
		if ( protoClassLoader != null ) {
			return protoClassLoader;
		}
		ProtobufCompilerService compiler = OptionalService.service(compilerService);
		if ( compiler == null ) {
			return null;
		}
		ClassLoader cl = compileProtobufResources(compiler);
		this.protoClassLoader = cl;
		return cl;
	}

	/**
	 * Convert the provided object into a map suitable for passing to a
	 * {@link ProtobufMessagePopulator}.
	 * 
	 * <p>
	 * This method is called from {@link #encodeAsBytes(Object, Map)}.
	 * </p>
	 * 
	 * @param obj
	 *        the object to convert to a map
	 * @param parameters
	 *        the parameters
	 * @return the map of data, or {@literal null} if populating a message is
	 *         not possible
	 */
	protected abstract Map<String, ?> convertToMap(Object obj, Map<String, ?> parameters);

	/**
	 * Compile protobuf resources.
	 * 
	 * <p>
	 * This method is called from {@link #encodeAsBytes(Object, Map)}.
	 * </p>
	 * 
	 * @param compiler
	 *        the compiler to use
	 * @return the resulting class loader
	 * @throws IOException
	 *         if any compile error occurs
	 */
	protected abstract ClassLoader compileProtobufResources(ProtobufCompilerService compiler)
			throws IOException;

	/**
	 * Get the compiler service UID filter.
	 * 
	 * <p>
	 * The configured {@link #getCompilerService()} must also implement
	 * {@link FilterableService} for this method to work.
	 * </p>
	 * 
	 * @return the UID filter
	 */
	public String getCompilerServiceUidFilter() {
		return FilterableService.filterPropValue(getCompilerService(), UID_PROPERTY);
	}

	/**
	 * Set the compiler service UID filter.
	 * 
	 * <p>
	 * The configured {@link #getCompilerService()} must also implement
	 * {@link FilterableService} for this method to work.
	 * </p>
	 * 
	 * @param uid
	 *        the filter to set
	 */
	public void setCompilerServiceUidFilter(String uid) {
		FilterableService.setFilterProp(getCompilerService(), UID_PROPERTY, uid);
	}

	/**
	 * Get the compiler service.
	 * 
	 * @return the service
	 */
	public OptionalService<ProtobufCompilerService> getCompilerService() {
		return compilerService;
	}

	/**
	 * Set the compiler service.
	 * 
	 * @param compilerService
	 *        the service to set
	 */
	public void setCompilerService(OptionalService<ProtobufCompilerService> compilerService) {
		this.compilerService = compilerService;
	}

	/**
	 * Get the Protobuf message class name to map objects to.
	 * 
	 * @return the message class name
	 */
	public String getMessageClassName() {
		return messageClassName;
	}

	/**
	 * Set the Protobuf message class name to map objects to.
	 * 
	 * @param messageClassName
	 *        the class name to set
	 */
	public void setMessageClassName(String messageClassName) {
		this.messageClassName = messageClassName;
	}

}

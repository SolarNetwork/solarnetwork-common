/* ==================================================================
 * ProtobufObjectCodec.java - 26/04/2021 12:02:44 PM
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

import static java.lang.String.format;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.protobuf.Message;
import net.solarnetwork.codec.ObjectCodec;
import net.solarnetwork.codec.ObjectDecoder;
import net.solarnetwork.codec.ObjectEncoder;
import net.solarnetwork.service.FilterableService;
import net.solarnetwork.service.OptionalService;
import net.solarnetwork.service.support.BasicIdentifiable;
import net.solarnetwork.settings.SettingsChangeObserver;

/**
 * A {@link ObjectEncoder} and {@link ObjectDecoder} service that uses a
 * configurable {@link ProtobufCompilerService} to dynamically encode and decode
 * objects into Protobuf message byte arrays.
 * 
 * <p>
 * Each instance of this class is designed to encode/decode a single Protobuf
 * message, configured via {@link #setMessageClassName(String)}.
 * </p>
 * 
 * @author matt
 * @version 2.0
 */
public abstract class ProtobufObjectCodec extends BasicIdentifiable
		implements ObjectCodec, SettingsChangeObserver {

	/** A class-level logger. */
	protected final Logger log = LoggerFactory.getLogger(getClass());

	private OptionalService<ProtobufCompilerService> compilerService;
	private String messageClassName;

	private ClassLoader protoClassLoader;

	/**
	 * Constructor.
	 */
	public ProtobufObjectCodec() {
		super();
	}

	/**
	 * Internal constructor.
	 * 
	 * @param protoClassLoader
	 *        the class loader to use
	 */
	protected ProtobufObjectCodec(ClassLoader protoClassLoader) {
		super();
		this.protoClassLoader = protoClassLoader;
	}

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
		final String className = getMessageClassName();
		if ( className == null || className.isEmpty() ) {
			throw new IOException("No Protobuf message class name configured to encode object into.");
		}
		final Map<String, ?> data = convertToMap(obj, parameters);
		if ( data == null ) {
			throw new IOException(
					format("Data not available for conversion to Protobuf message %s", className));
		}
		ClassLoader cl = protoClassLoader();
		if ( cl == null ) {
			throw new IOException(format(
					"ClassLoader not available for conversion to Protobuf message %s", className));
		}
		ProtobufMessagePopulator populator = new ProtobufMessagePopulator(cl, className);
		try {
			populator.setMessageProperties(data, false);
			Message msg = populator.build();
			log.trace("Encoded {} as message:\n{}", obj, msg);
			return msg.toByteArray();
		} catch ( IllegalArgumentException e ) {
			throw new IOException(
					format("Error populating Protobuf message %s: %s", className, e.getMessage(), e));
		}
	}

	@Override
	public Object decodeFromBytes(byte[] data, Map<String, ?> parameters) throws IOException {
		final String className = getMessageClassName();
		if ( className == null || className.isEmpty() ) {
			return null;
		}
		ClassLoader cl = protoClassLoader();
		try {
			@SuppressWarnings("unchecked")
			Class<? extends Message> clazz = (Class<? extends Message>) cl.loadClass(className);
			Method parseFromMethod = clazz.getMethod("parseFrom", byte[].class);
			Object result = parseFromMethod.invoke(null, data);
			if ( result == null ) {
				throw new IOException(format("No object decoded for message class %s.", className));
			}
			return result;
		} catch ( ClassNotFoundException e ) {
			throw new IOException(format("Message class %s not found.", className), e);
		} catch ( NoSuchMethodException | SecurityException e ) {
			throw new IOException(
					format("Error getting parseFrom(byte[]) method on message class %s: %s", className,
							e.getMessage()),
					e);
		} catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
			throw new IOException(
					format("Error invoking parseFrom(byte[]) method on message class %s: %s", className,
							e.getMessage()),
					e);
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

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
import net.solarnetwork.io.ObjectEncoder;
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
public class ProtobufObjectEncoder extends BasicIdentifiable implements ObjectEncoder {

	private OptionalService<ProtobufCompilerService> compilerService;

	@Override
	public byte[] encodeAsBytes(Object obj, Map<String, ?> parameters) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

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

}

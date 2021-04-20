/* ==================================================================
 * ProtobufCompilerService.java - 20/04/2021 1:11:21 PM
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
import org.springframework.core.io.Resource;

/**
 * API for a service that can compile Protobuf definitions into Java classes.
 * 
 * @author matt
 * @version 1.0
 */
public interface ProtobufCompilerService {

	/**
	 * Compile a set of protobuf resources into Java classes.
	 * 
	 * @param protobufResources
	 *        the protobuf resources to compile
	 * @param parameter
	 *        compiler parameters
	 * @return a class loader that can load the compiled protobuf classes
	 * @throws IOException
	 *         if any IO error occurs
	 */
	ClassLoader compileProtobufResources(Iterable<Resource> protobufResources, Map<String, ?> parameters)
			throws IOException;

}

/* ==================================================================
 * MapClassLoader.java - 20/04/2021 2:55:14 PM
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

package net.solarnetwork.common.protobuf.protoc;

import java.util.Map;

/**
 * Simple {@link ClassLoader} for an in-memory map of byte code.
 * 
 * <p>
 * Adapted from Core Java, Volume II--Advanced Features, 10th Edition.
 * </p>
 * 
 * @author matt
 * @version 1.0
 */
public class MapClassLoader extends ClassLoader {

	private final Map<String, byte[]> classes;

	/**
	 * Constructor.
	 * 
	 * @param classes
	 *        the compiled byte code
	 */
	public MapClassLoader(Map<String, byte[]> classes) {
		this.classes = classes;
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		byte[] classBytes = classes.get(name);
		if ( classBytes == null ) {
			throw new ClassNotFoundException(name);
		}
		Class<?> clazz = defineClass(name, classBytes, 0, classBytes.length);
		if ( clazz == null ) {
			throw new NoClassDefFoundError(name);
		}
		return clazz;
	}

}

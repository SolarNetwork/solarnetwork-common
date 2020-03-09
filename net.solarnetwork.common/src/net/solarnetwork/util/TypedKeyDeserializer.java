/* ==================================================================
 * TypedKeyDeserializer.java - 1/10/2016 5:12:12 PM
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

package net.solarnetwork.util;

import com.fasterxml.jackson.databind.KeyDeserializer;

/**
 * {@link KeyDeserializer} does not implement an interface, nor provide a
 * default "type" the deserializer supports. This API provides a way to
 * configure them on a {@link com.fasterxml.jackson.databind.Module}.
 * 
 * @author matt
 * @version 1.0
 */
public interface TypedKeyDeserializer {

	/**
	 * The type to register the key deserializer with.
	 * 
	 * @return A type.
	 */
	Class<?> getKeyType();

	/**
	 * The key deserializer to register.
	 * 
	 * @return The key deserializer.
	 */
	KeyDeserializer getKeyDeserializer();

}

/* ===================================================================
 * PropertySerializer.java
 * 
 * Copyright (c) 2007 Matt Magoffin (spamsqr@msqr.us)
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
 * ===================================================================
 * $Id$
 * ===================================================================
 */

package net.solarnetwork.util;

/**
 * API for special handling of data serialization, in place of simply
 * calling {@code String.valueOf()}.
 * 
 * <p>This API is designed with implementations being thread-safe by
 * default, so a single implementation can be instantiated once and
 * used throughout an application.</p>
 * 
 * @author matt
 * @version $Revision$ $Date$
 */
public interface PropertySerializer {
	
	/**
	 * Serialize a property value.
	 * 
	 * <p>The {@code data} and {@code propertyName} parameters might not
	 * be used by different implementations, but allow for a single
	 * implementation to serialize more than one property of an object
	 * in different ways, if desired.</p>
	 * 
	 * @param data the source data being serialized, i.e. a JavaBean
	 * @param propertyName the name of the property being serialized
	 * @param propertyValue the value of the property to serialize
	 * @return the serialized value of the property
	 */
	Object serialize(Object data, String propertyName, Object propertyValue);
	
}
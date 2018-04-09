/* ==================================================================
 * SerializeIgnore.java - Jun 3, 2011 2:42:43 PM
 * 
 * Copyright 2007-2011 SolarNetwork.net Dev Team
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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to signal a field or method should not be used during
 * introspection-based serialization or de-serialization.
 * 
 * <p>
 * This is designed to be used during view rendering, for example, where
 * specific properties should not be rendered into the view output format (e.g.
 * JSON, XML, etc).
 * 
 * @author matt
 * @version 1.0
 */
@Target({ ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface SerializeIgnore {

	/**
	 * Optional argument that defines if this annotation is active.
	 */
	boolean value() default true;

}

/* ==================================================================
 * BindingResultSerializer.java - Jun 11, 2011 5:33:01 PM
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

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.validation.BindingResult;

/**
 * Serialize a {@link BindingResult} into a simple Map, to eliminate circular
 * references.
 * 
 * @author matt
 * @version 1.0
 */
public class BindingResultSerializer implements PropertySerializer {

	@Override
	public Object serialize(Object data, String propertyName, Object propertyValue) {
		if ( propertyValue == null ) {
			return null;
		}
		if ( !(propertyValue instanceof BindingResult) ) {
			throw new IllegalArgumentException("Not a BindingResult: " + propertyValue.getClass());
		}
		BindingResult br = (BindingResult) propertyValue;
		if ( !br.hasErrors() ) {
			return null;
		}
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		map.put("objectName", br.getObjectName());
		map.put("errors", br.getAllErrors());
		map.put("globalErrors", br.getGlobalErrors());
		return map;
	}

}

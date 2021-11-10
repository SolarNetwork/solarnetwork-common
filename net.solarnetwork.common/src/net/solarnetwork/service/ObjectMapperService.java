/* ==================================================================
 * ObjectMapperService.java - 24/09/2016 1:21:21 PM
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

package net.solarnetwork.service;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * API for exposing {@link ObjectMapper} as a service.
 * 
 * @author matt
 * @version 1.0
 */
public interface ObjectMapperService {

	/**
	 * Get the {@link ObjectMapper} instance.
	 * 
	 * @return the mapper
	 */
	ObjectMapper getObjectMapper();

}

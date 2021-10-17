/* ==================================================================
 * CloseableService.java - 20/06/2017 5:50:09 PM
 * 
 * Copyright 2017 SolarNetwork.net Dev Team
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

/**
 * A dynamic service that wishes to have internal resources freed when it is no
 * longer needed.
 * 
 * @author matt
 * @version 1.0
 * @since 1.36
 */
public interface CloseableService {

	/**
	 * Method to call when the service is released.
	 */
	public void closeService();

}

/* ==================================================================
 * WireLoggingSupport.java - 3/05/2021 2:29:30 PM
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

package net.solarnetwork.common.mqtt;

/**
 * API for components that support toggling "wire" debug logging.
 * 
 * <p>
 * By "wire" we mean logging the contents of protocol data, as sent over the
 * network connection.
 * </p>
 * 
 * @author matt
 * @version 1.0
 * @since 2.2
 */
public interface WireLoggingSupport {

	/**
	 * Get the wire logging enabled flag.
	 * 
	 * @return {@literal true} if wire logging is enabled}
	 */
	boolean isWireLoggingEnabled();

	/**
	 * Set the wire logging enabled flag.
	 * 
	 * @param enabled
	 *        {@literal true} to enable wire logging, {@literal false} to
	 *        disable
	 */
	void setWireLoggingEnabled(boolean enabled);

}

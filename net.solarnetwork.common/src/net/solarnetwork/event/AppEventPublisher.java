/* ==================================================================
 * AppEventPublisher.java - 9/11/2021 11:04:35 AM
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

package net.solarnetwork.event;

/**
 * API for publishing application events.
 * 
 * <p>
 * This has been modeled after the
 * <code>org.osgi.service.event.EventAdmin</code> service to help working in
 * non-OSGi environments.
 * </p>
 * 
 * @author matt
 * @version 1.0
 * @since 2.0
 */
public interface AppEventPublisher {

	/**
	 * Asynchronously publish an application event.
	 * 
	 * @param event
	 *        the event to publish
	 */
	void postEvent(AppEvent event);

}

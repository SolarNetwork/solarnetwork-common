/* ==================================================================
 * AppEventHandlerRegistrar.java - 13/06/2017 10:23:55 PM
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

package net.solarnetwork.common.osgi.event;

import org.osgi.service.event.EventHandler;

/**
 * API for registering OSGi {@link EventHandler} instances with topics.
 * 
 * @author matt
 * @version 1.0
 * @since 1.36
 */
public interface EventHandlerRegistrar {

	/**
	 * Register a handler for a set of topics.
	 * 
	 * @param handler
	 *        the handler
	 * @param topics
	 *        the topics to regsiter
	 */
	void registerEventHandler(EventHandler handler, String... topics);

	/**
	 * Deregister a handler from all topics.
	 * 
	 * @param handler
	 *        the handler to deregister
	 */
	void deregisterEventHandler(EventHandler handler);

}

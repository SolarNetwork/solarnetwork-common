/* ==================================================================
 * SimpMessageSendingOperations.java - 26/09/2016 6:24:08 PM
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

package net.solarnetwork.support;

import org.springframework.messaging.simp.SimpMessageSendingOperations;

/**
 * Factory bean to facilitate auto-wiring of a
 * {@link SimpMessageSendingOperations}.
 * 
 * With Spring's websocket support, the automatically registered
 * {@code SimpMessageSendingOperations} has a generated ID, and cannot be easily
 * exported as an OSGi service. This factory can overcome that, by auto-wiring
 * the object as a property, then exporting the bean with a known ID (or simply
 * as an OSGi service).
 * 
 * @author matt
 * @version 1.0
 */
public class SimpMessageSendingOperationsFactoryBean
		extends AutowiredPropertyFactoryBean<SimpMessageSendingOperations> {

	public SimpMessageSendingOperationsFactoryBean() {
		super(SimpMessageSendingOperations.class);
	}

}

/* ==================================================================
 * RegistrationReceipt.java - Dec 18, 2009 3:55:41 PM
 * 
 * Copyright 2007-2009 SolarNetwork.net Dev Team
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
 * $Id$
 * ==================================================================
 */

package net.solarnetwork.domain;

import java.io.Serializable;

/**
 * A receipt for registration.
 * 
 * @author matt
 * @version $Id$
 */
public interface RegistrationReceipt extends Serializable {
	
	/**
	 * Get the username that has been registered.
	 * 
	 * @return the email address
	 */
	String getUsername();
	
	/**
	 * Get the confirmation code required to activate the registered
	 * user.
	 * 
	 * @return confirmation code
	 */
	String getConfirmationCode();

}

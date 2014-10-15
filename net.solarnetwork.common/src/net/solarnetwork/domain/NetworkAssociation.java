/* ==================================================================
 * NetworkAssociation.java - Nov 29, 2012 10:30:24 AM
 * 
 * Copyright 2007-2012 SolarNetwork.net Dev Team
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

package net.solarnetwork.domain;

/**
 * API for node/network association details.
 * 
 * @author matt
 * @version 1.1
 */
public interface NetworkAssociation extends NetworkIdentity {

	/**
	 * Get a confirmation key, generated on the network side.
	 * 
	 * @return confirmation key
	 */
	String getConfirmationKey();

	/**
	 * Get a security phrase, generated on the network side.
	 * 
	 * @return a security phrase
	 */
	String getSecurityPhrase();

	/**
	 * Get the username associated with this association.
	 * 
	 * @return the username
	 */
	String getUsername();

	/**
	 * Get a password to use for this association's keystore.
	 * 
	 * @return a keystore password
	 * @since 1.1
	 */
	String getKeystorePassword();

}

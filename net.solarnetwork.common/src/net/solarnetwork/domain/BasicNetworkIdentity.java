/* ==================================================================
 * BasicNetworkIdentity.java - Sep 13, 2011 8:16:37 PM
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
 * $Id$
 * ==================================================================
 */

package net.solarnetwork.domain;

import java.io.Serializable;

/**
 * Basic implementation of {@link NetworkIdentity}.
 * 
 * @author matt
 * @version $Revision$
 */
public class BasicNetworkIdentity implements NetworkIdentity, Serializable {

	private static final long serialVersionUID = 6888385242844976306L;

	private String identityKey;
	private String termsOfService;
	
	/**
	 * Default constructor.
	 */
	public BasicNetworkIdentity() {
		super();
	}
	
	/**
	 * Construct with an identity key.
	 * 
	 * @param identityKey the identity key
	 * @param termsOfService the TOS
	 */
	public BasicNetworkIdentity(String identityKey, String termsOfService) {
		super();
		setIdentityKey(identityKey);
		setTermsOfService(termsOfService);
	}
	
	@Override
	public String getIdentityKey() {
		return identityKey;
	}

	@Override
	public String getTermsOfService() {
		return termsOfService;
	}

	public void setIdentityKey(String identityKey) {
		this.identityKey = identityKey;
	}

	public void setTermsOfService(String termsOfService) {
		this.termsOfService = termsOfService;
	}

}

/* ==================================================================
 * BasicRegistrationReceipt.java - Dec 18, 2009 4:06:10 PM
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
 */

package net.solarnetwork.domain;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Basic implementation of {@link RegistrationReceipt}.
 * 
 * @author matt
 * @version 1.1
 */
public class BasicRegistrationReceipt implements RegistrationReceipt, Cloneable {

	private static final long serialVersionUID = -8288922092122946581L;

	/** The username. */
	private String username;

	/** The confirmation code. */
	private String confirmationCode;

	/**
	 * Default constructor.
	 */
	public BasicRegistrationReceipt() {
		this(null, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param username
	 *        the usenrame
	 * @param confirmationCode
	 *        the confirmation code
	 */
	public BasicRegistrationReceipt(String username, String confirmationCode) {
		super();
		this.username = username;
		this.confirmationCode = confirmationCode;
	}

	@Override
	public String getConfirmationCode() {
		return confirmationCode;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public String getUsernameURLComponent() {
		String result = getUsername();
		try {
			return (result == null ? null : URLEncoder.encode(result, "UTF-8"));
		} catch ( UnsupportedEncodingException e ) {
			// this should not happen
			throw new RuntimeException("Error encoding username for URL", e);
		}
	}

	@Override
	protected Object clone() {
		try {
			return super.clone();
		} catch ( CloneNotSupportedException e ) {
			// should not get here
			throw new RuntimeException(e);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((confirmationCode == null) ? 0 : confirmationCode.hashCode());
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj )
			return true;
		if ( obj == null )
			return false;
		if ( getClass() != obj.getClass() )
			return false;
		BasicRegistrationReceipt other = (BasicRegistrationReceipt) obj;
		if ( confirmationCode == null ) {
			if ( other.confirmationCode != null )
				return false;
		} else if ( !confirmationCode.equals(other.confirmationCode) )
			return false;
		if ( username == null ) {
			if ( other.username != null )
				return false;
		} else if ( !username.equals(other.username) )
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "RegistrationReceipt{username=" + username + ",confirmationCode=" + confirmationCode
				+ '}';
	}

	/**
	 * Set the username.
	 * 
	 * @param username
	 *        the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Set the confirmation code.
	 * 
	 * @param confirmationCode
	 *        the confirmationCode to set
	 */
	public void setConfirmationCode(String confirmationCode) {
		this.confirmationCode = confirmationCode;
	}

}

/* ==================================================================
 * AuthorizationException.java - Dec 18, 2009 4:00:06 PM
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

package net.solarnetwork.security;

import java.util.Arrays;

/**
 * Exception thrown when authorization to some resource fails.
 * 
 * @author matt
 * @version 1.0
 * @since 1.60
 */
public class AuthorizationException extends SecurityException {

	private static final long serialVersionUID = -7269908721527606492L;

	/** Authorization exception reason. */
	public enum Reason {

		/** Bad password. */
		BAD_PASSWORD,

		/** Unknown email. */
		UNKNOWN_EMAIL,

		/** Duplicate email. */
		DUPLICATE_EMAIL,

		/** Registration not confirmed. */
		REGISTRATION_NOT_CONFIRMED,

		/** Registration already confirmed. */
		REGISTRATION_ALREADY_CONFIRMED,

		/** Forgotten password not confirmed. */
		FORGOTTEN_PASSWORD_NOT_CONFIRMED,

		/** Access denied to something. */
		ACCESS_DENIED,

		/** Access for anonymous users denied. */
		ANONYMOUS_ACCESS_DENIED,

		/** Access was requested to an unknown object. */
		UNKNOWN_OBJECT,
	}

	/** The reason. */
	private final Reason reason;

	/** The email. */
	private final String email;

	/** The ID. */
	private final Object id;

	/**
	 * Construct authorization exception.
	 * 
	 * @param username
	 *        the attempted login
	 * @param reason
	 *        the reason for the exception
	 */
	public AuthorizationException(String username, Reason reason) {
		super();
		this.reason = reason;
		this.email = username;
		this.id = null;
	}

	/**
	 * Construct authorization exception related to some primary key
	 * 
	 * @param reason
	 *        the reason for the exception
	 * @param id
	 *        the object ID
	 */
	public AuthorizationException(Reason reason, Object id) {
		super();
		this.reason = reason;
		this.email = null;
		this.id = id;
	}

	/**
	 * Get the attempted login.
	 * 
	 * @return login value (or {@literal null} if not available)
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Get the primary key.
	 * 
	 * @return the primary key (or {@literal null} if not available)
	 */
	public Object getId() {
		return id;
	}

	/**
	 * Get the authorization exception reason.
	 * 
	 * @return reason
	 */
	public Reason getReason() {
		return reason;
	}

	@Override
	public String getMessage() {
		return (reason == null ? null
				: reason.toString() + " [" + (email == null
						? (id != null && id.getClass().isArray() ? Arrays.toString((Object[]) id) : id)
						: email) + "]");
	}

}

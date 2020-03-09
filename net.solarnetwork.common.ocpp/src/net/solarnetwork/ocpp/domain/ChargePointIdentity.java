/* ==================================================================
 * ChargePointIdentity.java - 27/02/2020 9:53:36 am
 * 
 * Copyright 2020 SolarNetwork.net Dev Team
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

package net.solarnetwork.ocpp.domain;

import java.util.Objects;

/**
 * A unique identity for a charge point in SolarNetwork.
 * 
 * @author matt
 * @version 1.0
 */
public class ChargePointIdentity implements Comparable<ChargePointIdentity> {

	/**
	 * A username constant for the concept of "any user".
	 * 
	 * <p>
	 * This username can be used in contexts where charge points can be uniquely
	 * identified by their {@link #getIdentifier()} values alone, such as in
	 * SolarNode.
	 * </p>
	 */
	public static final String ANY_USERNAME = "";

	private final String identifier;
	private final String username;

	/**
	 * Constructor.
	 * 
	 * @param identifier
	 *        the charge point identifier
	 * @param username
	 *        the username
	 * @throws IllegalArgumentException
	 *         if any parameter is {@literal null}
	 */
	public ChargePointIdentity(String identifier, String username) {
		super();
		if ( identifier == null ) {
			throw new IllegalArgumentException("The identifier parameter must not be null.");
		}
		this.identifier = identifier;
		if ( username == null ) {
			throw new IllegalArgumentException("The username parameter must not be null.");
		}
		this.username = username;
	}

	@Override
	public int hashCode() {
		return Objects.hash(identifier, username);
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( !(obj instanceof ChargePointIdentity) ) {
			return false;
		}
		ChargePointIdentity other = (ChargePointIdentity) obj;
		return Objects.equals(identifier, other.identifier) && Objects.equals(username, other.username);
	}

	@Override
	public int compareTo(ChargePointIdentity o) {
		int result = identifier.compareTo(o.identifier);
		if ( result == 0 ) {
			result = username.compareTo(o.username);
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ChargePointIdentity{identifier=");
		builder.append(identifier);
		builder.append(", username=");
		builder.append(username);
		builder.append("}");
		return builder.toString();
	}

	/**
	 * Get the charge point identifier.
	 * 
	 * @return the identifier; never {@literal null}
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Get the system user username.
	 * 
	 * @return the username; never {@literal null}
	 */
	public String getUsername() {
		return username;
	}

}

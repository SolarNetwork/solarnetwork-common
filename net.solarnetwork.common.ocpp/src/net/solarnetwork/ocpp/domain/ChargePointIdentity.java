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
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A unique identity for a charge point in SolarNetwork.
 * 
 * @author matt
 * @version 1.2
 */
public class ChargePointIdentity implements Comparable<ChargePointIdentity> {

	/**
	 * A user identifier constant for the concept of "any user".
	 * 
	 * <p>
	 * This username can be used in contexts where charge points can be uniquely
	 * identified by their {@link #getIdentifier()} values alone, such as in
	 * SolarNode.
	 * </p>
	 */
	public static final String ANY_USER = "";

	private final String identifier;
	private final Object userIdentifier;

	/**
	 * Constructor.
	 * 
	 * @param identifier
	 *        the charge point identifier
	 * @param userIdentifier
	 *        a unique identifier for the charge point account owner; this
	 *        object should implement {@link Comparable} and have proper
	 *        {@link Object#hashCode()} and {@link Object#equals(Object)}
	 *        support; all {@code java.lang.Integer} instances will be converted
	 *        to {@code java.lang.Long} values
	 * @throws IllegalArgumentException
	 *         if any parameter is {@literal null}
	 */
	@JsonCreator
	public ChargePointIdentity(@JsonProperty("identifier") String identifier,
			@JsonProperty("userIdentifier") Object userIdentifier) {
		super();
		if ( identifier == null ) {
			throw new IllegalArgumentException("The identifier parameter must not be null.");
		}
		this.identifier = identifier;
		if ( userIdentifier == null ) {
			throw new IllegalArgumentException("The userIdentifier parameter must not be null.");
		}
		// because JSON might parse numbers as Integers but in SN all users are Longs, we normalize
		// Integer to Long here so that equals() and such work as expected
		this.userIdentifier = (userIdentifier instanceof Integer ? ((Integer) userIdentifier).longValue()
				: userIdentifier);
	}

	@Override
	public int hashCode() {
		return Objects.hash(identifier, userIdentifier);
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
		return Objects.equals(identifier, other.identifier)
				&& Objects.equals(userIdentifier, other.userIdentifier);
	}

	@SuppressWarnings("unchecked")
	@Override
	public int compareTo(ChargePointIdentity o) {
		int result = identifier.compareTo(o.identifier);
		if ( result == 0 ) {
			if ( userIdentifier instanceof Comparable<?> ) {
				result = ((Comparable<Object>) userIdentifier).compareTo(o.userIdentifier);
			} else {
				String u1 = userIdentifier.toString();
				String u2 = o.userIdentifier.toString();
				result = u1.compareTo(u2);
			}
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ChargePointIdentity{identifier=");
		builder.append(identifier);
		builder.append(", userIdentifier=");
		builder.append(userIdentifier);
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
	 * Get the user identifier.
	 * 
	 * @return the user identifier; never {@literal null}
	 */
	public Object getUserIdentifier() {
		return userIdentifier;
	}

}

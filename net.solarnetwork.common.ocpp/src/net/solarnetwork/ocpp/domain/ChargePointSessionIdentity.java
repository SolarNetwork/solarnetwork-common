/* ==================================================================
 * ChargePointSessionIdentity.java - 4/07/2024 9:47:15 am
 *
 * Copyright 2024 SolarNetwork.net Dev Team
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
import net.solarnetwork.util.ObjectUtils;

/**
 * A unique identity for a charge point session in SolarNetwork.
 *
 * <p>
 * This identity supports a charge point associated with a "session" identifier.
 * A "session" could be something like a web socket session ID.
 * </p>
 *
 * @author matt
 * @version 1.0
 * @since 4.3
 */
public class ChargePointSessionIdentity implements Comparable<ChargePointSessionIdentity> {

	private final ChargePointIdentity identity;
	private final String sessionId;

	/**
	 * Constructor.
	 *
	 * @param identity
	 *        the charge point identity
	 * @param sessionId
	 *        the session ID
	 * @throws IllegalArgumentException
	 *         if any parameter is {@literal null}
	 */
	public ChargePointSessionIdentity(ChargePointIdentity identity, String sessionId) {
		super();
		this.identity = ObjectUtils.requireNonNullArgument(identity, "identity");
		this.sessionId = ObjectUtils.requireNonNullArgument(sessionId, "sessionId");
	}

	/**
	 * Create a boundary key for a given identity, where the session ID will be
	 * set to an empty string.
	 *
	 * @param identity
	 *        the identity
	 * @return the new instance
	 * @throws IllegalArgumentException
	 *         if any parameter is {@literal null}
	 */
	public static ChargePointSessionIdentity boundaryKey(ChargePointIdentity identity) {
		return new ChargePointSessionIdentity(identity, "");
	}

	@Override
	public int hashCode() {
		return Objects.hash(identity, sessionId);
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( !(obj instanceof ChargePointSessionIdentity) ) {
			return false;
		}
		ChargePointSessionIdentity other = (ChargePointSessionIdentity) obj;
		return Objects.equals(identity, other.identity) && Objects.equals(sessionId, other.sessionId);
	}

	@Override
	public int compareTo(ChargePointSessionIdentity o) {
		int result = identity.compareTo(o.identity);
		if ( result == 0 ) {
			result = sessionId.compareTo(o.sessionId);
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ChargePointSessionIdentity{identifier=");
		builder.append(identity.getIdentifier());
		builder.append(", userIdentifier=");
		builder.append(identity.getUserIdentifier());
		builder.append(", sessionId=");
		builder.append(sessionId);
		builder.append("}");
		return builder.toString();
	}

	/**
	 * Get the charge point identifier.
	 *
	 * @return the identifier; never {@literal null}
	 */
	public String getIdentifier() {
		return identity.getIdentifier();
	}

	/**
	 * Get the user identifier.
	 *
	 * @return the user identifier; never {@literal null}
	 */
	public Object getUserIdentifier() {
		return identity.getUserIdentifier();
	}

	/**
	 * Get the identity.
	 *
	 * @return the identity; never {@literal null}
	 */
	public final ChargePointIdentity getIdentity() {
		return identity;
	}

	/**
	 * Get the session ID
	 *
	 * @return the sessionId; never {@literal null}
	 */
	public final String getSessionId() {
		return sessionId;
	}

}

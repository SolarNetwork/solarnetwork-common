/* ==================================================================
 * ChargePointConnectorKey.java - 12/02/2020 4:01:00 pm
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

import java.io.Serializable;
import java.util.Objects;

/**
 * A primary key for a Charge Point connector.
 * 
 * @author matt
 * @version 1.0
 */
public class ChargePointConnectorKey
		implements Serializable, Cloneable, Comparable<ChargePointConnectorKey> {

	private static final long serialVersionUID = 6544054010677060649L;

	/** The charge point ID. */
	private final long chargePointId;

	/** The connector ID. */
	private final int connectorId;

	/**
	 * Create a new key instance.
	 * 
	 * @param chargePointId
	 *        the Charge Point ID
	 * @param connectorId
	 *        the connector ID
	 * @return the new key
	 * @throws IllegalArgumentException
	 *         if {@code chargePointId} is {@literal null}
	 */
	public static ChargePointConnectorKey keyFor(long chargePointId, int connectorId) {
		return new ChargePointConnectorKey(chargePointId, connectorId);
	}

	/**
	 * Constructor.
	 * 
	 * @param chargePointId
	 *        the Charge Point ID
	 * @param connectorId
	 *        the connector ID
	 * @throws IllegalArgumentException
	 *         if {@code chargePointId} is {@literal null}
	 */
	public ChargePointConnectorKey(long chargePointId, int connectorId) {
		super();
		this.chargePointId = chargePointId;
		this.connectorId = connectorId;
	}

	@Override
	public int compareTo(ChargePointConnectorKey o) {
		int result = Long.compare(chargePointId, o.chargePointId);
		if ( result == 0 ) {
			result = Integer.compare(connectorId, o.connectorId);
		}
		return result;
	}

	@Override
	public ChargePointConnectorKey clone() {
		try {
			return (ChargePointConnectorKey) super.clone();
		} catch ( CloneNotSupportedException e ) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(chargePointId, connectorId);
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( !(obj instanceof ChargePointConnectorKey) ) {
			return false;
		}
		ChargePointConnectorKey other = (ChargePointConnectorKey) obj;
		return Objects.equals(chargePointId, other.chargePointId) && connectorId == other.connectorId;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ChargePointConnectorKey{chargePointId=");
		builder.append(chargePointId);
		builder.append(", connectorId=");
		builder.append(connectorId);
		builder.append("}");
		return builder.toString();
	}

	/**
	 * Get the Charge Point ID.
	 * 
	 * @return the chargePointId the Charge Point ID, never {@literal null}
	 */
	public long getChargePointId() {
		return chargePointId;
	}

	/**
	 * Get the connector ID.
	 * 
	 * @return the connector ID
	 */
	public int getConnectorId() {
		return connectorId;
	}

}

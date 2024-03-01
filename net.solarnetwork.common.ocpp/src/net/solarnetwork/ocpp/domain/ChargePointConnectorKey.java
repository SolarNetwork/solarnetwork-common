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
 * @version 1.1
 */
public class ChargePointConnectorKey
		implements Serializable, Cloneable, Comparable<ChargePointConnectorKey> {

	private static final long serialVersionUID = 1143142263454730088L;

	/** The charge point ID. */
	private final long chargePointId;

	/** The EVSE ID. */
	private final int evseId;

	/** The connector ID. */
	private final int connectorId;

	/**
	 * Create a new key instance.
	 * 
	 * <p>
	 * The EVSE ID will be set to {@literal 0}.
	 * </p>
	 * 
	 * @param chargePointId
	 *        the Charge Point ID
	 * @param connectorId
	 *        the connector ID
	 * @return the new key
	 */
	public static ChargePointConnectorKey keyFor(long chargePointId, int connectorId) {
		return new ChargePointConnectorKey(chargePointId, connectorId);
	}

	/**
	 * Create a new key instance.
	 * 
	 * <p>
	 * The EVSE ID will be set to {@literal 0}.
	 * </p>
	 * 
	 * @param chargePointId
	 *        the Charge Point ID
	 * @param evseId
	 *        the EVSE ID
	 * @param connectorId
	 *        the connector ID
	 * @return the new key
	 * @since 1.1
	 */
	public static ChargePointConnectorKey keyFor(long chargePointId, int evseId, int connectorId) {
		return new ChargePointConnectorKey(chargePointId, evseId, connectorId);
	}

	/**
	 * Constructor.
	 * 
	 * <p>
	 * The EVSE ID will be set to {@literal 0}.
	 * </p>
	 * 
	 * @param chargePointId
	 *        the Charge Point ID
	 * @param connectorId
	 *        the connector ID
	 */
	public ChargePointConnectorKey(long chargePointId, int connectorId) {
		this(chargePointId, 0, connectorId);
	}

	/**
	 * Constructor.
	 * 
	 * @param chargePointId
	 *        the Charge Point ID
	 * @param evseId
	 *        the EVSE ID
	 * @param connectorId
	 *        the connector ID
	 * @since 1.1
	 */
	public ChargePointConnectorKey(long chargePointId, int evseId, int connectorId) {
		super();
		this.chargePointId = chargePointId;
		this.evseId = evseId;
		this.connectorId = connectorId;
	}

	@Override
	public int compareTo(ChargePointConnectorKey o) {
		int result = Long.compare(chargePointId, o.chargePointId);
		if ( result == 0 ) {
			result = Integer.compare(evseId, o.evseId);
		}
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
		return Objects.hash(chargePointId, evseId, connectorId);
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
		return Objects.equals(chargePointId, other.chargePointId) && evseId == other.evseId
				&& connectorId == other.connectorId;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ChargePointConnectorKey{chargePointId=");
		builder.append(chargePointId);
		builder.append(", evseId=");
		builder.append(evseId);
		builder.append(", connectorId=");
		builder.append(connectorId);
		builder.append("}");
		return builder.toString();
	}

	/**
	 * Get the Charge Point ID.
	 * 
	 * @return the Charge Point ID
	 */
	public long getChargePointId() {
		return chargePointId;
	}

	/**
	 * Get the EVSE ID.
	 * 
	 * @return the EVSE ID
	 * @since 1.1
	 */
	public int getEvseId() {
		return evseId;
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

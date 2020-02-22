/* ==================================================================
 * ChargePointConnector.java - 12/02/2020 4:00:44 pm
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

import java.time.Instant;
import net.solarnetwork.dao.BasicEntity;

/**
 * A Charge Point connector entity.
 * 
 * <p>
 * A connector ID of {@literal 0} represents the Charge Point as a whole.
 * </p>
 * 
 * @author matt
 * @version 1.0
 */
public class ChargePointConnector extends BasicEntity<ChargePointConnectorKey> {

	private StatusNotification info;

	/**
	 * Constructor.
	 */
	public ChargePointConnector() {
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *        the primary key
	 */
	public ChargePointConnector(ChargePointConnectorKey id) {
		this(id, Instant.now());
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *        the primary key
	 * @param created
	 *        the created date
	 */
	public ChargePointConnector(ChargePointConnectorKey id, Instant created) {
		super(id, created);
	}

	/**
	 * Copy constructor.
	 * 
	 * @param other
	 *        the other charge point to copy
	 */
	public ChargePointConnector(ChargePointConnector other) {
		this(other.getId(), other.getCreated());
		this.info = other.info;
	}

	/**
	 * Test if the properties of another entity are the same as in this
	 * instance.
	 * 
	 * <p>
	 * The {@code id} and {@code created} properties are not compared by this
	 * method.
	 * </p>
	 * 
	 * @param other
	 *        the other entity to compare to
	 * @return {@literal true} if the properties of this instance are equal to
	 *         the other
	 */
	public boolean isSameAs(ChargePointConnector other) {
		if ( other == null ) {
			return false;
		}
		// @formatter:off
		return info != null && info.isSameAs(other.info);
		// @formatter:on
	}

	/**
	 * Get the status info.
	 * 
	 * @return the info
	 */
	public StatusNotification getInfo() {
		return info;
	}

	/**
	 * Set the status info.
	 * 
	 * @param info
	 *        the info to set
	 * @throws IllegalArgumentException
	 *         if {@link StatusNotification#getConnectorId()} does not match the
	 *         {@link ChargePointConnectorKey#getConnectorId()} in
	 *         {@link #getId()}
	 */
	public void setInfo(StatusNotification info) {
		if ( info != null && info.getConnectorId() != getId().getConnectorId() ) {
			throw new IllegalArgumentException(
					"The info->connectorId must not differ from this object's id->connectorId.");
		}
		this.info = info;
	}

}

/* ==================================================================
 * ChargePoint.java - 7/02/2020 7:56:29 am
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
import java.util.Objects;
import net.solarnetwork.dao.BasicLongEntity;
import net.solarnetwork.domain.Differentiable;

/**
 * A Charge Point entity.
 * 
 * @author matt
 * @version 1.3
 */
public class ChargePoint extends BasicLongEntity implements Differentiable<ChargePoint> {

	private static final long serialVersionUID = -5780143529087352852L;

	/** The charge point information. */
	private final ChargePointInfo info;

	/** The registration status. */
	private RegistrationStatus registrationStatus;

	/** The enabled status. */
	private boolean enabled;

	/** The connector count. */
	private int connectorCount;

	/**
	 * Constructor.
	 */
	public ChargePoint() {
		super();
		this.info = new ChargePointInfo();
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *        the primary key
	 */
	public ChargePoint(Long id) {
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
	public ChargePoint(Long id, Instant created) {
		this(id, created, new ChargePointInfo());
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *        the primary key
	 * @param created
	 *        the created date
	 * @param info
	 *        the info
	 * @throws IllegalArgumentException
	 *         if {@code info} is {@literal null}
	 */
	public ChargePoint(Long id, Instant created, ChargePointInfo info) {
		super(id, created);
		if ( info == null ) {
			throw new IllegalArgumentException("The info parameter must not be null.");
		}
		setRegistrationStatus(RegistrationStatus.Pending);
		this.info = info;
	}

	/**
	 * Constructor.
	 * 
	 * @param created
	 *        the created date
	 * @param identifier
	 *        the charge point ID
	 * @param chargePointVendor
	 *        the vendor
	 * @param chargePointModel
	 *        the model
	 */
	public ChargePoint(Instant created, String identifier, String chargePointVendor,
			String chargePointModel) {
		this(null, created, new ChargePointInfo(identifier, chargePointVendor, chargePointModel));
	}

	/**
	 * Copy constructor.
	 * 
	 * @param other
	 *        the other charge point to copy
	 */
	public ChargePoint(ChargePoint other) {
		this(other.getId(), other.getCreated(), new ChargePointInfo(other.getInfo()));
		this.registrationStatus = other.registrationStatus;
		this.enabled = other.enabled;
		this.connectorCount = other.connectorCount;
	}

	/**
	 * Create a charge point identity based on this entity.
	 * 
	 * <p>
	 * This implementation uses {@link ChargePointIdentity#ANY_USER} for the
	 * resolved username.
	 * </p>
	 * 
	 * @return the new identity, never {@literal null}
	 */
	public ChargePointIdentity chargePointIdentity() {
		return new ChargePointIdentity(getInfo().getId(), ChargePointIdentity.ANY_USER);
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
	public boolean isSameAs(ChargePoint other) {
		if ( other == null ) {
			return false;
		}
		// @formatter:off
		return (info.isSameAs(other.info))
				&& Objects.equals(registrationStatus, other.registrationStatus)
				&& enabled == other.enabled
				&& connectorCount == other.connectorCount;
		// @formatter:on
	}

	@Override
	public boolean differsFrom(ChargePoint other) {
		return !isSameAs(other);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ChargePoint{");
		if ( getId() != null ) {
			builder.append("id=");
			builder.append(getId());
			builder.append(", ");
		}
		if ( registrationStatus != null ) {
			builder.append("registrationStatus=");
			builder.append(registrationStatus);
			builder.append(", ");
		}
		builder.append("enabled=");
		builder.append(enabled);
		builder.append(", connectorCount=");
		builder.append(connectorCount);
		builder.append(", info=");
		builder.append(info);
		builder.append("}");
		return builder.toString();
	}

	/**
	 * Copy the properties of a {@link ChargePointInfo}.
	 * 
	 * @param info
	 *        the properties to copy
	 */
	public void copyInfoFrom(ChargePointInfo info) {
		this.info.copyFrom(info);
	}

	/**
	 * Get the Charge Point information.
	 * 
	 * @return the info; never {@literal null}
	 */
	public ChargePointInfo getInfo() {
		return info;
	}

	/**
	 * Get the enabled flag.
	 * 
	 * @return the enabled flag
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Set the enabled flag.
	 * 
	 * @param enabled
	 *        the enabled flag to set
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Get the registration status.
	 * 
	 * @return the registrationStatus
	 */
	public RegistrationStatus getRegistrationStatus() {
		return registrationStatus;
	}

	/**
	 * Set the registration status.
	 * 
	 * @param registrationStatus
	 *        the registrationStatus to set
	 */
	public void setRegistrationStatus(RegistrationStatus registrationStatus) {
		this.registrationStatus = registrationStatus;
	}

	/**
	 * Get the total number of connectors available on this charge point.
	 * 
	 * @return the total number of connectors, or {@literal 0} if not known
	 */
	public int getConnectorCount() {
		return connectorCount;
	}

	/**
	 * Set the total number of connectors on this charge point.
	 * 
	 * @param connectorCount
	 *        the count to set, or {@literal 0} if not known
	 */
	public void setConnectorCount(int connectorCount) {
		this.connectorCount = connectorCount;
	}

}

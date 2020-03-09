/* ==================================================================
 * ChargingProfile.java - 18/02/2020 3:15:08 pm
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
import java.util.UUID;
import net.solarnetwork.dao.BasicUuidEntity;
import net.solarnetwork.domain.Differentiable;

/**
 * An entity for an OCPP charging profile, which represents charging constraints
 * to apply over a time period.
 * 
 * @author matt
 * @version 1.0
 */
public class ChargingProfile extends BasicUuidEntity implements Differentiable<ChargingProfile> {

	/** The default purpose. */
	public static final ChargingProfilePurpose DEFAULT_PURPOSE = ChargingProfilePurpose.ChargePointMaxProfile;

	/** The default profile kind. */
	public static final ChargingProfileKind DEFAULT_KIND = ChargingProfileKind.Recurring;

	/** The default rate unit. */
	public static final UnitOfMeasure DEFAULT_RATE_UNIT = UnitOfMeasure.W;

	private ChargingProfileInfo info;

	/**
	 * Constructor.
	 */
	public ChargingProfile() {
		super();
		info = new ChargingProfileInfo(DEFAULT_PURPOSE, DEFAULT_KIND,
				new ChargingScheduleInfo(DEFAULT_RATE_UNIT));
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *        the primary key
	 */
	public ChargingProfile(UUID id) {
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
	public ChargingProfile(UUID id, Instant created) {
		this(id, created, new ChargingProfileInfo(DEFAULT_PURPOSE, DEFAULT_KIND,
				new ChargingScheduleInfo(DEFAULT_RATE_UNIT)));
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *        the primary key
	 * @param created
	 *        the created date
	 * @param info
	 *        the info * @throws IllegalArgumentException if {@code info} is
	 *        {@literal null} Ill
	 */
	public ChargingProfile(UUID id, Instant created, ChargingProfileInfo info) {
		super(id, created);
		setInfo(info);
	}

	/**
	 * Copy constructor.
	 * 
	 * @param other
	 *        the other charge point to copy
	 */
	public ChargingProfile(ChargingProfile other) {
		this(other.getId(), other.getCreated());
		this.info = new ChargingProfileInfo(other.info);
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
	public boolean isSameAs(ChargingProfile other) {
		if ( other == null ) {
			return false;
		}
		return info.isSameAs(other.info);
	}

	@Override
	public boolean differsFrom(ChargingProfile other) {
		return !isSameAs(other);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ChargingProfile{");
		if ( getId() != null ) {
			builder.append("id=");
			builder.append(getId());
		}
		if ( info != null ) {
			builder.append("info=");
			builder.append(info);
			builder.append(", ");
		}
		builder.append("}");
		return builder.toString();
	}

	/**
	 * Get the charging profile info.
	 * 
	 * @return the info, never {@literal null}
	 */
	public ChargingProfileInfo getInfo() {
		return info;
	}

	/**
	 * Set the charging profile info.
	 * 
	 * @param info
	 *        the info to set
	 * @throws IllegalArgumentException
	 *         if {@code info} is {@literal null}
	 */
	public void setInfo(ChargingProfileInfo info) {
		if ( info == null ) {
			throw new IllegalArgumentException("The info parameter must not be null.");
		}
		this.info = info;
	}

}

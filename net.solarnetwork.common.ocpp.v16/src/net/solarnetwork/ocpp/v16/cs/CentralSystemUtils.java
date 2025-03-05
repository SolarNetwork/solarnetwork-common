/* ==================================================================
 * CentralSystemUtils.java - 14/02/2020 2:23:29 pm
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

package net.solarnetwork.ocpp.v16.cs;

import java.time.Instant;
import java.util.UUID;
import net.solarnetwork.ocpp.domain.AuthorizationStatus;
import net.solarnetwork.ocpp.domain.Location;
import net.solarnetwork.ocpp.domain.Measurand;
import net.solarnetwork.ocpp.domain.Phase;
import net.solarnetwork.ocpp.domain.ReadingContext;
import net.solarnetwork.ocpp.domain.SampledValue;
import net.solarnetwork.ocpp.domain.UnitOfMeasure;

/**
 * Common utilities for a Central System.
 *
 * @author matt
 * @version 1.1
 */
public final class CentralSystemUtils {

	private CentralSystemUtils() {
		super();
	}

	/**
	 * Get a {@link ocpp.v16.cs.AuthorizationStatus} for an
	 * {@link AuthorizationStatus}.
	 *
	 * @param status
	 *        the status to translate
	 * @return the status, never {@literal null}
	 */
	public static ocpp.v16.cs.AuthorizationStatus statusForStatus(AuthorizationStatus status) {
		switch (status) {
			case Accepted:
				return ocpp.v16.cs.AuthorizationStatus.ACCEPTED;

			case Blocked:
				return ocpp.v16.cs.AuthorizationStatus.BLOCKED;

			case ConcurrentTx:
				return ocpp.v16.cs.AuthorizationStatus.CONCURRENT_TX;

			case Expired:
				return ocpp.v16.cs.AuthorizationStatus.EXPIRED;

			default:
				return ocpp.v16.cs.AuthorizationStatus.INVALID;
		}
	}

	/**
	 * Convert a {@link ocpp.v16.cs.SampledValue} into a {@link SampledValue}.
	 *
	 * @param chargeSessionId
	 *        the charge session ID associated with the sample
	 * @param timestamp
	 *        the timestamp associated with the sample
	 * @param value
	 *        the value to translate
	 * @return the value, never {@literal null}
	 */
	public static SampledValue sampledValue(UUID chargeSessionId, Instant timestamp,
			ocpp.v16.cs.SampledValue value) {
		// @formatter:off
		SampledValue.Builder result = SampledValue.builder()
				.withSessionId(chargeSessionId)
				.withTimestamp(timestamp)
				.withContext(readingContext(value.getContext()))
				.withLocation(location(value.getLocation()))
				.withMeasurand(measurand(value.getMeasurand()))
				.withPhase(phase(value.getPhase()))
				.withUnit(unit(value.getUnit()))
				.withValue(value.getValue());
		// @formatter:on
		return result.build();
	}

	/**
	 * Convert a {@link ocpp.v16.cs.UnitOfMeasure} into a {@link UnitOfMeasure}.
	 *
	 * @param unit
	 *        the unit to translate
	 * @return the unit, never {@literal null}
	 */
	@SuppressWarnings("deprecation")
	public static UnitOfMeasure unit(ocpp.v16.cs.UnitOfMeasure unit) {
		// handle OCPP-J errata 2.1: typo in Celsius unit
		if ( ocpp.v16.cs.UnitOfMeasure.CELCIUS == unit ) {
			return UnitOfMeasure.Celsius;
		}
		try {
			return UnitOfMeasure.valueOf(unit.value());
		} catch ( IllegalArgumentException | NullPointerException e ) {
			return UnitOfMeasure.Unknown;
		}
	}

	/**
	 * Convert a {@link ocpp.v16.cs.Phase} into a {@link Phase}.
	 *
	 * @param phase
	 *        the phase to translate
	 * @return the phase, never {@literal null}
	 */
	public static Phase phase(ocpp.v16.cs.Phase phase) {
		if ( phase == null ) {
			return null;
		}
		try {
			return Phase.valueOf(phase.value().replace("-", ""));
		} catch ( IllegalArgumentException | NullPointerException e ) {
			return Phase.Unknown;
		}
	}

	/**
	 * Convert a {@link ocpp.v16.cs.Measurand} into a {@link Measurand}.
	 *
	 * @param measurand
	 *        the measurand to translate
	 * @return the measurand, never {@literal null}
	 */
	public static Measurand measurand(ocpp.v16.cs.Measurand measurand) {
		try {
			return Measurand.valueOf(measurand.value().replace(".", ""));
		} catch ( IllegalArgumentException | NullPointerException e ) {
			return Measurand.Unknown;
		}
	}

	/**
	 * Convert a {@link ocpp.v16.cs.Location} into a {@link Location}.
	 *
	 * @param location
	 *        the location to translate
	 * @return the location, never {@literal null}
	 */
	public static Location location(ocpp.v16.cs.Location location) {
		try {
			return Location.valueOf(location.value());
		} catch ( IllegalArgumentException | NullPointerException e ) {
			return Location.Outlet;
		}

	}

	/**
	 * Convert a {@link ocpp.v16.cs.ReadingContext} into a
	 * {@link ReadingContext}.
	 *
	 * @param context
	 *        the context to translate
	 * @return the context, never {@literal null}
	 */
	public static ReadingContext readingContext(ocpp.v16.cs.ReadingContext context) {
		try {
			return ReadingContext.valueOf(context.value().replace(".", ""));
		} catch ( IllegalArgumentException | NullPointerException e ) {
			return ReadingContext.Unknown;
		}
	}

}

/* ==================================================================
 * ChargingProfileInfo.java - 18/02/2020 3:17:04 pm
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;
import net.solarnetwork.domain.Differentiable;
import net.solarnetwork.util.DateUtils;

/**
 * Information about a charging profile.
 * 
 * @author matt
 * @version 1.0
 */
public class ChargingProfileInfo implements Differentiable<ChargingProfileInfo> {

	private ChargingProfilePurpose purpose;
	private ChargingProfileKind kind;
	private ChargingScheduleRecurrency recurrency;
	private Instant validFrom;
	private Instant validTo;
	private ChargingScheduleInfo schedule;

	/**
	 * Constructor.
	 * 
	 * @param purpose
	 *        the purpose
	 * @param kind
	 *        the kind
	 * @param schedule
	 *        the schedule
	 * @throws IllegalArgumentException
	 *         if {@code purpose}, {@code kind}, or {@code schedule} are
	 *         {@literal null}
	 */
	public ChargingProfileInfo(ChargingProfilePurpose purpose, ChargingProfileKind kind,
			ChargingScheduleInfo schedule) {
		super();
		setPurpose(purpose);
		setKind(kind);
		setSchedule(schedule);
	}

	/**
	 * Constructor.
	 * 
	 * @param purpose
	 *        the purpose
	 * @param kind
	 *        the kind
	 * @param recurrency
	 *        the recurrency
	 * @param validFrom
	 *        the valid from date
	 * @param validTo
	 *        the valid to date
	 * @param schedule
	 *        the schedule
	 * @throws IllegalArgumentException
	 *         if {@code purpose}, {@code kind}, or {@code schedule} are
	 *         {@literal null}
	 */
	public ChargingProfileInfo(ChargingProfilePurpose purpose, ChargingProfileKind kind,
			ChargingScheduleRecurrency recurrency, Instant validFrom, Instant validTo,
			ChargingScheduleInfo schedule) {
		this(purpose, kind, schedule);
		this.recurrency = recurrency;
		this.validFrom = validFrom;
		this.validTo = validTo;
	}

	/**
	 * Copy constructor.
	 * 
	 * @param other
	 *        the info to copy
	 */
	public ChargingProfileInfo(ChargingProfileInfo other) {
		this(other.purpose, other.kind, other.recurrency, other.validFrom, other.validTo,
				new ChargingScheduleInfo(other.schedule));
	}

	/**
	 * Test if the properties of another entity are the same as in this
	 * instance.
	 * 
	 * @param other
	 *        the other entity to compare to
	 * @return {@literal true} if the properties of this instance are equal to
	 *         the other
	 */
	public boolean isSameAs(ChargingProfileInfo other) {
		if ( other == null ) {
			return false;
		}
		// @formatter:off
		return Objects.equals(purpose, other.purpose)
				&& Objects.equals(kind, other.kind)
				&& Objects.equals(recurrency, other.recurrency)
				&& Objects.equals(validFrom, other.validFrom)
				&& Objects.equals(validTo, other.validTo)
				&& schedule.isSameAs(other.schedule);
		// @formatter:on
	}

	@Override
	public boolean differsFrom(ChargingProfileInfo other) {
		return !isSameAs(other);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ChargingProfileInfo{");
		if ( purpose != null ) {
			builder.append("purpose=");
			builder.append(purpose);
			builder.append(", ");
		}
		if ( kind != null ) {
			builder.append("kind=");
			builder.append(kind);
			builder.append(", ");
		}
		if ( recurrency != null ) {
			builder.append("recurrency=");
			builder.append(recurrency);
			builder.append(", ");
		}
		if ( validFrom != null ) {
			builder.append("validFrom=");
			builder.append(validFrom);
			builder.append(", ");
		}
		if ( validTo != null ) {
			builder.append("validTo=");
			builder.append(validTo);
			builder.append(", ");
		}
		if ( schedule != null ) {
			builder.append("schedule=");
			builder.append(schedule);
		}
		builder.append("}");
		return builder.toString();
	}

	/**
	 * Get the purpose.
	 * 
	 * @return the purpose; never {@literal null}
	 */
	public ChargingProfilePurpose getPurpose() {
		return purpose;
	}

	/**
	 * Set the purpose.
	 * 
	 * @param purpose
	 *        the purpose to set
	 * @throws IllegalArgumentException
	 *         if {@code purpose} is {@literal null}
	 */
	public void setPurpose(ChargingProfilePurpose purpose) {
		if ( purpose == null ) {
			throw new IllegalArgumentException("The purpose parameter must not be null.");
		}
		this.purpose = purpose;
	}

	/**
	 * Get the purpose as a code value.
	 * 
	 * @return the purpose code
	 */
	public int getPurposeCode() {
		return getPurpose().getCode();
	}

	/**
	 * Set the purpose as a code value.
	 * 
	 * @param code
	 *        the purpose code
	 */
	public void setPurposeCode(int code) {
		setPurpose(ChargingProfilePurpose.forCode(code));
	}

	/**
	 * Get the profile kind.
	 * 
	 * @return the kind; never {@literal null}
	 */
	public ChargingProfileKind getKind() {
		return kind;
	}

	/**
	 * Set the profile kind.
	 * 
	 * @param kind
	 *        the kind to set
	 * @throws IllegalArgumentException
	 *         if {@code kind} is {@literal null}
	 */
	public void setKind(ChargingProfileKind kind) {
		if ( kind == null ) {
			throw new IllegalArgumentException("The kind parameter must not be null.");
		}
		this.kind = kind;
	}

	/**
	 * Get the profile kind as a code value.
	 * 
	 * @return the kind code
	 */
	public int getKindCode() {
		return getKind().getCode();
	}

	/**
	 * Set the profile kind as a code value.
	 * 
	 * @param code
	 *        the kind code
	 */
	public void setKindCode(int code) {
		setKind(ChargingProfileKind.forCode(code));
	}

	/**
	 * Get the recurrency.
	 * 
	 * @return the recurrency
	 */
	public ChargingScheduleRecurrency getRecurrency() {
		return recurrency;
	}

	/**
	 * Set the recurrency.
	 * 
	 * @param recurrency
	 *        the recurrency to set
	 */
	public void setRecurrency(ChargingScheduleRecurrency recurrency) {
		this.recurrency = recurrency;
	}

	/**
	 * Get the recurrency as a code value.
	 * 
	 * @return the recurrency code
	 */
	public int getRecurrencyCode() {
		ChargingScheduleRecurrency r = getRecurrency();
		if ( r == null ) {
			r = ChargingScheduleRecurrency.Unknown;
		}
		return r.getCode();
	}

	/**
	 * Set the recurrency as a code value.
	 * 
	 * @param code
	 *        the recurrency code
	 */
	public void setRecurrencyCode(int code) {
		setRecurrency(ChargingScheduleRecurrency.forCode(code));
	}

	/**
	 * Get the valid from date.
	 * 
	 * @return the date
	 */
	public Instant getValidFrom() {
		return validFrom;
	}

	/**
	 * Set the valid from date.
	 * 
	 * @param validFrom
	 *        the date to set
	 */
	public void setValidFrom(Instant validFrom) {
		this.validFrom = validFrom;
	}

	/**
	 * Get the valid from date as a formatted instant.
	 * 
	 * @return the date, as an ISO 8601 formatted string
	 */
	public String getValidFromValue() {
		Instant ts = getValidFrom();
		return (ts != null ? DateUtils.ISO_DATE_OPT_TIME_ALT_LOCAL.format(ts) : null);
	}

	/**
	 * Set the valid from date as an ISO 8601 formatted timestamp.
	 * 
	 * @param value
	 *        the date string
	 */
	public void setValidFromValue(String value) {
		Instant ts = null;
		if ( value != null ) {
			ZonedDateTime date = DateUtils.parseIsoAltTimestamp(value, ZoneId.systemDefault());
			if ( date != null ) {
				ts = date.toInstant();
			}
		}
		setValidFrom(ts);
	}

	/**
	 * Get the valid to date.
	 * 
	 * @return the date
	 */
	public Instant getValidTo() {
		return validTo;
	}

	/**
	 * Set the valid to date.
	 * 
	 * @param validTo
	 *        the date to set
	 */
	public void setValidTo(Instant validTo) {
		this.validTo = validTo;
	}

	/**
	 * Get the valid to date as a formatted instant.
	 * 
	 * @return the date, as an ISO 8601 formatted string
	 */
	public String getValidToValue() {
		Instant ts = getValidTo();
		return (ts != null ? DateUtils.ISO_DATE_OPT_TIME_ALT_LOCAL.format(ts) : null);
	}

	/**
	 * Set the valid to date as an ISO 8601 formatted timestamp.
	 * 
	 * @param value
	 *        the date string
	 */
	public void setValidToValue(String value) {
		Instant ts = null;
		if ( value != null ) {
			ZonedDateTime date = DateUtils.parseIsoAltTimestamp(value, ZoneId.systemDefault());
			if ( date != null ) {
				ts = date.toInstant();
			}
		}
		setValidTo(ts);
	}

	/**
	 * Get the schedule.
	 * 
	 * @return the schedule
	 */
	public ChargingScheduleInfo getSchedule() {
		return schedule;
	}

	/**
	 * Set the schedule.
	 * 
	 * @param schedule
	 *        the schedule to set
	 * @throws IllegalArgumentException
	 *         if {@code schedule} is {@literal null}
	 */
	public void setSchedule(ChargingScheduleInfo schedule) {
		if ( schedule == null ) {
			throw new IllegalArgumentException("The schedule parameter must not be null.");
		}
		this.schedule = schedule;
	}

}

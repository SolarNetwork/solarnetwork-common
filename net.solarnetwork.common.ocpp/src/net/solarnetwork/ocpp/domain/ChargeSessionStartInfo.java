/* ==================================================================
 * ChargeSessionStartInfo.java - 14/02/2020 2:03:03 pm
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

/**
 * Information about a charging session, at the start of a session.
 * 
 * @author matt
 * @version 1.0
 */
public class ChargeSessionStartInfo {

	private final ChargePointIdentity chargePointId;
	private final String authorizationId;
	private final int connectorId;
	private final Instant timestampStart;
	private final long meterStart;
	private final Integer reservationId;

	private ChargeSessionStartInfo(Builder builder) {
		this.chargePointId = builder.chargePointId;
		this.authorizationId = builder.authorizationId;
		this.connectorId = builder.connectorId;
		this.timestampStart = builder.timestampStart;
		this.meterStart = builder.meterStart;
		this.reservationId = builder.reservationId;
	}

	@Override
	public String toString() {
		StringBuilder builder2 = new StringBuilder();
		builder2.append("ChargeSessionStartInfo{");
		if ( chargePointId != null ) {
			builder2.append("chargePointId=");
			builder2.append(chargePointId);
			builder2.append(", ");
		}
		if ( authorizationId != null ) {
			builder2.append("authorizationId=");
			builder2.append(authorizationId);
			builder2.append(", ");
		}
		builder2.append("connectorId=");
		builder2.append(connectorId);
		builder2.append(", ");
		if ( timestampStart != null ) {
			builder2.append("timestampStart=");
			builder2.append(timestampStart);
			builder2.append(", ");
		}
		builder2.append("meterStart=");
		builder2.append(meterStart);
		builder2.append(", ");
		if ( reservationId != null ) {
			builder2.append("reservationId=");
			builder2.append(reservationId);
		}
		builder2.append("}");
		return builder2.toString();
	}

	/**
	 * Get the Charge Point ID.
	 * 
	 * @return the Charge Point ID
	 */
	public ChargePointIdentity getChargePointId() {
		return chargePointId;
	}

	/**
	 * Get the authorization ID, e.g. RFID value.
	 * 
	 * @return the authorization ID
	 */
	public String getAuthorizationId() {
		return authorizationId;
	}

	/**
	 * Get the charge point connector ID.
	 * 
	 * @return the connector ID
	 */
	public int getConnectorId() {
		return connectorId;
	}

	/**
	 * Get the timestamp the session started at.
	 * 
	 * @return the starting timestamp
	 */
	public Instant getTimestampStart() {
		return timestampStart;
	}

	/**
	 * Get the meter reading at the time the session started.
	 * 
	 * @return the meter reading, in WH
	 */
	public long getMeterStart() {
		return meterStart;
	}

	/**
	 * Get the optional reservation ID.
	 * 
	 * @return the reservation ID
	 */
	public Integer getReservationId() {
		return reservationId;
	}

	/**
	 * Get a builder, populated with this instance's values.
	 * 
	 * @return a pre-populated builder
	 */
	public Builder toBuilder() {
		return new Builder(this);
	}

	/**
	 * Creates builder to build {@link ChargeSessionStartInfo}.
	 * 
	 * @return created builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder to build {@link ChargeSessionStartInfo}.
	 */
	public static final class Builder {

		private ChargePointIdentity chargePointId;
		private String authorizationId;
		private int connectorId;
		private Instant timestampStart;
		private long meterStart;
		private Integer reservationId;

		private Builder() {
		}

		private Builder(ChargeSessionStartInfo chargeSessionInfo) {
			this.chargePointId = chargeSessionInfo.chargePointId;
			this.authorizationId = chargeSessionInfo.authorizationId;
			this.connectorId = chargeSessionInfo.connectorId;
			this.timestampStart = chargeSessionInfo.timestampStart;
			this.meterStart = chargeSessionInfo.meterStart;
			this.reservationId = chargeSessionInfo.reservationId;
		}

		public Builder withChargePointId(ChargePointIdentity chargePointId) {
			this.chargePointId = chargePointId;
			return this;
		}

		public Builder withAuthorizationId(String authorizationId) {
			this.authorizationId = authorizationId;
			return this;
		}

		public Builder withConnectorId(int connectorId) {
			this.connectorId = connectorId;
			return this;
		}

		public Builder withTimestampStart(Instant timestampStart) {
			this.timestampStart = timestampStart;
			return this;
		}

		public Builder withMeterStart(long meterStart) {
			this.meterStart = meterStart;
			return this;
		}

		public Builder withReservationId(Integer reservationId) {
			this.reservationId = reservationId;
			return this;
		}

		public ChargeSessionStartInfo build() {
			return new ChargeSessionStartInfo(this);
		}
	}

}

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
 * @version 1.2
 */
public class ChargeSessionStartInfo {

	private final ChargePointIdentity chargePointId;
	private final String authorizationId;
	private final String transactionId;
	private final int evseId;
	private final int connectorId;
	private final Instant timestampStart;
	private final long meterStart;
	private final Integer reservationId;

	private ChargeSessionStartInfo(Builder builder) {
		this.chargePointId = builder.chargePointId;
		this.authorizationId = builder.authorizationId;
		this.transactionId = builder.transactionId;
		this.evseId = builder.evseId;
		this.connectorId = builder.connectorId;
		this.timestampStart = builder.timestampStart;
		this.meterStart = builder.meterStart;
		this.reservationId = builder.reservationId;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ChargeSessionStartInfo{");
		if ( chargePointId != null ) {
			builder.append("chargePointId=");
			builder.append(chargePointId);
			builder.append(", ");
		}
		if ( authorizationId != null ) {
			builder.append("authorizationId=");
			builder.append(authorizationId);
			builder.append(", ");
		}
		if ( transactionId != null ) {
			builder.append("transactionId=");
			builder.append(transactionId);
			builder.append(", ");
		}
		builder.append("evseId=");
		builder.append(evseId);
		builder.append(", connectorId=");
		builder.append(connectorId);
		builder.append(", ");
		if ( timestampStart != null ) {
			builder.append("timestampStart=");
			builder.append(timestampStart);
			builder.append(", ");
		}
		builder.append("meterStart=");
		builder.append(meterStart);
		builder.append(", ");
		if ( reservationId != null ) {
			builder.append("reservationId=");
			builder.append(reservationId);
		}
		builder.append("}");
		return builder.toString();
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
	 * Get the transaction ID.
	 * 
	 * @return the transaction ID
	 * @since 1.2
	 */
	public String getTransactionId() {
		return transactionId;
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
		private String transactionId;
		private int evseId;
		private int connectorId;
		private Instant timestampStart;
		private long meterStart;
		private Integer reservationId;

		private Builder() {
		}

		private Builder(ChargeSessionStartInfo chargeSessionInfo) {
			this.chargePointId = chargeSessionInfo.chargePointId;
			this.authorizationId = chargeSessionInfo.authorizationId;
			this.transactionId = chargeSessionInfo.transactionId;
			this.evseId = chargeSessionInfo.evseId;
			this.connectorId = chargeSessionInfo.connectorId;
			this.timestampStart = chargeSessionInfo.timestampStart;
			this.meterStart = chargeSessionInfo.meterStart;
			this.reservationId = chargeSessionInfo.reservationId;
		}

		/**
		 * Configure the charge point ID.
		 * 
		 * @param chargePointId
		 *        the charge point ID
		 * @return this instance
		 */
		public Builder withChargePointId(ChargePointIdentity chargePointId) {
			this.chargePointId = chargePointId;
			return this;
		}

		/**
		 * Configure the authorization ID.
		 * 
		 * @param authorizationId
		 *        the authorization ID
		 * @return this instance
		 */
		public Builder withAuthorizationId(String authorizationId) {
			this.authorizationId = authorizationId;
			return this;
		}

		/**
		 * Configure the transaction ID.
		 * 
		 * @param transactionId
		 *        the transaction ID
		 * @return this instance
		 * @since 1.2
		 */
		public Builder withTransactionId(String transactionId) {
			this.transactionId = transactionId;
			return this;
		}

		/**
		 * Configure the EVSE ID.
		 * 
		 * @param evseId
		 *        the EVSE ID
		 * @return this instance
		 */
		public Builder withEvseId(int evseId) {
			this.evseId = evseId;
			return this;
		}

		/**
		 * Configure the connector ID.
		 * 
		 * @param connectorId
		 *        the connector ID
		 * @return this instance
		 */
		public Builder withConnectorId(int connectorId) {
			this.connectorId = connectorId;
			return this;
		}

		/**
		 * Configure the start timestamp.
		 * 
		 * @param timestampStart
		 *        the start timestamp
		 * @return this instance
		 */
		public Builder withTimestampStart(Instant timestampStart) {
			this.timestampStart = timestampStart;
			return this;
		}

		/**
		 * Configure the starting meter value.
		 * 
		 * @param meterStart
		 *        the start value
		 * @return this instance
		 */
		public Builder withMeterStart(long meterStart) {
			this.meterStart = meterStart;
			return this;
		}

		/**
		 * Configure the reservation ID.
		 * 
		 * @param reservationId
		 *        the reservation ID
		 * @return this instance
		 */
		public Builder withReservationId(Integer reservationId) {
			this.reservationId = reservationId;
			return this;
		}

		/**
		 * Build a new instance from this builder.
		 * 
		 * @return the new instance
		 */
		public ChargeSessionStartInfo build() {
			return new ChargeSessionStartInfo(this);
		}
	}

}

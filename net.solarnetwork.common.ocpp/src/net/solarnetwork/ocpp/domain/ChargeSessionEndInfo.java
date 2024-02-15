/* ==================================================================
 * ChargeSessionEndInfo.java - 14/02/2020 3:44:29 pm
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
import java.util.Collections;

/**
 * Information about a charging session, at the end of the session.
 * 
 * @author matt
 * @version 1.1
 */
public class ChargeSessionEndInfo {

	private final ChargePointIdentity chargePointId;
	private final String authorizationId;
	private final String transactionId;
	private final Instant timestampEnd;
	private final long meterEnd;
	private final ChargeSessionEndReason reason;
	private final Iterable<SampledValue> transactionData;

	private ChargeSessionEndInfo(Builder builder) {
		this.chargePointId = builder.chargePointId;
		this.authorizationId = builder.authorizationId;
		this.transactionId = builder.transactionId;
		this.timestampEnd = builder.timestampEnd;
		this.meterEnd = builder.meterEnd;
		this.reason = builder.reason;
		this.transactionData = builder.transactionData;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ChargeSessionEndInfo{");
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
		if ( timestampEnd != null ) {
			builder.append("timestampEnd=");
			builder.append(timestampEnd);
			builder.append(", ");
		}
		builder.append("meterEnd=");
		builder.append(meterEnd);
		builder.append(", ");
		if ( reason != null ) {
			builder.append("reason=");
			builder.append(reason);
			builder.append(", ");
		}
		if ( transactionData != null ) {
			builder.append("transactionData=");
			builder.append(transactionData);
		}
		builder.append("}");
		return builder.toString();
	}

	/**
	 * The Charge Point ID.
	 * 
	 * @return the Charge Point ID
	 */
	public ChargePointIdentity getChargePointId() {
		return chargePointId;
	}

	/**
	 * The authorization ID.
	 * 
	 * @return the authorization ID
	 */
	public String getAuthorizationId() {
		return authorizationId;
	}

	/**
	 * The transaction ID.
	 * 
	 * @return the transaction ID
	 */
	public String getTransactionId() {
		return transactionId;
	}

	/**
	 * The time the session ended.
	 * 
	 * @return the end timestamp
	 */
	public Instant getTimestampEnd() {
		return timestampEnd;
	}

	/**
	 * The final meter reading at the end of the session.
	 * 
	 * @return the ending meter reading, in Wh
	 */
	public long getMeterEnd() {
		return meterEnd;
	}

	/**
	 * The reason for the session end.
	 * 
	 * @return the reason the reason
	 */
	public ChargeSessionEndReason getReason() {
		return reason;
	}

	/**
	 * Optional transaction data.
	 * 
	 * @return the transaction data, or {@literal null}
	 */
	public Iterable<SampledValue> getTransactionData() {
		return transactionData;
	}

	/**
	 * Creates builder to build {@link ChargeSessionEndInfo}.
	 * 
	 * @return created builder
	 */
	public static Builder builder() {
		return new Builder();
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
	 * Builder to build {@link ChargeSessionEndInfo}.
	 */
	public static final class Builder {

		private ChargePointIdentity chargePointId;
		private String authorizationId;
		private String transactionId;
		private Instant timestampEnd;
		private long meterEnd;
		private ChargeSessionEndReason reason;
		private Iterable<SampledValue> transactionData = Collections.emptyList();

		private Builder() {
		}

		private Builder(ChargeSessionEndInfo chargeSessionEndInfo) {
			this.chargePointId = chargeSessionEndInfo.chargePointId;
			this.authorizationId = chargeSessionEndInfo.authorizationId;
			this.transactionId = chargeSessionEndInfo.transactionId;
			this.timestampEnd = chargeSessionEndInfo.timestampEnd;
			this.meterEnd = chargeSessionEndInfo.meterEnd;
			this.reason = chargeSessionEndInfo.reason;
			this.transactionData = chargeSessionEndInfo.transactionData;
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
		 */
		public Builder withTransactionId(String transactionId) {
			this.transactionId = transactionId;
			return this;
		}

		/**
		 * Configure the ending timestamp.
		 * 
		 * @param timestampEnd
		 *        the end time
		 * @return this instance
		 */
		public Builder withTimestampEnd(Instant timestampEnd) {
			this.timestampEnd = timestampEnd;
			return this;
		}

		/**
		 * Configure the final meter value.
		 * 
		 * @param meterEnd
		 *        the final meter value
		 * @return this instance
		 */
		public Builder withMeterEnd(long meterEnd) {
			this.meterEnd = meterEnd;
			return this;
		}

		/**
		 * Configure the end reason.
		 * 
		 * @param reason
		 *        the reason
		 * @return this instance
		 */
		public Builder withReason(ChargeSessionEndReason reason) {
			this.reason = reason;
			return this;
		}

		/**
		 * Configure the transaction data.
		 * 
		 * @param transactionData
		 *        the transaction data
		 * @return this instance
		 */
		public Builder withTransactionData(Iterable<SampledValue> transactionData) {
			this.transactionData = transactionData;
			return this;
		}

		/**
		 * Create a new instance from this builder.
		 * 
		 * @return the new instance
		 */
		public ChargeSessionEndInfo build() {
			return new ChargeSessionEndInfo(this);
		}
	}

}

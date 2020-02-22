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
 * @version 1.0
 */
public class ChargeSessionEndInfo {

	private final String chargePointId;
	private final String authorizationId;
	private final int transactionId;
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

	/**
	 * The Charge Point ID.
	 * 
	 * @return the Charge Point ID
	 */
	public String getChargePointId() {
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
	public int getTransactionId() {
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

		private String chargePointId;
		private String authorizationId;
		private int transactionId;
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

		public Builder withChargePointId(String chargePointId) {
			this.chargePointId = chargePointId;
			return this;
		}

		public Builder withAuthorizationId(String authorizationId) {
			this.authorizationId = authorizationId;
			return this;
		}

		public Builder withTransactionId(int transactionId) {
			this.transactionId = transactionId;
			return this;
		}

		public Builder withTimestampEnd(Instant timestampEnd) {
			this.timestampEnd = timestampEnd;
			return this;
		}

		public Builder withMeterEnd(long meterEnd) {
			this.meterEnd = meterEnd;
			return this;
		}

		public Builder withReason(ChargeSessionEndReason reason) {
			this.reason = reason;
			return this;
		}

		public Builder withTransactionData(Iterable<SampledValue> transactionData) {
			this.transactionData = transactionData;
			return this;
		}

		public ChargeSessionEndInfo build() {
			return new ChargeSessionEndInfo(this);
		}
	}

}

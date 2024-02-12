/* ==================================================================
 * StatusNotification.java - 12/02/2020 1:03:07 pm
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
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * An OCPP status notification.
 * 
 * @author matt
 * @version 1.0
 */
@JsonDeserialize(builder = StatusNotification.Builder.class)
@JsonPropertyOrder({ "connectorId", "timestamp", "status", "errorCode", "info", "vendorId",
		"vendorErrorCode" })
public class StatusNotification {

	private final int connectorId;
	private final ChargePointStatus status;
	private final ChargePointErrorCode errorCode;
	private final String info;
	private final Instant timestamp;
	private final String vendorId;
	private final String vendorErrorCode;

	/**
	 * Constructor.
	 * 
	 * @param connectorId
	 *        the connector ID
	 * @param status
	 *        the status
	 * @param errorCode
	 *        the error code
	 * @param info
	 *        the info
	 * @param timestamp
	 *        the timestamp
	 * @param vendorId
	 *        the vendor ID
	 * @param vendorErrorCode
	 *        the vendor error code
	 */
	public StatusNotification(int connectorId, ChargePointStatus status, ChargePointErrorCode errorCode,
			String info, Instant timestamp, String vendorId, String vendorErrorCode) {
		super();
		this.connectorId = connectorId;
		this.status = status;
		this.errorCode = errorCode;
		this.info = info;
		this.timestamp = timestamp;
		this.vendorId = vendorId;
		this.vendorErrorCode = vendorErrorCode;
	}

	private StatusNotification(Builder builder) {
		this(builder.connectorId, builder.status, builder.errorCode, builder.info, builder.timestamp,
				builder.vendorId, builder.vendorErrorCode);
	}

	@Override
	public int hashCode() {
		return Objects.hash(connectorId, errorCode, info, status, timestamp, vendorErrorCode, vendorId);
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( !(obj instanceof StatusNotification) ) {
			return false;
		}
		StatusNotification other = (StatusNotification) obj;
		return connectorId == other.connectorId && isSameAs(other);
	}

	@Override
	public String toString() {
		StringBuilder builder2 = new StringBuilder();
		builder2.append("StatusNotification{connectorId=");
		builder2.append(connectorId);
		builder2.append(", ");
		if ( status != null ) {
			builder2.append("status=");
			builder2.append(status);
			builder2.append(", ");
		}
		if ( errorCode != null ) {
			builder2.append("errorCode=");
			builder2.append(errorCode);
			builder2.append(", ");
		}
		if ( timestamp != null ) {
			builder2.append("timestamp=");
			builder2.append(timestamp);
			builder2.append(", ");
		}
		if ( info != null ) {
			builder2.append("info=");
			builder2.append(info);
			builder2.append(", ");
		}
		if ( vendorId != null ) {
			builder2.append("vendorId=");
			builder2.append(vendorId);
			builder2.append(", ");
		}
		if ( vendorErrorCode != null ) {
			builder2.append("vendorErrorCode=");
			builder2.append(vendorErrorCode);
		}
		builder2.append("}");
		return builder2.toString();
	}

	/**
	 * Get the connector ID.
	 * 
	 * @return the connector ID
	 */
	public int getConnectorId() {
		return connectorId;
	}

	/**
	 * Get the status.
	 * 
	 * @return the status
	 */
	public ChargePointStatus getStatus() {
		return status;
	}

	/**
	 * Get the error code.
	 * 
	 * @return the errorCode
	 */
	public ChargePointErrorCode getErrorCode() {
		return errorCode;
	}

	/**
	 * Get the info.
	 * 
	 * @return the info
	 */
	public String getInfo() {
		return info;
	}

	/**
	 * Get the timestamp.
	 * 
	 * @return the timestamp
	 */
	public Instant getTimestamp() {
		return timestamp;
	}

	/**
	 * Get the vendor ID.
	 * 
	 * @return the vendor ID
	 */
	public String getVendorId() {
		return vendorId;
	}

	/**
	 * Get the vendor error code.
	 * 
	 * @return the error code
	 */
	public String getVendorErrorCode() {
		return vendorErrorCode;
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
	public boolean isSameAs(StatusNotification other) {
		if ( other == null ) {
			return false;
		}
		// @formatter:off
		return Objects.equals(status, other.status)
				&& Objects.equals(errorCode, other.errorCode)
				&& Objects.equals(info, other.info)
				&& Objects.equals(timestamp, other.timestamp)
				&& Objects.equals(vendorId, other.vendorId)
				&& Objects.equals(vendorErrorCode, other.vendorErrorCode);
		// @formatter:on
	}

	/**
	 * Creates builder to build {@link StatusNotification}.
	 * 
	 * @return created builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Creates a builder to build {@link StatusNotification} and initialize it
	 * with this object.
	 * 
	 * @return created builder
	 */
	public Builder toBuilder() {
		return new Builder(this);
	}

	/**
	 * Builder to build {@link StatusNotification}.
	 */
	public static final class Builder {

		private int connectorId;
		private ChargePointStatus status;
		private ChargePointErrorCode errorCode;
		private String info;
		private Instant timestamp;
		private String vendorId;
		private String vendorErrorCode;

		private Builder() {
		}

		private Builder(StatusNotification statusNotification) {
			this.connectorId = statusNotification.connectorId;
			this.status = statusNotification.status;
			this.errorCode = statusNotification.errorCode;
			this.info = statusNotification.info;
			this.timestamp = statusNotification.timestamp;
			this.vendorId = statusNotification.vendorId;
			this.vendorErrorCode = statusNotification.vendorErrorCode;
		}

		/**
		 * Configure a connector ID.
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
		 * Configure the status.
		 * 
		 * @param status
		 *        the status
		 * @return this instance
		 */
		public Builder withStatus(ChargePointStatus status) {
			this.status = status;
			return this;
		}

		/**
		 * Configure the error code.
		 * 
		 * @param errorCode
		 *        the error code
		 * @return this instance
		 */
		public Builder withErrorCode(ChargePointErrorCode errorCode) {
			this.errorCode = errorCode;
			return this;
		}

		/**
		 * Configure the info.
		 * 
		 * @param info
		 *        the info
		 * @return this instance
		 */
		public Builder withInfo(String info) {
			this.info = info;
			return this;
		}

		/**
		 * Configure the timestamp.
		 * 
		 * @param timestamp
		 *        the timestamp
		 * @return this instance
		 */
		public Builder withTimestamp(Instant timestamp) {
			this.timestamp = timestamp;
			return this;
		}

		/**
		 * Configure the vendor ID.
		 * 
		 * @param vendorId
		 *        the vendor ID
		 * @return this instance
		 */
		public Builder withVendorId(String vendorId) {
			this.vendorId = vendorId;
			return this;
		}

		/**
		 * Configure the vendor error code.
		 * 
		 * @param vendorErrorCode
		 *        the error code
		 * @return this instance
		 */
		public Builder withVendorErrorCode(String vendorErrorCode) {
			this.vendorErrorCode = vendorErrorCode;
			return this;
		}

		/**
		 * Build a notification instance from this builder.
		 * 
		 * @return the new instance
		 */
		public StatusNotification build() {
			return new StatusNotification(this);
		}
	}

}

/* ==================================================================
 * AuthorizationInfo.java - 6/02/2020 7:23:43 pm
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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Charge Point authorization information.
 * 
 * @author matt
 * @version 1.0
 */
@JsonDeserialize(builder = AuthorizationInfo.Builder.class)
public class AuthorizationInfo {

	private final String id;
	private final AuthorizationStatus status;
	private final Instant expiryDate;
	private final String parentId;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *        the ID value, e.g. RFID tag ID
	 * @param status
	 *        the associated OCCP status
	 */
	public AuthorizationInfo(String id, AuthorizationStatus status) {
		this(id, status, null, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *        the ID value, e.g. RFID tag ID
	 * @param status
	 *        the associated OCCP status
	 * @param expiryDate
	 *        the expiration date
	 * @param parentId
	 *        a parent ID
	 */
	public AuthorizationInfo(String id, AuthorizationStatus status, Instant expiryDate,
			String parentId) {
		super();
		this.id = id;
		this.status = status;
		this.expiryDate = expiryDate;
		this.parentId = parentId;
	}

	private AuthorizationInfo(Builder builder) {
		this(builder.id, builder.status, builder.expiryDate, builder.parentId);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AuthorizationInfo{");
		if ( id != null ) {
			builder.append("id=");
			builder.append(id);
			builder.append(", ");
		}
		if ( status != null ) {
			builder.append("status=");
			builder.append(status);
			builder.append(", ");
		}
		if ( expiryDate != null ) {
			builder.append("expiryDate=");
			builder.append(expiryDate);
			builder.append(", ");
		}
		if ( parentId != null ) {
			builder.append("parentId=");
			builder.append(parentId);
		}
		builder.append("}");
		return builder.toString();
	}

	public String getId() {
		return id;
	}

	public AuthorizationStatus getStatus() {
		return status;
	}

	public Instant getExpiryDate() {
		return expiryDate;
	}

	public String getParentId() {
		return parentId;
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
	 * Creates builder to build {@link AuthorizationInfo}.
	 * 
	 * @return created builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder to build {@link AuthorizationInfo}.
	 */
	public static final class Builder {

		private String id;
		private AuthorizationStatus status;
		private Instant expiryDate;
		private String parentId;

		private Builder() {
			super();
		}

		private Builder(AuthorizationInfo info) {
			super();
			this.id = info.id;
			this.status = info.status;
			this.expiryDate = info.expiryDate;
			this.parentId = info.parentId;
		}

		public Builder withId(String id) {
			this.id = id;
			return this;
		}

		public Builder withStatus(AuthorizationStatus status) {
			this.status = status;
			return this;
		}

		public Builder withExpiryDate(Instant expiryDate) {
			this.expiryDate = expiryDate;
			return this;
		}

		public Builder withParentId(String parentId) {
			this.parentId = parentId;
			return this;
		}

		public AuthorizationInfo build() {
			return new AuthorizationInfo(this);
		}
	}

}

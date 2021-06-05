/* ==================================================================
 * Authorization.java - 9/02/2020 1:57:16 pm
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
 * An authorization entity.
 * 
 * <p>
 * The primary key used is the external ID value, e.g. RFID tag ID.
 * </p>
 * 
 * @author matt
 * @version 1.1
 */
public class Authorization extends BasicLongEntity implements Differentiable<Authorization> {

	private static final long serialVersionUID = -540993930373637753L;

	private String token;
	private boolean enabled;
	private Instant expiryDate;
	private String parentId;

	/**
	 * Constructor.
	 */
	public Authorization() {
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *        the primary key
	 */
	public Authorization(Long id) {
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
	public Authorization(Long id, Instant created) {
		super(id, created);
	}

	/**
	 * Constructor.
	 * 
	 * @param created
	 *        the created date
	 * @param token
	 *        the token
	 */
	public Authorization(Instant created, String token) {
		super(null, created);
		setToken(token);
	}

	/**
	 * Copy constructor.
	 * 
	 * @param other
	 *        the authorization to copy
	 */
	public Authorization(Authorization other) {
		this(other.getId(), other.getCreated());
		this.token = other.token;
		this.enabled = other.enabled;
		this.expiryDate = other.expiryDate;
		this.parentId = other.parentId;
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
	public boolean isSameAs(Authorization other) {
		if ( other == null ) {
			return false;
		}
		// @formatter:off
		return enabled == other.enabled
				&& Objects.equals(token, other.token)
				&& Objects.equals(expiryDate, other.expiryDate)
				&& Objects.equals(parentId, other.parentId);
		// @formatter:on
	}

	@Override
	public boolean differsFrom(Authorization other) {
		return !isSameAs(other);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Authorization{id=");
		builder.append(getId());
		builder.append(", ");
		if ( token != null ) {
			builder.append("token=");
			builder.append(token);
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
			builder.append(", ");
		}
		builder.append("}");
		return builder.toString();
	}

	/**
	 * Test if this authorization has expired.
	 * 
	 * @return {@literal true} if the {@code expiryDate} property is available
	 *         and is not before the current system time
	 */
	public boolean isExpired() {
		return (expiryDate != null && expiryDate.isBefore(Instant.now()));
	}

	/**
	 * Get the authorization token value.
	 * 
	 * @return the token
	 */
	public String getToken() {
		return token;
	}

	/**
	 * Set the authorization token value.
	 * 
	 * @param token
	 *        the token to set
	 */
	public void setToken(String token) {
		this.token = token;
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
	 * Get the expiration date.
	 * 
	 * @return the expiration date, or {@literal null} for no expiration
	 */
	public Instant getExpiryDate() {
		return expiryDate;
	}

	/**
	 * Set the expiration date.
	 * 
	 * @param expiryDate
	 *        the expiration date to set, or {@literal null} for no expiration
	 */
	public void setExpiryDate(Instant expiryDate) {
		this.expiryDate = expiryDate;
	}

	/**
	 * Get the ID of a parent authorization.
	 * 
	 * @return the parent ID, or {@literal null} if there is no parent
	 */
	public String getParentId() {
		return parentId;
	}

	/**
	 * Set the ID of a parent authorization.
	 * 
	 * @param parentId
	 *        the parent ID to set, or {@literal null} if there is no parent
	 */
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

}

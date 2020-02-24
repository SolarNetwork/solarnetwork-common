/* ==================================================================
 * SystemUser.java - 20/02/2020 10:17:54 am
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
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import net.solarnetwork.dao.BasicLongEntity;
import net.solarnetwork.domain.Differentiable;
import net.solarnetwork.util.StringUtils;

/**
 * An OCPP charge point system user.
 * 
 * @author matt
 * @version 1.0
 */
public class SystemUser extends BasicLongEntity implements Differentiable<SystemUser> {

	private String username;
	private String password;
	private Set<String> allowedChargePoints;

	/**
	 * Default constructor.
	 */
	public SystemUser() {
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *        the primary key
	 * @param created
	 *        the creation date
	 */
	public SystemUser(Long id, Instant created) {
		super(id, created);
	}

	/**
	 * Constructor.
	 * 
	 * @param created
	 *        the creation date
	 * @param username
	 *        the username
	 * @param password
	 *        the password
	 */
	public SystemUser(Instant created, String username, String password) {
		super(null, created);
		setUsername(username);
		setPassword(password);
	}

	/**
	 * Copy constructor.
	 * 
	 * @param other
	 *        the other system
	 */
	public SystemUser(SystemUser other) {
		super(other.getId(), other.getCreated());
		setUsername(other.getUsername());
		setPassword(other.getPassword());
		Set<String> allowed = other.getAllowedChargePoints();
		if ( allowed != null ) {
			setAllowedChargePoints(new LinkedHashSet<>(allowed));
		}
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
	public boolean isSameAs(SystemUser other) {
		if ( other == null ) {
			return false;
		}
		int myCpSize = allowedChargePoints != null ? allowedChargePoints.size() : 0;
		int oCpSize = other.allowedChargePoints != null ? other.allowedChargePoints.size() : 0;
		// @formatter:off
		return Objects.equals(username, other.username)
				&& Objects.equals(password, other.password)
				&& ((myCpSize == 0 && oCpSize == 0) || Objects.equals(allowedChargePoints, other.allowedChargePoints));
		// @formatter:on
	}

	@Override
	public boolean differsFrom(SystemUser other) {
		return !isSameAs(other);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SystemUser{");
		if ( username != null ) {
			builder.append("username=");
			builder.append(username);
			builder.append(", ");
		}
		if ( allowedChargePoints != null ) {
			builder.append("allowedChargePoints=");
			builder.append(allowedChargePoints);
		}
		builder.append("}");
		return builder.toString();
	}

	/**
	 * Get the username.
	 * 
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Set the username.
	 * 
	 * @param username
	 *        the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Get the password.
	 * 
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Set the password.
	 * 
	 * @param password
	 *        the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the allowedChargePoints
	 */
	public Set<String> getAllowedChargePoints() {
		return allowedChargePoints;
	}

	/**
	 * @param allowedChargePoints
	 *        the allowedChargePoints to set
	 */
	public void setAllowedChargePoints(Set<String> allowedChargePoints) {
		this.allowedChargePoints = allowedChargePoints;
	}

	/**
	 * Get the allowed charge points as a comma-delimited list.
	 * 
	 * @return the allowed charge points list value
	 */
	public String getAllowedChargePointsValue() {
		return StringUtils.commaDelimitedStringFromCollection(getAllowedChargePoints());
	}

	/**
	 * Set the allowed charge points as a comma-delimited list.
	 * 
	 * @param list
	 *        the list to set
	 */
	public void setAllowedChargePointsValue(String list) {
		setAllowedChargePoints(StringUtils.commaDelimitedStringToSet(list));
	}

	/**
	 * Get the allowed charge points as an array.
	 * 
	 * @return the allowed charge points array, or {@literal null}
	 */
	public String[] getAllowedChargePointsArray() {
		Set<String> allowed = getAllowedChargePoints();
		return (allowed != null ? allowed.toArray(new String[allowed.size()]) : null);
	}

	/**
	 * Set the allowed charge points as an array.
	 * 
	 * @param array
	 *        the array to set
	 */
	public void setAllowedChargePointsArray(String[] array) {
		setAllowedChargePoints(array != null ? new LinkedHashSet<>(Arrays.asList(array)) : null);
	}

}

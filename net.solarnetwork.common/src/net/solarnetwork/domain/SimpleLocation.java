/* ==================================================================
 * SimpleLocation.java - 11/12/2020 2:10:13 pm
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

package net.solarnetwork.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.TimeZone;

/**
 * Simple, mutable implementation of {@link Location}.
 * 
 * @author matt
 * @version 1.0
 * @since 1.67
 */
public class SimpleLocation implements Location, Cloneable, Serializable {

	private static final long serialVersionUID = 7734763057760648362L;

	private String name;
	private String country;
	private String region;
	private String stateOrProvince;
	private String locality;
	private String postalCode;
	private String street;
	private BigDecimal latitude;
	private BigDecimal longitude;
	private BigDecimal elevation;
	private String timeZoneId;

	/**
	 * Default constructor.
	 */
	public SimpleLocation() {
		super();
	}

	/**
	 * Copy constructor for {@link Location} objects.
	 * 
	 * @param loc
	 *        the location to copy
	 */
	public SimpleLocation(Location loc) {
		super();
		setName(loc.getName());
		setCountry(loc.getCountry());
		setRegion(loc.getRegion());
		setStateOrProvince(loc.getStateOrProvince());
		setLocality(loc.getLocality());
		setPostalCode(loc.getPostalCode());
		setStreet(loc.getStreet());
		setLatitude(loc.getLatitude());
		setLongitude(loc.getLongitude());
		setElevation(loc.getElevation());
		setTimeZoneId(loc.getTimeZoneId());
	}

	/**
	 * Create a new location instance.
	 * 
	 * @param name
	 *        the name
	 * @param country
	 *        the country
	 * @param region
	 *        the region
	 * @param stateOrProvince
	 *        the state or province
	 * @param locality
	 *        the locality (city)
	 * @param postalCode
	 *        the postal code
	 * @param street
	 *        the street
	 * @param timeZoneId
	 *        the time zone ID
	 * @return the new location instance
	 */
	public static SimpleLocation locationOf(String name, String country, String region,
			String stateOrProvince, String locality, String postalCode, String street,
			String timeZoneId) {
		SimpleLocation l = new SimpleLocation();
		l.setName(name);
		l.setCountry(country);
		l.setRegion(region);
		l.setStateOrProvince(stateOrProvince);
		l.setLocality(locality);
		l.setPostalCode(postalCode);
		l.setStreet(street);
		l.setTimeZoneId(timeZoneId);
		return l;
	}

	/**
	 * Create a new location instance.
	 * 
	 * @param country
	 *        the country
	 * @param region
	 *        the region
	 * @param timeZoneId
	 *        the time zone ID
	 * @return the new location instance
	 */
	public static SimpleLocation locationOf(String country, String region, String timeZoneId) {
		SimpleLocation l = new SimpleLocation();
		l.setCountry(country);
		l.setRegion(region);
		l.setTimeZoneId(timeZoneId);
		return l;
	}

	@Override
	public SimpleLocation clone() {
		try {
			return (SimpleLocation) super.clone();
		} catch ( CloneNotSupportedException e ) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(country, elevation, latitude, locality, longitude, name, postalCode, region,
				stateOrProvince, street, timeZoneId);
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( !(obj instanceof Location) ) {
			return false;
		}
		Location other = (Location) obj;
		return Objects.equals(country, other.getCountry())
				&& Objects.equals(elevation, other.getElevation())
				&& Objects.equals(latitude, other.getLatitude())
				&& Objects.equals(locality, other.getLocality())
				&& Objects.equals(longitude, other.getLongitude())
				&& Objects.equals(name, other.getName())
				&& Objects.equals(postalCode, other.getPostalCode())
				&& Objects.equals(region, other.getRegion())
				&& Objects.equals(stateOrProvince, other.getStateOrProvince())
				&& Objects.equals(street, other.getStreet())
				&& Objects.equals(timeZoneId, other.getTimeZoneId());
	}

	/**
	 * Change values that are non-null but empty to null.
	 * 
	 * <p>
	 * This method is helpful for web form submission, to remove filter values
	 * that are empty and would otherwise try to match on empty string values.
	 * </p>
	 */
	public void removeEmptyValues() {
		if ( country != null && !hasText(country) ) {
			country = null;
		}
		if ( locality != null && !hasText(locality) ) {
			locality = null;
		}
		if ( name != null && !hasText(name) ) {
			name = null;
		}
		if ( postalCode != null && !hasText(postalCode) ) {
			postalCode = null;
		}
		if ( region != null && !hasText(region) ) {
			region = null;
		}
		if ( stateOrProvince != null && !hasText(stateOrProvince) ) {
			stateOrProvince = null;
		}
		if ( street != null && !hasText(street) ) {
			street = null;
		}
		if ( timeZoneId != null && !hasText(timeZoneId) ) {
			timeZoneId = null;
		}
	}

	private static boolean hasText(String s) {
		if ( s == null || s.isEmpty() ) {
			return false;
		}
		final int strLen = s.length();
		for ( int i = 0; i < strLen; i++ ) {
			if ( !Character.isWhitespace(s.charAt(i)) ) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Return a new SolarLocation with normalized values from another Location.
	 * 
	 * @param loc
	 *        the location to normalize
	 * @return the normalized location
	 */
	public static SimpleLocation normalizedLocation(Location loc) {
		assert loc != null;
		SimpleLocation norm = new SimpleLocation();
		if ( loc.getName() != null ) {
			String name = loc.getName().trim();
			if ( name.length() > 0 ) {
				norm.setName(name);
			}
		}
		if ( loc.getCountry() != null && loc.getCountry().length() >= 2 ) {
			String country = loc.getCountry();
			if ( country.length() > 2 ) {
				country = country.substring(0, 2);
			}
			norm.setCountry(country.toUpperCase());
		}
		if ( loc.getTimeZoneId() != null ) {
			TimeZone tz = TimeZone.getTimeZone(loc.getTimeZoneId());
			if ( tz != null ) {
				norm.setTimeZoneId(tz.getID());
			}
		}
		if ( loc.getRegion() != null ) {
			String region = loc.getRegion().trim();
			if ( region.length() > 0 ) {
				norm.setRegion(region);
			}
		}
		if ( loc.getStateOrProvince() != null ) {
			String state = loc.getStateOrProvince().trim();
			if ( state.length() > 0 ) {
				norm.setStateOrProvince(state);
			}
		}
		if ( loc.getLocality() != null ) {
			String locality = loc.getLocality().trim();
			if ( locality.length() > 0 ) {
				norm.setLocality(locality);
			}
		}
		if ( loc.getPostalCode() != null ) {
			String postalCode = loc.getPostalCode().trim().toUpperCase();
			if ( postalCode.length() > 0 ) {
				norm.setPostalCode(postalCode);
			}
		}
		if ( loc.getStreet() != null ) {
			String street = loc.getStreet().trim();
			if ( street.length() > 0 ) {
				norm.setStreet(street);
			}
		}
		norm.setLatitude(loc.getLatitude());
		norm.setLongitude(loc.getLongitude());
		norm.setElevation(loc.getElevation());
		return norm;
	}

	/**
	 * Get a {@code SimpleLocation} for a {@code Location}.
	 * 
	 * <p>
	 * <b>Note</b> if {@code location} is already a {@code SimpleLocation} then
	 * it will be returned via a cast. Otherwise a new instance will be created.
	 * </p>
	 * 
	 * @param location
	 *        the location to get as a {@code SimpleLocation}
	 * @return the {@code SimpleLocation} instance, or {@literal null} if
	 *         {@code location} is {@literal null}
	 * @since 1.1
	 */
	public static SimpleLocation locationValue(Location location) {
		return (location == null ? null
				: location instanceof SimpleLocation ? (SimpleLocation) location
						: new SimpleLocation(location));
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SimpleLocation{");
		if ( name != null ) {
			builder.append("name=");
			builder.append(name);
			builder.append(", ");
		}
		if ( country != null ) {
			builder.append("country=");
			builder.append(country);
			builder.append(", ");
		}
		if ( region != null ) {
			builder.append("region=");
			builder.append(region);
			builder.append(", ");
		}
		if ( locality != null ) {
			builder.append("locality=");
			builder.append(locality);
		}
		builder.append("}");
		return builder.toString();
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	@Override
	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	@Override
	public String getStateOrProvince() {
		return stateOrProvince;
	}

	public void setStateOrProvince(String stateOrProvince) {
		this.stateOrProvince = stateOrProvince;
	}

	@Override
	public String getLocality() {
		return locality;
	}

	public void setLocality(String locality) {
		this.locality = locality;
	}

	@Override
	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	@Override
	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	@Override
	public BigDecimal getLatitude() {
		return latitude;
	}

	public void setLatitude(BigDecimal latitude) {
		this.latitude = latitude;
	}

	@Override
	public BigDecimal getLongitude() {
		return longitude;
	}

	public void setLongitude(BigDecimal longitude) {
		this.longitude = longitude;
	}

	@Override
	public BigDecimal getElevation() {
		return elevation;
	}

	public void setElevation(BigDecimal elevation) {
		this.elevation = elevation;
	}

	@Override
	public String getTimeZoneId() {
		return timeZoneId;
	}

	public void setTimeZoneId(String timeZoneId) {
		this.timeZoneId = timeZoneId;
	}

}

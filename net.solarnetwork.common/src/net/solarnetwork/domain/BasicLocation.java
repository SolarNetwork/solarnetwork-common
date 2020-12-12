/* ==================================================================
 * BasicLocation.java - Oct 22, 2014 12:06:47 PM
 * 
 * Copyright 2007-2014 SolarNetwork.net Dev Team
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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Basic, immutable implementation of {@link Location}.
 * 
 * @author matt
 * @version 1.1
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BasicLocation implements Location, Cloneable, Serializable {

	private static final long serialVersionUID = -7249883372922528538L;

	private final String name;
	private final String country;
	private final String region;
	private final String stateOrProvince;
	private final String locality;
	private final String postalCode;
	private final String street;
	private final BigDecimal latitude;
	private final BigDecimal longitude;
	private final BigDecimal elevation;
	private final String timeZoneId;

	/**
	 * Copy constructor for {@link Location} objects.
	 * 
	 * @param loc
	 *        the location to copy
	 */
	public BasicLocation(Location loc) {
		this(loc.getName(), loc.getCountry(), loc.getRegion(), loc.getStateOrProvince(),
				loc.getLocality(), loc.getPostalCode(), loc.getStreet(), loc.getLatitude(),
				loc.getLongitude(), loc.getElevation(), loc.getTimeZoneId());
	}

	/**
	 * Constructor.
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
	 * @param latitude
	 *        the latitude
	 * @param longitude
	 *        the longitude
	 * @param elevation
	 *        the elevation
	 * @param timeZoneId
	 *        the time zone ID
	 */
	public BasicLocation(String name, String country, String region, String stateOrProvince,
			String locality, String postalCode, String street, BigDecimal latitude, BigDecimal longitude,
			BigDecimal elevation, String timeZoneId) {
		super();
		this.name = name;
		this.country = country;
		this.region = region;
		this.stateOrProvince = stateOrProvince;
		this.locality = locality;
		this.postalCode = postalCode;
		this.street = street;
		this.latitude = latitude;
		this.longitude = longitude;
		this.elevation = elevation;
		this.timeZoneId = timeZoneId;
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
	public static BasicLocation locationOf(String name, String country, String region,
			String stateOrProvince, String locality, String postalCode, String street,
			String timeZoneId) {
		return new BasicLocation(name, country, region, stateOrProvince, locality, postalCode, street,
				null, null, null, timeZoneId);
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
	public static BasicLocation locationOf(String country, String region, String timeZoneId) {
		return new BasicLocation(null, country, region, null, null, null, null, null, null, null,
				timeZoneId);
	}

	@Override
	public BasicLocation clone() {
		try {
			return (BasicLocation) super.clone();
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
	 * Return a new BasicLocation with normalized values from another Location.
	 * 
	 * @param loc
	 *        the location to normalize
	 * @return the normalized location
	 */
	public static BasicLocation normalizedLocation(Location loc) {
		assert loc != null;

		String name = loc.getName();
		if ( name != null ) {
			name = name.trim();
			if ( name.isEmpty() ) {
				name = null;
			}
		}

		String country = loc.getCountry();
		if ( country != null ) {
			country = country.trim();
			if ( country.length() >= 2 ) {
				country = country.substring(0, 2).toUpperCase();
			} else {
				country = null;
			}
		}

		String region = loc.getRegion();
		if ( region != null ) {
			region = region.trim();
			if ( region.isEmpty() ) {
				region = null;
			}
		}

		String stateOrProvince = loc.getStateOrProvince();
		if ( stateOrProvince != null ) {
			stateOrProvince = stateOrProvince.trim();
			if ( stateOrProvince.isEmpty() ) {
				stateOrProvince = null;
			}
		}

		String locality = loc.getLocality();
		if ( locality != null ) {
			locality = locality.trim();
			if ( locality.isEmpty() ) {
				locality = null;
			}
		}

		String postalCode = loc.getPostalCode();
		if ( postalCode != null ) {
			postalCode = postalCode.trim();
			if ( postalCode.isEmpty() ) {
				postalCode = null;
			}
		}

		String street = loc.getStreet();
		if ( street != null ) {
			street = street.trim();
			if ( street.isEmpty() ) {
				street = null;
			}
		}

		String timeZoneId = loc.getTimeZoneId();
		if ( timeZoneId != null ) {
			timeZoneId = timeZoneId.trim();
			if ( timeZoneId.isEmpty() ) {
				timeZoneId = null;
			}
		}

		return new BasicLocation(name, country, region, stateOrProvince, locality, postalCode, street,
				loc.getLatitude(), loc.getLongitude(), loc.getElevation(), timeZoneId);
	}

	/**
	 * Get a {@code BasicLocation} for a {@code Location}.
	 * 
	 * <p>
	 * <b>Note</b> if {@code location} is already a {@code BasicLocation} then
	 * it will be returned via a cast. Otherwise a new instance will be created.
	 * </p>
	 * 
	 * @param location
	 *        the location to get as a {@code BasicLocation}
	 * @return the {@code BasicLocation} instance, or {@literal null} if
	 *         {@code location} is {@literal null}
	 * @since 1.1
	 */
	public static BasicLocation locationValue(Location location) {
		return (location == null ? null
				: location instanceof BasicLocation ? (BasicLocation) location
						: new BasicLocation(location));
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BasicLocation{");
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

	@Override
	public String getCountry() {
		return country;
	}

	@Override
	public String getRegion() {
		return region;
	}

	@Override
	public String getStateOrProvince() {
		return stateOrProvince;
	}

	@Override
	public String getLocality() {
		return locality;
	}

	@Override
	public String getPostalCode() {
		return postalCode;
	}

	@Override
	public String getStreet() {
		return street;
	}

	@Override
	public BigDecimal getLatitude() {
		return latitude;
	}

	@Override
	public BigDecimal getLongitude() {
		return longitude;
	}

	@Override
	public BigDecimal getElevation() {
		return elevation;
	}

	@Override
	public String getTimeZoneId() {
		return timeZoneId;
	}

}

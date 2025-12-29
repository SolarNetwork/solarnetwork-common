/* ==================================================================
 * BasicLocationSerializer.java - 6/06/2021 9:56:10 AM
 *
 * Copyright 2021 SolarNetwork.net Dev Team
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

package net.solarnetwork.codec.jackson;

import net.solarnetwork.domain.Identity;
import net.solarnetwork.domain.Location;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * Serializer for {@link Location} objects.
 *
 * @author matt
 * @version 1.0
 * @since 4.13
 */
public class BasicLocationSerializer extends StdSerializer<Location> {

	/** A default instance. */
	public static final ValueSerializer<Location> INSTANCE = new BasicLocationSerializer();

	/**
	 * Constructor.
	 */
	public BasicLocationSerializer() {
		super(Location.class);
	}

	@Override
	public void serialize(Location value, JsonGenerator generator, SerializationContext provider)
			throws JacksonException {
		if ( value == null ) {
			generator.writeNull();
			return;
		}

		Object id = (value instanceof Identity<?> ? ((Identity<?>) value).getId() : null);

		// @formatter:off
		final int size =
				  (id != null ? 1 : 0)
				+ (value.getName() != null ? 1 : 0)
				+ (value.getCountry() != null ? 1 : 0)
				+ (value.getRegion() != null ? 1 : 0)
				+ (value.getStateOrProvince() != null ? 1 : 0)
				+ (value.getPostalCode() != null ? 1 : 0)
				+ (value.getLocality() != null ? 1 : 0)
				+ (value.getStreet() != null ? 1 : 0)
				+ (value.getLatitude() != null ? 1 : 0)
				+ (value.getLongitude() != null ? 1 : 0)
				+ (value.getElevation() != null ? 1 : 0)
				+ (value.getTimeZoneId() != null ? 1 : 0)
				;
		// @formatter:on
		generator.writeStartObject(value, size);
		if ( id != null ) {
			generator.writeName("id");
			if ( id instanceof Long ) {
				generator.writeNumber((Long) id);
			} else {
				generator.writeString(id.toString());
			}
		}
		BasicLocationField.Name.writeValue(generator, provider, value.getName());
		BasicLocationField.Country.writeValue(generator, provider, value.getCountry());
		BasicLocationField.Region.writeValue(generator, provider, value.getRegion());
		BasicLocationField.StateOrProvince.writeValue(generator, provider, value.getStateOrProvince());
		BasicLocationField.PostalCode.writeValue(generator, provider, value.getPostalCode());
		BasicLocationField.Locality.writeValue(generator, provider, value.getLocality());
		BasicLocationField.Street.writeValue(generator, provider, value.getStreet());
		BasicLocationField.Latitude.writeValue(generator, provider, value.getLatitude());
		BasicLocationField.Longitude.writeValue(generator, provider, value.getLongitude());
		BasicLocationField.Elevation.writeValue(generator, provider, value.getElevation());
		BasicLocationField.TimeZoneId.writeValue(generator, provider, value.getTimeZoneId());
		generator.writeEndObject();
	}

}

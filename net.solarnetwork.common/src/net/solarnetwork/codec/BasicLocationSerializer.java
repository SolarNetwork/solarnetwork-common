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

package net.solarnetwork.codec;

import java.io.IOException;
import java.io.Serializable;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import net.solarnetwork.domain.Location;

/**
 * Serializer for {@link Location} objects.
 * 
 * @author matt
 * @version 1.0
 * @since 1.72
 */
public class BasicLocationSerializer extends StdScalarSerializer<Location> implements Serializable {

	private static final long serialVersionUID = 4083741477465008605L;

	/** A default instance. */
	public static final BasicLocationSerializer INSTANCE = new BasicLocationSerializer();

	/**
	 * Constructor.
	 */
	public BasicLocationSerializer() {
		super(Location.class);
	}

	@Override
	public void serialize(Location value, JsonGenerator generator, SerializerProvider provider)
			throws IOException {
		if ( value == null ) {
			generator.writeNull();
			return;
		}
		generator.writeStartObject(value, 11);
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

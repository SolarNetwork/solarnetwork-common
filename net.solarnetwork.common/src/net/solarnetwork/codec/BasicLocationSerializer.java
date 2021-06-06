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

	/** A default instance. */
	public static final BasicLocationSerializer INSTANCE = new BasicLocationSerializer();

	private static final long serialVersionUID = 4083741477465008605L;

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
		if ( value.getName() != null ) {
			generator.writeStringField("name", value.getName());
		}
		if ( value.getCountry() != null ) {
			generator.writeStringField("country", value.getCountry());
		}
		if ( value.getRegion() != null ) {
			generator.writeStringField("region", value.getRegion());
		}
		if ( value.getStateOrProvince() != null ) {
			generator.writeStringField("stateOrProvince", value.getStateOrProvince());
		}
		if ( value.getPostalCode() != null ) {
			generator.writeStringField("postalCode", value.getPostalCode());
		}
		if ( value.getLocality() != null ) {
			generator.writeStringField("locality", value.getLocality());
		}
		if ( value.getStreet() != null ) {
			generator.writeStringField("street", value.getStreet());
		}
		if ( value.getLatitude() != null ) {
			generator.writeNumberField("lat", value.getLatitude());
		}
		if ( value.getLongitude() != null ) {
			generator.writeNumberField("lon", value.getLongitude());
		}
		if ( value.getElevation() != null ) {
			generator.writeNumberField("el", value.getElevation());
		}
		if ( value.getTimeZoneId() != null ) {
			generator.writeStringField("zone", value.getTimeZoneId());
		}
		generator.writeEndObject();

	}

}

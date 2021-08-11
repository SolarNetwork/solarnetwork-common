/* ==================================================================
 * BasicObjectDatumStreamMetadataField.java - 6/06/2021 6:08:55 PM
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
import java.math.BigDecimal;
import java.util.Map;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.solarnetwork.domain.BasicLocation;

/**
 * Fields for {@link BasicLocation}.
 * 
 * @author matt
 * @version 1.1
 * @since 1.72
 */
public enum BasicLocationField implements IndexedField {

	Name(0, "name"),

	Country(1, "country"),

	Region(2, "region"),

	StateOrProvince(3, "stateOrProvince"),

	PostalCode(4, "postalCode"),

	Locality(5, "locality"),

	Street(6, "street"),

	Latitude(7, "lat"),

	Longitude(8, "lon"),

	Elevation(9, "el"),

	TimeZoneId(10, "zone"),

	;

	/** A field map. */
	public static final Map<String, BasicLocationField> FIELD_MAP = IndexedField
			.fieldMap(BasicLocationField.class);

	private final int index;
	private final String fieldName;

	private BasicLocationField(int index, String fieldName) {
		this.index = index;
		this.fieldName = fieldName;
	}

	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public String getFieldName() {
		return fieldName;
	}

	@Override
	public Object parseValue(JsonParser parser, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		switch (this) {
			case Name:
			case Country:
			case Locality:
			case PostalCode:
			case Region:
			case StateOrProvince:
			case Street:
			case TimeZoneId:
				return parser.nextTextValue();

			case Elevation:
			case Latitude:
			case Longitude:
				return JsonUtils.parseDecimal(parser);

			default:
				return null;
		}
	}

	@Override
	public void writeValue(JsonGenerator generator, SerializerProvider provider, Object value)
			throws IOException, JsonProcessingException {
		if ( value == null ) {
			return;
		}
		switch (this) {
			case Name:
			case Country:
			case Locality:
			case PostalCode:
			case Region:
			case StateOrProvince:
			case Street:
			case TimeZoneId:
				generator.writeStringField(fieldName, value.toString());
				break;

			case Elevation:
			case Latitude:
			case Longitude:
				generator.writeNumberField(fieldName, (BigDecimal) value);
				break;

			default:
				// nothing
		}
	}

}

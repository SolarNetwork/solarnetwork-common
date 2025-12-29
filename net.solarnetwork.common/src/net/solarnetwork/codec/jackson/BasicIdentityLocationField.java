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

package net.solarnetwork.codec.jackson;

import java.math.BigDecimal;
import java.util.Map;
import net.solarnetwork.domain.BasicLocation;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.SerializationContext;

/**
 * Fields for {@link BasicLocation}.
 *
 * @author matt
 * @version 1.0
 * @since 4.13
 */
public enum BasicIdentityLocationField implements IndexedField {

	/** The ID. */
	Id(0, "id"),

	/** The name. */
	Name(1, "name"),

	/** The country. */
	Country(2, "country"),

	/** The region. */
	Region(3, "region"),

	/** The state or province. */
	StateOrProvince(4, "stateOrProvince"),

	/** The postal code. */
	PostalCode(5, "postalCode"),

	/** The locality. */
	Locality(6, "locality"),

	/** The street. */
	Street(7, "street"),

	/** The latitude. */
	Latitude(8, "lat"),

	/** The longitude. */
	Longitude(9, "lon"),

	/** The elevation. */
	Elevation(10, "el"),

	/** The time zone ID. */
	TimeZoneId(11, "zone"),

	;

	/** A field map. */
	public static final Map<String, BasicIdentityLocationField> FIELD_MAP = IndexedField
			.fieldMap(BasicIdentityLocationField.class);

	private final int index;
	private final String fieldName;

	private BasicIdentityLocationField(int index, String fieldName) {
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
	public Object parseValue(JsonParser parser, DeserializationContext ctxt) throws JacksonException {
		switch (this) {
			case Id:
				return JsonUtils.parseLong(parser);

			case Name:
			case Country:
			case Locality:
			case PostalCode:
			case Region:
			case StateOrProvince:
			case Street:
			case TimeZoneId:
				return parser.nextStringValue();

			case Elevation:
			case Latitude:
			case Longitude:
				return JsonUtils.parseDecimal(parser);

			default:
				return null;
		}
	}

	@Override
	public void writeValue(JsonGenerator generator, SerializationContext provider, Object value)
			throws JacksonException {
		if ( value == null ) {
			return;
		}
		switch (this) {
			case Id:
				generator.writeNumberProperty(fieldName, ((Number) value).longValue());
				break;

			case Name:
			case Country:
			case Locality:
			case PostalCode:
			case Region:
			case StateOrProvince:
			case Street:
			case TimeZoneId:
				generator.writeStringProperty(fieldName, value.toString());
				break;

			case Elevation:
			case Latitude:
			case Longitude:
				generator.writeNumberProperty(fieldName, (BigDecimal) value);
				break;

			default:
				// nothing
		}
	}

}

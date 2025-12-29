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

import java.util.Map;
import java.util.UUID;
import net.solarnetwork.domain.datum.BasicObjectDatumStreamMetadata;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.SerializationContext;

/**
 * Fields for {@link BasicObjectDatumStreamMetadata}.
 *
 * @author matt
 * @version 1.0
 * @since 4.13
 */
public enum BasicObjectDatumStreamMetadataField implements IndexedField {

	/** The stream ID. */
	StreamId(0, "streamId"),

	/** The time zone ID. */
	TimeZoneId(1, "zone"),

	/** The object datum kind. */
	ObjectDatumKind(2, "kind"),

	/** The object ID. */
	ObjectId(3, "objectId"),

	/** The source ID. */
	SourceId(4, "sourceId"),

	/** The location. */
	Location(5, "location"),

	/** The instantaneous properties. */
	Instantaneous(6, "i"),

	/** The accumulating properties. */
	Accumulating(7, "a"),

	/** The status properties. */
	Status(8, "s"),

	;

	/** A field map. */
	public static final Map<String, BasicObjectDatumStreamMetadataField> FIELD_MAP = IndexedField
			.fieldMap(BasicObjectDatumStreamMetadataField.class);

	private final int index;
	private final String fieldName;

	private BasicObjectDatumStreamMetadataField(int index, String fieldName) {
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
			case StreamId:
				return UUID.fromString(parser.nextStringValue());

			case TimeZoneId:
			case SourceId:
				return parser.nextStringValue();

			case ObjectDatumKind:
				return net.solarnetwork.domain.datum.ObjectDatumKind.forKey(parser.nextStringValue());

			case ObjectId:
				return parser.nextLongValue(0);

			case Location:
				parser.nextToken();
				return BasicLocationDeserializer.INSTANCE.deserialize(parser, ctxt);

			case Instantaneous:
			case Accumulating:
			case Status:
				return JsonUtils.parseStringArray(parser);

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
			case StreamId:
			case TimeZoneId:
			case SourceId:
				generator.writeStringProperty(fieldName, value.toString());
				break;

			case ObjectDatumKind:
				generator.writeStringProperty(fieldName, Character
						.toString(((net.solarnetwork.domain.datum.ObjectDatumKind) value).getKey()));
				break;

			case ObjectId:
				generator.writeNumberProperty(fieldName, (Long) value);
				break;

			case Location:
				generator.writeName(fieldName);
				BasicLocationSerializer.INSTANCE.serialize((net.solarnetwork.domain.Location) value,
						generator, provider);
				break;

			case Instantaneous:
			case Accumulating:
			case Status:
				JsonUtils.writeStringArrayField(generator, fieldName, (String[]) value);
				break;

			default:
				// nothing
		}
	}

}

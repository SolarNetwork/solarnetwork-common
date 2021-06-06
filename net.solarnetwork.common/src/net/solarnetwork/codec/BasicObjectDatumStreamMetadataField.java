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
import java.util.Map;
import java.util.UUID;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.solarnetwork.domain.datum.BasicObjectDatumStreamMetadata;

/**
 * Fields for {@link BasicObjectDatumStreamMetadata}.
 * 
 * @author matt
 * @version 1.0
 * @since 1.72
 */
public enum BasicObjectDatumStreamMetadataField implements IndexedField {

	StreamId(0, "streamId"),

	TimeZoneId(1, "zone"),

	ObjectDatumKind(2, "kind"),

	ObjectId(3, "objectId"),

	SourceId(4, "sourceId"),

	Location(5, "location"),

	Instantaneous(6, "i"),

	Accumulating(7, "a"),

	Status(8, "s"),

	;

	/** A field map. */
	public static final Map<String, IndexedField> FIELD_MAP = IndexedField
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
	public Object parseValue(JsonParser parser, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		switch (this) {
			case StreamId:
				return UUID.fromString(parser.nextTextValue());

			case TimeZoneId:
			case SourceId:
				return parser.nextTextValue();

			case ObjectDatumKind:
				return net.solarnetwork.domain.datum.ObjectDatumKind.forKey(parser.nextTextValue());

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
	public void writeValue(JsonGenerator generator, SerializerProvider provider, Object value)
			throws IOException, JsonProcessingException {
		if ( value == null ) {
			return;
		}
		switch (this) {
			case StreamId:
			case TimeZoneId:
			case SourceId:
				generator.writeStringField(fieldName, value.toString());
				break;

			case ObjectDatumKind:
				generator.writeStringField(fieldName, Character
						.toString(((net.solarnetwork.domain.datum.ObjectDatumKind) value).getKey()));
				break;

			case ObjectId:
				generator.writeNumberField(fieldName, (Long) value);
				break;

			case Location:
				generator.writeFieldName(fieldName);
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

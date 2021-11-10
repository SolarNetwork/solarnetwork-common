/* ==================================================================
 * BasicInstructionField.java - 11/08/2021 3:22:37 PM
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
import java.time.Instant;
import java.util.Map;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.solarnetwork.codec.JsonDateUtils.InstantDeserializer;
import net.solarnetwork.codec.JsonDateUtils.InstantSerializer;
import net.solarnetwork.domain.BasicInstructionStatus;
import net.solarnetwork.domain.InstructionStatus;

/**
 * Fields for {@link BasicInstructionStatus} de/serialization.
 * 
 * @author matt
 * @version 1.0
 * @since 2.0
 */
public enum BasicInstructionStatusField implements IndexedField {

	InstructionId(0, "instructionId"),

	StatusDate(1, "statusDate"),

	InstructionState(2, "state"),

	ResultParameters(3, "resultParameters"),

	;

	/** A field map. */
	public static final Map<String, BasicInstructionStatusField> FIELD_MAP = IndexedField
			.fieldMap(BasicInstructionStatusField.class);

	private final int index;
	private final String fieldName;

	private BasicInstructionStatusField(int index, String fieldName) {
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
			case InstructionId:
				parser.nextToken();
				return parser.getLongValue();

			case InstructionState:
				try {
					return InstructionStatus.InstructionState.valueOf(parser.nextTextValue());
				} catch ( Exception e ) {
					return InstructionStatus.InstructionState.Unknown;
				}

			case StatusDate:
				parser.nextToken();
				return InstantDeserializer.INSTANCE.deserialize(parser, ctxt);

			case ResultParameters:
				return JsonUtils.parseSimpleMap(parser);

			default:
				return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void writeValue(JsonGenerator generator, SerializerProvider provider, Object value)
			throws IOException, JsonProcessingException {
		if ( value == null ) {
			return;
		}
		switch (this) {
			case InstructionId:
				generator.writeNumberField(fieldName, ((Number) value).longValue());
				break;

			case InstructionState:
				generator.writeStringField(fieldName, value.toString());
				break;

			case StatusDate:
				generator.writeFieldName(fieldName);
				InstantSerializer.INSTANCE.serialize((Instant) value, generator, provider);
				break;

			case ResultParameters:
				generator.writeFieldName(fieldName);
				JsonUtils.writeSimpleMap(generator, (Map<String, ?>) value);
				break;

			default:
				// nothing
		}
	}

}
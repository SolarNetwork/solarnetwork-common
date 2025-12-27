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

package net.solarnetwork.codec.jackson;

import java.time.Instant;
import java.util.Map;
import net.solarnetwork.codec.jackson.JsonDateUtils.InstantDeserializer;
import net.solarnetwork.codec.jackson.JsonDateUtils.InstantSerializer;
import net.solarnetwork.domain.BasicInstructionStatus;
import net.solarnetwork.domain.InstructionStatus;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.SerializationContext;

/**
 * Fields for {@link BasicInstructionStatus} de/serialization.
 *
 * @author matt
 * @version 1.0
 * @since 4.13
 */
public enum BasicInstructionStatusField implements IndexedField {

	/** The instruction ID alternate field name. */
	Id(0, "id"),

	/** The instruction ID. */
	InstructionId(0, "instructionId"),

	/** The status date. */
	StatusDate(1, "statusDate"),

	/** The instruction state. */
	InstructionState(2, "state"),

	/** The result parameters. */
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
	public Object parseValue(JsonParser parser, DeserializationContext ctxt) throws JacksonException {
		switch (this) {
			case Id:
			case InstructionId:
				parser.nextToken();
				return parser.getLongValue();

			case InstructionState:
				try {
					return InstructionStatus.InstructionState.valueOf(parser.nextStringValue());
				} catch ( Exception e ) {
					return InstructionStatus.InstructionState.Unknown;
				}

			case StatusDate:
				parser.nextToken();
				return InstantDeserializer.INSTANCE.deserialize(parser, ctxt);

			case ResultParameters:
				parser.nextToken();
				return parser.readValueAs(JsonUtils.STRING_MAP_TYPE);

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
			case InstructionId:
				generator.writeNumberProperty(fieldName, ((Number) value).longValue());
				break;

			case InstructionState:
				generator.writeStringProperty(fieldName, value.toString());
				break;

			case StatusDate:
				generator.writeName(fieldName);
				InstantSerializer.INSTANCE.serialize((Instant) value, generator, provider);
				break;

			case ResultParameters:
				generator.writeName(fieldName);
				generator.writePOJO(value);
				break;

			default:
				// nothing
		}
	}

}

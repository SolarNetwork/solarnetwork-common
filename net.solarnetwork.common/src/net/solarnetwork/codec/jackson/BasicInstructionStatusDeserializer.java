/* ==================================================================
 * BasicInstructionStatusDeserializer.java - 6/09/2021 7:25:29 AM
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
import net.solarnetwork.domain.BasicInstructionStatus;
import net.solarnetwork.domain.InstructionStatus;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.exc.MismatchedInputException;

/**
 * Deserializer for {@link InstructionStatus} instances.
 *
 * @author matt
 * @version 1.0
 * @since 4.13
 */
public class BasicInstructionStatusDeserializer extends StdDeserializer<InstructionStatus> {

	/** A default instance . */
	public static final ValueDeserializer<InstructionStatus> INSTANCE = new BasicInstructionStatusDeserializer();

	/**
	 * Constructor.
	 */
	public BasicInstructionStatusDeserializer() {
		super(InstructionStatus.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public InstructionStatus deserialize(JsonParser p, DeserializationContext ctxt)
			throws JacksonException {
		JsonToken t = p.currentToken();
		if ( t == JsonToken.VALUE_NULL ) {
			return null;
		} else if ( p.isExpectedStartObjectToken() ) {
			Long id = null;
			InstructionStatus.InstructionState state = null;
			Instant statusDate = null;
			Map<String, ?> resultParameters = null;

			while ( (t = p.nextToken()) != JsonToken.END_OBJECT ) {
				String f = p.currentName();
				BasicInstructionStatusField statusField = BasicInstructionStatusField.FIELD_MAP.get(f);
				if ( statusField == null ) {
					p.nextToken();
					p.skipChildren();
					continue;
				}
				Object v = statusField.parseValue(p, ctxt);
				switch (statusField) {
					case Id:
					case InstructionId:
						id = (Long) v;
						break;

					case InstructionState:
						state = (InstructionStatus.InstructionState) v;
						break;

					case StatusDate:
						statusDate = (Instant) v;
						break;

					case ResultParameters:
						resultParameters = (Map<String, ?>) v;
						break;

				}
			}
			// jump to end object
			while ( (t = p.currentToken()) != JsonToken.END_OBJECT && t != null ) {
				t = p.nextToken();
			}
			return new BasicInstructionStatus(id, state, statusDate, resultParameters);
		}
		throw MismatchedInputException.from(p, "Unable to parse Instruction (not an object)");
	}

}

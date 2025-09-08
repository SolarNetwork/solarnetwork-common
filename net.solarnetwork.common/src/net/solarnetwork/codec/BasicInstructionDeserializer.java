/* ==================================================================
 * BasicInstructionDeserializer.java - 5/08/2021 1:39:21 PM
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
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import net.solarnetwork.domain.BasicInstruction;
import net.solarnetwork.domain.BasicInstructionStatus;
import net.solarnetwork.domain.Instruction;
import net.solarnetwork.domain.InstructionStatus;

/**
 * Deserializer for {@link Instruction} instances.
 *
 * @author matt
 * @version 1.1
 * @since 2.0
 */
public class BasicInstructionDeserializer extends StdScalarDeserializer<Instruction>
		implements Serializable {

	private static final long serialVersionUID = -1844182290390256234L;

	/** A default instance . */
	public static final JsonDeserializer<Instruction> INSTANCE = new BasicInstructionDeserializer();

	/**
	 * Constructor.
	 */
	public BasicInstructionDeserializer() {
		super(Instruction.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public BasicInstruction deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		JsonToken t = p.currentToken();
		if ( t == JsonToken.VALUE_NULL ) {
			return null;
		} else if ( p.isExpectedStartObjectToken() ) {
			Long id = null;
			String topic = null;
			Instant date = null;
			Map<String, List<String>> parameters = null;

			InstructionStatus status = null;
			InstructionStatus.InstructionState state = null;
			Instant statusDate = null;
			Map<String, ?> resultParameters = null;

			while ( (t = p.nextToken()) != JsonToken.END_OBJECT ) {
				String f = p.currentName();
				BasicInstructionField field = BasicInstructionField.FIELD_MAP.get(f);
				if ( field == null ) {
					BasicInstructionStatusField statusField = BasicInstructionStatusField.FIELD_MAP
							.get(f);
					if ( statusField != null ) {
						Object v = statusField.parseValue(p, ctxt);
						switch (statusField) {
							case InstructionState:
								state = (InstructionStatus.InstructionState) v;
								break;

							case StatusDate:
								statusDate = (Instant) v;
								break;

							case ResultParameters:
								resultParameters = (Map<String, ?>) v;
								break;

							default:
								// ignore

						}
					} else {
						p.nextToken();
						p.skipChildren();
					}
					continue;
				}
				Object v = field.parseValue(p, ctxt);
				switch (field) {
					case Id:
						id = (Long) v;
						break;

					case Topic:
						topic = (String) v;
						break;

					case InstructionDate:
						date = (Instant) v;
						break;

					case Params:
					case Parameters:
						parameters = (Map<String, List<String>>) v;
						break;

					case Status:
						status = (InstructionStatus) v;
						break;

				}
			}

			// jump to end object
			while ( (t = p.currentToken()) != JsonToken.END_OBJECT && t != null ) {
				t = p.nextToken();
			}
			if ( status == null && state != null ) {
				status = new BasicInstructionStatus(id, state, statusDate, resultParameters);
			}
			BasicInstruction result = new BasicInstruction(id, topic,
					date != null ? date : Instant.now(), status);
			if ( parameters != null ) {
				for ( Entry<String, List<String>> e : parameters.entrySet() ) {
					final String paramName = e.getKey();
					for ( String paramValue : e.getValue() ) {
						result.addParameter(paramName, paramValue);
					}
				}
			}
			return result;
		}
		throw new JsonParseException(p, "Unable to parse Instruction (not an object)");
	}

}

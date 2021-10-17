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

package net.solarnetwork.codec;

import java.io.IOException;
import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import net.solarnetwork.domain.BasicInstructionStatus;
import net.solarnetwork.domain.InstructionStatus;

/**
 * Deserializer for {@link InstructionStatus} instances.
 * 
 * @author matt
 * @version 1.0
 * @since 2.0
 */
public class BasicInstructionStatusDeserializer extends StdScalarDeserializer<InstructionStatus>
		implements Serializable {

	private static final long serialVersionUID = -5321961973815076686L;

	/** A default instance . */
	public static final JsonDeserializer<InstructionStatus> INSTANCE = new BasicInstructionStatusDeserializer();

	/**
	 * Constructor.
	 */
	public BasicInstructionStatusDeserializer() {
		super(InstructionStatus.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public InstructionStatus deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		JsonToken t = p.currentToken();
		if ( t == JsonToken.VALUE_NULL ) {
			return null;
		} else if ( p.isExpectedStartObjectToken() ) {
			Long id = null;
			InstructionStatus.InstructionState state = null;
			Instant statusDate = null;
			Map<String, ?> resultParameters = null;

			String f;
			while ( (f = p.nextFieldName()) != null ) {
				BasicInstructionStatusField statusField = BasicInstructionStatusField.FIELD_MAP.get(f);
				if ( statusField == null ) {
					p.nextToken();
					continue;
				}
				Object v = statusField.parseValue(p, ctxt);
				switch (statusField) {
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
			while ( (t = p.currentToken()) != JsonToken.END_OBJECT ) {
				t = p.nextToken();
			}
			return new BasicInstructionStatus(id, state, statusDate, resultParameters);
		}
		throw new JsonParseException(p, "Unable to parse Instruction (not an object)");
	}

}

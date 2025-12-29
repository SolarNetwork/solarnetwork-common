/* ==================================================================
 * BasicInstructionSerializer.java - 11/08/2021 3:17:48 PM
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

import java.util.List;
import java.util.Map;
import net.solarnetwork.domain.Instruction;
import net.solarnetwork.domain.InstructionStatus;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * Serializer for {@link Instruction} instances.
 *
 * @author matt
 * @version 1.0
 * @since 4.13
 */
public class BasicInstructionSerializer extends StdSerializer<Instruction> {

	/** A default instance. */
	public static final ValueSerializer<Instruction> INSTANCE = new BasicInstructionSerializer();

	/**
	 * Constructor.
	 */
	public BasicInstructionSerializer() {
		super(Instruction.class);
	}

	@Override
	public void serialize(Instruction value, JsonGenerator gen, SerializationContext provider)
			throws JacksonException {
		if ( value == null ) {
			gen.writeNull();
			return;
		}

		final Map<String, List<String>> params = value.getParameterMultiMap();
		final InstructionStatus status = value.getStatus();

		// @formatter:off
		final int size = (value.getId() != null ? 1 : 0)
				+ (value.getTopic() != null ? 1 : 0)
				+ (value.getInstructionDate() != null ? 1 : 0)
				+ (params != null && !params.isEmpty() ? 1 : 0)
				+ (status != null ? (
						  (status.getInstructionState() != null ? 1 : 0)
						+ (status.getStatusDate() != null ? 1 : 0)
						+ (status.getResultParameters() != null ? 1 : 0)
					) : 0)
				;
		// @formatter:on
		gen.writeStartObject(value, size);
		BasicInstructionField.Id.writeValue(gen, provider, value.getId());
		BasicInstructionField.Topic.writeValue(gen, provider, value.getTopic());
		BasicInstructionField.InstructionDate.writeValue(gen, provider, value.getInstructionDate());
		BasicInstructionField.Params.writeValue(gen, provider, params);
		BasicInstructionField.Status.writeValue(gen, provider, value.getStatus());
		gen.writeEndObject();
	}

}

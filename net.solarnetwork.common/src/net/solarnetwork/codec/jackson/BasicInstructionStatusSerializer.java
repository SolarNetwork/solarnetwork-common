/* ==================================================================
 * BasicInstructionStatusSerializer.java - 5/09/2021 5:01:26 PM
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

import net.solarnetwork.domain.InstructionStatus;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * Serializer for {@link InstructionStatus} instances.
 *
 * @author matt
 * @version 1.0
 * @since 4.13
 */
public class BasicInstructionStatusSerializer extends StdSerializer<InstructionStatus> {

	/** A default instance. */
	public static final ValueSerializer<InstructionStatus> INSTANCE = new BasicInstructionStatusSerializer();

	/** A default embedded instance. */
	public static final ValueSerializer<InstructionStatus> EMBEDDED_INSTANCE = new BasicInstructionStatusSerializer(
			true);

	/** The embedded flag. */
	private final boolean embedded;

	/**
	 * Constructor.
	 *
	 * <p>
	 * The {@code embedded} property will be set to {@literal false}.
	 * </p>
	 */
	public BasicInstructionStatusSerializer() {
		this(false);
	}

	/**
	 * Constructor.
	 *
	 * @param embedded
	 *        {@literal true} to generate fields directly without any outer
	 *        object, to embed within an instruction; {@literal false} to
	 *        generate a stand-alone object
	 */
	public BasicInstructionStatusSerializer(boolean embedded) {
		super(InstructionStatus.class);
		this.embedded = embedded;
	}

	@Override
	public void serialize(InstructionStatus value, JsonGenerator gen, SerializationContext provider)
			throws JacksonException {
		if ( value == null ) {
			if ( !embedded ) {
				gen.writeNull();
			}
			return;
		}
		if ( !embedded ) {
			final int size = (value.getInstructionId() != null ? 1 : 0)
					+ (value.getInstructionState() != null ? 1 : 0)
					+ (value.getStatusDate() != null ? 1 : 0)
					+ (value.getResultParameters() != null ? 1 : 0);
			gen.writeStartObject(value, size);
			if ( value.getInstructionId() != null ) {
				BasicInstructionStatusField.InstructionId.writeValue(gen, provider,
						value.getInstructionId());
			}
		}
		if ( value.getInstructionState() != null ) {
			BasicInstructionStatusField.InstructionState.writeValue(gen, provider,
					value.getInstructionState());
		}
		if ( value.getStatusDate() != null ) {
			BasicInstructionStatusField.StatusDate.writeValue(gen, provider, value.getStatusDate());
		}
		if ( value.getResultParameters() != null ) {
			BasicInstructionStatusField.ResultParameters.writeValue(gen, provider,
					value.getResultParameters());
		}
		if ( !embedded ) {
			gen.writeEndObject();
		}
	}

}

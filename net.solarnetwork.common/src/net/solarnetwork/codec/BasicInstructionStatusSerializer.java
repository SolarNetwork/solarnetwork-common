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

package net.solarnetwork.codec;

import java.io.IOException;
import java.io.Serializable;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import net.solarnetwork.domain.InstructionStatus;

/**
 * Serializer for {@link InstructionStatus} instances.
 *
 * @author matt
 * @version 1.2
 * @since 2.0
 */
public class BasicInstructionStatusSerializer extends StdScalarSerializer<InstructionStatus>
		implements Serializable {

	private static final long serialVersionUID = 3557353976716434922L;

	/** A default instance. */
	public static final JsonSerializer<InstructionStatus> INSTANCE = new BasicInstructionStatusSerializer();

	/** A default embedded instance. */
	public static final JsonSerializer<InstructionStatus> EMBEDDED_INSTANCE = new BasicInstructionStatusSerializer(
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
	public void serialize(InstructionStatus value, JsonGenerator gen, SerializerProvider provider)
			throws IOException {
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

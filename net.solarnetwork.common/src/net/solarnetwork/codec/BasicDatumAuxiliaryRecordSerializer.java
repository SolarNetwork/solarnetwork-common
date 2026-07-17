/* ==================================================================
 * BasicDatumAuxiliaryRecordSerializer.java - 18/07/2026 8:09:17 am
 *
 * Copyright 2026 SolarNetwork.net Dev Team
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
import java.io.Serial;
import org.jspecify.annotations.Nullable;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import net.solarnetwork.domain.datum.DatumAuxiliaryRecord;

/**
 * Serializer for {@link DatumAuxiliaryRecord} instances.
 *
 * @author matt
 * @version 1.0
 * @since 4.44
 */
public class BasicDatumAuxiliaryRecordSerializer extends StdSerializer<DatumAuxiliaryRecord> {

	@Serial
	private static final long serialVersionUID = -3905722754162128887L;

	/** A default instance. */
	public static final JsonSerializer<DatumAuxiliaryRecord> INSTANCE = new BasicDatumAuxiliaryRecordSerializer();

	/**
	 * Constructor.
	 */
	public BasicDatumAuxiliaryRecordSerializer() {
		super(DatumAuxiliaryRecord.class);
	}

	@Override
	public void serialize(@Nullable DatumAuxiliaryRecord value, JsonGenerator gen,
			SerializerProvider provider) throws IOException {
		if ( value == null ) {
			gen.writeNull();
			return;
		}
		// @formatter:off
		gen.writeStartObject(5
				+ (value.getNotes() != null ? 1 : 0)
				+ (value.getMetadata() != null ? 1 : 0)
				+ (value.getSamplesFinal() != null ? 1 : 0)
				+ (value.getSamplesStart() != null ? 1 : 0)
				);
		// @formatter:on
		gen.writeStringField("type", value.getType() != null ? value.getType().name() : null);
		gen.writeStringField("kind", value.getKind() != null ? value.getKind().keyValue() : null);
		gen.writeNumberField("objectId", value.getObjectId());
		gen.writeStringField("sourceId", value.getSourceId());
		gen.writeObjectField("timestamp", value.getTimestamp());

		if ( value.getNotes() != null ) {
			gen.writeStringField("notes", value.getNotes());
		}
		if ( value.getMetadata() != null ) {
			gen.writeObjectField("metadata", value.getMetadata());
		}
		if ( value.getSamplesFinal() != null ) {
			gen.writeObjectField("samplesFinal", value.getSamplesFinal());
		}
		if ( value.getSamplesStart() != null ) {
			gen.writeObjectField("samplesStart", value.getSamplesStart());
		}

		gen.writeEndObject();
	}

}

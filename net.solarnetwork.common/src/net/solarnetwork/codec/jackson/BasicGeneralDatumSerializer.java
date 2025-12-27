/* ==================================================================
 * BasicGeneralDatumSerializer.java - 17/08/2021 2:11:16 PM
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

import static net.solarnetwork.util.DateUtils.ISO_DATE_TIME_ALT_UTC;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;
import net.solarnetwork.domain.datum.Datum;
import net.solarnetwork.domain.datum.DatumSamplesOperations;
import net.solarnetwork.domain.datum.DatumSamplesType;
import net.solarnetwork.domain.datum.ObjectDatumKind;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * Serializer for {@link Datum} instances.
 *
 * @author matt
 * @version 1.0
 * @since 4.13
 */
public class BasicGeneralDatumSerializer extends StdSerializer<Datum> {

	/** A default instance. */
	public static final ValueSerializer<Datum> INSTANCE = new BasicGeneralDatumSerializer();

	/**
	 * Constructor.
	 */
	public BasicGeneralDatumSerializer() {
		super(Datum.class);
	}

	@Override
	public void serialize(Datum value, JsonGenerator gen, SerializationContext provider)
			throws JacksonException {
		gen.writeStartObject(7);
		if ( value.getTimestamp() != null ) {
			gen.writeStringProperty("created",
					ISO_DATE_TIME_ALT_UTC.format(value.getTimestamp().truncatedTo(ChronoUnit.MILLIS)));
		}
		if ( value.getKind() == ObjectDatumKind.Node && value.getObjectId() != null ) {
			gen.writeNumberProperty("nodeId", value.getObjectId());
		} else if ( value.getKind() == ObjectDatumKind.Location && value.getObjectId() != null ) {
			gen.writeNumberProperty("locationId", value.getObjectId());
		}
		if ( value.getSourceId() != null ) {
			gen.writeStringProperty("sourceId", value.getSourceId());
		}

		DatumSamplesOperations ops = value.asSampleOperations();
		for ( DatumSamplesType t : DatumSamplesType.values() ) {
			if ( t == DatumSamplesType.Tag ) {
				Set<String> tags = ops.getTags();
				if ( tags != null && !tags.isEmpty() ) {
					String[] tagsArray = tags.toArray(new String[tags.size()]);
					JsonUtils.writeStringArrayField(gen, "t", tagsArray);
				}
			} else {
				Map<String, ?> data = ops.getSampleData(t);
				if ( data != null && !data.isEmpty() ) {
					gen.writePOJOProperty(String.valueOf(t.toKey()), data);
				}
			}
		}

		gen.writeEndObject();
	}

}

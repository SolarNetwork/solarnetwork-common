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

package net.solarnetwork.codec;

import static net.solarnetwork.util.DateUtils.ISO_DATE_TIME_ALT_UTC;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import net.solarnetwork.domain.GeneralDatumSamplesOperations;
import net.solarnetwork.domain.GeneralDatumSamplesType;
import net.solarnetwork.domain.datum.GeneralDatum;

/**
 * Serializer for {@link GeneralDatum} instances.
 * 
 * @author matt
 * @version 1.0
 * @since 1.78
 */
public class BasicGeneralDatumSerializer extends StdScalarSerializer<GeneralDatum> {

	/** A default instance. */
	public static final JsonSerializer<GeneralDatum> INSTANCE = new BasicGeneralDatumSerializer();

	private static final long serialVersionUID = -5820173690461042501L;

	/**
	 * Constructor.
	 */
	public BasicGeneralDatumSerializer() {
		super(GeneralDatum.class);
	}

	@Override
	public void serialize(GeneralDatum value, JsonGenerator gen, SerializerProvider provider)
			throws IOException {
		gen.writeStartObject(7);
		if ( value.getTimestamp() != null ) {
			gen.writeStringField("created", ISO_DATE_TIME_ALT_UTC.format(value.getTimestamp()));
		}
		if ( value.getSourceId() != null ) {
			gen.writeStringField("sourceId", value.getSourceId());
		}

		GeneralDatumSamplesOperations ops = value.asSampleOperations();
		for ( GeneralDatumSamplesType t : GeneralDatumSamplesType.values() ) {
			if ( t == GeneralDatumSamplesType.Tag ) {
				Set<String> tags = ops.getTags();
				if ( tags != null && !tags.isEmpty() ) {
					String[] tagsArray = tags.toArray(new String[tags.size()]);
					JsonUtils.writeStringArrayField(gen, "t", tagsArray);
				}
			} else {
				Map<String, ?> data = ops.getSampleData(t);
				if ( data != null && !data.isEmpty() ) {
					gen.writeObjectField(String.valueOf(t.toKey()), data);
				}
			}
		}

		gen.writeEndObject();
	}

}

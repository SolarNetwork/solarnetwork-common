/* ==================================================================
 * BasicStreamDatumArraySerializer.java - 4/06/2021 5:12:29 PM
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

import java.util.UUID;
import net.solarnetwork.domain.datum.BasicStreamDatum;
import net.solarnetwork.domain.datum.DatumProperties;
import net.solarnetwork.domain.datum.StreamDatum;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * Serializer for {@link BasicStreamDatum} instance to an array.
 *
 * @author matt
 * @version 1.0
 * @since 4.13
 */
public class BasicStreamDatumArraySerializer extends StdSerializer<StreamDatum> {

	/** A default instance. */
	public static final ValueSerializer<StreamDatum> INSTANCE = new BasicStreamDatumArraySerializer();

	/**
	 * Constructor.
	 */
	public BasicStreamDatumArraySerializer() {
		super(StreamDatum.class);
	}

	@Override
	public void serialize(StreamDatum datum, JsonGenerator generator, SerializationContext provider)
			throws JacksonException {
		generator.writeStartArray(datum, 7);

		// 1: timestamp
		if ( datum.getTimestamp() != null ) {
			generator.writeNumber(datum.getTimestamp().toEpochMilli());
		} else {
			generator.writeNull();
		}

		// 2-3: UUID high,low
		UUID streamId = datum.getStreamId();
		if ( streamId != null ) {
			generator.writeNumber(streamId.getMostSignificantBits());
			generator.writeNumber(streamId.getLeastSignificantBits());
		} else {
			generator.writeNull();
			generator.writeNull();
		}

		DatumProperties props = datum.getProperties();

		// 4: array of i props
		JsonUtils.writeDecimalArray(generator, props.getInstantaneous());

		// 5: array of a props
		JsonUtils.writeDecimalArray(generator, props.getAccumulating());

		// 6: array of s props
		JsonUtils.writeStringArray(generator, props.getStatus());

		// 7: array of tags
		JsonUtils.writeStringArray(generator, props.getTags());

		generator.writeEndArray();
	}

}

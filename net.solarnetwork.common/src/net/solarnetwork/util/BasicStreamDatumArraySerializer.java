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

package net.solarnetwork.util;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import net.solarnetwork.domain.datum.BasicStreamDatum;
import net.solarnetwork.domain.datum.DatumProperties;
import net.solarnetwork.domain.datum.StreamDatum;

/**
 * Serializer for {@link BasicStreamDatum} instance to an array.
 * 
 * @author matt
 * @version 1.0
 * @since 1.72
 */
public class BasicStreamDatumArraySerializer extends StdScalarSerializer<StreamDatum>
		implements Serializable {

	/** A default instance. */
	public static final BasicStreamDatumArraySerializer INSTANCE = new BasicStreamDatumArraySerializer();

	private static final long serialVersionUID = -4548263284532264499L;

	/**
	 * Constructor.
	 */
	public BasicStreamDatumArraySerializer() {
		super(StreamDatum.class);
	}

	@Override
	public void serialize(StreamDatum datum, JsonGenerator generator, SerializerProvider provider)
			throws IOException, JsonGenerationException {
		generator.writeStartArray(7);

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
		int len = props.getInstantaneousLength();
		if ( len > 0 ) {
			generator.writeStartArray(len);
			BigDecimal[] data = props.getInstantaneous();
			for ( int i = 0; i < len; i++ ) {
				generator.writeNumber(data[i]);
			}
			generator.writeEndArray();
		} else {
			generator.writeNull();
		}

		// 5: array of a props
		len = props.getAccumulatingLength();
		if ( len > 0 ) {
			generator.writeStartArray(len);
			BigDecimal[] data = props.getAccumulating();
			for ( int i = 0; i < len; i++ ) {
				generator.writeNumber(data[i]);
			}
			generator.writeEndArray();
		} else {
			generator.writeNull();
		}

		// 6: array of s props
		len = props.getStatusLength();
		if ( len > 0 ) {
			generator.writeStartArray(len);
			String[] data = props.getStatus();
			for ( int i = 0; i < len; i++ ) {
				generator.writeString(data[i]);
			}
			generator.writeEndArray();
		} else {
			generator.writeNull();
		}

		// 7: array of tags
		len = props.getTagsLength();
		if ( len > 0 ) {
			generator.writeStartArray(len);
			String[] data = props.getTags();
			for ( int i = 0; i < len; i++ ) {
				generator.writeString(data[i]);
			}
			generator.writeEndArray();
		} else {
			generator.writeNull();
		}

		generator.writeEndArray();
	}

}

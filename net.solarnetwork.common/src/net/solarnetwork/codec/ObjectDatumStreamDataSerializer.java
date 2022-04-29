/* ==================================================================
 * ObjectDatumStreamDataSerializer.java - 29/04/2022 11:13:40 AM
 * 
 * Copyright 2022 SolarNetwork.net Dev Team
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

import static net.solarnetwork.codec.JsonUtils.writeDecimalArrayValues;
import static net.solarnetwork.codec.JsonUtils.writeStringArrayValues;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import net.solarnetwork.domain.datum.DatumProperties;
import net.solarnetwork.domain.datum.DatumSamplesType;
import net.solarnetwork.domain.datum.ObjectDatumStreamData;
import net.solarnetwork.domain.datum.ObjectDatumStreamMetadata;
import net.solarnetwork.domain.datum.StreamDatum;

/**
 * Serializer for {@link ObjectDatumStreamData}.
 * 
 * @author matt
 * @version 1.0
 * @since 2.4
 */
public class ObjectDatumStreamDataSerializer extends StdScalarSerializer<ObjectDatumStreamData>
		implements Serializable {

	private static final long serialVersionUID = 3073007559757187920L;

	/** A default instance. */
	public static final ObjectDatumStreamDataSerializer INSTANCE = new ObjectDatumStreamDataSerializer();

	/**
	 * Constructor.
	 */
	public ObjectDatumStreamDataSerializer() {
		super(ObjectDatumStreamData.class);
	}

	@Override
	public void serialize(ObjectDatumStreamData value, JsonGenerator gen, SerializerProvider provider)
			throws IOException {
		gen.writeStartObject(value, 2);

		final ObjectDatumStreamMetadata meta = value.getMetadata();
		final String[] iNames = meta.propertyNamesForType(DatumSamplesType.Instantaneous);
		final String[] aNames = meta.propertyNamesForType(DatumSamplesType.Accumulating);
		final String[] sNames = meta.propertyNamesForType(DatumSamplesType.Status);
		final int iLen = (iNames != null ? iNames.length : 0);
		final int aLen = (aNames != null ? aNames.length : 0);
		final int sLen = (sNames != null ? sNames.length : 0);
		final int baseLen = (1 + iLen + aLen + sLen);

		gen.writeFieldName("meta");
		BasicObjectDatumStreamMetadataSerializer.INSTANCE.serialize(meta, gen, provider);

		gen.writeFieldName("data");

		final Collection<StreamDatum> data = value.getData();

		gen.writeStartArray(data.size());
		for ( StreamDatum d : data ) {
			final DatumProperties p = d.getProperties();
			final long ts = (d.getTimestamp() != null ? d.getTimestamp().toEpochMilli() : 0);
			int tLen = (p != null ? p.getTagsLength() : 0);
			int totalLen = baseLen + tLen;

			gen.writeStartArray(totalLen);
			gen.writeNumber(ts);
			if ( p != null ) {
				writeDecimalArrayValues(gen, p.getInstantaneous(), iLen);
				writeDecimalArrayValues(gen, p.getAccumulating(), aLen);
				writeStringArrayValues(gen, p.getStatus(), sLen);
				writeStringArrayValues(gen, p.getTags(), tLen);
			}
			gen.writeEndArray();
		}
		gen.writeEndArray();

		gen.writeEndObject();
	}

}

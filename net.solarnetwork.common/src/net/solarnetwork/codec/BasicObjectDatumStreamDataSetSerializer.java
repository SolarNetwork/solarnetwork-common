/* ==================================================================
 * BasicObjectDatumStreamDataSetSerializer.java - 30/04/2022 9:43:09 am
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import net.solarnetwork.domain.datum.DatumProperties;
import net.solarnetwork.domain.datum.DatumSamplesType;
import net.solarnetwork.domain.datum.ObjectDatumStreamDataSet;
import net.solarnetwork.domain.datum.ObjectDatumStreamMetadata;
import net.solarnetwork.domain.datum.StreamDatum;

/**
 * Serializer for {@link ObjectDatumStreamDataSet}.
 * 
 * <p>
 * This generates a JSON object with the following form ({@literal null} values
 * are omitted}:
 * </p>
 * 
 * <pre>
 * <code>{
 *   "returnedResultCount" : &lt;count&gt;,
 *   "startingOffset"      : &lt;offset&gt;,
 *   "totalResultCount"    : &lt;count&gt;,
 *   "meta" : [
 *     {
 *       // ObjectDatumStreamMetadata
 *     },
 *     ...
 *   ],
 *   "data" : [
 *     [&lt;meta index&gt;, &lt;timestamp&gt;, &lt;i data&gt;..., &lt;a data&gt;..., &lt;s data&gt;..., &lt;tags*&gt;...],
 *     ...
 *   ]
 * }</code>
 * </pre>
 * 
 * @author matt
 * @version 1.0
 * @since 2.4
 */
public class BasicObjectDatumStreamDataSetSerializer
		extends StdScalarSerializer<ObjectDatumStreamDataSet> implements Serializable {

	private static final long serialVersionUID = -4226762305023708915L;

	/** A default instance. */
	public static final BasicObjectDatumStreamDataSetSerializer INSTANCE = new BasicObjectDatumStreamDataSetSerializer();

	/** The metadata array field name. */
	public static final SerializedString META_FIELD_NAME = new SerializedString("meta");

	/** The data array field name. */
	public static final SerializedString DATA_FIELD_NAME = new SerializedString("data");

	/** The returned result count field name. */
	public static final SerializedString RETURNED_RESULT_COUNT_FIELD_NAME = new SerializedString(
			"returnedResultCount");

	/** The starting offset field name. */
	public static final SerializedString STARTING_OFFSET_FIELD_NAME = new SerializedString(
			"startingOffset");

	/** The total result count field name. */
	public static final SerializedString TOTAL_RESULT_COUNT_FIELD_NAME = new SerializedString(
			"totalResultCount");

	/**
	 * Constructor.
	 */
	public BasicObjectDatumStreamDataSetSerializer() {
		super(ObjectDatumStreamDataSet.class);
	}

	@Override
	public void serialize(ObjectDatumStreamDataSet value, JsonGenerator gen, SerializerProvider provider)
			throws IOException {
		final Collection<UUID> streamIds = value.metadataStreamIds();
		final Map<UUID, Integer> metaIndexMap = new HashMap<>(streamIds.size());
		final Iterator<StreamDatum> itr = value.iterator();
		int count = (value.getReturnedResultCount() != null ? 1 : 0)
				+ (value.getStartingOffset() != null ? 1 : 0)
				+ (value.getTotalResultCount() != null ? 1 : 0)
				+ (streamIds != null && !streamIds.isEmpty() ? 1 : 0) + (itr.hasNext() ? 1 : 0);

		gen.writeStartObject(value, count);

		if ( value.getReturnedResultCount() != null ) {
			gen.writeFieldName(RETURNED_RESULT_COUNT_FIELD_NAME);
			gen.writeNumber(value.getReturnedResultCount());
		}
		if ( value.getStartingOffset() != null ) {
			gen.writeFieldName(STARTING_OFFSET_FIELD_NAME);
			gen.writeNumber(value.getStartingOffset());
		}
		if ( value.getTotalResultCount() != null ) {
			gen.writeFieldName(TOTAL_RESULT_COUNT_FIELD_NAME);
			gen.writeNumber(value.getTotalResultCount());
		}

		int i = 0;

		if ( streamIds != null && !streamIds.isEmpty() ) {
			gen.writeFieldName(META_FIELD_NAME);
			gen.writeStartArray(streamIds.size());
			for ( UUID streamId : streamIds ) {
				metaIndexMap.put(streamId, i);
				BasicObjectDatumStreamMetadataSerializer.INSTANCE
						.serialize(value.metadataForStreamId(streamId), gen, provider);
				i++;
			}
			gen.writeEndArray();
		}
		if ( itr.hasNext() ) {
			gen.writeFieldName(DATA_FIELD_NAME);
			gen.writeStartArray();
			i = 0;
			while ( itr.hasNext() ) {
				final StreamDatum d = itr.next();
				final ObjectDatumStreamMetadata meta = value.metadataForStreamId(d.getStreamId());
				if ( meta == null ) {
					throw new JsonMappingException(gen, String.format(
							"Metadata for stream %s not available for datum %d", d.getStreamId(), i));
				}
				final String[] iNames = meta.propertyNamesForType(DatumSamplesType.Instantaneous);
				final String[] aNames = meta.propertyNamesForType(DatumSamplesType.Accumulating);
				final String[] sNames = meta.propertyNamesForType(DatumSamplesType.Status);
				final int iLen = (iNames != null ? iNames.length : 0);
				final int aLen = (aNames != null ? aNames.length : 0);
				final int sLen = (sNames != null ? sNames.length : 0);
				final int baseLen = (1 + iLen + aLen + sLen);
				final DatumProperties p = d.getProperties();
				final long ts = (d.getTimestamp() != null ? d.getTimestamp().toEpochMilli() : 0);
				int tLen = (p != null ? p.getTagsLength() : 0);
				int totalLen = 1 + baseLen + tLen;

				gen.writeStartArray(totalLen);
				gen.writeNumber(metaIndexMap.get(d.getStreamId()));
				gen.writeNumber(ts);
				if ( p != null ) {
					writeDecimalArrayValues(gen, p.getInstantaneous(), iLen);
					writeDecimalArrayValues(gen, p.getAccumulating(), aLen);
					writeStringArrayValues(gen, p.getStatus(), sLen);
					writeStringArrayValues(gen, p.getTags(), tLen);
				}
				gen.writeEndArray();
				i++;
			}
			gen.writeEndArray();
		}

		gen.writeEndObject();
	}

}

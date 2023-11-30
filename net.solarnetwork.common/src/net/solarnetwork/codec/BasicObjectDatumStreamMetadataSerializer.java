/* ==================================================================
 * BasicObjectDatumStreamMetadataSerializer.java - 5/06/2021 8:00:28 PM
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
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import net.solarnetwork.domain.datum.DatumSamplesType;
import net.solarnetwork.domain.datum.ObjectDatumStreamMetadata;

/**
 * Serializer for {@link ObjectDatumStreamMetadata}.
 * 
 * <p>
 * Note that the {@link ObjectDatumStreamMetadata#getMetaJson()} is <b>not</b>
 * serialized.
 * </p>
 * 
 * @author matt
 * @version 2.1
 * @since 1.72
 */
public class BasicObjectDatumStreamMetadataSerializer
		extends StdScalarSerializer<ObjectDatumStreamMetadata> implements Serializable {

	private static final long serialVersionUID = -1844182290390256234L;

	/** A default instance. */
	public static final BasicObjectDatumStreamMetadataSerializer INSTANCE = new BasicObjectDatumStreamMetadataSerializer();

	/**
	 * Constructor.
	 */
	public BasicObjectDatumStreamMetadataSerializer() {
		super(ObjectDatumStreamMetadata.class);
	}

	@Override
	public void serialize(ObjectDatumStreamMetadata meta, JsonGenerator generator,
			SerializerProvider provider) throws IOException, JsonGenerationException {

		// @formatter:off
		final int size = 
				  (meta.getStreamId() != null ? 1 : 0)
				+ (meta.getTimeZoneId() != null ? 1 : 0)
				+ (meta.getKind() != null ? 1 : 0)
				+ (meta.getObjectId() != null ? 1 : 0)
				+ (meta.getSourceId() != null ? 1 : 0)
				+ (meta.getLocation() != null ? 1 : 0)
				+ (meta.propertyNamesForType(DatumSamplesType.Instantaneous) != null ? 1 : 0)
				+ (meta.propertyNamesForType(DatumSamplesType.Accumulating) != null ? 1 : 0)
				+ (meta.propertyNamesForType(DatumSamplesType.Status) != null ? 1 : 0)
				;
		// @formatter:on

		generator.writeStartObject(meta, size);

		BasicObjectDatumStreamMetadataField.StreamId.writeValue(generator, provider, meta.getStreamId());
		BasicObjectDatumStreamMetadataField.TimeZoneId.writeValue(generator, provider,
				meta.getTimeZoneId());
		BasicObjectDatumStreamMetadataField.ObjectDatumKind.writeValue(generator, provider,
				meta.getKind());
		BasicObjectDatumStreamMetadataField.ObjectId.writeValue(generator, provider, meta.getObjectId());
		BasicObjectDatumStreamMetadataField.SourceId.writeValue(generator, provider, meta.getSourceId());
		BasicObjectDatumStreamMetadataField.Location.writeValue(generator, provider, meta.getLocation());
		BasicObjectDatumStreamMetadataField.Instantaneous.writeValue(generator, provider,
				meta.propertyNamesForType(DatumSamplesType.Instantaneous));
		BasicObjectDatumStreamMetadataField.Accumulating.writeValue(generator, provider,
				meta.propertyNamesForType(DatumSamplesType.Accumulating));
		BasicObjectDatumStreamMetadataField.Status.writeValue(generator, provider,
				meta.propertyNamesForType(DatumSamplesType.Status));

		generator.writeEndObject();
	}

}

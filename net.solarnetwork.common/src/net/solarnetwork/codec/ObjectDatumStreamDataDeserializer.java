/* ==================================================================
 * ObjectDatumStreamDataDeserializer.java - 29/04/2022 11:13:40 AM
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

import static java.time.Instant.ofEpochMilli;
import static net.solarnetwork.domain.datum.DatumProperties.propertiesOf;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import net.solarnetwork.domain.datum.BasicStreamDatum;
import net.solarnetwork.domain.datum.DatumSamplesType;
import net.solarnetwork.domain.datum.ObjectDatumStreamData;
import net.solarnetwork.domain.datum.ObjectDatumStreamMetadata;
import net.solarnetwork.domain.datum.StreamDatum;

/**
 * Deserializer for {@link ObjectDatumStreamData} instances.
 * 
 * @author matt
 * @version 1.0
 * @since 2.4
 */
public class ObjectDatumStreamDataDeserializer extends StdScalarDeserializer<ObjectDatumStreamData> {

	private static final long serialVersionUID = -4563457706145963574L;

	/** A default instance. */
	public static final ObjectDatumStreamDataDeserializer INSTANCE = new ObjectDatumStreamDataDeserializer();

	private static final SerializedString META_FIELD_NAME = new SerializedString("meta");
	private static final SerializedString DATA_FIELD_NAME = new SerializedString("data");

	/**
	 * Constructor.
	 */
	public ObjectDatumStreamDataDeserializer() {
		super(ObjectDatumStreamData.class);
	}

	@Override
	public ObjectDatumStreamData deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		JsonToken t = p.currentToken();
		if ( t == JsonToken.VALUE_NULL ) {
			return null;
		} else if ( p.isExpectedStartObjectToken() && p.nextFieldName(META_FIELD_NAME)
				&& p.nextToken() == JsonToken.START_OBJECT ) {
			final ObjectDatumStreamMetadata meta = BasicObjectDatumStreamMetadataDeserializer.INSTANCE
					.deserialize(p, ctxt);
			final List<StreamDatum> data = new ArrayList<>(32);
			final String[] iNames = meta.propertyNamesForType(DatumSamplesType.Instantaneous);
			final String[] aNames = meta.propertyNamesForType(DatumSamplesType.Accumulating);
			final String[] sNames = meta.propertyNamesForType(DatumSamplesType.Status);
			final int iLen = (iNames != null ? iNames.length : 0);
			final int aLen = (aNames != null ? aNames.length : 0);
			final int sLen = (sNames != null ? sNames.length : 0);
			final int aStart = iLen;
			final int sStart = aStart + aLen;
			final int tStart = sStart + sLen;
			if ( p.nextFieldName(DATA_FIELD_NAME) && p.nextToken() == JsonToken.START_ARRAY ) {
				while ( p.nextToken() == JsonToken.START_ARRAY ) {
					int i = -1;
					long ts = 0;
					BigDecimal[] iData = (iLen > 0 ? new BigDecimal[iLen] : null);
					BigDecimal[] aData = (aLen > 0 ? new BigDecimal[aLen] : null);
					String[] sData = (sLen > 0 ? new String[sLen] : null);
					List<String> tags = null;
					for ( t = p.nextToken(); t != null
							&& t != JsonToken.END_ARRAY; t = p.nextToken(), i++ ) {
						if ( i == -1 ) {
							ts = p.getLongValue();
						} else if ( iLen > 0 && i < aStart ) {
							iData[i] = (t == JsonToken.VALUE_NULL ? null : p.getDecimalValue());
						} else if ( aLen > 0 && i < sStart ) {
							aData[i - aStart] = (t == JsonToken.VALUE_NULL ? null : p.getDecimalValue());
						} else if ( sLen > 0 && i < tStart ) {
							sData[i - sStart] = (t == JsonToken.VALUE_NULL ? null : p.getText());
						} else if ( t == JsonToken.VALUE_STRING ) {
							if ( tags == null ) {
								tags = new ArrayList<>(4);
							}
							tags.add(p.getText());
						}
					}
					data.add(new BasicStreamDatum(meta.getStreamId(), ofEpochMilli(ts),
							propertiesOf(iData, aData, sData,
									tags != null ? tags.toArray(new String[tags.size()]) : null)));
				}
			}
			p.nextToken(); // advance to final end-object '}'
			return new ObjectDatumStreamData(meta, data);
		}
		throw new JsonParseException(p, "Unable to parse StreamDatum (not an array)");
	}

}

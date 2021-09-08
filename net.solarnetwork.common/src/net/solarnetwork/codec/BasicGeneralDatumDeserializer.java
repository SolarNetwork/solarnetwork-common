/* ==================================================================
 * BasicGeneralDatumDeserializer.java - 17/08/2021 2:34:49 PM
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
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Map.Entry;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import net.solarnetwork.domain.datum.Datum;
import net.solarnetwork.domain.datum.DatumId;
import net.solarnetwork.domain.datum.DatumSamples;
import net.solarnetwork.domain.datum.DatumSamplesType;
import net.solarnetwork.domain.datum.GeneralDatum;
import net.solarnetwork.domain.datum.ObjectDatumKind;
import net.solarnetwork.util.DateUtils;

/**
 * Deserializer for {@link Datum} objects
 * 
 * @author matt
 * @version 1.0
 * @since 2.0
 */
public class BasicGeneralDatumDeserializer extends StdScalarDeserializer<Datum> implements Serializable {

	private static final long serialVersionUID = 3787325819424216521L;

	/** A default instance. */
	public static final JsonDeserializer<Datum> INSTANCE = new BasicGeneralDatumDeserializer();

	/**
	 * Constructor.
	 */
	public BasicGeneralDatumDeserializer() {
		super(Datum.class);
	}

	@Override
	public Datum deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		JsonToken t = p.currentToken();
		if ( t == JsonToken.VALUE_NULL ) {
			return null;
		} else if ( p.isExpectedStartObjectToken() ) {
			Instant ts = null;
			String sourceId = null;
			Long objectId = null;
			ObjectDatumKind kind = ObjectDatumKind.Node;
			DatumSamples s = new DatumSamples();

			String field;
			while ( (field = p.nextFieldName()) != null ) {
				switch (field) {
					case "created":
						try {
							ts = DateUtils.ISO_DATE_TIME_ALT_UTC.parse(p.nextTextValue(), Instant::from);
						} catch ( DateTimeParseException e ) {
							throw new JsonParseException(p, "Invalid 'created' date value.",
									p.getCurrentLocation(), e);
						}
						break;

					case "sourceId":
						sourceId = p.nextTextValue();
						break;

					case "locationId":
						objectId = p.nextLongValue(-1);
						kind = ObjectDatumKind.Location;
						break;

					case "i":
						parseSampleMap(p, ctxt, s, DatumSamplesType.Instantaneous);
						break;

					case "a":
						parseSampleMap(p, ctxt, s, DatumSamplesType.Accumulating);
						break;

					case "s":
						parseSampleMap(p, ctxt, s, DatumSamplesType.Status);
						break;

					case "t":
						String[] tags = JsonUtils.parseStringArray(p);
						if ( tags != null && tags.length > 0 ) {
							for ( String tag : tags ) {
								s.addTag(tag);
							}
						}
						break;
				}
			}
			DatumId id = new DatumId(kind, objectId, sourceId, ts);
			return new GeneralDatum(id, s);
		}
		throw new JsonParseException(p, "Unable to parse GeneralDatum (not an object)");
	}

	private void parseSampleMap(JsonParser p, DeserializationContext ctxt, DatumSamples s,
			DatumSamplesType type) throws IOException {
		p.nextToken();
		Map<String, Object> map = p.readValueAs(JsonUtils.STRING_MAP_TYPE);
		if ( map != null && !map.isEmpty() ) {
			for ( Entry<String, Object> e : map.entrySet() ) {
				s.putSampleValue(type, e.getKey(), e.getValue());
			}
		}
	}

}

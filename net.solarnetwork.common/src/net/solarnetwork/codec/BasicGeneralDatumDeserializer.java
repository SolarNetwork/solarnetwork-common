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

import static net.solarnetwork.domain.GeneralDatum.locationDatum;
import static net.solarnetwork.domain.GeneralDatum.nodeDatum;
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
import net.solarnetwork.domain.GeneralDatumSamples;
import net.solarnetwork.domain.GeneralDatumSamplesType;
import net.solarnetwork.domain.datum.GeneralDatum;
import net.solarnetwork.domain.datum.ObjectDatumKind;
import net.solarnetwork.util.DateUtils;

/**
 * Deserializer for {@link GeneralDatum} objects.
 * 
 * <p>
 * Supports both "direct" and "nested" style of sample properties. For example a
 * direct style looks like:
 * </p>
 * 
 * <pre>
 * <code>
 * {"created":"2021-08-17 14:28:12.345Z","sourceId":"foo","i":{"watts":123}}
 * </code>
 * </pre>
 * 
 * <p>
 * while the nested style looks like:
 * </p>
 * 
 * <pre>
 * <code>
 * {"created":3801091820980,"sourceId":"foo","samples":{"i":{"watts":123}}}
 * </code>
 * </pre>
 * 
 * @author matt
 * @version 1.1
 * @since 1.78
 */
public class BasicGeneralDatumDeserializer extends StdScalarDeserializer<GeneralDatum>
		implements Serializable {

	/** A default instance. */
	public static final JsonDeserializer<GeneralDatum> INSTANCE = new BasicGeneralDatumDeserializer();

	private static final long serialVersionUID = 3787325819424216521L;

	/**
	 * Constructor.
	 */
	public BasicGeneralDatumDeserializer() {
		super(GeneralDatum.class);
	}

	@Override
	public GeneralDatum deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		JsonToken t = p.currentToken();
		if ( t == JsonToken.VALUE_NULL ) {
			return null;
		} else if ( p.isExpectedStartObjectToken() ) {
			Instant ts = null;
			String sourceId = null;
			ObjectDatumKind kind = ObjectDatumKind.Node;
			Long objectId = null;
			GeneralDatumSamples s = new GeneralDatumSamples();
			int nestLevel = 1;
			while ( (t = p.nextToken()) != null ) {
				if ( t == JsonToken.END_OBJECT ) {
					if ( --nestLevel < 1 ) {
						break;
					}
					continue;
				} else if ( t == JsonToken.START_OBJECT ) {
					nestLevel++;
					continue;
				}
				if ( t != JsonToken.FIELD_NAME ) {
					continue;
				}
				String field = p.getCurrentName();
				switch (field) {
					case "created":
						try {
							t = p.nextToken();
							if ( t.isNumeric() ) {
								ts = Instant.ofEpochMilli(p.getValueAsLong());
							} else {
								ts = DateUtils.ISO_DATE_TIME_ALT_UTC.parse(p.getText(), Instant::from);
							}
						} catch ( DateTimeParseException e ) {
							throw new JsonParseException(p, "Invalid 'created' date value.",
									p.getCurrentLocation(), e);
						}
						break;

					case "sourceId":
						sourceId = p.nextTextValue();
						break;

					case "nodeId":
						objectId = p.nextLongValue(0);
						kind = ObjectDatumKind.Node;
						break;

					case "locationId":
						objectId = p.nextLongValue(0);
						kind = ObjectDatumKind.Location;
						break;

					case "i":
						parseSampleMap(p, ctxt, s, GeneralDatumSamplesType.Instantaneous);
						break;

					case "a":
						parseSampleMap(p, ctxt, s, GeneralDatumSamplesType.Accumulating);
						break;

					case "s":
						parseSampleMap(p, ctxt, s, GeneralDatumSamplesType.Status);
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
			if ( kind == ObjectDatumKind.Location ) {
				return locationDatum(objectId, sourceId, ts, s);
			}
			return nodeDatum(objectId, sourceId, ts, s);
		}
		throw new JsonParseException(p, "Unable to parse GeneralDatum (not an object)");
	}

	private void parseSampleMap(JsonParser p, DeserializationContext ctxt, GeneralDatumSamples s,
			GeneralDatumSamplesType type) throws IOException {
		p.nextToken();
		Map<String, Object> map = p.readValueAs(JsonUtils.STRING_MAP_TYPE);
		if ( map != null && !map.isEmpty() ) {
			for ( Entry<String, Object> e : map.entrySet() ) {
				s.putSampleValue(type, e.getKey(), e.getValue());
			}
		}
	}

}

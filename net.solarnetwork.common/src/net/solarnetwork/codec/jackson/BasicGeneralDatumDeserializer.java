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

package net.solarnetwork.codec.jackson;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Map.Entry;
import net.solarnetwork.domain.datum.Datum;
import net.solarnetwork.domain.datum.DatumId;
import net.solarnetwork.domain.datum.DatumSamples;
import net.solarnetwork.domain.datum.DatumSamplesType;
import net.solarnetwork.domain.datum.GeneralDatum;
import net.solarnetwork.domain.datum.ObjectDatumKind;
import net.solarnetwork.util.DateUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.std.StdScalarDeserializer;
import tools.jackson.databind.exc.MismatchedInputException;

/**
 * Deserializer for {@link Datum} objects
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
 * @version 1.0
 * @since 4.13
 */
public class BasicGeneralDatumDeserializer extends StdScalarDeserializer<Datum> {

	/** A default instance. */
	public static final ValueDeserializer<Datum> INSTANCE = new BasicGeneralDatumDeserializer();

	/**
	 * Constructor.
	 */
	public BasicGeneralDatumDeserializer() {
		super(Datum.class);
	}

	@Override
	public Datum deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
		JsonToken t = p.currentToken();
		if ( t == JsonToken.VALUE_NULL ) {
			return null;
		} else if ( p.isExpectedStartObjectToken() ) {
			Instant ts = null;
			String sourceId = null;
			ObjectDatumKind kind = null;
			Long objectId = null;
			DatumSamples s = new DatumSamples();
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
				if ( t != JsonToken.PROPERTY_NAME ) {
					continue;
				}
				String field = p.currentName();
				switch (field) {
					case "created":
						try {
							t = p.nextToken();
							if ( t.isNumeric() ) {
								ts = Instant.ofEpochMilli(p.getValueAsLong());
							} else {
								try {
									ts = DateUtils.ISO_DATE_TIME_ALT_UTC.parse(p.getString(),
											Instant::from);
								} catch ( DateTimeParseException e2 ) {
									ZonedDateTime zdt = DateUtils.parseIsoTimestamp(p.getString(),
											ZoneOffset.UTC);
									if ( zdt != null ) {
										ts = zdt.toInstant();
									} else {
										throw e2;
									}
								}
							}
						} catch ( DateTimeParseException e ) {
							throw new StreamReadException(p, "Invalid 'created' date value.",
									p.currentLocation(), e);
						}
						break;

					case "sourceId":
						sourceId = p.nextStringValue();
						break;

					case "nodeId":
						objectId = p.nextLongValue(-1);
						kind = ObjectDatumKind.Node;
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
		throw MismatchedInputException.from(p, "Unable to parse GeneralDatum (not an object)");
	}

	private void parseSampleMap(JsonParser p, DeserializationContext ctxt, DatumSamples s,
			DatumSamplesType type) throws JacksonException {
		p.nextToken();
		Map<String, Object> map = p.readValueAs(JsonUtils.STRING_MAP_TYPE);
		if ( map != null && !map.isEmpty() ) {
			for ( Entry<String, Object> e : map.entrySet() ) {
				s.putSampleValue(type, e.getKey(), e.getValue());
			}
		}
	}

}

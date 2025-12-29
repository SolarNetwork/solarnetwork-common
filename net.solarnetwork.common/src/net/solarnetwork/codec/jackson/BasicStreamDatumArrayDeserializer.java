/* ==================================================================
 * BasicStreamDatumArrayDeserializer.java - 4/06/2021 5:13:40 PM
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

import static java.time.Instant.ofEpochMilli;
import static net.solarnetwork.domain.datum.DatumProperties.propertiesOf;
import java.math.BigDecimal;
import java.util.UUID;
import net.solarnetwork.domain.datum.BasicStreamDatum;
import net.solarnetwork.domain.datum.StreamDatum;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.exc.MismatchedInputException;

/**
 * Deserializer for {@link BasicStreamDatum} arrays.
 *
 * @author matt
 * @version 1.0
 * @since 4.13
 */
public class BasicStreamDatumArrayDeserializer extends StdDeserializer<StreamDatum> {

	/** A default instance. */
	public static final ValueDeserializer<StreamDatum> INSTANCE = new BasicStreamDatumArrayDeserializer();

	/**
	 * Constructor.
	 */
	public BasicStreamDatumArrayDeserializer() {
		super(StreamDatum.class);
	}

	@Override
	public StreamDatum deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
		JsonToken t = p.currentToken();
		if ( t == JsonToken.VALUE_NULL ) {
			return null;
		} else if ( p.isExpectedStartArrayToken() ) {
			long ts = p.nextLongValue(0);
			long idHi = p.nextLongValue(0);
			long idLo = p.nextLongValue(0);
			if ( ts < 1 || idHi == 0 || idLo == 0 ) {
				throw MismatchedInputException.from(p,
						"Unable to parse StreamDatum (timestamp or stream ID missing)");
			}
			BigDecimal[] i = JsonUtils.parseDecimalArray(p);
			BigDecimal[] a = JsonUtils.parseDecimalArray(p);
			String[] s = JsonUtils.parseStringArray(p);
			String[] tags = JsonUtils.parseStringArray(p);
			p.nextToken(); // advance to final end-array ']'
			return new BasicStreamDatum(new UUID(idHi, idLo), ofEpochMilli(ts),
					propertiesOf(i, a, s, tags));
		}
		throw MismatchedInputException.from(p, "Unable to parse StreamDatum (not an array)");
	}

}

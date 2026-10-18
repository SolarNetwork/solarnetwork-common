/* ==================================================================
 * GeneralDatumMetadataDeserializer.java - 19 Oct 2026 7:08:59 am
 *
 * Copyright 2026 SolarNetwork.net Dev Team
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
import java.io.Serial;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import net.solarnetwork.domain.datum.GeneralDatumMetadata;

/**
 * Deserializer for {@link GeneralDatumMetadata} instances.
 *
 * @author matt
 * @version 1.0
 * @since 4.55
 */
public class GeneralDatumMetadataDeserializer extends StdDeserializer<GeneralDatumMetadata> {

	@Serial
	private static final long serialVersionUID = -3327538214825536067L;

	/** A default instance. */
	public static final JsonDeserializer<GeneralDatumMetadata> INSTANCE = new GeneralDatumMetadataDeserializer();

	/**
	 * Constructor.
	 */
	public GeneralDatumMetadataDeserializer() {
		super(GeneralDatumMetadata.class);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public @Nullable GeneralDatumMetadata deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JacksonException {
		JsonToken t = p.currentToken();
		if ( t == JsonToken.VALUE_NULL ) {
			return null;
		} else if ( !p.isExpectedStartObjectToken() ) {
			throw new JsonParseException(p, "Unable to parse GeneralDatumMetadata (not an object)");
		}
		final GeneralDatumMetadata result = new GeneralDatumMetadata();
		for ( String field = p.nextFieldName(); field != null; field = p.nextFieldName() ) {
			p.nextToken();
			if ( "m".equals(field) && p.isExpectedStartObjectToken() ) {
				Map<String, Object> map = p.readValueAs(JsonUtils.STRING_MAP_TYPE);
				result.setM(map);
			} else if ( "pm".equals(field) && p.isExpectedStartObjectToken() ) {
				Map<String, Object> map = p.readValueAs(JsonUtils.STRING_MAP_TYPE);
				// remove non-map values
				for ( Iterator<Object> itr = map.values().iterator(); itr.hasNext(); ) {
					if ( !(itr.next() instanceof Map<?, ?>) ) {
						itr.remove();
					}
				}
				result.setPm((Map) map);
			} else if ( "t".equals(field) && p.isExpectedStartArrayToken() ) {
				Object[] array = p.readValueAs(Object[].class);
				var tags = new LinkedHashSet<String>(array.length);
				for ( Object o : array ) {
					if ( o instanceof String s ) {
						tags.add(s);
					}
				}
				if ( !tags.isEmpty() ) {
					result.setTags(tags);
				}
			} else {
				// ignore other properties, and values of the wrong type
				p.skipChildren();
			}
		}
		return result;
	}

}

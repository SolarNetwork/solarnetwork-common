/* ==================================================================
 * JodaDateTimeEpochDeserializer.java - 6/11/2019 7:15:02 am
 * 
 * Copyright 2019 SolarNetwork.net Dev Team
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
import org.joda.time.DateTime;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;

/**
 * Deserialize millisecond epoch numbers into {@link DateTime} instances.
 * 
 * @author matt
 * @version 1.1
 * @since 1.55
 * @deprecated since 1.1, use
 *             {@link net.solarnetwork.codec.JodaDateTimeEpochDeserializer}
 */
@Deprecated
public class JodaDateTimeEpochDeserializer extends StdScalarDeserializer<DateTime> {

	private static final long serialVersionUID = 7261772164817148373L;

	/**
	 * Constructor.
	 */
	public JodaDateTimeEpochDeserializer() {
		super(DateTime.class);
	}

	@Override
	public DateTime deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		JsonToken t = p.currentToken();
		long l = 0;
		if ( t == JsonToken.VALUE_NULL ) {
			return null;
		} else if ( t == JsonToken.VALUE_NUMBER_INT || t == JsonToken.VALUE_NUMBER_FLOAT ) {
			l = p.getLongValue();
		} else {
			String s = p.getValueAsString();
			if ( s == null ) {
				return null;
			}
			try {
				l = Long.parseLong(s);
			} catch ( NumberFormatException e ) {
				throw new JsonParseException(p,
						"Unable to parse millisecond epoch timestamp from [" + s + "]", e);
			}
		}
		return new DateTime(l);
	}

}

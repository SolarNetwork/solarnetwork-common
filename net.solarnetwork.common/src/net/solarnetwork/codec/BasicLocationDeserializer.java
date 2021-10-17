/* ==================================================================
 * BasicLocationDeserializer.java - 6/06/2021 9:56:41 AM
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
import java.math.BigDecimal;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import net.solarnetwork.domain.BasicLocation;
import net.solarnetwork.domain.Location;

/**
 * Deserializer for {@link Location} objects.
 * 
 * @author matt
 * @version 1.0
 * @since 1.72
 */
public class BasicLocationDeserializer extends StdScalarDeserializer<Location> implements Serializable {

	private static final long serialVersionUID = -5998708607249785150L;

	/** A default instance. */
	public static final BasicLocationDeserializer INSTANCE = new BasicLocationDeserializer();

	/**
	 * Constructor.
	 */
	public BasicLocationDeserializer() {
		super(Location.class);
	}

	@Override
	public Location deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		JsonToken t = p.currentToken();
		if ( t == JsonToken.VALUE_NULL ) {
			return null;
		} else if ( p.isExpectedStartObjectToken() ) {
			Object[] data = new Object[11];
			JsonUtils.parseIndexedFieldsObject(p, ctxt, data, BasicLocationField.FIELD_MAP);
			// @formatter:off
			return new BasicLocation(
					(String)data[0],
					(String)data[1],
					(String)data[2],
					(String)data[3],
					(String)data[5],
					(String)data[4],
					(String)data[6],
					(BigDecimal)data[7],
					(BigDecimal)data[8],
					(BigDecimal)data[9],
					(String)data[10]);
			// @formatter:on
		}
		throw new JsonParseException(p, "Unable to parse Location (not an object)");
	}

}

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

package net.solarnetwork.codec.jackson;

import java.math.BigDecimal;
import net.solarnetwork.domain.BasicIdentityLocation;
import net.solarnetwork.domain.BasicLocation;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.exc.MismatchedInputException;

/**
 * Deserializer for {@link BasicIdentityLocation} objects.
 *
 * @author matt
 * @version 1.0
 * @since 4.13
 */
public class BasicIdentityLocationDeserializer extends StdDeserializer<BasicIdentityLocation> {

	/** A default instance. */
	public static final ValueDeserializer<BasicIdentityLocation> INSTANCE = new BasicIdentityLocationDeserializer();

	/**
	 * Constructor.
	 */
	public BasicIdentityLocationDeserializer() {
		super(BasicIdentityLocation.class);
	}

	@Override
	public BasicIdentityLocation deserialize(JsonParser p, DeserializationContext ctxt)
			throws JacksonException {
		JsonToken t = p.currentToken();
		if ( t == JsonToken.VALUE_NULL ) {
			return null;
		} else if ( p.isExpectedStartObjectToken() ) {
			Object[] data = new Object[12];
			JsonUtils.parseIndexedFieldsObject(p, ctxt, data, BasicIdentityLocationField.FIELD_MAP);
			// @formatter:off
			return new BasicIdentityLocation((Long)data[0], new BasicLocation(
					(String)data[1],
					(String)data[2],
					(String)data[3],
					(String)data[4],
					(String)data[6],
					(String)data[5],
					(String)data[7],
					(BigDecimal)data[8],
					(BigDecimal)data[9],
					(BigDecimal)data[10],
					(String)data[11]));
			// @formatter:on
		}
		throw MismatchedInputException.from(p, "Unable to parse Location (not an object)");
	}

}

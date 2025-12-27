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

package net.solarnetwork.codec.jackson;

import java.util.UUID;
import net.solarnetwork.domain.Location;
import net.solarnetwork.domain.datum.BasicObjectDatumStreamMetadata;
import net.solarnetwork.domain.datum.ObjectDatumKind;
import net.solarnetwork.domain.datum.ObjectDatumStreamMetadata;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.exc.MismatchedInputException;

/**
 * Deserializer for {@link ObjectDatumStreamMetadata}.
 *
 * <p>
 * Note that the {@link ObjectDatumStreamMetadata#getMetaJson()} is <b>not</b>
 * serialized.
 * </p>
 *
 * @author matt
 * @version 1.0
 * @since 4.13
 */
public class BasicObjectDatumStreamMetadataDeserializer
		extends StdDeserializer<ObjectDatumStreamMetadata> {

	/** A default instance. */
	public static final ValueDeserializer<ObjectDatumStreamMetadata> INSTANCE = new BasicObjectDatumStreamMetadataDeserializer();

	/**
	 * Constructor.
	 */
	public BasicObjectDatumStreamMetadataDeserializer() {
		super(ObjectDatumStreamMetadata.class);
	}

	@Override
	public ObjectDatumStreamMetadata deserialize(JsonParser p, DeserializationContext ctxt)
			throws JacksonException {
		JsonToken t = p.currentToken();
		if ( t == JsonToken.VALUE_NULL ) {
			return null;
		} else if ( p.isExpectedStartObjectToken() ) {
			Object[] data = new Object[9];
			JsonUtils.parseIndexedFieldsObject(p, ctxt, data,
					BasicObjectDatumStreamMetadataField.FIELD_MAP);
			// @formatter:off
			return new BasicObjectDatumStreamMetadata(
					(UUID) data[0],
					(String) data[1],
					(ObjectDatumKind) data[2],
					(Long) data[3],
					(String) data[4],
					(Location) data[5],
					(String[]) data[6],
					(String[]) data[7],
					(String[]) data[8],
					(String) null);
			// @formattter:on
		}
		throw MismatchedInputException.from(p, "Unable to parse ObjectDatumStreamMetadata (not an object)");
	}

}

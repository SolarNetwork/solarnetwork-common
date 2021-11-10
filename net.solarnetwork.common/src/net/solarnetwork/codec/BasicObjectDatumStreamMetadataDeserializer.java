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

package net.solarnetwork.codec;

import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import net.solarnetwork.domain.Location;
import net.solarnetwork.domain.datum.BasicObjectDatumStreamMetadata;
import net.solarnetwork.domain.datum.ObjectDatumKind;
import net.solarnetwork.domain.datum.ObjectDatumStreamMetadata;

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
 * @since 1.72
 */
public class BasicObjectDatumStreamMetadataDeserializer
		extends StdScalarDeserializer<ObjectDatumStreamMetadata> implements Serializable {

	private static final long serialVersionUID = -1844182290390256234L;

	/** A default instance. */
	public static final BasicObjectDatumStreamMetadataDeserializer INSTANCE = new BasicObjectDatumStreamMetadataDeserializer();

	/**
	 * Constructor.
	 */
	public BasicObjectDatumStreamMetadataDeserializer() {
		super(ObjectDatumStreamMetadata.class);
	}

	@Override
	public ObjectDatumStreamMetadata deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
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
		throw new JsonParseException(p, "Unable to parse ObjectDatumStreamMetadata (not an object)");
	}

}

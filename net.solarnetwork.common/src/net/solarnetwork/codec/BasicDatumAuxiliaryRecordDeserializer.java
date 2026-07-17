/* ==================================================================
 * BasicDatumAuxiliaryRecordDeserializer.java - 18/07/2026 8:09:03 am
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

import static net.solarnetwork.codec.JsonUtils.readObject;
import java.io.IOException;
import java.io.Serial;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import org.jspecify.annotations.Nullable;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import net.solarnetwork.domain.datum.BasicDatumAuxiliaryRecord;
import net.solarnetwork.domain.datum.DatumAuxiliaryRecord;
import net.solarnetwork.domain.datum.DatumAuxiliaryType;
import net.solarnetwork.domain.datum.DatumId.DatumIdent;
import net.solarnetwork.domain.datum.DatumIdentity;
import net.solarnetwork.domain.datum.DatumSamples;
import net.solarnetwork.domain.datum.GeneralDatumMetadata;
import net.solarnetwork.domain.datum.ObjectDatumKind;

/**
 * Deserializer for {@link DatumAuxiliaryRecord} objects
 *
 * @author matt
 * @version 1.0
 * @since 4.44
 */
public class BasicDatumAuxiliaryRecordDeserializer extends StdDeserializer<DatumAuxiliaryRecord> {

	@Serial
	private static final long serialVersionUID = 1681756578931200838L;

	/** A default instance. */
	public static final JsonDeserializer<DatumAuxiliaryRecord> INSTANCE = new BasicDatumAuxiliaryRecordDeserializer();

	/**
	 * Constructor.
	 */
	public BasicDatumAuxiliaryRecordDeserializer() {
		super(DatumAuxiliaryRecord.class);
	}

	@Override
	public @Nullable DatumAuxiliaryRecord deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JacksonException {
		JsonToken t = p.currentToken();
		if ( t == JsonToken.VALUE_NULL ) {
			return null;
		} else if ( !p.isExpectedStartObjectToken() ) {
			throw new JsonParseException(p, "Unable to parse DatumAuxiliaryRecord (not an object)");
		}

		final TreeNode tree = p.readValueAsTree();
		if ( !(tree instanceof JsonNode json) ) {
			throw new IllegalStateException(
					"JsonParser " + p + " produced " + tree.getClass() + " that is not a JsonNode");
		}

		final DatumAuxiliaryType type;
		try {
			type = DatumAuxiliaryType.valueOf(json.path("type").textValue());
		} catch ( NullPointerException | IllegalArgumentException e ) {
			throw MismatchedInputException.from(p, DatumAuxiliaryType.class,
					"Invalid type property value: %s".formatted(json.path("type")));
		}

		final Instant ts;
		if ( json.has("timestamp") ) {
			try {
				ts = JsonUtils.parseTimestamp(json.path("timestamp"));
			} catch ( DateTimeParseException e ) {
				throw InvalidFormatException.from(p, "Invalid 'timestamp' value.", e);
			}
		} else {
			throw MismatchedInputException.from(p, DatumAuxiliaryRecord.class,
					"Missing 'timestamp' property.");
		}

		final DatumIdentity datumIdentity;
		if ( json.has("objectId") && json.has("kind") && json.has("sourceId") ) {
			final ObjectDatumKind kind;
			try {
				kind = ObjectDatumKind.fromValue(json.path("kind").textValue());
			} catch ( IllegalArgumentException e ) {
				throw InvalidFormatException.from(p, "Invalid 'kind' value.", e);
			}
			datumIdentity = new DatumIdent(kind, json.path("objectId").asLong(),
					json.path("sourceId").textValue(), ts);
		} else if ( json.has("nodeId") && json.has("sourceId") ) {
			datumIdentity = new DatumIdent(ObjectDatumKind.Node, json.path("nodeId").asLong(),
					json.path("sourceId").textValue(), ts);
		} else if ( json.has("locationId") && json.has("sourceId") ) {
			datumIdentity = new DatumIdent(ObjectDatumKind.Location, json.path("locationId").asLong(),
					json.path("sourceId").textValue(), ts);
		} else {
			throw MismatchedInputException.from(p, DatumAuxiliaryRecord.class,
					"Missing datum identity kind/objectId/sourceId or nodeId/sourceId or locatinoId/sourceId.");
		}

		final String notes = json.path("notes").textValue();
		final GeneralDatumMetadata meta = readObject(p, json.path("metadata"),
				GeneralDatumMetadata.class);
		final DatumSamples samplesFinal = readObject(p, json.path("samplesFinal"), DatumSamples.class);
		final DatumSamples samplesStart = readObject(p, json.path("samplesStart"), DatumSamples.class);

		return new BasicDatumAuxiliaryRecord(type, datumIdentity, notes, samplesFinal, samplesStart,
				meta);
	}

}

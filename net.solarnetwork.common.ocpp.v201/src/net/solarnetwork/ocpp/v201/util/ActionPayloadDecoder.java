/* ==================================================================
 * ActionPayloadDecoder.java - 12/02/2024 5:45:00 pm
 * 
 * Copyright 2024 SolarNetwork.net Dev Team
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

package net.solarnetwork.ocpp.v201.util;

import static net.solarnetwork.ocpp.v201.util.OcppUtils.parseOcppMessage;
import java.io.IOException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.JsonSchemaFactory;
import net.solarnetwork.ocpp.domain.Action;
import net.solarnetwork.ocpp.domain.SchemaValidationException;

/**
 * Implementation of {@link net.solarnetwork.ocpp.json.ActionPayloadDecoder}
 * with schema validation support.
 * 
 * @author matt
 * @version 1.0
 */
public class ActionPayloadDecoder implements net.solarnetwork.ocpp.json.ActionPayloadDecoder {

	private final JsonSchemaFactory validator;

	/**
	 * Constructor.
	 */
	public ActionPayloadDecoder() {
		this(null);
	}

	/**
	 * Constructor.
	 * 
	 * @param validator
	 *        the optional validator to use
	 */
	public ActionPayloadDecoder(JsonSchemaFactory validator) {
		super();
		this.validator = validator;
	}

	@Override
	public <T> T decodeActionPayload(Action action, boolean forResult, JsonNode payload)
			throws IOException {
		if ( payload.isNull() ) {
			return null;
		}
		if ( !(payload instanceof ObjectNode) ) {
			throw new SchemaValidationException(payload, "Message is not a JSON object.");
		}
		@SuppressWarnings("unchecked")
		T result = (T) parseOcppMessage(action.getName(), !forResult, (ObjectNode) payload, validator);
		return result;
	}

}

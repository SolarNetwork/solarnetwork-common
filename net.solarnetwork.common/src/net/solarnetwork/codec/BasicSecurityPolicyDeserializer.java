/* ==================================================================
 * BasicSecurityPolicyDeserializer.java - 27/09/2025 11:09:46 am
 *
 * Copyright 2025 SolarNetwork.net Dev Team
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
import java.time.Instant;
import java.util.Set;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import net.solarnetwork.domain.BasicSecurityPolicy;
import net.solarnetwork.domain.LocationPrecision;
import net.solarnetwork.domain.SecurityPolicy;
import net.solarnetwork.domain.datum.Aggregation;

/**
 * Deserializer for {@link SecurityPolicy}.
 *
 * @author matt
 * @version 1.0
 */
public class BasicSecurityPolicyDeserializer extends StdDeserializer<SecurityPolicy> {

	@Serial
	private static final long serialVersionUID = 6609692122522382793L;

	/** A default instance. */
	public static final BasicSecurityPolicyDeserializer INSTANCE = new BasicSecurityPolicyDeserializer();

	/**
	 * Constructor.
	 */
	public BasicSecurityPolicyDeserializer() {
		super(SecurityPolicy.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public SecurityPolicy deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JacksonException {
		JsonToken t = p.currentToken();
		if ( t == JsonToken.VALUE_NULL ) {
			return null;
		} else if ( p.isExpectedStartObjectToken() ) {
			Object[] data = new Object[11];
			JsonUtils.parseIndexedFieldsObject(p, ctxt, data, BasicSecurityPolicyField.FIELD_MAP);
			// @formatter:off
			return BasicSecurityPolicy.builder()
					.withNodeIds((Set<Long>)data[BasicSecurityPolicyField.NodeIds.getIndex()])
					.withSourceIds((Set<String>)data[BasicSecurityPolicyField.SourceIds.getIndex()])
					.withMinAggregation((Aggregation)data[BasicSecurityPolicyField.MinAggregation.getIndex()])
					.withAggregations((Set<Aggregation>)data[BasicSecurityPolicyField.Aggregations.getIndex()])
					.withMinLocationPrecision((LocationPrecision)data[BasicSecurityPolicyField.MinLocationPrecision.getIndex()])
					.withLocationPrecisions((Set<LocationPrecision>)data[BasicSecurityPolicyField.LocationPrecisions.getIndex()])
					.withNodeMetadataPaths((Set<String>)data[BasicSecurityPolicyField.NodeMetadataPaths.getIndex()])
					.withUserMetadataPaths((Set<String>)data[BasicSecurityPolicyField.UserMetadataPaths.getIndex()])
					.withApiPaths((Set<String>)data[BasicSecurityPolicyField.ApiPaths.getIndex()])
					.withNotAfter((Instant)data[BasicSecurityPolicyField.NotAfter.getIndex()])
					.withRefreshAllowed((Boolean)data[BasicSecurityPolicyField.RefreshAllowed.getIndex()])
					.build();
			// @formatter:on
		}
		throw new JsonParseException(p, "Unable to parse SecurityPolicy (not an object)");
	}

}

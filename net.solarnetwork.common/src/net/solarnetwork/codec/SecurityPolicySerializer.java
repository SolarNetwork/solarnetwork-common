/* ==================================================================
 * SecurityPolicySerializer.java - 9/10/2016 12:45:17 PM
 *
 * Copyright 2007-2016 SolarNetwork.net Dev Team
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
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import net.solarnetwork.domain.SecurityPolicy;

/**
 * JSON serializer for {@link SecurityPolicy}.
 *
 * @author matt
 * @version 1.0
 */
public class SecurityPolicySerializer extends StdSerializer<SecurityPolicy> {

	/** A default instance. */
	public static final SecurityPolicySerializer INSTANCE = new SecurityPolicySerializer();

	@Serial
	private static final long serialVersionUID = -5542903806671694581L;

	/**
	 * Constructor.
	 */
	public SecurityPolicySerializer() {
		super(SecurityPolicy.class);
	}

	@Override
	public void serialize(SecurityPolicy policy, JsonGenerator generator, SerializerProvider provider)
			throws IOException {
		if ( policy == null ) {
			generator.writeNull();
			return;
		}
		generator.writeStartObject();

		BasicSecurityPolicyField.NodeIds.writeValue(generator, provider, policy.getNodeIds());
		BasicSecurityPolicyField.SourceIds.writeValue(generator, provider, policy.getSourceIds());
		if ( policy.getMinAggregation() != null ) {
			BasicSecurityPolicyField.MinAggregation.writeValue(generator, provider,
					policy.getMinAggregation());
		} else {
			BasicSecurityPolicyField.Aggregations.writeValue(generator, provider,
					policy.getAggregations());
		}
		if ( policy.getMinLocationPrecision() != null ) {
			BasicSecurityPolicyField.MinLocationPrecision.writeValue(generator, provider,
					policy.getMinLocationPrecision());
		} else {
			BasicSecurityPolicyField.LocationPrecisions.writeValue(generator, provider,
					policy.getLocationPrecisions());
		}
		BasicSecurityPolicyField.NodeMetadataPaths.writeValue(generator, provider,
				policy.getNodeMetadataPaths());
		BasicSecurityPolicyField.UserMetadataPaths.writeValue(generator, provider,
				policy.getUserMetadataPaths());
		BasicSecurityPolicyField.ApiPaths.writeValue(generator, provider, policy.getApiPaths());
		BasicSecurityPolicyField.NotAfter.writeValue(generator, provider, policy.getNotAfter());
		BasicSecurityPolicyField.RefreshAllowed.writeValue(generator, provider,
				policy.getRefreshAllowed());

		generator.writeEndObject();
	}

}

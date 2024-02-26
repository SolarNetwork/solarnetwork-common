/* ==================================================================
 * BasicNetworkIdentityTests.java - 26/02/2024 3:38:35 pm
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

package net.solarnetwork.domain.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.solarnetwork.domain.BasicNetworkIdentity;

/**
 * Test cases for the {@link BasicNetworkIdentity} class.
 *
 * @author matt
 * @version 1.0
 */
public class BasicNetworkIdentityTests {

	private ObjectMapper objectMapper;

	@Before
	public void setup() {
		objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(Include.NON_NULL);
		objectMapper.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);
		objectMapper.registerModule(new JavaTimeModule());
	}

	@Test
	public void serializeJson() throws Exception {
		// GIVEN
		final String identityKey = UUID.randomUUID().toString();
		final String termsOfService = UUID.randomUUID().toString();
		final String host = UUID.randomUUID().toString();
		final Integer port = 443;
		final Boolean forceTLS = Boolean.TRUE;

		final BasicNetworkIdentity ident = new BasicNetworkIdentity(identityKey, termsOfService, host,
				port, forceTLS);
		ident.setSolarQueryServiceURL(UUID.randomUUID().toString());
		ident.setSolarInMqttServiceURL(UUID.randomUUID().toString());
		ident.setSolarUserServiceURL(UUID.randomUUID().toString());

		// WHEN
		String json = objectMapper.writeValueAsString(ident);

		// THEN
		// prop order: "host", "port", "forceTLS", "networkServiceURLs", "identityKey", "termsOfService"
		assertThat(json, is(equalTo(String.format(
				"{\"host\":\"%s\",\"port\":%d,\"forceTLS\":%s,\"networkServiceURLs\":{\"solarquery\":\"%s\","
						+ "\"solarin-mqtt\":\"%s\",\"solaruser\":\"%s\"},\"identityKey\":\"%s\",\"termsOfService\":\"%s\"}",
				host, port, forceTLS, ident.getSolarQueryServiceURL(), ident.getSolarInMqttServiceURL(),
				ident.getSolarUserServiceURL(), identityKey, termsOfService))));
	}

}

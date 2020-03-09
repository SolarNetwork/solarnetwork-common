/* ==================================================================
 * ChargePointConnectorTests.java - 4/03/2020 7:11:59 am
 * 
 * Copyright 2020 SolarNetwork.net Dev Team
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

package net.solarnetwork.ocpp.domain.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import java.time.Instant;
import org.junit.Test;
import net.solarnetwork.ocpp.domain.ChargePointConnector;
import net.solarnetwork.ocpp.domain.ChargePointConnectorKey;
import net.solarnetwork.ocpp.domain.ChargePointStatus;
import net.solarnetwork.ocpp.domain.StatusNotification;

/**
 * Test cases for the {@link ChargePointConnector} class.
 * 
 * @author matt
 * @version 1.0
 */
public class ChargePointConnectorTests {

	@Test
	public void foo() {
		// GIVEN
		ChargePointConnector conn = new ChargePointConnector(new ChargePointConnectorKey(1L, 1));

		// WHEN
		StatusNotification info = StatusNotification.builder().withStatus(ChargePointStatus.Available)
				.withTimestamp(Instant.now()).build();
		conn.setInfo(info);

		// THEN
		assertThat("Info connector ID set", conn.getInfo().getConnectorId(),
				equalTo(conn.getId().getConnectorId()));
		assertThat("Info properties copied", conn.getInfo(),
				equalTo(info.toBuilder().withConnectorId(conn.getId().getConnectorId()).build()));
	}

}

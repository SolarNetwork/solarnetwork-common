/* ==================================================================
 * MqttQosTests.java - 27/11/2019 12:14:07 pm
 * 
 * Copyright 2019 SolarNetwork.net Dev Team
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

package net.solarnetwork.common.mqtt.test;

import static org.hamcrest.MatcherAssert.assertThat;
import org.hamcrest.Matchers;
import org.junit.Test;
import net.solarnetwork.common.mqtt.MqttQos;

/**
 * Test cases for the {@link MqttQos} class.
 * 
 * @author matt
 * @version 1.0
 */
public class MqttQosTests {

	@Test
	public void valueOf_int() {
		MqttQos qos = MqttQos.valueOf(0);
		assertThat("QOS", qos, Matchers.equalTo(MqttQos.AtMostOnce));

		qos = MqttQos.valueOf(1);
		assertThat("QOS", qos, Matchers.equalTo(MqttQos.AtLeastOnce));

		qos = MqttQos.valueOf(2);
		assertThat("QOS", qos, Matchers.equalTo(MqttQos.ExactlyOnce));
	}

}

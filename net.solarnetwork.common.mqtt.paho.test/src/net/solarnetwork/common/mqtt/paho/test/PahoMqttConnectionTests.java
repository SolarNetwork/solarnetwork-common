/* ==================================================================
 * PahoMqttConnectionTests.java - 27/11/2019 7:13:11 am
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

package net.solarnetwork.common.mqtt.paho.test;

import java.util.concurrent.Executors;
import org.junit.Before;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import net.solarnetwork.common.mqtt.MqttStats;
import net.solarnetwork.common.mqtt.integration.test.MqttConnectionIntegrationTests;
import net.solarnetwork.common.mqtt.paho.PahoMqttConnection;

/**
 * Test cases for the {@link PahoMqttConnection} class.
 * 
 * @author matt
 * @version 1.0
 */
public class PahoMqttConnectionTests extends MqttConnectionIntegrationTests {

	@Override
	@Before
	public void setup() throws Exception {
		super.setup();
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setThreadNamePrefix("PahoMqtt-Scheduler-Test-");
		scheduler.initialize();
		config.setUid("Paho-Test");
		config.setStats(new MqttStats(5));
		PahoMqttConnection conn = new PahoMqttConnection(
				Executors.newCachedThreadPool(new CustomizableThreadFactory("PahoMqtt-Test-")),
				scheduler, config);
		setService(conn);
	}

}

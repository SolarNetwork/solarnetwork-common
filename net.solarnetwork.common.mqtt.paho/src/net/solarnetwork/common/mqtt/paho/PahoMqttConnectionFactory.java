/* ==================================================================
 * PahoMqttConnectionFactory.java - 27/11/2019 7:15:05 am
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

package net.solarnetwork.common.mqtt.paho;

import java.util.concurrent.Executor;
import org.springframework.scheduling.TaskScheduler;
import net.solarnetwork.common.mqtt.MqttConnection;
import net.solarnetwork.common.mqtt.MqttConnectionConfig;
import net.solarnetwork.common.mqtt.MqttConnectionFactory;

/**
 * Factory for Paho {@link MqttConnection} intances.
 * 
 * @author matt
 * @version 1.0
 */
public class PahoMqttConnectionFactory implements MqttConnectionFactory {

	private final Executor executor;
	private final TaskScheduler scheduler;

	/**
	 * Constructor.
	 * 
	 * @param executor
	 *        the executor to use
	 * @param scheduler
	 *        the scheduler to use
	 */
	public PahoMqttConnectionFactory(Executor executor, TaskScheduler scheduler) {
		super();
		this.executor = executor;
		this.scheduler = scheduler;
	}

	@Override
	public MqttConnection createConnection(MqttConnectionConfig config) {
		PahoMqttConnection conn = new PahoMqttConnection(executor, scheduler, config);
		return conn;
	}

}

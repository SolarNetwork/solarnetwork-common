/* ==================================================================
 * NettyMqttConnectionFactory.java - 26/11/2019 4:05:45 pm
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

package net.solarnetwork.common.mqtt.netty;

import java.util.concurrent.Executor;
import org.springframework.scheduling.TaskScheduler;
import net.solarnetwork.common.mqtt.MqttConnection;
import net.solarnetwork.common.mqtt.MqttConnectionConfig;
import net.solarnetwork.common.mqtt.MqttConnectionFactory;
import net.solarnetwork.common.mqtt.WireLoggingSupport;

/**
 * Netty implementation of {@link MqttConnectionFactory}.
 * 
 * @author matt
 * @version 1.1
 */
public class NettyMqttConnectionFactory implements MqttConnectionFactory, WireLoggingSupport {

	private final Executor executor;
	private final TaskScheduler scheduler;
	private int ioThreadCount = NettyMqttConnection.DEFAULT_IO_THREAD_COUNT;
	private boolean wireLogging = NettyMqttConnection.DEFAULT_WIRE_LOGGING;

	/**
	 * Constructor.
	 * 
	 * @param executor
	 *        the executor to use
	 * @param scheduler
	 *        the scheduler to use
	 */
	public NettyMqttConnectionFactory(Executor executor, TaskScheduler scheduler) {
		super();
		this.executor = executor;
		this.scheduler = scheduler;
	}

	@Override
	public MqttConnection createConnection(MqttConnectionConfig config) {
		NettyMqttConnection conn = new NettyMqttConnection(executor, scheduler, config);
		conn.setIoThreadCount(ioThreadCount);
		conn.setWireLoggingEnabled(wireLogging);
		return conn;
	}

	/**
	 * Get the IO thread count to use.
	 * 
	 * @return the thread count
	 */
	public int getIoThreadCount() {
		return ioThreadCount;
	}

	/**
	 * Set the IO thread count to use.
	 * 
	 * @param ioThreadCount
	 *        the thread count
	 */
	public void setIoThreadCount(int ioThreadCount) {
		this.ioThreadCount = ioThreadCount;
	}

	/**
	 * Get the wire-level logging flag.
	 * 
	 * @return {@literal true} to enable wire-level logging support; defaults to
	 *         {@link NettyMqttConnection#DEFAULT_WIRE_LOGGING}
	 */
	@Override
	public boolean isWireLoggingEnabled() {
		return wireLogging;
	}

	/**
	 * Set the wire-level logging flag.
	 * 
	 * @param wireLogging
	 *        {@literal true} to enable wire-level logging support
	 */
	@Override
	public void setWireLoggingEnabled(boolean wireLogging) {
		this.wireLogging = wireLogging;
	}

}

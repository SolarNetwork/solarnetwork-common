/* ==================================================================
 * BaseMqttConnection.java - 27/11/2019 1:45:12 pm
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

package net.solarnetwork.common.mqtt;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import net.solarnetwork.service.PingTest;
import net.solarnetwork.service.PingTestResult;
import net.solarnetwork.service.support.BasicIdentifiable;
import net.solarnetwork.settings.SettingsChangeObserver;

/**
 * Base implementation of {@link MqttConnection}.
 * 
 * <p>
 * This base class is designed to try to connect to the configured MQTT server
 * and keep trying until it is able to do so.
 * </p>
 * 
 * @author matt
 * @version 1.1
 */
public abstract class BaseMqttConnection extends BasicIdentifiable
		implements MqttConnection, ReconfigurableMqttConnection, SettingsChangeObserver, PingTest {

	/** A class-level logger. */
	protected final Logger log = LoggerFactory.getLogger(getClass());

	protected final Executor executor;
	protected final TaskScheduler scheduler;
	protected final BasicMqttConnectionConfig connectionConfig;

	protected volatile MqttMessageHandler messageHandler;
	protected volatile MqttConnectionObserver connectionObserver;

	private boolean closed;

	private CompletableFuture<MqttConnectReturnCode> connectFuture;
	private CompletableFuture<Void> reconfigureFuture;

	/**
	 * Constructor.
	 * 
	 * @param executor
	 *        the executor
	 * @param scheduler
	 *        the task scheduler
	 */
	public BaseMqttConnection(Executor executor, TaskScheduler scheduler) {
		this(executor, scheduler, new BasicMqttConnectionConfig());
	}

	/**
	 * Constructor.
	 * 
	 * @param executor
	 *        the executor
	 * @param scheduler
	 *        the task scheduler
	 * @param connectionConfig
	 *        initial connection configuration, or {@literal null} to use a
	 *        default
	 */
	public BaseMqttConnection(Executor executor, TaskScheduler scheduler,
			MqttConnectionConfig connectionConfig) {
		super();
		this.executor = executor;
		this.scheduler = scheduler;
		this.closed = false;
		this.connectionConfig = connectionConfig instanceof BasicMqttConnectionConfig
				? (BasicMqttConnectionConfig) connectionConfig
				: new BasicMqttConnectionConfig(connectionConfig);
	}

	/**
	 * Initialize after all properties have been configured.
	 */
	public synchronized void init() {
		try {
			open();
		} catch ( IOException e ) {
			// ignore;
		}
	}

	@Override
	public synchronized void configurationChanged(Map<String, Object> properties) {
		reconfigure();
	}

	@Override
	public final synchronized Future<?> reconfigure() {
		if ( reconfigureFuture != null ) {
			return reconfigureFuture;
		}
		if ( connectFuture != null ) {
			if ( !connectFuture.isDone() ) {
				try {
					log.info(
							"Cancelling scheduled connection to {} MQTT server from configuration change",
							getUid());
					connectFuture.cancel(true);
				} catch ( Exception e ) {
					// ignore
				}
			}
			connectFuture = null;
		}
		final CompletableFuture<Void> f = new CompletableFuture<>();
		reconfigureFuture = f;
		executor.execute(new Runnable() {

			@Override
			public void run() {
				Throwable t = null;
				try {
					try {
						closeConnection().get(connectionConfig.getConnectTimeoutSeconds(),
								TimeUnit.SECONDS);
					} catch ( Exception e ) {
						// ignore
					}
					log.info(
							"Scheduling re-connection to {} MQTT server from configuration change in {}s",
							getUid(), connectionConfig.getReconnectDelaySeconds());
					final long reconnectDelay = Math.max(200L,
							connectionConfig.getReconnectDelaySeconds() * 1000L);
					try {
						Thread.sleep(reconnectDelay);
					} catch ( InterruptedException e2 ) {
						// ignore
					}
					try {
						open().get(connectionConfig.getConnectTimeoutSeconds(), TimeUnit.SECONDS);
					} catch ( Exception e ) {
						t = e;
					}
				} finally {
					complete(t);
				}
			}

			private void complete(Throwable t) {
				synchronized ( BaseMqttConnection.this ) {
					reconfigureFuture = null;
				}
				if ( t != null ) {
					f.completeExceptionally(t);

				} else {
					f.complete(null);
				}
			}

		});
		return f;
	}

	@Override
	public final synchronized Future<MqttConnectReturnCode> open() throws IOException {
		if ( connectFuture != null ) {
			return connectFuture;
		}
		if ( isEstablished() ) {
			return CompletableFuture.completedFuture(null);
		}
		closed = false;
		final long connectDelay = Math.max(200L,
				(connectionConfig.getReconnectDelaySeconds() * 1000L) / 4);
		final Date connectDate = new Date(System.currentTimeMillis() + connectDelay);
		final CompletableFuture<MqttConnectReturnCode> f = new CompletableFuture<>();
		this.connectFuture = f;
		log.info("Scheduling connection to {} MQTT server in {}ms", getUid(), connectDelay);
		scheduler.schedule(createConnectScheduledTask(f), connectDate);
		return f;
	}

	/**
	 * Create a scheduled task to connect to the MQTT server.
	 * 
	 * @param future
	 *        the future to complete the connection with
	 * @return the task
	 */
	protected abstract Runnable createConnectScheduledTask(
			CompletableFuture<MqttConnectReturnCode> future);

	/**
	 * Get the future for a completed connection.
	 * 
	 * @return the future, or {@literal null}
	 */
	protected CompletableFuture<MqttConnectReturnCode> connectFuture() {
		return connectFuture;
	}

	/**
	 * Get the future for the reconfigure task.
	 * 
	 * @return the future, or {@literal null}
	 */
	protected CompletableFuture<Void> reconfigureFuture() {
		return reconfigureFuture;
	}

	@Override
	public final void close() throws IOException {
		synchronized ( this ) {
			closed = true;
			connectFuture = null;
		}
		final URI serverUri = connectionConfig.getServerUri();
		try {
			closeConnection().get(connectionConfig.getConnectTimeoutSeconds(), TimeUnit.SECONDS);
		} catch ( ExecutionException e ) {
			log.warn("Error closing connection to MQTT server {}", serverUri);
			throw new IOException("Error closing connection to MQTT server " + serverUri, e);
		} catch ( TimeoutException | InterruptedException e ) {
			log.warn("Timeout closing connection to MQTT server {}", serverUri);
		}
	}

	/**
	 * Close any open connection.
	 * 
	 * @return a future that completes when the connection has been closed
	 */
	protected abstract Future<?> closeConnection();

	@Override
	public final synchronized boolean isClosed() {
		return closed;
	}

	@Override
	public final void setMessageHandler(MqttMessageHandler handler) {
		this.messageHandler = handler;
	}

	@Override
	public final void setConnectionObserver(MqttConnectionObserver observer) {
		this.connectionObserver = observer;
	}

	/*---------------------
	 * Ping test support
	 *------------------ */

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("{");
		if ( getUid() != null ) {
			builder.append(getUid());
			builder.append(',');
		}
		URI uri = getConnectionConfig().getServerUri();
		if ( uri != null ) {
			builder.append(uri);
		} else {
			builder.append("n/a");
		}
		builder.append("}");
		return builder.toString();
	}

	@Override
	public String getPingTestId() {
		return getClass().getName() + "-" + getUid();
	}

	@Override
	public String getPingTestName() {
		return "MQTT Service";
	}

	@Override
	public long getPingTestMaximumExecutionMilliseconds() {
		return 10000;
	}

	@Override
	public PingTest.Result performPingTest() throws Exception {
		boolean healthy = isEstablished();
		URI serverUri = connectionConfig.getServerUri();
		String msg = (healthy ? "Connected to " + serverUri : "Not connected");
		Map<String, Object> props = Collections.singletonMap("serverUri", serverUri);
		PingTestResult result = new PingTestResult(healthy, msg, props);
		return result;
	}

	/*---------------------
	 * Accessors
	 *------------------ */

	@Override
	public String getUid() {
		return connectionConfig.getUid();
	}

	@Override
	public void setUid(String uid) {
		connectionConfig.setUid(uid);
	}

	/**
	 * Get the connection configuration.
	 * 
	 * @return the configuration, never {@literal null}
	 */
	public final BasicMqttConnectionConfig getConnectionConfig() {
		return connectionConfig;
	}

}

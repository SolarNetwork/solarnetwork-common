/* ==================================================================
 * AbstractHttpClientTests.java - 19/05/2017 4:10:22 PM
 *
 * Copyright 2017 SolarNetwork.net Dev Team
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

package net.solarnetwork.test.internal;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Support for HTTP server based tests.
 *
 * @author matt
 * @version 2.0
 */
public abstract class AbstractHttpClientTests {

	private Server httpServer;
	private int httpServerPort;
	private String httpServerBaseUrl;
	private Handler.Sequence httpServerHandlers;

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Before
	public void setup() throws Exception {
		httpServer = new Server(0);
		httpServerHandlers = new Handler.Sequence();
		httpServer.setHandler(httpServerHandlers);
		httpServer.start();

		ServerConnector c = (ServerConnector) httpServer.getConnectors()[0];
		httpServerPort = c.getLocalPort();

		httpServerBaseUrl = "http://localhost:" + c.getLocalPort();
	}

	/**
	 * Register a handler.
	 *
	 * @param handler
	 *        the handler to register
	 * @since 2.0
	 */
	protected void addHandler(Handler handler) {
		httpServerHandlers.addHandler(handler);
	}

	/**
	 * Unregister a handler.
	 *
	 * @param handler
	 *        the handler to unregister
	 * @since 2.0
	 */
	protected void removeHandler(Handler handler) {
		httpServerHandlers.removeHandler(handler);
	}

	@After
	public void teardown() throws Exception {
		httpServer.stop();
	}

	/**
	 * Get the port the HTTP server is listening on.
	 *
	 * @return the HTTP server port
	 */
	protected int getHttpServerPort() {
		return httpServerPort;
	}

	/**
	 * Get a "base URL" for connecting to the HTTP server.
	 *
	 * @return the base URL (without any path) to the HTTP server
	 */
	protected String getHttpServerBaseUrl() {
		return httpServerBaseUrl;
	}

}

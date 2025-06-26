/* ==================================================================
 * TestHttpServer.java - 11/06/2025 5:54:45 am
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

package net.solarnetwork.test.http;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

/**
 * HTTP server wrapper for testing.
 *
 * @author matt
 * @version 1.0
 * @since 1.22
 */
public class TestHttpServer {

	private Server httpServer;
	private int httpServerPort;
	private String httpServerBaseUrl;
	private Handler.Sequence httpServerHandlers;

	/**
	 * Constructor.
	 *
	 * <p>
	 * A dynamic IP port will be used.
	 * </p>
	 */
	public TestHttpServer() {
		super();
	}

	/**
	 * Constructor.
	 *
	 * @param port
	 *        the IP port to use
	 */
	public TestHttpServer(int port) {
		super();
		httpServerPort = port;
	}

	/**
	 * Start up the HTTP server.
	 *
	 * @throws Exception
	 *         if an error occurs
	 */
	public synchronized void start() throws Exception {
		if ( httpServer == null ) {
			httpServer = new Server(httpServerPort);
			httpServerHandlers = new Handler.Sequence();
			httpServer.setHandler(httpServerHandlers);
			httpServer.start();

			ServerConnector c = (ServerConnector) httpServer.getConnectors()[0];
			if ( c.getLocalPort() != httpServerPort ) {
				httpServerPort = c.getLocalPort();
			}

			httpServerBaseUrl = "http://localhost:" + c.getLocalPort();
		}
		if ( httpServer.isStopped() ) {
			httpServer.start();
		}
	}

	/**
	 * Shut down the HTTP server.
	 *
	 * @throws Exception
	 *         if an error occurs
	 */
	public synchronized void stop() throws Exception {
		httpServer.stop();
	}

	/**
	 * Register a handler.
	 *
	 * @param handler
	 *        the handler to register
	 */
	public void addHandler(Handler handler) {
		httpServerHandlers.addHandler(handler);
	}

	/**
	 * Unregister a handler.
	 *
	 * @param handler
	 *        the handler to unregister
	 */
	public void removeHandler(Handler handler) {
		httpServerHandlers.removeHandler(handler);
	}

	/**
	 * Get the port the HTTP server is listening on.
	 *
	 * @return the HTTP server port
	 */
	public int getHttpServerPort() {
		return httpServerPort;
	}

	/**
	 * Get a "base URL" for connecting to the HTTP server.
	 *
	 * @return the base URL (without any path) to the HTTP server
	 */
	public String getHttpServerBaseUrl() {
		return httpServerBaseUrl;
	}

}

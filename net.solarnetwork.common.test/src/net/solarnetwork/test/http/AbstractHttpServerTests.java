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

package net.solarnetwork.test.http;

import org.eclipse.jetty.server.Handler;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Support for HTTP server based tests.
 *
 * @author matt
 * @version 1.0
 * @since 1.22
 */
public abstract class AbstractHttpServerTests {

	/** A class-level logger. */
	protected final Logger log = LoggerFactory.getLogger(getClass());

	/** A fixed HTTP server port to use. */
	protected int httpServerPort;

	private TestHttpServer httpServer;

	@Before
	public void setup() {
		if ( httpServer == null ) {
			httpServer = new TestHttpServer(httpServerPort);
			try {
				httpServer.start();
			} catch ( RuntimeException e ) {
				throw e;
			} catch ( Exception e ) {
				throw new RuntimeException(e);
			}
		}
	}

	@After
	public void teardown() {
		if ( httpServer != null ) {
			try {
				httpServer.stop();
			} catch ( Exception e ) {
				// ignore
			}
		}
	}

	/**
	 * Start the HTTP server.
	 *
	 * @throws Exception
	 *         if an error occurs
	 */
	public void startHttpServer() {
		try {
			if ( httpServer == null ) {
				setup();
			}
			httpServer.start();
		} catch ( RuntimeException e ) {
			throw e;
		} catch ( Exception e ) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Stop the HTTP server.
	 *
	 * @throws Exception
	 *         if an error occurs
	 */
	public void stopHttpServer() {
		if ( httpServer != null ) {
			try {
				httpServer.stop();
			} catch ( RuntimeException e ) {
				throw e;
			} catch ( Exception e ) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Register a handler.
	 *
	 * @param handler
	 *        the handler to register
	 */
	protected void addHandler(Handler handler) {
		if ( httpServer == null ) {
			try {
				setup();
			} catch ( RuntimeException e ) {
				throw e;
			} catch ( Exception e ) {
				throw new RuntimeException(e);
			}
		}
		httpServer.addHandler(handler);
	}

	/**
	 * Unregister a handler.
	 *
	 * @param handler
	 *        the handler to unregister
	 */
	protected void removeHandler(Handler handler) {
		if ( httpServer != null ) {
			httpServer.removeHandler(handler);
		}
	}

	/**
	 * Get the port the HTTP server is listening on.
	 *
	 * @return the HTTP server port
	 */
	protected int getHttpServerPort() {
		if ( httpServer != null ) {
			return httpServer.getHttpServerPort();
		}
		return httpServerPort;
	}

	/**
	 * Get a "base URL" for connecting to the HTTP server.
	 *
	 * @return the base URL (without any path) to the HTTP server
	 */
	protected String getHttpServerBaseUrl() {
		if ( httpServer == null ) {
			try {
				setup();
			} catch ( RuntimeException e ) {
				throw e;
			} catch ( Exception e ) {
				throw new RuntimeException(e);
			}
		}
		return httpServer.getHttpServerBaseUrl();
	}

}

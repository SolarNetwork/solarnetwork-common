/* ==================================================================
 * MqttConnection.java - 24/11/2019 12:29:14 pm
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

import java.io.Closeable;
import java.io.IOException;

/**
 * API for a connection to a MQTT broker.
 * 
 * @author matt
 * @version 1.0
 */
public interface MqttConnection extends Closeable {

	/**
	 * Open the connection, if it is not already open.
	 * 
	 * <p>
	 * The connection must be opened before calling any of the other methods in
	 * this API. The {@link #close()} method must be called when the connection
	 * is longer needed.
	 * </p>
	 * 
	 * @throws IOException
	 *         if the connection cannot be opened
	 */
	void open() throws IOException;

	/**
	 * Test if the connection has been established.
	 * 
	 * @return {@literal true} if the connection has been established,
	 *         {@literal false} if the connection has never been opened or has
	 *         been closed
	 */
	boolean isEstablished();

	/**
	 * Test if {@link #close()} has been called.
	 * 
	 * <p>
	 * This method does not necessarily verify if the physical connection has
	 * been terminated, it is merely an indication if {@link #close()} has been
	 * invoked.
	 * </p>
	 * 
	 * @return {@literal true} if {@link #close()} has been invoked on this
	 *         connection
	 */
	boolean isClosed();

}

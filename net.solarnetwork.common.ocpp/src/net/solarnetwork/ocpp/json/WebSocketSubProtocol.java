/* ==================================================================
 * WebSocketSubProtocol.java - 1/02/2020 8:03:21 am
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

package net.solarnetwork.ocpp.json;

/**
 * OCPP WebSocket sub-protocol enumeration.
 * 
 * @author matt
 * @version 1.1
 */
public enum WebSocketSubProtocol {

	/** OCPP version 1.2. */
	OCPP_V12("ocpp1.2"),

	/** OCPP version 1.5. */
	OCPP_V15("ocpp1.5"),

	/** OCPP version 1.6. */
	OCPP_V16("ocpp1.6"),

	/** OCPP version 2.0. */
	OCPP_V20("ocpp2.0"),

	/** OCPP version 2.0.1. */
	OCPP_V201("ocpp2.0.1");

	private final String value;

	private WebSocketSubProtocol(String value) {
		this.value = value;
	}

	/**
	 * Get the WebSocket sub-protocol value.
	 * 
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Get an enumeration instance for a value.
	 * 
	 * @param value
	 *        the OCPP value (e.g. {@link #getValue()})
	 * @return the associated enumeration
	 * @throws IllegalArgumentException
	 *         if {@code value} is not a valid value
	 */
	public static WebSocketSubProtocol forValue(String value) {
		for ( WebSocketSubProtocol p : values() ) {
			if ( p.value.equalsIgnoreCase(value) ) {
				return p;
			}
		}
		throw new IllegalArgumentException("Unknown protocol value [" + value + "]");
	}

}

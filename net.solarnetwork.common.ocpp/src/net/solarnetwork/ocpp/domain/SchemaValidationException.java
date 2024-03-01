/* ==================================================================
 * SchemaValidationException.java - 3/02/2020 3:14:12 pm
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

package net.solarnetwork.ocpp.domain;

/**
 * Exception for when schema validation fails for an object or message.
 * 
 * @author matt
 * @version 1.0
 */
public class SchemaValidationException extends RuntimeException {

	private static final long serialVersionUID = 5519379160853548931L;

	/** The source. */
	private final Object source;

	/**
	 * Constructor.
	 * 
	 * @param source
	 *        the source of the validation failure, for example the message
	 *        object
	 */
	public SchemaValidationException(Object source) {
		super();
		this.source = source;
	}

	/**
	 * Constructor.
	 * 
	 * @param source
	 *        the source of the validation failure, for example the message
	 *        object
	 * @param message
	 *        the message
	 */
	public SchemaValidationException(Object source, String message) {
		super(message);
		this.source = source;
	}

	/**
	 * Constructor.
	 * 
	 * @param source
	 *        the source of the validation failure, for example the message
	 *        object
	 * @param cause
	 *        the cause
	 */
	public SchemaValidationException(Object source, Throwable cause) {
		super(cause);
		this.source = source;
	}

	/**
	 * Constructor.
	 * 
	 * @param source
	 *        the source of the validation failure, for example the message
	 *        object
	 * @param message
	 *        the message
	 * @param cause
	 *        the cause
	 */
	public SchemaValidationException(Object source, String message, Throwable cause) {
		super(message, cause);
		this.source = source;
	}

	/**
	 * Get the source of the validation failure, for example the original
	 * message object.
	 * 
	 * @return the source object
	 */
	public Object getSource() {
		return source;
	}

}

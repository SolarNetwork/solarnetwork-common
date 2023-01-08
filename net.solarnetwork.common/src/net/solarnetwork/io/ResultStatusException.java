/* ==================================================================
 * ResultStatusException.java - 21/10/2019 6:20:07 am
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

package net.solarnetwork.io;

import java.net.URL;

/**
 * Exception thrown when an error or unexpected result status is returned from
 * an IO operation.
 * 
 * <p>
 * This exception can be used to report conditions like non-200 level HTTP
 * status codes.
 * </p>
 * 
 * @author matt
 * @version 1.0
 * @since 1.54
 */
public class ResultStatusException extends RuntimeException {

	private static final long serialVersionUID = 1692555466635119640L;

	/** The URL. */
	private final URL url;

	/** The status code. */
	private final int statusCode;

	/**
	 * Constructor.
	 * 
	 * @param statusCode
	 *        the status code
	 * @param message
	 *        the message
	 */
	public ResultStatusException(int statusCode, String message) {
		this(null, statusCode, message);
	}

	/**
	 * Constructor.
	 * 
	 * @param url
	 *        the URL
	 * @param statusCode
	 *        the status code
	 * @param message
	 *        the message
	 */
	public ResultStatusException(URL url, int statusCode, String message) {
		super(message);
		this.url = url;
		this.statusCode = statusCode;
	}

	/**
	 * Constructor.
	 * 
	 * @param statusCode
	 *        the status code
	 * @param message
	 *        the message
	 * @param cause
	 *        the cause
	 */
	public ResultStatusException(int statusCode, String message, Throwable cause) {
		this(null, statusCode, message, cause);
	}

	/**
	 * Constructor.
	 * 
	 * @param url
	 *        the URL
	 * @param statusCode
	 *        the status code
	 * @param message
	 *        the message
	 * @param cause
	 *        the cause
	 */
	public ResultStatusException(URL url, int statusCode, String message, Throwable cause) {
		super(message, cause);
		this.url = url;
		this.statusCode = statusCode;
	}

	/**
	 * Get the source URL, if available.
	 * 
	 * @return the url the URL, or {@literal null}
	 */
	public URL getUrl() {
		return url;
	}

	/**
	 * Get the result status code.
	 * 
	 * @return the status code
	 */
	public int getStatusCode() {
		return statusCode;
	}

}

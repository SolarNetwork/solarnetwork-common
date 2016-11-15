/* ==================================================================
 * Result.java - Nov 20, 2012 6:55:06 AM
 * 
 * Copyright 2007-2012 SolarNetwork.net Dev Team
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

package net.solarnetwork.domain;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * A simple service result envelope object.
 * 
 * @author matt
 * @version 1.1
 * @param <T>
 *        the object type
 */
@JsonPropertyOrder({ "success", "code", "message", "data" })
public class Result<T> {

	private final Boolean success;
	private final String code;
	private final String message;
	private final T data;

	/**
	 * Construct a successful response with no data.
	 */
	public Result() {
		this(Boolean.TRUE, null, null, null);
	}

	/**
	 * Construct a successful response with just data.
	 * 
	 * @param data
	 *        the data
	 */
	public Result(T data) {
		this(Boolean.TRUE, null, null, data);
	}

	/**
	 * Constructor.
	 * 
	 * @param success
	 *        flag of success
	 * @param code
	 *        optional code, e.g. error code
	 * @param message
	 *        optional descriptive message
	 * @param data
	 *        optional data in the response
	 */
	public Result(Boolean success, String code, String message, T data) {
		super();
		this.success = success;
		this.code = code;
		this.message = message;
		this.data = data;
	}

	/**
	 * Helper method to construct instance using generic return type inference.
	 * If you import this static method, then in your code you can write
	 * {@code return result(myData)} instead of
	 * {@code new Result&lt;Object&gt;(myData)}.
	 * 
	 * @param data
	 *        the data
	 * @return the response
	 */
	public static <V> Result<V> result(V data) {
		return new Result<V>(data);
	}

	public Boolean getSuccess() {
		return success;
	}

	public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	public T getData() {
		return data;
	}

}

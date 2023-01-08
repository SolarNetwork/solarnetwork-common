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

import java.util.Arrays;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * A simple service result envelope object.
 * 
 * @author matt
 * @version 1.2
 * @param <T>
 *        the object type
 */
@JsonPropertyOrder({ "success", "code", "message", "errors", "data" })
public class Result<T> {

	private final Boolean success;
	private final String code;
	private final String message;
	private final List<ErrorDetail> errors;
	private final T data;

	/**
	 * An error detail object.
	 */
	@JsonPropertyOrder({ "location", "code", "rejectedValue", "message" })
	public static class ErrorDetail {

		private final String location;
		private final String code;
		private final String rejectedValue;
		private final String message;

		/**
		 * Constructor.
		 * 
		 * @param location
		 *        the error location, such as a bean-style path, or
		 *        {@literal null} for an overall error
		 * @param rejectedValue
		 *        the value the rejected value, if available
		 * @param message
		 *        the error message
		 */
		public ErrorDetail(String location, String rejectedValue, String message) {
			this(location, null, rejectedValue, message);
		}

		/**
		 * Constructor.
		 * 
		 * @param location
		 *        the error location, such as a bean-style path, or
		 *        {@literal null} for an overall error
		 * @param code
		 *        the code
		 * @param rejectedValue
		 *        the value the rejected value, if available
		 * @param message
		 *        the error message
		 */
		public ErrorDetail(String location, String code, String rejectedValue, String message) {
			super();
			this.location = location;
			this.code = code;
			this.rejectedValue = rejectedValue;
			this.message = message;
		}

		/**
		 * Get the error location.
		 * 
		 * @return the location
		 */
		public String getLocation() {
			return location;
		}

		/**
		 * Get an error code.
		 * 
		 * @return the code
		 */
		public String getCode() {
			return code;
		}

		/**
		 * Get the rejected value.
		 * 
		 * @return the rejectedValue
		 */
		public String getRejectedValue() {
			return rejectedValue;
		}

		/**
		 * Get the error message.
		 * 
		 * @return the message
		 */
		public String getMessage() {
			return message;
		}

	}

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
		this(success, code, message, null, data);
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
	 * @param errors
	 *        optional errors
	 * @param data
	 *        optional data in the response
	 * @since 1.2
	 */
	public Result(Boolean success, String code, String message, List<ErrorDetail> errors, T data) {
		super();
		this.success = success;
		this.code = code;
		this.message = message;
		this.errors = errors;
		this.data = data;
	}

	/**
	 * Helper method to construct instance using generic return type inference.
	 * 
	 * <p>
	 * This is an alias for {@link #success(Object)}. If you import this static
	 * method, then in your code you can write {@code return result(myData)}
	 * instead of {@code new Result&lt;Object&gt;(myData)}.
	 * </p>
	 * 
	 * @param <V>
	 *        the result type
	 * @param data
	 *        the data
	 * @return the result with {@code success} set to {@literal true}
	 * @see #success(Object)
	 */
	public static <V> Result<V> result(V data) {
		return success(data);
	}

	/**
	 * Helper method to construct a success instance using generic return type
	 * inference.
	 * <p>
	 * If you import this static method, then in your code you can write
	 * {@code return success(myData)} instead of
	 * {@code new Result&lt;Object&gt;(myData)}.
	 * </p>
	 *
	 * @param <V>
	 *        the result type
	 * @param data
	 *        the value
	 * @return the result with {@code success} set to {@literal true}
	 * @since 1.2
	 */
	public static <V> Result<V> success(V data) {
		return new Result<V>(Boolean.TRUE, null, null, null, data);
	}

	/**
	 * Helper method to construct a success instance using generic return type
	 * inference.
	 *
	 * @param <V>
	 *        the result type
	 * @return the result with {@code success} set to {@literal true}
	 * @since 1.2
	 */
	public static <V> Result<V> success() {
		return new Result<V>(Boolean.TRUE, null, null, null, null);
	}

	/**
	 * Helper method to construct an error instance using generic return type
	 * inference.
	 *
	 * @param <V>
	 *        the result type
	 * @return the result
	 * @since 1.2
	 */
	public static <V> Result<V> error() {
		return new Result<V>(Boolean.FALSE, null, null, null, null);
	}

	/**
	 * Helper method to construct an error instance using generic return type
	 * inference.
	 *
	 * @param <V>
	 *        the result type
	 * @param message
	 *        the message
	 * @param errors
	 *        the errors
	 * @return the result
	 * @since 1.2
	 */
	public static <V> Result<V> error(String message, List<ErrorDetail> errors) {
		return new Result<V>(Boolean.FALSE, null, message, errors, null);
	}

	/**
	 * Helper method to construct an error instance using generic return type
	 * inference.
	 *
	 * @param <V>
	 *        the result type
	 * @param code
	 *        the code
	 * @param message
	 *        the message
	 * @param errors
	 *        the errors
	 * @return the result
	 * @since 1.2
	 */
	public static <V> Result<V> error(String code, String message, List<ErrorDetail> errors) {
		return new Result<V>(Boolean.FALSE, code, message, errors, null);
	}

	/**
	 * Helper method to construct an error instance using generic return type
	 * inference.
	 *
	 * @param <V>
	 *        the result type
	 * @param code
	 *        the code
	 * @param message
	 *        the message
	 * @param errors
	 *        the errors
	 * @return the result
	 * @since 1.2
	 */
	public static <V> Result<V> error(String code, String message, ErrorDetail... errors) {
		return new Result<V>(Boolean.FALSE, code, message,
				(errors != null && errors.length > 0 ? Arrays.asList(errors) : null), null);
	}

	/**
	 * Get the success indicator.
	 * 
	 * @return the success indicator, or {@literal null} if not known
	 */
	public Boolean getSuccess() {
		return success;
	}

	/**
	 * Get the code.
	 * 
	 * @return the code, or {@literal null}
	 */
	public String getCode() {
		return code;
	}

	/**
	 * Get the message.
	 * 
	 * @return the message, or {@literal null}
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Get the error details.
	 * 
	 * @return the error details, or {@literal null}
	 */
	public List<ErrorDetail> getErrors() {
		return errors;
	}

	/**
	 * Get the data.
	 * 
	 * @return the data, or {@literal null}
	 */
	public T getData() {
		return data;
	}

}

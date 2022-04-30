/* ==================================================================
 * ObjectUtils.java - 7/10/2021 10:14:12 AM
 * 
 * Copyright 2021 SolarNetwork.net Dev Team
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

package net.solarnetwork.util;

import java.util.Collection;

/**
 * Utilities for dealing with objects.
 * 
 * @author matt
 * @version 1.1
 */
public final class ObjectUtils {

	/**
	 * Require a non-null method argument.
	 * 
	 * <p>
	 * This is similar to
	 * {@link java.util.Objects#requireNonNull(Object, String)} except
	 * {@code argumentName} is just the name of the required argument and an
	 * {@link IllegalArgumentException} is thrown instead of a
	 * {@code NullPointerException}. Example use:
	 * </p>
	 * 
	 * <!-- @formatter:off -->
	 * <blockquote><pre>
	 * public Foo(Bar bar, Baz baz) {
	 * 	this.bar = ObjectUtils.requireNonNullArgument(bar, "bar");
	 * 	this.baz = ObjectUtils.requireNonNullArgument(baz, "baz");
	 * }
	 * </pre></blockquote>
	 * <!-- @formatter:on -->
	 * 
	 * @param <T>
	 *        the argument type
	 * @param arg
	 *        the argument to require to be non-null
	 * @param argumentName
	 *        the name of {@code arg} to report in the
	 *        {@link IllegalArgumentException} if {@code arg} is {@literal null}
	 * @return {@code arg}
	 * @throws IllegalArgumentException
	 *         if {@code arg} is {@literal null}
	 */
	public static <T> T requireNonNullArgument(T arg, String argumentName) {
		if ( arg == null ) {
			throw new IllegalArgumentException(
					String.format("The %s argument must not be null.", argumentName));
		}
		return arg;
	}

	/**
	 * Require a non-empty array method argument.
	 * 
	 * <p>
	 * This is similar to
	 * {@link java.util.Objects#requireNonNull(Object, String)} except
	 * {@code argumentName} is just the name of the required argument and an
	 * {@link IllegalArgumentException} is thrown instead of a
	 * {@code NullPointerException}. Example use:
	 * </p>
	 * 
	 * <!-- @formatter:off -->
	 * <blockquote><pre>
	 * public Foo(Bar[] bar) {
	 * 	this.bar = ObjectUtils.requireNonNullArgument(bar, "bar");
	 * }
	 * </pre></blockquote>
	 * <!-- @formatter:on -->
	 * 
	 * @param <T>
	 *        the argument type
	 * @param arg
	 *        the argument to require to be non-empty
	 * @param argumentName
	 *        the name of {@code arg} to report in the
	 *        {@link IllegalArgumentException} if {@code arg} is {@literal null}
	 * @return {@code arg}
	 * @throws IllegalArgumentException
	 *         if {@code arg} is {@literal null} or empty
	 */
	public static <T> T[] requireNonEmptyArgument(T[] arg, String argumentName) {
		if ( arg == null || arg.length < 1 ) {
			throw new IllegalArgumentException(
					String.format("The %s argument must not be empty.", argumentName));
		}
		return arg;
	}

	/**
	 * Require a non-empty array method argument.
	 * 
	 * <p>
	 * This is similar to
	 * {@link java.util.Objects#requireNonNull(Object, String)} except
	 * {@code argumentName} is just the name of the required argument and an
	 * {@link IllegalArgumentException} is thrown instead of a
	 * {@code NullPointerException}. Example use:
	 * </p>
	 * 
	 * <!-- @formatter:off -->
	 * <blockquote><pre>
	 * public Foo(List&lt;Bar&gt; bar) {
	 * 	this.bar = ObjectUtils.requireNonNullArgument(bar, "bar");
	 * }
	 * </pre></blockquote>
	 * <!-- @formatter:on -->
	 * 
	 * @param <T>
	 *        the argument type
	 * @param arg
	 *        the argument to require to be non-empty
	 * @param argumentName
	 *        the name of {@code arg} to report in the
	 *        {@link IllegalArgumentException} if {@code arg} is {@literal null}
	 * @return {@code arg}
	 * @throws IllegalArgumentException
	 *         if {@code arg} is {@literal null} or empty
	 */
	public static <T extends Collection<?>> T requireNonEmptyArgument(T arg, String argumentName) {
		if ( arg == null || arg.size() < 1 ) {
			throw new IllegalArgumentException(
					String.format("The %s argument must not be empty.", argumentName));
		}
		return arg;
	}

}

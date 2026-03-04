/* ==================================================================
 * ArrayUtils.java - 16/03/2018 6:48:58 AM
 *
 * Copyright 2018 SolarNetwork.net Dev Team
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

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectFactory;

/**
 * Utilities for dealing with arrays.
 *
 * @author matt
 * @version 1.4
 * @since 1.42
 */
public final class ArrayUtils {

	private ArrayUtils() {
		// do not construct me
	}

	/**
	 * Grow an array to a minimum length, filling in any new elements with newly
	 * allocated objects.
	 *
	 * <p>
	 * This method can only lengthen an array of objects. After adjusting the
	 * array length, any {@code null} element in the array will be initialized
	 * to an object returned from {@code factory}, or if {@code factory} is not
	 * provided a new instance of {@code itemClass} via
	 * {@link Class#newInstance()}. The {@link ObjectFactory#getObject()} method
	 * (or class constructor if no {@code factory} provided) will be called for
	 * <b>each</b> {@code null} array index.
	 * </p>
	 *
	 * <p>
	 * Note that if a size adjustment is made, a new array instance is returned
	 * from this method, with elements copied from {@code array} where
	 * appropriate. If no size adjustment is necessary, then {@code array} is
	 * returned directly.
	 * </p>
	 *
	 * @param <T>
	 *        the array item type
	 * @param array
	 *        the source array, or {@code null}
	 * @param minLength
	 *        the desired minimum length of the array; if less than zero will be
	 *        treated as zero
	 * @param itemClass
	 *        the class of array items
	 * @param factory
	 *        a factory to create new array items, or {@code null} to create
	 *        {@code itemClass} instances directly
	 * @return a copy of {@code array} with the adjusted length, or
	 *         {@code array} if no adjustment was necessary
	 * @see #arrayOfLength(Object[], int, Class, Supplier)
	 */
	public static <T> T @Nullable [] arrayWithLength(T @Nullable [] array, int minLength,
			Class<T> itemClass, @Nullable ObjectFactory<? extends T> factory) {
		return arrayOfLength(array, minLength, itemClass,
				factory != null ? (Supplier<T>) factory::getObject : null);
	}

	/**
	 * Grow an array to a minimum length, filling in any new elements with newly
	 * allocated objects.
	 *
	 * <p>
	 * This method can only lengthen an array of objects. After adjusting the
	 * array length, any {@code null} element in the array will be initialized
	 * to an object returned from {@code factory}, or if {@code factory} is not
	 * provided a new instance of {@code itemClass} via
	 * {@link Class#newInstance()}. The {@link ObjectFactory#getObject()} method
	 * (or class constructor if no {@code factory} provided) will be called for
	 * <b>each</b> {@code null} array index.
	 * </p>
	 *
	 * <p>
	 * Note that if a size adjustment is made, a new array instance is returned
	 * from this method, with elements copied from {@code array} where
	 * appropriate. If no size adjustment is necessary, then {@code array} is
	 * returned directly.
	 * </p>
	 *
	 * @param <T>
	 *        the array item type
	 * @param array
	 *        the source array, or {@code null}
	 * @param minLength
	 *        the desired length of the array; if less than zero will be treated
	 *        as zero
	 * @param itemClass
	 *        the class of array items
	 * @param factory
	 *        a factory to create new array items, or {@code null} to create
	 *        {@code itemClass} instances directly
	 * @return a copy of {@code array} with the adjusted length, or
	 *         {@code array} if no adjustment was necessary
	 * @since 1.3
	 * @see #arrayOfLength(Object[], int, Class, boolean, Supplier)
	 */
	public static <T> T @Nullable [] arrayOfLength(T @Nullable [] array, int minLength,
			Class<T> itemClass, @Nullable Supplier<? extends T> factory) {
		return arrayOfLength(array, minLength, itemClass, false, factory);
	}

	/**
	 * Adjust an array to a specific length, filling in any new elements with
	 * newly allocated objects.
	 *
	 * <p>
	 * This method can shorten or lengthen an array of objects. After adjusting
	 * the array length, any {@code null} element in the array will be
	 * initialized to an object returned from {@code factory}, or if
	 * {@code factory} is not provided a new instance of {@code itemClass} via
	 * {@link Class#newInstance()}. The {@link ObjectFactory#getObject()} method
	 * (or class constructor if no {@code factory} provided) will be called for
	 * <b>each</b> {@code null} array index.
	 * </p>
	 *
	 * <p>
	 * Note that if a size adjustment is made, a new array instance is returned
	 * from this method, with elements copied from {@code array} where
	 * appropriate. If no size adjustment is necessary, then {@code array} is
	 * returned directly.
	 * </p>
	 *
	 * @param <T>
	 *        the array item type
	 * @param array
	 *        the source array, or {@code null}
	 * @param count
	 *        the desired length of the array; if less than zero will be treated
	 *        as zero; if {@code shrink} if {@code false} this becomes the
	 *        desired minimum length of the array
	 * @param itemClass
	 *        the class of array items
	 * @param shrink
	 *        {@code true} to allow shrinking {@code array} to {@code count}
	 *        length; otherwise only allow growing {@code array} to
	 *        {@code count} minimum length so if {@code array} is already larger
	 *        than {@code count} the returned array will preserve the longer
	 *        length
	 * @param factory
	 *        a factory to create new array items, or {@code null} to create
	 *        {@code itemClass} instances directly
	 * @return a copy of {@code array} with the adjusted length, or
	 *         {@code array} if no adjustment was necessary
	 * @since 1.4
	 */
	public static <T> T @Nullable [] arrayOfLength(T @Nullable [] array, int count, Class<T> itemClass,
			boolean shrink, @Nullable Supplier<? extends T> factory) {
		if ( count < 0 ) {
			count = 0;
		}
		final int inCount = (array == null ? -1 : array.length);
		final int outCount = (shrink ? count : Math.max(inCount, count));
		T[] result = array;
		if ( inCount != outCount ) {
			@SuppressWarnings("unchecked")
			T[] newArray = (T[]) Array.newInstance(itemClass, outCount);
			if ( array != null ) {
				System.arraycopy(array, 0, newArray, 0, Math.min(outCount, inCount));
			}
			for ( int i = 0; i < outCount; i++ ) {
				if ( newArray[i] == null ) {
					if ( factory != null ) {
						newArray[i] = factory.get();
					} else {
						try {
							newArray[i] = itemClass.getDeclaredConstructor().newInstance();
						} catch ( IllegalArgumentException | InvocationTargetException
								| NoSuchMethodException | SecurityException | IllegalAccessException
								| InstantiationException e ) {
							throw new RuntimeException(e);
						}
					}
				}
			}
			result = newArray;
		}
		return result;
	}

	/**
	 * Filter a set of strings based on enabled/disabled patterns.
	 *
	 * <p>
	 * The {@code enabled} patterns are applied first, followed by
	 * {@code disabled}. Both arguments are optional. The patterns are treated
	 * in a case-insensitive manner, using {@link Matcher#find()} to find
	 * partial matches.
	 * </p>
	 *
	 * @param list
	 *        the list to filter
	 * @param enabled
	 *        an optional list of regular expressions to limit the result to
	 * @param disabled
	 *        an optional list of regular expressions to exclude from the
	 *        results
	 * @return the filtered list of protocols
	 * @since 1.1
	 */
	public static String[] filterByEnabledDisabled(String[] list, String @Nullable [] enabled,
			String @Nullable [] disabled) {
		String[] finalEnabledProtocols = list;
		if ( enabled != null ) {
			Set<Pattern> pats = new LinkedHashSet<>(enabled.length);
			for ( String proto : enabled ) {
				pats.add(Pattern.compile(proto, Pattern.CASE_INSENSITIVE));
			}
			finalEnabledProtocols = Arrays.stream(finalEnabledProtocols).filter(
					p -> pats.stream().filter(pat -> pat.matcher(p).find()).findAny().isPresent())
					.toArray(String[]::new);
		}
		if ( disabled != null ) {
			for ( String proto : disabled ) {
				Pattern pat = Pattern.compile(proto, Pattern.CASE_INSENSITIVE);
				finalEnabledProtocols = Arrays.stream(finalEnabledProtocols)
						.filter(p -> !pat.matcher(p).find()).toArray(String[]::new);
			}
		}
		return finalEnabledProtocols;
	}

	/**
	 * Test if an array has only {@code null} elements or is itself {@code null}
	 * or empty.
	 *
	 * <p>
	 * This method will perform a linear search for the first non-null element.
	 * </p>
	 *
	 * @param array
	 *        the array to test
	 * @return {@literal true} if {@code array} is {@code null}, empty, or has
	 *         only {@code null} elements
	 * @since 1.2
	 */
	public static boolean isOnlyNull(@Nullable Object @Nullable [] array) {
		if ( array == null || array.length < 1 ) {
			return true;
		}
		for ( Object o : array ) {
			if ( o != null ) {
				return false;
			}
		}
		return true;
	}

}

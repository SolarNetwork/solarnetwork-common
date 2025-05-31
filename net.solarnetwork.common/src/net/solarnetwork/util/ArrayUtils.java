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
import org.springframework.beans.factory.ObjectFactory;

/**
 * Utilities for dealing with arrays.
 *
 * @author matt
 * @version 1.3
 * @since 1.42
 */
public final class ArrayUtils {

	private ArrayUtils() {
		// do not construct me
	}

	/**
	 * Adjust an array to a specific length, filling in any new elements with
	 * newly objects.
	 *
	 * <p>
	 * This method can shorten or lengthen an array of objects. After adjusting
	 * the array length, any {@literal null} element in the array will be
	 * initialized to an object returned from {@code factory}, or if
	 * {@code factory} is not provided a new instance of {@code itemClass} via
	 * {@link Class#newInstance()}. The {@link ObjectFactory#getObject()} method
	 * (or class constructor if no {@code factory} provided) will be called for
	 * <b>each</b> {@literal null} array index.
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
	 *        the source array, or {@literal null}
	 * @param count
	 *        the desired length of the array; if less than zero will be treated
	 *        as zero
	 * @param itemClass
	 *        the class of array items
	 * @param factory
	 *        a factory to create new array items, or {@literal null} to create
	 *        {@code itemClass} instances directly
	 * @return a copy of {@code array} with the adjusted length, or
	 *         {@code array} if no adjustment was necessary
	 * @see #arrayOfLength(Object[], int, Class, Supplier)
	 */
	public static <T> T[] arrayWithLength(T[] array, int count, Class<T> itemClass,
			ObjectFactory<? extends T> factory) {
		return arrayOfLength(array, count, itemClass,
				factory != null ? (Supplier<T>) factory::getObject : null);
	}

	/**
	 * Adjust an array to a specific length, filling in any new elements with
	 * newly objects.
	 *
	 * <p>
	 * This method can shorten or lengthen an array of objects. After adjusting
	 * the array length, any {@literal null} element in the array will be
	 * initialized to an object returned from {@code factory}, or if
	 * {@code factory} is not provided a new instance of {@code itemClass} via
	 * {@link Class#newInstance()}. The {@link ObjectFactory#getObject()} method
	 * (or class constructor if no {@code factory} provided) will be called for
	 * <b>each</b> {@literal null} array index.
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
	 *        the source array, or {@literal null}
	 * @param count
	 *        the desired length of the array; if less than zero will be treated
	 *        as zero
	 * @param itemClass
	 *        the class of array items
	 * @param factory
	 *        a factory to create new array items, or {@literal null} to create
	 *        {@code itemClass} instances directly
	 * @return a copy of {@code array} with the adjusted length, or
	 *         {@code array} if no adjustment was necessary
	 * @since 1.3
	 */
	public static <T> T[] arrayOfLength(T[] array, int count, Class<T> itemClass,
			Supplier<? extends T> factory) {
		if ( count < 0 ) {
			count = 0;
		}
		int inCount = (array == null ? -1 : array.length);
		T[] result = array;
		if ( inCount != count ) {
			@SuppressWarnings("unchecked")
			T[] newIncs = (T[]) Array.newInstance(itemClass, count);
			if ( array != null ) {
				System.arraycopy(array, 0, newIncs, 0, Math.min(count, inCount));
			}
			for ( int i = 0; i < count; i++ ) {
				if ( newIncs[i] == null ) {
					if ( factory != null ) {
						newIncs[i] = factory.get();
					} else {
						try {
							newIncs[i] = itemClass.getDeclaredConstructor().newInstance();
						} catch ( IllegalArgumentException | InvocationTargetException
								| NoSuchMethodException | SecurityException | IllegalAccessException
								| InstantiationException e ) {
							throw new RuntimeException(e);
						}
					}
				}
			}
			result = newIncs;
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
	public static String[] filterByEnabledDisabled(String[] list, String[] enabled, String[] disabled) {
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
	 * Test if an array has only {@literal null} elements or is itself
	 * {@literal null} or empty.
	 *
	 * <p>
	 * This method will perform a linear search for the first non-null element.
	 * </p>
	 *
	 * @param array
	 *        the array to test
	 * @return {@literal true} if {@code array} is {@literal null}, empty, or
	 *         has only {@literal null} elements
	 * @since 1.2
	 */
	public static boolean isOnlyNull(Object[] array) {
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

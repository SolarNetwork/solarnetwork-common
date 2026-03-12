/* ==================================================================
 * ServiceConfiguration.java - 17/10/2024 7:50:34 am
 *
 * Copyright 2024 SolarNetwork.net Dev Team
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

package net.solarnetwork.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;
import net.solarnetwork.util.CollectionUtils;
import net.solarnetwork.util.NumberUtils;
import net.solarnetwork.util.StringUtils;

/**
 * API for a user-supplied set of configuration to use with some service.
 *
 * @author matt
 * @version 1.3
 * @since 3.24
 */
public interface ServiceConfiguration {

	/**
	 * Get a map of properties to pass to the service in order to perform
	 * actions.
	 *
	 * <p>
	 * It is expected this map would contain user-supplied runtime configuration
	 * such as credentials to use, host name, etc.
	 * </p>
	 *
	 * @return the runtime properties to pass to the service
	 */
	@Nullable
	Map<String, ?> getServiceProperties();

	/**
	 * Check if a service property value is present.
	 *
	 * @param key
	 *        the service property key to check has a value
	 * @return {@literal true} if the service property value exists and if a
	 *         string value is not empty
	 */
	default boolean hasServiceProperty(String key) {
		assert key != null;
		Map<String, ?> props = getServiceProperties();
		Object val = (props != null ? props.get(key) : null);
		if ( val == null ) {
			return false;
		}
		if ( val instanceof String s && s.isEmpty() ) {
			return false;
		}
		return true;
	}

	/**
	 * Check if a service property value is present and of a given type.
	 *
	 * @param <T>
	 *        the expected type of the value
	 * @param key
	 *        the service property key to check has a value
	 * @param type
	 *        the type of the value
	 * @return {@literal true} if the service property value exists and can be
	 *         returned as the given type and if a string value is not empty, or
	 *         {@code null} if not available or cannot be converted to the given
	 *         type
	 */
	default <T> boolean hasServiceProperty(String key, Class<T> type) {
		assert key != null && type != null;
		return serviceProperty(key, type) != null;
	}

	/**
	 * Get a service property value.
	 *
	 * @param <T>
	 *        the expected type of the value
	 * @param key
	 *        the service property key to get the value for
	 * @param type
	 *        the type of the value
	 * @return the service property value, or {@code null} if not available or
	 *         cannot be converted to the given type or if a string value is
	 *         empty
	 */
	default <T> @Nullable T serviceProperty(String key, Class<T> type) {
		return CollectionUtils.mapProperty(key, type, null, getServiceProperties());
	}

	/**
	 * Get a service property, assuming non-null result.
	 *
	 * <p>
	 * This method is a nullness-check shortcut, for example to be used after
	 * {@link #hasServiceProperty(String, Class)} returns {@code true}.
	 * </p>
	 *
	 * @param <T>
	 *        the expected type of the value
	 * @param key
	 *        the service property key to get the value for
	 * @param type
	 *        the type of the value
	 * @return the service property value, or {@code null} if not available or
	 *         cannot be converted to the given type or if a string value is
	 *         empty
	 * @since 1.2
	 */
	@SuppressWarnings("NullAway")
	default <T> T serviceProp(String key, Class<T> type) {
		return serviceProperty(key, type);
	}

	/**
	 * Resolve a string map from a service property value.
	 *
	 * @param key
	 *        the service property key to extract
	 * @return the mapping, or {@code null}
	 * @since 1.1
	 */
	default @Nullable Map<String, String> servicePropertyStringMap(@Nullable String key) {
		return CollectionUtils.mapPropertyStringMap(key, getServiceProperties());
	}

	/**
	 * Resolve a string map from a service property value on a configuration.
	 *
	 * @param configuration
	 *        the configuration to extract the mapping from
	 * @param key
	 *        the service property key to extract
	 * @return the mapping, or {@code null}
	 * @see #servicePropertyStringMap(String)
	 * @since 1.1
	 */
	static @Nullable Map<String, String> servicePropertyStringMap(
			@Nullable ServiceConfiguration configuration, @Nullable String key) {
		if ( configuration == null || key == null ) {
			return null;
		}
		return configuration.servicePropertyStringMap(key);
	}

	/**
	 * Resolve a list from a service property value.
	 *
	 * <p>
	 * The property value can be a {@code List}, array, or a comma-delimited
	 * single value.
	 * </p>
	 *
	 * @param key
	 *        the service property key to extract
	 * @return the list, or {@code null}
	 * @since 1.1
	 */
	default @Nullable List<String> servicePropertyStringList(@Nullable String key) {
		return CollectionUtils.mapPropertyStringList(key, getServiceProperties());
	}

	/**
	 * Resolve a list from a service property value on a configuration.
	 *
	 * @param configuration
	 *        the configuration to extract the mapping from
	 * @param key
	 *        the service property key to extract
	 * @return the list, or {@code null}
	 * @see #servicePropertyStringList(String)
	 * @since 1.1
	 */
	static @Nullable List<String> servicePropertyStringList(@Nullable ServiceConfiguration configuration,
			@Nullable String key) {
		if ( configuration == null || key == null ) {
			return null;
		}
		return configuration.servicePropertyStringList(key);
	}

	/**
	 * Resolve a {@link Duration} from a setting on a configuration.
	 *
	 * <p>
	 * The property value can be a number value, which will be treated as
	 * seconds, or an ISO duration suitable for passing to
	 * {@link Duration#parse(CharSequence)} (for example {@code PT2H} for "2
	 * hours").
	 * </p>
	 *
	 * @param key
	 *        the service property key to extract
	 * @param defaultResult
	 *        the default duration to return if the property is not available
	 * @return the duration, or {@code defaultResult} if a duration is not
	 *         available
	 * @since 1.3
	 */
	default @Nullable Duration servicePropertyDuration(String key, @Nullable Duration defaultResult) {
		return CollectionUtils.mapPropertyDuration(key, defaultResult, getServiceProperties());
	}

	/**
	 * Resolve a {@link Duration} from a setting on a configuration.
	 *
	 * @param configuration
	 *        the configuration to extract the mapping from
	 * @param key
	 *        the service property key to extract
	 * @param defaultResult
	 *        the default duration to return if the property is not available
	 * @return the duration, or {@code defaultResult} if a duration is not
	 *         available
	 * @see #servicePropertyDuration(String, Duration)
	 * @since 1.3
	 */
	static @Nullable Duration servicePropertyDuration(@Nullable ServiceConfiguration configuration,
			String key, @Nullable Duration defaultResult) {
		if ( configuration == null ) {
			return defaultResult;
		}
		return configuration.servicePropertyDuration(key, defaultResult);
	}

	/**
	 * Resolve an {@link Instant} from a setting on a configuration.
	 *
	 * <p>
	 * The property value can be a number value, which will be treated as
	 * milliseconds since the epoch, or an ISO timestamp suitable for passing to
	 * {@link Instant#parse(CharSequence)} (for example {@code PT2H} for "2
	 * hours").
	 * </p>
	 *
	 * @param key
	 *        the service property key to extract
	 * @param defaultResult
	 *        the default instant to return if the property is not available
	 * @return the instant, or {@code defaultResult} if a timestamp is not
	 *         available
	 * @since 1.3
	 */
	default @Nullable Instant servicePropertyTimestamp(String key, @Nullable Instant defaultResult) {
		return CollectionUtils.mapPropertyTimestamp(key, defaultResult, getServiceProperties());
	}

	/**
	 * Resolve an {@link Instant} from a setting on a configuration.
	 *
	 * @param configuration
	 *        the configuration to extract the timestamp from
	 * @param key
	 *        the service property key to extract
	 * @param defaultResult
	 *        the default duration to return if the property is not available
	 * @return the duration, or {@code defaultResult} if a duration is not
	 *         available
	 * @see #servicePropertyTimestamp(String, Instant)
	 * @since 1.3
	 */
	static @Nullable Instant servicePropertyTimestamp(@Nullable ServiceConfiguration configuration,
			String key, @Nullable Instant defaultResult) {
		if ( configuration == null ) {
			return defaultResult;
		}
		return configuration.servicePropertyTimestamp(key, defaultResult);
	}

	/**
	 * Resolve a {@link Number} from a setting on a configuration.
	 *
	 * <p>
	 * String values will be parsed witH
	 * {@link StringUtils#numberValue(String)}, and narrowed to 32-bit (if
	 * possible) using {@link NumberUtils#narrow32(Number)}.
	 * </p>
	 *
	 * @param key
	 *        the service property key to extract
	 * @param defaultResult
	 *        the default number to return if the property is not available
	 * @return the number, or {@code defaultResult} if a number is not available
	 * @see #servicePropertyNumber(String, Number, Function, Function)
	 * @since 1.3
	 */
	default @Nullable Number servicePropertyNumber(String key, @Nullable Number defaultResult) {
		return servicePropertyNumber(key, defaultResult, StringUtils::numberValue,
				NumberUtils::narrow32);
	}

	/**
	 * Resolve a {@link Number} from a setting on a configuration.
	 *
	 * <p>
	 * The property value can be a number or string value.
	 * </p>
	 *
	 * <p>
	 * First, if {@code parser} is provided then non-numeric property values are
	 * converted to strings and passed to this function.
	 * </p>
	 *
	 * <p>
	 * Then, if {@code mapper} is provided then numeric property values are
	 * passed to this function, with the function result being returned.
	 * </p>
	 *
	 * @param key
	 *        the service property key to extract
	 * @param defaultResult
	 *        the default number to return if the property is not available
	 * @param parser
	 *        an optional function to map string values to numbers
	 * @param mapper
	 *        an optional function to map number values to other forms, for
	 *        example {@code NumberUtils::bigDecimalForNumber}
	 * @return the number, or {@code defaultResult} if a number is not available
	 * @since 1.3
	 */
	default @Nullable Number servicePropertyNumber(String key, @Nullable Number defaultResult,
			@Nullable Function<String, Number> parser, @Nullable Function<Number, Number> mapper) {
		return CollectionUtils.mapPropertyNumber(key, defaultResult, parser, mapper,
				getServiceProperties());
	}

	/**
	 * Resolve a {@link Number} from a setting on a configuration.
	 *
	 * @param configuration
	 *        the configuration to extract the mapping from
	 * @param key
	 *        the service property key to extract
	 * @param defaultResult
	 *        the default number to return if the property is not available
	 * @return the number, or {@code defaultResult} if a number is not available
	 * @see #servicePropertyNumber(String, Number, Function, Function)
	 * @since 1.3
	 */
	static @Nullable Number servicePropertyNumber(@Nullable ServiceConfiguration configuration,
			String key, @Nullable Number defaultResult) {
		if ( configuration == null ) {
			return defaultResult;
		}
		return configuration.servicePropertyNumber(key, defaultResult);
	}

	/**
	 * Resolve a {@link Number} from a setting on a configuration.
	 *
	 * @param configuration
	 *        the configuration to extract the mapping from
	 * @param key
	 *        the service property key to extract
	 * @param defaultResult
	 *        the default number to return if the property is not available
	 * @param parser
	 *        an optional function to map string values to numbers, for example
	 *        {@code StringUtils::numberValue}
	 * @param mapper
	 *        an optional function to map number values to other forms, for
	 *        example {@code NumberUtils::bigDecimalForNumber}
	 * @return the number, or {@code defaultResult} if a number is not available
	 * @see #servicePropertyNumber(String, Number, Function, Function)
	 * @since 1.3
	 */
	static @Nullable Number servicePropertyNumber(@Nullable ServiceConfiguration configuration,
			String key, @Nullable Number defaultResult, @Nullable Function<String, Number> parser,
			@Nullable Function<Number, Number> mapper) {
		if ( configuration == null ) {
			return defaultResult;
		}
		return configuration.servicePropertyNumber(key, defaultResult, parser, mapper);
	}

}

/* ==================================================================
 * ConfigurationItem.java - 10/02/2020 4:27:25 pm
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

import static net.solarnetwork.util.ObjectUtils.requireNonNullArgument;
import org.jspecify.annotations.Nullable;

/**
 * A configuration/setting item.
 *
 * @author matt
 * @version 1.0
 */
public class ConfigurationItem {

	private final String key;
	private final boolean readonly;
	private final @Nullable String value;

	/**
	 * Constructor.
	 *
	 * <p>
	 * The {@code readonly} flag will be set to {@literal false}.
	 * </p>
	 *
	 * @param key
	 *        the key
	 * @param value
	 *        the value
	 */
	public ConfigurationItem(String key, @Nullable String value) {
		this(key, value, false);
	}

	/**
	 * Constructor.
	 *
	 * @param key
	 *        the key
	 * @param readonly
	 *        the read-only flag
	 * @param value
	 *        the value
	 * @throws IllegalArgumentException
	 *         if {@code key} is {@code null}
	 */
	public ConfigurationItem(String key, @Nullable String value, boolean readonly) {
		super();
		this.key = requireNonNullArgument(key, "key");
		this.value = value;
		this.readonly = readonly;
	}

	/**
	 * Get the key.
	 *
	 * @return the key
	 */
	public final String getKey() {
		return key;
	}

	/**
	 * Get the read-only flag.
	 *
	 * @return {@literal true} if the configuration item is read-only and cannot
	 *         be changed
	 */
	public final boolean isReadonly() {
		return readonly;
	}

	/**
	 * Get the value.
	 *
	 * @return the value
	 */
	public final @Nullable String getValue() {
		return value;
	}

}

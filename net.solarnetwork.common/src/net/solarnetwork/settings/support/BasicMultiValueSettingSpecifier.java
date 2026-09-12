/* ==================================================================
 * BasicMultiValueSettingSpecifier.java - Mar 12, 2012 10:11:50 AM
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

package net.solarnetwork.settings.support;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.springframework.context.MessageSource;
import net.solarnetwork.domain.KeyedValue;
import net.solarnetwork.settings.MappableSpecifier;
import net.solarnetwork.settings.MultiValueSettingSpecifier;
import net.solarnetwork.settings.SettingSpecifier;

/**
 * Basic implementation of {@link MultiValueSettingSpecifier}.
 *
 * @author matt
 * @version 1.2
 */
public class BasicMultiValueSettingSpecifier extends BasicTextFieldSettingSpecifier
		implements MultiValueSettingSpecifier {

	/**
	 * Constructor.
	 *
	 * @param key
	 *        the key
	 * @param defaultValue
	 *        the default value
	 */
	public BasicMultiValueSettingSpecifier(String key, @Nullable String defaultValue) {
		super(key, defaultValue);
	}

	@Override
	public SettingSpecifier mappedWithPlaceholer(String template) {
		BasicMultiValueSettingSpecifier spec = new BasicMultiValueSettingSpecifier(
				String.format(template, getKey()), getDefaultValue());
		spec.setTitle(getTitle());
		spec.setValueTitles(getValueTitles());
		spec.setDescriptionArguments(getDescriptionArguments());
		return spec;
	}

	@Override
	public SettingSpecifier mappedWithMapper(MappableSpecifier.Mapper mapper) {
		BasicMultiValueSettingSpecifier spec = new BasicMultiValueSettingSpecifier(
				mapper.mapKey(getKey()), getDefaultValue());
		spec.setTitle(getTitle());
		spec.setValueTitles(getValueTitles());
		spec.setDescriptionArguments(getDescriptionArguments());
		return spec;
	}

	/**
	 * Create a drop-down menu setting for an enum property.
	 *
	 * <p>
	 * Each {@code valueTitles} key comes from the enum's constant name.
	 * </p>
	 *
	 * <p>
	 * Each {@code valueTitles} value comes from the message bundle, keyed by
	 * the enum's simple class name and constant name, for example
	 * {@code MyEnum.MY_VALUE}.
	 * </p>
	 *
	 * @param <E>
	 *        the enum type
	 * @param enumClass
	 *        the enum class that provides the available values
	 * @param key
	 *        the setting key
	 * @param defaultValue
	 *        the default value, or {@code null} to include an "empty" menu item
	 * @param msg
	 *        the message source to resolve value titles with
	 * @param locale
	 *        the locale to resolve value titles for
	 * @return the setting
	 * @since 1.2
	 */
	public static <E extends Enum<E>> BasicMultiValueSettingSpecifier enumSpec(Class<E> enumClass,
			String key, @Nullable E defaultValue, MessageSource msg, Locale locale) {
		final E[] values = enumClass.getEnumConstants();
		final BasicMultiValueSettingSpecifier spec = new BasicMultiValueSettingSpecifier(key,
				(defaultValue != null ? defaultValue.name() : ""));
		final Map<String, String> titles = new LinkedHashMap<>(values.length);
		if ( defaultValue == null ) {
			titles.put("", "");
		}
		for ( E e : values ) {
			titles.put(e.name(),
					msg.getMessage(enumClass.getSimpleName() + "." + e.name(), null, e.name(), locale));
		}
		spec.setValueTitles(titles);
		return spec;
	}

	/**
	 * Create a drop-down menu setting for an enum property using key values.
	 *
	 * <p>
	 * Each {@code valueTitles} key comes from the enum's
	 * {@link KeyedValue#getKey()}.
	 * </p>
	 *
	 * <p>
	 * Each {@code valueTitles} value comes from the message bundle, keyed by
	 * the enum's simple class name and constant name, for example
	 * {@code MyEnum.MY_VALUE}.
	 * </p>
	 *
	 * @param <E>
	 *        the enum type
	 * @param enumClass
	 *        the enum class that provides the available values
	 * @param key
	 *        the setting key
	 * @param defaultValue
	 *        the default value, or {@code null} to include an "empty" menu item
	 * @param msg
	 *        the message source to resolve value titles with
	 * @param locale
	 *        the locale to resolve value titles for
	 * @return the setting
	 * @since 1.2
	 */
	public static <E extends Enum<E> & KeyedValue> BasicMultiValueSettingSpecifier keyedEnumSpec(
			Class<E> enumClass, String key, @Nullable E defaultValue, MessageSource msg, Locale locale) {
		final E[] values = enumClass.getEnumConstants();
		final BasicMultiValueSettingSpecifier spec = new BasicMultiValueSettingSpecifier(key,
				(defaultValue != null ? defaultValue.getKey() : ""));
		final Map<String, String> titles = new LinkedHashMap<>(values.length);
		if ( defaultValue == null ) {
			titles.put("", "");
		}
		for ( E e : values ) {
			titles.put(e.getKey(),
					msg.getMessage(enumClass.getSimpleName() + "." + e.name(), null, e.name(), locale));
		}
		spec.setValueTitles(titles);
		return spec;
	}

}

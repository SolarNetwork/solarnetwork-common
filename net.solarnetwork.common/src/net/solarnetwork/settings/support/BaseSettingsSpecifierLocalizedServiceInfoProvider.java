/* ==================================================================
 * BaseSettingsSpecifierLocalizedServiceInfoProvider.java - 11/04/2018 5:06:39 PM
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

package net.solarnetwork.settings.support;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.context.MessageSource;
import net.solarnetwork.domain.LocalizedServiceInfo;
import net.solarnetwork.service.LocalizedServiceInfoProvider;
import net.solarnetwork.service.support.BaseLocalizedServiceInfoProvider;
import net.solarnetwork.settings.KeyedSettingSpecifier;
import net.solarnetwork.settings.SettingSpecifier;
import net.solarnetwork.settings.SettingSpecifierProvider;

/**
 * Convenient abstract class that is both a {@link SettingSpecifierProvider} and
 * a {@link LocalizedServiceInfoProvider}.
 *
 * @param <PK>
 *        the primary key type
 * @author matt
 * @version 2.1
 * @since 1.43
 */
public abstract class BaseSettingsSpecifierLocalizedServiceInfoProvider<PK extends Comparable<PK>>
		extends BaseLocalizedServiceInfoProvider<PK> implements SettingSpecifierProvider {

	/**
	 * Constructor.
	 *
	 * @param id
	 *        the identity of this provider
	 */
	public BaseSettingsSpecifierLocalizedServiceInfoProvider(PK id) {
		super(id);
	}

	@Override
	public String getSettingUid() {
		PK id = getId();
		return (id != null ? id.toString() : null);
	}

	@Override
	public LocalizedServiceInfo getLocalizedServiceInfo(Locale locale) {
		return new BasicConfigurableLocalizedServiceInfo(
				super.getLocalizedServiceInfo(locale != null ? locale : Locale.getDefault()),
				getSettingSpecifiers());
	}

	@Override
	protected Map<String, String> resolveInfoMessages(Locale locale) {
		locale = (locale != null ? locale : Locale.getDefault());
		List<SettingSpecifier> specs = getSettingSpecifiers();
		MessageSource ms = getMessageSource();
		String title = (ms != null ? ms.getMessage("title", null, null, locale) : null);
		String desc = (ms != null ? ms.getMessage("desc", null, null, locale) : null);
		if ( ms == null || ((specs == null || specs.isEmpty()) && (title == null || title.isEmpty())
				&& (desc == null || desc.isEmpty())) ) {
			return Collections.emptyMap();
		}
		Map<String, String> msgs = new LinkedHashMap<>(specs.size() * 2 + 2);
		if ( title != null ) {
			msgs.put("title", title);
		}
		if ( desc != null ) {
			msgs.put("desc", desc);
		}
		for ( SettingSpecifier spec : specs ) {
			populateInfoMessages(locale, spec, msgs, ms);
		}
		return msgs;
	}

	/**
	 * Populate the info messages for a single {@link SettingSpecifier}.
	 *
	 * <p>
	 * This implementation looks for message codes based on
	 * {@link KeyedSettingSpecifier#getKey()} values with {@code .key} and
	 * {@code .desc} appended, for "title" and "description" messages for that
	 * setting.
	 * </p>
	 *
	 * @param locale
	 *        the desired locale
	 * @param spec
	 *        the specifier
	 * @param msgs
	 *        the info map to store the resolved messages in
	 * @param ms
	 *        the message source to resolve messages with
	 */
	protected void populateInfoMessages(Locale locale, SettingSpecifier spec, Map<String, String> msgs,
			MessageSource ms) {
		if ( spec instanceof KeyedSettingSpecifier<?> ) {
			KeyedSettingSpecifier<?> ks = (KeyedSettingSpecifier<?>) spec;
			String key = ks.getKey();
			String code = key + ".key";
			String value = ms.getMessage(code, null, "", locale);
			msgs.put(code, value);

			code = key + ".desc";
			value = ms.getMessage(code, null, "", locale);
			msgs.put(code, value);
		}
	}

}

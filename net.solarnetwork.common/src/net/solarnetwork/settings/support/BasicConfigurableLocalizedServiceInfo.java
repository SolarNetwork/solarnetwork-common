/* ==================================================================
 * BasicConfigurableLocalizedServiceInfo.java - 13/04/2018 7:08:08 AM
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.solarnetwork.domain.BasicLocalizedServiceInfo;
import net.solarnetwork.domain.LocalizedServiceInfo;
import net.solarnetwork.settings.ConfigurableLocalizedServiceInfo;
import net.solarnetwork.settings.SettingSpecifier;

/**
 * Basic immutable implementation of {@link ConfigurableLocalizedServiceInfo}.
 * 
 * @author matt
 * @version 1.0
 * @since 1.43
 */
public class BasicConfigurableLocalizedServiceInfo extends BasicLocalizedServiceInfo
		implements ConfigurableLocalizedServiceInfo {

	private final List<SettingSpecifier> settings;

	/**
	 * Construct without any settings.
	 * 
	 * @param id
	 *        the unique service identifier
	 * @param locale
	 *        the locale
	 * @param name
	 *        the localized name
	 * @param description
	 *        the localized description
	 * @param infoMessages
	 *        the localized info messages
	 */
	public BasicConfigurableLocalizedServiceInfo(String id, Locale locale, String name,
			String description, Map<String, String> infoMessages) {
		this(id, locale, name, description, infoMessages, Collections.<SettingSpecifier> emptyList());
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *        the unique service identifier
	 * @param locale
	 *        the locale
	 * @param name
	 *        the localized name
	 * @param description
	 *        the localized description
	 * @param infoMessages
	 *        the localized info messages
	 * @param settings
	 *        the settings
	 */
	public BasicConfigurableLocalizedServiceInfo(String id, Locale locale, String name,
			String description, Map<String, String> infoMessages, List<SettingSpecifier> settings) {
		super(id, locale, name, description, infoMessages);
		this.settings = settings;
	}

	/**
	 * Copy constructor from another {@link LocalizedServiceInfo} instance.
	 * 
	 * @param info
	 *        the info to copy
	 * @param settings
	 *        the settings
	 */
	public BasicConfigurableLocalizedServiceInfo(LocalizedServiceInfo info,
			List<SettingSpecifier> settings) {
		this(info.getId(), Locale.forLanguageTag(info.getLocale()), info.getLocalizedName(),
				info.getLocalizedDescription(), info.getLocalizedInfoMessages(), settings);
	}

	@Override
	public List<SettingSpecifier> getSettingSpecifiers() {
		return settings;
	}

}

/* ==================================================================
 * BasicLocalizedServiceInfo.java - 11/04/2018 4:22:12 PM
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

package net.solarnetwork.domain;

import java.util.Locale;
import java.util.Map;

/**
 * Basic immutable implementation of {@link LocalizedServiceInfo}.
 * 
 * @author matt
 * @version 1.0
 * @since 1.43
 */
public class BasicLocalizedServiceInfo extends BasicIdentity<String> implements LocalizedServiceInfo {

	private final String locale;
	private final String name;
	private final String description;
	private final Map<String, String> infoMessages;

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
	 */
	public BasicLocalizedServiceInfo(String id, Locale locale, String name, String description,
			Map<String, String> infoMessages) {
		super(id);
		this.locale = (locale != null ? locale : Locale.getDefault()).toLanguageTag();
		this.name = name;
		this.description = description;
		this.infoMessages = infoMessages;
	}

	@Override
	public String getLocale() {
		return locale;
	}

	@Override
	public String getLocalizedName() {
		return name;
	}

	@Override
	public String getLocalizedDescription() {
		return description;
	}

	@Override
	public Map<String, String> getLocalizedInfoMessages() {
		return infoMessages;
	}

}

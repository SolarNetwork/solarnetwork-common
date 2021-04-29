/* ==================================================================
 * BasicIdentifiable.java - 26/11/2019 6:39:24 am
 * 
 * Copyright 2019 SolarNetwork.net Dev Team
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

package net.solarnetwork.support;

import java.util.ArrayList;
import java.util.List;
import org.springframework.context.MessageSource;
import net.solarnetwork.domain.Identifiable;
import net.solarnetwork.settings.SettingSpecifier;
import net.solarnetwork.settings.support.BasicTextFieldSettingSpecifier;

/**
 * Basic implementation of {@link Identifiable}.
 * 
 * @author matt
 * @version 1.1
 * @since 1.56
 */
public class BasicIdentifiable implements Identifiable {

	private String uid;
	private String groupUid;
	private String displayName;
	private MessageSource messageSource;

	/**
	 * Get settings for the configurable properties of
	 * {@link BasicIdentifiable}.
	 * 
	 * <p>
	 * Empty strings are used for the {@code prefix}, default {@code uid} and
	 * default {@code groupUid} setting values.
	 * </p>
	 * 
	 * @return the settings
	 * @see #basicIdentifiableSettings(String, String, String)
	 * @since 1.1
	 */
	public static List<SettingSpecifier> basicIdentifiableSettings() {
		return basicIdentifiableSettings("", "", "");
	}

	/**
	 * Get settings for the configurable properties of
	 * {@link BasicIdentifiable}.
	 * 
	 * <p>
	 * Empty strings are used for the default {@code uid} and {@code groupUid}
	 * setting values.
	 * </p>
	 * 
	 * @param prefix
	 *        an optional prefix to include in all setting keys
	 * @return the settings
	 * @see #basicIdentifiableSettings(String, String, String)
	 * @since 1.1
	 */
	public static List<SettingSpecifier> basicIdentifiableSettings(String prefix) {
		return basicIdentifiableSettings(prefix, "", "");
	}

	/**
	 * Get settings for the configurable properties of
	 * {@link BasicIdentifiable}.
	 * 
	 * @param prefix
	 *        an optional prefix to include in all setting keys
	 * @param defaultUid
	 *        the default {@code uid} value to use
	 * @param defaultGroupUid
	 *        the default {@code groupUid} value to use
	 * @return the settings
	 * @since 1.1
	 */
	public static List<SettingSpecifier> basicIdentifiableSettings(String prefix, String defaultUid,
			String defaultGroupUid) {
		if ( prefix == null ) {
			prefix = "";
		}
		List<SettingSpecifier> results = new ArrayList<>(8);
		results.add(new BasicTextFieldSettingSpecifier(prefix + "uid", defaultUid));
		results.add(new BasicTextFieldSettingSpecifier(prefix + "groupUid", defaultGroupUid));
		return results;
	}

	@Override
	public String getUid() {
		return uid;
	}

	/**
	 * Set the UID.
	 * 
	 * @param uid
	 *        the UID to set
	 */
	public void setUid(String uid) {
		this.uid = uid;
	}

	@Override
	public String getGroupUid() {
		return groupUid;
	}

	/**
	 * Set the group UID.
	 * 
	 * @param groupUid
	 *        the group UID to set
	 */
	public void setGroupUid(String groupUid) {
		this.groupUid = groupUid;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Set the display name.
	 * 
	 * @param displayName
	 *        the displayName to set
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * Get a message source, to use for localizing this service with.
	 * 
	 * @return a message source
	 */
	public MessageSource getMessageSource() {
		return messageSource;
	}

	/**
	 * Set a message source, to use for localizing this service with.
	 * 
	 * @param messageSource
	 *        the message source to use
	 */
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

}

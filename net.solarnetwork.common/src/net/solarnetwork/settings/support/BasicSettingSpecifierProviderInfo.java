/* ==================================================================
 * BasicSettingSpecifierProviderInfo.java - 5/07/2024 4:01:33 pm
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

package net.solarnetwork.settings.support;

import static net.solarnetwork.util.ObjectUtils.requireNonNullArgument;
import net.solarnetwork.settings.SettingSpecifierProviderInfo;

/**
 * Basic implementation of {@link SettingSpecifierProviderInfo}.
 *
 * @author matt
 * @version 1.0
 * @since 3.15
 */
public class BasicSettingSpecifierProviderInfo implements SettingSpecifierProviderInfo {

	private final String settingUid;
	private final String displayName;
	private final String uid;
	private final String groupUid;

	/**
	 * Constructor.
	 *
	 * @param settingUid
	 *        the setting UID
	 * @param displayName
	 *        the display name
	 * @param uid
	 *        the identifiable UID
	 * @param groupUid
	 *        the identifiable group UID
	 * @throws IllegalAccessException
	 *         if any argument except {@code groupUid} is {@literal null}
	 */
	public BasicSettingSpecifierProviderInfo(String settingUid, String displayName, String uid,
			String groupUid) {
		super();
		this.settingUid = requireNonNullArgument(settingUid, "settingUid");
		this.displayName = requireNonNullArgument(displayName, "displayName");
		this.uid = requireNonNullArgument(uid, "uid");
		this.groupUid = groupUid;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BasicSettingSpecifierProviderInfo{");
		if ( settingUid != null ) {
			builder.append("settingUid=");
			builder.append(settingUid);
			builder.append(", ");
		}
		if ( displayName != null ) {
			builder.append("displayName=");
			builder.append(displayName);
			builder.append(", ");
		}
		if ( uid != null ) {
			builder.append("uid=");
			builder.append(uid);
			builder.append(", ");
		}
		if ( groupUid != null ) {
			builder.append("groupUid=");
			builder.append(groupUid);
		}
		builder.append("}");
		return builder.toString();
	}

	@Override
	public String getSettingUid() {
		return settingUid;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

	@Override
	public final String getUid() {
		return uid;
	}

	@Override
	public final String getGroupUid() {
		return groupUid;
	}

}

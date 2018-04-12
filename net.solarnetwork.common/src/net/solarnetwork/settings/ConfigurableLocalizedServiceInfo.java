/* ==================================================================
 * ConfigurableLocalizedServiceInfo.java - 13/04/2018 7:05:50 AM
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

package net.solarnetwork.settings;

import java.util.List;
import net.solarnetwork.domain.LocalizedServiceInfo;

/**
 * Extension of {@link LocalizedServiceInfo} that adds configurable setting
 * information.
 * 
 * @author matt
 * @version 1.0
 * @since 1.43
 */
public interface ConfigurableLocalizedServiceInfo extends LocalizedServiceInfo {

	/**
	 * Get a list of {@link SettingSpecifier} instances.
	 * 
	 * @return list of {@link SettingSpecifier}
	 */
	List<SettingSpecifier> getSettingSpecifiers();

}

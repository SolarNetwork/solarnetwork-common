/* ==================================================================
 * SettingSpecifierProviderInfo.java - 5/07/2024 3:59:24 pm
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

package net.solarnetwork.settings;

import net.solarnetwork.service.Identifiable;

/**
 * Basic information about a {@link SettingSpecifierProvider}.
 *
 * @author matt
 * @version 1.0
 * @since 3.15
 */
public interface SettingSpecifierProviderInfo extends Identifiable {

	/**
	 * Get a unique, application-wide setting ID.
	 *
	 * <p>
	 * This ID must be unique across all setting providers registered within the
	 * system.
	 * </p>
	 *
	 * @return unique ID
	 */
	String getSettingUid();

}

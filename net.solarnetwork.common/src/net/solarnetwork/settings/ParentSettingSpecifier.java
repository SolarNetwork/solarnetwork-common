/* ==================================================================
 * ParentSettingSpecifier.java - Mar 12, 2012 9:23:25 AM
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

package net.solarnetwork.settings;

import java.util.List;

/**
 * A placeholder setting for a list of "child" settings.
 * 
 * @author matt
 * @version 1.0
 */
public interface ParentSettingSpecifier extends SettingSpecifier {

	/**
	 * Get the settings in this parent.
	 * 
	 * @return the list of child settings
	 */
	List<SettingSpecifier> getChildSettings();

}

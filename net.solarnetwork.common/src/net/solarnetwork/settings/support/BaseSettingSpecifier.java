/* ==================================================================
 * BaseSettingSpecifier.java - Mar 12, 2012 9:53:59 AM
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

import net.solarnetwork.settings.SettingSpecifier;

/**
 * Base implementation of {@link SettingSpecifier}.
 * 
 * @author matt
 * @version 1.1
 */
public abstract class BaseSettingSpecifier implements SettingSpecifier {

	private String title;

	/**
	 * Constructor.
	 */
	public BaseSettingSpecifier() {
		super();
	}

	@Override
	public String getTitle() {
		return this.title;
	}

	/**
	 * Set the title.
	 * 
	 * @param title
	 *        the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String getType() {
		Class<? extends BaseSettingSpecifier> clazz = getClass();
		Class<?>[] interfaces = clazz.getInterfaces();
		if ( interfaces != null && interfaces.length > 0 ) {
			return interfaces[0].getName();
		}
		return clazz.getName();
	}

}

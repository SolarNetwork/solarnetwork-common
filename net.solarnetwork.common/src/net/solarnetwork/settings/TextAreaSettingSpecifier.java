/* ==================================================================
 * TextAreaSettingSpecifier.java - 16/09/2019 4:48:36 pm
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

package net.solarnetwork.settings;

/**
 * A read-write large string setting.
 * 
 * @author matt
 * @version 1.1
 * @since 1.70
 */
public interface TextAreaSettingSpecifier extends KeyedSettingSpecifier<String> {

	/**
	 * Flag indicating the text area content should be handled directly like a
	 * text field.
	 * 
	 * @return {@literal true} to treat the text area content directly like a
	 *         text field, {@literal false} to treat like an external resource
	 *         to upload indirectly
	 * @since 1.1
	 */
	default boolean isDirect() {
		return false;
	}

}

/* ==================================================================
 * LocalizedServiceInfoProvider.java - 11/04/2018 4:15:54 PM
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

package net.solarnetwork.support;

import java.util.Locale;
import net.solarnetwork.domain.LocalizedServiceInfo;

/**
 * API for a service that can provide locailzed information about itself.
 * 
 * @author matt
 * @version 1.0
 * @since 1.43
 */
public interface LocalizedServiceInfoProvider {

	/**
	 * Get localized information for a specific locale.
	 * 
	 * @param locale
	 *        the locale to get localized information for
	 * @return the localized info, never {@literal null}
	 */
	LocalizedServiceInfo getLocalizedServiceInfo(Locale locale);

}

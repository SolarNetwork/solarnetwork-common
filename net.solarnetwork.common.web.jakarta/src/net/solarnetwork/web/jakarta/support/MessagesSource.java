/* ==================================================================
 * MessagesSource.java - Jun 18, 2011 12:11:35 PM
 * 
 * Copyright 2007-2011 SolarNetwork.net Dev Team
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

package net.solarnetwork.web.jakarta.support;

import java.util.Enumeration;
import java.util.Locale;
import org.springframework.context.MessageSource;

/**
 * Extension of MessageSource to allow for getting all messages.
 * 
 * <p>
 * Adapted from {@code magoffin.matt.xweb.util.MessagesSource}.
 * </p>
 * 
 * @author matt
 * @version 1.0
 */
public interface MessagesSource extends MessageSource {

	/**
	 * Get an enumeration of keys.
	 * 
	 * @param locale
	 *        the desired locale
	 * @return enumeration of message keys
	 */
	public Enumeration<String> getKeys(Locale locale);

	/**
	 * Register an additional message resource at runtime.
	 * 
	 * @param resource
	 *        the resource path to register
	 */
	public void registerMessageResource(String resource);

}

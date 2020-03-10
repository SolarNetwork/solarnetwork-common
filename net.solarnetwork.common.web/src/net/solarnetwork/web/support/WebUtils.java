/* ==================================================================
 * WebUtils.java - May 31, 2010 3:33:01 PM
 * 
 * Copyright 2007-2010 SolarNetwork.net Dev Team
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

package net.solarnetwork.web.support;

import javax.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;

/**
 * Common utility helper methods for web processing.
 * 
 * @author matt
 * @version 1.0
 */
public final class WebUtils {

	/**
	 * Resolve a ModelAndView with an empty model and a view name determined by
	 * the URL "suffix".
	 * 
	 * <p>
	 * The resolved view name will be derived from the value of the URL
	 * "suffix", that is, everything after the last period in the URL. This uses
	 * {@link StringUtils#getFilenameExtension(String)} on the request URI to
	 * accomplish this. For example a URL like {@code /myController.json} would
	 * resolve to a view named {@code json}. This can be handy when you want to
	 * return different data formats for the same business logic, such as XML or
	 * JSON.
	 * </p>
	 * 
	 * <p>
	 * The {@code viewName} parameter can be used to override the view mapping
	 * logic and instead simply return a {@link ModelAndView} object for the
	 * given name. For normal controllers with a configurable view name
	 * property, that property can be passed in here, but usually the value will
	 * not be configured.
	 * </p>
	 * 
	 * @param request
	 *        the HTTP request
	 * @param viewName
	 *        the custom view name
	 * @return a view name (never <em>null</em>)
	 */
	public static String resolveViewFromUrlExtension(HttpServletRequest request, String viewName) {
		// resolve the final view name based on the URL suffix, i.e. "*.xml" -> "xml"
		String resolvedViewName = viewName;
		if ( resolvedViewName == null ) {
			resolvedViewName = StringUtils.getFilenameExtension(request.getRequestURI());
		}
		return resolvedViewName;
	}

}

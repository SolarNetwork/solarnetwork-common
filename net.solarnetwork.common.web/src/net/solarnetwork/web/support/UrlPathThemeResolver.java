/* ==================================================================
 * UrlPathThemeResolver.java - Apr 28, 2012 8:38:38 PM
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

package net.solarnetwork.web.support;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestWrapper;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.ThemeResolver;
import org.springframework.web.servlet.theme.CookieThemeResolver;

/**
 * {@link ThemeResolver} that resolves a theme name from a URL path variable.
 * 
 * <p>This class extends {@link CookieThemeResolver} and as such uses cookies
 * to persist the theme with the client browser. The theme can be changed
 * (or initially set) by passing a URL path variable with the HTTP request,
 * for example {@code /path/to/something.html;theme=mytheme}.</p>
 * 
 * <p>The configurable properties of this class are:</p>
 * 
 * <dl class="class-properties">
 *   <dt>themePathVariable</dt>
 *   <dd>The name of the URL path variable to look for the current theme in.
 *   Defaults to {@link #DEFAULT_THEME_PATH_VARIABLE}.</dd>
 * </dl>
 * 
 * @author matt
 * @version 1.0
 */
public class UrlPathThemeResolver extends CookieThemeResolver {
	
	/** Default value for the {@link themePathVariable} property. */
	public static final String DEFAULT_THEME_PATH_VARIABLE = "theme";

	private String themePathVariable;
	private Pattern urlPattern;
	
	/**
	 * Default constructor.
	 */
	public UrlPathThemeResolver() {
		super();
		setThemePathVariable(DEFAULT_THEME_PATH_VARIABLE);
	}
	
	private HttpServletRequest originalRequest(HttpServletRequest request) {
		if ( request instanceof ServletRequestWrapper ) {
			ServletRequest wrapped = ((ServletRequestWrapper)request).getRequest();
			if ( wrapped instanceof HttpServletRequest ) {
				return originalRequest((HttpServletRequest)wrapped);
			}
		}
		return request;
	}
	
	@Override
	public String resolveThemeName(HttpServletRequest request) {
		String themeName = super.resolveThemeName(request);
		
		// look on the original request for the theme path, in case we've been 
		// forwarded to another request, e.g. /WEB-INF/jsp/index.jsp from
		// original request /myapp/;theme=foo
		String path = originalRequest(request).getRequestURI();
		Matcher m = urlPattern.matcher(path);
		if ( m.find() ) {
			themeName = m.group(1).toLowerCase();
		}
		return themeName;
	}

	public String getThemePathVariable() {
		return themePathVariable;
	}

	public void setThemePathVariable(String themePathVariable) {
		this.themePathVariable = themePathVariable;
		urlPattern = Pattern.compile("\\b" + themePathVariable + "=(\\w+)", 
				Pattern.CASE_INSENSITIVE);
	}

}

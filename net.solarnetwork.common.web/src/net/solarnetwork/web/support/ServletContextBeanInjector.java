/* ==================================================================
 * ServletContextBeanInjector.java - Jun 16, 2011 7:56:14 PM
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

package net.solarnetwork.web.support;

import java.util.Map;
import javax.servlet.ServletContext;
import org.springframework.web.context.ServletContextAware;

/**
 * Helper class to inject beans into a ServletContext.
 * 
 * @author matt
 * @version 1.0
 */
public class ServletContextBeanInjector implements ServletContextAware {

	private Map<String, Object> beans;
	private ServletContext servletContext;
	private boolean initialized = false;

	@Override
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
		injectBeans();
	}

	private void injectBeans() {
		if ( this.initialized || this.beans == null || this.servletContext == null ) {
			return;
		}
		for ( Map.Entry<String, Object> me : beans.entrySet() ) {
			this.servletContext.setAttribute(me.getKey(), me.getValue());
		}
		initialized = true;
	}

	/**
	 * Initialize the ServletContext.
	 */
	public void init() {
		if ( beans == null || servletContext == null ) {
			return;
		}
		injectBeans();
	}

	/**
	 * Get the beans.
	 * 
	 * @return the beans
	 */
	public Map<String, Object> getBeans() {
		return beans;
	}

	/**
	 * Set the beans.
	 * 
	 * @param beans
	 *        the beans to set
	 */
	public void setBeans(Map<String, Object> beans) {
		this.beans = beans;
	}

}

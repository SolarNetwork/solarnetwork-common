/* ==================================================================
 * NonValidatingServerOsgiBundleXmlWebApplicationContext.java - Jan 23, 2015 6:39:52 PM
 * 
 * Copyright 2007-2015 SolarNetwork.net Dev Team
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

package net.solarnetwork.web.gemini;

import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;

/**
 * Replacement of {@link ServerOsgiBundleXmlWebApplicationContext} that disables
 * XML validation on Spring configuration files.
 * 
 * <p>
 * This can greatly increase application startup time on low-powered devices, as
 * well as reduce network access.
 * </p>
 * 
 * @author matt
 * @version 1.2
 */
public class NonValidatingServerOsgiBundleXmlWebApplicationContext
		extends ServerOsgiBundleXmlWebApplicationContext {

	/**
	 * Default constructor.
	 */
	public NonValidatingServerOsgiBundleXmlWebApplicationContext() {
		super();
	}

	/**
	 * Construct with configuration locations.
	 * 
	 * @param configLocations
	 *        The locations.
	 */
	public NonValidatingServerOsgiBundleXmlWebApplicationContext(String[] configLocations) {
		super(configLocations);
	}

	/**
	 * Construct with a parent context.
	 * 
	 * @param parent
	 *        The parent context.
	 */
	public NonValidatingServerOsgiBundleXmlWebApplicationContext(ApplicationContext parent) {
		super(parent);
	}

	/**
	 * Construct with configuration locations and a parent context.
	 * 
	 * @param configLocations
	 *        The configuration locations.
	 * @param parent
	 *        The parent context.
	 */
	public NonValidatingServerOsgiBundleXmlWebApplicationContext(String[] configLocations,
			ApplicationContext parent) {
		super(configLocations, parent);
	}

	@Override
	protected void initBeanDefinitionReader(XmlBeanDefinitionReader beanDefinitionReader) {
		super.initBeanDefinitionReader(beanDefinitionReader);
		beanDefinitionReader.setValidating(false);
	}

}

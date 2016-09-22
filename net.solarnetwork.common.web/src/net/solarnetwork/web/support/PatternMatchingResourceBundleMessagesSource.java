/* ==================================================================
 * PatternMatchingResourceBundleMessagesSource.java - 22/09/2016 1:46:47 PM
 * 
 * Copyright 2007-2016 SolarNetwork.net Dev Team
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

import java.io.IOException;
import java.security.cert.Extension;
import java.util.Properties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * {@link Extension of {@link ReloadableResourceBundleMessagesSource} that
 * supports a pattern resolver for matching any number of resources.
 * 
 * @author matt
 * @version 1.0
 */
public class PatternMatchingResourceBundleMessagesSource extends ReloadableResourceBundleMessagesSource {

	private final static String PROPERTIES_EXTENSION = ".properties";

	private ResourcePatternResolver resourceResolver = null;

	@Override
	protected PropertiesHolder refreshProperties(String filename, PropertiesHolder propHolder) {
		if ( resourceResolver != null ) {
			return refreshMatchingProperties(filename, propHolder);
		}
		return super.refreshProperties(filename, propHolder);
	}

	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		super.setResourceLoader(resourceLoader);
		if ( resourceResolver == null ) {
			if ( resourceLoader instanceof ResourcePatternResolver ) {
				resourceResolver = (ResourcePatternResolver) resourceLoader;
			} else {
				resourceResolver = new PathMatchingResourcePatternResolver(resourceLoader);
			}
		}
	}

	private PropertiesHolder refreshMatchingProperties(String filename, PropertiesHolder propHolder) {
		Properties properties = new Properties();
		long lastModified = -1;
		String name = filename + PROPERTIES_EXTENSION;
		try {
			Resource[] resources = resourceResolver.getResources(name);
			for ( Resource resource : resources ) {
				String sourcePath = resource.getURI().toString();
				if ( sourcePath.endsWith(PROPERTIES_EXTENSION) ) {
					sourcePath = sourcePath.substring(0,
							sourcePath.length() - PROPERTIES_EXTENSION.length());
				}
				PropertiesHolder holder = super.refreshProperties(sourcePath, propHolder);
				properties.putAll(holder.getProperties());
				if ( lastModified < resource.lastModified() )
					lastModified = resource.lastModified();
			}
		} catch ( IOException e ) {
			// ignore this
		}
		return new PropertiesHolder(properties, lastModified);
	}

	/**
	 * Set the resource resolver to use to.
	 * 
	 * @param resourceResolver
	 *        The resolver to use.
	 */
	public void setResourceResolver(ResourcePatternResolver resourceResolver) {
		this.resourceResolver = resourceResolver;
	}

}

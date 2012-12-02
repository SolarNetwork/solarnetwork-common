/* ==================================================================
 * DynamicServiceTracker.java - Mar 24, 2012 8:32:05 PM
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
 * $Id$
 * ==================================================================
 */

package net.solarnetwork.util;

import java.util.Map;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;

/**
 * Utility for dynamically obtaining an OSGi service based on comparing bean
 * properties of a filtered subset of available services for matching values.
 * 
 * <p>
 * An example scenario for this class would be in the case of a factory service
 * that publishes differently configured service instances. This class can be
 * used to pick a single service published by the factory, based on properties
 * of that instance. For example, a factory that publishes a serial port service
 * might expose the serial port identifier as a bean property, which could be
 * used to match on the desired port.
 * </p>
 * 
 * <p>
 * The configurable properties of this class are:
 * </p>
 * 
 * <dl class="class-properties">
 * <dt>bundleContext</dt>
 * <dd>The OSGi {@link BundleContext} to use.</dd>
 * 
 * <dt>serviceClassName</dt>
 * <dd>The OSGi service class name to look for, or <em>null</em> for all
 * services.</dd>
 * 
 * <dt>serviceFilter</dt>
 * <dd>An OSGi service filter to match services to, or <em>null</em> for no
 * filter.</dd>
 * 
 * <dt>propertyFilters</dt>
 * <dd>A map of bean property names and associated values to match against all
 * found OSGi services. The first service to match will be returned in
 * {@link #service()}. This can be <em>null</em>, in which case the first
 * service found matching {@code serviceClassName} and {@code serviceFilter}
 * will be returned.</dd>
 * </dl>
 * 
 * @param <T>
 *        the tracked service type
 * @author matt
 * @version $Revision$
 */
public class DynamicServiceTracker<T> implements OptionalService<T> {

	private BundleContext bundleContext;

	private String serviceClassName;
	private String serviceFilter;
	private Map<String, Object> propertyFilters;

	private final Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * Get the tracked service, or <em>null</em> if no service currently
	 * available.
	 * 
	 * @return the service, or <em>null</em> if not available
	 */
	@Override
	@SuppressWarnings("unchecked")
	public T service() {
		ServiceReference[] refs;
		try {
			refs = bundleContext.getServiceReferences(serviceClassName, serviceFilter);
		} catch ( InvalidSyntaxException e ) {
			log.error("Error in service filter {}: {}", serviceFilter, e);
			return null;
		}
		log.debug("Found {} possible services of type {} matching filter {}", new Object[] {
				(refs == null ? 0 : refs.length), serviceClassName, serviceFilter });
		if ( refs == null ) {
			return null;
		}
		for ( ServiceReference ref : refs ) {
			Object service = bundleContext.getService(ref);
			if ( propertyFilters == null || propertyFilters.size() < 1 ) {
				log.debug("No property filter configured, returning first {} service", serviceClassName);
				return (T) service;
			}
			log.trace("Examining service {} for property match {}", service, propertyFilters);
			PropertyAccessor accessor = PropertyAccessorFactory.forBeanPropertyAccess(service);
			boolean match = true;
			for ( Map.Entry<String, Object> me : propertyFilters.entrySet() ) {
				if ( accessor.isReadableProperty(me.getKey()) ) {
					if ( !me.getValue().equals(accessor.getPropertyValue(me.getKey())) ) {
						match = false;
						break;
					}
				} else {
					match = false;
					break;
				}
			}
			if ( match ) {
				log.debug("Found {} service matching properties {}: {}", new Object[] {
						serviceClassName, propertyFilters, service });
				return (T) service;
			}
		}
		return null;
	}

	public BundleContext getBundleContext() {
		return bundleContext;
	}

	public void setBundleContext(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}

	public String getServiceClassName() {
		return serviceClassName;
	}

	public void setServiceClassName(String serviceClassName) {
		this.serviceClassName = serviceClassName;
	}

	public String getServiceFilter() {
		return serviceFilter;
	}

	public void setServiceFilter(String serviceFilter) {
		this.serviceFilter = serviceFilter;
	}

	public Map<String, Object> getPropertyFilters() {
		return propertyFilters;
	}

	public void setPropertyFilters(Map<String, Object> propertyFilters) {
		this.propertyFilters = propertyFilters;
	}

}

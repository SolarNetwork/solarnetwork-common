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
 */

package net.solarnetwork.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
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
 * 
 * <dt>ignoreEmptyPropertyFilterValues</dt>
 * <dd>If <em>true</em>, then ignore property filter values that are
 * <em>null</em> or, if strings, have no length for purposes of filtering
 * services. If <em>false</em> then the property filters must match even if
 * empty, that is a <em>null</em> filter value will only match services whose
 * corresponding property is also <em>null</em>. Defaults to <em>true</em>.</dd>
 * 
 * <dt>fallbackService</dt>
 * <dd>If no matching service is available and this property is configured, then
 * this service will be returned as a fallback.</dd>
 * </dl>
 * 
 * @param <T>
 *        the tracked service type
 * @author matt
 * @version 1.1
 */
public class DynamicServiceTracker<T> implements OptionalService<T>, OptionalServiceCollection<T>,
		FilterableService {

	private BundleContext bundleContext;

	private String serviceClassName;
	private String serviceFilter;
	private Map<String, Object> propertyFilters;
	private T fallbackService;
	private boolean ignoreEmptyPropertyFilterValues = true;

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
		ServiceReference<?>[] refs;
		try {
			refs = bundleContext.getServiceReferences(serviceClassName, serviceFilter);
		} catch ( InvalidSyntaxException e ) {
			log.error("Error in service filter {}: {}", serviceFilter, e);
			return null;
		}
		log.debug("Found {} possible services of type {} matching filter {}", new Object[] {
				(refs == null ? 0 : refs.length), serviceClassName, serviceFilter });
		if ( refs != null ) {
			for ( ServiceReference<?> ref : refs ) {
				Object service = bundleContext.getService(ref);
				final boolean match = serviceMatchesFilters(service);
				if ( match ) {
					log.debug("Found {} service matching properties {}: {}", new Object[] {
							serviceClassName, propertyFilters, service });
					return (T) service;
				}
			}
		}
		if ( fallbackService != null ) {
			log.debug("Using fallback {} service {}, no matching service found matching properties {}",
					serviceClassName, fallbackService.getClass().getName(), propertyFilters);
		}
		return fallbackService;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterable<T> services() {
		ServiceReference<?>[] refs;
		try {
			refs = bundleContext.getServiceReferences(serviceClassName, serviceFilter);
		} catch ( InvalidSyntaxException e ) {
			log.error("Error in service filter {}: {}", serviceFilter, e);
			return Collections.emptyList();
		}
		log.debug("Found {} possible services of type {} matching filter {}", new Object[] {
				(refs == null ? 0 : refs.length), serviceClassName, serviceFilter });
		if ( refs == null ) {
			return Collections.emptyList();
		}
		List<T> results = new ArrayList<T>(refs.length);
		for ( ServiceReference<?> ref : refs ) {
			Object service = bundleContext.getService(ref);
			final boolean match = serviceMatchesFilters(service);
			if ( match ) {
				log.debug("Found {} service matching properties {}: {}", new Object[] {
						serviceClassName, propertyFilters, service });
				results.add((T) service);
			}
		}
		if ( results.size() == 0 && fallbackService != null ) {
			log.debug("Using fallback {} service {}, no matching service found matching properties {}",
					serviceClassName, fallbackService.getClass().getName(), propertyFilters);
			results.add(fallbackService);
		}
		return results;
	}

	private boolean serviceMatchesFilters(Object service) {
		if ( service == null ) {
			return false;
		}
		if ( propertyFilters == null || propertyFilters.size() < 1 ) {
			log.debug("No property filter configured, {} service matches", serviceClassName);
			return true;
		}
		log.trace("Examining service {} for property match {}", service, propertyFilters);
		PropertyAccessor accessor = PropertyAccessorFactory.forBeanPropertyAccess(service);
		for ( Map.Entry<String, Object> me : propertyFilters.entrySet() ) {
			if ( accessor.isReadableProperty(me.getKey()) ) {
				Object requiredValue = me.getValue();
				if ( ignoreEmptyPropertyFilterValues
						&& (requiredValue == null || ((requiredValue instanceof String) && ((String) requiredValue)
								.length() == 0)) ) {
					// ignore empty filter values, so this is a matching property... skip to the next filter
					continue;
				}
				Object serviceValue = accessor.getPropertyValue(me.getKey());
				if ( requiredValue == null ) {
					if ( serviceValue == null ) {
						continue;
					}
					return false;
				}
				if ( serviceValue instanceof Collection<?> ) {
					// for collections, we test for containment
					Collection<?> collection = (Collection<?>) serviceValue;
					if ( !collection.contains(requiredValue) ) {
						return false;
					}
				} else if ( !requiredValue.equals(serviceValue) ) {
					return false;
				}
			} else {
				return false;
			}
		}
		return true;
	}

	@Override
	public void setPropertyFilter(String key, Object value) {
		Map<String, Object> filters = propertyFilters;
		if ( filters == null ) {
			filters = new LinkedHashMap<String, Object>(8);
			propertyFilters = filters;
		}
		filters.put(key, value);
	}

	@Override
	public Object removePropertyFilter(String key) {
		Object result = null;
		Map<String, Object> filters = propertyFilters;
		if ( filters != null ) {
			result = filters.remove(key);
		}
		return result;
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

	@Override
	public Map<String, Object> getPropertyFilters() {
		return propertyFilters;
	}

	public void setPropertyFilters(Map<String, Object> propertyFilters) {
		this.propertyFilters = propertyFilters;
	}

	public T getFallbackService() {
		return fallbackService;
	}

	public void setFallbackService(T fallbackService) {
		this.fallbackService = fallbackService;
	}

	public boolean isIgnoreEmptyPropertyFilterValues() {
		return ignoreEmptyPropertyFilterValues;
	}

	public void setIgnoreEmptyPropertyFilterValues(boolean ignoreEmptyPropertyFilterValues) {
		this.ignoreEmptyPropertyFilterValues = ignoreEmptyPropertyFilterValues;
	}

}

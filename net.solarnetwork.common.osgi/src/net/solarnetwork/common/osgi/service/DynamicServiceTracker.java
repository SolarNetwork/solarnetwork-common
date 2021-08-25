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

package net.solarnetwork.common.osgi.service;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
import net.solarnetwork.service.FilterableService;
import net.solarnetwork.service.OptionalService;
import net.solarnetwork.service.OptionalServiceCollection;

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
 * The {@code sticky} property allows for caching of a resolved service so it
 * need not be resolved every time {@link #service()} is invoked. The resolved
 * service is only weakly referenced so it may still be garbage collected, after
 * which the service will be re-resolved when {@link #service()} is later
 * invoked. Using "sticky" mode is suitable when the runtime service is not
 * expected to change much, or at all, during the life of this tracker, and the
 * speed of resolving the service it critical.
 * </p>
 * 
 * <p>
 * Conceptually this is very similar to what OSGi Blueprint service references
 * provide, just with more features such as filtering on service JavaBean
 * properties.
 * </p>
 * 
 * @param <T>
 *        the tracked service type
 * @author matt
 * @version 1.0
 */
public class DynamicServiceTracker<T> implements OptionalService<T>, OptionalServiceCollection<T>,
		FilterableService, OptionalService.OptionalFilterableService<T>,
		OptionalServiceCollection.OptionalFilterableServiceCollection<T> {

	private static final Comparator<ServiceReference<?>> RANK_COMPARATOR = new ServiceReferenceRankComparator();

	private BundleContext bundleContext;

	private String serviceClassName;
	private String serviceFilter;
	private Map<String, Object> propertyFilters;
	private T fallbackService;
	private boolean ignoreEmptyPropertyFilterValues = true;
	private boolean sticky = false;
	private WeakReference<T> stickyService;

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Override
	@SuppressWarnings("unchecked")
	public T service() {
		final boolean sticky = isSticky();
		if ( sticky ) {
			synchronized ( this ) {
				T service = (stickyService != null ? stickyService.get() : null);
				if ( service != null ) {
					return service;
				}
			}
		}
		ServiceReference<?>[] refs;
		try {
			refs = bundleContext.getServiceReferences(serviceClassName, serviceFilter);
		} catch ( InvalidSyntaxException e ) {
			log.error("Error in service filter {}: {}", serviceFilter, e);
			return null;
		}
		log.debug("Found {} possible services of type {} matching filter {}",
				new Object[] { (refs == null ? 0 : refs.length), serviceClassName, serviceFilter });
		if ( refs != null ) {
			if ( refs.length > 1 ) {
				// make sure sorted highest rank first
				Arrays.sort(refs, RANK_COMPARATOR);
			}
			for ( ServiceReference<?> ref : refs ) {
				Object service = bundleContext.getService(ref);
				final boolean match = serviceMatchesFilters(service);
				if ( match ) {
					log.debug("Found {} service matching properties {}: {}",
							new Object[] { serviceClassName, propertyFilters, service });
					if ( sticky ) {
						synchronized ( this ) {
							stickyService = new WeakReference<>((T) service);
						}
					}
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
		log.debug("Found {} possible services of type {} matching filter {}",
				new Object[] { (refs == null ? 0 : refs.length), serviceClassName, serviceFilter });
		if ( refs == null ) {
			return Collections.emptyList();
		}
		if ( refs.length > 1 ) {
			// make sure sorted highest rank first
			Arrays.sort(refs, RANK_COMPARATOR);
		}
		List<T> results = new ArrayList<T>(refs.length);
		for ( ServiceReference<?> ref : refs ) {
			Object service = bundleContext.getService(ref);
			final boolean match = serviceMatchesFilters(service);
			if ( match ) {
				log.debug("Found {} service matching properties {}: {}",
						new Object[] { serviceClassName, propertyFilters, service });
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
						&& (requiredValue == null || ((requiredValue instanceof String)
								&& ((String) requiredValue).length() == 0)) ) {
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
		stickyService = null;
	}

	@Override
	public Object removePropertyFilter(String key) {
		Object result = null;
		Map<String, Object> filters = propertyFilters;
		if ( filters != null ) {
			result = filters.remove(key);
		}
		stickyService = null;
		return result;
	}

	/**
	 * Get the OSGi bundle context.
	 * 
	 * @return the bundle context
	 */
	public BundleContext getBundleContext() {
		return bundleContext;
	}

	/**
	 * Set the OSGi bundle context to use.
	 * 
	 * <p>
	 * In OSGi Blueprint, this is available via an implicit
	 * {@literal bundleContext} bean ID.
	 * </p>
	 * 
	 * @param bundleContext
	 *        the bundle context to use
	 */
	public void setBundleContext(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}

	/**
	 * Get the service class name to filter on.
	 * 
	 * @return the service class name, or {@literal null} to consider all
	 *         services
	 */
	public String getServiceClassName() {
		return serviceClassName;
	}

	/**
	 * Set the OSGi service class name to filter on.
	 * 
	 * @param serviceClassName
	 *        the service class name to look for , or {@literal null} to
	 *        consider all services
	 */
	public void setServiceClassName(String serviceClassName) {
		this.serviceClassName = serviceClassName;
	}

	/**
	 * Get the OSGi service filter to filter services on.
	 * 
	 * @return the service filter expression, or {@literal null} to not restrict
	 *         by any service filter
	 */
	public String getServiceFilter() {
		return serviceFilter;
	}

	/**
	 * Set an OSGi service filter to filter services on.
	 * 
	 * @param serviceFilter
	 *        the OSGi service filter expression, or {@literal null} to not
	 *        restrict by any service filter
	 */
	public void setServiceFilter(String serviceFilter) {
		this.serviceFilter = serviceFilter;
	}

	@Override
	public Map<String, Object> getPropertyFilters() {
		return propertyFilters;
	}

	/**
	 * Set a map of bean property names and associated values to match against
	 * all found OSGi services.
	 * 
	 * <p>
	 * The first service to match will be returned in {@link #service()}. This
	 * can be {@literal null}, in which case the first service found matching
	 * {@code serviceClassName} and {@code serviceFilter} will be returned.
	 * </p>
	 * 
	 * @param propertyFilters
	 *        the JavaBean property values to filter services on, or
	 *        {@literal null} to not restrict by bean properties
	 */
	public void setPropertyFilters(Map<String, Object> propertyFilters) {
		this.propertyFilters = propertyFilters;
	}

	/**
	 * Get the fallback service.
	 * 
	 * @return the fallback service
	 */
	public T getFallbackService() {
		return fallbackService;
	}

	/**
	 * Set a service to use if no other matching service is available.
	 * 
	 * <p>
	 * If no matching service is available when {@link #service()} is called,
	 * this value will be returned.
	 * </p>
	 * 
	 * @param fallbackService
	 *        the fallback service to use when no other service is available
	 */
	public void setFallbackService(T fallbackService) {
		this.fallbackService = fallbackService;
	}

	/**
	 * Get a flag to ignore empty property filter values.
	 * 
	 * @return {@literal true} to ignore empty property values when filtering;
	 *         defaults to {@literal true}.
	 */
	public boolean isIgnoreEmptyPropertyFilterValues() {
		return ignoreEmptyPropertyFilterValues;
	}

	/**
	 * Set a flag to ignore empty property filter values.
	 * 
	 * <p>
	 * If {@literal true}, then ignore property filter values that are
	 * {@literal null} or, if strings, have no length, for purposes of filtering
	 * services. If {@literal false} then the property filters must match even
	 * if empty, that is a {@literal null} filter value will only match services
	 * whose corresponding property is also {@literal null}.
	 * </p>
	 * 
	 * @param ignoreEmptyPropertyFilterValues
	 *        the ignore setting to use
	 */
	public void setIgnoreEmptyPropertyFilterValues(boolean ignoreEmptyPropertyFilterValues) {
		this.ignoreEmptyPropertyFilterValues = ignoreEmptyPropertyFilterValues;
	}

	/**
	 * Get the "sticky" mode.
	 * 
	 * @return {@literal true} to maintain a reference to the first-available
	 *         service
	 * @since 1.4
	 */
	public synchronized boolean isSticky() {
		return sticky;
	}

	/**
	 * Set the "sticky" mode.
	 * 
	 * <p>
	 * When {@literal true} then maintain a reference to the first-available
	 * service discovered, rather than resolve it each time {@link #service()}
	 * is called.
	 * </p>
	 * 
	 * @param sticky
	 *        {@literal true} to maintain a reference to the first-available
	 *        service
	 * @since 1.4
	 */
	public synchronized void setSticky(boolean sticky) {
		this.sticky = sticky;
	}

}

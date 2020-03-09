/* ==================================================================
 * DynamicServiceProxy.java - 8/06/2015 2:55:41 pm
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

package net.solarnetwork.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.wiring.BundleWiring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.FactoryBean;

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
 * This class is similar in purpose to the {@link DynamicServiceTracker} class,
 * except that it implements {@link FactoryBean} and returns a {@link Proxy}
 * that implements the configured {@code serviceClass} directly. Thus you can
 * configure this as a Spring bean (or Gemini Blueprint bean) and inject it
 * directly without needing the {@link OptionalService} API. Any method defined
 * in the {@code serviceClass} interface can be called on the proxy returned by
 * {@link #getObject()}, which will dynamically resolve an OSGi service matching
 * the filters configured on <em>this</em> class and then invoke the same method
 * on the resolved service.
 * </p>
 * 
 * <p>
 * The exposed proxy will also implement the {@link FilterableService}
 * interface.
 * </p>
 * 
 * @param <T>
 *        the tracked service type
 * @author matt
 * @version 1.1
 */
public class DynamicServiceProxy<T> implements InvocationHandler, FactoryBean<T>, FilterableService {

	private static final Comparator<ServiceReference<?>> RANK_COMPARATOR = new ServiceReferenceRankComparator();

	private BundleContext bundleContext;

	private Class<? extends T> serviceClass;
	private String serviceFilter;
	private Map<String, Object> propertyFilters;
	private boolean ignoreEmptyPropertyFilterValues = true;

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		try {
			Method ownMethod = this.getClass().getMethod(method.getName(), method.getParameterTypes());
			return ownMethod.invoke(this, args);
		} catch ( NoSuchMethodException e ) {
			// call on the service!
			T delegate = getServiceInstance();
			if ( delegate == null ) {
				throw new DynamicServiceUnavailableException(
						"No " + serviceClass.getName() + " service available matching service filter "
								+ serviceFilter + ", property filters " + propertyFilters);
			}
			Method delegateMethod = delegate.getClass().getMethod(method.getName(),
					method.getParameterTypes());
			try {
				return delegateMethod.invoke(delegate, args);
			} catch ( InvocationTargetException proxyException ) {
				log.debug("Exception calling proxy method " + method.getName());
				if ( proxyException.getCause() != null ) {
					throw proxyException.getCause();
				}
				throw proxyException;
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getObject() throws Exception {
		BundleWiring wiring = bundleContext.getBundle().adapt(BundleWiring.class);
		ClassLoader cl = wiring.getClassLoader();
		if ( cl == null ) {
			cl = serviceClass.getClassLoader();
		}
		return (T) Proxy.newProxyInstance(cl, new Class<?>[] { serviceClass, FilterableService.class },
				this);
	}

	@Override
	public Class<?> getObjectType() {
		return null;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@SuppressWarnings("unchecked")
	private T getServiceInstance() {
		final String serviceClassName = serviceClass.getName();
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
				Arrays.sort(refs, RANK_COMPARATOR);
			}
			for ( ServiceReference<?> ref : refs ) {
				Object service = bundleContext.getService(ref);
				final boolean match = serviceMatchesFilters(service);
				if ( match ) {
					log.debug("Found {} service matching properties {}: {}",
							new Object[] { serviceClassName, propertyFilters, service });
					return (T) service;
				}
			}
		}
		return null;
	}

	private boolean serviceMatchesFilters(Object service) {
		if ( service == null ) {
			return false;
		}
		final String serviceClassName = serviceClass.getName();
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

	/**
	 * Get the OSGi bundle context.
	 * 
	 * @return The configured bundle context.
	 */
	public BundleContext getBundleContext() {
		return bundleContext;
	}

	/**
	 * Set the OSGi {@link BundleContext} to use.
	 * 
	 * @param bundleContext
	 *        The bundle context for resolving services with.
	 */
	public void setBundleContext(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}

	/**
	 * Get the OSGi service interface to proxy.
	 * 
	 * @return The interface to proxy.
	 */
	public Class<? extends T> getServiceClass() {
		return serviceClass;
	}

	/**
	 * Set the OSGi service interface to proxy.
	 * 
	 * @param serviceClass
	 *        The interface to proxy.
	 */
	public void setServiceClass(Class<? extends T> serviceClass) {
		this.serviceClass = serviceClass;
	}

	/**
	 * Get the OSGi service filter to use.
	 * 
	 * @return The service filter, or {@literal null} if none.
	 * @see #setServiceFilter(String)
	 */
	public String getServiceFilter() {
		return serviceFilter;
	}

	/**
	 * Set an OSGi service filter to restrict services to, or {@literal null}
	 * for no filter.
	 * 
	 * @param serviceFilter
	 *        The service filter to use.
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
	 * all found OSGi services. The first service to match will be returned in
	 * {@link #getObject()}. This can be {@literal null}, in which case the
	 * first service found matching {@code serviceClassName} and
	 * {@code serviceFilter} will be returned.
	 * 
	 * @param propertyFilters
	 *        The property filter map to set.
	 */
	public void setPropertyFilters(Map<String, Object> propertyFilters) {
		this.propertyFilters = propertyFilters;
	}

	/**
	 * Get the flag to ignore empty property filter values.
	 * 
	 * @return The flag value.
	 * @see #setIgnoreEmptyPropertyFilterValues(boolean)
	 */
	public boolean isIgnoreEmptyPropertyFilterValues() {
		return ignoreEmptyPropertyFilterValues;
	}

	/**
	 * Set the flag to ignore empty property filter values or not. If
	 * {@literal true}, then ignore property filter values that are
	 * {@literal null} or, if strings, have no length, for purposes of filtering
	 * services. If {@literal false} then the property filters must match even
	 * if empty, that is a {@literal null} filter value will only match services
	 * whose corresponding property is also {@literal null}. Defaults to
	 * {@literal true}.
	 * 
	 * @param ignoreEmptyPropertyFilterValues
	 *        The flag to set.
	 */
	public void setIgnoreEmptyPropertyFilterValues(boolean ignoreEmptyPropertyFilterValues) {
		this.ignoreEmptyPropertyFilterValues = ignoreEmptyPropertyFilterValues;
	}

}

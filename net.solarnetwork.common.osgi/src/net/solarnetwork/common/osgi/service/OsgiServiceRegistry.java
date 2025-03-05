/* ==================================================================
 * OsgiServiceRegistry.java - 6/07/2024 7:36:26 am
 *
 * Copyright 2024 SolarNetwork.net Dev Team
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

import static net.solarnetwork.util.ObjectUtils.requireNonNullArgument;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import net.solarnetwork.service.RegisteredService;
import net.solarnetwork.service.ServiceRegistry;
import net.solarnetwork.util.ClassUtils;
import net.solarnetwork.util.CollectionUtils;
import net.solarnetwork.util.ObjectUtils;

/**
 * OSGi implementation of {@link ServiceRegistry}.
 *
 * @author matt
 * @version 1.0
 * @since 1.1
 */
public class OsgiServiceRegistry implements ServiceRegistry {

	private final BundleContext bundleContext;

	/**
	 * Constructor.
	 *
	 * @param bundleContext
	 *        the bundle context
	 * @throws IllegalArgumentException
	 *         if any argument is {@literal null}
	 */
	public OsgiServiceRegistry(BundleContext bundleContext) {
		super();
		this.bundleContext = requireNonNullArgument(bundleContext, "bundleContext");
	}

	@Override
	public Collection<?> services(String filter) {
		ServiceReference<?>[] refs;
		try {
			refs = bundleContext.getAllServiceReferences(null, filter);
		} catch ( InvalidSyntaxException e ) {
			throw new IllegalArgumentException("Invalid filter syntax.", e);
		}
		if ( refs == null ) {
			return Collections.emptyList();
		}
		final List<Object> results = new ArrayList<>(refs.length);
		for ( ServiceReference<?> ref : refs ) {
			try {
				final Object s = bundleContext.getService(ref);
				if ( s == null ) {
					continue;
				}
				results.add(s);
			} catch ( Exception e ) {
				// ignore and continue
			}
		}
		return results;
	}

	@Override
	public <S> Collection<S> services(Class<S> clazz, String filter) {
		Collection<ServiceReference<S>> refs;
		try {
			refs = bundleContext.getServiceReferences(clazz, filter);
		} catch ( InvalidSyntaxException e ) {
			throw new IllegalArgumentException("Invalid filter syntax.", e);
		}
		if ( refs == null || refs.isEmpty() ) {
			return Collections.emptyList();
		}
		final List<S> results = new ArrayList<>(refs.size());
		for ( ServiceReference<S> ref : refs ) {
			try {
				final S s = bundleContext.getService(ref);
				if ( s == null ) {
					continue;
				}
				results.add(s);
			} catch ( Exception e ) {
				// ignore and continue
			}
		}
		return results;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <S> RegisteredService<S> registerService(S service, Map<String, ?> properties,
			Class<?>... classes) {
		final String[] classNames;
		if ( classes != null ) {
			classNames = Arrays.stream(classes).map(c -> c.getName()).toArray(String[]::new);
		} else {
			Set<Class<?>> interfaces = ClassUtils.getAllNonJavaInterfacesForClassAsSet(
					ObjectUtils.requireNonNullArgument(service, "service").getClass());
			classNames = new String[] { (interfaces.isEmpty() ? service.getClass().getName()
					: interfaces.iterator().next().getName()) };
		}
		Dictionary<String, ?> props = CollectionUtils.dictionaryForMap(properties);
		ServiceRegistration<?> reg = bundleContext.registerService(classNames, service, props);
		return new OsgiRegisteredService<S>((ServiceRegistration<S>) reg);
	}

	@Override
	public void unregisterService(RegisteredService<?> registeredService) {
		if ( !(registeredService instanceof OsgiRegisteredService<?>) ) {
			return;
		}
		try {
			((OsgiRegisteredService<?>) registeredService).serviceRegistration().unregister();
		} catch ( IllegalStateException e ) {
			// ignore
		}
	}

}

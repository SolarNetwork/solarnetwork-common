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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import net.solarnetwork.service.ServiceRegistry;

/**
 * OSGi implementation of {@link ServiceRegistry}.
 *
 * @author matt
 * @version 1.0
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

}

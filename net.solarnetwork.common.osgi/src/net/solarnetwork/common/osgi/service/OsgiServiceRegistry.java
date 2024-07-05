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
import java.util.function.Predicate;
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
	public <S> Collection<S> services(Class<S> clazz, String filter, Predicate<S> predicate) {
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
			final S p = bundleContext.getService(ref);
			if ( p == null ) {
				continue;
			}
			if ( predicate != null && !predicate.test(p) ) {
				continue;
			}
			results.add(p);
		}
		return results;
	}

}

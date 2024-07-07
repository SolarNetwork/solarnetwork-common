/* ==================================================================
 * OsgiServiceRegistration.java - 8/07/2024 8:09:09 am
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

import java.util.Collections;
import java.util.Dictionary;
import java.util.Map;
import org.osgi.framework.ServiceRegistration;
import net.solarnetwork.service.ServiceRegistry;
import net.solarnetwork.util.CollectionUtils;
import net.solarnetwork.util.ObjectUtils;

/**
 * OSGi implementation of [@link
 * {@link net.solarnetwork.service.ServiceRegistry.RegisteredService}}.
 *
 * @param <S>
 *        the service type
 * @author matt
 * @version 1.0
 * @since 1.1
 */
public class OsgiRegisteredService<S> implements ServiceRegistry.RegisteredService<S> {

	private final ServiceRegistration<S> serviceRegistration;

	/**
	 * Constructor.
	 *
	 * @param serviceRegistration
	 *        the service registration to wrap
	 * @throws IllegalArgumentException
	 *         if any argument is {@literal null}
	 */
	public OsgiRegisteredService(ServiceRegistration<S> serviceRegistration) {
		super();
		this.serviceRegistration = ObjectUtils.requireNonNullArgument(serviceRegistration,
				"serviceRegistration");
	}

	@Override
	public Map<String, Object> properties() {
		try {
			Dictionary<String, Object> props = serviceRegistration.getReference().getProperties();
			Map<String, Object> result = CollectionUtils.mapForDictionary(props);
			if ( result != null ) {
				return result;
			}
		} catch ( IllegalStateException e ) {
			// ignore
		}
		return Collections.emptyMap();
	}

	/**
	 * Get the OSGi service registration.
	 *
	 * @return the service registration
	 */
	public final ServiceRegistration<S> serviceRegistration() {
		return serviceRegistration;
	}

}

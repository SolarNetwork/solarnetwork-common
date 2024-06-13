/* ==================================================================
 * Activator.java - 26/07/2019 11:38:17 am
 *
 * Copyright 2019 SolarNetwork.net Dev Team
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

package net.solarnetwork.common.jdbc.pool.hikari;

import java.util.Hashtable;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedServiceFactory;

/**
 * Bundle activator for the connection pool.
 *
 * @author matt
 * @version 1.0
 */
public class Activator implements BundleActivator {

	/**
	 * The service PID used for the
	 * {@link HikariDataSourceManagedServiceFactory}.
	 */
	public static final String SERVICE_PID = "net.solarnetwork.jdbc.pool.hikari";

	private ServiceRegistration<ManagedServiceFactory> msf = null;

	/**
	 * Constructor.
	 */
	public Activator() {
		super();
	}

	@Override
	public void start(BundleContext context) throws Exception {
		Hashtable<String, Object> properties = new Hashtable<>();
		properties.put(Constants.SERVICE_PID, SERVICE_PID);
		HikariDataSourceManagedServiceFactory dsmsf = new HikariDataSourceManagedServiceFactory(context);
		msf = context.registerService(ManagedServiceFactory.class, dsmsf, properties);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		if ( msf != null ) {
			msf.unregister();
			msf = null;
		}
	}

}

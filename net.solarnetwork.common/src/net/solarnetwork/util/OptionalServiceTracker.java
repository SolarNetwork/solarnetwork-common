/* ==================================================================
 * OptionalServiceTracker.java - Mar 1, 2011 9:34:07 AM
 * 
 * Copyright 2007-2011 SolarNetwork.net Dev Team
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

import org.osgi.framework.ServiceReference;

/**
 * An OSGi service registration listener for any type of object, so they can be
 * used to dynamically configure and publish other OSGi services.
 * 
 * <p>
 * For example, this might be configured via Spring DM like this:
 * </p>
 * 
 * <pre>
 * &lt;osgi:reference id="fooService"
 * 		interface="net.solarnetwork.node.FooService" cardinality="0..1"&gt;
 * 		&lt;osgi:listener bind-method="onBind" unbind-method="onUnbind" ref="optionalFooService"&gt;
 * &lt;/osgi:list&gt;
 * 
 * &lt;bean id="optionalFooService" 
 * 		class="net.solarnetwork.node.util.OptionalServiceTracker"&gt;
 * 		&lt;property name="service" ref="fooService"/&gt;
 * &lt;/bean&gt;
 * 
 * &lt;bean id="fooServiceConsumer" 
 * 		class="net.solarnetwork.node.FooServiceConsumer"&gt;
 * 		&lt;property name="service" ref="optionalFooService"/&gt;
 * &lt;/bean&gt;
 * </pre>
 * 
 * <p>
 * The configurable properties of this class are:
 * </p>
 * 
 * <dl class="class-properties">
 * <dt>service</dt>
 * <dd>The managed service to track.</dd>
 * </dl>
 * 
 * @author matt
 * @version 1.1
 */
public class OptionalServiceTracker<T> implements OptionalService<T> {

	private T service;
	private boolean available;

	/**
	 * Get the tracked service, or <em>null</em> if no service currently
	 * available.
	 * 
	 * @return the service
	 */
	public T getService() {
		if ( available ) {
			return service;
		}
		return null;
	}

	@Override
	public T service() {
		return getService();
	}

	/**
	 * Call when a matching service is available.
	 * 
	 * @param ref
	 *        the service reference
	 */
	@SuppressWarnings("rawtypes")
	// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=402255
	public void onBind(ServiceReference ref) {
		available = true;
	}

	/**
	 * Call when a service is no longer available.
	 * 
	 * @param ref
	 *        the service reference
	 */
	@SuppressWarnings("rawtypes")
	public void onUnbind(ServiceReference ref) {
		available = false;
	}

	/**
	 * @param service
	 *        the service to set
	 */
	public void setService(T service) {
		this.service = service;
	}

	/**
	 * @return the available
	 */
	public boolean isAvailable() {
		return available;
	}

}

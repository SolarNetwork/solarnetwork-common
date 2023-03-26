/* ==================================================================
 * CloseableServiceTracker.java - 20/06/2017 6:13:31 PM
 * 
 * Copyright 2017 SolarNetwork.net Dev Team
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

package net.solarnetwork.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A tracker of {@link CloseableService} instances, so they have their resources
 * freed when removed from the system runtime.
 * 
 * <p>
 * For example, this class might be configured via OSGi Blueprint like this:
 * </p>
 * 
 * <pre>
 * &lt;reference-list interface="net.solarnetwork.util.CloseableService" availability="optional"&gt;
 * 		&lt;reference-listener unbind-method="onReleased"&gt;
 * 			&lt;bean class="net.solarnetwork.util.CloseableServiceTracker"/&gt;
 * 		&lt;/reference-listener&gt;
 * &lt;/reference-list&gt;
 * </pre>
 * 
 * @author matt
 * @version 1.0
 * @since 1.36
 */
public class CloseableServiceTracker {

	private final Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * Constructor.
	 */
	public CloseableServiceTracker() {
		super();
	}

	/**
	 * Call when an {@link CloseableService} is no longer available.
	 * 
	 * @param service
	 *        the service reference
	 */
	public void onReleased(CloseableService service) {
		if ( service == null ) {
			return;
		}
		log.debug("Releasing CloseableService {}", service);
		try {
			service.closeService();
		} catch ( RuntimeException e ) {
			log.error("Error closing CloseableService {}", service, e);
		}
	}

}

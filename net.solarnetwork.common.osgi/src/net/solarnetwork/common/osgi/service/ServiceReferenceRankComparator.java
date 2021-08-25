/* ==================================================================
 * ServiceReferenceRankComparator.java - 7/06/2018 9:48:22 AM
 * 
 * Copyright 2018 SolarNetwork.net Dev Team
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

import java.util.Comparator;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

/**
 * Comparator of {@link ServiceReference} that orders from highest rank to
 * lowest rank.
 * 
 * <p>
 * If two references have the same rank, then the order is from lowest to
 * highest service ID.
 * </p>
 * 
 * <p>
 * <b>Note</b> this is essentially the reverse of the
 * {@link ServiceReference#compareTo(Object)} contract.
 * </p>
 * 
 * @author matt
 * @version 1.0
 * @since 1.45
 */
public class ServiceReferenceRankComparator implements Comparator<ServiceReference<?>> {

	@Override
	public int compare(ServiceReference<?> o1, ServiceReference<?> o2) {
		Object p1 = o1.getProperty(Constants.SERVICE_RANKING);
		Object p2 = o2.getProperty(Constants.SERVICE_RANKING);

		final int r1 = (p1 instanceof Number ? ((Number) p1).intValue() : 0);
		final int r2 = (p2 instanceof Number ? ((Number) p2).intValue() : 0);
		if ( r1 != r2 ) {
			return (r1 > r2 ? -1 : 1);
		}

		p1 = o1.getProperty(Constants.SERVICE_ID);
		p2 = o2.getProperty(Constants.SERVICE_ID);

		final long id1 = (p1 instanceof Number ? ((Number) p1).longValue() : 0L);
		final long id2 = (p2 instanceof Number ? ((Number) p2).longValue() : 0L);
		return (id1 < id2 ? -1 : id1 > id2 ? 1 : 0);
	}

}

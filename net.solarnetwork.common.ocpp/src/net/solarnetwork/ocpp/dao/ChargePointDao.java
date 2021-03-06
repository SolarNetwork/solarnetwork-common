/* ==================================================================
 * ChargePointDao.java - 7/02/2020 9:43:12 am
 * 
 * Copyright 2020 SolarNetwork.net Dev Team
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

package net.solarnetwork.ocpp.dao;

import net.solarnetwork.dao.GenericDao;
import net.solarnetwork.ocpp.domain.ChargePoint;
import net.solarnetwork.ocpp.domain.ChargePointIdentity;

/**
 * Data Access Object API for {@link ChargePoint} entities.
 * 
 * @author matt
 * @version 1.0
 */
public interface ChargePointDao extends GenericDao<ChargePoint, Long> {

	/**
	 * Get a charge point by its unique identity.
	 * 
	 * @param identity
	 *        the charge point identity to look for
	 * @return the matching charge point, or {@literal null} if not found
	 */
	ChargePoint getForIdentity(ChargePointIdentity identity);

}

/* ==================================================================
 * SystemUserDao.java - 20/02/2020 10:26:54 am
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
import net.solarnetwork.ocpp.domain.SystemUser;

/**
 * DAO for {@link SystemUser} entities.
 * 
 * <p>
 * The {@code username} property is expected to be treated as a unique key.
 * </p>
 * 
 * @author matt
 * @version 1.1
 */
public interface SystemUserDao extends GenericDao<SystemUser, Long> {

	/**
	 * Get a system user by its unique username.
	 * 
	 * @param username
	 *        the username to look for
	 * @return the matching system user, or {@literal null} if not found
	 */
	SystemUser getForUsername(String username);

	/**
	 * Get a system user for a unique username that is associated with a given
	 * charge point.
	 * 
	 * <p>
	 * This method should only return the system user if it user exists
	 * <b>and</b> has {@code chargePointIdentifier} configured in
	 * {@link SystemUser#getAllowedChargePoints()} <b>and</b> the charge point
	 * is not disabled ({@link ChargePoint#isEnabled()} is {@literal true}).
	 * </p>
	 * 
	 * @param username
	 *        the username to look for
	 * @param chargePointIdentifier
	 *        the associated charge point to restrict to
	 * @return the matching system user, or {@literal null} if not found
	 * @since 1.1
	 */
	SystemUser getForUsernameAndChargePoint(String username, String chargePointIdentifier);

}

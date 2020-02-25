/* ==================================================================
 * ChargePointConnectorDao.java - 12/02/2020 4:26:51 pm
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

import java.util.Collection;
import net.solarnetwork.dao.GenericDao;
import net.solarnetwork.ocpp.domain.ChargePointConnector;
import net.solarnetwork.ocpp.domain.ChargePointConnectorKey;
import net.solarnetwork.ocpp.domain.ChargePointStatus;
import net.solarnetwork.ocpp.domain.StatusNotification;

/**
 * Data Access Object API for {@link ChargePointConnector} entities.
 * 
 * @author matt
 * @version 1.0
 */
public interface ChargePointConnectorDao
		extends GenericDao<ChargePointConnector, ChargePointConnectorKey> {

	/**
	 * Save (insert/update) status information directly.
	 * 
	 * <p>
	 * If a {@link ChargePointConnector} entity does not exist, this method will
	 * insert a new row, using default values for all non
	 * {@link StatusNotification} properties.
	 * </p>
	 * 
	 * @param chargePointId
	 *        the Charge Point ID
	 * @param info
	 *        the status info
	 * @return the saved primary key
	 */
	ChargePointConnectorKey saveStatusInfo(long chargePointId, StatusNotification info);

	/**
	 * Update just the status of one or more connectors for a specific Charge
	 * Point.
	 * 
	 * @param chargePointId
	 *        the Charge Point ID
	 * @param connectorId
	 *        the connector ID, or {@literal 0} for all connectors for Charge
	 *        Point
	 * @param status
	 *        the desired status
	 * @return the number of connectors updated
	 */
	int updateChargePointStatus(long chargePointId, int connectorId, ChargePointStatus status);

	/**
	 * Find all available connectors for a given Charge Point ID.
	 * 
	 * @param chargePointId
	 *        the ID of the Charge Point to find connectors for
	 * @return the connectors, ordered by connector ID in ascending order
	 */
	Collection<ChargePointConnector> findByChargePointId(long chargePointId);

}

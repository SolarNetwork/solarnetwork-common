/* ==================================================================
 * MqttMessageDao.java - 10/06/2021 5:33:46 PM
 * 
 * Copyright 2021 SolarNetwork.net Dev Team
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

package net.solarnetwork.common.mqtt.dao;

import net.solarnetwork.dao.BatchableDao;
import net.solarnetwork.dao.GenericDao;

/**
 * DAO API for {@link MqttMessageEntity} objects.
 * 
 * @author matt
 * @version 1.0
 * @since 2.5
 */
public interface MqttMessageDao
		extends GenericDao<MqttMessageEntity, Long>, BatchableDao<MqttMessageEntity> {

	/**
	 * A {@link BatchableDao.BatchOptions} parameter for a destination to filter
	 * on.
	 */
	String BATCH_OPTION_DESTINATION = "destination";

}

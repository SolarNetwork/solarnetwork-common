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

import java.util.Collection;
import java.util.List;
import net.solarnetwork.dao.GenericDao;
import net.solarnetwork.domain.SortDescriptor;

/**
 * DAO API for {@link MqttMessageEntity} objects.
 * 
 * @author matt
 * @version 1.0
 * @since 2.5
 */
public interface MqttMessageDao extends GenericDao<MqttMessageEntity, Long> {

	/**
	 * API for querying for an ordered subset of results from all possible
	 * results.
	 * 
	 * @param sorts
	 *        the optional sort descriptors
	 * @param offset
	 *        an optional result offset
	 * @param max
	 *        an optional maximum number of returned results
	 * @return the results, never {@literal null}
	 */
	Collection<MqttMessageEntity> getRange(List<SortDescriptor> sorts, Integer offset, Integer max);

}

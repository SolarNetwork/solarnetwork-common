/* ==================================================================
 * ChargePointBrokerTracker.java - 11/02/2020 10:40:42 am
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

package net.solarnetwork.ocpp.service;

import java.util.Set;
import java.util.TreeSet;
import net.solarnetwork.util.OptionalServiceCollection;

/**
 * Simple implementation of {@link ChargePointRouter} using an
 * {@link OptionalServiceCollection} of brokers.
 * 
 * @author matt
 * @version 1.0
 */
public class ChargePointBrokerTracker implements ChargePointRouter {

	private final OptionalServiceCollection<ChargePointBroker> brokers;

	public ChargePointBrokerTracker(OptionalServiceCollection<ChargePointBroker> brokers) {
		super();
		if ( brokers == null ) {
			throw new IllegalArgumentException("The brokers parameter must not be null.");
		}
		this.brokers = brokers;
	}

	@Override
	public Set<String> availableChargePointsIds() {
		Set<String> ids = new TreeSet<>();
		for ( ChargePointBroker b : brokers.services() ) {
			ids.addAll(b.availableChargePointsIds());
		}
		return ids;
	}

	@Override
	public ChargePointBroker brokerForChargePoint(String clientId) {
		for ( ChargePointBroker b : brokers.services() ) {
			if ( b.isChargePointAvailable(clientId) ) {
				return b;
			}
		}
		return null;
	}

}

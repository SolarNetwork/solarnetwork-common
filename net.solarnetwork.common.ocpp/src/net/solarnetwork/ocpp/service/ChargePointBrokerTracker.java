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
import net.solarnetwork.ocpp.domain.ChargePointIdentity;
import net.solarnetwork.service.OptionalServiceCollection;

/**
 * Simple implementation of {@link ChargePointRouter} using an
 * {@link OptionalServiceCollection} of brokers.
 * 
 * @author matt
 * @version 2.0
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
	public Set<ChargePointIdentity> availableChargePointsIds() {
		Set<ChargePointIdentity> ids = new TreeSet<>();
		for ( ChargePointBroker b : brokers.services() ) {
			ids.addAll(b.availableChargePointsIds());
		}
		return ids;
	}

	@Override
	public boolean isChargePointAvailable(ChargePointIdentity identity) {
		if ( identity != null ) {
			for ( ChargePointBroker b : brokers.services() ) {
				if ( b.isChargePointAvailable(identity) ) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public ChargePointBroker brokerForChargePoint(ChargePointIdentity identity) {
		if ( identity != null ) {
			for ( ChargePointBroker b : brokers.services() ) {
				if ( b.isChargePointAvailable(identity) ) {
					return b;
				}
			}
		}
		return null;
	}

}

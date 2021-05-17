/* ==================================================================
 * SimpleTariff.java - 12/05/2021 5:12:05 PM
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

package net.solarnetwork.domain.tariff;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Simple implementation of {@link Tariff}.
 * 
 * @author matt
 * @version 1.0
 * @since 1.71
 */
public class SimpleTariff implements Tariff {

	private final Map<String, Rate> rates;

	/**
	 * Constructor.
	 * 
	 * @param rates
	 *        the rate mapping of rate IDs to associated rates
	 */
	public SimpleTariff(Map<String, Rate> rates) {
		super();
		this.rates = rates;
	}

	/**
	 * Constructor.
	 * 
	 * <p>
	 * This will create a map from the given rates, using the rate IDs as the
	 * map keys.
	 * </p>
	 * 
	 * @param rates
	 *        the rate collection
	 */
	public SimpleTariff(Iterable<Rate> rates) {
		this(Collections.unmodifiableMap(StreamSupport.stream(rates.spliterator(), false).collect(
				Collectors.toMap(Rate::getId, Function.identity(), (k, v) -> v, LinkedHashMap::new))));
	}

	@Override
	public Map<String, Rate> getRates() {
		return rates;
	}

}

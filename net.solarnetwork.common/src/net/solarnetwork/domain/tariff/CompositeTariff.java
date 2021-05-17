/* ==================================================================
 * CompositeTariff.java - 12/05/2021 3:55:36 PM
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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A composite collection of tariffs that act like a single {@link Tariff}.
 * 
 * <p>
 * The {@link #getRates()} will return a composite map out of all configured
 * tariff rate maps. The tariffs and their rate maps are iterated over in their
 * natural order, and duplicate keys are skipped.
 * </p>
 * 
 * @author matt
 * @version 1.0
 * @since 1.71
 */
public class CompositeTariff implements Tariff {

	private final Collection<? extends Tariff> tariffs;

	/**
	 * Constructor.
	 * 
	 * @param tariffs
	 *        the tariffs
	 */
	public CompositeTariff(Collection<? extends Tariff> tariffs) {
		super();
		this.tariffs = (tariffs != null ? tariffs : Collections.emptyList());
	}

	@Override
	public Map<String, Rate> getRates() {
		if ( tariffs.isEmpty() ) {
			return Collections.emptyMap();
		}
		LinkedHashMap<String, Rate> r = new LinkedHashMap<>();
		for ( Tariff t : tariffs ) {
			Map<String, Rate> tr = t.getRates();
			if ( tr != null ) {
				for ( Map.Entry<String, Rate> me : tr.entrySet() ) {
					if ( !r.containsKey(me.getKey()) ) {
						r.put(me.getKey(), me.getValue());
					}
				}
			}
		}
		return r;
	}

}

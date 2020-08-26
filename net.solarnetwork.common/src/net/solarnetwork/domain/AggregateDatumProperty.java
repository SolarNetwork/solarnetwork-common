/* ==================================================================
 * AggregateDatumProperty.java - 27/08/2020 10:40:05 AM
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

package net.solarnetwork.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * An aggregate datum property.
 * 
 * @author matt
 * @version 1.0
 * @since 1.65
 */
public class AggregateDatumProperty {

	private final BigDecimal first;
	private int count;
	private BigDecimal total;
	private BigDecimal min;
	private BigDecimal max;
	private BigDecimal last;

	/**
	 * Constructor.
	 * 
	 * @param val
	 *        the initial value
	 */
	public AggregateDatumProperty(BigDecimal val) {
		super();
		this.count = 1;
		this.total = (val == null ? BigDecimal.ZERO : val);
		this.min = total;
		this.max = total;
		this.first = total;
		this.last = total;
	}

	/**
	 * Accumulate another value.
	 * 
	 * @param val
	 *        the value to accumulate
	 */
	public void accumulate(BigDecimal val) {
		count++;
		if ( val == null ) {
			val = BigDecimal.ZERO;
		}
		total = total.add(val);
		if ( val.compareTo(min) < 0 ) {
			min = val;
		} else if ( val.compareTo(max) > 0 ) {
			max = val;
		}
		this.last = val;
	}

	/**
	 * Compute the average value of all accumulated values.
	 * 
	 * @param decimalScale
	 *        the maximum decimal scale to round to
	 * @return the average value
	 */
	public BigDecimal average(int decimalScale) {
		return total.divide(new BigDecimal(count), decimalScale, RoundingMode.HALF_UP)
				.stripTrailingZeros();
	}

	/**
	 * Get the count of accumulated values.
	 * 
	 * @return the count of values
	 */
	public int getCount() {
		return count;
	}

	/**
	 * Get the total sum of accumulated values.
	 * 
	 * @return the total sum of values
	 */
	public BigDecimal getTotal() {
		return total;
	}

	/**
	 * Get the minimum accumulated value.
	 * 
	 * @return the min
	 */
	public BigDecimal getMin() {
		return min;
	}

	/**
	 * Get the maximum accumulated value.
	 * 
	 * @return the max
	 */
	public BigDecimal getMax() {
		return max;
	}

	/**
	 * Get the first accumulated value.
	 * 
	 * @return the first value, never {@literal null}
	 */
	public BigDecimal first() {
		return first;
	}

	/**
	 * Get the last accumulated value.
	 * 
	 * @return the last value, never {@literal null}
	 */
	public BigDecimal last() {
		return last;
	}

}

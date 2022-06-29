/* ==================================================================
 * DatumPropertiesStatistics.java - 30/10/2020 4:35:52 pm
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

package net.solarnetwork.domain.datum;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;

/**
 * Statistic information associated with datum properties.
 * 
 * @author matt
 * @version 1.0
 * @since 2.7
 */
public class DatumPropertiesStatistics implements Serializable {

	/**
	 * Instantaneous statistic enumeration.
	 * 
	 * <p>
	 * The ordinal of each enumeration value represents its position in the
	 * statistics array for a given instantaneous property.
	 * </p>
	 */
	public static enum InstantaneousStatistic {

		/** The count of properties that participated in the aggregate value. */
		Count,

		/** The minimum property value seen within the aggregate period. */
		Minimum,

		/** The maximum property value seen within the aggregate period. */
		Maximum;
	}

	/**
	 * Accumulating statistic enumeration.
	 * 
	 * <p>
	 * The ordinal of each enumeration value represents its position in the
	 * statistics array for a given instantaneous property.
	 * </p>
	 */
	public static enum AccumulatingStatistic {

		/** The first property value seen within the aggregate period. */
		Start,

		/** The last property value seen within the aggregate period. */
		End;
	}

	private static final long serialVersionUID = -1933887645480711417L;

	private BigDecimal[][] instantaneous;
	private BigDecimal[][] accumulating;

	/**
	 * Create a datum statistics instance.
	 * 
	 * @param instantaneous
	 *        the instantaneous statistic values
	 * @param accumulating
	 *        the accumulating statistic values
	 * @return the new instance, never {@literal null}
	 */
	public static DatumPropertiesStatistics statisticsOf(BigDecimal[][] instantaneous,
			BigDecimal[][] accumulating) {
		DatumPropertiesStatistics s = new DatumPropertiesStatistics();
		s.instantaneous = instantaneous;
		s.accumulating = accumulating;
		return s;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DatumPropertiesStatistics{");
		if ( instantaneous != null ) {
			builder.append("i=");
			builder.append(Arrays.deepToString(instantaneous));
			builder.append("], ");
		}
		if ( accumulating != null ) {
			builder.append("a=");
			builder.append(Arrays.deepToString(accumulating));
		}
		builder.append("}");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.deepHashCode(accumulating);
		result = prime * result + Arrays.deepHashCode(instantaneous);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( !(obj instanceof DatumPropertiesStatistics) ) {
			return false;
		}
		DatumPropertiesStatistics other = (DatumPropertiesStatistics) obj;
		return Arrays.deepEquals(accumulating, other.accumulating)
				&& Arrays.deepEquals(instantaneous, other.instantaneous);
	}

	/**
	 * Get the overall number of array property values (first dimension).
	 * 
	 * <p>
	 * This returns the sum of the length (first dimension) all the array fields
	 * of this class.
	 * </p>
	 * 
	 * @return the number of values (including {@literal null} values)
	 */
	public int getLength() {
		return getInstantaneousLength() + getAccumulatingLength();
	}

	/**
	 * Get the instantaneous values array length (first dimension).
	 * 
	 * @return the number of instantaneous values (including {@literal null}
	 *         values)
	 */
	public int getInstantaneousLength() {
		BigDecimal[][] array = getInstantaneous();
		return (array != null ? array.length : 0);
	}

	/**
	 * Get the instantaneous statistics.
	 * 
	 * @return the instantaneous statistics
	 */
	public BigDecimal[][] getInstantaneous() {
		return instantaneous;
	}

	/**
	 * Set the instantaneous statistics.
	 * 
	 * @param instantaneous
	 *        the instantaneous statistics to set
	 */
	public void setInstantaneous(BigDecimal[][] instantaneous) {
		this.instantaneous = instantaneous;
	}

	private static BigDecimal getStat(BigDecimal[][] array, int propertyIndex, int statIndex) {
		if ( array == null || propertyIndex >= array.length ) {
			return null;
		}
		BigDecimal[] stats = array[propertyIndex];
		if ( stats == null || statIndex >= stats.length ) {
			return null;
		}
		return stats[statIndex];
	}

	/**
	 * Get the count statistic for an instantaneous property.
	 * 
	 * @param propertyIndex
	 *        the index of the property to get the statistic for
	 * @return the statistic value, or {@literal null} if not available
	 */
	public BigDecimal getInstantaneousCount(int propertyIndex) {
		return getStat(getInstantaneous(), propertyIndex, InstantaneousStatistic.Count.ordinal());
	}

	/**
	 * Get the minimum statistic for an instantaneous property.
	 * 
	 * @param propertyIndex
	 *        the index of the property to get the statistic for
	 * @return the statistic value, or {@literal null} if not available
	 */
	public BigDecimal getInstantaneousMinimum(int propertyIndex) {
		return getStat(getInstantaneous(), propertyIndex, InstantaneousStatistic.Minimum.ordinal());
	}

	/**
	 * Get the maximum statistic for an instantaneous property.
	 * 
	 * @param propertyIndex
	 *        the index of the property to get the statistic for
	 * @return the statistic value, or {@literal null} if not available
	 */
	public BigDecimal getInstantaneousMaximum(int propertyIndex) {
		return getStat(getInstantaneous(), propertyIndex, InstantaneousStatistic.Maximum.ordinal());
	}

	/**
	 * Get the start statistic for an accumulating property.
	 * 
	 * @param propertyIndex
	 *        the index of the property to get the statistic for
	 * @return the statistic value, or {@literal null} if not available
	 */
	public BigDecimal getAccumulatingStart(int propertyIndex) {
		return getStat(getAccumulating(), propertyIndex, AccumulatingStatistic.Start.ordinal());
	}

	/**
	 * Get the end statistic for an accumulating property.
	 * 
	 * @param propertyIndex
	 *        the index of the property to get the statistic for
	 * @return the statistic value, or {@literal null} if not available
	 */
	public BigDecimal getAccumulatingEnd(int propertyIndex) {
		return getStat(getAccumulating(), propertyIndex, AccumulatingStatistic.End.ordinal());
	}

	/**
	 * Get the accumulating values array length (first dimension).
	 * 
	 * @return the number of accumulating values (including {@literal null}
	 *         values)
	 */
	public int getAccumulatingLength() {
		BigDecimal[][] array = getAccumulating();
		return (array != null ? array.length : 0);
	}

	/**
	 * Get the accumulating statistics.
	 * 
	 * @return the accumulating statistics
	 */
	public BigDecimal[][] getAccumulating() {
		return accumulating;
	}

	/**
	 * Set the accumulating statistics.
	 * 
	 * @param accumulating
	 *        the accumulating statistics to set
	 */
	public void setAccumulating(BigDecimal[][] accumulating) {
		this.accumulating = accumulating;
	}

}

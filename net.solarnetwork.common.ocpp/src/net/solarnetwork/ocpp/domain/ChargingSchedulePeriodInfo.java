/* ==================================================================
 * ChargingSchedulePeriodInfo.java - 18/02/2020 3:18:46 pm
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

package net.solarnetwork.ocpp.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Objects;
import net.solarnetwork.domain.Differentiable;

/**
 * Information about a charging schedule period.
 * 
 * @author matt
 * @version 1.0
 */
public class ChargingSchedulePeriodInfo implements Differentiable<ChargingSchedulePeriodInfo> {

	private Duration startOffset;
	private BigDecimal rateLimit;
	private Integer numPhases;

	/**
	 * Default constructor.
	 * 
	 * <p>
	 * This rate limit is set to zero.
	 * </p>
	 */
	public ChargingSchedulePeriodInfo() {
		this(Duration.ZERO, BigDecimal.ZERO);
	}

	/**
	 * Constructor.
	 * 
	 * @param startOffset
	 *        the start offset from the start of the schedule
	 * @param rateLimit
	 *        the rate limit
	 * @throws IllegalArgumentException
	 *         if {@code startOffset} or {@code rateLimit} are {@literal null}
	 */
	public ChargingSchedulePeriodInfo(Duration startOffset, BigDecimal rateLimit) {
		super();
		setStartOffset(startOffset);
		setRateLimit(rateLimit);
	}

	/**
	 * Constructor.
	 * 
	 * @param startOffset
	 *        the start offset from the start of the schedule
	 * @param rateLimit
	 *        the rate limit
	 * @param numPhases
	 *        the optional number of phases
	 * @throws IllegalArgumentException
	 *         if {@code startOffset} or {@code rateLimit} are {@literal null}
	 */
	public ChargingSchedulePeriodInfo(Duration startOffset, BigDecimal rateLimit, Integer numPhases) {
		this(startOffset, rateLimit);
		this.numPhases = numPhases;
	}

	/**
	 * Copy constructor.
	 * 
	 * @param other
	 *        the other info to copy
	 */
	public ChargingSchedulePeriodInfo(ChargingSchedulePeriodInfo other) {
		this(other.startOffset, other.rateLimit, other.numPhases);
	}

	/**
	 * Test if the properties of another entity are the same as in this
	 * instance.
	 * 
	 * @param other
	 *        the other entity to compare to
	 * @return {@literal true} if the properties of this instance are equal to
	 *         the other
	 */
	public boolean isSameAs(ChargingSchedulePeriodInfo other) {
		if ( other == null ) {
			return false;
		}
		// @formatter:off
		return Objects.equals(startOffset, other.startOffset)
				&& Objects.equals(rateLimit, other.rateLimit)
				&& Objects.equals(numPhases, other.numPhases);
		// @formatter:on
	}

	@Override
	public boolean differsFrom(ChargingSchedulePeriodInfo other) {
		return !isSameAs(other);
	}

	@Override
	public int hashCode() {
		return Objects.hash(numPhases, rateLimit, startOffset);
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( !(obj instanceof ChargingSchedulePeriodInfo) ) {
			return false;
		}
		ChargingSchedulePeriodInfo other = (ChargingSchedulePeriodInfo) obj;
		return Objects.equals(numPhases, other.numPhases) && Objects.equals(rateLimit, other.rateLimit)
				&& Objects.equals(startOffset, other.startOffset);
	}

	@Override
	public String toString() {
		StringBuilder builder2 = new StringBuilder();
		builder2.append("ChargingSchedulePeriodInfo{");
		if ( startOffset != null ) {
			builder2.append("startOffset=");
			builder2.append(startOffset);
			builder2.append(", ");
		}
		if ( rateLimit != null ) {
			builder2.append("rateLimit=");
			builder2.append(rateLimit);
			builder2.append(", ");
		}
		if ( numPhases != null ) {
			builder2.append("numPhases=");
			builder2.append(numPhases);
		}
		builder2.append("}");
		return builder2.toString();
	}

	/**
	 * Get the start offset.
	 * 
	 * @return the offset from the start of the schedule; never {@literal null}
	 */
	public Duration getStartOffset() {
		return startOffset;
	}

	/**
	 * Set the start offset.
	 * 
	 * @param startOffset
	 *        the offset from the start of the schedule to set
	 * @throws IllegalArgumentException
	 *         if {@code startOffset} is {@literal null}
	 */
	public void setStartOffset(Duration startOffset) {
		this.startOffset = startOffset;
	}

	/**
	 * Get the start offset, in seconds.
	 * 
	 * @return the offset from the start of the schedule
	 */
	public int getStartOffsetSeconds() {
		return (int) getStartOffset().getSeconds();
	}

	/**
	 * Set the start offset, in seconds.
	 * 
	 * @param seconds
	 *        the seconds
	 */
	public void setStartOffsetSeconds(int seconds) {
		setStartOffset(Duration.ofSeconds(seconds));
	}

	/**
	 * Get the rate limit.
	 * 
	 * @return the limit; never {@literal null}
	 */
	public BigDecimal getRateLimit() {
		return rateLimit;
	}

	/**
	 * Set the rate limit.
	 * 
	 * @param rateLimit
	 *        the limit to set
	 * @throws IllegalArgumentException
	 *         if {@code rateLimit} is {@literal null}
	 */
	public void setRateLimit(BigDecimal rateLimit) {
		if ( rateLimit == null ) {
			throw new IllegalArgumentException("The rateLimit parameter must not be null.");
		}
		if ( rateLimit.scale() != 1 ) {
			rateLimit = rateLimit.setScale(1, RoundingMode.HALF_UP);
		}
		this.rateLimit = rateLimit;
	}

	/**
	 * Get the number of phases.
	 * 
	 * @return the phase count, or {@literal null} if not applicable (e.g. DC
	 *         charging)
	 */
	public Integer getNumPhases() {
		return numPhases;
	}

	/**
	 * Set the number of phases.
	 * 
	 * @param numPhases
	 *        the phase count to set
	 */
	public void setNumPhases(Integer numPhases) {
		this.numPhases = numPhases;
	}

}

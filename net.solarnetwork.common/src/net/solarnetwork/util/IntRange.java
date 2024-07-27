/* ==================================================================
 * IntRange.java - 15/01/2020 10:28:27 am
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

package net.solarnetwork.util;

import java.io.Serializable;
import java.util.Collections;
import java.util.Objects;

/**
 * An immutable integer range with min/max values.
 *
 * <p>
 * Inspired and adapted from <a href="http://pcj.sourceforge.net/">PCJ's</a>
 * {@code bak.pcj.set.IntRange} class.
 * </p>
 *
 * @author matt
 * @version 1.1
 * @since 1.58
 */
public final class IntRange implements Serializable, Comparable<IntRange>, IntRangeContainer {

	private static final long serialVersionUID = 2815680548854317296L;

	/** The minimum value. */
	private final int min;

	/** The maximum value. */
	private final int max;

	/**
	 * Constructor.
	 *
	 * <p>
	 * Note that if {@code min > max} then {@link #getMin()} will return
	 * {@code max} and {@link #getMax()} will return {@code min}. That is, the
	 * minimum and maximum values passed to this constructor will be compared
	 * before storing in this class so that {@link #getMin()} always returns the
	 * actual minimum value.
	 * </p>
	 *
	 * @param min
	 *        the minimum value
	 * @param max
	 *        the maximum value
	 */
	public IntRange(int min, int max) {
		super();
		this.min = min < max ? min : max;
		this.max = max > min ? max : min;
	}

	/**
	 * Create a singleton range (where the minimum and maximum are the same).
	 *
	 * @param value
	 *        the singleton value
	 * @return the new range
	 */
	public static IntRange rangeOf(int value) {
		return new IntRange(value, value);
	}

	/**
	 * Create a range.
	 *
	 * @param min
	 *        the minimum value
	 * @param max
	 *        the maximum value
	 * @return the new range
	 */
	public static IntRange rangeOf(int min, int max) {
		return new IntRange(min, max);
	}

	/**
	 * Get the minimum value.
	 *
	 * @return the minimum
	 */
	public int getMin() {
		return min;
	}

	/**
	 * Get the maximum value.
	 *
	 * @return the maximum
	 */
	public int getMax() {
		return max;
	}

	@Override
	public Integer min() {
		// TODO Auto-generated method stub
		return getMin();
	}

	@Override
	public Integer max() {
		return getMax();
	}

	@Override
	public Iterable<IntRange> ranges() {
		return Collections.singleton(this);
	}

	/**
	 * Get the number of integer values between {@code min} and {@code max},
	 * inclusive.
	 *
	 * @return the inclusive length between {@code min} and {@code max}
	 */
	public int length() {
		return (max - min) + 1;
	}

	/**
	 * Test if this range represents a singleton value, where the minimum and
	 * maximum values in the range are equal.
	 *
	 * @return {@literal true} if {@code min == max}
	 */
	public boolean isSingleton() {
		return max == min;
	}

	/**
	 * Test if a value is within this range, inclusive.
	 *
	 * @param value
	 *        the value to test
	 * @return {@literal true} if {@code min <= value <= max}
	 */
	@Override
	public boolean contains(final int value) {
		return (value >= min && value <= max);
	}

	/**
	 * Test if another range is completely within this range, inclusive.
	 *
	 * @param min
	 *        the minimum of the range to test
	 * @param max
	 *        the maximum of the range to test
	 * @return {@literal true} if {@code this.min <= min <= max <= this.max}
	 */
	public boolean containsAll(final int min, final int max) {
		return (min <= max && min >= this.min && max <= this.max);
	}

	/**
	 * Test if another range is completely within this range, inclusive.
	 *
	 * @param o
	 *        the range to test
	 * @return {@literal true} if {@code this.min <= o.min <= o.max <= this.max}
	 */
	public boolean containsAll(final IntRange o) {
		return containsAll(o.getMin(), o.getMax());
	}

	/**
	 * Test if this range intersects with a given range.
	 *
	 * @param o
	 *        the range to compare to this range
	 * @return {@literal true} if this range intersects (overlaps) with the
	 *         given range
	 * @throws NullPointerException
	 *         if {@code o} is {@literal null}
	 */
	public boolean intersects(final IntRange o) {
		return (min >= o.min && min <= o.max) || (o.min >= min && o.min <= max);
	}

	/**
	 * Test if this range is adjacent to (but not intersecting) a given range.
	 *
	 * @param o
	 *        the range to compare to this range
	 * @return {@literal true} if this range is adjacent to the given range
	 * @throws NullPointerException
	 *         if {@code o} is {@literal null}
	 */
	public boolean adjacentTo(final IntRange o) {
		return (max + 1 == o.min) || (o.max + 1 == min);
	}

	/**
	 * Test if this range could be merged with another range.
	 *
	 *
	 * <p>
	 * Two ranges can be merged if they are either adjacent to or intersect with
	 * each other.
	 * </p>
	 *
	 * @param o
	 *        the range to test
	 * @return {@literal true} if this range is either adjacent to or intersects
	 *         with the given range
	 */
	public boolean canMergeWith(IntRange o) {
		return o != null && (intersects(o) || adjacentTo(o));
	}

	/**
	 * Merge this range with a given range, returning the merged range.
	 *
	 * @param o
	 *        the range to merge with this range
	 * @return the new merged range
	 * @throws IllegalArgumentException
	 *         if the this range cannot be merged with the given range
	 */
	public IntRange mergeWith(IntRange o) {
		if ( !canMergeWith(o) ) {
			throw new IllegalArgumentException("IntRange " + this + " cannot be merged with " + o);
		}
		int a = min < o.min ? min : o.min;
		int b = max > o.max ? max : o.max;
		return a == min && b == max ? this : a == o.min && b == o.max ? o : new IntRange(a, b);
	}

	/**
	 * Compares this object with the specified object for order.
	 *
	 * <p>
	 * This implementation only compares the {@code min} values of each range.
	 * </p>
	 *
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(final IntRange o) {
		return (min < o.min ? -1 : min > o.min ? 1 : 0);
	}

	@Override
	public int hashCode() {
		return Objects.hash(max, min);
	}

	@Override
	public boolean equals(final Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( !(obj instanceof IntRange) ) {
			return false;
		}
		IntRange other = (IntRange) obj;
		return max == other.max && min == other.min;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		builder.append(min);
		builder.append("..");
		builder.append(max);
		builder.append("]");
		return builder.toString();
	}

}

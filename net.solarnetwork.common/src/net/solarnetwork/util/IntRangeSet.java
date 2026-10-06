/* ==================================================================
 * IntRangeSet.java - 15/01/2020 10:47:50 am
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

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.SortedSet;
import java.util.function.IntConsumer;
import org.jspecify.annotations.Nullable;

/**
 * A {@code Set} implementation based on ordered and disjoint integer ranges.
 *
 * <p>
 * This set is optimized for integer sets where ranges of consecutive integers
 * are common. Instead of storing individual integer values, it stores an
 * ordered list of {@link IntRange} objects whose ranges do not overlap. The
 * {@link #ranges()} method can be used to get the list of ranges.
 * </p>
 *
 * <p>
 * Finding values runs in {@code O(log n)} time, where {@code n} is the number
 * of ranges. Adding or removing values can also shift the ranges that follow in
 * the underlying list.
 * </p>
 *
 * <p>
 * <b>This class is not thread-safe.</b> If multiple threads access an instance
 * concurrently, and at least one of them modifies it, then all access must be
 * synchronized externally.
 * </p>
 *
 * <p>
 * The iterators of this set and its views, and the {@code forEachOrdered()}
 * methods, are <i>fail-fast</i>: if the set is modified while iterating, in any
 * way except through the iterator's own {@code remove()} method, they throw a
 * {@link ConcurrentModificationException}. Fail-fast behavior is best-effort,
 * so it cannot be relied on to detect unsynchronized concurrent modification.
 * </p>
 *
 * @author matt
 * @version 1.3
 * @since 1.58
 */
public class IntRangeSet extends AbstractSet<Integer>
		implements NavigableSet<Integer>, IntRangeContainer, IntOrderedIterable, Cloneable {

	private final boolean immutable;
	private List<IntRange> ranges;

	/** A count of modifications, to make iteration fail-fast. */
	private int modCount;

	/**
	 * Default constructor.
	 */
	public IntRangeSet() {
		this(16);
	}

	/**
	 * Construct with an initial capacity.
	 *
	 * @param initialCapacity
	 *        the initial capacity, which refers to the number of discreet
	 *        ranges, not elements
	 */
	public IntRangeSet(int initialCapacity) {
		super();
		this.ranges = new ArrayList<>(initialCapacity);
		this.immutable = false;
	}

	/**
	 * Construct from an existing set of values.
	 *
	 * @param c
	 *        the integer collection to copy into this set
	 */
	public IntRangeSet(Collection<Integer> c) {
		this();
		addAll(c);
	}

	/**
	 * Construct from an existing set of ranges.
	 *
	 * @param ranges
	 *        the ranges to copy into this set
	 */
	public IntRangeSet(IntRange... ranges) {
		this(ranges != null ? ranges.length : 16);
		if ( ranges != null ) {
			for ( IntRange r : ranges ) {
				addRange(r.getMin(), r.getMax());
			}
		}
	}

	private IntRangeSet(List<IntRange> ranges, boolean immutable) {
		super();
		this.ranges = new ArrayList<>(ranges.size());
		this.ranges.addAll(ranges);
		this.immutable = immutable;
	}

	/**
	 * Get an immutable copy of this set.
	 *
	 * @return an immutable copy of this set
	 */
	public IntRangeSet immutableCopy() {
		if ( immutable ) {
			return this;
		}
		return new IntRangeSet(this.ranges, true);
	}

	/**
	 * Create a copy of this set.
	 *
	 * <p>
	 * The copy is immutable if this set is immutable.
	 * </p>
	 *
	 * @return the copy
	 */
	@Override
	public IntRangeSet clone() {
		final IntRangeSet s;
		try {
			s = (IntRangeSet) super.clone();
		} catch ( CloneNotSupportedException e ) {
			// should not get here
			throw new RuntimeException(e);
		}
		s.ranges = new ArrayList<>(ranges);
		return s;
	}

	@Override
	public String toString() {
		return ranges.toString();
	}

	@Override
	public boolean contains(Object o) {
		if ( !(o instanceof Integer) ) {
			return false;
		}
		return contains(((Integer) o).intValue());
	}

	@Override
	public boolean contains(int value) {
		final int idx = ceilingIndex(value);
		return (idx < ranges.size() && ranges.get(idx).getMin() <= value);
	}

	/**
	 * Find the first range whose maximum is greater than or equal to a value.
	 *
	 * @param value
	 *        the value to search for
	 * @return the index of the range, or the number of ranges if there is no
	 *         such range
	 */
	private int ceilingIndex(final long value) {
		int low = 0;
		int high = ranges.size();
		while ( low < high ) {
			final int mid = (low + high) >>> 1;
			if ( ranges.get(mid).getMax() < value ) {
				low = mid + 1;
			} else {
				high = mid;
			}
		}
		return low;
	}

	/**
	 * Find the last range whose minimum is less than or equal to a value.
	 *
	 * @param value
	 *        the value to search for
	 * @return the index of the range, or {@literal -1} if there is no such
	 *         range
	 */
	private int floorIndex(final long value) {
		int low = 0;
		int high = ranges.size();
		while ( low < high ) {
			final int mid = (low + high) >>> 1;
			if ( ranges.get(mid).getMin() <= value ) {
				low = mid + 1;
			} else {
				high = mid;
			}
		}
		return low - 1;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws ConcurrentModificationException
	 *         if this set is modified while iterating, for example by
	 *         {@code action}
	 */
	@Override
	public void forEachOrdered(IntConsumer action) {
		Objects.requireNonNull(action);
		final int mc = modCount;
		for ( int idx = 0, len = ranges.size(); modCount == mc && idx < len; idx++ ) {
			final IntRange r = ranges.get(idx);
			for ( int i = r.getMin(); modCount == mc && i <= r.getMax(); i++ ) {
				action.accept(i);
				if ( i == Integer.MAX_VALUE ) {
					// prevent overflow
					break;
				}
			}
		}
		if ( modCount != mc ) {
			throw new ConcurrentModificationException();
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws ConcurrentModificationException
	 *         if this set is modified while iterating, for example by
	 *         {@code action}
	 */
	@Override
	public void forEachOrdered(int min, int max, IntConsumer action) {
		Objects.requireNonNull(action);
		final int mc = modCount;
		for ( int idx = ceilingIndex(min), len = ranges.size(); modCount == mc && idx < len; idx++ ) {
			final IntRange r = ranges.get(idx);
			if ( max <= r.getMin() ) {
				break;
			}
			int i = r.getMin();
			if ( i < min ) {
				i = min;
			}
			for ( final int stop = max <= r.getMax() ? max - 1 : r.getMax(); modCount == mc
					&& i <= stop; i++ ) {
				action.accept(i);
				if ( i == Integer.MAX_VALUE ) {
					// prevent overflow
					break;
				}
			}
		}
		if ( modCount != mc ) {
			throw new ConcurrentModificationException();
		}
	}

	/**
	 * Add an integer to this set.
	 *
	 * {@inheritDoc}
	 *
	 * @see #add(int)
	 */
	@Override
	public boolean add(Integer e) {
		if ( immutable ) {
			throw new UnsupportedOperationException("Set is immutable.");
		}
		Objects.requireNonNull(e, "Integer cannot be null");
		return add(e.intValue());
	}

	/**
	 * Add a single integer to this set.
	 *
	 * <p>
	 * This method may merge the value into an existing range, or cause two
	 * ranges to merge together.
	 * </p>
	 *
	 * @param v
	 *        the integer to add
	 * @return {@literal true} if this set changed as a result
	 */
	public boolean add(final int v) {
		if ( immutable ) {
			throw new UnsupportedOperationException("Set is immutable.");
		}
		// the first range ending on or after v; any previous range ends before v
		final int idx = ceilingIndex(v);
		final int len = ranges.size();
		if ( idx < len && ranges.get(idx).getMin() <= v ) {
			// already in this set, nothing to do
			return false;
		}
		final boolean joinPrev = (idx > 0 && ranges.get(idx - 1).getMax() + 1 == v);
		final boolean joinNext = (idx < len && ranges.get(idx).getMin() - 1 == v);
		if ( joinPrev ) {
			final IntRange p = ranges.get(idx - 1);
			if ( joinNext ) {
				// inserting such that two existing ranges are merged
				ranges.set(idx - 1, new IntRange(p.getMin(), ranges.get(idx).getMax()));
				ranges.remove(idx);
			} else {
				// just expand prev range right
				ranges.set(idx - 1, new IntRange(p.getMin(), v));
			}
		} else if ( joinNext ) {
			// just expand next range left
			ranges.set(idx, new IntRange(v, ranges.get(idx).getMax()));
		} else {
			// insert singleton range
			ranges.add(idx, new IntRange(v, v));
		}
		modCount++;
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends Integer> col) {
		if ( immutable ) {
			throw new UnsupportedOperationException("Set is immutable.");
		}
		if ( col == null || col.isEmpty() ) {
			return false;
		}
		final int[] sorted = col.stream().mapToInt(Integer::intValue).toArray();
		Arrays.sort(sorted);
		final int len = sorted.length;
		if ( len == 1 ) {
			return add(sorted[0]);
		}
		int a = sorted[0];
		int b = a;
		boolean changed = false;
		for ( int i = 1; i < len; i++ ) {
			int c = sorted[i];
			long d = (long) c - b;
			if ( d > 1 ) {
				changed |= addRange(a, b);
				a = c;
			}
			b = c;
		}
		changed |= addRange(a, b);
		return changed;
	}

	/**
	 * Add a range of integers, inclusive.
	 *
	 * @param min
	 *        the starting value to add
	 * @param max
	 *        the last value to add
	 * @return {@literal true} if any changes resulted from adding the given
	 *         range
	 * @see #addRange(IntRange)
	 */
	public boolean addRange(final int min, final int max) {
		return addRange(new IntRange(min, max));
	}

	/**
	 * Add a range of integers, inclusive.
	 *
	 * <p>
	 * This method may merge the given range into existing ranges, or cause
	 * existing ranges to merge together.
	 * </p>
	 *
	 * @param range
	 *        the range to add
	 * @return {@literal true} if any changes resulted from adding the given
	 *         range
	 */
	public boolean addRange(IntRange range) {
		if ( immutable ) {
			throw new UnsupportedOperationException("Set is immutable.");
		}
		// the ranges from a to b, inclusive, intersect or are adjacent to range
		final int a = ceilingIndex(range.getMin() - 1L);
		final int b = floorIndex(range.getMax() + 1L);
		if ( a > b ) {
			// no overlap, just insert range
			ranges.add(a, range);
		} else {
			final IntRange first = ranges.get(a);
			if ( a == b && first.containsAll(range) ) {
				// already in this set, nothing to do
				return false;
			}
			ranges.set(a, new IntRange(Math.min(first.getMin(), range.getMin()),
					Math.max(ranges.get(b).getMax(), range.getMax())));
			if ( b > a ) {
				ranges.subList(a + 1, b + 1).clear();
			}
		}
		modCount++;
		return true;
	}

	@Override
	public void clear() {
		if ( immutable ) {
			throw new UnsupportedOperationException("Set is immutable.");
		}
		ranges.clear();
		modCount++;
	}

	@Override
	public Iterator<Integer> iterator() {
		return new IntegerIterator(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	/**
	 * Get the ranges of this set.
	 *
	 * <p>
	 * This returns an unmodifiable "live" view of the ranges in this set;
	 * mutations to this set will impact the values in the returned collection.
	 * </p>
	 *
	 * @return the disjoint ranges in this set, ordered from least to greatest,
	 *         never {@code null}
	 */
	@Override
	public Iterable<IntRange> ranges() {
		return Collections.unmodifiableList(ranges);
	}

	@Override
	public boolean isEmpty() {
		return ranges.isEmpty();
	}

	@Override
	public int size() {
		long size = 0;
		for ( IntRange r : ranges ) {
			size += (long) r.getMax() - r.getMin() + 1;
		}
		return (int) Math.min(size, Integer.MAX_VALUE);
	}

	@Override
	public @Nullable Comparator<? super Integer> comparator() {
		return null;
	}

	@Override
	public Integer first() {
		if ( ranges.isEmpty() ) {
			throw new NoSuchElementException();
		}
		return ranges.get(0).getMin();
	}

	@Override
	public Integer last() {
		if ( ranges.isEmpty() ) {
			throw new NoSuchElementException();
		}
		return ranges.get(ranges.size() - 1).getMax();
	}

	@Override
	public @Nullable Integer min() {
		return (ranges.isEmpty() ? null : ranges.get(0).getMin());
	}

	@Override
	public @Nullable Integer max() {
		return (ranges.isEmpty() ? null : ranges.get(ranges.size() - 1).getMax());
	}

	@Override
	public @Nullable Integer lower(Integer e) {
		return floorValue(e - 1L);
	}

	@Override
	public @Nullable Integer floor(Integer e) {
		return floorValue(e);
	}

	@Override
	public @Nullable Integer ceiling(Integer e) {
		return ceilingValue(e);
	}

	@Override
	public @Nullable Integer higher(Integer e) {
		return ceilingValue(e + 1L);
	}

	/**
	 * Get the greatest value in this set less than or equal to a value.
	 *
	 * @param v
	 *        the value
	 * @return the greatest value less than or equal to {@code v}, or
	 *         {@code null} if there is no such value
	 */
	private @Nullable Integer floorValue(final long v) {
		final int idx = floorIndex(v);
		return (idx < 0 ? null : (int) Math.min(ranges.get(idx).getMax(), v));
	}

	/**
	 * Get the least value in this set greater than or equal to a value.
	 *
	 * @param v
	 *        the value
	 * @return the least value greater than or equal to {@code v}, or
	 *         {@code null} if there is no such value
	 */
	private @Nullable Integer ceilingValue(final long v) {
		final int idx = ceilingIndex(v);
		return (idx < ranges.size() ? (int) Math.max(ranges.get(idx).getMin(), v) : null);
	}

	@Override
	public boolean remove(@Nullable Object o) {
		if ( immutable ) {
			throw new UnsupportedOperationException("Set is immutable.");
		}
		if ( !(o instanceof Integer) ) {
			return false;
		}
		final int v = (Integer) o;
		final int idx = ceilingIndex(v);
		if ( idx >= ranges.size() ) {
			return false;
		}
		final IntRange r = ranges.get(idx);
		if ( r.getMin() > v ) {
			return false;
		}
		if ( v == r.getMin() ) {
			if ( v < r.getMax() ) {
				// contract range from left
				ranges.set(idx, new IntRange(v + 1, r.getMax()));
			} else {
				// remove singleton range
				ranges.remove(idx);
			}
		} else if ( v == r.getMax() ) {
			// contract range from right
			ranges.set(idx, new IntRange(r.getMin(), v - 1));
		} else {
			// create hole by splitting range
			ranges.set(idx, new IntRange(r.getMin(), v - 1));
			ranges.add(idx + 1, new IntRange(v + 1, r.getMax()));
		}
		modCount++;
		return true;
	}

	@Override
	public boolean removeAll(@Nullable Collection<?> c) {
		if ( immutable ) {
			throw new UnsupportedOperationException("Set is immutable.");
		}
		if ( c == null ) {
			return false;
		}
		boolean modified = false;
		for ( Iterator<?> i = c.iterator(); i.hasNext(); ) {
			modified |= remove(i.next());
		}
		return modified;
	}

	@Override
	public @Nullable Integer pollFirst() {
		if ( immutable ) {
			throw new UnsupportedOperationException("Set is immutable.");
		}
		if ( ranges.isEmpty() ) {
			return null;
		}
		IntRange old = ranges.get(0);
		if ( old.isSingleton() ) {
			// remove singleton
			ranges.remove(0);
		} else {
			// contract from left
			ranges.set(0, new IntRange(old.getMin() + 1, old.getMax()));
		}
		modCount++;
		return old.getMin();
	}

	@Override
	public @Nullable Integer pollLast() {
		if ( immutable ) {
			throw new UnsupportedOperationException("Set is immutable.");
		}
		if ( ranges.isEmpty() ) {
			return null;
		}
		final int lastIndex = ranges.size() - 1;
		IntRange old = ranges.get(lastIndex);
		if ( old.isSingleton() ) {
			// remove singleton
			ranges.remove(lastIndex);
		} else {
			// contract from right
			ranges.set(lastIndex, new IntRange(old.getMin(), old.getMax() - 1));
		}
		modCount++;
		return old.getMax();
	}

	@Override
	public NavigableSet<Integer> descendingSet() {
		return new ReverseSet(this);
	}

	@Override
	public Iterator<Integer> descendingIterator() {
		return new IntegerReverseIterator(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Override
	public SortedSet<Integer> subSet(Integer fromElement, Integer toElement) {
		return subSet(fromElement, true, toElement, false);
	}

	@Override
	public NavigableSet<Integer> subSet(Integer fromElement, boolean fromInclusive, Integer toElement,
			boolean toInclusive) {
		if ( fromElement > toElement ) {
			throw new IllegalArgumentException("fromElement > toElement");
		}
		return new SubSet(false, fromElement, fromInclusive, false, toElement, toInclusive);
	}

	@Override
	public NavigableSet<Integer> headSet(Integer toElement, boolean inclusive) {
		return new SubSet(true, 0, true, false, toElement, inclusive);
	}

	@Override
	public NavigableSet<Integer> tailSet(Integer fromElement, boolean inclusive) {
		return new SubSet(false, fromElement, inclusive, true, 0, true);
	}

	@Override
	public SortedSet<Integer> headSet(Integer toElement) {
		return headSet(toElement, false);
	}

	@Override
	public SortedSet<Integer> tailSet(Integer fromElement) {
		return tailSet(fromElement, true);
	}

	/**
	 * Remove a range of integers, inclusive.
	 *
	 * @param min
	 *        the first value to remove
	 * @param max
	 *        the last value to remove
	 * @return {@literal true} if any values were removed
	 */
	private boolean removeRange(final long min, final long max) {
		if ( immutable ) {
			throw new UnsupportedOperationException("Set is immutable.");
		}
		if ( min > max ) {
			return false;
		}
		// the ranges from a to b, inclusive, intersect the range to remove
		final int a = ceilingIndex(min);
		final int b = floorIndex(max);
		if ( a > b ) {
			return false;
		}
		final IntRange first = ranges.get(a);
		final IntRange last = ranges.get(b);
		ranges.subList(a, b + 1).clear();
		if ( last.getMax() > max ) {
			// keep the part of the last range after max
			ranges.add(a, new IntRange((int) (max + 1), last.getMax()));
		}
		if ( first.getMin() < min ) {
			// keep the part of the first range before min
			ranges.add(a, new IntRange(first.getMin(), (int) (min - 1)));
		}
		modCount++;
		return true;
	}

	/**
	 * Iterate over the values of this set within a range, in ascending order.
	 */
	private class IntegerIterator implements Iterator<Integer> {

		private final long max;
		private int index;
		private long next;
		private long stop;
		private boolean canRemove;
		private int lastReturned;
		private int expectedModCount = modCount;

		private IntegerIterator(long min, long max) {
			super();
			this.max = max;
			this.index = ceilingIndex(min);
			if ( index < ranges.size() ) {
				IntRange r = ranges.get(index);
				next = Math.max(r.getMin(), min);
				stop = Math.min(r.getMax(), max);
			} else {
				next = 1;
				stop = 0;
			}
		}

		@Override
		public boolean hasNext() {
			return next <= stop;
		}

		@Override
		public Integer next() {
			if ( modCount != expectedModCount ) {
				throw new ConcurrentModificationException();
			}
			if ( next > stop ) {
				throw new NoSuchElementException();
			}
			final int n = (int) next;
			next++;
			if ( next > stop && ++index < ranges.size() ) {
				IntRange r = ranges.get(index);
				next = r.getMin();
				stop = Math.min(r.getMax(), max);
			}
			lastReturned = n;
			canRemove = true;
			return n;
		}

		@Override
		public void remove() {
			if ( !canRemove ) {
				throw new IllegalStateException();
			}
			if ( modCount != expectedModCount ) {
				throw new ConcurrentModificationException();
			}
			IntRangeSet.this.remove(lastReturned);
			canRemove = false;
			expectedModCount = modCount;
			if ( next <= stop ) {
				// removing may have split or removed a range, shifting the next range
				index = ceilingIndex(next);
			}
		}

	}

	/**
	 * Iterate over the values of this set within a range, in descending order.
	 */
	private class IntegerReverseIterator implements Iterator<Integer> {

		private final long min;
		private int index;
		private long next;
		private long stop;
		private boolean canRemove;
		private int lastReturned;
		private int expectedModCount = modCount;

		private IntegerReverseIterator(long min, long max) {
			super();
			this.min = min;
			this.index = floorIndex(max);
			if ( index >= 0 ) {
				IntRange r = ranges.get(index);
				next = Math.min(r.getMax(), max);
				stop = Math.max(r.getMin(), min);
			} else {
				next = 0;
				stop = 1;
			}
		}

		@Override
		public boolean hasNext() {
			return next >= stop;
		}

		@Override
		public Integer next() {
			if ( modCount != expectedModCount ) {
				throw new ConcurrentModificationException();
			}
			if ( next < stop ) {
				throw new NoSuchElementException();
			}
			final int n = (int) next;
			next--;
			if ( next < stop && --index >= 0 ) {
				IntRange r = ranges.get(index);
				next = r.getMax();
				stop = Math.max(r.getMin(), min);
			}
			lastReturned = n;
			canRemove = true;
			return n;
		}

		@Override
		public void remove() {
			if ( !canRemove ) {
				throw new IllegalStateException();
			}
			if ( modCount != expectedModCount ) {
				throw new ConcurrentModificationException();
			}
			IntRangeSet.this.remove(lastReturned);
			canRemove = false;
			expectedModCount = modCount;
			if ( next >= stop ) {
				// removing may have split or removed a range, shifting the next range
				index = floorIndex(next);
			}
		}

	}

	/**
	 * An ascending view of the values of this set within a range.
	 *
	 * <p>
	 * The range bounds follow the same rules as {@link java.util.TreeSet}
	 * views.
	 * </p>
	 */
	private final class SubSet extends AbstractSet<Integer> implements NavigableSet<Integer> {

		private final boolean fromStart;
		private final int lo;
		private final boolean loInclusive;
		private final boolean toEnd;
		private final int hi;
		private final boolean hiInclusive;

		/** The minimum value in range, inclusive. */
		private final long min;

		/** The maximum value in range, inclusive. */
		private final long max;

		private SubSet(boolean fromStart, int lo, boolean loInclusive, boolean toEnd, int hi,
				boolean hiInclusive) {
			super();
			this.fromStart = fromStart;
			this.lo = lo;
			this.loInclusive = loInclusive;
			this.toEnd = toEnd;
			this.hi = hi;
			this.hiInclusive = hiInclusive;
			this.min = (fromStart ? Integer.MIN_VALUE : loInclusive ? lo : lo + 1L);
			this.max = (toEnd ? Integer.MAX_VALUE : hiInclusive ? hi : hi - 1L);
		}

		private boolean inRange(long v) {
			return (v >= min && v <= max);
		}

		private boolean inClosedRange(int v) {
			return (fromStart || v >= lo) && (toEnd || v <= hi);
		}

		private boolean inRange(int v, boolean inclusive) {
			return (inclusive ? inRange(v) : inClosedRange(v));
		}

		private @Nullable Integer floorWithin(long v) {
			final long bound = Math.min(v, max);
			if ( bound < min ) {
				return null;
			}
			Integer result = IntRangeSet.this.floorValue(bound);
			return (result != null && result >= min ? result : null);
		}

		private @Nullable Integer ceilingWithin(long v) {
			final long bound = Math.max(v, min);
			if ( bound > max ) {
				return null;
			}
			Integer result = IntRangeSet.this.ceilingValue(bound);
			return (result != null && result <= max ? result : null);
		}

		@Override
		public @Nullable Comparator<? super Integer> comparator() {
			return null;
		}

		@Override
		public boolean contains(@Nullable Object o) {
			if ( !(o instanceof Integer) ) {
				return false;
			}
			final int v = (Integer) o;
			return (inRange(v) && IntRangeSet.this.contains(v));
		}

		@Override
		public boolean add(Integer e) {
			if ( !inRange(e.intValue()) ) {
				throw new IllegalArgumentException("Value out of range");
			}
			return IntRangeSet.this.add(e.intValue());
		}

		@Override
		public boolean remove(@Nullable Object o) {
			if ( !(o instanceof Integer) ) {
				return false;
			}
			final int v = (Integer) o;
			return (inRange(v) && IntRangeSet.this.remove(o));
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			boolean modified = false;
			for ( Object o : c ) {
				modified |= remove(o);
			}
			return modified;
		}

		@Override
		public void clear() {
			IntRangeSet.this.removeRange(min, max);
		}

		@Override
		public boolean isEmpty() {
			return (ceilingWithin(min) == null);
		}

		@Override
		public int size() {
			if ( min > max ) {
				return 0;
			}
			long size = 0;
			for ( int idx = ceilingIndex(min), len = ranges.size(); idx < len; idx++ ) {
				final IntRange r = ranges.get(idx);
				if ( r.getMin() > max ) {
					break;
				}
				size += Math.min(r.getMax(), max) - Math.max(r.getMin(), min) + 1;
			}
			return (int) Math.min(size, Integer.MAX_VALUE);
		}

		@Override
		public Iterator<Integer> iterator() {
			return new IntegerIterator(min, max);
		}

		@Override
		public Iterator<Integer> descendingIterator() {
			return new IntegerReverseIterator(min, max);
		}

		@Override
		public NavigableSet<Integer> descendingSet() {
			return new ReverseSet(this);
		}

		@Override
		public Integer first() {
			Integer result = ceilingWithin(min);
			if ( result == null ) {
				throw new NoSuchElementException();
			}
			return result;
		}

		@Override
		public Integer last() {
			Integer result = floorWithin(max);
			if ( result == null ) {
				throw new NoSuchElementException();
			}
			return result;
		}

		@Override
		public @Nullable Integer lower(Integer e) {
			return floorWithin(e - 1L);
		}

		@Override
		public @Nullable Integer floor(Integer e) {
			return floorWithin(e);
		}

		@Override
		public @Nullable Integer ceiling(Integer e) {
			return ceilingWithin(e);
		}

		@Override
		public @Nullable Integer higher(Integer e) {
			return ceilingWithin(e + 1L);
		}

		@Override
		public @Nullable Integer pollFirst() {
			Integer result = ceilingWithin(min);
			if ( result != null ) {
				IntRangeSet.this.remove(result);
			}
			return result;
		}

		@Override
		public @Nullable Integer pollLast() {
			Integer result = floorWithin(max);
			if ( result != null ) {
				IntRangeSet.this.remove(result);
			}
			return result;
		}

		@Override
		public SortedSet<Integer> subSet(Integer fromElement, Integer toElement) {
			return subSet(fromElement, true, toElement, false);
		}

		@Override
		public NavigableSet<Integer> subSet(Integer fromElement, boolean fromInclusive,
				Integer toElement, boolean toInclusive) {
			if ( !inRange(fromElement, fromInclusive) ) {
				throw new IllegalArgumentException("fromElement out of range");
			}
			if ( !inRange(toElement, toInclusive) ) {
				throw new IllegalArgumentException("toElement out of range");
			}
			if ( fromElement > toElement ) {
				throw new IllegalArgumentException("fromElement > toElement");
			}
			return new SubSet(false, fromElement, fromInclusive, false, toElement, toInclusive);
		}

		@Override
		public NavigableSet<Integer> headSet(Integer toElement, boolean inclusive) {
			if ( !inRange(toElement, inclusive) ) {
				throw new IllegalArgumentException("toElement out of range");
			}
			return new SubSet(fromStart, lo, loInclusive, false, toElement, inclusive);
		}

		@Override
		public NavigableSet<Integer> tailSet(Integer fromElement, boolean inclusive) {
			if ( !inRange(fromElement, inclusive) ) {
				throw new IllegalArgumentException("fromElement out of range");
			}
			return new SubSet(false, fromElement, inclusive, toEnd, hi, hiInclusive);
		}

		@Override
		public SortedSet<Integer> headSet(Integer toElement) {
			return headSet(toElement, false);
		}

		@Override
		public SortedSet<Integer> tailSet(Integer fromElement) {
			return tailSet(fromElement, true);
		}

	}

	/**
	 * A descending view of an ascending set.
	 */
	private static final class ReverseSet extends AbstractSet<Integer> implements NavigableSet<Integer> {

		private final NavigableSet<Integer> delegate;

		private ReverseSet(NavigableSet<Integer> delegate) {
			super();
			this.delegate = delegate;
		}

		@Override
		public Comparator<? super Integer> comparator() {
			return Collections.reverseOrder();
		}

		@Override
		public boolean add(Integer e) {
			return delegate.add(e);
		}

		@Override
		public boolean addAll(Collection<? extends Integer> col) {
			return delegate.addAll(col);
		}

		@Override
		public boolean contains(@Nullable Object o) {
			return delegate.contains(o);
		}

		@Override
		public boolean remove(@Nullable Object o) {
			return delegate.remove(o);
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			return delegate.removeAll(c);
		}

		@Override
		public void clear() {
			delegate.clear();
		}

		@Override
		public Integer first() {
			return delegate.last();
		}

		@Override
		public Integer last() {
			return delegate.first();
		}

		@Override
		public @Nullable Integer lower(Integer e) {
			return delegate.higher(e);
		}

		@Override
		public @Nullable Integer floor(Integer e) {
			return delegate.ceiling(e);
		}

		@Override
		public @Nullable Integer ceiling(Integer e) {
			return delegate.floor(e);
		}

		@Override
		public @Nullable Integer higher(Integer e) {
			return delegate.lower(e);
		}

		@Override
		public @Nullable Integer pollFirst() {
			return delegate.pollLast();
		}

		@Override
		public @Nullable Integer pollLast() {
			return delegate.pollFirst();
		}

		@Override
		public NavigableSet<Integer> descendingSet() {
			return delegate;
		}

		@Override
		public Iterator<Integer> descendingIterator() {
			return delegate.iterator();
		}

		@Override
		public SortedSet<Integer> subSet(Integer fromElement, Integer toElement) {
			return subSet(fromElement, true, toElement, false);
		}

		@Override
		public NavigableSet<Integer> subSet(Integer fromElement, boolean fromInclusive,
				Integer toElement, boolean toInclusive) {
			return new ReverseSet(delegate.subSet(toElement, toInclusive, fromElement, fromInclusive));
		}

		@Override
		public NavigableSet<Integer> headSet(Integer toElement, boolean inclusive) {
			return new ReverseSet(delegate.tailSet(toElement, inclusive));
		}

		@Override
		public NavigableSet<Integer> tailSet(Integer fromElement, boolean inclusive) {
			return new ReverseSet(delegate.headSet(fromElement, inclusive));
		}

		@Override
		public SortedSet<Integer> headSet(Integer toElement) {
			return headSet(toElement, false);
		}

		@Override
		public SortedSet<Integer> tailSet(Integer fromElement) {
			return tailSet(fromElement, true);
		}

		@Override
		public Iterator<Integer> iterator() {
			return delegate.descendingIterator();
		}

		@Override
		public boolean isEmpty() {
			return delegate.isEmpty();
		}

		@Override
		public int size() {
			return delegate.size();
		}

	}

}

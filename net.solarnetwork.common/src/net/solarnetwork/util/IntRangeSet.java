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
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.SortedSet;
import java.util.function.IntConsumer;

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
 * @author matt
 * @version 1.2
 * @since 1.58
 */
public class IntRangeSet extends AbstractSet<Integer>
		implements NavigableSet<Integer>, IntRangeContainer, IntOrderedIterable, Cloneable {

	private final boolean immutable;
	private final List<IntRange> ranges;

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

	@Override
	public Object clone() {
		return new IntRangeSet(ranges, immutable);
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
		for ( IntRange r : ranges ) {
			if ( r.contains(value) ) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void forEachOrdered(IntConsumer action) {
		Objects.requireNonNull(action);
		for ( IntRange r : ranges ) {
			for ( int i = r.getMin(); i <= r.getMax(); i++ ) {
				action.accept(i);
				if ( i == Integer.MAX_VALUE ) {
					// prevent overflow
					return;
				}
			}
		}
	}

	@Override
	public void forEachOrdered(int min, int max, IntConsumer action) {
		Objects.requireNonNull(action);
		for ( IntRange r : ranges ) {
			if ( min > r.getMax() ) {
				continue;
			} else if ( max <= r.getMin() ) {
				break;
			}
			int i = r.getMin();
			if ( i < min ) {
				i = min;
			}
			for ( final int stop = max <= r.getMax() ? max - 1 : r.getMax(); i <= stop; i++ ) {
				action.accept(i);
				if ( i == Integer.MAX_VALUE ) {
					// prevent overflow
					return;
				}
			}
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
			throw new UnsupportedOperationException("Set it immutable.");
		}
		if ( e == null ) {
			throw new IllegalArgumentException("Integer cannot be null");
		}
		return add(e.intValue());
	}

	/**
	 * Add a single integer to this set.
	 *
	 * <p>
	 * This method requires a linear search of all existing discreet ranges to
	 * maintain ordering and possibly merge the value into an existing range or
	 * cause two ranges to merge together.
	 * </p>
	 *
	 * @param v
	 *        the integer to add
	 * @return {@literal true} if this set changed as a result
	 */
	public boolean add(final int v) {
		if ( immutable ) {
			throw new UnsupportedOperationException("Set it immutable.");
		}
		IntRange p = null;
		boolean changed = false;
		if ( ranges.isEmpty() ) {
			// first range to add
			ranges.add(new IntRange(v, v));
			changed = true;
		} else {
			for ( ListIterator<IntRange> itr = ranges.listIterator(); itr.hasNext(); ) {
				IntRange r = itr.next();
				if ( r.contains(v) ) {
					// already in this set, nothing to do
					return false;
				} else if ( v < r.getMin() ) {
					if ( v + 1 == r.getMin() ) {
						// new value adjacent to curr minimum; expand curr minimum - 1
						if ( p != null && v - 1 == p.getMax() ) {
							// inserting such that two existing ranges are merged
							itr.remove();
							ranges.set(itr.previousIndex(), new IntRange(p.getMin(), r.getMax()));
						} else {
							// just expand curr range left
							itr.set(new IntRange(v, r.getMax()));
						}
					} else if ( p != null && v - 1 == p.getMax() ) {
						// just expand prev range right
						ranges.set(itr.previousIndex() - 1, new IntRange(p.getMin(), v));
					} else {
						// insert singleton range before curr
						ranges.add(itr.previousIndex(), new IntRange(v, v));
					}
					changed = true;
					break;
				}
				p = r;
			}
			if ( !changed ) {
				// append to end
				if ( p != null && v - 1 == p.getMax() ) {
					// just expand the last range
					ranges.set(ranges.size() - 1, new IntRange(p.getMin(), v));
				} else {
					ranges.add(new IntRange(v, v));
				}
				changed = true;
			}
		}
		return changed;
	}

	@Override
	public boolean addAll(Collection<? extends Integer> col) {
		if ( immutable ) {
			throw new UnsupportedOperationException("Set it immutable.");
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
			int d = c - b;
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
	 * This method requires a linear search of all existing discreet ranges to
	 * maintain ordering and possibly merge the given range into an existing
	 * range or cause existing ranges to merge together.
	 * </p>
	 *
	 * @param range
	 *        the range to add
	 * @return {@literal true} if any changes resulted from adding the given
	 *         range
	 */
	public boolean addRange(IntRange range) {
		if ( immutable ) {
			throw new UnsupportedOperationException("Set it immutable.");
		}
		boolean changed = false;
		if ( ranges.isEmpty() ) {
			// first range to add
			ranges.add(range);
			changed = true;
		} else {
			for ( ListIterator<IntRange> itr = ranges.listIterator(); itr.hasNext(); ) {
				IntRange r = itr.next();
				if ( r.containsAll(range) ) {
					// already in this set, nothing to do
					return false;
				} else if ( r.canMergeWith(range) ) {
					IntRange merged = r.mergeWith(range);

					// try to expand right as far as possible
					while ( itr.hasNext() ) {
						IntRange n = itr.next();
						if ( merged.canMergeWith(n) ) {
							merged = merged.mergeWith(n);
							itr.remove();
						} else {
							// back up to last merged
							itr.previous();
							break;
						}
					}
					ranges.set(itr.previousIndex(), merged);
					changed = true;
					break;
				} else if ( range.getMin() < r.getMin() ) {
					// no overlap, just insert range before curr
					ranges.add(itr.previousIndex(), range);
					changed = true;
					break;
				}
			}
			if ( !changed ) {
				// append to end
				ranges.add(range);
				changed = true;
			}
		}
		return changed;
	}

	@Override
	public void clear() {
		if ( immutable ) {
			throw new UnsupportedOperationException("Set it immutable.");
		}
		ranges.clear();
	}

	@Override
	public Iterator<Integer> iterator() {
		return new IntegerIterator();
	}

	/**
	 * Get the ranges of this set.
	 *
	 * <p>
	 * This returns a "live" reference to the ranges in this set; mutations to
	 * this set will impact the values in the returned collection.
	 * </p>
	 *
	 * @return the disjoint ranges in this set, ordered from least to greatest,
	 *         never {@literal null}
	 */
	@Override
	public Iterable<IntRange> ranges() {
		return ranges;
	}

	@Override
	public boolean isEmpty() {
		return ranges.isEmpty();
	}

	@Override
	public int size() {
		return ranges.stream().mapToInt(IntRange::length).sum();
	}

	@Override
	public Comparator<? super Integer> comparator() {
		return null;
	}

	@Override
	public Integer first() {
		IntRange r = (ranges.isEmpty() ? null : ranges.get(0));
		return (r != null ? r.getMin() : null);
	}

	@Override
	public Integer last() {
		IntRange r = (ranges.isEmpty() ? null : ranges.get(ranges.size() - 1));
		return (r != null ? r.getMax() : null);
	}

	@Override
	public Integer min() {
		// TODO Auto-generated method stub
		return first();
	}

	@Override
	public Integer max() {
		return last();
	}

	@Override
	public Integer lower(Integer e) {
		final int v = e;
		for ( ListIterator<IntRange> itr = ranges.listIterator(); itr.hasNext(); ) {
			IntRange r = itr.next();
			if ( r.contains(v) || r.getMin() >= v ) {
				if ( v > r.getMin() ) {
					// current range starts _before_ e, so can return e - 1
					return v - 1;
				} else if ( itr.previousIndex() > 0 ) {
					// current range starts _on_ or _after_ e, so return previous range max
					return ranges.get(itr.previousIndex() - 1).getMax();
				} else {
					// there is no lower value available
					break;
				}
			} else if ( r.getMax() < v && !itr.hasNext() ) {
				// last element's max is lower than e, return that
				return r.getMax();
			}
		}
		return null;
	}

	@Override
	public Integer floor(Integer e) {
		final int v = e;
		for ( ListIterator<IntRange> itr = ranges.listIterator(); itr.hasNext(); ) {
			IntRange r = itr.next();
			if ( r.contains(v) ) {
				// current range contains e, so return e
				return v;
			} else if ( r.getMin() > v ) {
				if ( itr.previousIndex() > 0 ) {
					// current range starts _after_ e, so return previous range max
					return ranges.get(itr.previousIndex() - 1).getMax();
				} else {
					// no lower element
					break;
				}
			} else if ( r.getMax() < v && !itr.hasNext() ) {
				// last element's max is lower than e, return that
				return r.getMax();
			}
		}
		return null;
	}

	@Override
	public Integer ceiling(Integer e) {
		final int v = e;
		final int len = ranges.size();
		for ( ListIterator<IntRange> itr = ranges.listIterator(len); itr.hasPrevious(); ) {
			IntRange r = itr.previous();
			if ( r.contains(v) ) {
				// current range contains e, so return e
				return v;
			} else if ( r.getMax() < v ) {
				if ( itr.nextIndex() + 1 < len ) {
					// current range ends _before_ e, so return next range min
					return ranges.get(itr.nextIndex() + 1).getMin();
				} else {
					// no higher element
					break;
				}
			} else if ( r.getMin() > v && !itr.hasPrevious() ) {
				// first element's min is higher than e, return that
				return r.getMin();
			}
		}
		return null;
	}

	@Override
	public Integer higher(Integer e) {
		final int v = e;
		final int len = ranges.size();
		for ( ListIterator<IntRange> itr = ranges.listIterator(len); itr.hasPrevious(); ) {
			IntRange r = itr.previous();
			if ( r.contains(v) || r.getMax() <= v ) {
				if ( v < r.getMax() ) {
					// current range ends _before_ e, so can return e + 1
					return v + 1;
				} else if ( itr.nextIndex() + 1 < len ) {
					// current range ends _on_ or _before_ e, so return next range min
					return ranges.get(itr.nextIndex() + 1).getMin();
				} else {
					// there is no higher value available
					break;
				}
			} else if ( r.getMin() > v && !itr.hasPrevious() ) {
				// first element's min is higher than e, return that
				return r.getMin();
			}
		}
		return null;
	}

	@Override
	public boolean remove(Object o) {
		if ( immutable ) {
			throw new UnsupportedOperationException("Set it immutable.");
		}
		if ( !(o instanceof Integer) ) {
			return false;
		}
		final int v = (Integer) o;
		for ( ListIterator<IntRange> itr = ranges.listIterator(); itr.hasNext(); ) {
			IntRange r = itr.next();
			if ( r.contains(v) ) {
				if ( v == r.getMin() ) {
					if ( v < r.getMax() ) {
						// contract range from left
						itr.set(new IntRange(v + 1, r.getMax()));
					} else {
						// remove singleton range
						itr.remove();
					}
				} else if ( v == r.getMax() ) {
					// contract range from right
					itr.set(new IntRange(r.getMin(), v - 1));
				} else {
					// create hole by splitting range
					itr.set(new IntRange(r.getMin(), v - 1));
					ranges.add(itr.nextIndex(), new IntRange(v + 1, r.getMax()));
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		if ( immutable ) {
			throw new UnsupportedOperationException("Set it immutable.");
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
	public Integer pollFirst() {
		if ( immutable ) {
			throw new UnsupportedOperationException("Set it immutable.");
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
		return old.getMin();
	}

	@Override
	public Integer pollLast() {
		if ( immutable ) {
			throw new UnsupportedOperationException("Set it immutable.");
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
		return old.getMax();
	}

	@Override
	public NavigableSet<Integer> descendingSet() {
		return new ReverseSet(this);
	}

	@Override
	public Iterator<Integer> descendingIterator() {
		return new IntegerReverseIterator();
	}

	@Override
	public SortedSet<Integer> subSet(Integer fromElement, Integer toElement) {
		return subSet(fromElement, true, toElement, false);
	}

	@Override
	public NavigableSet<Integer> subSet(Integer fromElement, boolean fromInclusive, Integer toElement,
			boolean toInclusive) {
		throw new UnsupportedOperationException();
	}

	@Override
	public NavigableSet<Integer> headSet(Integer toElement, boolean inclusive) {
		throw new UnsupportedOperationException();
	}

	@Override
	public NavigableSet<Integer> tailSet(Integer fromElement, boolean inclusive) {
		throw new UnsupportedOperationException();
	}

	@Override
	public SortedSet<Integer> headSet(Integer toElement) {
		return headSet(toElement, false);
	}

	@Override
	public SortedSet<Integer> tailSet(Integer fromElement) {
		return tailSet(fromElement, true);
	}

	private class IntegerIterator implements Iterator<Integer> {

		private final Iterator<IntRange> rangeItr;
		private IntRange curr;
		private int next;

		private IntegerIterator() {
			super();
			rangeItr = ranges.iterator();
			if ( rangeItr.hasNext() ) {
				curr = rangeItr.next();
				next = curr.getMin();
			} else {
				curr = null;
			}
		}

		private IntegerIterator(int min, int max) {
			super();
			rangeItr = ranges.iterator();
			next = min;
			while ( rangeItr.hasNext() ) {
				curr = rangeItr.next();
				if ( curr.contains(min) ) {
					break;
				}
			}
			if ( !curr.contains(min) ) {
				curr = null;
			}
		}

		@Override
		public boolean hasNext() {
			return (curr != null && (next <= curr.getMax() || rangeItr.hasNext()));
		}

		@Override
		public Integer next() {
			if ( curr == null ) {
				throw new NoSuchElementException();
			}
			int n = next;
			next++;
			if ( !curr.contains(next) ) {
				if ( rangeItr.hasNext() ) {
					curr = rangeItr.next();
					next = curr.getMin();
				} else {
					curr = null;
				}
			}
			return n;
		}

	}

	private class IntegerReverseIterator implements Iterator<Integer> {

		private final ListIterator<IntRange> rangeItr;
		private IntRange curr;
		private int next;

		private IntegerReverseIterator() {
			super();
			rangeItr = ranges.listIterator(ranges.size());
			if ( rangeItr.hasPrevious() ) {
				curr = rangeItr.previous();
				next = curr.getMax();
			} else {
				curr = null;
			}
		}

		private IntegerReverseIterator(int min, int max) {
			super();
			rangeItr = ranges.listIterator(ranges.size());
			next = min;
			while ( rangeItr.hasPrevious() ) {
				curr = rangeItr.previous();
				if ( curr.contains(max) ) {
					break;
				}
			}
			if ( !curr.contains(max) ) {
				curr = null;
			}
		}

		@Override
		public boolean hasNext() {
			return (curr != null && (next >= curr.getMin() || rangeItr.hasPrevious()));
		}

		@Override
		public Integer next() {
			if ( curr == null ) {
				throw new NoSuchElementException();
			}
			int n = next;
			next--;
			if ( !curr.contains(next) ) {
				if ( rangeItr.hasPrevious() ) {
					curr = rangeItr.previous();
					next = curr.getMax();
				} else {
					curr = null;
				}
			}
			return n;
		}

	}

	private static class ReverseSet extends AbstractSet<Integer> implements NavigableSet<Integer> {

		private final IntRangeSet delegate;

		private ReverseSet(IntRangeSet delegate) {
			super();
			this.delegate = delegate;
		}

		@Override
		public Comparator<? super Integer> comparator() {
			return null;
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
		public Integer lower(Integer e) {
			return delegate.higher(e);
		}

		@Override
		public Integer floor(Integer e) {
			return delegate.ceiling(e);
		}

		@Override
		public Integer ceiling(Integer e) {
			return delegate.floor(e);
		}

		@Override
		public Integer higher(Integer e) {
			return delegate.lower(e);
		}

		@Override
		public Integer pollFirst() {
			return delegate.pollLast();
		}

		@Override
		public Integer pollLast() {
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
			throw new UnsupportedOperationException();
		}

		@Override
		public NavigableSet<Integer> headSet(Integer toElement, boolean inclusive) {
			return delegate.tailSet(toElement, inclusive);
		}

		@Override
		public NavigableSet<Integer> tailSet(Integer fromElement, boolean inclusive) {
			return delegate.headSet(fromElement, inclusive);
		}

		@Override
		public SortedSet<Integer> headSet(Integer toElement) {
			return delegate.tailSet(toElement);
		}

		@Override
		public SortedSet<Integer> tailSet(Integer fromElement) {
			return delegate.headSet(fromElement);
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

/* ==================================================================
 * ByteList.java - 25/01/2020 8:15:07 am
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

import java.util.AbstractList;
import java.util.Collection;

/**
 * A list of byte primitives.
 * 
 * <p>
 * The {@link ByteOrderedIterable} API has been adopted by this class such that
 * iteration ordering is the same as array ordering. That is, order is by array
 * indexes, not array values.
 * </p>
 * 
 * <p>
 * This class has been adapted from the
 * <a href="http://trove4j.sourceforge.net/html/overview.html">GNU Trove</a>
 * project's {@code gnu.trove.list.array.TByteArrayList} class, which is
 * released under the LGPL 2.1 licence.
 * </p>
 * 
 * @author matt
 * @version 1.0
 * @since 1.58
 */
public class ByteList extends AbstractList<Byte> implements ByteOrderedIterable, Cloneable {

	/** The default capacity value. */
	public static final int DEFAULT_CAPACITY = 16;

	/** The default "null" value. */
	public static final byte DEFAULT_NULL_VALUE = 0;

	private final byte nullValue;
	private byte[] data;
	private int size;

	/**
	 * Default constructor.
	 * 
	 * <p>
	 * The {@link #DEFAULT_CAPACITY} and {@link #DEFAULT_NULL_VALUE} values will
	 * be used.
	 * </p>
	 */
	public ByteList() {
		this(DEFAULT_CAPACITY, DEFAULT_NULL_VALUE);
	}

	/**
	 * Constructor.
	 * 
	 * <p>
	 * The {@link #DEFAULT_NULL_VALUE} will be used.
	 * </p>
	 *
	 * @param capacity
	 *        the initial capacity
	 */
	public ByteList(int capacity) {
		this(capacity, DEFAULT_NULL_VALUE);
	}

	/**
	 * Creates a new <code>TByteArrayList</code> instance with the specified
	 * capacity.
	 *
	 * @param capacity
	 *        the initial capacity
	 * @param nullValue
	 *        the value that represents {@literal null}
	 */
	public ByteList(int capacity, byte nullValue) {
		super();
		data = new byte[capacity];
		size = 0;
		this.nullValue = nullValue;
	}

	/**
	 * Constructor.
	 *
	 * <p>
	 * The {@link #DEFAULT_NULL_VALUE} will be used.
	 * </p>
	 * 
	 * @param values
	 *        the initial content to add to this list; the values are copied
	 */
	public ByteList(byte[] values) {
		this(values, DEFAULT_NULL_VALUE);
	}

	/**
	 * Constructor.
	 *
	 * @param values
	 *        the initial content to add to this list; the values are copied
	 * @param nullValue
	 *        the value that represents {@literal null}
	 */
	public ByteList(byte[] values, byte nullValue) {
		super();
		data = values.clone();
		size = values.length;
		this.nullValue = nullValue;
	}

	/**
	 * Copy constructor.
	 * 
	 * @param other
	 *        the list to copy
	 */
	public ByteList(Collection<Byte> other) {
		super();
		if ( other instanceof ByteList ) {
			ByteList l = (ByteList) other;
			data = l.data.clone();
			size = l.size;
			nullValue = l.nullValue;
		} else {
			data = new byte[other.size()];
			size = 0;
			nullValue = DEFAULT_NULL_VALUE;
			for ( Byte b : other ) {
				if ( b == null ) {
					add(nullValue);
				} else {
					add(b.byteValue());
				}
			}
		}
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new ByteList(data, nullValue);
	}

	private void ensureCapacity(int capacity) {
		if ( capacity > data.length ) {
			int newCap = Math.max(data.length << 1, capacity);
			byte[] tmp = new byte[newCap];
			System.arraycopy(data, 0, tmp, 0, data.length);
			data = tmp;
		}
	}

	@Override
	public void forEachOrdered(ByteConsumer action) {
		forEachOrdered(0, size, action);
	}

	@Override
	public void forEachOrdered(int min, int max, ByteConsumer action) {
		if ( min < 0 || min > size ) {
			throw new ArrayIndexOutOfBoundsException(min);
		}
		if ( max < 0 || max > size ) {
			throw new ArrayIndexOutOfBoundsException(max);
		}
		for ( int i = min; i < max; i++ ) {
			action.accept(data[i]);
		}
	}

	@Override
	public int size() {
		return size;
	}

	/**
	 * Get the current capacity.
	 * 
	 * @return the capacity
	 */
	public int getCapacity() {
		return data.length;
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	@Override
	public Byte get(int index) {
		return Byte.valueOf(getValue(index));
	}

	/**
	 * Get the byte at a given index.
	 * 
	 * @param index
	 *        the index of the byte to get
	 * @return the byte
	 * @throws ArrayIndexOutOfBoundsException
	 *         if {@code index} is out of bounds
	 */
	public byte getValue(int index) {
		if ( index < 0 || index >= size ) {
			throw new ArrayIndexOutOfBoundsException(index);
		}
		return data[index];
	}

	/**
	 * Get the byte value used for "null".
	 * 
	 * @return the null value
	 */
	public byte getNullValue() {
		return nullValue;
	}

	@Override
	public boolean add(Byte e) {
		return add(e == null ? nullValue : e.byteValue());
	}

	/**
	 * Add a byte.
	 * 
	 * @param b
	 *        the byte to add
	 * @return {@literal true} if the byte was added
	 */
	public boolean add(byte b) {
		ensureCapacity(size + 1);
		data[size++] = b;
		return true;
	}

	@Override
	public void add(int index, Byte element) {
		addAll(index, new byte[] { element == null ? nullValue : element.byteValue() }, 0, 1);
	}

	@Override
	public boolean addAll(int index, Collection<? extends Byte> c) {
		if ( c instanceof ByteList ) {
			return addAll(index, ((ByteList) c).data, 0, c.size());
		}
		byte[] vals = new byte[c.size()];
		int i = 0;
		for ( Byte b : c ) {
			vals[i++] = (b == null ? nullValue : b.byteValue());
		}
		return addAll(index, vals, 0, vals.length);
	}

	/**
	 * Add values from a byte array to this list.
	 * 
	 * @param index
	 *        the index to insert the copied bytes at; all existing elements
	 *        starting at this index will be shifted to the right
	 * @param src
	 *        the array to copy bytes from
	 * @param srcPos
	 *        the position in {@code src} to start copying from
	 * @param length
	 *        the number of bytes to copy
	 * @return {@literal true} if all bytes were copied
	 */
	public boolean addAll(int index, byte[] src, int srcPos, int length) {
		ensureCapacity(size + length);
		System.arraycopy(data, index, data, index + length, length);
		System.arraycopy(src, srcPos, data, index, length);
		size += length;
		return true;
	}

	/**
	 * Add an array of bytes.
	 * 
	 * @param src
	 *        the bytes to add
	 */
	public void addAll(byte[] src) {
		add(src, 0, src.length);
	}

	/**
	 * Add an array of bytes.
	 * 
	 * @param src
	 *        the array to copy bytes from
	 * @param srcPos
	 *        the position within {@code vals} to start copying from
	 * @param length
	 *        the number of bytes to copy
	 */
	public void add(byte[] src, int srcPos, int length) {
		ensureCapacity(size + length);
		System.arraycopy(src, srcPos, data, size, length);
		size += length;
	}

	/**
	 * Get a copy of the bytes in this list.
	 * 
	 * @return the copied array of bytes
	 */
	public byte[] toArrayValue() {
		return slice(0, size);
	}

	/**
	 * Get a copied subset of bytes in this list.
	 * 
	 * @param index
	 *        the position in this list to start copying from
	 * @param length
	 *        the number of bytes to copy
	 * @return the new array of bytes
	 */
	public byte[] slice(int index, int length) {
		byte[] dest = new byte[length];
		copy(index, dest, 0, length);
		return dest;
	}

	/**
	 * Copy a range of bytes onto a byte array.
	 * 
	 * @param index
	 *        the position in this list to start copying from
	 * @param dest
	 *        the destination to copy the bytes to
	 * @param destPos
	 *        the position in {@code dest} to start copying to
	 * @param length
	 *        the number of bytes to copy
	 * @throws ArrayIndexOutOfBoundsException
	 *         if {@code index} or {@code length} are out of bounds
	 */
	public void copy(int index, byte[] dest, int destPos, int length) {
		if ( length == 0 ) {
			return;
		}
		if ( index < 0 || index >= size ) {
			throw new ArrayIndexOutOfBoundsException(index);
		}
		System.arraycopy(data, index, dest, destPos, length);
		return;
	}

	@Override
	public void clear() {
		size = 0;
	}

	@Override
	public Byte remove(int index) {
		byte b = get(index);
		remove(index, 1);
		return b;
	}

	@Override
	protected void removeRange(int fromIndex, int toIndex) {
		remove(fromIndex, toIndex - fromIndex);
	}

	/**
	 * Remove a range of bytes.
	 * 
	 * @param index
	 *        the position to start removing from
	 * @param length
	 *        the number of bytes to remove
	 * @throws ArrayIndexOutOfBoundsException
	 *         if {@code index} or {@code length} are out of bounds
	 */
	public void remove(int index, int length) {
		if ( length == 0 ) {
			return;
		}
		if ( index < 0 || index >= size ) {
			throw new ArrayIndexOutOfBoundsException(index);
		}

		if ( index == 0 ) {
			// data at the front
			System.arraycopy(data, length, data, 0, size - length);
		} else if ( size - length != index ) {
			// data in the middle
			System.arraycopy(data, index + length, data, index, size - (index + length));
		}
		size -= length;
	}

}

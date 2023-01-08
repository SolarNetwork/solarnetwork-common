/* ==================================================================
 * KeyValuePair.java - Jun 1, 2010 11:47:49 AM
 * 
 * Copyright 2007-2010 SolarNetwork.net Dev Team
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

import java.io.Serializable;

/**
 * A mutable key and value pair.
 * 
 * @author matt
 * @version 1.0
 * @since 1.43
 */
public class KeyValuePair implements Serializable, Comparable<KeyValuePair> {

	private static final long serialVersionUID = -8143671046909870551L;

	/** The key. */
	private String key;

	/** The value. */
	private String value;

	/**
	 * Default constructor.
	 */
	public KeyValuePair() {
		super();
	}

	/**
	 * Construct with values.
	 * 
	 * @param key
	 *        the key
	 * @param value
	 *        the value
	 */
	public KeyValuePair(String key, String value) {
		super();
		this.key = key;
		this.value = value;
	}

	/**
	 * Compare the {@code key} values of two KeyValuePair objects, falling back
	 * to {@code value} if {@code key} is {@literal null} in both objects.
	 */
	@Override
	public int compareTo(KeyValuePair o) {
		if ( key == null ) {
			return 1;
		} else if ( o.key == null ) {
			return -1;
		}
		int result = key.compareTo(o.key);
		if ( result == 0 ) {
			if ( value == null ) {
				result = 1;
			} else if ( o.value == null ) {
				result = -1;
			} else {
				result = value.compareTo(o.value);
			}
		}
		return result;
	}

	@Override
	public String toString() {
		return "KeyValuePair{" + key + '=' + value + '}';
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		return result;
	}

	/**
	 * Compare the {@code key} values of two {@code KeyValuePair} objects.
	 */
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( obj == null ) {
			return false;
		}
		if ( getClass() != obj.getClass() ) {
			return false;
		}
		KeyValuePair other = (KeyValuePair) obj;
		if ( key == null ) {
			if ( other.key != null ) {
				return false;
			}
		} else if ( !key.equals(other.key) ) {
			return false;
		}
		return true;
	}

	/**
	 * Get the key.
	 * 
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Set the key.
	 * 
	 * @param key
	 *        the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * Get the value.
	 * 
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Set the value.
	 * 
	 * @param value
	 *        the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

}

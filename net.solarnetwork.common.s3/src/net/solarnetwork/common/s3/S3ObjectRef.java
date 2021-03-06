/* ==================================================================
 * S3ObjectRef.java - 3/10/2017 2:54:27 PM
 * 
 * Copyright 2017 SolarNetwork.net Dev Team
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

package net.solarnetwork.common.s3;

import java.net.URL;
import java.util.Date;

/**
 * An immutable reference to an S3 object.
 * 
 * @author matt
 * @version 1.0
 */
public class S3ObjectRef implements S3ObjectReference {

	private final String key;
	private final long size;
	private final Date modified;
	private final URL url;

	/**
	 * Constructor.
	 * 
	 * @param key
	 *        the key
	 */
	public S3ObjectRef(String key) {
		this(key, -1, null, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param key
	 *        the key
	 * @param size
	 *        the size
	 * @param modified
	 *        the modification date
	 * @param url
	 *        the URL
	 */
	public S3ObjectRef(String key, long size, Date modified, URL url) {
		super();
		this.key = key;
		this.size = size;
		this.modified = modified;
		this.url = url;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("S3ObjectRef{key=");
		builder.append(key);
		builder.append("}");
		return builder.toString();
	}

	@Override
	public String getKey() {
		return key;
	}

	@Override
	public long getSize() {
		return size;
	}

	@Override
	public Date getModified() {
		return modified;
	}

	@Override
	public URL getURL() {
		return url;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		return result;
	}

	/**
	 * Test for equality.
	 * 
	 * <p>
	 * Only the {@code key} property is compared for equality.
	 * </p>
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( obj == null ) {
			return false;
		}
		if ( !(obj instanceof S3ObjectRef) ) {
			return false;
		}
		S3ObjectRef other = (S3ObjectRef) obj;
		if ( key == null ) {
			if ( other.key != null ) {
				return false;
			}
		} else if ( !key.equals(other.key) ) {
			return false;
		}
		return true;
	}

}

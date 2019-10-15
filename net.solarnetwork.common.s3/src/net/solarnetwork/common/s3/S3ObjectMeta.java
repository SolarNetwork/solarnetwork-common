/* ==================================================================
 * S3ObjectMeta.java - 15/10/2019 1:55:27 pm
 * 
 * Copyright 2019 SolarNetwork.net Dev Team
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

import java.util.Date;
import org.springframework.util.MimeType;

/**
 * Immutable implementation of {@link S3ObjectMetadata}.
 * 
 * @author matt
 * @version 1.0
 */
public class S3ObjectMeta implements S3ObjectMetadata {

	private final Date modified;
	private final long size;
	private final MimeType contentType;

	/**
	 * Constructor.
	 * 
	 * @param size
	 *        the content size
	 * @param modified
	 *        the modified date
	 */
	public S3ObjectMeta(long size, Date modified) {
		this(size, modified, DEFAULT_CONTENT_TYPE);
	}

	/**
	 * Constructor.
	 * 
	 * @param size
	 *        the content size
	 * @param modified
	 *        the modified date
	 * @param contentType
	 *        the content type
	 */
	public S3ObjectMeta(long size, Date modified, MimeType contentType) {
		super();
		this.size = size;
		this.modified = modified;
		this.contentType = (contentType != null ? contentType : DEFAULT_CONTENT_TYPE);
	}

	@Override
	public Date getModified() {
		return modified;
	}

	@Override
	public long getSize() {
		return size;
	}

	@Override
	public MimeType getContentType() {
		return contentType;
	}

}

/* ==================================================================
 * SimpleResourceMetadata.java - 16/10/2019 6:52:57 am
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

package net.solarnetwork.io;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.util.MimeType;

/**
 * Basic implementation of {@link ResourceMetadata}.
 * 
 * @author matt
 * @version 1.0
 */
public class SimpleResourceMetadata implements ResourceMetadata {

	private final Date modified;
	private final MimeType contentType;
	private Map<String, Object> extendedMetadata;

	/**
	 * Constructor.
	 * 
	 * @param modified
	 *        the modified date
	 */
	public SimpleResourceMetadata(Date modified) {
		this(modified, DEFAULT_CONTENT_TYPE);
	}

	/**
	 * Constructor.
	 * 
	 * @param modified
	 *        the modified date
	 * @param contentType
	 *        the content type
	 */
	public SimpleResourceMetadata(Date modified, MimeType contentType) {
		this(modified, contentType, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param modified
	 *        the modified date
	 * @param contentType
	 *        the content type
	 */
	public SimpleResourceMetadata(Date modified, MimeType contentType, Map<String, ?> extendedMetadata) {
		super();
		this.modified = modified;
		this.contentType = (contentType != null ? contentType : DEFAULT_CONTENT_TYPE);
		putExtendedMetadata(extendedMetadata);
	}

	@Override
	public final Date getModified() {
		return modified;
	}

	@Override
	public final MimeType getContentType() {
		return contentType;
	}

	@Override
	public void populateMap(Map<String, Object> map) {
		ResourceMetadata.super.populateMap(map);
		Map<String, Object> extended = getExtendedMetadata();
		if ( extended != null ) {
			map.putAll(extended);
		}
	}

	/**
	 * Get the extended metadata.
	 * 
	 * @return the extended metadata, or {@literal null} if none
	 */
	public final Map<String, Object> getExtendedMetadata() {
		return extendedMetadata;
	}

	/**
	 * Put a set of extended metadata.
	 * 
	 * <p>
	 * This will overwrite any existing keys already present in the extended
	 * metadata.
	 * </p>
	 * 
	 * @param extendedMetadata
	 *        the extended metadata to add
	 */
	public final void putExtendedMetadata(Map<String, ?> extendedMetadata) {
		if ( extendedMetadata == null || extendedMetadata.isEmpty() ) {
			return;
		}
		Map<String, Object> map = getExtendedMetadata();
		if ( map == null ) {
			map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			this.extendedMetadata = map;
		}
		map.putAll(extendedMetadata);
	}

}

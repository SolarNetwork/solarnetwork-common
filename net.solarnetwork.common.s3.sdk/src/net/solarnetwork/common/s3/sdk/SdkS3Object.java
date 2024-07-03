/* ==================================================================
 * SdkS3Object.java - 15/10/2019 10:50:29 am
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

package net.solarnetwork.common.s3.sdk;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.Map;
import org.springframework.util.MimeType;
import com.amazonaws.services.s3.model.ObjectMetadata;
import net.solarnetwork.common.s3.S3Object;
import net.solarnetwork.common.s3.S3ObjectMetadata;

/**
 * AWS SDK implementation of {@link S3Object}.
 * 
 * @author matt
 * @version 1.1
 */
public class SdkS3Object implements S3Object, S3ObjectMetadata {

	private final com.amazonaws.services.s3.model.S3Object s3Object;
	private final URL url;

	/**
	 * Constructor.
	 * 
	 * @param s3Object
	 *        the object
	 * @param url
	 *        the URL
	 */
	public SdkS3Object(com.amazonaws.services.s3.model.S3Object s3Object, URL url) {
		super();
		this.s3Object = s3Object;
		this.url = url;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return s3Object.getObjectContent();
	}

	@Override
	public S3ObjectMetadata getMetadata() {
		return this;
	}

	@Override
	public Date getModified() {
		ObjectMetadata m = s3Object.getObjectMetadata();
		return (m != null ? m.getLastModified() : null);
	}

	@Override
	public long getSize() {
		ObjectMetadata m = s3Object.getObjectMetadata();
		return (m != null ? m.getContentLength() : null);
	}

	@Override
	public String getStorageClass() {
		ObjectMetadata m = s3Object.getObjectMetadata();
		return (m != null ? m.getStorageClass() : null);
	}

	@Override
	public URL getURL() {
		return url;
	}

	@Override
	public MimeType getContentType() {
		ObjectMetadata m = s3Object.getObjectMetadata();
		return (m != null && m.getContentType() != null ? MimeType.valueOf(m.getContentType())
				: S3ObjectMetadata.DEFAULT_CONTENT_TYPE);
	}

	@Override
	public void populateMap(Map<String, Object> map) {
		ObjectMetadata m = s3Object.getObjectMetadata();
		Map<String, Object> extra = (m != null ? m.getRawMetadata() : null);
		if ( extra != null ) {
			map.putAll(extra);
		}
		S3ObjectMetadata.super.populateMap(map);
	}

}

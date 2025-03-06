/* ==================================================================
 * Sdk2S3Object.java - 17/06/2024 8:41:50 am
 *
 * Copyright 2024 SolarNetwork.net Dev Team
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

package net.solarnetwork.common.s3.sdk2;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.util.Date;
import org.springframework.util.MimeType;
import net.solarnetwork.common.s3.S3Object;
import net.solarnetwork.common.s3.S3ObjectMetadata;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

/**
 * AWS SDK V2 implementation of {@link S3Object}.
 *
 * @author matt
 * @version 1.0
 */
public class Sdk2S3Object implements S3Object, S3ObjectMetadata {

	private final ResponseInputStream<GetObjectResponse> response;
	private final URL url;

	/**
	 * Constructor.
	 *
	 * @param response
	 *        the response
	 * @param url
	 *        the URL
	 */
	public Sdk2S3Object(ResponseInputStream<GetObjectResponse> response, URL url) {
		super();
		this.response = response;
		this.url = url;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return response;
	}

	@Override
	public S3ObjectMetadata getMetadata() {
		return this;
	}

	@Override
	public URL getURL() {
		return url;
	}

	@Override
	public Date getModified() {
		Instant ts = response.response().lastModified();
		return (ts != null ? Date.from(ts) : null);
	}

	@Override
	public long getSize() {
		Long len = response.response().contentLength();
		return (len != null ? len : -1);
	}

	@Override
	public String getStorageClass() {
		return response.response().storageClassAsString();
	}

	@Override
	public MimeType getContentType() {
		String type = response.response().contentType();
		return (type != null ? MimeType.valueOf(type) : S3ObjectMetadata.super.getContentType());
	}

}

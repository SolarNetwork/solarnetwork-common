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
import net.solarnetwork.common.s3.S3Object;

/**
 * AWS SDK implementation of {@link S3Object}.
 * 
 * @author matt
 * @version 1.0
 */
public class SdkS3Object implements S3Object {

	private final com.amazonaws.services.s3.model.S3Object s3Object;

	/**
	 * Constructor.
	 * 
	 * @param s3Object
	 *        the object
	 */
	public SdkS3Object(com.amazonaws.services.s3.model.S3Object s3Object) {
		super();
		this.s3Object = s3Object;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return s3Object.getObjectContent();
	}

}
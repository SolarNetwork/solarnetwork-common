/* ==================================================================
 * S3Object.java - 15/10/2019 10:49:44 am
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

import java.net.URL;
import org.springframework.core.io.InputStreamSource;
import net.solarnetwork.io.ResourceMetadataHolder;

/**
 * API for an object in S3.
 * 
 * @author matt
 * @version 1.0
 */
public interface S3Object extends InputStreamSource, ResourceMetadataHolder {

	/**
	 * Get the metadata associated with this object.
	 * 
	 * @return the metadata
	 */
	@Override
	S3ObjectMetadata getMetadata();

	/**
	 * Get a URL for this object.
	 * 
	 * @return a URL for this object
	 */
	URL getURL();

}

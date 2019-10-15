/* ==================================================================
 * S3ObjectMetadata.java - 15/10/2019 1:44:31 pm
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

import net.solarnetwork.io.ResourceMetadata;

/**
 * Metadata related to an S3 object.
 * 
 * @author matt
 * @version 1.0
 */
public interface S3ObjectMetadata extends ResourceMetadata {

	/** The metadata map key for the {@link #getSize()} value. */
	String SIZE_KEY = "Content-Length";

	/**
	 * Get the object's content size.
	 * 
	 * @return the size, in bytes
	 */
	long getSize();

	@Override
	default boolean isCustomKey(String key) {
		boolean result = ResourceMetadata.super.isCustomKey(key);
		if ( !result ) {
			return false;
		}
		switch (key) {
			case SIZE_KEY:
				return false;

			default:
				return true;
		}
	}

}

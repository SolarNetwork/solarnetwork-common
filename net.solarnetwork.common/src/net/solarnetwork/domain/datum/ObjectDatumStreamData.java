/* ==================================================================
 * ObjectDatumStreamData.java - 29/04/2022 11:05:39 AM
 * 
 * Copyright 2022 SolarNetwork.net Dev Team
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

package net.solarnetwork.domain.datum;

import static net.solarnetwork.util.ObjectUtils.requireNonNullArgument;
import java.util.List;

/**
 * A set of object datum stream data with associated metadata.
 * 
 * @author matt
 * @version 1.0
 * @since 2.4
 */
public class ObjectDatumStreamData {

	private final ObjectDatumStreamMetadata metadata;
	private final List<StreamDatum> data;

	/**
	 * Constructor.
	 * 
	 * @param metadata
	 *        the metadata
	 * @param data
	 *        the data
	 * @throws IllegalArgumentException
	 *         if any argument is {@literal null}
	 */
	public ObjectDatumStreamData(ObjectDatumStreamMetadata metadata, List<StreamDatum> data) {
		super();
		this.metadata = requireNonNullArgument(metadata, "metadata");
		this.data = requireNonNullArgument(data, "data");
	}

	/**
	 * Get the metadata.
	 * 
	 * @return the metadata, never {@literal null}
	 */
	public ObjectDatumStreamMetadata getMetadata() {
		return metadata;
	}

	/**
	 * Get the data.
	 * 
	 * @return the data, never {@literal null}
	 */
	public List<StreamDatum> getData() {
		return data;
	}

}

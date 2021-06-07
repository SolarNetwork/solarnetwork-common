/* ==================================================================
 * BasicStreamDatum.java - 22/10/2020 10:02:38 am
 * 
 * Copyright 2020 SolarNetwork.net Dev Team
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

import java.io.Serializable;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

/**
 * Basic implementation of {@link StreamDatum}.
 * 
 * @author matt
 * @version 1.0
 * @since 1.72
 */
public class BasicStreamDatum implements StreamDatum, Cloneable, Serializable {

	private static final long serialVersionUID = -8735056339943547310L;

	private final UUID streamId;
	private final Instant timestamp;
	private final DatumProperties properties;

	/**
	 * Constructor.
	 * 
	 * @param streamId
	 *        the stream ID
	 * @param timestamp
	 *        the timestamp
	 * @param properties
	 *        the samples
	 */
	public BasicStreamDatum(UUID streamId, Instant timestamp, DatumProperties properties) {
		super();
		if ( streamId == null ) {
			throw new IllegalArgumentException("The streamId argument must no be null");
		}
		this.streamId = streamId;
		if ( timestamp == null ) {
			throw new IllegalArgumentException("The timestamp argument must no be null");
		}
		this.timestamp = timestamp;
		this.properties = properties;
	}

	@Override
	public BasicStreamDatum clone() {
		try {
			return (BasicStreamDatum) super.clone();
		} catch ( CloneNotSupportedException e ) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BasicStreamDatum{streamId=");
		builder.append(streamId);
		builder.append(", ts=");
		builder.append(timestamp);
		if ( properties != null ) {
			if ( properties.getInstantaneous() != null ) {
				builder.append(", i=");
				builder.append(Arrays.toString(properties.getInstantaneous()));
			}
			if ( properties.getAccumulating() != null ) {
				builder.append(", a=");
				builder.append(Arrays.toString(properties.getAccumulating()));
			}
			if ( properties.getStatus() != null ) {
				builder.append(", s=");
				builder.append(Arrays.toString(properties.getStatus()));
			}
			if ( properties.getTags() != null ) {
				builder.append(", t=");
				builder.append(Arrays.toString(properties.getTags()));
			}
		}
		builder.append("}");
		return builder.toString();
	}

	@Override
	public Instant getTimestamp() {
		return timestamp;
	}

	@Override
	public UUID getStreamId() {
		return streamId;
	}

	@Override
	public DatumProperties getProperties() {
		return properties;
	}

}

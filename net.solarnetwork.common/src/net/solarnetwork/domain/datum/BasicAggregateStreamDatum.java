/* ==================================================================
 * BasicAggregateStreamDatum.java - 29/06/2022 10:25:04 am
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

import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

/**
 * Basic implementation of {@link AggregateStreamDatum}.
 * 
 * @author matt
 * @version 1.0
 * @since 2.7
 */
public class BasicAggregateStreamDatum extends BasicStreamDatum implements AggregateStreamDatum {

	private static final long serialVersionUID = 2501630135742744682L;

	private final Instant endTimestamp;
	private final DatumPropertiesStatistics statistics;

	/**
	 * @param streamId
	 * @param timestamp
	 * @param properties
	 */
	public BasicAggregateStreamDatum(UUID streamId, Instant timestamp, DatumProperties properties,
			Instant endTimestamp, DatumPropertiesStatistics statistics) {
		super(streamId, timestamp, properties);
		this.endTimestamp = endTimestamp;
		this.statistics = statistics;
	}

	@Override
	public BasicAggregateStreamDatum clone() {
		return (BasicAggregateStreamDatum) super.clone();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BasicAggregateStreamDatum{streamId=");
		builder.append(getStreamId());
		builder.append(", ts=");
		builder.append(getTimestamp());
		builder.append(", endTs=");
		builder.append(endTimestamp);
		DatumProperties properties = getProperties();
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
		if ( statistics != null ) {
			if ( statistics.getInstantaneous() != null ) {
				builder.append(", si=");
				builder.append(Arrays.deepToString(statistics.getInstantaneous()));
			}
			if ( statistics.getAccumulating() != null ) {
				builder.append(", sa=");
				builder.append(Arrays.deepToString(statistics.getAccumulating()));
			}
		}
		builder.append("}");
		return builder.toString();
	}

	@Override
	public Instant getEndTimestamp() {
		return endTimestamp;
	}

	@Override
	public DatumPropertiesStatistics getStatistics() {
		return statistics;
	}

}

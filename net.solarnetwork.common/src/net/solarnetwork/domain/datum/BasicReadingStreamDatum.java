/* ==================================================================
 * BasicReadingStreamDatum.java - 15/11/2026 10:20:14 am
 *
 * Copyright 2026 SolarNetwork.net Dev Team
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

import java.io.Serial;
import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Basic implementation of {@link ReadingStreamDatum}.
 *
 * <p>
 * The accumulating {@link DatumPropertiesStatistics} of a reading are the
 * reading difference, starting reading, and ending reading of each property, as
 * given by {@link DatumPropertiesStatistics.AccumulatingStatistic}.
 * </p>
 *
 * <p>
 * Equality is that of {@link BasicAggregateStreamDatum}, so a reading equals an
 * aggregate datum with the same values.
 * </p>
 *
 * @author matt
 * @version 1.0
 * @since 4.61
 */
public class BasicReadingStreamDatum extends BasicAggregateStreamDatum implements ReadingStreamDatum {

	@Serial
	private static final long serialVersionUID = 6200460926305829011L;

	/**
	 * Constructor.
	 *
	 * @param streamId
	 *        the stream ID
	 * @param timestamp
	 *        the starting timestamp
	 * @param properties
	 *        the properties
	 * @param endTimestamp
	 *        the ending timestamp, or {@code null} if it is implicit
	 * @param statistics
	 *        the statistics
	 * @throws IllegalArgumentException
	 *         if any argument other than {@code endTimestamp} is {@code null}
	 */
	public BasicReadingStreamDatum(UUID streamId, Instant timestamp, DatumProperties properties,
			@Nullable Instant endTimestamp, DatumPropertiesStatistics statistics) {
		super(streamId, timestamp, properties, endTimestamp, statistics);
	}

	@Override
	public BasicReadingStreamDatum clone() {
		return (BasicReadingStreamDatum) super.clone();
	}

	@Override
	public String toString() {
		// replace the class name in the BasicAggregateStreamDatum form
		final String s = super.toString();
		final int idx = s.indexOf('{');
		return "BasicReadingStreamDatum" + (idx < 0 ? s : s.substring(idx));
	}

}

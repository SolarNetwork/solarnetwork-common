/* ==================================================================
 * SampledValue.java - 10/02/2020 9:29:55 am
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

package net.solarnetwork.ocpp.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * A sampled value, e.g. a meter reading.
 * 
 * @author matt
 * @version 1.1
 */
@JsonDeserialize(builder = SampledValue.Builder.class)
public class SampledValue implements Comparable<SampledValue> {

	private final UUID sessionId;
	private final Instant timestamp;
	private final String value;
	private final ReadingContext context;
	private final Measurand measurand;
	private final Phase phase;
	private final Location location;
	private final UnitOfMeasure unit;

	/**
	 * Constructor.
	 * 
	 * @param sessionId
	 *        the session ID
	 * @param timestamp
	 *        the timestamp
	 * @param value
	 *        the value
	 * @param context
	 *        the context
	 * @param measurand
	 *        the measurand
	 * @param phase
	 *        the phase
	 * @param location
	 *        the location
	 * @param unit
	 *        the unit
	 */
	public SampledValue(UUID sessionId, Instant timestamp, String value, ReadingContext context,
			Measurand measurand, Phase phase, Location location, UnitOfMeasure unit) {
		super();
		this.sessionId = sessionId;
		this.timestamp = timestamp;
		this.value = value;
		this.context = context;
		this.measurand = measurand;
		this.phase = phase;
		this.location = location;
		this.unit = unit;
	}

	private SampledValue(Builder builder) {
		this(builder.sessionId, builder.timestamp, builder.value, builder.context, builder.measurand,
				builder.phase, builder.location, builder.unit);
	}

	@Override
	public int hashCode() {
		return Objects.hash(context, location, measurand, phase, sessionId, timestamp);
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( !(obj instanceof SampledValue) ) {
			return false;
		}
		SampledValue other = (SampledValue) obj;
		return context == other.context && location == other.location && measurand == other.measurand
				&& phase == other.phase && Objects.equals(sessionId, other.sessionId)
				&& Objects.equals(timestamp, other.timestamp);
	}

	@Override
	public int compareTo(SampledValue o) {
		int result = timestamp.compareTo(o.timestamp);
		if ( result != 0 ) {
			return result;
		}
		if ( context != o.context ) {
			if ( context == null ) {
				return -1;
			} else if ( o.context == null ) {
				return 1;
			}
			result = Integer.compare(context.getCode(), o.context.getCode());
			if ( result != 0 ) {
				return result;
			}
		}
		if ( location != o.location ) {
			if ( location == null ) {
				return -1;
			} else if ( o.location == null ) {
				return 1;
			}
			result = Integer.compare(location.getCode(), o.location.getCode());
			if ( result != 0 ) {
				return result;
			}
		}
		if ( measurand != o.measurand ) {
			if ( measurand == null ) {
				return -1;
			} else if ( o.measurand == null ) {
				return 1;
			}
			result = Integer.compare(measurand.getCode(), o.measurand.getCode());
			if ( result != 0 ) {
				return result;
			}
		}
		if ( phase == o.phase ) {
			return 0;
		} else if ( phase == null ) {
			return -1;
		} else if ( o.phase == null ) {
			return 1;
		}
		return Integer.compare(phase.getCode(), o.phase.getCode());
	}

	@Override
	public String toString() {
		StringBuilder builder2 = new StringBuilder();
		builder2.append("SampledValue{");
		if ( timestamp != null ) {
			builder2.append("timestamp=");
			builder2.append(timestamp);
			builder2.append(", ");
		}
		if ( context != null ) {
			builder2.append("context=");
			builder2.append(context);
			builder2.append(", ");
		}
		if ( location != null ) {
			builder2.append("location=");
			builder2.append(location);
			builder2.append(", ");
		}
		if ( measurand != null ) {
			builder2.append("measurand=");
			builder2.append(measurand);
			builder2.append(", ");
		}
		if ( value != null ) {
			builder2.append("value=");
			builder2.append(value);
		}
		builder2.append("}");
		return builder2.toString();
	}

	/**
	 * Get the {@link ChargeSession} ID associated with this value.
	 * 
	 * @return the session ID
	 */
	public UUID getSessionId() {
		return sessionId;
	}

	/**
	 * Get the time the sample was captured.
	 * 
	 * @return the timestamp
	 */
	public Instant getTimestamp() {
		return timestamp;
	}

	/**
	 * Get the sampled value.
	 * 
	 * @return the value the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Get the reading context.
	 * 
	 * @return the context
	 */
	public ReadingContext getContext() {
		return context;
	}

	/**
	 * Get the measurement type.
	 * 
	 * @return the measurand
	 */
	public Measurand getMeasurand() {
		return measurand;
	}

	/**
	 * Get the phase.
	 * 
	 * @return the phase
	 */
	public Phase getPhase() {
		return phase;
	}

	/**
	 * Get the location.
	 * 
	 * @return the location
	 */
	public Location getLocation() {
		return location;
	}

	/**
	 * Get the measurement unit.
	 * 
	 * @return the unit
	 */
	public UnitOfMeasure getUnit() {
		return unit;
	}

	/**
	 * Creates builder to build {@link SampledValue}.
	 * 
	 * @return created builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Get a builder, populated with this instance's values.
	 * 
	 * @return a pre-populated builder
	 */
	public Builder toBuilder() {
		return new Builder(this);
	}

	/**
	 * Builder to build {@link SampledValue}.
	 */
	public static final class Builder {

		private UUID sessionId;
		private Instant timestamp;
		private String value;
		private ReadingContext context;
		private Measurand measurand;
		private Phase phase;
		private Location location;
		private UnitOfMeasure unit;

		private Builder() {
			super();
		}

		private Builder(SampledValue value) {
			super();
			this.sessionId = value.sessionId;
			this.timestamp = value.timestamp;
			this.value = value.value;
			this.context = value.context;
			this.measurand = value.measurand;
			this.phase = value.phase;
			this.location = value.location;
			this.unit = value.unit;
		}

		public Builder withSessionId(UUID sessionId) {
			this.sessionId = sessionId;
			return this;
		}

		public Builder withTimestamp(Instant timestamp) {
			this.timestamp = timestamp;
			return this;
		}

		public Builder withValue(String value) {
			this.value = value;
			return this;
		}

		public Builder withContext(ReadingContext context) {
			this.context = context;
			return this;
		}

		public Builder withMeasurand(Measurand measurand) {
			this.measurand = measurand;
			return this;
		}

		public Builder withPhase(Phase phase) {
			this.phase = phase;
			return this;
		}

		public Builder withLocation(Location location) {
			this.location = location;
			return this;
		}

		public Builder withUnit(UnitOfMeasure unit) {
			this.unit = unit;
			return this;
		}

		public SampledValue build() {
			return new SampledValue(this);
		}
	}

}

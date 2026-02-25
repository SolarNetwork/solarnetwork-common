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
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.annotation.JsonDeserialize;

/**
 * A sampled value, e.g. a meter reading.
 *
 * @author matt
 * @version 1.1
 */
@JsonDeserialize(builder = SampledValue.Builder.class)
public class SampledValue implements Comparable<SampledValue> {

	private final @Nullable UUID sessionId;
	private final @Nullable Instant timestamp;
	private final @Nullable String value;
	private final @Nullable ReadingContext context;
	private final @Nullable Measurand measurand;
	private final @Nullable Phase phase;
	private final @Nullable Location location;
	private final @Nullable UnitOfMeasure unit;

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
	public SampledValue(@Nullable UUID sessionId, @Nullable Instant timestamp, @Nullable String value,
			@Nullable ReadingContext context, @Nullable Measurand measurand, @Nullable Phase phase,
			@Nullable Location location, @Nullable UnitOfMeasure unit) {
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
	public boolean equals(@Nullable Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( !(obj instanceof SampledValue other) ) {
			return false;
		}
		return context == other.context && location == other.location && measurand == other.measurand
				&& phase == other.phase && Objects.equals(sessionId, other.sessionId)
				&& Objects.equals(timestamp, other.timestamp);
	}

	@Override
	public int compareTo(SampledValue o) {
		int result = 0;
		if ( timestamp != o.timestamp ) {
			if ( timestamp == null ) {
				return -1;
			} else if ( o.timestamp == null ) {
				return 1;
			}
			result = timestamp.compareTo(o.timestamp);
			if ( result != 0 ) {
				return result;
			}
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
		StringBuilder builder = new StringBuilder();
		builder.append("SampledValue{");
		if ( timestamp != null ) {
			builder.append("timestamp=");
			builder.append(timestamp);
			builder.append(", ");
		}
		if ( context != null ) {
			builder.append("context=");
			builder.append(context);
			builder.append(", ");
		}
		if ( location != null ) {
			builder.append("location=");
			builder.append(location);
			builder.append(", ");
		}
		if ( measurand != null ) {
			builder.append("measurand=");
			builder.append(measurand);
			builder.append(", ");
		}
		if ( value != null ) {
			builder.append("value=");
			builder.append(value);
		}
		builder.append("}");
		return builder.toString();
	}

	/**
	 * Get the {@link ChargeSession} ID associated with this value.
	 *
	 * @return the session ID
	 */
	public final @Nullable UUID getSessionId() {
		return sessionId;
	}

	/**
	 * Get the time the sample was captured.
	 *
	 * @return the timestamp
	 */
	public final @Nullable Instant getTimestamp() {
		return timestamp;
	}

	/**
	 * Get the sampled value.
	 *
	 * @return the value the value
	 */
	public final @Nullable String getValue() {
		return value;
	}

	/**
	 * Get the reading context.
	 *
	 * @return the context
	 */
	public final @Nullable ReadingContext getContext() {
		return context;
	}

	/**
	 * Get the measurement type.
	 *
	 * @return the measurand
	 */
	public final @Nullable Measurand getMeasurand() {
		return measurand;
	}

	/**
	 * Get the phase.
	 *
	 * @return the phase
	 */
	public final @Nullable Phase getPhase() {
		return phase;
	}

	/**
	 * Get the location.
	 *
	 * @return the location
	 */
	public final @Nullable Location getLocation() {
		return location;
	}

	/**
	 * Get the measurement unit.
	 *
	 * @return the unit
	 */
	public final @Nullable UnitOfMeasure getUnit() {
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

		private @Nullable UUID sessionId;
		private @Nullable Instant timestamp;
		private @Nullable String value;
		private @Nullable ReadingContext context;
		private @Nullable Measurand measurand;
		private @Nullable Phase phase;
		private @Nullable Location location;
		private @Nullable UnitOfMeasure unit;

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

		/**
		 * Configure the session ID.
		 *
		 * @param sessionId
		 *        the session ID
		 * @return this instance
		 */
		public Builder withSessionId(@Nullable UUID sessionId) {
			this.sessionId = sessionId;
			return this;
		}

		/**
		 * Configure the timestamp.
		 *
		 * @param timestamp
		 *        the timestamp
		 * @return this instance
		 */
		public Builder withTimestamp(@Nullable Instant timestamp) {
			this.timestamp = timestamp;
			return this;
		}

		/**
		 * Configure the value.
		 *
		 * @param value
		 *        the value
		 * @return this instance
		 */
		public Builder withValue(@Nullable String value) {
			this.value = value;
			return this;
		}

		/**
		 * Configure the context.
		 *
		 * @param context
		 *        the context
		 * @return this instance
		 */
		public Builder withContext(@Nullable ReadingContext context) {
			this.context = context;
			return this;
		}

		/**
		 * Configure the measurand.
		 *
		 * @param measurand
		 *        the measurand
		 * @return this instance
		 */
		public Builder withMeasurand(@Nullable Measurand measurand) {
			this.measurand = measurand;
			return this;
		}

		/**
		 * Configure the phase.
		 *
		 * @param phase
		 *        the phase
		 * @return this instance
		 */
		public Builder withPhase(@Nullable Phase phase) {
			this.phase = phase;
			return this;
		}

		/**
		 * Configure the location.
		 *
		 * @param location
		 *        the location
		 * @return this instance
		 */
		public Builder withLocation(@Nullable Location location) {
			this.location = location;
			return this;
		}

		/**
		 * Configure the unit.
		 *
		 * @param unit
		 *        the unit
		 * @return this instance
		 */
		public Builder withUnit(@Nullable UnitOfMeasure unit) {
			this.unit = unit;
			return this;
		}

		/**
		 * Build a value from this builder.
		 *
		 * @return the new instance
		 */
		public SampledValue build() {
			return new SampledValue(this);
		}
	}

}

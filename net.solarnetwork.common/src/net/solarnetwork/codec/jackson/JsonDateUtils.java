/* ==================================================================
 * JsonDateUtils.java - 16/06/2020 10:28:54 am
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

package net.solarnetwork.codec.jackson;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import net.solarnetwork.util.DateUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.cfg.ConfigOverrides;
import tools.jackson.databind.cfg.ModuleContextBase;
import tools.jackson.databind.exc.InvalidFormatException;
import tools.jackson.databind.ext.javatime.JavaTimeInitializer;
import tools.jackson.databind.module.SimpleModule;

/**
 * JSON date handling utilities.
 *
 * @author matt
 * @version 1.0
 * @since 4.13
 */
public final class JsonDateUtils {

	private JsonDateUtils() {
		// no construction
	}

	/**
	 * {@link java.time.Instant} serializer that formats using a space date/time
	 * separator.
	 */
	public static class InstantSerializer
			extends tools.jackson.databind.ext.javatime.ser.InstantSerializer {

		/** A global instance. */
		public static final ValueSerializer<Instant> INSTANCE = new InstantSerializer();

		/**
		 * Constructor.
		 */
		public InstantSerializer() {
			super(tools.jackson.databind.ext.javatime.ser.InstantSerializer.INSTANCE,
					DateUtils.ISO_DATE_TIME_ALT_UTC, null, null, null);
		}

	}

	/**
	 * {@link java.time.ZonedDateTime} serializer that formats using a space
	 * date/time separator.
	 */
	public static class ZonedDateTimeSerializer
			extends tools.jackson.databind.ext.javatime.ser.ZonedDateTimeSerializer {

		/** A global instance. */
		public static final ValueSerializer<ZonedDateTime> INSTANCE = new ZonedDateTimeSerializer();

		/**
		 * Constructor.
		 */
		public ZonedDateTimeSerializer() {
			super(DateUtils.ISO_DATE_TIME_ALT_UTC);
		}

	}

	/**
	 * {@link java.time.LocalDateTime} serializer that formats using a space
	 * date/time separator.
	 */
	public static class LocalDateTimeSerializer
			extends tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer {

		/** A global instance. */
		public static final ValueSerializer<LocalDateTime> INSTANCE = new LocalDateTimeSerializer();

		/**
		 * Constructor.
		 */
		public LocalDateTimeSerializer() {
			super(DateUtils.ISO_DATE_TIME_ALT_UTC);
		}

	}

	/**
	 * {@link java.time.LocalTime} serializer that formats as {@literal HH:mm}.
	 */
	public static class LocalTimeSerializer
			extends tools.jackson.databind.ext.javatime.ser.LocalTimeSerializer {

		/** A global instance. */
		public static final ValueSerializer<LocalTime> INSTANCE = new LocalTimeSerializer();

		/**
		 * Constructor.
		 */
		public LocalTimeSerializer() {
			super(DateUtils.LOCAL_TIME);
		}

	}

	/**
	 * {@link java.time.Instant} deserializer that formats using a space or
	 * {@literal T} date/time separator.
	 */
	public static class InstantDeserializer
			extends tools.jackson.databind.ext.javatime.deser.InstantDeserializer<Instant> {

		/** A global instance. */
		public static final ValueDeserializer<Instant> INSTANCE = new InstantDeserializer();

		/**
		 * Constructor.
		 */
		public InstantDeserializer() {
			super(Instant.class, DateUtils.ISO_DATE_TIME_ALT_UTC, Instant::from,
					a -> Instant.ofEpochMilli(a.value),
					a -> Instant.ofEpochSecond(a.integer, a.fraction), null, true, true, false);
		}

		@Override
		public Instant deserialize(JsonParser parser, DeserializationContext context)
				throws JacksonException {
			try {
				return super.deserialize(parser, context);
			} catch ( InvalidFormatException e ) {
				// try T separator
				Object v = e.getValue();
				if ( v instanceof String ) {
					try {
						return DateTimeFormatter.ISO_INSTANT.parse(v.toString(), Instant::from);
					} catch ( DateTimeParseException e2 ) {
						// one last try as zoned time time
						try {
							return DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(v.toString(),
									Instant::from);
						} catch ( DateTimeException e3 ) {
							// give up and throw original exception
						}
					}
				}
				throw e;
			}
		}

	}

	/**
	 * {@link java.time.ZonedDateTime} deserializer that parses using a space or
	 * {@literal T} date/time separator.
	 */
	public static class ZonedDateTimeDeserializer
			extends tools.jackson.databind.ext.javatime.deser.InstantDeserializer<ZonedDateTime> {

		/** A global instance. */
		public static final ValueDeserializer<ZonedDateTime> INSTANCE = new ZonedDateTimeDeserializer();

		/**
		 * Constructor.
		 */
		public ZonedDateTimeDeserializer() {
			super(ZonedDateTime.class, DateUtils.ISO_DATE_TIME_ALT_UTC, ZonedDateTime::from,
					a -> ZonedDateTime.ofInstant(Instant.ofEpochMilli(a.value), a.zoneId),
					a -> ZonedDateTime.ofInstant(Instant.ofEpochSecond(a.integer, a.fraction), a.zoneId),
					ZonedDateTime::withZoneSameInstant, false, true, false);
		}

		@Override
		public ZonedDateTime deserialize(JsonParser parser, DeserializationContext context)
				throws JacksonException {
			try {
				return super.deserialize(parser, context);
			} catch ( InvalidFormatException e ) {
				// try T separator
				Object v = e.getValue();
				if ( v instanceof String ) {
					try {
						return DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(v.toString(),
								ZonedDateTime::from);
					} catch ( DateTimeParseException e2 ) {
						// ignore this and throw original exception
					}
				}
				throw e;
			}
		}
	}

	/**
	 * {@link java.time.LocalDateTime} deserializer that parses using a space or
	 * {@literal T} date/time separator.
	 */
	public static class LocalDateTimeDeserializer
			extends tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer {

		/** A global instance. */
		public static final ValueDeserializer<LocalDateTime> INSTANCE = new LocalDateTimeDeserializer();

		/**
		 * Constructor.
		 */
		public LocalDateTimeDeserializer() {
			super(DateUtils.ISO_DATE_TIME_ALT_UTC);
		}

		@Override
		public LocalDateTime deserialize(JsonParser parser, DeserializationContext context)
				throws JacksonException {
			try {
				return super.deserialize(parser, context);
			} catch ( InvalidFormatException e ) {
				// try T separator
				Object v = e.getValue();
				if ( v instanceof String ) {
					try {
						return DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse(v.toString(),
								LocalDateTime::from);
					} catch ( DateTimeParseException e2 ) {
						// ignore this and throw original exception
					}
				}
				throw e;
			}
		}

	}

	/**
	 * {@link java.time.LocalTime} deserializer that parses using the pattern
	 * {@literal HH:mm}.
	 */
	public static class LocalTimeDeserializer
			extends tools.jackson.databind.ext.javatime.deser.LocalTimeDeserializer {

		/** A global instance. */
		public static final ValueDeserializer<LocalTime> INSTANCE = new LocalTimeDeserializer();

		/**
		 * Constructor.
		 */
		public LocalTimeDeserializer() {
			super(DateUtils.LOCAL_TIME);
		}

	}

	/**
	 * A module for handling Java date and time objects in The SolarNetwork Way.
	 */
	public static final JacksonModule JAVA_TIME_MODULE;
	static {
		SimpleModule m = new SimpleModule("SolarNetwork Date and Time");

		ModuleContextBase ctx = new ModuleContextBase(new ObjectMapper().rebuild(),
				new ConfigOverrides());
		JavaTimeInitializer.getInstance().setupModule(ctx);
		m.setupModule(ctx);

		m.addSerializer(Instant.class, JsonDateUtils.InstantSerializer.INSTANCE);
		m.addSerializer(ZonedDateTime.class, JsonDateUtils.ZonedDateTimeSerializer.INSTANCE);
		m.addSerializer(LocalDateTime.class, JsonDateUtils.LocalDateTimeSerializer.INSTANCE);

		m.addDeserializer(Instant.class, JsonDateUtils.InstantDeserializer.INSTANCE);
		m.addDeserializer(ZonedDateTime.class, JsonDateUtils.ZonedDateTimeDeserializer.INSTANCE);
		m.addDeserializer(LocalDateTime.class, JsonDateUtils.LocalDateTimeDeserializer.INSTANCE);
		JAVA_TIME_MODULE = m;
	}

	/**
	 * A module for handling Java date and time objects in The SolarNetwork Way.
	 *
	 * <p>
	 * Note for this module to write number time values instead of text, the
	 * {@link tools.jackson.databind.cfg.DateTimeFeature#WRITE_DATES_AS_TIMESTAMPS}
	 * should be enabled. Also consider the
	 * {@link tools.jackson.databind.cfg.DateTimeFeature#WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS}
	 * and
	 * {@link tools.jackson.databind.cfg.DateTimeFeature#READ_DATE_TIMESTAMPS_AS_NANOSECONDS}
	 * features.
	 * </p>
	 */
	public static final JacksonModule JAVA_TIMESTAMP_MODULE;
	static {
		SimpleModule m = new SimpleModule("SolarNetwork Date and Time");

		ModuleContextBase ctx = new ModuleContextBase(new ObjectMapper().rebuild(),
				new ConfigOverrides());
		JavaTimeInitializer.getInstance().setupModule(ctx);
		m.setupModule(ctx);

		m.addSerializer(LocalDateTime.class, JsonDateUtils.LocalDateTimeSerializer.INSTANCE);

		m.addDeserializer(Instant.class, JsonDateUtils.InstantDeserializer.INSTANCE);
		m.addDeserializer(ZonedDateTime.class, JsonDateUtils.ZonedDateTimeDeserializer.INSTANCE);
		m.addDeserializer(LocalDateTime.class, JsonDateUtils.LocalDateTimeDeserializer.INSTANCE);
		JAVA_TIMESTAMP_MODULE = m;
	}

}

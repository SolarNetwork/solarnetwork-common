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

package net.solarnetwork.codec;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZonedDateTime;
import net.solarnetwork.util.DateUtils;

/**
 * JSON date handling utilities.
 * 
 * @author matt
 * @version 1.1
 * @since 1.72
 */
public final class JsonDateUtils implements Serializable {

	private static final long serialVersionUID = 8430495610507712465L;

	private JsonDateUtils() {
		// no construction
	}

	/**
	 * {@link java.time.Instant} serializer that formats using a space date/time
	 * separator.
	 */
	public static class InstantSerializer
			extends com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer {

		private static final long serialVersionUID = 2056512911882404402L;

		/** A global instance. */
		public static final InstantSerializer INSTANCE = new InstantSerializer();

		/**
		 * Constructor.
		 */
		public InstantSerializer() {
			super(com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer.INSTANCE, null, null,
					DateUtils.ISO_DATE_TIME_ALT_UTC);
		}

	}

	/**
	 * {@link java.time.ZonedDateTime} serializer that formats using a space
	 * date/time separator.
	 */
	public static class ZonedDateTimeSerializer
			extends com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer {

		private static final long serialVersionUID = 6913055308929776378L;

		/** A global instance. */
		public static final ZonedDateTimeSerializer INSTANCE = new ZonedDateTimeSerializer();

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
			extends com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer {

		private static final long serialVersionUID = 8528306055696764724L;

		/** A global instance. */
		public static final LocalDateTimeSerializer INSTANCE = new LocalDateTimeSerializer();

		/**
		 * Constructor.
		 */
		public LocalDateTimeSerializer() {
			super(DateUtils.ISO_DATE_TIME_ALT_UTC);
		}

	}

	/**
	 * {@link java.time.Instant} deserializer that formats using a space
	 * date/time separator.
	 * 
	 * @since 1.1
	 */
	public static class InstantDeserializer
			extends com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer<Instant> {

		private static final long serialVersionUID = 6619624665216114464L;

		/** A global instance. */
		public static final InstantDeserializer INSTANCE = new InstantDeserializer();

		/**
		 * Constructor.
		 */
		public InstantDeserializer() {
			super(Instant.class, DateUtils.ISO_DATE_TIME_ALT_UTC, Instant::from,
					a -> Instant.ofEpochMilli(a.value),
					a -> Instant.ofEpochSecond(a.integer, a.fraction), null, true);
		}

	}

	/**
	 * {@link java.time.ZonedDateTime} deserializer that formats using a space
	 * date/time separator.
	 * 
	 * @since 1.1
	 */
	public static class ZonedDateTimeDeserializer
			extends com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer<ZonedDateTime> {

		private static final long serialVersionUID = 461897970976758049L;

		/** A global instance. */
		public static final ZonedDateTimeDeserializer INSTANCE = new ZonedDateTimeDeserializer();

		/**
		 * Constructor.
		 */
		public ZonedDateTimeDeserializer() {
			super(ZonedDateTime.class, DateUtils.ISO_DATE_TIME_ALT_UTC, ZonedDateTime::from,
					a -> ZonedDateTime.ofInstant(Instant.ofEpochMilli(a.value), a.zoneId),
					a -> ZonedDateTime.ofInstant(Instant.ofEpochSecond(a.integer, a.fraction), a.zoneId),
					ZonedDateTime::withZoneSameInstant, false);
		}

	}

	/**
	 * {@link java.time.LocalDateTime} deserializer that parses using a space
	 * date/time separator.
	 * 
	 * @since 1.1
	 */
	public static class LocalDateTimeDeserializer
			extends com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer {

		private static final long serialVersionUID = -8187990594614635776L;

		/** A global instance. */
		public static final LocalDateTimeDeserializer INSTANCE = new LocalDateTimeDeserializer();

		/**
		 * Constructor.
		 */
		public LocalDateTimeDeserializer() {
			super(DateUtils.ISO_DATE_TIME_ALT_UTC);
		}

	}

}

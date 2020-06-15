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

package net.solarnetwork.util;

import java.io.Serializable;

/**
 * JSON date handling utilities.
 * 
 * @author matt
 * @version 1.0
 * @since 1.63
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

		/**
		 * Constructor.
		 */
		public LocalDateTimeSerializer() {
			super(DateUtils.ISO_DATE_TIME_ALT_UTC);
		}

	}

}

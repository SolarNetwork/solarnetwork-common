/* ==================================================================
 * XmlDateUtils.java - 31/01/2020 12:12:29 pm
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

package net.solarnetwork.ocpp.xml.support;

import java.time.Instant;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.function.Supplier;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Utilities for working with {@link XMLGregorianCalendar} dates.
 * 
 * @author matt
 * @version 1.0
 */
public final class XmlDateUtils {

	private XmlDateUtils() {
		// not available
	}

	/** A default {@link DatatypeFactory} instance. */
	public static final DatatypeFactory DATATYPE_FACTORY = defaultDatatypeFactory();

	private static final GregorianCalendar UTC_CALENDAR = new GregorianCalendar(
			TimeZone.getTimeZone("UTC"));

	private static DatatypeFactory defaultDatatypeFactory() {
		try {
			return DatatypeFactory.newInstance();
		} catch ( DatatypeConfigurationException e ) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Get a {@link XMLGregorianCalendar} for the current time, set to the UTC
	 * time zone.
	 * 
	 * @return a new calendar instance
	 */
	public static XMLGregorianCalendar newXmlCalendar() {
		return newXmlCalendar(System.currentTimeMillis());
	}

	/**
	 * Get a {@link XMLGregorianCalendar} for a specific time, set to the UTC
	 * time zone, using the {@link #DATATYPE_FACTORY}.
	 * 
	 * @param date
	 *        the date, in milliseconds since the epoch
	 * @return a new calendar instance
	 */
	public static XMLGregorianCalendar newXmlCalendar(long date) {
		return newXmlCalendar(DATATYPE_FACTORY, date);
	}

	/**
	 * Get a {@link XMLGregorianCalendar} for a specific time, set to the UTC
	 * time zone.
	 * 
	 * @param datatypeFactory
	 *        the factory to use
	 * @param date
	 *        the date, in milliseconds since the epoch
	 * @return a new calendar instance
	 */
	public static final XMLGregorianCalendar newXmlCalendar(DatatypeFactory datatypeFactory, long date) {
		GregorianCalendar now = (GregorianCalendar) UTC_CALENDAR.clone();
		now.setTimeInMillis(date);
		return datatypeFactory.newXMLGregorianCalendar(now);
	}

	/**
	 * Get a {@link XMLGregorianCalendar} for a specific time, set to the UTC
	 * time zone, using the {@link #DATATYPE_FACTORY}.
	 * 
	 * @param year
	 *        the year
	 * @param month
	 *        the month of the year, starting with {@literal 1} for January
	 * @param day
	 *        the day of the month
	 * @param hour
	 *        the hour of the day, from {@literal 0} for midnight
	 * @param minute
	 *        the minute of the hour
	 * @param second
	 *        the second of the minute
	 * @param millisecond
	 *        the millisecond of the second
	 * @return a new calendar instance
	 */
	public static XMLGregorianCalendar newXmlCalendar(int year, int month, int day, int hour, int minute,
			int second, int millisecond) {
		return newXmlCalendar(DATATYPE_FACTORY, year, month, day, hour, minute, second, millisecond);
	}

	/**
	 * Get a {@link XMLGregorianCalendar} for a specific time, set to the UTC
	 * time zone, using the {@link #DATATYPE_FACTORY}.
	 * 
	 * @param datatypeFactory
	 *        the factory to use
	 * @param year
	 *        the year
	 * @param month
	 *        the month of the year, starting with {@literal 1} for January
	 * @param day
	 *        the day of the month
	 * @param hour
	 *        the hour of the day, from {@literal 0} for midnight
	 * @param minute
	 *        the minute of the hour
	 * @param second
	 *        the second of the minute
	 * @param millisecond
	 *        the millisecond of the second
	 * @return a new calendar instance
	 */
	public static XMLGregorianCalendar newXmlCalendar(DatatypeFactory datatypeFactory, int year,
			int month, int day, int hour, int minute, int second, int millisecond) {
		GregorianCalendar cal = (GregorianCalendar) UTC_CALENDAR.clone();
		cal.set(year, month - 1, day, hour, minute, second);
		cal.set(Calendar.MILLISECOND, millisecond);
		return datatypeFactory.newXMLGregorianCalendar(cal);
	}

	/**
	 * Get an {@link Instant} from an XML date/time.
	 * 
	 * <p>
	 * You can use the current system time for the default value by passing
	 * {@code Instant::now} as the {@code defaultSupplier} parameter.
	 * </p>
	 * 
	 * @param cal
	 *        the XML calendar value, or {@literal null}
	 * @param defaultSupplier
	 *        if {@code xmlCal} is {@literal null}, a supplier to provide the
	 *        resulting value, or {@literal null} to return {@literal null}
	 * @return the instant, or {@literal null} if {@code defaultSupplier}
	 *         returns {@literal null} or was itself {@literal null}
	 */
	public static Instant timestamp(XMLGregorianCalendar cal, Supplier<Instant> defaultSupplier) {
		return cal != null ? Instant.ofEpochMilli(cal.toGregorianCalendar().getTimeInMillis())
				: defaultSupplier != null ? defaultSupplier.get() : null;
	}

}

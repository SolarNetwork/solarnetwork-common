/* ==================================================================
 * JodaBaseJsonDeserializer.java - Oct 22, 2014 10:56:24 AM
 * 
 * Copyright 2007-2014 SolarNetwork.net Dev Team
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

import java.util.TimeZone;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;

/**
 * Abstract {@link JsonDeserializer} class for converting strings into Joda
 * objects.
 * 
 * @author matt
 * @version 1.0
 */
public abstract class JodaBaseJsonDeserializer<T> extends StdScalarDeserializer<T> {

	private static final long serialVersionUID = -7136078168225301983L;

	/** The {@link DateTimeFormatter} for parsing dates. */
	protected final DateTimeFormatter formatter;

	/**
	 * Construct from a String date pattern.
	 * 
	 * @param clazz
	 *        the class type
	 * @param pattern
	 *        the Joda date format pattern
	 */
	public JodaBaseJsonDeserializer(Class<T> clazz, String pattern) {
		this(clazz, pattern, null);
	}

	/**
	 * Construct from a String date pattern.
	 * 
	 * @param clazz
	 *        the class type
	 * @param pattern
	 *        the Joda date format pattern
	 * @param timeZone
	 *        the time zone to format in
	 */
	public JodaBaseJsonDeserializer(Class<T> clazz, String pattern, TimeZone timeZone) {
		super(clazz);
		if ( timeZone != null ) {
			formatter = DateTimeFormat.forPattern(pattern).withZone(DateTimeZone.forTimeZone(timeZone));
		} else {
			formatter = DateTimeFormat.forPattern(pattern);
		}
	}

}

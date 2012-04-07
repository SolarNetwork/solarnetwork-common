/* ===================================================================
 * JodaDatePropertySerializer.java
 * 
 * Created Sep 24, 2009 2:48:09 PM
 * 
 * Copyright (c) 2009 Solarnetwork.net Dev Team.
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
 * ===================================================================
 * $Id$
 * ===================================================================
 */

package net.solarnetwork.util;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.joda.time.DateTimeZone;
import org.joda.time.ReadableInstant;
import org.joda.time.ReadablePartial;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * {@link PropertySerializer} for Joda date/time objects into Strings.
 *
 * @author matt
 * @version $Revision$ $Date$
 */
public class JodaDatePropertySerializer implements PropertySerializer {
	
	private DateTimeFormatter formatter = null;

	/**
	 * Construct from a String date pattern.
	 * 
	 * @param pattern the Joda date format pattern
	 */
	public JodaDatePropertySerializer(String pattern) {
		this(pattern, null);
	}
	
	/**
	 * Construct from a String date pattern.
	 * 
	 * @param pattern the Joda date format pattern
	 * @param timeZone the time zone to format in
	 */
	public JodaDatePropertySerializer(String pattern, TimeZone timeZone) {
		formatter = DateTimeFormat.forPattern(pattern);
		if ( timeZone != null ) {
			formatter = formatter.withZone(DateTimeZone.forTimeZone(timeZone));
		}
	}
	
	/* (non-Javadoc)
	 * @see net.sf.solarnetwork.util.PropertySerializer#serialize(java.lang.Object, java.lang.String, java.lang.Object)
	 */
	public Object serialize(Object data, String propertyName, Object propertyValue) {
		if ( propertyValue == null ) {
			return null;
		} else if ( propertyValue instanceof ReadableInstant  ) {
			return formatter.print((ReadableInstant)propertyValue);
		} else if ( propertyValue instanceof ReadablePartial ) {
			return formatter.print((ReadablePartial)propertyValue);
		} else if ( propertyValue instanceof Date ) {
			return formatter.print(((Date)propertyValue).getTime());
		} else if ( propertyValue instanceof Calendar ) {
			return formatter.print(((Calendar)propertyValue).getTimeInMillis());
		} 
		throw new IllegalArgumentException("Unsupported date object [" 
				+propertyValue.getClass() +"]: " +propertyValue);
	}

}

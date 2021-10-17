/* ==================================================================
 * TemporalPropertySerializer.java - 2/10/2021 10:01:25 PM
 * 
 * Copyright 2021 SolarNetwork.net Dev Team
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

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

/**
 * Property serializer for dates and times.
 * 
 * @author matt
 * @version 1.0
 * @since 2.0
 */
public class TemporalPropertySerializer implements PropertySerializer {

	private final DateTimeFormatter formatter;

	/**
	 * Constructor.
	 * 
	 * <p>
	 * The {@literal UTC} time zone will be used.
	 * </p>
	 * 
	 * @param pattern
	 *        the pattern to use
	 */
	public TemporalPropertySerializer(String pattern) {
		this(pattern, ZoneOffset.UTC);
	}

	/**
	 * Constructor.
	 * 
	 * @param pattern
	 *        the pattern to use
	 * @param zone
	 *        if not {@literal null} then a zone to use
	 */
	public TemporalPropertySerializer(String pattern, ZoneId zone) {
		this((zone != null ? DateTimeFormatter.ofPattern(pattern).withZone(zone)
				: DateTimeFormatter.ofPattern(pattern)));
	}

	/**
	 * Constructor.
	 * 
	 * @param formatter
	 *        the formatter to use
	 * @throws IllegalArgumentException
	 *         if any argument is {@literal null}
	 */
	public TemporalPropertySerializer(DateTimeFormatter formatter) {
		super();
		if ( formatter == null ) {
			throw new IllegalArgumentException("The formatter argument must not be null.");
		}
		this.formatter = formatter;
	}

	@Override
	public Object serialize(Object data, String propertyName, Object propertyValue) {
		if ( !(propertyValue instanceof TemporalAccessor) ) {
			return null;
		}
		return formatter.format((TemporalAccessor) propertyValue);
	}

}

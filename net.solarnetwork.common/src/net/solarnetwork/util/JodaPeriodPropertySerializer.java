/* ==================================================================
 * JodaPeriodPropertySerializer.java - Jun 22, 2011 6:41:35 PM
 * 
 * Copyright 2007-2011 SolarNetwork.net Dev Team
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

import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.ReadablePeriod;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;

/**
 * {@link PropertySerializer} for Joda Period and Duration objects into Strings.
 * 
 * <p>
 * {@link Duration} instances will be converted to {@link Period} instances via
 * {@link Duration#toPeriod()}.
 * </p>
 * 
 * @author matt
 * @version 1.0
 */
public class JodaPeriodPropertySerializer implements PropertySerializer {

	private final PeriodFormatter formatter = ISOPeriodFormat.standard();

	@Override
	public Object serialize(Object data, String propertyName, Object propertyValue) {
		if ( propertyValue == null ) {
			return null;
		} else if ( propertyValue instanceof ReadablePeriod ) {
			return formatter.print((ReadablePeriod) propertyValue);
		} else if ( propertyValue instanceof Duration ) {
			Period p = ((Duration) propertyValue).toPeriod();
			return formatter.print(p);
		}
		throw new IllegalArgumentException(
				"Unsupported date object [" + propertyValue.getClass() + "]: " + propertyValue);
	}

}

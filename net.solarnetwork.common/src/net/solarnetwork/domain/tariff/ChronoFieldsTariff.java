/* ==================================================================
 * ChronoFieldsTariff.java - 26/07/2024 11:18:38 am
 *
 * Copyright 2024 SolarNetwork.net Dev Team
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

package net.solarnetwork.domain.tariff;

import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.util.Locale;
import net.solarnetwork.util.IntRangeContainer;

/**
 * A tariff that supports chronologically related field components.
 *
 * @author matt
 * @version 1.0
 * @since 3.16
 */
public interface ChronoFieldsTariff extends Tariff {

	/**
	 * Get the range of a given field.
	 *
	 * @param field
	 *        the field
	 * @return the range, or {@literal null} if no range defined for the given
	 *         field
	 */
	IntRangeContainer rangeForChronoField(ChronoField field);

	/**
	 * Format the tariff value of a given field.
	 *
	 * @param field
	 *        the field to format
	 * @param locale
	 *        the locale
	 * @param style
	 *        the style
	 * @return the formatted tariff value
	 * @throws IllegalArgumentException
	 *         if {@code field} is not supported or any argument is
	 *         {@literal null}
	 */
	String formatChronoField(ChronoField field, Locale locale, TextStyle style);

}

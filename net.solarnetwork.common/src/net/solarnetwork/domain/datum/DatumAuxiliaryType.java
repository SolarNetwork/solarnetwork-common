/* ==================================================================
 * DatumAuxiliaryType.java - 1/02/2019 4:31:45 pm
 *
 * Copyright 2019 SolarNetwork.net Dev Team
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

package net.solarnetwork.domain.datum;

import org.jspecify.annotations.Nullable;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Enumeration of auxiliary datum types.
 *
 * @author matt
 * @version 1.2
 * @since 4.3
 */
public enum DatumAuxiliaryType {

	/** A data stream "reset" event, such as when hardware is replaced. */
	Reset,

	/** An arbitrary mark event, to annotate the datum stream. */
	Mark,

	/** A general annotation. */
	Annotation,

	/** A general flag. */
	Flag,

	/** A general note. */
	Note,

	;

	/**
	 * Get an enum instance for a name or key value.
	 *
	 * @param value
	 *        the enumeration name or key value, case-insensitive
	 * @return the enum, or {@code null} if value is {@code null} or empty
	 * @throws IllegalArgumentException
	 *         if {@code value} is not a valid value
	 */
	@JsonCreator
	public static @Nullable DatumAuxiliaryType fromValue(@Nullable String value) {
		if ( value == null || value.isEmpty() ) {
			return null;
		}
		try {
			return DatumAuxiliaryType.valueOf(value);
		} catch ( IllegalArgumentException ex ) {
			for ( DatumAuxiliaryType e : DatumAuxiliaryType.values() ) {
				if ( value.equalsIgnoreCase(e.name()) ) {
					return e;
				}
			}
		}
		throw new IllegalArgumentException("Unknown DatumAuxiliaryType value [" + value + "]");
	}
}

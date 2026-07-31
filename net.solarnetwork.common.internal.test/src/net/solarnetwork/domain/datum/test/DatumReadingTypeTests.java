/* ==================================================================
 * DatumReadingTypeTests.java - 31/07/2026 4:48:23 pm
 *
 * Copyright 2026 SolarNetwork.net Dev Team
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

package net.solarnetwork.domain.datum.test;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenIllegalArgumentException;
import org.junit.Test;
import net.solarnetwork.domain.datum.DatumReadingType;

/**
 * Test cases for the {@link DatumReadingType} enum.
 *
 * @author matt
 * @version 1.0
 */
public class DatumReadingTypeTests {

	@Test
	public void forKey() {
		for ( DatumReadingType e : DatumReadingType.values() ) {
			then(DatumReadingType.forKey(e.name())).as("Name matches").isEqualTo(e);
			then(DatumReadingType.forKey(e.getKey())).as("Key matches").isEqualTo(e);
			then(DatumReadingType.forKey(e.name().toUpperCase())).as("Case-insensitive name matches")
					.isEqualTo(e);
			then(DatumReadingType.forKey(e.getKey().toUpperCase())).as("Case-insensitive key matches")
					.isEqualTo(e);
		}
	}

	@Test
	public void forKey_null() {
		then(DatumReadingType.forKey(null)).as("Null resolves to None")
				.isEqualTo(DatumReadingType.NearestDifference);
	}

	@Test
	public void forKey_empty() {
		then(DatumReadingType.forKey("")).as("Empty resolves to None")
				.isEqualTo(DatumReadingType.NearestDifference);
	}

	@Test
	public void forKey_unknown() {
		thenIllegalArgumentException().as("Unsupported value results in IAE")
				.isThrownBy(() -> DatumReadingType.forKey("not a known value"));
	}

}

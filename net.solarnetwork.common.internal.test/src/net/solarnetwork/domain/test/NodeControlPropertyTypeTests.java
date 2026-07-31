/* ==================================================================
 * NodeControlPropertyTypeTests.java - 31/07/2026 10:59:37 am
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

package net.solarnetwork.domain.test;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenIllegalArgumentException;
import org.junit.Test;
import net.solarnetwork.domain.NodeControlPropertyType;

/**
 * Test cases for the {@link NodeControlPropertyType} enum.
 *
 * @author matt
 * @version 1.0
 */
public class NodeControlPropertyTypeTests {

	@Test
	public void valueFor() {
		for ( NodeControlPropertyType e : NodeControlPropertyType.values() ) {
			then(NodeControlPropertyType.fromValue(e.name())).as("Name matches").isEqualTo(e);
			then(NodeControlPropertyType.fromValue(String.valueOf(e.getKey()))).as("Key matches")
					.isEqualTo(e);
			then(NodeControlPropertyType.fromValue(e.name().toUpperCase()))
					.as("Case-insensitive name matches").isEqualTo(e);
			then(NodeControlPropertyType.fromValue(String.valueOf(Character.toUpperCase(e.getKey()))))
					.as("Case-insensitive key matches").isEqualTo(e);
		}
	}

	@Test
	public void valueFor_null() {
		thenIllegalArgumentException().as("Null results in IAE")
				.isThrownBy(() -> NodeControlPropertyType.fromValue(null));
	}

	@Test
	public void valueFor_empty() {
		thenIllegalArgumentException().as("Empty results in IAE")
				.isThrownBy(() -> NodeControlPropertyType.fromValue(""));
	}

	@Test
	public void valueFor_unknown() {
		thenIllegalArgumentException().as("Unsupported value results in IAE")
				.isThrownBy(() -> NodeControlPropertyType.fromValue("but not a known value"));
	}

}

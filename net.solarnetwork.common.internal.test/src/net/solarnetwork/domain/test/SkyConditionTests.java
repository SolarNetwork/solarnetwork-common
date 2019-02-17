/* ==================================================================
 * SkyConditionTests.java - 18/02/2019 9:50:25 am
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

package net.solarnetwork.domain.test;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import org.junit.Test;
import net.solarnetwork.domain.Bitmaskable;
import net.solarnetwork.domain.SkyCondition;

/**
 * Test cases for the {@link SkyCondition} enum.
 * 
 * @author matt
 * @version 1.0
 * @since 1.3
 */
public class SkyConditionTests {

	@Test
	public void bitmaskValueNull() {
		int mask = SkyCondition.bitmaskValue(null);
		assertThat("Null allowed", mask, equalTo(0));
	}

	@Test
	public void bitmaskValueEmpty() {
		int mask = SkyCondition.bitmaskValue(Collections.emptySet());
		assertThat("Empty set allowed", mask, equalTo(0));
	}

	@Test
	public void bitmaskValueSingle() {
		for ( SkyCondition c : SkyCondition.values() ) {
			int mask = SkyCondition.bitmaskValue(Collections.singleton(c));
			assertThat("Singleton set same as code shifted", mask, equalTo(1 << (c.getCode() - 1)));
		}
	}

	@Test
	public void bitmaskValueMulti() {
		Set<SkyCondition> set = EnumSet.of(SkyCondition.Cloudy, SkyCondition.Windy);
		int mask = Bitmaskable.bitmaskValue(set);
		int expected = (1 << (SkyCondition.Cloudy.getCode() - 1))
				| (1 << (SkyCondition.Windy.getCode() - 1));
		assertThat("Multi set bitmask", mask, equalTo(expected));
	}

	@Test
	public void conditionsForBitmaskZero() {
		assertThat("Conditions for 0 allowed", SkyCondition.conditionsForBitmask(0), hasSize(0));
	}

	@Test
	public void conditionsForBitmaskNegative() {
		assertThat("Conditions for negative allowed", SkyCondition.conditionsForBitmask(-1), hasSize(0));
	}

	@Test
	public void conditionsForBitmaskNoMatch() {
		assertThat("Conditions for no match", SkyCondition.conditionsForBitmask(1 << 31), hasSize(0));
	}

	@Test
	public void conditionsForBitmaskSingle() {
		for ( SkyCondition c : SkyCondition.values() ) {
			int mask = 1 << (c.getCode() - 1);
			Set<SkyCondition> set = SkyCondition.conditionsForBitmask(mask);
			assertThat("Singleton set same as code shifted", set, contains(c));
		}
	}

	@Test
	public void conditionsForBitmaskMulti() {
		int mask = (1 << (SkyCondition.Cloudy.getCode() - 1))
				| (1 << (SkyCondition.Windy.getCode() - 1));
		Set<SkyCondition> set = SkyCondition.conditionsForBitmask(mask);
		assertThat("Multi set bitmask", set,
				equalTo(EnumSet.of(SkyCondition.Cloudy, SkyCondition.Windy)));
	}

}

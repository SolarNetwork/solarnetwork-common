/* ==================================================================
 * LocalizedServiceInfoTests.java - 25/03/2024 5:34:50 pm
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

package net.solarnetwork.domain.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.junit.Test;
import net.solarnetwork.domain.BasicLocalizedServiceInfo;
import net.solarnetwork.domain.LocalizedServiceInfo;

/**
 * Test cases for the {@link LocalizedServiceInfo} class.
 *
 * @author matt
 * @version 1.0
 */
public class LocalizedServiceInfoTests {

	@Test
	public void sortByName() {
		// GIVEN
		LocalizedServiceInfo info1 = new BasicLocalizedServiceInfo("a", Locale.ENGLISH, "Zing Pow", null,
				null);
		LocalizedServiceInfo info2 = new BasicLocalizedServiceInfo("b", Locale.ENGLISH, "Service 2",
				null, null);
		LocalizedServiceInfo info3 = new BasicLocalizedServiceInfo("c", Locale.ENGLISH, "service 1",
				null, null);
		LocalizedServiceInfo info4 = new BasicLocalizedServiceInfo("d", Locale.ENGLISH, "A Service",
				null, null);

		// WHEN
		List<LocalizedServiceInfo> result = Arrays.asList(info1, info2, info3, info4).stream()
				.sorted(LocalizedServiceInfo.SORT_BY_NAME).collect(Collectors.toList());

		// THEN
		assertThat("Sorted by name", result, contains(info4, info3, info2, info1));
	}

}

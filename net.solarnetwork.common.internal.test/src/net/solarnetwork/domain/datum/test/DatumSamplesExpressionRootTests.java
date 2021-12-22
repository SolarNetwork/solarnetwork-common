/* ==================================================================
 * DatumSamplesExpressionRootTests.java - 14/05/2021 6:44:45 AM
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

package net.solarnetwork.domain.datum.test;

import static java.util.stream.Collectors.toCollection;
import static net.solarnetwork.domain.datum.DatumSamplesType.Accumulating;
import static net.solarnetwork.domain.datum.DatumSamplesType.Instantaneous;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.junit.Test;
import net.solarnetwork.domain.datum.DatumSamples;
import net.solarnetwork.domain.datum.DatumSamplesExpressionRoot;
import net.solarnetwork.domain.datum.GeneralDatum;

/**
 * Test cases for the {@link ExpressionRoot} class.
 * 
 * @author matt
 * @version 1.0
 */
public class DatumSamplesExpressionRootTests {

	private DatumSamplesExpressionRoot createTestRoot() {
		GeneralDatum d = new GeneralDatum("foo");
		d.putSampleValue(Instantaneous, "a", 3);
		d.putSampleValue(Instantaneous, "b", 5);
		d.putSampleValue(Accumulating, "c", 7);
		d.putSampleValue(Accumulating, "d", 9);

		DatumSamples s = new DatumSamples();
		d.putSampleValue(Instantaneous, "b", 21);
		d.putSampleValue(Instantaneous, "c", 23);
		d.putSampleValue(Accumulating, "e", 25);
		d.putSampleValue(Accumulating, "f", 25);

		Map<String, Object> p = new HashMap<>();
		p.put("d", 31);
		p.put("c", 33);
		p.put("f", 35);
		p.put("g", 35);

		return new DatumSamplesExpressionRoot(d, s, p);
	}

	@Test
	public void mergedMapProps() {
		// GIVEN
		DatumSamplesExpressionRoot root = createTestRoot();

		// THEN
		assertThat("Datum only prop", root.get("a"), is(equalTo(3)));
		assertThat("Sample overrides datum", root.get("b"), is(equalTo(21)));
		assertThat("Params override sample and datum", root.get("c"), is(equalTo(33)));
		assertThat("Params override datum", root.get("d"), is(equalTo(31)));
		assertThat("Sample only prop", root.get("e"), is(equalTo(25)));
		assertThat("Params override sample", root.get("f"), is(equalTo(35)));
		assertThat("Params only prop", root.get("g"), is(equalTo(35)));
	}

	@Test
	public void entrySet() {
		// GIVEN
		DatumSamplesExpressionRoot root = createTestRoot();

		// WHEN
		Set<Entry<String, Object>> set = root.entrySet();

		// THEN
		assertThat("Set size", set, hasSize(7));
		Set<String> keys = set.stream().map(Entry::getKey).collect(toCollection(LinkedHashSet::new));
		assertThat("Key order", keys, contains("a", "b", "c", "d", "e", "f", "g"));
	}

}
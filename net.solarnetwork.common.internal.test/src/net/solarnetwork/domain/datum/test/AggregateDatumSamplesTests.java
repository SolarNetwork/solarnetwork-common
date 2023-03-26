/* ==================================================================
 * AggregateDatumSamplesTests.java - 27/03/2023 10:12:12 am
 * 
 * Copyright 2023 SolarNetwork.net Dev Team
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import java.math.BigDecimal;
import org.junit.Test;
import net.solarnetwork.domain.datum.AggregateDatumSamples;
import net.solarnetwork.domain.datum.DatumSamples;

/**
 * Test cases for the {@link AggregateDatumSamples} class.
 * 
 * @author matt
 * @version 1.0
 */
public class AggregateDatumSamplesTests {

	private static final String PROP_1 = "a";
	private static final String PROP_2 = "b";
	private static final String PROP_3 = "c";
	private static final String PROP_MIN = "%s_min";
	private static final String PROP_MAX = "%s_max";

	@Test
	public void avg_fromSamples() {
		// GIVEN
		AggregateDatumSamples agg = new AggregateDatumSamples();

		// WHEN
		final int count = 4;
		for ( int i = 0; i < count; i++ ) {
			DatumSamples s = new DatumSamples();
			s.putInstantaneousSampleValue(PROP_1, i);
			s.putAccumulatingSampleValue(PROP_2, i * 2);
			s.putStatusSampleValue(PROP_3, i * i);
			s.addTag(String.valueOf(i));
			agg.addSample(s);
		}
		DatumSamples result = agg.average(2, PROP_MIN, PROP_MAX);

		// THEN
		assertThat("Average inst prop returned", result.getInstantaneousSampleBigDecimal(PROP_1),
				is(equalTo(new BigDecimal("1.5"))));
		assertThat("Last accumulating prop returned", result.getAccumulatingSampleInteger(PROP_2),
				is(equalTo(6)));
		assertThat("Last status prop returned", result.getStatusSampleInteger(PROP_3), is(equalTo(9)));
		assertThat("Tag union returned", result.getTags(), containsInAnyOrder("0", "1", "2", "3"));
	}

}

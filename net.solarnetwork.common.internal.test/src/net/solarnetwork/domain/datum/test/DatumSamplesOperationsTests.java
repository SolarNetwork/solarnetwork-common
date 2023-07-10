/* ==================================================================
 * DatumSamplesOperationsTests.java - 3/07/2023 10:02:55 am
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
import static org.hamcrest.Matchers.is;
import java.util.Collections;
import org.junit.Test;
import net.solarnetwork.domain.datum.DatumSamples;
import net.solarnetwork.domain.datum.DatumSamplesOperations;

/**
 * Test cases for the {@link DatumSamplesOperations} class.
 * 
 * @author matt
 * @version 1.0
 */
public class DatumSamplesOperationsTests {

	private static final String PROP_1 = "p1";
	private static final String PROP_2 = "p2";
	private static final String PROP_3 = "p3";
	private static final String TAG_1 = "t1";
	private static final String TAG_2 = "t2";

	@Test
	public void differsFrom_bothEmpty() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();

		DatumSamples s2 = new DatumSamples();

		// THEN
		assertThat("Property value equal", s1.differsFrom(s2), is(false));
		assertThat("Property value equal (reverse)", s2.differsFrom(s1), is(false));
	}

	@Test
	public void differsFrom_i_nonEqualValue() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putInstantaneousSampleValue(PROP_1, 1);

		DatumSamples s2 = new DatumSamples();
		s2.putInstantaneousSampleValue(PROP_1, 2);

		// THEN
		assertThat("Property value differs", s1.differsFrom(s2), is(true));
		assertThat("Property value differs (reverse)", s2.differsFrom(s1), is(true));
	}

	@Test
	public void differsFrom_i_equalValue() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putInstantaneousSampleValue(PROP_1, 1);

		DatumSamples s2 = new DatumSamples();
		s2.putInstantaneousSampleValue(PROP_1, 1);

		// THEN
		assertThat("Property value equal", s1.differsFrom(s2), is(false));
		assertThat("Property value equal (reverse)", s2.differsFrom(s1), is(false));
	}

	@Test
	public void differsFrom_i_oneNullMap() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putInstantaneousSampleValue(PROP_1, 1);

		DatumSamples s2 = new DatumSamples();

		// THEN
		assertThat("Property value differs", s1.differsFrom(s2), is(true));
		assertThat("Property value differs (reverse)", s2.differsFrom(s1), is(true));
	}

	@Test
	public void differsFrom_i_oneNullOneEmptyMap() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.setInstantaneous(Collections.emptyMap());

		DatumSamples s2 = new DatumSamples();

		// THEN
		assertThat("Property value equal", s1.differsFrom(s2), is(false));
		assertThat("Property value equal (reverse)", s2.differsFrom(s1), is(false));
	}

	@Test
	public void differsFrom_i_withEqualAccumulating() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putInstantaneousSampleValue(PROP_1, 1);
		s1.putAccumulatingSampleValue(PROP_2, 2);

		DatumSamples s2 = new DatumSamples();
		s2.putInstantaneousSampleValue(PROP_1, 1);
		s2.putAccumulatingSampleValue(PROP_2, 2);

		// THEN
		assertThat("Property value equal", s1.differsFrom(s2), is(false));
		assertThat("Property value equal (reverse)", s2.differsFrom(s1), is(false));
	}

	@Test
	public void differsFrom_i_withDifferentAccumulating() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putInstantaneousSampleValue(PROP_1, 1);
		s1.putAccumulatingSampleValue(PROP_2, 2);

		DatumSamples s2 = new DatumSamples();
		s2.putInstantaneousSampleValue(PROP_1, 1);
		s2.putAccumulatingSampleValue(PROP_2, 22);

		// THEN
		assertThat("Property value different", s1.differsFrom(s2), is(true));
		assertThat("Property value different (reverse)", s2.differsFrom(s1), is(true));
	}

	@Test
	public void differsFrom_i_withEqualStatus() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putInstantaneousSampleValue(PROP_1, 1);
		s1.putStatusSampleValue(PROP_3, "3");

		DatumSamples s2 = new DatumSamples();
		s2.putInstantaneousSampleValue(PROP_1, 1);
		s2.putStatusSampleValue(PROP_3, "3");

		// THEN
		assertThat("Property value equal", s1.differsFrom(s2), is(false));
		assertThat("Property value equal (reverse)", s2.differsFrom(s1), is(false));
	}

	@Test
	public void differsFrom_i_withDifferentStatus() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putInstantaneousSampleValue(PROP_1, 1);
		s1.putStatusSampleValue(PROP_3, "3");

		DatumSamples s2 = new DatumSamples();
		s2.putInstantaneousSampleValue(PROP_1, 1);
		s2.putStatusSampleValue(PROP_3, "3a");

		// THEN
		assertThat("Property value differs", s1.differsFrom(s2), is(true));
		assertThat("Property value differs (reverse)", s2.differsFrom(s1), is(true));
	}

	@Test
	public void differsFrom_i_withEqualTag() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putInstantaneousSampleValue(PROP_1, 1);
		s1.addTag(TAG_1);

		DatumSamples s2 = new DatumSamples();
		s2.putInstantaneousSampleValue(PROP_1, 1);
		s2.addTag(TAG_1);

		// THEN
		assertThat("Property value equal", s1.differsFrom(s2), is(false));
		assertThat("Property value equal (reverse)", s2.differsFrom(s1), is(false));
	}

	@Test
	public void differsFrom_i_withDifferentTag() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putInstantaneousSampleValue(PROP_1, 1);
		s1.addTag(TAG_1);

		DatumSamples s2 = new DatumSamples();
		s2.putInstantaneousSampleValue(PROP_1, 1);
		s2.addTag(TAG_2);

		// THEN
		assertThat("Property value differs", s1.differsFrom(s2), is(true));
		assertThat("Property value differs (reverse)", s2.differsFrom(s1), is(true));
	}

	@Test
	public void differsfrom_a_nonEqualValue() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putAccumulatingSampleValue(PROP_2, 1);

		DatumSamples s2 = new DatumSamples();
		s2.putAccumulatingSampleValue(PROP_2, 2);

		// THEN
		assertThat("Property value differs", s1.differsFrom(s2), is(true));
		assertThat("Property value differs (reverse)", s2.differsFrom(s1), is(true));
	}

	@Test
	public void differsfrom_a_equalValue() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putAccumulatingSampleValue(PROP_2, 1);

		DatumSamples s2 = new DatumSamples();
		s2.putAccumulatingSampleValue(PROP_2, 1);

		// THEN
		assertThat("Property value equal", s1.differsFrom(s2), is(false));
		assertThat("Property value equal (reverse)", s2.differsFrom(s1), is(false));
	}

	@Test
	public void differsfrom_a_oneNullMap() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putAccumulatingSampleValue(PROP_2, 1);

		DatumSamples s2 = new DatumSamples();

		// THEN
		assertThat("Property value differs", s1.differsFrom(s2), is(true));
		assertThat("Property value differs (reverse)", s2.differsFrom(s1), is(true));
	}

	@Test
	public void differsfrom_a_oneNullOneEmptyMap() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.setAccumulating(Collections.emptyMap());

		DatumSamples s2 = new DatumSamples();

		// THEN
		assertThat("Property value equal", s1.differsFrom(s2), is(false));
		assertThat("Property value equal (reverse)", s2.differsFrom(s1), is(false));
	}

	@Test
	public void differsFrom_a_withEqualInstantaneous() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putAccumulatingSampleValue(PROP_2, 2);
		s1.putInstantaneousSampleValue(PROP_1, 1);

		DatumSamples s2 = new DatumSamples();
		s2.putAccumulatingSampleValue(PROP_2, 2);
		s2.putInstantaneousSampleValue(PROP_1, 1);

		// THEN
		assertThat("Property value equal", s1.differsFrom(s2), is(false));
		assertThat("Property value equal (reverse)", s2.differsFrom(s1), is(false));
	}

	@Test
	public void differsFrom_a_withDifferentInstantaneous() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putAccumulatingSampleValue(PROP_2, 2);
		s1.putInstantaneousSampleValue(PROP_1, 1);

		DatumSamples s2 = new DatumSamples();
		s2.putAccumulatingSampleValue(PROP_2, 2);
		s2.putInstantaneousSampleValue(PROP_1, 11);

		// THEN
		assertThat("Property value different", s1.differsFrom(s2), is(true));
		assertThat("Property value different (reverse)", s2.differsFrom(s1), is(true));
	}

	@Test
	public void differsFrom_a_withEqualStatus() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putAccumulatingSampleValue(PROP_2, 2);
		s1.putStatusSampleValue(PROP_3, "3");

		DatumSamples s2 = new DatumSamples();
		s2.putAccumulatingSampleValue(PROP_2, 2);
		s2.putStatusSampleValue(PROP_3, "3");

		// THEN
		assertThat("Property value equal", s1.differsFrom(s2), is(false));
		assertThat("Property value equal (reverse)", s2.differsFrom(s1), is(false));
	}

	@Test
	public void differsFrom_a_withDifferentStatus() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putAccumulatingSampleValue(PROP_2, 2);
		s1.putStatusSampleValue(PROP_3, "3");

		DatumSamples s2 = new DatumSamples();
		s2.putAccumulatingSampleValue(PROP_2, 2);
		s2.putStatusSampleValue(PROP_3, "3a");

		// THEN
		assertThat("Property value differs", s1.differsFrom(s2), is(true));
		assertThat("Property value differs (reverse)", s2.differsFrom(s1), is(true));
	}

	@Test
	public void differsFrom_a_withEqualTag() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putAccumulatingSampleValue(PROP_2, 2);
		s1.addTag(TAG_1);

		DatumSamples s2 = new DatumSamples();
		s2.putAccumulatingSampleValue(PROP_2, 2);
		s2.addTag(TAG_1);

		// THEN
		assertThat("Property value equal", s1.differsFrom(s2), is(false));
		assertThat("Property value equal (reverse)", s2.differsFrom(s1), is(false));
	}

	@Test
	public void differsFrom_a_withDifferentTag() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putAccumulatingSampleValue(PROP_2, 2);
		s1.addTag(TAG_1);

		DatumSamples s2 = new DatumSamples();
		s2.putAccumulatingSampleValue(PROP_2, 2);
		s2.addTag(TAG_2);

		// THEN
		assertThat("Property value differs", s1.differsFrom(s2), is(true));
		assertThat("Property value differs (reverse)", s2.differsFrom(s1), is(true));
	}

	@Test
	public void differsFrom_s_nonEqualValue() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putStatusSampleValue(PROP_3, "3");

		DatumSamples s2 = new DatumSamples();
		s2.putStatusSampleValue(PROP_3, "33");

		// THEN
		assertThat("Property value differs", s1.differsFrom(s2), is(true));
		assertThat("Property value differs (reverse)", s2.differsFrom(s1), is(true));
	}

	@Test
	public void differsFrom_s_equalValue() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putStatusSampleValue(PROP_3, "3");

		DatumSamples s2 = new DatumSamples();
		s2.putStatusSampleValue(PROP_3, "3");

		// THEN
		assertThat("Property value equal", s1.differsFrom(s2), is(false));
		assertThat("Property value equal (reverse)", s2.differsFrom(s1), is(false));
	}

	@Test
	public void differsFrom_s_oneNullMap() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putStatusSampleValue(PROP_3, "3");

		DatumSamples s2 = new DatumSamples();

		// THEN
		assertThat("Property value differs", s1.differsFrom(s2), is(true));
		assertThat("Property value differs (reverse)", s2.differsFrom(s1), is(true));
	}

	@Test
	public void differsFrom_s_oneNullOneEmptyMap() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.setStatus(Collections.emptyMap());

		DatumSamples s2 = new DatumSamples();

		// THEN
		assertThat("Property value equal", s1.differsFrom(s2), is(false));
		assertThat("Property value equal (reverse)", s2.differsFrom(s1), is(false));
	}

	@Test
	public void differsFrom_s_withEqualInstantaneous() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putStatusSampleValue(PROP_3, "3");
		s1.putInstantaneousSampleValue(PROP_1, 1);

		DatumSamples s2 = new DatumSamples();
		s2.putStatusSampleValue(PROP_3, "3");
		s2.putInstantaneousSampleValue(PROP_1, 1);

		// THEN
		assertThat("Property value equal", s1.differsFrom(s2), is(false));
		assertThat("Property value equal (reverse)", s2.differsFrom(s1), is(false));
	}

	@Test
	public void differsFrom_s_withDifferentInstantaneous() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putStatusSampleValue(PROP_3, "3");
		s1.putInstantaneousSampleValue(PROP_1, 1);

		DatumSamples s2 = new DatumSamples();
		s2.putStatusSampleValue(PROP_3, "3");
		s2.putInstantaneousSampleValue(PROP_1, 2);

		// THEN
		assertThat("Property value differs", s1.differsFrom(s2), is(true));
		assertThat("Property value differs (reverse)", s2.differsFrom(s1), is(true));
	}

	@Test
	public void differsFrom_s_withEqualAccumulating() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putStatusSampleValue(PROP_3, "3");
		s1.putAccumulatingSampleValue(PROP_2, 2);

		DatumSamples s2 = new DatumSamples();
		s2.putStatusSampleValue(PROP_3, "3");
		s2.putAccumulatingSampleValue(PROP_2, 2);

		// THEN
		assertThat("Property value equal", s1.differsFrom(s2), is(false));
		assertThat("Property value equal (reverse)", s2.differsFrom(s1), is(false));
	}

	@Test
	public void differsFrom_s_withDifferentAccumulating() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putStatusSampleValue(PROP_3, "3");
		s1.putAccumulatingSampleValue(PROP_2, 2);

		DatumSamples s2 = new DatumSamples();
		s2.putStatusSampleValue(PROP_3, "3");
		s2.putAccumulatingSampleValue(PROP_2, 22);

		// THEN
		assertThat("Property value different", s1.differsFrom(s2), is(true));
		assertThat("Property value different (reverse)", s2.differsFrom(s1), is(true));
	}

	@Test
	public void differsFrom_s_withEqualTag() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putStatusSampleValue(PROP_3, "3");
		s1.addTag(TAG_1);

		DatumSamples s2 = new DatumSamples();
		s2.putStatusSampleValue(PROP_3, "3");
		s2.addTag(TAG_1);

		// THEN
		assertThat("Property value equal", s1.differsFrom(s2), is(false));
		assertThat("Property value equal (reverse)", s2.differsFrom(s1), is(false));
	}

	@Test
	public void differsFrom_s_withDifferentTag() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putStatusSampleValue(PROP_3, "3");
		s1.addTag(TAG_1);

		DatumSamples s2 = new DatumSamples();
		s2.putStatusSampleValue(PROP_3, "3");
		s2.addTag(TAG_2);

		// THEN
		assertThat("Property value differs", s1.differsFrom(s2), is(true));
		assertThat("Property value differs (reverse)", s2.differsFrom(s1), is(true));
	}

}

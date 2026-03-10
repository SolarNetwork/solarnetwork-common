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

import static org.assertj.core.api.BDDAssertions.then;
import java.math.BigDecimal;
import java.util.Collections;
import org.junit.Test;
import net.solarnetwork.domain.datum.DatumSamples;
import net.solarnetwork.domain.datum.DatumSamplesOperations;
import net.solarnetwork.util.NumberUtils;
import net.solarnetwork.util.StringUtils;

/**
 * Test cases for the
 * {@link DatumSamplesOperations#differsNumericallyFrom(DatumSamplesOperations, java.util.function.Function)}
 * method.
 *
 * @author matt
 * @version 1.0
 */
public class DatumSamplesOperations_NumberDiffTests {

	private static final String PROP_1 = "p1";
	private static final String PROP_2 = "p2";
	private static final String PROP_3 = "p3";
	private static final String TAG_1 = "t1";
	private static final String TAG_2 = "t2";

	@Test
	public void differsNumericallyFrom_bothEmpty() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();

		DatumSamples s2 = new DatumSamples();

		// THEN
		then(s1.differsNumericallyFrom(s2)).as("Both empty are not different").isFalse();
	}

	@Test
	public void differsNumericallyFrom_i_nonEqualValue() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putInstantaneousSampleValue(PROP_1, 1);

		DatumSamples s2 = new DatumSamples();
		s2.putInstantaneousSampleValue(PROP_1, 2);

		// THEN
		then(s1.differsNumericallyFrom(s2)).as("Property value differs").isTrue();
	}

	@Test
	public void differsNumericallyFrom_i_comparableValue_decimal() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putInstantaneousSampleValue(PROP_1, new BigDecimal("1.0"));

		DatumSamples s2 = new DatumSamples();
		s2.putInstantaneousSampleValue(PROP_1, new BigDecimal("1"));

		// THEN
		then(s1.differsNumericallyFrom(s2)).as("Property values are comparably equal").isFalse();
	}

	@Test
	public void differsNumericallyFrom_i_comparableValue_double() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putInstantaneousSampleValue(PROP_1, 1.0);

		DatumSamples s2 = new DatumSamples();
		s2.putInstantaneousSampleValue(PROP_1, (double) 1);

		// THEN
		then(s1.differsNumericallyFrom(s2)).as("Property values are comparably equal").isFalse();
	}

	@Test
	public void differsNumericallyFrom_i_comparableValue_misMatchTypes() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putInstantaneousSampleValue(PROP_1, 1.0);

		DatumSamples s2 = new DatumSamples();
		s2.putInstantaneousSampleValue(PROP_1, 1.0f);

		// THEN
		then(s1.differsNumericallyFrom(s2)).as("Property values of different types differ").isTrue();
	}

	@Test
	public void differsNumericallyFrom_i_comparableValue_misMatchTypes_mapped() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putInstantaneousSampleValue(PROP_1, 1);

		DatumSamples s2 = new DatumSamples();
		s2.putInstantaneousSampleValue(PROP_1, 1f);

		// THEN
		then(s1.differsNumericallyFrom(s2, StringUtils::numberValue, NumberUtils::bigDecimalForNumber))
				.as("Property values of different types mapped to BigDecimal are comparably equal")
				.isFalse();
	}

	@Test
	public void differsNumericallyFrom_i_equalValue() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putInstantaneousSampleValue(PROP_1, 1);

		DatumSamples s2 = new DatumSamples();
		s2.putInstantaneousSampleValue(PROP_1, 1);

		// THEN
		then(s1.differsNumericallyFrom(s2)).as("Property value equal").isFalse();
	}

	@Test
	public void differsNumericallyFrom_i_oneNullMap() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putInstantaneousSampleValue(PROP_1, 1);

		DatumSamples s2 = new DatumSamples();

		// THEN
		then(s1.differsNumericallyFrom(s2)).as("One null one not differ").isTrue();
		then(s2.differsNumericallyFrom(s1)).as("One null one not differ (reverse)").isTrue();
	}

	@Test
	public void differsNumericallyFrom_i_oneNullOneEmptyMap() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.setInstantaneous(Collections.emptyMap());

		DatumSamples s2 = new DatumSamples();

		// THEN
		then(s1.differsNumericallyFrom(s2)).as("One null one empty equal").isFalse();
		then(s2.differsNumericallyFrom(s1)).as("One null one empty equal (reverse)").isFalse();
	}

	@Test
	public void differsNumericallyFrom_i_withEqualAccumulating() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putInstantaneousSampleValue(PROP_1, 1);
		s1.putAccumulatingSampleValue(PROP_2, 2);

		DatumSamples s2 = new DatumSamples();
		s2.putInstantaneousSampleValue(PROP_1, 1);
		s2.putAccumulatingSampleValue(PROP_2, 2);

		// THEN
		then(s1.differsNumericallyFrom(s2)).as("All properties equal").isFalse();
	}

	@Test
	public void differsNumericallyFrom_i_withDifferentAccumulating() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putInstantaneousSampleValue(PROP_1, 1);
		s1.putAccumulatingSampleValue(PROP_2, 2);

		DatumSamples s2 = new DatumSamples();
		s2.putInstantaneousSampleValue(PROP_1, 1);
		s2.putAccumulatingSampleValue(PROP_2, 22);

		// THEN
		then(s1.differsNumericallyFrom(s2)).as("Different prop values differ").isTrue();
	}

	@Test
	public void differsNumericallyFrom_i_withEqualStatus() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putInstantaneousSampleValue(PROP_1, 1);
		s1.putStatusSampleValue(PROP_3, "3");

		DatumSamples s2 = new DatumSamples();
		s2.putInstantaneousSampleValue(PROP_1, 1);
		s2.putStatusSampleValue(PROP_3, "3");

		// THEN
		then(s1.differsNumericallyFrom(s2)).as("Status values parsed as numbers are equal").isFalse();
	}

	@Test
	public void differsNumericallyFrom_i_withDifferentStatus() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putInstantaneousSampleValue(PROP_1, 1);
		s1.putStatusSampleValue(PROP_3, "3");

		DatumSamples s2 = new DatumSamples();
		s2.putInstantaneousSampleValue(PROP_1, 1);
		s2.putStatusSampleValue(PROP_3, "33");

		// THEN
		then(s1.differsNumericallyFrom(s2)).as("Status values parsed as numbers differ").isTrue();
	}

	@Test
	public void differsNumericallyFrom_i_withDifferentStatus_oneNotNumber() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putInstantaneousSampleValue(PROP_1, 1);
		s1.putStatusSampleValue(PROP_3, "3");

		DatumSamples s2 = new DatumSamples();
		s2.putInstantaneousSampleValue(PROP_1, 1);
		s2.putStatusSampleValue(PROP_3, "3a");

		// THEN
		then(s1.differsNumericallyFrom(s2)).as("Status values parsed as numbers, one NaN, differ")
				.isTrue();
	}

	@Test
	public void differsNumericallyFrom_i_withDifferentTag() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putInstantaneousSampleValue(PROP_1, 1);
		s1.addTag(TAG_1);

		DatumSamples s2 = new DatumSamples();
		s2.putInstantaneousSampleValue(PROP_1, 1);
		s2.addTag(TAG_2);

		// THEN
		then(s1.differsNumericallyFrom(s2)).as("Tag values are ignored").isFalse();
	}

	@Test
	public void differsNumericallyFrom_a_nonEqualValue() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putAccumulatingSampleValue(PROP_2, 1);

		DatumSamples s2 = new DatumSamples();
		s2.putAccumulatingSampleValue(PROP_2, 2);

		// THEN
		then(s1.differsNumericallyFrom(s2)).as("Property values are different").isTrue();
	}

	@Test
	public void differsNumericallyFrom_a_equalValue() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putAccumulatingSampleValue(PROP_1, -1);
		s1.putAccumulatingSampleValue(PROP_2, 1);

		DatumSamples s2 = new DatumSamples();
		s2.putAccumulatingSampleValue(PROP_1, -1);
		s2.putAccumulatingSampleValue(PROP_2, 1);

		// THEN
		then(s1.differsNumericallyFrom(s2)).as("Property values are equal").isFalse();
	}

	@Test
	public void differsNumericallyFrom_a_comparableValue_decimal() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putAccumulatingSampleValue(PROP_1, new BigDecimal("1.0"));

		DatumSamples s2 = new DatumSamples();
		s2.putAccumulatingSampleValue(PROP_1, new BigDecimal("1"));

		// THEN
		then(s1.differsNumericallyFrom(s2)).as("Property values are comparably equal").isFalse();
	}

	@Test
	public void differsNumericallyFrom_a_comparableValue_double() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putAccumulatingSampleValue(PROP_1, 1.0);

		DatumSamples s2 = new DatumSamples();
		s2.putAccumulatingSampleValue(PROP_1, (double) 1);

		// THEN
		then(s1.differsNumericallyFrom(s2)).as("Property values are comparably equal").isFalse();
	}

	@Test
	public void differsNumericallyFrom_a_oneNullMap() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putAccumulatingSampleValue(PROP_2, 1);

		DatumSamples s2 = new DatumSamples();

		// THEN
		then(s1.differsNumericallyFrom(s2)).as("One null one not differ").isTrue();
		then(s2.differsNumericallyFrom(s1)).as("One null one not differ (reverse)").isTrue();
	}

	@Test
	public void differsNumericallyFrom_a_oneNullOneEmptyMap() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.setAccumulating(Collections.emptyMap());

		DatumSamples s2 = new DatumSamples();

		// THEN
		then(s1.differsNumericallyFrom(s2)).as("One null one empty equal").isFalse();
		then(s2.differsNumericallyFrom(s1)).as("One null one empty equal (reverse)").isFalse();
	}

	@Test
	public void differsNumericallyFrom_a_withEqualInstantaneous() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putAccumulatingSampleValue(PROP_2, 2);
		s1.putInstantaneousSampleValue(PROP_1, 1);

		DatumSamples s2 = new DatumSamples();
		s2.putAccumulatingSampleValue(PROP_2, 2);
		s2.putInstantaneousSampleValue(PROP_1, 1);

		// THEN
		then(s1.differsNumericallyFrom(s2)).as("All properties equal").isFalse();
	}

	@Test
	public void differsNumericallyFrom_a_withDifferentInstantaneous() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putAccumulatingSampleValue(PROP_2, 2);
		s1.putInstantaneousSampleValue(PROP_1, 1);

		DatumSamples s2 = new DatumSamples();
		s2.putAccumulatingSampleValue(PROP_2, 2);
		s2.putInstantaneousSampleValue(PROP_1, 11);

		// THEN
		then(s1.differsNumericallyFrom(s2)).as("Property values differ").isTrue();
	}

	@Test
	public void differsNumericallyFrom_a_withEqualStatus() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putAccumulatingSampleValue(PROP_2, 2);
		s1.putStatusSampleValue(PROP_3, "3");

		DatumSamples s2 = new DatumSamples();
		s2.putAccumulatingSampleValue(PROP_2, 2);
		s2.putStatusSampleValue(PROP_3, "3");

		// THEN
		then(s1.differsNumericallyFrom(s2)).as("Status values parsed as numbers are equal").isFalse();
	}

	@Test
	public void differsNumericallyFrom_a_withDifferentStatus() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putAccumulatingSampleValue(PROP_1, 1);
		s1.putStatusSampleValue(PROP_3, "3");

		DatumSamples s2 = new DatumSamples();
		s2.putAccumulatingSampleValue(PROP_1, 1);
		s2.putStatusSampleValue(PROP_3, "33");

		// THEN
		then(s1.differsNumericallyFrom(s2)).as("Status values parsed as numbers differ").isTrue();
	}

	@Test
	public void differsNumericallyFrom_a_withDifferentStatus_oneNotNumber() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putAccumulatingSampleValue(PROP_1, 1);
		s1.putStatusSampleValue(PROP_3, "3");

		DatumSamples s2 = new DatumSamples();
		s2.putAccumulatingSampleValue(PROP_1, 1);
		s2.putStatusSampleValue(PROP_3, "3a");

		// THEN
		then(s1.differsNumericallyFrom(s2)).as("Status values parsed as numbers, one NaN, differ")
				.isTrue();
	}

	@Test
	public void differsNumericallyFrom_a_withDifferentTag() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putAccumulatingSampleValue(PROP_1, 1);
		s1.addTag(TAG_1);

		DatumSamples s2 = new DatumSamples();
		s2.putAccumulatingSampleValue(PROP_1, 1);
		s2.addTag(TAG_2);

		// THEN
		then(s1.differsNumericallyFrom(s2)).as("Tag values are ignored").isFalse();
	}

	@Test
	public void differsNumericallyFrom_s_nonEqualValue() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putStatusSampleValue(PROP_1, "1");

		DatumSamples s2 = new DatumSamples();
		s2.putStatusSampleValue(PROP_1, "2");

		// THEN
		then(s1.differsNumericallyFrom(s2)).as("Property value differs").isTrue();
	}

	@Test
	public void differsNumericallyFrom_s_nonEqualValue_NaN() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putStatusSampleValue(PROP_1, "A");

		DatumSamples s2 = new DatumSamples();
		s2.putStatusSampleValue(PROP_1, "B");

		// THEN
		then(s1.differsNumericallyFrom(s2)).as("Property value strings differ").isTrue();
	}

	@Test
	public void differsNumericallyFrom_s_comparableValue_decimalIntegerMisMatch() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putStatusSampleValue(PROP_1, "1.0");

		DatumSamples s2 = new DatumSamples();
		s2.putStatusSampleValue(PROP_1, "1");

		// THEN
		then(s1.differsNumericallyFrom(s2))
				.as("Property values strings parsed as BigDecimal and BigInteger differ in type")
				.isTrue();
	}

	@Test
	public void differsNumericallyFrom_s_comparableValue_decimalIntegerMisMatch_mapped() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putStatusSampleValue(PROP_1, "1.0");

		DatumSamples s2 = new DatumSamples();
		s2.putStatusSampleValue(PROP_1, "1");

		// THEN
		then(s1.differsNumericallyFrom(s2, StringUtils::numberValue, NumberUtils::bigDecimalForNumber))
				.as("Property values strings parsed as BigDecimal and BigInteger mapped to decimal are comparably equal")
				.isFalse();
	}

	@Test
	public void differsNumericallyFrom_s_comparableValue_double() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putStatusSampleValue(PROP_1, 1.0);

		DatumSamples s2 = new DatumSamples();
		s2.putStatusSampleValue(PROP_1, (double) 1);

		// THEN
		then(s1.differsNumericallyFrom(s2)).as("Property values are comparably equal").isFalse();
	}

	@Test
	public void differsNumericallyFrom_s_comparableValue_misMatchTypes() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putStatusSampleValue(PROP_1, 1.0);

		DatumSamples s2 = new DatumSamples();
		s2.putStatusSampleValue(PROP_1, 1.0f);

		// THEN
		then(s1.differsNumericallyFrom(s2)).as("Property values of different types differ").isTrue();
	}

	@Test
	public void differsNumericallyFrom_s_comparableValue_misMatchTypes_mapped() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putStatusSampleValue(PROP_1, 1);

		DatumSamples s2 = new DatumSamples();
		s2.putStatusSampleValue(PROP_1, 1f);

		// THEN
		then(s1.differsNumericallyFrom(s2, StringUtils::numberValue, NumberUtils::bigDecimalForNumber))
				.as("Property values of different types mapped to BigDecimal are comparably equal")
				.isFalse();
	}

	@Test
	public void differsNumericallyFrom_s_equalValue() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putStatusSampleValue(PROP_1, 1);

		DatumSamples s2 = new DatumSamples();
		s2.putStatusSampleValue(PROP_1, 1);

		// THEN
		then(s1.differsNumericallyFrom(s2)).as("Property value equal").isFalse();
	}

	@Test
	public void differsNumericallyFrom_s_equalValue_NaN() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putStatusSampleValue(PROP_1, "A");

		DatumSamples s2 = new DatumSamples();
		s2.putStatusSampleValue(PROP_1, "A");

		// THEN
		then(s1.differsNumericallyFrom(s2)).as("Property value strings equal").isFalse();
	}

	@Test
	public void differsNumericallyFrom_s_oneNullMap() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putStatusSampleValue(PROP_1, 1);

		DatumSamples s2 = new DatumSamples();

		// THEN
		then(s1.differsNumericallyFrom(s2)).as("One null one not differ").isTrue();
		then(s2.differsNumericallyFrom(s1)).as("One null one not differ (reverse)").isTrue();
	}

	@Test
	public void differsNumericallyFrom_s_oneNullOneEmptyMap() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.setStatus(Collections.emptyMap());

		DatumSamples s2 = new DatumSamples();

		// THEN
		then(s1.differsNumericallyFrom(s2)).as("One null one empty equal").isFalse();
		then(s2.differsNumericallyFrom(s1)).as("One null one empty equal (reverse)").isFalse();
	}

	@Test
	public void differsNumericallyFrom_s_withEqualAccumulating() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putStatusSampleValue(PROP_1, 1);
		s1.putAccumulatingSampleValue(PROP_2, 2);

		DatumSamples s2 = new DatumSamples();
		s2.putStatusSampleValue(PROP_1, 1);
		s2.putAccumulatingSampleValue(PROP_2, 2);

		// THEN
		then(s1.differsNumericallyFrom(s2)).as("All properties equal").isFalse();
	}

	@Test
	public void differsNumericallyFrom_s_withDifferentAccumulating() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putStatusSampleValue(PROP_1, 1);
		s1.putAccumulatingSampleValue(PROP_2, 2);

		DatumSamples s2 = new DatumSamples();
		s2.putStatusSampleValue(PROP_1, 1);
		s2.putAccumulatingSampleValue(PROP_2, 22);

		// THEN
		then(s1.differsNumericallyFrom(s2)).as("Different prop values differ").isTrue();
	}

	@Test
	public void differsNumericallyFrom_s_withEqualStatus() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putStatusSampleValue(PROP_1, 1);
		s1.putStatusSampleValue(PROP_3, "3");

		DatumSamples s2 = new DatumSamples();
		s2.putStatusSampleValue(PROP_1, 1);
		s2.putStatusSampleValue(PROP_3, "3");

		// THEN
		then(s1.differsNumericallyFrom(s2)).as("Status values parsed as numbers are equal").isFalse();
	}

	@Test
	public void differsNumericallyFrom_s_withDifferentStatus() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putStatusSampleValue(PROP_1, 1);
		s1.putStatusSampleValue(PROP_3, "3");

		DatumSamples s2 = new DatumSamples();
		s2.putStatusSampleValue(PROP_1, 1);
		s2.putStatusSampleValue(PROP_3, "33");

		// THEN
		then(s1.differsNumericallyFrom(s2)).as("Status values parsed as numbers differ").isTrue();
	}

	@Test
	public void differsNumericallyFrom_s_withDifferentStatus_oneNotNumber() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putStatusSampleValue(PROP_1, 1);
		s1.putStatusSampleValue(PROP_3, "3");

		DatumSamples s2 = new DatumSamples();
		s2.putStatusSampleValue(PROP_1, 1);
		s2.putStatusSampleValue(PROP_3, "3a");

		// THEN
		then(s1.differsNumericallyFrom(s2)).as("Status values parsed as numbers, one NaN, differ")
				.isTrue();
	}

	@Test
	public void differsNumericallyFrom_s_withDifferentTag() {
		// GIVEN
		DatumSamples s1 = new DatumSamples();
		s1.putStatusSampleValue(PROP_1, 1);
		s1.addTag(TAG_1);

		DatumSamples s2 = new DatumSamples();
		s2.putStatusSampleValue(PROP_1, 1);
		s2.addTag(TAG_2);

		// THEN
		then(s1.differsNumericallyFrom(s2)).as("Tag values are ignored").isFalse();
	}

}

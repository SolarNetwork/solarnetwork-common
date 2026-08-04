/* ==================================================================
 * CollectionUtils_ValueAtPathTests.java - 5 Aug 2026 7:35:39 am
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

package net.solarnetwork.util.test;

import static org.assertj.core.api.BDDAssertions.then;
import java.util.Map;
import org.junit.Test;
import net.solarnetwork.test.CommonTestUtils;
import net.solarnetwork.util.CollectionUtils;

/**
 * Test cases for the various {@code valueAtPath} methods in
 * {@link CollectionUtils}.
 *
 * @author matt
 * @version 1.0
 */
public class CollectionUtils_ValueAtPathTests {

	private static final String TEST_MESSAGE = CommonTestUtils.randomString();
	private static final String TEST_NESTED_MESSAGE = CommonTestUtils.randomString();
	private static final String TEST_DEEPLY_NESTED_MESSAGE = CommonTestUtils.randomString();
	private static final Integer TEST_NUMBER = CommonTestUtils.randomInt();
	private static final Integer TEST_NESTED_NUMBER = CommonTestUtils.randomInt();
	private static final Integer TEST_DEEPLY_NESTED_NUMBER = CommonTestUtils.randomInt();

	private Map<String, Object> getTestInstance() {
		// @formatter:off
		return Map.of(
				"msg", TEST_MESSAGE,
				"num", TEST_NUMBER,
				"m", Map.of(
					"msg", TEST_NESTED_MESSAGE,
					"num", TEST_NESTED_NUMBER
				),
				"pm", Map.of(
					"foo", Map.of(
						"msg", TEST_DEEPLY_NESTED_MESSAGE,
						"num", TEST_DEEPLY_NESTED_NUMBER,
						"bar", Map.of(
							"bim", "bam"
						)
					)
				)
			);
		// @formatter:on
	}

	@Test
	public void valueAtPath_nullData() {
		// GIVEN
		final Map<String, Object> data = null;

		// WHEN
		final String result = CollectionUtils.valueAtPath("/msg", data, String.class);

		// THEN
		then(result).as("Null data resolves to null").isNull();
	}

	@Test
	public void valueAtPath_emptyData() {
		// GIVEN
		final Map<String, Object> data = Map.of();

		// WHEN
		final String result = CollectionUtils.valueAtPath("/msg", data, String.class);

		// THEN
		then(result).as("Empty data resolves to null").isNull();
	}

	@Test
	public void valueAtPath_nullPath() {
		// GIVEN
		final Map<String, Object> data = getTestInstance();

		// WHEN
		final String result = CollectionUtils.valueAtPath((String) null, data, String.class);

		// THEN
		then(result).as("Null path resolves to null").isNull();
	}

	@Test
	public void valueAtPath_emptyPath() {
		// GIVEN
		final Map<String, Object> data = getTestInstance();

		// WHEN
		final String result = CollectionUtils.valueAtPath("", data, String.class);

		// THEN
		then(result).as("Empth paty resolves to null").isNull();
	}

	@Test
	public void valueAtPath_root() {
		// GIVEN
		final Map<String, Object> data = getTestInstance();

		// WHEN
		final String result = CollectionUtils.valueAtPath("/msg", data, String.class);

		// THEN
		then(result).as("Root data resolved").isEqualTo(TEST_MESSAGE);
	}

	@Test
	public void valueAtPath_root_noLeadingSlash() {
		// GIVEN
		final Map<String, Object> data = getTestInstance();

		// WHEN
		final String result = CollectionUtils.valueAtPath("msg", data, String.class);

		// THEN
		then(result).as("Root data resolved (no leading slash)").isEqualTo(TEST_MESSAGE);
	}

	@Test
	public void valueAtPath_root_wrongType() {
		// GIVEN
		final Map<String, Object> data = getTestInstance();

		// WHEN
		final Integer result = CollectionUtils.valueAtPath("/msg", data, Integer.class);

		// THEN
		then(result).as("Root data of wrong type resolves to null").isNull();
	}

	@Test
	public void valueAtPath_root_number() {
		// GIVEN
		final Map<String, Object> data = getTestInstance();

		// WHEN
		final Integer result = CollectionUtils.valueAtPath("/num", data, Integer.class);

		// THEN
		then(result).as("Root data numberresolved").isEqualTo(TEST_NUMBER);
	}

	@Test
	public void valueAtPath_root_map() {
		// GIVEN
		final Map<String, Object> data = getTestInstance();

		// WHEN
		@SuppressWarnings("unchecked")
		final Map<String, Object> result = CollectionUtils.valueAtPath("/m", data, Map.class);

		// THEN
		then(result).as("Root data resolves nested map").isSameAs(data.get("m"));
	}

	@Test
	public void valueAtPath_root_notFound() {
		// GIVEN
		final Map<String, Object> data = getTestInstance();

		// WHEN
		final String result = CollectionUtils.valueAtPath("/msg_nope", data, String.class);

		// THEN
		then(result).as("Root data unknown key resolves to null").isNull();
	}

	@Test
	public void valueAtPath_nested() {
		// GIVEN
		final Map<String, Object> data = getTestInstance();

		// WHEN
		final String result = CollectionUtils.valueAtPath("/m/msg", data, String.class);

		// THEN
		then(result).as("Nested data resolved").isEqualTo(TEST_NESTED_MESSAGE);
	}

	@Test
	public void valueAtPath_nested_noLeadingSlash() {
		// GIVEN
		final Map<String, Object> data = getTestInstance();

		// WHEN
		final String result = CollectionUtils.valueAtPath("m/msg", data, String.class);

		// THEN
		then(result).as("Nested data resolved (no leading slash)").isEqualTo(TEST_NESTED_MESSAGE);
	}

	@Test
	public void valueAtPath_nested_wrongType() {
		// GIVEN
		final Map<String, Object> data = getTestInstance();

		// WHEN
		final Integer result = CollectionUtils.valueAtPath("/m/msg", data, Integer.class);

		// THEN
		then(result).as("Nested data of wrong type resolves to null").isNull();
	}

	@Test
	public void valueAtPath_nested_number() {
		// GIVEN
		final Map<String, Object> data = getTestInstance();

		// WHEN
		final Integer result = CollectionUtils.valueAtPath("/m/num", data, Integer.class);

		// THEN
		then(result).as("Nested data numberresolved").isEqualTo(TEST_NESTED_NUMBER);
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void valueAtPath_nested_map() {
		// GIVEN
		final Map<String, Object> data = getTestInstance();

		// WHEN
		@SuppressWarnings("unchecked")
		final Map<String, Object> result = CollectionUtils.valueAtPath("/pm/foo", data, Map.class);

		// THEN
		then(result).as("Nested data resolves nested map").isSameAs(((Map) data.get("pm")).get("foo"));
	}

	@Test
	public void valueAtPath_nested_notFound() {
		// GIVEN
		final Map<String, Object> data = getTestInstance();

		// WHEN
		final String result = CollectionUtils.valueAtPath("/m/msg_nope", data, String.class);

		// THEN
		then(result).as("Nested data unknown key resolves to null").isNull();
	}

	@Test
	public void valueAtPath_deeplyNested() {
		// GIVEN
		final Map<String, Object> data = getTestInstance();

		// WHEN
		final String result = CollectionUtils.valueAtPath("/pm/foo/msg", data, String.class);

		// THEN
		then(result).as("Nested data resolved").isEqualTo(TEST_DEEPLY_NESTED_MESSAGE);
	}

	@Test
	public void valueAtPath_deeplyNested_noLeadingSlash() {
		// GIVEN
		final Map<String, Object> data = getTestInstance();

		// WHEN
		final String result = CollectionUtils.valueAtPath("pm/foo/msg", data, String.class);

		// THEN
		then(result).as("Nested data resolved (no leading slash)").isEqualTo(TEST_DEEPLY_NESTED_MESSAGE);
	}

	@Test
	public void valueAtPath_deeplyNested_wrongType() {
		// GIVEN
		final Map<String, Object> data = getTestInstance();

		// WHEN
		final Integer result = CollectionUtils.valueAtPath("/pm/foo/msg", data, Integer.class);

		// THEN
		then(result).as("Nested data of wrong type resolves to null").isNull();
	}

	@Test
	public void valueAtPath_deeplyNested_number() {
		// GIVEN
		final Map<String, Object> data = getTestInstance();

		// WHEN
		final Integer result = CollectionUtils.valueAtPath("/pm/foo/num", data, Integer.class);

		// THEN
		then(result).as("Nested data numberresolved").isEqualTo(TEST_DEEPLY_NESTED_NUMBER);
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void valueAtPath_deeplyNested_map() {
		// GIVEN
		final Map<String, Object> data = getTestInstance();

		// WHEN
		@SuppressWarnings("unchecked")
		final Map<String, Object> result = CollectionUtils.valueAtPath("/pm/foo/bar", data, Map.class);

		// THEN
		then(result).as("Nested data resolves nested map")
				.isSameAs(((Map) ((Map) data.get("pm")).get("foo")).get("bar"));
	}

	@Test
	public void valueAtPath_deeplyNested_notFound() {
		// GIVEN
		final Map<String, Object> data = getTestInstance();

		// WHEN
		final String result = CollectionUtils.valueAtPath("/pm/foo/msg_nope", data, String.class);

		// THEN
		then(result).as("Nested data unknown key resolves to null").isNull();
	}

	@Test
	public void valueAtPath_deeplyNested_notFoundIntermediate() {
		// GIVEN
		final Map<String, Object> data = getTestInstance();

		// WHEN
		final String result = CollectionUtils.valueAtPath("/pm/nope/msg", data, String.class);

		// THEN
		then(result).as("Nested data unknown inermediate key resolves to null").isNull();
	}

	@Test
	public void valueAtPath_offsetStart() {
		// GIVEN
		final Map<String, Object> data = getTestInstance();

		// WHEN
		final String result = CollectionUtils.valueAtPath("/skip/m/msg", 1, data, String.class);

		// THEN
		then(result).as("Nested data resolved after skipping first path component")
				.isEqualTo(TEST_NESTED_MESSAGE);
	}

	@Test
	public void valueAtPath_offsetStart_noLeadingSlash() {
		// GIVEN
		final Map<String, Object> data = getTestInstance();

		// WHEN
		final String result = CollectionUtils.valueAtPath("skip/m/msg", 1, data, String.class);

		// THEN
		then(result).as("Nested data resolved after skipping first path component (no leading slash)")
				.isEqualTo(TEST_NESTED_MESSAGE);
	}

	@Test
	public void valueAtPath_offsetStart_2() {
		// GIVEN
		final Map<String, Object> data = getTestInstance();

		// WHEN
		final String result = CollectionUtils.valueAtPath("/skip/m/msg", 2, data, String.class);

		// THEN
		then(result).as("Nested data resolved after skipping first 2 path components")
				.isEqualTo(TEST_MESSAGE);
	}

	@Test
	public void valueAtPath_offsetStart_outOfBounds() {
		// GIVEN
		final Map<String, Object> data = getTestInstance();

		// WHEN
		final String result = CollectionUtils.valueAtPath("/skip/m/msg", 99, data, String.class);

		// THEN
		then(result).as("Null resolved after skipping all path components").isNull();
	}

}

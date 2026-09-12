/* ==================================================================
 * BasicMultiValueSettingSpecifierTests.java - 12 Sept 2026 1:03:36 pm
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

package net.solarnetwork.settings.support.test;

import static org.assertj.core.api.BDDAssertions.from;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.InstanceOfAssertFactories.map;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.junit.Test;
import org.springframework.context.support.ResourceBundleMessageSource;
import net.solarnetwork.domain.KeyedValue;
import net.solarnetwork.settings.support.BasicMultiValueSettingSpecifier;
import net.solarnetwork.test.CommonTestUtils;

/**
 * Test cases for the {@link BasicMultiValueSettingSpecifier} class.
 *
 * @author matt
 * @version 1.0
 */
public class BasicMultiValueSettingSpecifierTests {

	private enum MyEnum implements KeyedValue {

		Val1("v1"),

		Val2("v2"),

		;

		private final String key;

		private MyEnum(String key) {
			this.key = key;
		}

		@Override
		public String getKey() {
			return key;
		}
	}

	@Test
	public void enumSpec_noDefault() {
		// GIVEN
		final var msg = new ResourceBundleMessageSource();
		msg.setBasename(getClass().getName());

		final String key = CommonTestUtils.randomString();

		// WHEN
		final BasicMultiValueSettingSpecifier result = BasicMultiValueSettingSpecifier
				.enumSpec(MyEnum.class, key, null, msg, Locale.ENGLISH);

		// THEN
		final Map<String, String> expectedTitles = new LinkedHashMap<>();
		expectedTitles.put("", "");
		expectedTitles.put("Val1", "Value 1");
		expectedTitles.put("Val2", "Value 2");

		// @formatter:off
		then(result)
			.as("Instance created")
			.isNotNull()
			.as("Key configured")
			.returns(key, from(BasicMultiValueSettingSpecifier::getKey))
			.as("Empty default value configured")
			.returns("", from(BasicMultiValueSettingSpecifier::getDefaultValue))
			.extracting(BasicMultiValueSettingSpecifier::getValueTitles, map(String.class, String.class))
			.as("Titles generated for enum values, with no default")
			.containsExactlyEntriesOf(expectedTitles)
			;
		// @formatter:on
	}

	@Test
	public void enumSpec_withDefault() {
		// GIVEN
		final var msg = new ResourceBundleMessageSource();
		msg.setBasename(getClass().getName());

		final String key = CommonTestUtils.randomString();

		// WHEN
		final BasicMultiValueSettingSpecifier result = BasicMultiValueSettingSpecifier
				.enumSpec(MyEnum.class, key, MyEnum.Val2, msg, Locale.ENGLISH);

		// THEN
		final Map<String, String> expectedTitles = new LinkedHashMap<>();
		expectedTitles.put("Val1", "Value 1");
		expectedTitles.put("Val2", "Value 2");

		// @formatter:off
		then(result)
			.as("Instance created")
			.isNotNull()
			.as("Key configured")
			.returns(key, from(BasicMultiValueSettingSpecifier::getKey))
			.as("Empty default value configured")
			.returns(MyEnum.Val2.name(), from(BasicMultiValueSettingSpecifier::getDefaultValue))
			.extracting(BasicMultiValueSettingSpecifier::getValueTitles, map(String.class, String.class))
			.as("Titles generated for enum values, with no default")
			.containsExactlyEntriesOf(expectedTitles)
			;
		// @formatter:on
	}

	@Test
	public void keyedEnumSpec_noDefault() {
		// GIVEN
		final var msg = new ResourceBundleMessageSource();
		msg.setBasename(getClass().getName());

		final String key = CommonTestUtils.randomString();

		// WHEN
		final BasicMultiValueSettingSpecifier result = BasicMultiValueSettingSpecifier
				.keyedEnumSpec(MyEnum.class, key, null, msg, Locale.ENGLISH);

		// THEN
		final Map<String, String> expectedTitles = new LinkedHashMap<>();
		expectedTitles.put("", "");
		expectedTitles.put("v1", "Value 1");
		expectedTitles.put("v2", "Value 2");

		// @formatter:off
		then(result)
			.as("Instance created")
			.isNotNull()
			.as("Key configured")
			.returns(key, from(BasicMultiValueSettingSpecifier::getKey))
			.as("Empty default value configured")
			.returns("", from(BasicMultiValueSettingSpecifier::getDefaultValue))
			.extracting(BasicMultiValueSettingSpecifier::getValueTitles, map(String.class, String.class))
			.as("Titles generated for enum values using KeyedValue keys, with no default")
			.containsExactlyEntriesOf(expectedTitles)
			;
		// @formatter:on
	}

	@Test
	public void keyedEnumSpec_withDefault() {
		// GIVEN
		final var msg = new ResourceBundleMessageSource();
		msg.setBasename(getClass().getName());

		final String key = CommonTestUtils.randomString();

		// WHEN
		final BasicMultiValueSettingSpecifier result = BasicMultiValueSettingSpecifier
				.keyedEnumSpec(MyEnum.class, key, MyEnum.Val2, msg, Locale.ENGLISH);

		// THEN
		final Map<String, String> expectedTitles = new LinkedHashMap<>();
		expectedTitles.put("v1", "Value 1");
		expectedTitles.put("v2", "Value 2");

		// @formatter:off
		then(result)
			.as("Instance created")
			.isNotNull()
			.as("Key configured")
			.returns(key, from(BasicMultiValueSettingSpecifier::getKey))
			.as("Empty default value configured (using keyed value)")
			.returns(MyEnum.Val2.getKey(), from(BasicMultiValueSettingSpecifier::getDefaultValue))
			.extracting(BasicMultiValueSettingSpecifier::getValueTitles, map(String.class, String.class))
			.as("Titles generated for enum values using KeyedValue keys, with no default")
			.containsExactlyEntriesOf(expectedTitles)
			;
		// @formatter:on
	}

}

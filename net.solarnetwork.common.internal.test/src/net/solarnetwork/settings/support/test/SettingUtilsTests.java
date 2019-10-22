/* ==================================================================
 * SettingUtilsTests.java - 16/04/2018 9:31:54 AM
 * 
 * Copyright 2018 SolarNetwork.net Dev Team
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

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import net.solarnetwork.settings.GroupSettingSpecifier;
import net.solarnetwork.settings.SettingSpecifier;
import net.solarnetwork.settings.TextFieldSettingSpecifier;
import net.solarnetwork.settings.ToggleSettingSpecifier;
import net.solarnetwork.settings.support.BasicGroupSettingSpecifier;
import net.solarnetwork.settings.support.BasicParentSettingSpecifier;
import net.solarnetwork.settings.support.BasicTextFieldSettingSpecifier;
import net.solarnetwork.settings.support.BasicToggleSettingSpecifier;
import net.solarnetwork.settings.support.SettingUtils;

/**
 * Test cases for the {@link SettingUtils} class.
 * 
 * @author matt
 * @version 1.1
 */
public class SettingUtilsTests {

	@Test
	public void secureKeysNullSettings() {
		Set<String> result = SettingUtils.secureKeys(null);
		assertThat(result, hasSize(0));
	}

	@Test
	public void secureKeyEmptySettings() {
		Set<String> result = SettingUtils.secureKeys(Collections.<SettingSpecifier> emptyList());
		assertThat(result, hasSize(0));
	}

	@Test
	public void secureKeyNoSecureSettings() {
		List<SettingSpecifier> settings = asList(
				(SettingSpecifier) new BasicTextFieldSettingSpecifier("foo", "bar"));
		Set<String> result = SettingUtils.secureKeys(settings);
		assertThat(result, hasSize(0));
	}

	@Test
	public void secureKeyOneSecureSettings() {
		List<SettingSpecifier> settings = asList(
				(SettingSpecifier) new BasicTextFieldSettingSpecifier("foo", "bar"),
				(SettingSpecifier) new BasicTextFieldSettingSpecifier("bim", "bam", true));
		Set<String> result = SettingUtils.secureKeys(settings);
		assertThat(result, contains("bim"));
	}

	@Test
	public void secureKeyMultipleSecureSettings() {
		List<SettingSpecifier> settings = asList(
				(SettingSpecifier) new BasicTextFieldSettingSpecifier("foo", "bar"),
				(SettingSpecifier) new BasicTextFieldSettingSpecifier("bim", "bam", true),
				(SettingSpecifier) new BasicTextFieldSettingSpecifier("a", "b"),
				(SettingSpecifier) new BasicTextFieldSettingSpecifier("c", "d", true),
				(SettingSpecifier) new BasicTextFieldSettingSpecifier("e", "f", true),
				(SettingSpecifier) new BasicTextFieldSettingSpecifier("g", "h", true),
				(SettingSpecifier) new BasicTextFieldSettingSpecifier("i", "j"),
				(SettingSpecifier) new BasicTextFieldSettingSpecifier("k", "l", true));
		Set<String> result = SettingUtils.secureKeys(settings);
		assertThat(result, contains("bim", "c", "e", "g", "k"));
	}

	@Test
	public void mappedWithPrefix_null() {
		// WHEN
		List<SettingSpecifier> result = SettingUtils.mappedWithPrefix(null, null);

		// THEN
		assertThat("Null handled", result, nullValue());
	}

	@Test
	public void mappedWithPrefix_nullPrefix() {
		// GIVEN
		List<SettingSpecifier> settings = asList(
				(SettingSpecifier) new BasicTextFieldSettingSpecifier("foo", "bar"),
				(SettingSpecifier) new BasicToggleSettingSpecifier("tog", Boolean.TRUE));
		// WHEN
		List<SettingSpecifier> result = SettingUtils.mappedWithPrefix(settings, null);

		// THEN
		assertThat("Null prefix returns input list", result, sameInstance(settings));
	}

	@Test
	public void mappedWithPrefix_emptyPrefix() {
		// GIVEN
		List<SettingSpecifier> settings = asList(
				(SettingSpecifier) new BasicTextFieldSettingSpecifier("foo", "bar"),
				(SettingSpecifier) new BasicToggleSettingSpecifier("tog", Boolean.TRUE));
		// WHEN
		List<SettingSpecifier> result = SettingUtils.mappedWithPrefix(settings, "");

		// THEN
		assertThat("Empty prefix returns input list", result, sameInstance(settings));
	}

	@Test
	public void mappedWithPrefix_basic() {
		// GIVEN
		List<SettingSpecifier> settings = asList(
				(SettingSpecifier) new BasicTextFieldSettingSpecifier("foo", "bar"),
				(SettingSpecifier) new BasicToggleSettingSpecifier("tog", Boolean.TRUE),
				(SettingSpecifier) new BasicGroupSettingSpecifier("g", emptyList()));

		// WHEN
		List<SettingSpecifier> result = SettingUtils.mappedWithPrefix(settings, "p.");

		// THEN
		assertThat("Prefix mapping returns new list", result, not(sameInstance(settings)));
		assertThat("Prefix mapping has same size", result, hasSize(settings.size()));

		SettingSpecifier s = result.get(0);
		assertThat("Text field mapped into new text setting", s,
				allOf(instanceOf(TextFieldSettingSpecifier.class), not(sameInstance(settings.get(0)))));
		assertThat("Text field mapped key with prefix", ((TextFieldSettingSpecifier) s).getKey(),
				equalTo("p.foo"));
		assertThat("Text field default preserved", ((TextFieldSettingSpecifier) s).getDefaultValue(),
				equalTo("bar"));

		s = result.get(1);
		assertThat("Toggle field mapped into new toggle setting", s,
				allOf(instanceOf(ToggleSettingSpecifier.class), not(sameInstance(settings.get(1)))));
		assertThat("Toggle field mapped key with prefix", ((ToggleSettingSpecifier) s).getKey(),
				equalTo("p.tog"));
		assertThat("Toggle field default preserved", ((ToggleSettingSpecifier) s).getDefaultValue(),
				equalTo(Boolean.TRUE));

		s = result.get(2);
		assertThat("Group mapped into new group setting", s,
				allOf(instanceOf(GroupSettingSpecifier.class), not(sameInstance(settings.get(2)))));
	}

	@Test
	public void keyedData_null() {
		// WHEN
		Map<String, Object> data = SettingUtils.keyedSettingDefaults(null);

		// THEN
		assertThat("Null handled", data, notNullValue());
		assertThat("Empty map returned from null input", data.entrySet(), hasSize(0));
	}

	@Test
	public void keyedData_none() {
		// GIVEN
		List<SettingSpecifier> settings = asList((SettingSpecifier) new BasicParentSettingSpecifier(),
				(SettingSpecifier) new BasicGroupSettingSpecifier(emptyList()));

		// WHEN
		Map<String, Object> data = SettingUtils.keyedSettingDefaults(settings);

		// THEN
		assertThat("No keyed settings handled", data, notNullValue());
		assertThat("Empty map returned from no keyed input", data.entrySet(), hasSize(0));
	}

	@Test
	public void keyedData_basic() {
		// GIVEN
		List<SettingSpecifier> settings = asList(
				(SettingSpecifier) new BasicTextFieldSettingSpecifier("foo", "bar"),
				(SettingSpecifier) new BasicTextFieldSettingSpecifier("bim", "bam", true),
				(SettingSpecifier) new BasicToggleSettingSpecifier("tog", Boolean.TRUE),
				(SettingSpecifier) new BasicTextFieldSettingSpecifier("nul", null));

		// WHEN
		// WHEN
		Map<String, Object> data = SettingUtils.keyedSettingDefaults(settings);

		// THEN
		assertThat("Basic keyed settings handled", data, notNullValue());
		assertThat("Data map has one key for each input keyed setting", data.entrySet(), hasSize(4));
		assertThat("Data map has key value: foo", data, hasEntry("foo", "bar"));
		assertThat("Data map has key value: bim", data, hasEntry("bim", "bam"));
		assertThat("Data map has key value: tog", data, hasEntry("tog", Boolean.TRUE));
		assertThat("Data map has key value: nul", data, hasEntry("nul", null));
	}

	@Test
	public void keyedData_grouped() {
		// GIVEN
		List<SettingSpecifier> settings = asList(new BasicGroupSettingSpecifier("g",
				asList((SettingSpecifier) new BasicTextFieldSettingSpecifier("foo", "bar"),
						(SettingSpecifier) new BasicToggleSettingSpecifier("tog", Boolean.TRUE))));

		// WHEN
		// WHEN
		Map<String, Object> data = SettingUtils.keyedSettingDefaults(settings);

		// THEN
		assertThat("Group keyed settings handled", data, notNullValue());
		assertThat("Data map has one key for each input keyed setting", data.entrySet(), hasSize(2));
		assertThat("Data map has key value: foo", data, hasEntry("foo", "bar"));
		assertThat("Data map has key value: tog", data, hasEntry("tog", Boolean.TRUE));
	}

	@Test
	public void keyedData_parent() {
		// GIVEN
		BasicParentSettingSpecifier p = new BasicParentSettingSpecifier();
		p.setChildSettings(asList((SettingSpecifier) new BasicTextFieldSettingSpecifier("foo", "bar"),
				(SettingSpecifier) new BasicToggleSettingSpecifier("tog", Boolean.TRUE)));
		List<SettingSpecifier> settings = asList(p);

		// WHEN
		// WHEN
		Map<String, Object> data = SettingUtils.keyedSettingDefaults(settings);

		// THEN
		assertThat("Parent keyed settings handled", data, notNullValue());
		assertThat("Data map has one key for each input keyed setting", data.entrySet(), hasSize(2));
		assertThat("Data map has key value: foo", data, hasEntry("foo", "bar"));
		assertThat("Data map has key value: tog", data, hasEntry("tog", Boolean.TRUE));
	}
}

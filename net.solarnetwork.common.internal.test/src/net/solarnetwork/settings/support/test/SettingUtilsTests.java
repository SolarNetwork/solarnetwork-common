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

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.solarnetwork.codec.JsonUtils;
import net.solarnetwork.settings.CronExpressionSettingSpecifier;
import net.solarnetwork.settings.GroupSettingSpecifier;
import net.solarnetwork.settings.KeyedSettingSpecifier;
import net.solarnetwork.settings.MultiValueSettingSpecifier;
import net.solarnetwork.settings.ParentSettingSpecifier;
import net.solarnetwork.settings.RadioGroupSettingSpecifier;
import net.solarnetwork.settings.SettingSpecifier;
import net.solarnetwork.settings.SliderSettingSpecifier;
import net.solarnetwork.settings.TextAreaSettingSpecifier;
import net.solarnetwork.settings.TextFieldSettingSpecifier;
import net.solarnetwork.settings.TitleSettingSpecifier;
import net.solarnetwork.settings.ToggleSettingSpecifier;
import net.solarnetwork.settings.support.BaseSettingSpecifier;
import net.solarnetwork.settings.support.BasicCronExpressionSettingSpecifier;
import net.solarnetwork.settings.support.BasicGroupSettingSpecifier;
import net.solarnetwork.settings.support.BasicMultiValueSettingSpecifier;
import net.solarnetwork.settings.support.BasicParentSettingSpecifier;
import net.solarnetwork.settings.support.BasicRadioGroupSettingSpecifier;
import net.solarnetwork.settings.support.BasicSliderSettingSpecifier;
import net.solarnetwork.settings.support.BasicTextAreaSettingSpecifier;
import net.solarnetwork.settings.support.BasicTextFieldSettingSpecifier;
import net.solarnetwork.settings.support.BasicTitleSettingSpecifier;
import net.solarnetwork.settings.support.BasicToggleSettingSpecifier;
import net.solarnetwork.settings.support.SettingUtils;
import net.solarnetwork.util.StringUtils;

/**
 * Test cases for the {@link SettingUtils} class.
 *
 * @author matt
 * @version 1.3
 */
public class SettingUtilsTests {

	private static final Logger log = LoggerFactory.getLogger(SettingUtilsTests.class);

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

	@Test
	public void createBaseTemplateSpecification() {
		// GIVEN
		final String type = randomUUID().toString();
		BaseSettingSpecifier spec = new BaseSettingSpecifier() {

			@Override
			public String getType() {
				return type;
			}

		};
		final String title = randomUUID().toString();
		spec.setTitle(title);

		// WHEN
		Map<String, Object> result = SettingUtils.createBaseTemplateSpecification(spec, 2);

		// THEN
		assertThat("Result contains type", result, hasEntry("type", type));
		assertThat("Result contains title", result, hasEntry("title", title));
		assertThat("Result contains only type and title", result.keySet(), contains("type", "title"));
	}

	@Test
	public void createBaseTemplateSpecification_noTitle() {
		// GIVEN
		final String type = randomUUID().toString();
		BaseSettingSpecifier spec = new BaseSettingSpecifier() {

			@Override
			public String getType() {
				return type;
			}

		};

		// WHEN
		Map<String, Object> result = SettingUtils.createBaseTemplateSpecification(spec, 2);

		// THEN
		assertThat("Result contains type", result, hasEntry("type", type));
		assertThat("Result contains only type", result.keySet(), contains("type"));
	}

	private static final class TestCallbackArgs {

		private final SettingSpecifier spec;
		private final Map<String, Object> props;

		private TestCallbackArgs(SettingSpecifier spec, Map<String, Object> props) {
			super();
			this.spec = spec;
			this.props = props;
		}
	}

	private static final class TestCallback
			implements BiConsumer<SettingSpecifier, Map<String, Object>> {

		private final List<TestCallbackArgs> invocations = new ArrayList<>(4);

		@Override
		public void accept(SettingSpecifier spec, Map<String, Object> props) {
			invocations.add(new TestCallbackArgs(spec, props));
		}

	}

	private void assertCallback(TestCallback callback, int idx, SettingSpecifier spec,
			Map<String, Object> props) {
		assertThat(format("Callback invoked at least %d times", idx + 1), callback.invocations.size(),
				is(greaterThan(idx)));
		assertThat(format("Callback passed spec %d", idx), callback.invocations.get(idx).spec,
				is(sameInstance(spec)));
		assertThat(format("Callback passed props %d", idx), callback.invocations.get(idx).props,
				is(sameInstance(props)));
	}

	private void debugAsJson(String msg, Object o) {
		if ( log.isDebugEnabled() ) {
			log.debug(msg + ":\n" + JsonUtils.getJSONString(o, null));
		}
	}

	private void assertBaseSpecification(SettingSpecifier spec, Map<String, Object> props,
			Class<? extends SettingSpecifier> expectedType) {
		assertThat("Result contains type", props, hasEntry("type", expectedType.getName()));
		if ( spec.getTitle() != null && !spec.getTitle().isEmpty() ) {
			assertThat("Result contains title", props, hasEntry("title", spec.getTitle()));
		}
	}

	private void assertBaseKeyedSpecification(KeyedSettingSpecifier<?> spec, Map<String, Object> props,
			Class<? extends KeyedSettingSpecifier<?>> expectedType) {
		assertBaseSpecification(spec, props, expectedType);
		assertThat("Result contains key", props, hasEntry("key", spec.getKey()));
		if ( spec.getDefaultValue() != null && !spec.getDefaultValue().toString().isEmpty() ) {
			assertThat("Result contains defaultValue", props,
					hasEntry("defaultValue", spec.getDefaultValue()));
		}
		if ( spec.getDescriptionArguments() != null ) {
			assertThat("Result contains descriptionArguments",
					(Object[]) props.get("descriptionArguments"),
					is(arrayContaining(spec.getDescriptionArguments())));
		}
		assertThat("Result does NOT contain transient", props.keySet(), not(hasItems("transient")));
	}

	private void assertSliderSpecification(SliderSettingSpecifier spec, Map<String, Object> props) {
		debugAsJson("Slider specification", props);
		assertBaseKeyedSpecification(spec, props, SliderSettingSpecifier.class);
		assertThat("Result contains defaultValue", props,
				hasEntry("defaultValue", spec.getDefaultValue()));
		assertThat("Result contains minimumValue", props,
				hasEntry("minimumValue", spec.getMinimumValue()));
		assertThat("Result contains maximumValue", props,
				hasEntry("maximumValue", spec.getMaximumValue()));
		assertThat("Result contains step", props, hasEntry("step", spec.getStep()));
	}

	@Test
	public void populateTemplateSpecification_slider() {
		// GIVEN
		BasicSliderSettingSpecifier spec = new BasicSliderSettingSpecifier(randomUUID().toString(), 5.5,
				1.1, 9.9, 0.1);
		List<Map<String, Object>> list = new ArrayList<>(2);

		// WHEN
		SettingUtils.populateTemplateSpecification(spec, list, null);

		// THEN
		assertThat("Specification added", list, hasSize(1));
		Map<String, Object> result = list.get(0);
		assertSliderSpecification(spec, result);
	}

	@Test
	public void populateTemplateSpecification_slider_callback() {
		// GIVEN
		BasicSliderSettingSpecifier spec = new BasicSliderSettingSpecifier(randomUUID().toString(), 5.5,
				1.1, 9.9, 0.1);
		List<Map<String, Object>> list = new ArrayList<>(2);
		TestCallback callback = new TestCallback();

		// WHEN
		SettingUtils.populateTemplateSpecification(spec, list, callback);

		// THEN
		assertThat("Specification added", list, hasSize(1));
		Map<String, Object> result = list.get(0);
		assertSliderSpecification(spec, result);
		assertCallback(callback, 0, spec, result);
	}

	private void assertTextAreaSpecification(TextAreaSettingSpecifier spec, Map<String, Object> props) {
		debugAsJson("TextArea specification", props);
		assertBaseKeyedSpecification(spec, props, TextAreaSettingSpecifier.class);
		if ( spec.isDirect() ) {
			assertThat("Result contains direct", props, hasEntry("direct", spec.isDirect()));
		} else {
			assertThat("Result does NOT contain direct", props.keySet(), not(hasItems("direct")));
		}
	}

	@Test
	public void populateTemplateSpecification_textArea() {
		// GIVEN
		BasicTextAreaSettingSpecifier spec = new BasicTextAreaSettingSpecifier(randomUUID().toString(),
				"");
		List<Map<String, Object>> list = new ArrayList<>(4);

		// WHEN
		SettingUtils.populateTemplateSpecification(spec, list, null);

		// THEN
		assertThat("Specification added", list, hasSize(1));
		Map<String, Object> result = list.get(0);
		assertTextAreaSpecification(spec, result);
	}

	@Test
	public void populateTemplateSpecification_textArea_callback() {
		// GIVEN
		BasicTextAreaSettingSpecifier spec = new BasicTextAreaSettingSpecifier(randomUUID().toString(),
				"");
		List<Map<String, Object>> list = new ArrayList<>(4);
		TestCallback callback = new TestCallback();

		// WHEN
		SettingUtils.populateTemplateSpecification(spec, list, callback);

		// THEN
		assertThat("Specification added", list, hasSize(1));
		Map<String, Object> result = list.get(0);
		assertTextAreaSpecification(spec, result);
		assertCallback(callback, 0, spec, result);
	}

	@Test
	public void populateTemplateSpecification_textArea_withDefaultAndDescriptionArgumentsAndDirect() {
		// GIVEN
		BasicTextAreaSettingSpecifier spec = new BasicTextAreaSettingSpecifier(randomUUID().toString(),
				randomUUID().toString(), true);
		spec.setDescriptionArguments(new Object[] { "A", "B" });
		List<Map<String, Object>> list = new ArrayList<>(4);

		// WHEN
		SettingUtils.populateTemplateSpecification(spec, list, null);

		// THEN
		assertThat("Specification added", list, hasSize(1));
		Map<String, Object> result = list.get(0);
		assertTextAreaSpecification(spec, result);
	}

	private void assertBaseTitleSpecification(TitleSettingSpecifier spec, Map<String, Object> props,
			Class<? extends TitleSettingSpecifier> expectedType) {
		assertBaseKeyedSpecification(spec, props, expectedType);
		if ( spec.getValueTitles() != null ) {
			assertThat("Result contains valueTitles", props,
					hasEntry("valueTitles", spec.getValueTitles()));
		} else {
			assertThat("Result does NOT contain valueTitles", props.keySet(),
					not(hasItems("valueTitles")));
		}
		if ( spec.isMarkup() ) {
			assertThat("Result contains markup", props, hasEntry("markup", spec.isMarkup()));
		} else {
			assertThat("Result does NOT contain markup", props.keySet(), not(hasItems("markup")));
		}
	}

	private void assertTitleSpecification(TitleSettingSpecifier spec, Map<String, Object> props) {
		debugAsJson("Title specification", props);
		assertBaseTitleSpecification(spec, props, TitleSettingSpecifier.class);
	}

	@Test
	public void populateTemplateSpecification_title() {
		// GIVEN
		BasicTitleSettingSpecifier spec = new BasicTitleSettingSpecifier(randomUUID().toString(),
				"This be the title, arr.");
		List<Map<String, Object>> list = new ArrayList<>(4);

		// WHEN
		SettingUtils.populateTemplateSpecification(spec, list, null);

		// THEN
		assertThat("Specification added", list, hasSize(1));
		Map<String, Object> result = list.get(0);
		assertTitleSpecification(spec, result);
	}

	@Test
	public void populateTemplateSpecification_title_callback() {
		// GIVEN
		BasicTitleSettingSpecifier spec = new BasicTitleSettingSpecifier(randomUUID().toString(),
				"This be the title, arr.");
		List<Map<String, Object>> list = new ArrayList<>(4);
		TestCallback callback = new TestCallback();

		// WHEN
		SettingUtils.populateTemplateSpecification(spec, list, callback);

		// THEN
		assertThat("Specification added", list, hasSize(1));
		Map<String, Object> result = list.get(0);
		assertTitleSpecification(spec, result);
		assertCallback(callback, 0, spec, result);
	}

	@Test
	public void populateTemplateSpecification_title_withMarkup() {
		// GIVEN
		BasicTitleSettingSpecifier spec = new BasicTitleSettingSpecifier(randomUUID().toString(),
				"This be the title, arr.", false, true);
		List<Map<String, Object>> list = new ArrayList<>(4);

		// WHEN
		SettingUtils.populateTemplateSpecification(spec, list, null);

		// THEN
		assertThat("Specification added", list, hasSize(1));
		Map<String, Object> result = list.get(0);
		assertTitleSpecification(spec, result);
	}

	private void assertBaseTextFieldSpecification(TextFieldSettingSpecifier spec,
			Map<String, Object> props, Class<? extends TextFieldSettingSpecifier> expectedType) {
		assertBaseTitleSpecification(spec, props, expectedType);
		if ( spec.isSecureTextEntry() ) {
			assertThat("Result contains secureTextEntry", props,
					hasEntry("secureTextEntry", spec.isSecureTextEntry()));
		} else {
			assertThat("Result does NOT contain secureTextEntry", props.keySet(),
					not(hasItems("secureTextEntry")));
		}
	}

	private void assertTextFieldSpecification(TextFieldSettingSpecifier spec,
			Map<String, Object> props) {
		debugAsJson("TextField specification", props);
		assertBaseTextFieldSpecification(spec, props, TextFieldSettingSpecifier.class);
	}

	@Test
	public void populateTemplateSpecification_textField() {
		// GIVEN
		BasicTextFieldSettingSpecifier spec = new BasicTextFieldSettingSpecifier(randomUUID().toString(),
				"");
		List<Map<String, Object>> list = new ArrayList<>(4);

		// WHEN
		SettingUtils.populateTemplateSpecification(spec, list, null);

		// THEN
		assertThat("Specification added", list, hasSize(1));
		Map<String, Object> result = list.get(0);
		assertTextFieldSpecification(spec, result);
	}

	@Test
	public void populateTemplateSpecification_textField_callback() {
		// GIVEN
		BasicTextFieldSettingSpecifier spec = new BasicTextFieldSettingSpecifier(randomUUID().toString(),
				"");
		List<Map<String, Object>> list = new ArrayList<>(4);
		TestCallback callback = new TestCallback();

		// WHEN
		SettingUtils.populateTemplateSpecification(spec, list, callback);

		// THEN
		assertThat("Specification added", list, hasSize(1));
		Map<String, Object> result = list.get(0);
		assertTextFieldSpecification(spec, result);
		assertCallback(callback, 0, spec, result);
	}

	@Test
	public void populateTemplateSpecification_textField_withSecureTextEntry() {
		// GIVEN
		BasicTextFieldSettingSpecifier spec = new BasicTextFieldSettingSpecifier(randomUUID().toString(),
				"", true);
		List<Map<String, Object>> list = new ArrayList<>(4);

		// WHEN
		SettingUtils.populateTemplateSpecification(spec, list, null);

		// THEN
		assertThat("Specification added", list, hasSize(1));
		Map<String, Object> result = list.get(0);
		assertTextFieldSpecification(spec, result);
	}

	private void assertCronSpecification(TextFieldSettingSpecifier spec, Map<String, Object> props) {
		debugAsJson("Cron specification", props);
		assertBaseTextFieldSpecification(spec, props, CronExpressionSettingSpecifier.class);
	}

	@Test
	public void populateTemplateSpecification_cron() {
		// GIVEN
		BasicCronExpressionSettingSpecifier spec = new BasicCronExpressionSettingSpecifier(
				randomUUID().toString(), "0 * * * * *");
		List<Map<String, Object>> list = new ArrayList<>(4);

		// WHEN
		SettingUtils.populateTemplateSpecification(spec, list, null);

		// THEN
		assertThat("Specification added", list, hasSize(1));
		Map<String, Object> result = list.get(0);
		assertCronSpecification(spec, result);
	}

	@Test
	public void populateTemplateSpecification_cron_callback() {
		// GIVEN
		BasicCronExpressionSettingSpecifier spec = new BasicCronExpressionSettingSpecifier(
				randomUUID().toString(), "0 * * * * *");
		List<Map<String, Object>> list = new ArrayList<>(4);
		TestCallback callback = new TestCallback();

		// WHEN
		SettingUtils.populateTemplateSpecification(spec, list, callback);

		// THEN
		assertThat("Specification added", list, hasSize(1));
		Map<String, Object> result = list.get(0);
		assertCronSpecification(spec, result);
		assertCallback(callback, 0, spec, result);
	}

	private void assertMultiValueSpecification(MultiValueSettingSpecifier spec,
			Map<String, Object> props) {
		debugAsJson("MultiValue specification", props);
		assertBaseTextFieldSpecification(spec, props, MultiValueSettingSpecifier.class);
	}

	@Test
	public void populateTemplateSpecification_multiValue() {
		// GIVEN
		BasicMultiValueSettingSpecifier spec = new BasicMultiValueSettingSpecifier(
				randomUUID().toString(), "");
		Map<String, String> titles = new LinkedHashMap<>(4);
		titles.put("a", "Option 1");
		titles.put("b", "Option 2");
		titles.put("c", "Option 3");
		spec.setValueTitles(titles);
		List<Map<String, Object>> list = new ArrayList<>(4);

		// WHEN
		SettingUtils.populateTemplateSpecification(spec, list, null);

		// THEN
		assertThat("Specification added", list, hasSize(1));
		Map<String, Object> result = list.get(0);
		assertMultiValueSpecification(spec, result);
	}

	@Test
	public void populateTemplateSpecification_multiValue_callback() {
		// GIVEN
		BasicMultiValueSettingSpecifier spec = new BasicMultiValueSettingSpecifier(
				randomUUID().toString(), "");
		Map<String, String> titles = new LinkedHashMap<>(4);
		titles.put("a", "Option 1");
		titles.put("b", "Option 2");
		titles.put("c", "Option 3");
		spec.setValueTitles(titles);
		List<Map<String, Object>> list = new ArrayList<>(4);
		TestCallback callback = new TestCallback();

		// WHEN
		SettingUtils.populateTemplateSpecification(spec, list, callback);

		// THEN
		assertThat("Specification added", list, hasSize(1));
		Map<String, Object> result = list.get(0);
		assertMultiValueSpecification(spec, result);
		assertCallback(callback, 0, spec, result);
	}

	private void assertRadioGroupSpecification(RadioGroupSettingSpecifier spec,
			Map<String, Object> props) {
		debugAsJson("RadioGroup specification", props);
		assertBaseTextFieldSpecification(spec, props, RadioGroupSettingSpecifier.class);
		if ( spec.getFooterText() != null ) {
			assertThat("Result contains footerText", props,
					hasEntry("footerText", spec.getFooterText()));
		} else {
			assertThat("Result does NOT contain footerText", props.keySet(),
					not(hasItems("footerText")));
		}
	}

	@Test
	public void populateTemplateSpecification_radioGroup() {
		// GIVEN
		BasicRadioGroupSettingSpecifier spec = new BasicRadioGroupSettingSpecifier(
				randomUUID().toString(), "");
		Map<String, String> titles = new LinkedHashMap<>(4);
		titles.put("a", "Option 1");
		titles.put("b", "Option 2");
		titles.put("c", "Option 3");
		spec.setValueTitles(titles);
		List<Map<String, Object>> list = new ArrayList<>(4);

		// WHEN
		SettingUtils.populateTemplateSpecification(spec, list, null);

		// THEN
		assertThat("Specification added", list, hasSize(1));
		Map<String, Object> result = list.get(0);
		assertRadioGroupSpecification(spec, result);
	}

	@Test
	public void populateTemplateSpecification_radioGroup_callback() {
		// GIVEN
		BasicRadioGroupSettingSpecifier spec = new BasicRadioGroupSettingSpecifier(
				randomUUID().toString(), "");
		Map<String, String> titles = new LinkedHashMap<>(4);
		titles.put("a", "Option 1");
		titles.put("b", "Option 2");
		titles.put("c", "Option 3");
		spec.setValueTitles(titles);
		List<Map<String, Object>> list = new ArrayList<>(4);
		TestCallback callback = new TestCallback();

		// WHEN
		SettingUtils.populateTemplateSpecification(spec, list, callback);

		// THEN
		assertThat("Specification added", list, hasSize(1));
		Map<String, Object> result = list.get(0);
		assertRadioGroupSpecification(spec, result);
		assertCallback(callback, 0, spec, result);
	}

	@Test
	public void populateTemplateSpecification_radioGroup_withFooterText() {
		// GIVEN
		BasicRadioGroupSettingSpecifier spec = new BasicRadioGroupSettingSpecifier(
				randomUUID().toString(), "");
		Map<String, String> titles = new LinkedHashMap<>(4);
		titles.put("a", "Option 1");
		titles.put("b", "Option 2");
		titles.put("c", "Option 3");
		spec.setValueTitles(titles);
		spec.setFooterText(randomUUID().toString());
		List<Map<String, Object>> list = new ArrayList<>(4);

		// WHEN
		SettingUtils.populateTemplateSpecification(spec, list, null);

		// THEN
		assertThat("Specification added", list, hasSize(1));
		Map<String, Object> result = list.get(0);
		assertRadioGroupSpecification(spec, result);
	}

	private void assertToggleSpecification(ToggleSettingSpecifier spec, Map<String, Object> props) {
		debugAsJson("Toggle specification", props);
		assertBaseKeyedSpecification(spec, props, ToggleSettingSpecifier.class);
		if ( spec.getFalseValue() != null ) {
			assertThat("Result contains falseValue", props,
					hasEntry("falseValue", spec.getFalseValue()));
		} else {
			assertThat("Result does NOT contain falseValue", props.keySet(),
					not(hasItems("falseValue")));
		}
		if ( spec.getTrueValue() != null ) {
			assertThat("Result contains trueValue", props, hasEntry("trueValue", spec.getTrueValue()));
		} else {
			assertThat("Result does NOT contain trueValue", props.keySet(), not(hasItems("trueValue")));
		}
	}

	@Test
	public void populateTemplateSpecification_toggle() {
		// GIVEN
		BasicToggleSettingSpecifier spec = new BasicToggleSettingSpecifier(randomUUID().toString(),
				false);
		List<Map<String, Object>> list = new ArrayList<>(4);

		// WHEN
		SettingUtils.populateTemplateSpecification(spec, list, null);

		// THEN
		assertThat("Specification added", list, hasSize(1));
		Map<String, Object> result = list.get(0);
		assertToggleSpecification(spec, result);
	}

	@Test
	public void populateTemplateSpecification_toggle_callback() {
		// GIVEN
		BasicToggleSettingSpecifier spec = new BasicToggleSettingSpecifier(randomUUID().toString(),
				false);
		List<Map<String, Object>> list = new ArrayList<>(4);
		TestCallback callback = new TestCallback();

		// WHEN
		SettingUtils.populateTemplateSpecification(spec, list, callback);

		// THEN
		assertThat("Specification added", list, hasSize(1));
		Map<String, Object> result = list.get(0);
		assertToggleSpecification(spec, result);
		assertCallback(callback, 0, spec, result);
	}

	private void assertParentSpecification(ParentSettingSpecifier spec, Map<String, Object> props) {
		debugAsJson("Group specification", props);
		assertBaseSpecification(spec, props, ParentSettingSpecifier.class);
		if ( spec.getChildSettings() != null && !spec.getChildSettings().isEmpty() ) {
			List<Map<String, Object>> childProps = new ArrayList<>(2);
			for ( SettingSpecifier child : spec.getChildSettings() ) {
				SettingUtils.populateTemplateSpecification(child, childProps, null);
			}
			assertThat("Result contains childSettings", props, hasEntry("childSettings", childProps));
		} else {
			assertThat("Result does NOT contain childSettings", props.keySet(),
					not(hasItems("childSettings")));
		}
	}

	@Test
	public void populateTemplateSpecification_parent_simple() {
		// GIVEN
		List<SettingSpecifier> childSettings = asList(new BasicTextFieldSettingSpecifier("s[0]", ""));
		BasicParentSettingSpecifier spec = new BasicParentSettingSpecifier(childSettings);
		List<Map<String, Object>> list = new ArrayList<>(4);

		// WHEN
		SettingUtils.populateTemplateSpecification(spec, list, null);

		// THEN
		assertThat("Specification added", list, hasSize(1));
		Map<String, Object> result = list.get(0);
		assertParentSpecification(spec, result);
	}

	@Test
	public void populateTemplateSpecification_parent_simple_callback() {
		// GIVEN
		List<SettingSpecifier> childSettings = asList(new BasicTextFieldSettingSpecifier("s[0]", ""));
		BasicParentSettingSpecifier spec = new BasicParentSettingSpecifier(childSettings);
		List<Map<String, Object>> list = new ArrayList<>(4);
		TestCallback callback = new TestCallback();

		// WHEN
		SettingUtils.populateTemplateSpecification(spec, list, callback);

		// THEN
		assertThat("Specification added", list, hasSize(1));
		Map<String, Object> result = list.get(0);
		assertParentSpecification(spec, result);
		assertCallback(callback, 1, spec, result); // group comes after children
		@SuppressWarnings({ "unchecked", "rawtypes" })
		List<Map<String, Object>> resultChildren = (List) result.get("childSettings");
		assertCallback(callback, 0, childSettings.get(0), resultChildren.get(0));
	}

	private void assertGroupSpecification(GroupSettingSpecifier spec, Map<String, Object> props) {
		debugAsJson("Group specification", props);
		assertBaseSpecification(spec, props, GroupSettingSpecifier.class);
		if ( spec.getKey() != null ) {
			assertThat("Result contains key", props, hasEntry("key", spec.getKey()));
		} else {
			assertThat("Result does NOT contain key", props.keySet(), not(hasItems("key")));
		}
		if ( spec.isDynamic() ) {
			assertThat("Result contains dynamic", props, hasEntry("dynamic", spec.isDynamic()));
		} else {
			assertThat("Result does NOT contain dynamic", props.keySet(), not(hasItems("dynamic")));
		}
		if ( spec.getFooterText() != null ) {
			assertThat("Result contains footerText", props,
					hasEntry("footerText", spec.getFooterText()));
		} else {
			assertThat("Result does NOT contain footerText", props.keySet(),
					not(hasItems("footerText")));
		}
		if ( spec.getGroupSettings() != null && !spec.getGroupSettings().isEmpty() ) {
			List<Map<String, Object>> groupProps = new ArrayList<>(2);
			for ( SettingSpecifier child : spec.getGroupSettings() ) {
				SettingUtils.populateTemplateSpecification(child, groupProps, null);
			}
			assertThat("Result contains groupSettings", props, hasEntry("groupSettings", groupProps));
		} else {
			assertThat("Result does NOT contain groupSettings", props.keySet(),
					not(hasItems("groupSettings")));
		}
	}

	@Test
	public void populateTemplateSpecification_group_simple() {
		// GIVEN
		List<SettingSpecifier> groupSettings = asList(new BasicTextFieldSettingSpecifier("s[0]", ""));
		BasicGroupSettingSpecifier spec = new BasicGroupSettingSpecifier("s", groupSettings, true);
		List<Map<String, Object>> list = new ArrayList<>(4);

		// WHEN
		SettingUtils.populateTemplateSpecification(spec, list, null);

		// THEN
		assertThat("Specification added", list, hasSize(1));
		Map<String, Object> result = list.get(0);
		assertGroupSpecification(spec, result);
	}

	@Test
	public void populateTemplateSpecification_group_simple_callback() {
		// GIVEN
		List<SettingSpecifier> groupSettings = asList(new BasicTextFieldSettingSpecifier("s[0]", ""));
		BasicGroupSettingSpecifier spec = new BasicGroupSettingSpecifier("s", groupSettings, true);
		List<Map<String, Object>> list = new ArrayList<>(4);
		TestCallback callback = new TestCallback();

		// WHEN
		SettingUtils.populateTemplateSpecification(spec, list, callback);

		// THEN
		assertThat("Specification added", list, hasSize(1));
		Map<String, Object> result = list.get(0);
		assertGroupSpecification(spec, result);
		assertCallback(callback, 1, spec, result); // group comes after children
		@SuppressWarnings({ "unchecked", "rawtypes" })
		List<Map<String, Object>> resultChildren = (List) result.get("groupSettings");
		assertCallback(callback, 0, groupSettings.get(0), resultChildren.get(0));
	}

	@Test
	public void nonEmptyString_null() {
		assertThat("Null resolves to null", StringUtils.nonEmptyString(null), is(nullValue()));
		assertThat("Null resolves to null default", StringUtils.nonEmptyString(null, null),
				is(nullValue()));
		assertThat("Null resolves to non-null default", StringUtils.nonEmptyString(null, "foo"),
				is(equalTo("foo")));
	}

	@Test
	public void nonEmptyString_empty() {
		assertThat("Empty resolves to null", StringUtils.nonEmptyString(""), is(nullValue()));
		assertThat("Empty resolves to null default", StringUtils.nonEmptyString("", null),
				is(nullValue()));
		assertThat("Empty resolves to non-null default", StringUtils.nonEmptyString("", "foo"),
				is(equalTo("foo")));
	}

	@Test
	public void nonEmptyString_blank() {
		assertThat("Blank resolves to input", StringUtils.nonEmptyString(" "), is(equalTo(" ")));
		assertThat("Empty resolves to input not null default", StringUtils.nonEmptyString(" ", null),
				is(equalTo(" ")));
		assertThat("Empty resolves to input not non-null default",
				StringUtils.nonEmptyString(" ", "foo"), is(equalTo(" ")));
	}

}

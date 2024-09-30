/* ==================================================================
 * BaseSettingsSpecifierLocalizedServiceInfoProviderTests.java - 30/09/2024 4:26:10 pm
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

package net.solarnetwork.settings.support.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.junit.Test;
import org.springframework.context.support.ResourceBundleMessageSource;
import net.solarnetwork.domain.LocalizedServiceInfo;
import net.solarnetwork.settings.SettingSpecifier;
import net.solarnetwork.settings.support.BaseSettingsSpecifierLocalizedServiceInfoProvider;
import net.solarnetwork.settings.support.BasicTextFieldSettingSpecifier;

/**
 * Test cases for the {@link BaseSettingsSpecifierLocalizedServiceInfoProvider}
 * class.
 *
 * @author matt
 * @version 1.0
 */
public class BaseSettingsSpecifierLocalizedServiceInfoProviderTests {

	private static class TestSettingsSpecifierLocalizedServiceInfoProvider
			extends BaseSettingsSpecifierLocalizedServiceInfoProvider<String> {

		private TestSettingsSpecifierLocalizedServiceInfoProvider() {
			this("net.solarnetwork.settings.support.test.TestSettingsSpecifierLocalizedServiceInfoProvider");
		}

		private TestSettingsSpecifierLocalizedServiceInfoProvider(String bundleName) {
			this("test.service", bundleName);
		}

		private TestSettingsSpecifierLocalizedServiceInfoProvider(String id, String bundleName) {
			super(id);

			ResourceBundleMessageSource ms = new ResourceBundleMessageSource();
			ms.setBasename(bundleName);
			setMessageSource(ms);
		}

		@Override
		public String getDisplayName() {
			return "Test Service";
		}

		@Override
		public List<SettingSpecifier> getSettingSpecifiers() {
			List<SettingSpecifier> result = new ArrayList<>(1);
			result.add(new BasicTextFieldSettingSpecifier("prop1", null));
			return result;
		}

	}

	@Test
	public void id() {
		// GIVEN
		final String id = UUID.randomUUID().toString();
		final TestSettingsSpecifierLocalizedServiceInfoProvider service = new TestSettingsSpecifierLocalizedServiceInfoProvider(
				id,
				"net.solarnetwork.settings.support.test.TestSettingsSpecifierLocalizedServiceInfoProvider");

		// THEN
		assertThat("The 'id' value passed from constructor", service.getId(), is(equalTo(id)));
		assertThat("The 'settingsUid' value same as 'id'", service.getSettingUid(), is(equalTo(id)));

	}

	@Test
	public void infoMessages_withTitleAndDescription() {
		// GIVEN
		final TestSettingsSpecifierLocalizedServiceInfoProvider service = new TestSettingsSpecifierLocalizedServiceInfoProvider();

		// WHEN
		LocalizedServiceInfo info = service.getLocalizedServiceInfo(Locale.getDefault());

		// THEN
		assertThat("Localized info generated", info, is(notNullValue()));
		assertThat("Localized service properties provided", info.getLocalizedInfoMessages(),
				is(notNullValue()));
		assertThat("Localized service properties includes title, description, and settings keys",
				info.getLocalizedInfoMessages().keySet(),
				containsInAnyOrder("title", "desc", "prop1.key", "prop1.desc"));
		assertThat("Localized service properties extracted from resource bundle",
				info.getLocalizedInfoMessages(), hasEntry("title", "The Title"));
	}

	@Test
	public void infoMessages_withoutTitleAndDescription() {
		// GIVEN
		final TestSettingsSpecifierLocalizedServiceInfoProvider service = new TestSettingsSpecifierLocalizedServiceInfoProvider(
				"net.solarnetwork.settings.support.test.TestSettingsSpecifierLocalizedServiceInfoProvider2");

		// WHEN
		LocalizedServiceInfo info = service.getLocalizedServiceInfo(Locale.getDefault());

		// THEN
		assertThat("Localized info generated", info, is(notNullValue()));
		assertThat("Localized service properties provided", info.getLocalizedInfoMessages(),
				is(notNullValue()));
		assertThat("Localized service properties includes settings keys",
				info.getLocalizedInfoMessages().keySet(), containsInAnyOrder("prop1.key", "prop1.desc"));
		assertThat("Localized service properties extracted from resource bundle",
				info.getLocalizedInfoMessages(), hasEntry("prop1.key", "Property 1"));
	}

	@Test
	public void infoMessages_noSettings_withTitleAndDescription() {
		// GIVEN
		final TestSettingsSpecifierLocalizedServiceInfoProvider service = new TestSettingsSpecifierLocalizedServiceInfoProvider() {

			@Override
			public List<SettingSpecifier> getSettingSpecifiers() {
				return Collections.emptyList();
			}

		};

		// WHEN
		LocalizedServiceInfo info = service.getLocalizedServiceInfo(Locale.getDefault());

		// THEN
		assertThat("Localized info generated", info, is(notNullValue()));
		assertThat("Localized service properties provided", info.getLocalizedInfoMessages(),
				is(notNullValue()));
		assertThat("Localized service properties includes title, description",
				info.getLocalizedInfoMessages().keySet(), containsInAnyOrder("title", "desc"));
		assertThat("Localized service properties extracted from resource bundle",
				info.getLocalizedInfoMessages(), hasEntry("title", "The Title"));
	}

	@Test
	public void infoMessages_noSettings_withoutTitleAndDescription() {
		// GIVEN
		final TestSettingsSpecifierLocalizedServiceInfoProvider service = new TestSettingsSpecifierLocalizedServiceInfoProvider(
				"net.solarnetwork.settings.support.test.TestSettingsSpecifierLocalizedServiceInfoProvider3") {

			@Override
			public List<SettingSpecifier> getSettingSpecifiers() {
				return Collections.emptyList();
			}

		};

		// WHEN
		LocalizedServiceInfo info = service.getLocalizedServiceInfo(Locale.getDefault());

		// THEN
		assertThat("Localized info generated", info, is(notNullValue()));
		assertThat("Localized service properties provided", info.getLocalizedInfoMessages(),
				is(notNullValue()));
		assertThat("Localized service properties is empty", info.getLocalizedInfoMessages().keySet(),
				hasSize(0));
	}

}

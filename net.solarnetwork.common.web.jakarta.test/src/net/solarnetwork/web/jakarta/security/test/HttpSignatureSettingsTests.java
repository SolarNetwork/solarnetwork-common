/* ==================================================================
 * HttpSignatureSettingsTests.java - 20/09/2026 2:35:11 pm
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

package net.solarnetwork.web.jakarta.security.test;

import static org.assertj.core.api.BDDAssertions.and;
import static org.assertj.core.api.BDDAssertions.from;
import static org.assertj.core.api.BDDAssertions.then;
import java.util.EnumSet;
import java.util.Set;
import org.junit.Test;
import net.solarnetwork.security.http.sig.HttpSignatureAlgorithm;
import net.solarnetwork.test.CommonTestUtils;
import net.solarnetwork.web.jakarta.security.HttpSignatureSettings;

/**
 * Test cases for the {@link HttpSignatureSettings} class.
 *
 * @author matt
 * @version 1.0
 */
@SuppressWarnings("static-access")
public class HttpSignatureSettingsTests {

	@Test
	public void defaults() {
		// WHEN
		final HttpSignatureSettings settings = new HttpSignatureSettings();

		// THEN
		// @formatter:off
		then(settings)
			.as("Scheme is enabled by default")
			.returns(HttpSignatureSettings.DEFAULT_ENABLED, from(HttpSignatureSettings::isEnabled))
			.as("Tag defaults to the SolarNetwork application tag")
			.returns(HttpSignatureSettings.DEFAULT_TAG, from(HttpSignatureSettings::getTag))
			.as("A tag is not required by default")
			.returns(HttpSignatureSettings.DEFAULT_REQUIRE_TAG,
					from(HttpSignatureSettings::isRequireTag))
			.as("The token secret may be used directly by default")
			.returns(HttpSignatureSettings.DEFAULT_ALLOW_DIRECT_SECRET_KEY,
					from(HttpSignatureSettings::isAllowDirectSecretKey))
			.as("A derived signing key is allowed by default")
			.returns(HttpSignatureSettings.DEFAULT_ALLOW_DERIVED_SIGNING_KEY,
					from(HttpSignatureSettings::isAllowDerivedSigningKey))
			.as("Derived signing key lifetime defaults to a week")
			.returns(HttpSignatureSettings.DEFAULT_DERIVED_SIGNING_KEY_MAX_DAYS,
					from(HttpSignatureSettings::getDerivedSigningKeyMaxDays))
			;
		and.then(settings.getAlgorithms())
			.as("All supported algorithms are accepted by default")
			.containsExactlyInAnyOrderElementsOf(EnumSet.allOf(HttpSignatureAlgorithm.class))
			;
		// @formatter:on
	}

	@Test
	public void setters() {
		// GIVEN
		final String tag = CommonTestUtils.randomString();
		final int maxDays = CommonTestUtils.randomInt() & 0xFF;

		// WHEN
		final HttpSignatureSettings settings = new HttpSignatureSettings();
		settings.setEnabled(false);
		settings.setTag(tag);
		settings.setRequireTag(true);
		settings.setAllowDirectSecretKey(false);
		settings.setAllowDerivedSigningKey(false);
		settings.setDerivedSigningKeyMaxDays(maxDays);
		settings.setAlgorithms(Set.of(HttpSignatureAlgorithm.HmacSha256));

		// THEN
		// @formatter:off
		then(settings)
			.as("Enabled flag preserved")
			.returns(false, from(HttpSignatureSettings::isEnabled))
			.as("Tag preserved")
			.returns(tag, from(HttpSignatureSettings::getTag))
			.as("Require tag flag preserved")
			.returns(true, from(HttpSignatureSettings::isRequireTag))
			.as("Allow direct secret key flag preserved")
			.returns(false, from(HttpSignatureSettings::isAllowDirectSecretKey))
			.as("Allow derived signing key flag preserved")
			.returns(false, from(HttpSignatureSettings::isAllowDerivedSigningKey))
			.as("Derived signing key lifetime preserved")
			.returns(maxDays, from(HttpSignatureSettings::getDerivedSigningKeyMaxDays))
			;
		and.then(settings.getAlgorithms())
			.as("Explicit algorithms preserved")
			.containsExactly(HttpSignatureAlgorithm.HmacSha256)
			;
		// @formatter:on
	}

	@Test
	public void setTag_null() {
		// GIVEN
		final HttpSignatureSettings settings = new HttpSignatureSettings();
		settings.setTag(CommonTestUtils.randomString());

		// WHEN
		settings.setTag(null);

		// THEN
		// @formatter:off
		then(settings.getTag())
			.as("A null tag restores the default, as the tag is never null")
			.isEqualTo(HttpSignatureSettings.DEFAULT_TAG)
			;
		// @formatter:on
	}

	@Test
	public void setAlgorithms_null() {
		// GIVEN
		final HttpSignatureSettings settings = new HttpSignatureSettings();

		// WHEN
		settings.setAlgorithms(null);

		// THEN
		// @formatter:off
		then(settings.getAlgorithms())
			.as("A null algorithm set restores all supported algorithms")
			.containsExactlyInAnyOrderElementsOf(EnumSet.allOf(HttpSignatureAlgorithm.class))
			;
		// @formatter:on
	}

	@Test
	public void setAlgorithms_empty() {
		// GIVEN
		final HttpSignatureSettings settings = new HttpSignatureSettings();

		// WHEN
		settings.setAlgorithms(EnumSet.noneOf(HttpSignatureAlgorithm.class));

		// THEN
		// @formatter:off
		then(settings.getAlgorithms())
			.as("An empty algorithm set restores all supported algorithms, so no signature"
					+ " could ever be accepted")
			.containsExactlyInAnyOrderElementsOf(EnumSet.allOf(HttpSignatureAlgorithm.class))
			;
		// @formatter:on
	}

}

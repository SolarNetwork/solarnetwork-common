/* ==================================================================
 * DatumMetadataOperationsTests.java - 27/11/2025 3:35:56 pm
 *
 * Copyright 2025 SolarNetwork.net Dev Team
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

package net.solarnetwork.domain.test;

import static org.assertj.core.api.BDDAssertions.and;
import java.util.Locale;
import java.util.Map;
import org.junit.Test;
import net.solarnetwork.domain.datum.DatumMetadataOperations;
import net.solarnetwork.domain.datum.GeneralDatumMetadata;

/**
 * Test cases for the {@link DatumMetadataOperations} interface.
 *
 * @author matt
 * @version 1.0
 */
@SuppressWarnings("static-access")
public class DatumMetadataOperationsTests {

	@Test
	public void resolveLocale_empty() {
		// GIVEN
		final GeneralDatumMetadata meta = new GeneralDatumMetadata();

		// WHEN
		Locale result = meta.resolveLocale("/pm/foo/bar");

		// THEN
		// @formatter:off
		and.then(result)
			.as("Null metadata input results in default locale")
			.isEqualTo(Locale.ENGLISH)
			;
		// @formatter:on
	}

	@Test
	public void resolveLocale_empty_fallback() {
		// GIVEN
		final GeneralDatumMetadata meta = new GeneralDatumMetadata();

		// WHEN
		Locale result = meta.resolveLocale("/pm/foo/bar", Locale.CANADA);

		// THEN
		// @formatter:off
		and.then(result)
			.as("Null metadata input results in fallback locale")
			.isEqualTo(Locale.CANADA)
			;
		// @formatter:on
	}

	@Test
	public void resolveLocale_empty_nullFallback() {
		// GIVEN
		final GeneralDatumMetadata meta = new GeneralDatumMetadata();

		// WHEN
		Locale result = meta.resolveLocale("/pm/foo/bar", null);

		// THEN
		// @formatter:off
		and.then(result)
			.as("Null metadata input results in null fallback locale")
			.isEqualTo(null)
			;
		// @formatter:on
	}

	@Test
	public void resolveLocale_nullPath() {
		// GIVEN
		final GeneralDatumMetadata meta = new GeneralDatumMetadata(Map.of("foo", "bar"));

		// WHEN
		Locale result = meta.resolveLocale(null);

		// THEN
		// @formatter:off
		and.then(result)
			.as("Null path input results in default locale")
			.isEqualTo(Locale.ENGLISH)
			;
		// @formatter:on
	}

	@Test
	public void resolveLocale_emptyPath() {
		// GIVEN
		final GeneralDatumMetadata meta = new GeneralDatumMetadata(Map.of("foo", "bar"));

		// WHEN
		Locale result = meta.resolveLocale("");

		// THEN
		// @formatter:off
		and.then(result)
			.as("Null path input results in default locale")
			.isEqualTo(Locale.ENGLISH)
			;
		// @formatter:on
	}

	@Test
	public void resolveLocale_notFound() {
		final GeneralDatumMetadata meta = new GeneralDatumMetadata(
				new GeneralDatumMetadata(null, Map.of("foo", Map.of("bar", "baz"))));

		// WHEN
		Locale result = meta.resolveLocale("/pm/foo/bar");

		// THEN
		// @formatter:off
		and.then(result)
			.as("Non-matching path results in default locale")
			.isEqualTo(Locale.ENGLISH)
			;
		// @formatter:on
	}

	@Test
	public void resolveLocale_leaf() {
		final String localeId = "en-GB";

		// @formatter:off
		final GeneralDatumMetadata meta = new GeneralDatumMetadata(null,
				Map.of("foo", Map.of(
						"bar", "baz",
						"bar-locale", localeId
						)));
				// @formatter:off

		// WHEN
		Locale result = meta.resolveLocale("/pm/foo/bar");

		// THEN
		// @formatter:off
		and.then(result)
			.as("Matching path results in resolved locale")
			.isEqualTo(Locale.forLanguageTag(localeId))
			;
		// @formatter:on
	}

	@Test
	public void resolveLocale_leaf_invalid() {
		// @formatter:off
		final GeneralDatumMetadata meta = new GeneralDatumMetadata(null,
				Map.of("foo", Map.of(
						"bar", "baz",
						"bar-locale", "not a locale"
						)));
				// @formatter:off

		// WHEN
		Locale result = meta.resolveLocale("/pm/foo/bar");

		// THEN
		// @formatter:off
		and.then(result)
			.as("Non-parsable language tag results in exception")
			.isEqualTo(Locale.ENGLISH)
			;
		// @formatter:on
	}

	@Test
	public void ressolveLocale_example() {
		// @formatter:off
		final GeneralDatumMetadata meta = new GeneralDatumMetadata(
				Map.of(
						"a", 1,
						"a-locale", "fr",
						"locale", "en-US",
						"z", 99
					),
				Map.of("level1",
					Map.of(
						"locale", "en-CA",
						"level2a", Map.of(
							"b", 2,
							"b-locale", "en-GB",
							"c", 3
						),
						"level2b", Map.of(
							"d", 4
						),
						"level2b-locale", "de-DE"
					)
				)
				);
		// @formatter:off

		// WHEN

		// THEN
		and.then(meta.resolveLocale("/pm/level1/level2a/b"))
			.as("Sibling explicit locale resolved")
			.isEqualTo(Locale.forLanguageTag("en-GB"))
			;
		and.then(meta.resolveLocale("/pm/level1/level2a/c"))
			.as("Parent default locale resolved")
			.isEqualTo(Locale.forLanguageTag("en-CA"))
			;
		and.then(meta.resolveLocale("/pm/level1/level2b/d"))
			.as("Parent explicit locale resolved")
			.isEqualTo(Locale.forLanguageTag("de-DE"))
			;
		and.then(meta.resolveLocale("/pm/level1/level2a"))
			.as("Sibling default locale resolved")
			.isEqualTo(Locale.forLanguageTag("en-CA"))
			;
		and.then(meta.resolveLocale("/pm/level1"))
			.as("Global default locale resolved")
			.isEqualTo(Locale.forLanguageTag("en-US"))
			;
		and.then(meta.resolveLocale("/m/a"))
			.as("Sibling explicit locale resolved")
			.isEqualTo(Locale.forLanguageTag("fr"))
			;
		and.then(meta.resolveLocale("/m/z"))
			.as("Sibling default locale resolved")
			.isEqualTo(Locale.forLanguageTag("en-US"))
			;
		// @formatter:on
	}

}

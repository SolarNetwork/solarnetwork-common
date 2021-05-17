/* ==================================================================
 * SimpleTemporalTariffScheduleTests.java - 12/05/2021 5:32:46 PM
 * 
 * Copyright 2021 SolarNetwork.net Dev Team
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

package net.solarnetwork.domain.tariff.test;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.junit.Before;
import org.junit.Test;
import net.solarnetwork.domain.tariff.SimpleTariffRate;
import net.solarnetwork.domain.tariff.SimpleTemporalTariffSchedule;
import net.solarnetwork.domain.tariff.Tariff;
import net.solarnetwork.domain.tariff.TemporalRangesTariff;

/**
 * Test cases for the {@link SimpleTemporalTariffSchedule} class.
 * 
 * @author matt
 * @version 1.0
 */
public class SimpleTemporalTariffScheduleTests {

	private SimpleTemporalTariffSchedule schedule;

	@Before
	public void setup() {
		schedule = new SimpleTemporalTariffSchedule(createTestRules());
	}

	private List<TemporalRangesTariff> createTestRules() {
		List<TemporalRangesTariff> rules = new ArrayList<>(4);
		rules.add(new TemporalRangesTariff("Jan-Feb", null, null, null,
				asList(new SimpleTariffRate("a", BigDecimal.ONE)), Locale.getDefault()));
		rules.add(new TemporalRangesTariff("Mar-Nov", null, "Mon-Fri", "00:00-08:30",
				asList(new SimpleTariffRate("b", BigDecimal.ONE)), Locale.getDefault()));
		rules.add(new TemporalRangesTariff("Jun-Nov", null, null, null,
				asList(new SimpleTariffRate("c", BigDecimal.ONE)), Locale.getDefault()));
		return rules;
	}

	@Test
	public void resolve_noMatch() {
		// GIVEN
		LocalDateTime date = LocalDateTime.of(2021, 12, 1, 0, 0);

		// WHEN
		Tariff t = schedule.resolveTariff(date, null);

		// THEN
		assertThat("No match returned", t, nullValue());
	}

	@Test
	public void resolve_findFirst() {
		// GIVEN
		LocalDateTime date = LocalDateTime.of(2021, 5, 12, 6, 0);

		// WHEN
		Tariff t = schedule.resolveTariff(date, null);

		// THEN
		assertThat("First match returned", t, notNullValue());
		assertThat("B tariff matched", t.getRates().keySet(), contains("b"));
		assertThat("B tariff returned", t.getRates().get("b").getId(), equalTo("b"));
	}

	@Test
	public void resolve_findFirst_lastFallback() {
		// GIVEN
		LocalDateTime date = LocalDateTime.of(2021, 11, 1, 22, 0);

		// WHEN
		Tariff t = schedule.resolveTariff(date, null);

		// THEN
		assertThat("First match returned", t, notNullValue());
		assertThat("C tariff matched", t.getRates().keySet(), contains("c"));
		assertThat("C tariff returned", t.getRates().get("c").getId(), equalTo("c"));
	}

	@Test
	public void resolve_findAll_noMatch() {
		// GIVEN
		schedule.setFirstMatchOnly(false);
		LocalDateTime date = LocalDateTime.of(2021, 12, 1, 0, 0);

		// WHEN
		Tariff t = schedule.resolveTariff(date, null);

		// THEN
		assertThat("No match returned", t, nullValue());
	}

	@Test
	public void resolve_findAll_oneMatch() {
		// GIVEN
		schedule.setFirstMatchOnly(false);
		LocalDateTime date = LocalDateTime.of(2021, 5, 12, 6, 0);

		// WHEN
		Tariff t = schedule.resolveTariff(date, null);

		// THEN
		assertThat("First match returned", t, notNullValue());
		assertThat("B tariff matched", t.getRates().keySet(), contains("b"));
		assertThat("B tariff returned", t.getRates().get("b").getId(), equalTo("b"));
	}

	@Test
	public void resolve_findAll_multiMatch() {
		// GIVEN
		schedule.setFirstMatchOnly(false);
		LocalDateTime date = LocalDateTime.of(2021, 6, 7, 6, 0);

		// WHEN
		Tariff t = schedule.resolveTariff(date, null);

		// THEN
		assertThat("First match returned", t, notNullValue());
		assertThat("B, C tariffs matched", t.getRates().keySet(), contains("b", "c"));
		assertThat("B tariff returned", t.getRates().get("b").getId(), equalTo("b"));
		assertThat("C tariff returned", t.getRates().get("c").getId(), equalTo("c"));
	}

}

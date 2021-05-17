/* ==================================================================
 * TemporalRangesTariffTests.java - 12/05/2021 5:41:10 PM
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
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import net.solarnetwork.domain.tariff.SimpleTariffRate;
import net.solarnetwork.domain.tariff.TemporalRangesTariff;
import net.solarnetwork.domain.tariff.TemporalRangesTariffEvaluator;

/**
 * Test cases for the {@link TemporalRangesTariff} class.
 * 
 * @author matt
 * @version 1.0
 */
public class TemporalRangesTariffTests {

	private TemporalRangesTariffEvaluator evaluator;

	@Before
	public void setup() {
		evaluator = EasyMock.createMock(TemporalRangesTariffEvaluator.class);
	}

	@After
	public void teardown() {
		EasyMock.verify(evaluator);
	}

	private void replayAll() {
		EasyMock.replay(evaluator);
	}

	private TemporalRangesTariff createTestTariff() {
		SimpleTariffRate r = new SimpleTariffRate("rate", BigDecimal.ONE);
		return new TemporalRangesTariff("Mar-Nov", null, "Mon-Fri", "00:00-08:30", asList(r),
				Locale.getDefault());
	}

	@Test
	public void evaluate() {
		// GIVEN
		TemporalRangesTariff t = createTestTariff();
		LocalDateTime date = LocalDateTime.now();
		EasyMock.expect(evaluator.applies(t, date, null)).andReturn(true);

		// WHEN
		replayAll();
		boolean result = t.applies(evaluator, date, null);

		assertThat("Evaluator result returned", result, equalTo(true));
	}

}

/* ==================================================================
 * ChargingScheduleInfoTests.java - 19/02/2020 8:58:02 am
 * 
 * Copyright 2020 SolarNetwork.net Dev Team
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

package net.solarnetwork.ocpp.domain.test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import org.junit.Test;
import net.solarnetwork.ocpp.domain.ChargingScheduleInfo;
import net.solarnetwork.ocpp.domain.ChargingSchedulePeriodInfo;
import net.solarnetwork.ocpp.domain.UnitOfMeasure;

/**
 * Test cases for the {@link ChargingScheduleInfo} class.
 * 
 * @author matt
 * @version 1.0
 */
public class ChargingScheduleInfoTests {

	@Test
	public void copy() {
		ChargingScheduleInfo info = new ChargingScheduleInfo(Duration.ofHours(24), Instant.now(),
				UnitOfMeasure.W, new BigDecimal("0.1"));
		info.addPeriod(new ChargingSchedulePeriodInfo(Duration.ZERO, BigDecimal.ZERO));
		info.addPeriod(new ChargingSchedulePeriodInfo(Duration.ofHours(1), BigDecimal.ONE));
		info.addPeriod(new ChargingSchedulePeriodInfo(Duration.ofHours(2), BigDecimal.TEN));

		ChargingScheduleInfo copy = new ChargingScheduleInfo(info);
		assertThat("Copied duration", copy.getDuration(), sameInstance(info.getDuration()));
		assertThat("Copied start", copy.getStart(), sameInstance(info.getStart()));
		assertThat("Copied rate unit", copy.getRateUnit(), sameInstance(info.getRateUnit()));
		assertThat("Copied min rate", copy.getMinRate(), sameInstance(info.getMinRate()));
	}

	@Test
	public void same_withPeriods() {
		Instant now = Instant.now();
		ChargingScheduleInfo a = new ChargingScheduleInfo(Duration.ofHours(24), now, UnitOfMeasure.W,
				new BigDecimal("0.1"));
		a.addPeriod(new ChargingSchedulePeriodInfo(Duration.ZERO, BigDecimal.ZERO));
		a.addPeriod(new ChargingSchedulePeriodInfo(Duration.ofHours(1), BigDecimal.ONE));
		a.addPeriod(new ChargingSchedulePeriodInfo(Duration.ofHours(2), BigDecimal.TEN));

		ChargingScheduleInfo b = new ChargingScheduleInfo(Duration.ofHours(24), now, UnitOfMeasure.W,
				new BigDecimal("0.1"));
		b.addPeriod(new ChargingSchedulePeriodInfo(Duration.ZERO, BigDecimal.ZERO));
		b.addPeriod(new ChargingSchedulePeriodInfo(Duration.ofHours(1), BigDecimal.ONE));
		b.addPeriod(new ChargingSchedulePeriodInfo(Duration.ofHours(2), BigDecimal.TEN));

		assertThat("Same", a.isSameAs(b), equalTo(true));
		assertThat("Not different", a.differsFrom(b), equalTo(false));
	}

	@Test
	public void different_duration() {
		Instant now = Instant.now();
		ChargingScheduleInfo a = new ChargingScheduleInfo(Duration.ofHours(24), now, UnitOfMeasure.W,
				BigDecimal.ZERO);

		ChargingScheduleInfo b = new ChargingScheduleInfo(Duration.ofHours(20), now, UnitOfMeasure.W,
				BigDecimal.ZERO);

		assertThat("Not same", a.isSameAs(b), equalTo(false));
		assertThat("Different", a.differsFrom(b), equalTo(true));
	}

	@Test
	public void different_start() {
		Instant now = Instant.now();
		ChargingScheduleInfo a = new ChargingScheduleInfo(Duration.ofHours(24), now, UnitOfMeasure.W,
				BigDecimal.ZERO);

		ChargingScheduleInfo b = new ChargingScheduleInfo(Duration.ofHours(24), now.plusSeconds(1),
				UnitOfMeasure.W, BigDecimal.ZERO);

		assertThat("Not same", a.isSameAs(b), equalTo(false));
		assertThat("Different", a.differsFrom(b), equalTo(true));
	}

	@Test
	public void different_rateUnit() {
		Instant now = Instant.now();
		ChargingScheduleInfo a = new ChargingScheduleInfo(Duration.ofHours(24), now, UnitOfMeasure.W,
				BigDecimal.ZERO);

		ChargingScheduleInfo b = new ChargingScheduleInfo(Duration.ofHours(24), now, UnitOfMeasure.A,
				BigDecimal.ZERO);

		assertThat("Not same", a.isSameAs(b), equalTo(false));
		assertThat("Different", a.differsFrom(b), equalTo(true));
	}

	@Test
	public void different_minRate() {
		Instant now = Instant.now();
		ChargingScheduleInfo a = new ChargingScheduleInfo(Duration.ofHours(24), now, UnitOfMeasure.W,
				BigDecimal.ZERO);

		ChargingScheduleInfo b = new ChargingScheduleInfo(Duration.ofHours(24), now, UnitOfMeasure.W,
				BigDecimal.ONE);

		assertThat("Not same", a.isSameAs(b), equalTo(false));
		assertThat("Different", a.differsFrom(b), equalTo(true));
	}

	@Test
	public void different_periods() {
		Instant now = Instant.now();
		ChargingScheduleInfo info = new ChargingScheduleInfo(Duration.ofHours(24), now, UnitOfMeasure.W,
				new BigDecimal("0.1"));
		info.addPeriod(new ChargingSchedulePeriodInfo(Duration.ZERO, BigDecimal.ZERO));
		info.addPeriod(new ChargingSchedulePeriodInfo(Duration.ofHours(1), BigDecimal.ONE));
		info.addPeriod(new ChargingSchedulePeriodInfo(Duration.ofHours(2), BigDecimal.TEN));

		ChargingScheduleInfo copy = new ChargingScheduleInfo(Duration.ofHours(24), now, UnitOfMeasure.W,
				new BigDecimal("0.1"));
		copy.addPeriod(new ChargingSchedulePeriodInfo(Duration.ZERO, BigDecimal.ZERO));
		copy.addPeriod(new ChargingSchedulePeriodInfo(Duration.ofHours(1), BigDecimal.ZERO));
		copy.addPeriod(new ChargingSchedulePeriodInfo(Duration.ofHours(2), BigDecimal.TEN));

		assertThat("Not same", info.isSameAs(copy), equalTo(false));
		assertThat("Different", info.differsFrom(copy), equalTo(true));
	}

}

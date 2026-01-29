/* ==================================================================
 * BasicDatumStreamMetadataTests.java - 29/01/2026 7:16:28 pm
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

package net.solarnetwork.domain.datum.test;

import static net.solarnetwork.domain.datum.DatumSamplesType.Accumulating;
import static net.solarnetwork.domain.datum.DatumSamplesType.Instantaneous;
import static net.solarnetwork.domain.datum.DatumSamplesType.Status;
import static net.solarnetwork.test.CommonTestUtils.randomDecimal;
import static net.solarnetwork.test.CommonTestUtils.randomString;
import static org.assertj.core.api.BDDAssertions.then;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.Test;
import net.solarnetwork.domain.datum.BasicDatumStreamMetadata;
import net.solarnetwork.domain.datum.DatumProperties;
import net.solarnetwork.domain.datum.DatumPropertiesStatistics;
import net.solarnetwork.domain.datum.DatumPropertiesStatistics.AccumulatingStatistic;
import net.solarnetwork.domain.datum.DatumPropertiesStatistics.InstantaneousStatistic;
import net.solarnetwork.domain.datum.DatumSamplesType;

/**
 * Test cases for the {@link BasicDatumStreamMetadata} class.
 *
 * @author matt
 * @version 1.0
 */
public class BasicDatumStreamMetadataTests {

	@Test
	public void propertyNames() {
		// GIVEN
		final var iProps = new String[] { randomString(), randomString(), randomString() };
		final var aProps = new String[] { randomString(), randomString() };
		final var sProps = new String[] { randomString() };

		// WHEN
		var meta = new BasicDatumStreamMetadata(UUID.randomUUID(), randomString(), iProps, aProps,
				sProps);

		// THEN
		// @formatter:off
		then(meta.propertyNamesForType(DatumSamplesType.Instantaneous))
			.as("Instantaneous names provided in constructor are returned")
			.isSameAs(iProps)
			;
		then(meta.propertyNamesForType(DatumSamplesType.Accumulating))
			.as("Accumulating names provided in constructor are returned")
			.isSameAs(aProps)
			;
		then(meta.propertyNamesForType(DatumSamplesType.Status))
			.as("Status names provided in constructor are returned")
			.isSameAs(sProps)
			;
		then(meta.propertyNamesForType(DatumSamplesType.Tag))
			.as("Tag names always null")
			.isNull()
			;

		then(meta.getPropertyNames())
			.as("Property names contains all types in i, a, s order")
			.containsExactly(iProps[0], iProps[1], iProps[2], aProps[0], aProps[1], sProps[0])
			;
		// @formatter:on
	}

	@Test
	public void value() {
		// GIVEN
		final var iProps = new String[] { randomString(), randomString(), randomString() };
		final var aProps = new String[] { randomString(), randomString() };
		final var sProps = new String[] { randomString() };

		final var iData = new BigDecimal[] { randomDecimal(), randomDecimal(), randomDecimal() };
		final var aData = new BigDecimal[] { randomDecimal(), randomDecimal() };
		final var sData = new String[] { randomString() };

		final var data = new DatumProperties();
		data.setInstantaneous(iData);
		data.setAccumulating(aData);
		data.setStatus(sData);

		// WHEN
		var meta = new BasicDatumStreamMetadata(UUID.randomUUID(), randomString(), iProps, aProps,
				sProps);

		// THEN
		// @formatter:off
		for (int i = 0; i < iData.length; i++ ) {
			then(meta.value(data, Instantaneous, i))
				.as("Instantaneous value for valid index %d returned", i)
				.isSameAs(iData[i])
				;
		}

		then(meta.value(data, Instantaneous , iData.length))
			.as("Null returned for invalid Instantaneous index")
			.isNull()
			;

		for (int i = 0; i < aData.length; i++ ) {
			then(meta.value(data, Accumulating, i))
				.as("Accumulating value for valid index %d returned", i)
				.isSameAs(aData[i])
				;
		}

		then(meta.value(data, Accumulating , aData.length))
			.as("Null returned for invalid Accumulating index")
			.isNull()
			;

		for (int i = 0; i < sData.length; i++ ) {
			then(meta.value(data, Status, i))
				.as("Status value for valid index %d returned", i)
				.isSameAs(sData[i])
				;
		}

		then(meta.value(data, Status , sData.length))
			.as("Null returned for invalid Status index")
			.isNull()
			;

		// @formatter:on
	}

	@Test
	public void stat() {
		// GIVEN
		final var iProps = new String[] { randomString(), randomString(), randomString() };
		final var aProps = new String[] { randomString(), randomString() };
		final var sProps = new String[] { randomString() };

		// @formatter:off
		final var iStats = new BigDecimal[][] {
			new BigDecimal[] {randomDecimal(), randomDecimal(), randomDecimal() },
			new BigDecimal[] {randomDecimal(), randomDecimal(), randomDecimal() },
			new BigDecimal[] {randomDecimal(), randomDecimal(), randomDecimal() }
		};
		final var aStats = new BigDecimal[][] {
			new BigDecimal[] {randomDecimal(), randomDecimal(), randomDecimal() },
			new BigDecimal[] {randomDecimal(), randomDecimal(), randomDecimal() }
		};
		// @formatter:on

		final var stats = new DatumPropertiesStatistics();
		stats.setInstantaneous(iStats);
		stats.setAccumulating(aStats);

		// WHEN
		var meta = new BasicDatumStreamMetadata(UUID.randomUUID(), randomString(), iProps, aProps,
				sProps);

		// THEN
		// @formatter:off
		for (int i = 0; i < iStats.length; i++ ) {
			for (InstantaneousStatistic type : InstantaneousStatistic.values()) {
				then(meta.stat(stats, type, i))
					.as("Instantaneous value for valid type %s index %d returned", type, i)
					.isSameAs(iStats[i][type.ordinal()])
					;
			}
		}

		then(meta.stat(stats, InstantaneousStatistic.Count , iStats.length))
			.as("Null returned for invalid Instantaneous index")
			.isNull()
			;

		for (int i = 0; i < aStats.length; i++ ) {
			for (AccumulatingStatistic type : AccumulatingStatistic.values()) {
				then(meta.stat(stats, type, i))
					.as("Accumulating value for valid type %s index %d returned", type, i)
					.isSameAs(aStats[i][type.ordinal()])
					;
			}
		}

		then(meta.stat(stats, AccumulatingStatistic.Difference, aStats.length))
			.as("Null returned for invalid Accumulating index")
			.isNull()
			;

		// @formatter:on
	}

}

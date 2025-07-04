/* ==================================================================
 * DatumPropertiesTests.java - 6/11/2020 6:46:57 am
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

package net.solarnetwork.domain.datum.test;

import static net.solarnetwork.util.NumberUtils.decimalArray;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.Test;
import net.solarnetwork.domain.datum.BasicObjectDatumStreamMetadata;
import net.solarnetwork.domain.datum.DatumProperties;
import net.solarnetwork.domain.datum.DatumSamples;
import net.solarnetwork.domain.datum.GeneralDatum;
import net.solarnetwork.domain.datum.ObjectDatumKind;
import net.solarnetwork.util.Half;

/**
 * Test cases for the {@link DatumProperties} class.
 * 
 * @author matt
 * @version 1.0
 */
public class DatumPropertiesTests {

	@Test
	public void length_null() {
		DatumProperties p = DatumProperties.propertiesOf(null, null, null, null);

		assertThat("Null instantaneous length is 0", p.getInstantaneousLength(), equalTo(0));
		assertThat("Null accumulating length is 0", p.getAccumulatingLength(), equalTo(0));
		assertThat("Null status length is 0", p.getStatusLength(), equalTo(0));
		assertThat("Null tags length is 0", p.getTagsLength(), equalTo(0));
	}

	@Test
	public void length_empty() {
		DatumProperties p = DatumProperties.propertiesOf(new BigDecimal[0], new BigDecimal[0],
				new String[0], new String[0]);

		assertThat("Empty instantaneous length is 0", p.getInstantaneousLength(), equalTo(0));
		assertThat("Empty accumulating length is 0", p.getAccumulatingLength(), equalTo(0));
		assertThat("Empty status length is 0", p.getStatusLength(), equalTo(0));
		assertThat("Empty tags length is 0", p.getTagsLength(), equalTo(0));
	}

	@Test
	public void length() {
		DatumProperties p = DatumProperties.propertiesOf(new BigDecimal[] { new BigDecimal("1") },
				new BigDecimal[] { new BigDecimal("2.1"), new BigDecimal("2.2") },
				new String[] { "3.1", "3.2", "3.3" }, new String[] { "4.1", "4.2", "4.3", "4.4" });

		assertThat("Instantaneous length", p.getInstantaneousLength(), equalTo(1));
		assertThat("Accumulating length", p.getAccumulatingLength(), equalTo(2));
		assertThat("Status length", p.getStatusLength(), equalTo(3));
		assertThat("Tags length", p.getTagsLength(), equalTo(4));
	}

	@Test
	public void fromDatum() {
		// GIVEN
		BasicObjectDatumStreamMetadata meta = new BasicObjectDatumStreamMetadata(UUID.randomUUID(),
				"Pacific/Auckland", ObjectDatumKind.Node, 123L, "test.source", new String[] { "a", "b" },
				new String[] { "c", "d" }, new String[] { "e" });
		DatumSamples s = new DatumSamples();
		s.putInstantaneousSampleValue("a", 1);
		s.putInstantaneousSampleValue("b", 2);
		s.putAccumulatingSampleValue("c", 3);
		s.putAccumulatingSampleValue("d", 4);
		s.putStatusSampleValue("e", 5);
		GeneralDatum d = new GeneralDatum(123L, "test.source", Instant.now(), s);

		// WHEN
		DatumProperties p = DatumProperties.propertiesFrom(d, meta);

		// THEN
		assertThat("Properties created", p, is(notNullValue()));
		assertThat("Instantaneous values mapped", p.getInstantaneous(),
				is(arrayContaining(decimalArray("1", "2"))));
		assertThat("Accumulating values mapped", p.getAccumulating(),
				is(arrayContaining(decimalArray("3", "4"))));
		assertThat("Status values mapped", p.getStatus(), is(arrayContaining(new String[] { "5" })));
	}

	@Test
	public void fromDatum_withHalf() {
		// GIVEN
		BasicObjectDatumStreamMetadata meta = new BasicObjectDatumStreamMetadata(UUID.randomUUID(),
				"Pacific/Auckland", ObjectDatumKind.Node, 123L, "test.source", new String[] { "a", "b" },
				new String[] { "c", "d" }, new String[] { "e" });
		Half h = new Half("1.23");
		DatumSamples s = new DatumSamples();
		s.putInstantaneousSampleValue("a", 1);
		s.putInstantaneousSampleValue("b", h);
		GeneralDatum d = new GeneralDatum(123L, "test.source", Instant.now(), s);

		// WHEN
		DatumProperties p = DatumProperties.propertiesFrom(d, meta);

		// THEN
		assertThat("Properties created", p, is(notNullValue()));
		assertThat("Instantaneous values mapped", p.getInstantaneous(),
				is(arrayContaining(decimalArray("1", h.toString()))));
	}

	@Test(expected = IllegalArgumentException.class)
	public void fromDatum_unknownProperty() {
		// GIVEN
		BasicObjectDatumStreamMetadata meta = new BasicObjectDatumStreamMetadata(UUID.randomUUID(),
				"Pacific/Auckland", ObjectDatumKind.Node, 123L, "test.source", new String[] { "a", "b" },
				new String[] { "c", "d" }, new String[] { "e" });
		DatumSamples s = new DatumSamples();
		s.putInstantaneousSampleValue("foo", 1);
		GeneralDatum d = new GeneralDatum(123L, "test.source", Instant.now(), s);

		// WHEN
		DatumProperties.propertiesFrom(d, meta);
	}

	@Test(expected = IllegalArgumentException.class)
	public void fromDatum_unknownProperty_emptyInstantaneous() {
		// GIVEN
		BasicObjectDatumStreamMetadata meta = new BasicObjectDatumStreamMetadata(UUID.randomUUID(),
				"Pacific/Auckland", ObjectDatumKind.Node, 123L, "test.source", null, null, null);
		DatumSamples s = new DatumSamples();
		s.putInstantaneousSampleValue("foo", 1);
		GeneralDatum d = new GeneralDatum(123L, "test.source", Instant.now(), s);

		// WHEN
		DatumProperties.propertiesFrom(d, meta);
	}

	@Test(expected = IllegalArgumentException.class)
	public void fromDatum_unknownProperty_emptyAccumulating() {
		// GIVEN
		BasicObjectDatumStreamMetadata meta = new BasicObjectDatumStreamMetadata(UUID.randomUUID(),
				"Pacific/Auckland", ObjectDatumKind.Node, 123L, "test.source", null, null, null);
		DatumSamples s = new DatumSamples();
		s.putAccumulatingSampleValue("foo", 1);
		GeneralDatum d = new GeneralDatum(123L, "test.source", Instant.now(), s);

		// WHEN
		DatumProperties.propertiesFrom(d, meta);
	}

	@Test(expected = IllegalArgumentException.class)
	public void fromDatum_unknownProperty_emptyStatus() {
		// GIVEN
		BasicObjectDatumStreamMetadata meta = new BasicObjectDatumStreamMetadata(UUID.randomUUID(),
				"Pacific/Auckland", ObjectDatumKind.Node, 123L, "test.source", null, null, null);
		DatumSamples s = new DatumSamples();
		s.putStatusSampleValue("foo", "bar");
		GeneralDatum d = new GeneralDatum(123L, "test.source", Instant.now(), s);

		// WHEN
		DatumProperties.propertiesFrom(d, meta);
	}

	@Test
	public void fromDatum_trimTrailingNulls() {
		// GIVEN
		BasicObjectDatumStreamMetadata meta = new BasicObjectDatumStreamMetadata(UUID.randomUUID(),
				"Pacific/Auckland", ObjectDatumKind.Node, 123L, "test.source",
				new String[] { "a", "b", "c" }, new String[] { "c", "d" }, new String[] { "e" });
		DatumSamples s = new DatumSamples();
		s.putInstantaneousSampleValue("a", 1);
		s.putAccumulatingSampleValue("c", 3);
		s.putStatusSampleValue("e", 5);
		GeneralDatum d = new GeneralDatum(123L, "test.source", Instant.now(), s);

		// WHEN
		DatumProperties p = DatumProperties.propertiesFrom(d, meta);

		// THEN
		assertThat("Properties created", p, is(notNullValue()));
		assertThat("Instantaneous values mapped", p.getInstantaneous(),
				is(arrayContaining(decimalArray("1"))));
		assertThat("Accumulating values mapped", p.getAccumulating(),
				is(arrayContaining(decimalArray("3"))));
		assertThat("Status values mapped", p.getStatus(), is(arrayContaining(new String[] { "5" })));
	}

	@Test
	public void fromDatum_trimTrailingNulls_withHole() {
		// GIVEN
		BasicObjectDatumStreamMetadata meta = new BasicObjectDatumStreamMetadata(UUID.randomUUID(),
				"Pacific/Auckland", ObjectDatumKind.Node, 123L, "test.source",
				new String[] { "a", "b", "c", "d" }, new String[] { "e", "f" }, new String[] { "g" });
		DatumSamples s = new DatumSamples();
		s.putInstantaneousSampleValue("a", 1);
		// no b (the hole)
		s.putInstantaneousSampleValue("c", 3);
		// no trailing d

		GeneralDatum d = new GeneralDatum(123L, "test.source", Instant.now(), s);

		// WHEN
		DatumProperties p = DatumProperties.propertiesFrom(d, meta);

		// THEN
		assertThat("Properties created", p, is(notNullValue()));
		assertThat("Instantaneous values mapped", p.getInstantaneous(),
				is(arrayContaining(decimalArray("1", null, "3"))));
	}

}

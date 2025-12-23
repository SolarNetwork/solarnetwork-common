/* ==================================================================
 * MutableDatumSamplesWithMetadataTests.java - 23/12/2025 1:07:46 pm
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

package net.solarnetwork.domain.datum.test;

import static org.assertj.core.api.BDDAssertions.from;
import static org.assertj.core.api.BDDAssertions.then;
import java.security.SecureRandom;
import java.util.UUID;
import java.util.random.RandomGenerator;
import org.junit.Test;
import net.solarnetwork.domain.datum.DatumSamples;
import net.solarnetwork.domain.datum.DatumSamplesType;
import net.solarnetwork.domain.datum.GeneralDatumMetadata;
import net.solarnetwork.domain.datum.MutableDatumSamplesWithMetadata;

/**
 * Test cases for the {@link MutableDatumSamplesWithMetadata} class.
 *
 * @author matt
 * @version 1.0
 */
public class MutableDatumSamplesWithMetadataTests {

	private static final RandomGenerator RNG = new SecureRandom();

	@Test
	public void read() {
		// GIVEN
		final String path = "/pm/deviceInfo/serialNumber";
		final String data = UUID.randomUUID().toString();
		final GeneralDatumMetadata meta = new GeneralDatumMetadata();
		meta.populate(path, data);

		final DatumSamples samples = new DatumSamples();
		final String prop = UUID.randomUUID().toString();
		final Integer val = RNG.nextInt();
		samples.putSampleValue(DatumSamplesType.Instantaneous, prop, val);

		final String tag = UUID.randomUUID().toString();
		samples.addTag(tag);

		// WHEN
		final MutableDatumSamplesWithMetadata obj = new MutableDatumSamplesWithMetadata(samples, meta);

		// THEN
		// @formatter:off
		then(obj)
			.as("Metadata path read")
			.returns(data, from(o -> o.getSampleString(DatumSamplesType.Metadata, path)))
			.as("Instantaneous property read from samples")
			.returns(val, from(o -> o.getSampleInteger(DatumSamplesType.Instantaneous, prop)))
			.as("Tag read from samples")
			.returns(true, from(o -> o.hasTag(tag)))
			;
		// @formatter:on
	}

	@Test
	public void populate() {
		// GIVEN
		final GeneralDatumMetadata meta = new GeneralDatumMetadata();
		final DatumSamples samples = new DatumSamples();
		final MutableDatumSamplesWithMetadata obj = new MutableDatumSamplesWithMetadata(samples, meta);

		// WHEN
		final String path = "/pm/deviceInfo/serialNumber";
		final String data = UUID.randomUUID().toString();
		obj.putSampleValue(DatumSamplesType.Metadata, path, data);

		final String prop = UUID.randomUUID().toString();
		final Integer val = RNG.nextInt();
		obj.putSampleValue(DatumSamplesType.Instantaneous, prop, val);

		final String tag = UUID.randomUUID().toString();
		obj.addTag(tag);

		// THEN
		// @formatter:off
		then(meta)
			.as("Metadata populated")
			.returns(data, from(m -> m.metadataAtPath(path)))
			;

		then(samples)
			.as("Instantaneous property populated on samples")
			.returns(val, from(s -> s.getInstantaneousSampleInteger(prop)))
			.as("Tag populated on samples")
			.returns(true, from(s -> s.hasTag(tag)))
			;
		// @formatter:on
	}

}

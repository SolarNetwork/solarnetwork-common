/* ==================================================================
 * NumberDatumSamplePropertyConfigTests.java - 27/09/2019 3:12:38 pm
 * 
 * Copyright 2019 SolarNetwork.net Dev Team
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

import static net.solarnetwork.domain.datum.DatumSamplesType.Instantaneous;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import java.math.BigDecimal;
import org.junit.Test;
import net.solarnetwork.domain.datum.NumberDatumSamplePropertyConfig;

/**
 * Test cases for the {@link NumberDatumSamplePropertyConfig} class.
 * 
 * @author matt
 * @version 1.0
 */
public class NumberDatumSamplePropertyConfigTests {

	@Test
	public void sampleKey() {
		NumberDatumSamplePropertyConfig<String> c = new NumberDatumSamplePropertyConfig<String>("foo",
				Instantaneous, "bar");
		assertThat(c.getPropertyKey(), equalTo("foo"));
		assertThat(c.getPropertyType(), equalTo(Instantaneous));
		assertThat(c.getConfig(), equalTo("bar"));
	}

	@Test
	public void slopeIntercept() {
		NumberDatumSamplePropertyConfig<String> c = new NumberDatumSamplePropertyConfig<String>("foo",
				Instantaneous, "bar");
		c.setSlope(new BigDecimal("30"));
		c.setIntercept(new BigDecimal("15"));
		assertThat("y = mx + b applied", c.applyTransformations(10), equalTo(new BigDecimal("315")));
	}

	@Test
	public void slopeIntercept_noIntercept() {
		NumberDatumSamplePropertyConfig<String> c = new NumberDatumSamplePropertyConfig<String>("foo",
				Instantaneous, "bar");
		c.setSlope(new BigDecimal("30"));
		assertThat("y = mx + b applied", c.applyTransformations(10), equalTo(new BigDecimal("300")));
	}

	@Test
	public void slopeIntercept_noSlope() {
		NumberDatumSamplePropertyConfig<String> c = new NumberDatumSamplePropertyConfig<String>("foo",
				Instantaneous, "bar");
		c.setIntercept(new BigDecimal("15"));
		assertThat("y = mx + b applied", c.applyTransformations(10), equalTo(new BigDecimal("25")));
	}

	@Test
	public void unitSlopeIntercept() {
		NumberDatumSamplePropertyConfig<String> c = new NumberDatumSamplePropertyConfig<String>("foo",
				Instantaneous, "bar");
		c.setUnitSlope(new BigDecimal("30"));
		c.setUnitIntercept(new BigDecimal("15"));
		assertThat("y = M(x + B) applied", c.applyTransformations(10), equalTo(new BigDecimal("750")));
	}

	@Test
	public void unitSlopeIntercept_noIntercept() {
		NumberDatumSamplePropertyConfig<String> c = new NumberDatumSamplePropertyConfig<String>("foo",
				Instantaneous, "bar");
		c.setUnitSlope(new BigDecimal("30"));
		assertThat("y = M(x + B) applied", c.applyTransformations(10), equalTo(new BigDecimal("300")));
	}

	@Test
	public void unitSlopeIntercept_noSlope() {
		NumberDatumSamplePropertyConfig<String> c = new NumberDatumSamplePropertyConfig<String>("foo",
				Instantaneous, "bar");
		c.setUnitIntercept(new BigDecimal("15"));
		assertThat("y = M(x + B) applied", c.applyTransformations(10), equalTo(new BigDecimal("25")));
	}

}

/* ==================================================================
 * DatumSamplePropertyConfigTests.java - 14/03/2018 3:53:05 PM
 * 
 * Copyright 2018 SolarNetwork.net Dev Team
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
import org.junit.Test;
import net.solarnetwork.domain.datum.DatumSamplePropertyConfig;

/**
 * Test cases for the {@link GeneralDatumSamplePropertyConfig} class.
 * 
 * @author matt
 * @version 1.0
 */
public class DatumSamplePropertyConfigTests {

	@Test
	public void sampleKey() {
		DatumSamplePropertyConfig<String> c = new DatumSamplePropertyConfig<String>("foo", Instantaneous,
				"bar");
		assertThat(c.getPropertyKey(), equalTo("foo"));
		assertThat(c.getPropertyType(), equalTo(Instantaneous));
		assertThat(c.getConfig(), equalTo("bar"));
	}

}

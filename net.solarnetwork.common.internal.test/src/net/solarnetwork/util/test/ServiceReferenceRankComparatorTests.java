/* ==================================================================
 * ServiceReferenceRankComparatorTests.java - 7/06/2018 10:23:00 AM
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

package net.solarnetwork.util.test;

import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import java.util.Arrays;
import org.junit.Test;
import net.solarnetwork.util.ServiceReferenceRankComparator;
import net.solarnetwork.util.test.TestServiceReference.SerializableServiceRef;

/**
 * Test cases for the {@link ServiceReferenceRankComparator} class.
 * 
 * @author matt
 * @version 1.0
 */
public class ServiceReferenceRankComparatorTests {

	private static final int ASCENDING_ORDER = -1;
	private static final int SAME_ORDER = 0;
	private static final int DESCENDING_ORDER = 1;

	private final ServiceReferenceRankComparator comparator = new ServiceReferenceRankComparator();

	@Test
	public void idSameRankSame() {
		SerializableServiceRef r1 = new SerializableServiceRef(0, 0);
		SerializableServiceRef r2 = new SerializableServiceRef(0, 0);
		assertThat(comparator.compare(r1, r2), equalTo(SAME_ORDER));
	}

	@Test
	public void idSameRankDescending() {
		SerializableServiceRef r1 = new SerializableServiceRef(0, 0);
		SerializableServiceRef r2 = new SerializableServiceRef(0, 1);
		assertThat(comparator.compare(r1, r2), equalTo(DESCENDING_ORDER));
	}

	@Test
	public void idSameRankAscending() {
		SerializableServiceRef r1 = new SerializableServiceRef(0, 1);
		SerializableServiceRef r2 = new SerializableServiceRef(0, 0);
		assertThat(comparator.compare(r1, r2), equalTo(ASCENDING_ORDER));
	}

	@Test
	public void idDescendingRankSame() {
		SerializableServiceRef r1 = new SerializableServiceRef(1, 0);
		SerializableServiceRef r2 = new SerializableServiceRef(0, 0);
		assertThat(comparator.compare(r1, r2), equalTo(DESCENDING_ORDER));
	}

	@Test
	public void idDescendingRankDescending() {
		SerializableServiceRef r1 = new SerializableServiceRef(1, 0);
		SerializableServiceRef r2 = new SerializableServiceRef(0, 1);
		assertThat(comparator.compare(r1, r2), equalTo(DESCENDING_ORDER));
	}

	@Test
	public void idDescendingRankAscending() {
		SerializableServiceRef r1 = new SerializableServiceRef(1, 1);
		SerializableServiceRef r2 = new SerializableServiceRef(0, 0);
		assertThat(comparator.compare(r1, r2), equalTo(ASCENDING_ORDER));
	}

	@Test
	public void idAscendingRankSame() {
		SerializableServiceRef r1 = new SerializableServiceRef(0, 0);
		SerializableServiceRef r2 = new SerializableServiceRef(1, 0);
		assertThat(comparator.compare(r1, r2), equalTo(ASCENDING_ORDER));
	}

	@Test
	public void idAsscendingRankDescending() {
		SerializableServiceRef r1 = new SerializableServiceRef(0, 0);
		SerializableServiceRef r2 = new SerializableServiceRef(1, 1);
		assertThat(comparator.compare(r1, r2), equalTo(DESCENDING_ORDER));
	}

	@Test
	public void idAsscendingRankAscending() {
		SerializableServiceRef r1 = new SerializableServiceRef(0, 1);
		SerializableServiceRef r2 = new SerializableServiceRef(1, 0);
		assertThat(comparator.compare(r1, r2), equalTo(ASCENDING_ORDER));
	}

	@Test
	public void arraySort() {
		SerializableServiceRef r1 = new SerializableServiceRef(2, 0);
		SerializableServiceRef r2 = new SerializableServiceRef(1, 10);
		SerializableServiceRef r3 = new SerializableServiceRef(3, 5);
		SerializableServiceRef r4 = new SerializableServiceRef(4, 0);
		SerializableServiceRef[] array = new SerializableServiceRef[] { r1, r2, r3, r4 };
		Arrays.sort(array, comparator);
		assertThat("Sorted order", array, arrayContaining(r2, r3, r1, r4));
	}

}

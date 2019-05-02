/* ==================================================================
 * ArrayUtilsTests.java - 16/03/2018 7:04:22 AM
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
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;
import net.solarnetwork.util.ArrayUtils;

/**
 * Test cases for the {@link ArrayUtils} class.
 * 
 * @author matt
 * @version 1.1
 */
public class ArrayUtilsTests {

	public static class FooBean {

		private final int index;

		public FooBean() {
			this(-1);
		}

		public FooBean(int index) {
			super();
			this.index = index;
		}
	}

	private static class FooBeanFactory implements ObjectFactory<FooBean> {

		private int count;

		private FooBeanFactory(int start) {
			super();
			this.count = start;
		}

		@Override
		public FooBean getObject() throws BeansException {
			return new FooBean(count++);
		}

	}

	@Test
	public void adjustArrayLengthNullInput() {
		FooBean[] result = ArrayUtils.arrayWithLength(null, 0, FooBean.class, null);
		assertThat("Empty array created", result, arrayWithSize(0));
	}

	@Test
	public void adjustArrayLengthNullInputNegativeCount() {
		FooBean[] result = ArrayUtils.arrayWithLength(null, -5, FooBean.class, null);
		assertThat("Empty array created", result, arrayWithSize(0));
	}

	@Test
	public void adjustArrayLengthNoAdjustment() {
		FooBean[] array = new FooBean[5];
		FooBean[] result = ArrayUtils.arrayWithLength(array, 5, FooBean.class, null);
		assertThat("Source array returned", result, sameInstance(array));
	}

	@Test
	public void adjustArrayLengthShorter() {
		FooBean[] array = new FooBean[] { new FooBean(0), new FooBean(1) };
		FooBean[] result = ArrayUtils.arrayWithLength(array, 1, FooBean.class, null);
		assertThat("New array returned", result, not(sameInstance(array)));
		assertThat("New array length", result, arrayWithSize(1));
		assertThat("Item 0 copied", result[0], sameInstance(array[0]));
	}

	@Test
	public void adjustArrayLengthToZero() {
		FooBean[] array = new FooBean[] { new FooBean(0), new FooBean(1) };
		FooBean[] result = ArrayUtils.arrayWithLength(array, 0, FooBean.class, null);
		assertThat("New array returned", result, not(sameInstance(array)));
		assertThat("New array length", result, arrayWithSize(0));
	}

	@Test
	public void adjustArrayLengthNullInputLonger() {
		FooBean[] result = ArrayUtils.arrayWithLength(null, 3, FooBean.class, null);
		assertThat("New array returned", result, notNullValue());
		assertThat("New array length", result, arrayWithSize(3));
		for ( int i = 0; i < 3; i += 1 ) {
			assertThat("Item.index " + i, result[i].index, equalTo(-1));
		}
	}

	@Test
	public void adjustArrayLengthLonger() {
		FooBean[] array = new FooBean[] { new FooBean(0) };
		FooBean[] result = ArrayUtils.arrayWithLength(array, 3, FooBean.class, null);
		assertThat("New array returned", result, not(sameInstance(array)));
		assertThat("New array length", result, arrayWithSize(3));
		assertThat("Item 0 copied", result[0], sameInstance(array[0]));
		for ( int i = 1; i < 3; i += 1 ) {
			assertThat("Item.index " + i, result[i].index, equalTo(-1));
		}
	}

	@Test
	public void adjustArrayLengthLongerWithFactory() {
		FooBean[] array = new FooBean[] { new FooBean(0) };
		FooBean[] result = ArrayUtils.arrayWithLength(array, 3, FooBean.class, new FooBeanFactory(1));
		assertThat("New array returned", result, not(sameInstance(array)));
		assertThat("New array length", result, arrayWithSize(3));
		assertThat("Item 0 copied", result[0], sameInstance(array[0]));
		for ( int i = 1; i < 3; i += 1 ) {
			assertThat("Item.index " + i, result[i].index, equalTo(i));
		}
	}

	@Test
	public void filterByEnabledDisabledWithNullArgs() {
		String[] src = new String[] { "a", "b", "c" };
		String[] res = ArrayUtils.filterByEnabledDisabled(src, null, null);
		assertThat("Input returned", res, sameInstance(src));
		assertThat("Result", res, arrayContaining("a", "b", "c"));
	}

	@Test
	public void filterByEnabledDisabledWithEnabled() {
		String[] src = new String[] { "a", "b", "c" };
		String[] res = ArrayUtils.filterByEnabledDisabled(src, new String[] { "a", "c" }, null);
		assertThat("New array returned", res, not(sameInstance(src)));
		assertThat("Result", res, arrayContaining("a", "c"));
	}

	@Test
	public void filterByEnabledDisabledWithDisabled() {
		String[] src = new String[] { "a", "b", "c" };
		String[] res = ArrayUtils.filterByEnabledDisabled(src, null, new String[] { "a", "c" });
		assertThat("New array returned", res, not(sameInstance(src)));
		assertThat("Result", res, arrayContaining("b"));
	}

	@Test
	public void filterByEnabledDisabledWithEnabledAndDisabled() {
		String[] src = new String[] { "a", "b", "c" };
		String[] res = ArrayUtils.filterByEnabledDisabled(src, new String[] { "a", "b" },
				new String[] { "a" });
		assertThat("New array returned", res, not(sameInstance(src)));
		assertThat("Result", res, arrayContaining("b"));
	}

	@Test
	public void filterByEnabledDisabledWithEnabledAndDisabledNoneLeft() {
		String[] src = new String[] { "a", "b", "c" };
		String[] res = ArrayUtils.filterByEnabledDisabled(src, new String[] { "a", "b" },
				new String[] { "a", "b" });
		assertThat("New array returned", res, not(sameInstance(src)));
		assertThat("Result", res, arrayWithSize(0));
	}

	@Test
	public void filterByEnabledDisabledWithEnabledAndDisabledPatterns() {
		String[] src = new String[] { "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384",
				"TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384", "TLS_RSA_WITH_AES_128_CBC_SHA256",
				"TLS_RSA_WITH_AES_128_GCM_SHA256" };
		String[] res = ArrayUtils.filterByEnabledDisabled(src, new String[] { "^TLS_ECDHE" },
				new String[] { "_CBC_" });
		assertThat("New array returned", res, not(sameInstance(src)));
		assertThat("Result", res, arrayContaining("TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384"));
	}

	@Test
	public void filterByEnabledDisabledWithDisabledPatterns() {
		String[] src = new String[] { "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384",
				"TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384", "TLS_RSA_WITH_AES_128_CBC_SHA256",
				"TLS_RSA_WITH_AES_128_GCM_SHA256" };
		String[] res = ArrayUtils.filterByEnabledDisabled(src, null, new String[] { "_CBC_" });
		assertThat("New array returned", res, not(sameInstance(src)));
		assertThat("Result", res, arrayContaining("TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
				"TLS_RSA_WITH_AES_128_GCM_SHA256"));
	}
}

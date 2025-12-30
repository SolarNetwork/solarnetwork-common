/* ==================================================================
 * SimpleCsvHttpMessageCoverterTest.java - Apr 21, 2014 9:05:31 AM
 *
 * Copyright 2007-2014 SolarNetwork.net Dev Team
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

package net.solarnetwork.web.jakarta.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.mock.web.MockHttpServletResponse;
import net.solarnetwork.test.AbstractTest;
import net.solarnetwork.web.jakarta.support.SimpleCsvHttpMessageConverter;

/**
 * Test cases for the {@link SimpleCsvHttpMessageConverter} class.
 *
 * @author matt
 * @version 1.0
 */
public class SimpleCsvHttpMessageCoverterTest extends AbstractTest {

	private static final MediaType CSV_MEDIA_TYPE = MediaType.parseMediaType("text/csv; charset=UTF-8");

	private MockHttpServletResponse response;
	private HttpOutputMessage output;

	@Before
	public void setupTest() {
		response = new MockHttpServletResponse();
		output = new ServletServerHttpResponse(response);
	}

	@Test
	public void testAPIContract() {
		HttpMessageConverter<?> hmc = new SimpleCsvHttpMessageConverter();
		List<MediaType> supportedTypes = hmc.getSupportedMediaTypes();
		Assert.assertEquals("Supports text/csv", Arrays.asList(CSV_MEDIA_TYPE), supportedTypes);
		Assert.assertTrue("Can write CSV", hmc.canWrite(Object.class, CSV_MEDIA_TYPE));
	}

	@Test
	public void testEmptyModel() throws Exception {
		Map<String, Object> model = new LinkedHashMap<String, Object>();

		HttpMessageConverter<Object> hmc = new SimpleCsvHttpMessageConverter();
		hmc.write(model, CSV_MEDIA_TYPE, output);

		String result = response.getContentAsString();
		assertEquals("", result);
	}

	@Test
	public void testSingleRowSingleColumn() throws Exception {
		Map<String, Object> model = new LinkedHashMap<String, Object>();
		model.put("foo", "bar");

		HttpMessageConverter<Object> hmc = new SimpleCsvHttpMessageConverter();
		hmc.write(model, CSV_MEDIA_TYPE, output);

		String result = response.getContentAsString();
		assertEquals("foo\r\nbar\r\n", result);
	}

	@Test
	public void testSingleRowMap() throws Exception {
		Map<String, Object> row = new LinkedHashMap<String, Object>();
		row.put("one", "1");
		row.put("two", "2");
		row.put("three", "3");

		HttpMessageConverter<Object> hmc = new SimpleCsvHttpMessageConverter();
		hmc.write(row, CSV_MEDIA_TYPE, output);

		String result = response.getContentAsString();
		assertEquals("one,two,three\r\n1,2,3\r\n", result);
	}

	@Test
	public void testMultiRowMap() throws Exception {
		List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
		Map<String, Object> row = new LinkedHashMap<String, Object>();
		row.put("one", "1");
		row.put("two", "2");
		row.put("three", "3");
		rows.add(row);
		row = new LinkedHashMap<String, Object>();
		row.put("one", "4");
		row.put("two", "5");
		row.put("three", "6");
		rows.add(row);

		HttpMessageConverter<Object> hmc = new SimpleCsvHttpMessageConverter();
		hmc.write(rows, CSV_MEDIA_TYPE, output);

		String result = response.getContentAsString();
		assertEquals("one,two,three\r\n1,2,3\r\n4,5,6\r\n", result);
	}

	public static final class TestBean {

		private final String one;
		private final Integer two;
		private final String three;

		public TestBean(String a, Integer b, String c) {
			super();
			one = a;
			two = b;
			three = c;
		}

		public String getOne() {
			return one;
		}

		public Integer getTwo() {
			return two;
		}

		public String getThree() {
			return three;
		}
	}

	@Test
	public void testSingleRowBeanNoOrder() throws Exception {
		TestBean row = new TestBean("1", 2, "3");

		HttpMessageConverter<Object> hmc = new SimpleCsvHttpMessageConverter();
		hmc.write(row, CSV_MEDIA_TYPE, output);

		String result = response.getContentAsString();
		assertFalse("Result should not be empty", result.length() == 0);
		assertFalse("Order should not be preserved", "one,two,three\n1,2,3\n".equals(result));
	}

	@Test
	public void testFieldWithDelimiter() throws Exception {
		Map<String, Object> row = new LinkedHashMap<String, Object>();
		row.put("one", "1,1");
		row.put("two", "2");
		row.put("three", "3,3");

		HttpMessageConverter<Object> hmc = new SimpleCsvHttpMessageConverter();
		hmc.write(row, CSV_MEDIA_TYPE, output);

		String result = response.getContentAsString();
		assertEquals("one,two,three\r\n\"1,1\",2,\"3,3\"\r\n", result);
	}

}

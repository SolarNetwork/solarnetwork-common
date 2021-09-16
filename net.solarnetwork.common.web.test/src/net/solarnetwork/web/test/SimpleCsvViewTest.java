/* ==================================================================
 * SimpleCsvViewTest.java - Feb 11, 2012 7:08:42 PM
 * 
 * Copyright 2007-2012 SolarNetwork.net Dev Team
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

package net.solarnetwork.web.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.solarnetwork.test.AbstractTest;
import net.solarnetwork.web.support.SimpleCsvView;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Test case for the {@link SimpleCsvView} class.
 * 
 * @author matt
 * @version 1.0
 */
public class SimpleCsvViewTest extends AbstractTest {

	private MockHttpServletRequest request;
	private MockHttpServletResponse response;
	
	@Before
	public void setupTest() {
		request = new MockHttpServletRequest("GET", "/csv");
		response = new MockHttpServletResponse();
	}
	
	@Test
	public void testEmptyModel() throws Exception {
		Map<String, Object> model = new LinkedHashMap<String, Object>();

		SimpleCsvView view = new SimpleCsvView();
		view.render(model, request, response);

		String result = response.getContentAsString();
		assertEquals("", result);
	}
	
	@Test
	public void testMissingDataFallbackToModel() throws Exception {
		Map<String, Object> model = new LinkedHashMap<String, Object>();
		model.put("foo", "bar");

		SimpleCsvView view = new SimpleCsvView();
		view.setDataModelKey("data");
		view.render(model, request, response);

		String result = response.getContentAsString();
		assertEquals("foo\nbar\n", result);
	}
	
	@Test
	public void testSingleRowMap() throws Exception {
		Map<String, Object> row = new LinkedHashMap<String, Object>();
		row.put("one", "1");
		row.put("two", "2");
		row.put("three", "3");
		
		Map<String, Object> model = new LinkedHashMap<String, Object>();
		model.put("data", row);

		SimpleCsvView view = new SimpleCsvView();
		view.setDataModelKey("data");
		view.render(model, request, response);

		String result = response.getContentAsString();
		assertEquals("one,two,three\n1,2,3\n", result);
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
		
		Map<String, Object> model = new LinkedHashMap<String, Object>();
		model.put("data", rows);

		SimpleCsvView view = new SimpleCsvView();
		view.setDataModelKey("data");
		view.render(model, request, response);

		String result = response.getContentAsString();
		assertEquals("one,two,three\n1,2,3\n4,5,6\n", result);
	}
	
	public static final class TestBean {
		private String one;
		private Integer two;
		private String three;
		
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
		
		Map<String, Object> model = new LinkedHashMap<String, Object>();
		model.put("data", row);

		SimpleCsvView view = new SimpleCsvView();
		view.setDataModelKey("data");
		view.render(model, request, response);

		String result = response.getContentAsString();
		assertFalse("Result should not be empty", result.length() == 0);
		assertFalse("Order should not be preserved", "one,two,three\n1,2,3\n".equals(result));
	}
	
	@Test
	public void testSingleRowBeanWithOrder() throws Exception {
		TestBean row = new TestBean("1", 2, "3");
		
		Collection<String> fieldOrder = new ArrayList<String>(3);
		fieldOrder.add("one");
		fieldOrder.add("two");
		fieldOrder.add("three");
		
		Map<String, Object> model = new LinkedHashMap<String, Object>();
		model.put("data", row);
		model.put("fieldOrder", fieldOrder);

		SimpleCsvView view = new SimpleCsvView();
		view.setDataModelKey("data");
		view.setFieldOrderKey("fieldOrder");
		view.render(model, request, response);

		String result = response.getContentAsString();
		assertEquals("one,two,three\n1,2,3\n", result);
	}
	
	@Test
	public void testMultiRowBeanWithOrder() throws Exception {
		Collection<TestBean> rows = new ArrayList<SimpleCsvViewTest.TestBean>(2);
		rows.add(new TestBean("1", 2, "3"));
		rows.add(new TestBean("4", 5, "6"));
		
		Collection<String> fieldOrder = new ArrayList<String>(3);
		fieldOrder.add("one");
		fieldOrder.add("two");
		fieldOrder.add("three");
		
		Map<String, Object> model = new LinkedHashMap<String, Object>();
		model.put("data", rows);
		model.put("fieldOrder", fieldOrder);

		SimpleCsvView view = new SimpleCsvView();
		view.setDataModelKey("data");
		view.setFieldOrderKey("fieldOrder");
		view.render(model, request, response);

		String result = response.getContentAsString();
		assertEquals("one,two,three\n1,2,3\n4,5,6\n", result);
	}
	
	@Test
	public void testFieldWithDelimiter() throws Exception {
		Map<String, Object> row = new LinkedHashMap<String, Object>();
		row.put("one", "1,1");
		row.put("two", "2");
		row.put("three", "3,3");
		
		Map<String, Object> model = new LinkedHashMap<String, Object>();
		model.put("data", row);

		SimpleCsvView view = new SimpleCsvView();
		view.setDataModelKey("data");
		view.render(model, request, response);

		String result = response.getContentAsString();
		assertEquals("one,two,three\n\"1,1\",2,\"3,3\"\n", result);
	}
	
}


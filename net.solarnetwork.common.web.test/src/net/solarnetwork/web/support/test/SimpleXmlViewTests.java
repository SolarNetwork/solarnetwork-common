/* ==================================================================
 * SimpleXmlViewTests.java - 23/02/2020 2:16:28 pm
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

package net.solarnetwork.web.support.test;

import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import java.util.Collections;
import java.util.Map;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import net.solarnetwork.web.support.SimpleXmlView;

/**
 * Test cases for the {@link SimpleXmlView} class.
 * 
 * @author matt
 * @version 1.0
 */
public class SimpleXmlViewTests {

	private static final String XML_PREAMBLE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

	@Test
	public void renderArray() throws Exception {
		MockHttpServletRequest req = new MockHttpServletRequest("GET", "/foo.xml");
		MockHttpServletResponse res = new MockHttpServletResponse();
		SimpleXmlView view = new SimpleXmlView();
		view.render(singletonMap("array", new String[] { "one", "two", "three" }), req, res);

		assertThat("Output XML", res.getContentAsString(), equalTo(XML_PREAMBLE
				+ "<array><value type=\"String\" value=\"one\"/><value type=\"String\" value=\"two\"/><value type=\"String\" value=\"three\"/></array>"));
	}

	public static final class TestObj {

		private Map<String, Object> data;

		public Map<String, Object> getData() {
			return data;
		}

	}

	@Test
	public void renderDatumSamplesWithTags() throws Exception {
		MockHttpServletRequest req = new MockHttpServletRequest("GET", "/foo.xml");
		MockHttpServletResponse res = new MockHttpServletResponse();
		SimpleXmlView view = new SimpleXmlView();
		TestObj samples = new TestObj();
		samples.data = Collections.singletonMap("tags", new String[] { "a", "b" });
		view.render(singletonMap("obj", samples), req, res);

		assertThat("Output XML", res.getContentAsString(), equalTo(XML_PREAMBLE
				+ "<SimpleXmlViewTests.TestObj><data><entry key=\"tags\"><value type=\"String\" value=\"a\"/><value type=\"String\" value=\"b\"/></entry></data></SimpleXmlViewTests.TestObj>"));
	}

}

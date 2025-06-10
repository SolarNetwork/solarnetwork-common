/* ==================================================================
 * SimpleXmlHttpMessageConverterTests.java - 24/02/2020 7:28:15 am
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

package net.solarnetwork.web.jakarta.support.test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import java.util.Collections;
import java.util.Map;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.mock.web.MockHttpServletResponse;
import net.solarnetwork.web.jakarta.support.SimpleXmlHttpMessageConverter;

/**
 * Test cases for the {@Link SimpleXmlHttpMessageConverter} class.
 *
 * @author matt
 * @version 1.1
 */
public class SimpleXmlHttpMessageConverterTests {

	private static final String XML_PREAMBLE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

	@Test
	public void renderArray() throws Exception {
		MockHttpServletResponse res = new MockHttpServletResponse();
		SimpleXmlHttpMessageConverter converter = new SimpleXmlHttpMessageConverter();
		converter.write(new String[] { "one", "two", "three" }, MediaType.APPLICATION_XML,
				new ServletServerHttpResponse(res));

		assertThat("Output XML", res.getContentAsString(), equalTo(XML_PREAMBLE
				+ "<array><value type=\"String\" value=\"one\"></value><value type=\"String\" value=\"two\"></value><value type=\"String\" value=\"three\"></value></array>"));
	}

	public static final class TestObj {

		private Map<String, Object> data;

		public Map<String, Object> getData() {
			return data;
		}

	}

	@Test
	public void renderDatumSamplesWithTags() throws Exception {
		MockHttpServletResponse res = new MockHttpServletResponse();
		SimpleXmlHttpMessageConverter converter = new SimpleXmlHttpMessageConverter();
		TestObj samples = new TestObj();
		samples.data = Collections.singletonMap("tags", new String[] { "a", "b" });
		converter.write(samples, MediaType.APPLICATION_XML, new ServletServerHttpResponse(res));

		assertThat("Output XML", res.getContentAsString(), equalTo(XML_PREAMBLE
				+ "<SimpleXmlHttpMessageConverterTests.TestObj><data><entry key=\"tags\"><value type=\"String\" value=\"a\"></value><value type=\"String\" value=\"b\"></value></entry></data></SimpleXmlHttpMessageConverterTests.TestObj>"));
	}

	@Test
	public void renderNestedMap() throws Exception {
		MockHttpServletResponse res = new MockHttpServletResponse();
		SimpleXmlHttpMessageConverter converter = new SimpleXmlHttpMessageConverter();

		TestObj obj = new TestObj();

		Map<String, Object> data = Collections.singletonMap("resultParameters",
				Collections.singletonMap("result", "foo"));
		obj.data = data;

		converter.write(obj, MediaType.APPLICATION_XML, new ServletServerHttpResponse(res));

		assertThat("Output XML", res.getContentAsString(), equalTo(XML_PREAMBLE
				+ "<SimpleXmlHttpMessageConverterTests.TestObj><data><entry key=\"resultParameters\"><map><entry key=\"result\"><value type=\"String\" value=\"foo\"></value></entry></map></entry></data></SimpleXmlHttpMessageConverterTests.TestObj>"));
	}

}

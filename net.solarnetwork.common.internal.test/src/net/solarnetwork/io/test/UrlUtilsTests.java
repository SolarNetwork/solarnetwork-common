/* ==================================================================
 * UrlUtilsTests.java - 11/07/2023 8:07:17 am
 *
 * Copyright 2023 SolarNetwork.net Dev Team
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

package net.solarnetwork.io.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
import org.junit.Test;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StreamUtils;
import net.solarnetwork.io.UrlUtils;
import net.solarnetwork.test.internal.AbstractHttpClientTests;
import net.solarnetwork.test.internal.TestHttpHandler;

/**
 * Test cases for the {@link UrlUtils} class.
 *
 * @author matt
 * @version 1.0
 */
public class UrlUtilsTests extends AbstractHttpClientTests {

	@Test
	public void gzipResponseStream() throws IOException {
		// GIVEN
		final String json = FileCopyUtils.copyToString(new InputStreamReader(
				getClass().getResourceAsStream("test-01.json"), StandardCharsets.UTF_8));
		TestHttpHandler handler = new TestHttpHandler() {

			@Override
			protected boolean handleInternal(Request request, Response response, Callback callback)
					throws Exception {
				response.getHeaders().put("Content-Encoding", "gzip");
				respondWithJson(request, response, json);
				return true;
			}

		};
		addHandler(handler);

		// WHEN
		URLConnection conn = UrlUtils.getURL(log, getHttpServerBaseUrl(), "application/json", null,
				getHttpServerPort(), null);
		try (InputStream in = UrlUtils.getInputStreamFromURLConnection(log, conn)) {
			assertThat("InputStream is GZIP", in, is(instanceOf(GZIPInputStream.class)));
			String result = StreamUtils.copyToString(in, StandardCharsets.UTF_8);
			assertThat("JSON gunzipped", result, is(equalTo(json)));
		}
	}

	@Test
	public void deflateResponseStream() throws IOException {
		// GIVEN
		final String json = FileCopyUtils.copyToString(new InputStreamReader(
				getClass().getResourceAsStream("test-01.json"), StandardCharsets.UTF_8));
		TestHttpHandler handler = new TestHttpHandler() {

			@Override
			protected boolean handleInternal(Request request, Response response, Callback callback)
					throws Exception {
				response.getHeaders().put("Content-Encoding", "deflate");
				respondWithJson(request, response, json);
				return true;
			}

		};
		addHandler(handler);

		// WHEN
		URLConnection conn = UrlUtils.getURL(log, getHttpServerBaseUrl(), "application/json", null,
				getHttpServerPort(), null);
		try (InputStream in = UrlUtils.getInputStreamFromURLConnection(log, conn)) {
			assertThat("InputStream is Inflater", in, is(instanceOf(InflaterInputStream.class)));
			String result = StreamUtils.copyToString(in, StandardCharsets.UTF_8);
			assertThat("JSON deflated", result, is(equalTo(json)));
		}
	}

}

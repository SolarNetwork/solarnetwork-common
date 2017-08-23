/* ==================================================================
 * LoggingHttpRequestInterceptor.java - 24/08/2017 6:17:31 AM
 * 
 * Copyright 2017 SolarNetwork.net Dev Team
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

package net.solarnetwork.web.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.DeflaterInputStream;
import java.util.zip.GZIPInputStream;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.FileCopyUtils;

/**
 * A client HTTP interceptor for logging the full details of each request.
 * 
 * <p>
 * Request messages are logged at {@literal TRACE} level under the
 * {@literal net.solarnetwork.http.REQ} logger; responses under
 * {@literal net.solarnetwork.http.RES}.
 * </p>
 * 
 * @author matt
 * @version 1.0
 */
public class LoggingHttpRequestInterceptor implements ClientHttpRequestInterceptor {

	private static final Logger REQ_LOG = LoggerFactory.getLogger("net.solarnetwork.http.REQ");
	private static final Logger RES_LOG = LoggerFactory.getLogger("net.solarnetwork.http.RES");

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body,
			ClientHttpRequestExecution execution) throws IOException {
		if ( REQ_LOG.isTraceEnabled() ) {
			traceRequest(request, body);
		}
		ClientHttpResponse response = execution.execute(request, body);
		if ( RES_LOG.isTraceEnabled() ) {
			traceResponse(response);
		}
		return response;
	}

	private void traceRequest(HttpRequest request, byte[] body) throws IOException {
		URI uri = request.getURI();
		StringBuilder buf = new StringBuilder("Begin request to: ");
		buf.append(uri).append('\n');
		buf.append(">>>>>>>>>> request begin >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n");
		buf.append(request.getMethod()).append(' ').append(uri.getPath());
		if ( uri.getRawQuery() != null ) {
			buf.append('?').append(uri.getRawQuery());
		}
		buf.append('\n');
		buf.append("Host: ").append(uri.getHost());
		if ( uri.getPort() > 0 ) {
			buf.append(':').append(uri.getPort());
		}
		buf.append('\n');
		for ( Map.Entry<String, List<String>> me : request.getHeaders().entrySet() ) {
			buf.append(me.getKey()).append(": ")
					.append(me.getValue().stream().collect(Collectors.joining(", "))).append('\n');
		}
		buf.append('\n');
		buf.append(new String(body, "UTF-8")).append('\n');
		buf.append(">>>>>>>>>> request end   >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		REQ_LOG.trace(buf.toString());
	}

	private void traceResponse(ClientHttpResponse response) throws IOException {
		final StringBuilder buf = new StringBuilder("Request response:\n");
		buf.append("<<<<<<<<<< response begin <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<\n");
		buf.append(response.getStatusCode()).append(' ').append(response.getStatusText()).append('\n');
		for ( Map.Entry<String, List<String>> me : response.getHeaders().entrySet() ) {
			buf.append(me.getKey()).append(": ")
					.append(me.getValue().stream().collect(Collectors.joining(", "))).append('\n');
		}
		buf.append('\n');
		HttpHeaders headers = response.getHeaders();
		MediaType contentType = headers.getContentType();
		InputStream in = response.getBody();
		if ( contentType.isCompatibleWith(MediaType.APPLICATION_JSON)
				|| contentType.isCompatibleWith(MediaType.valueOf("text/*")) ) {
			// print out textual content; possibly decoding gzip/defalte
			if ( headers.containsKey(HttpHeaders.CONTENT_ENCODING) ) {
				String enc = headers.getFirst(HttpHeaders.CONTENT_ENCODING);
				if ( "gzip".equalsIgnoreCase(enc) ) {
					in = new GZIPInputStream(in);
				} else if ( "deflate".equalsIgnoreCase(enc) ) {
					in = new DeflaterInputStream(in);
				}
			}
			FileCopyUtils.copy(new InputStreamReader(in, "UTF-8"), new Writer() {

				@Override
				public void write(char[] cbuf, int off, int len) throws IOException {
					buf.append(cbuf, off, len);
				}

				@Override
				public void flush() throws IOException {
					// nothing to do
				}

				@Override
				public void close() throws IOException {
					// nothing to do
				}
			});
		} else {
			// dump binary data as Hex encoded text, wrapped to 80 characters
			byte[] byteBuf = new byte[40];
			int readLength = 0;
			int bufPos = 0;
			while ( true ) {
				readLength = in.read(byteBuf, bufPos, byteBuf.length - bufPos);
				bufPos += readLength;
				if ( readLength < 0 ) {
					break;
				}
				if ( bufPos + 1 == byteBuf.length ) {
					buf.append(Hex.encodeHex(byteBuf)).append('\n');
					bufPos = 0;
				}
			}
			if ( bufPos + 1 < byteBuf.length && bufPos > 0 ) {
				byte[] tmp = new byte[bufPos + 1];
				System.arraycopy(byteBuf, 0, tmp, 0, bufPos);
				buf.append(Hex.encodeHex(tmp)).append('\n');
			}
		}
		buf.append("<<<<<<<<<< response end   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
		RES_LOG.trace(buf.toString());
	}

}

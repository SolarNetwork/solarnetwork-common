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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.client.HttpStatusCodeException;

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
 * @version 1.1
 */
public class LoggingHttpRequestInterceptor implements ClientHttpRequestInterceptor {

	private static final Logger REQ_LOG = LoggerFactory.getLogger("net.solarnetwork.http.REQ");
	private static final Logger RES_LOG = LoggerFactory.getLogger("net.solarnetwork.http.RES");

	/**
	 * Constructor.
	 */
	public LoggingHttpRequestInterceptor() {
		super();
	}

	/**
	 * Get a client request factory with logging support if either request or
	 * response logging is enabled.
	 * 
	 * <p>
	 * This method will check if either {@code REQ_LOG} or {@code RES_LOG} have
	 * {@literal TRACE} level logging enabled, and if so return a
	 * {@link BufferingClientHttpRequestFactory} suitable for logging. Otherwise
	 * the default {@link SimpleClientHttpRequestFactory} is returned.
	 * 
	 * @return a new request factory
	 */
	public static ClientHttpRequestFactory requestFactory() {
		return requestFactory(new SimpleClientHttpRequestFactory());
	}

	/**
	 * Wrap a client request factory with logging support if either request or
	 * response logging is enabled.
	 * 
	 * <p>
	 * This method will check if either {@code REQ_LOG} or {@code RES_LOG} have
	 * {@literal TRACE} level logging enabled, and if so return a
	 * {@link BufferingClientHttpRequestFactory} suitable for logging. Otherwise
	 * {@code requestFactory} is returned directly.
	 * </p>
	 * 
	 * @param requestFactory
	 *        request factory to possibly wrap
	 * @return the request factory
	 * @since 1.1
	 */
	public static ClientHttpRequestFactory requestFactory(ClientHttpRequestFactory requestFactory) {
		ClientHttpRequestFactory reqFactory = requestFactory;
		if ( REQ_LOG.isTraceEnabled() || RES_LOG.isTraceEnabled() ) {
			reqFactory = new BufferingClientHttpRequestFactory(requestFactory);
		}
		return reqFactory;
	}

	/**
	 * Test if a {@link ClientHttpRequestFactory} supports logging with this
	 * interceptor.
	 * 
	 * @param reqFactory
	 *        the request factory to test
	 * @return {@literal true} if the factory is a
	 *         {@link BufferingClientHttpRequestFactory}
	 */
	public static boolean supportsLogging(ClientHttpRequestFactory reqFactory) {
		return (reqFactory instanceof BufferingClientHttpRequestFactory);
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body,
			ClientHttpRequestExecution execution) throws IOException {
		if ( REQ_LOG.isTraceEnabled() ) {
			traceRequest(request, body);
		}
		try {
			ClientHttpResponse response = execution.execute(request, body);
			if ( RES_LOG.isTraceEnabled() ) {
				traceResponse(response);
			}
			return response;
		} catch ( HttpStatusCodeException e ) {
			traceResponse(e.getStatusCode(), e.getStatusText(), e.getResponseHeaders(),
					new ByteArrayInputStream(e.getResponseBodyAsByteArray()), null);
			throw e;
		} catch ( IOException e ) {
			traceResponse(e);
			throw e;
		} catch ( RuntimeException e ) {
			traceResponse(e);
			throw e;
		}
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

	private void traceResponse(Throwable e) {
		try {
			traceResponse(null, null, null, null, e);
		} catch ( Exception ex ) {
			RES_LOG.trace("{} tracing response", ex.getClass().getName(), ex);
		}
	}

	private void traceResponse(ClientHttpResponse response) {
		try {
			traceResponse(response.getStatusCode(), response.getStatusText(), response.getHeaders(),
					response.getBody(), null);
		} catch ( Exception ex ) {
			RES_LOG.trace("{} tracing response", ex.getClass().getName(), ex);
		}
	}

	private void traceResponse(HttpStatus statusCode, String statusText, HttpHeaders headers,
			InputStream in, Throwable exception) throws IOException {
		final StringBuilder buf = new StringBuilder("Request response:\n");
		buf.append("<<<<<<<<<< response begin <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<\n");
		if ( statusCode != null ) {
			buf.append(statusCode).append(' ').append(statusText).append('\n');
		}
		if ( exception != null ) {
			StringWriter w = new StringWriter();
			exception.printStackTrace(new PrintWriter(w));
			buf.append(w.toString());
		}
		if ( headers != null ) {
			for ( Map.Entry<String, List<String>> me : headers.entrySet() ) {
				buf.append(me.getKey()).append(": ")
						.append(me.getValue().stream().collect(Collectors.joining(", "))).append('\n');
			}
			buf.append('\n');
			traceResponseBody(headers, in, buf);
		}
		buf.append("<<<<<<<<<< response end   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
		RES_LOG.trace(buf.toString());
	}

	private void traceResponseBody(HttpHeaders headers, InputStream in, final StringBuilder buf)
			throws IOException, UnsupportedEncodingException {
		if ( headers == null || in == null || buf == null ) {
			return;
		}
		MediaType contentType = headers.getContentType();
		if ( contentType != null && (contentType.isCompatibleWith(MediaType.APPLICATION_JSON)
				|| contentType.isCompatibleWith(MediaType.valueOf("text/*"))
				|| contentType.isCompatibleWith(MediaType.APPLICATION_XML)) ) {
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
			// make sure we have a terminating newline, for the closing "response end" line
			if ( buf.charAt(buf.length() - 1) != '\n' ) {
				buf.append('\n');
			}
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
				if ( bufPos >= byteBuf.length ) {
					buf.append(Hex.encodeHex(byteBuf)).append('\n');
					bufPos = 0;
				}
			}
			if ( bufPos < byteBuf.length && bufPos > 0 ) {
				byte[] tmp = new byte[bufPos];
				System.arraycopy(byteBuf, 0, tmp, 0, bufPos);
				buf.append(Hex.encodeHex(tmp)).append('\n');
			}
		}
	}

}

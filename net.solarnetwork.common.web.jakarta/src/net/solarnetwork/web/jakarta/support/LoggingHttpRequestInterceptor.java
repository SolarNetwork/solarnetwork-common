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

package net.solarnetwork.web.jakarta.support;

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
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.DeflaterInputStream;
import java.util.zip.GZIPInputStream;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
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
 * {@literal net.solarnetwork.http.RES}. If {@code dynamicUrlLoggers} is set to
 * {@code true} then URL components will be added as logger components:
 * </p>
 *
 * <ol>
 * <li>the host name</li>
 * <li>the port</li>
 * <li>the path, with {@code /} characters replaced by {@code .}</li>
 * </ol>
 *
 * @author matt
 * @version 1.3
 */
public class LoggingHttpRequestInterceptor implements ClientHttpRequestInterceptor {

	/**
	 * The static request log name, or dynamic request log prefix.
	 *
	 * @since 1.3
	 */
	public static final String REQUEST_LOG_NAME = "net.solarnetwork.http.REQ";

	/**
	 * The static response log name, or dynamic response log prefix.
	 *
	 * @since 1.4
	 */
	public static final String RESPONSE_LOG_NAME = "net.solarnetwork.http.RES";

	private static final Logger REQ_LOG = LoggerFactory.getLogger(REQUEST_LOG_NAME);
	private static final Logger RES_LOG = LoggerFactory.getLogger(RESPONSE_LOG_NAME);

	private static final Pattern PATH_SPLIT_REGEX = Pattern.compile("/");

	private final Function<String, Logger> dynamicLoggerProvider;
	private final Function<HttpRequest, String> requestIdProvider;
	private final boolean dynamicUrlLoggers;

	/**
	 * Constructor.
	 */
	public LoggingHttpRequestInterceptor() {
		this(false);
	}

	/**
	 * Constructor.
	 *
	 * @param dynamicUrlLoggers
	 *        {@code true} to dynamically add URL components to the resolved
	 *        request/response loggers
	 * @since 1.3
	 */
	public LoggingHttpRequestInterceptor(final boolean dynamicUrlLoggers) {
		this(dynamicUrlLoggers, null, null);
	}

	/**
	 * Constructor.
	 *
	 * @param dynamicUrlLoggers
	 *        {@code true} to dynamically add URL components to the resolved
	 *        request/response loggers
	 * @param dynamicLoggerProvider
	 *        function to supply dynamic loggers, or {@code null} for default
	 *        provider
	 * @param requestIdProvider
	 *        the request ID provider, or {@code null} for a default (random)
	 *        provider
	 * @since 1.3
	 */
	public LoggingHttpRequestInterceptor(final boolean dynamicUrlLoggers,
			final Function<String, Logger> dynamicLoggerProvider,
			Function<HttpRequest, String> requestIdProvider) {
		super();
		this.dynamicUrlLoggers = dynamicUrlLoggers;
		this.dynamicLoggerProvider = dynamicLoggerProvider != null ? dynamicLoggerProvider
				: LoggerFactory::getLogger;
		this.requestIdProvider = requestIdProvider != null ? requestIdProvider
				: LoggingHttpRequestInterceptor::randomRequestId;
	}

	/**
	 * The default request ID provider.
	 *
	 * <p>
	 * This implementation returns a random UUID string.
	 * </p>
	 *
	 * @param request
	 *        the HTTP request
	 * @return the random ID
	 * @since 1.3
	 */
	public static String randomRequestId(HttpRequest request) {
		return UUID.randomUUID().toString();
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
	 * </p>
	 *
	 * @return a new request factory
	 */
	public static ClientHttpRequestFactory requestFactory() {
		return requestFactory(false, null);
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
	 * </p>
	 *
	 * @param dynamicUrlLoggers
	 *        {@code true} to dynamically add URL components to the resolved
	 *        request/response loggers
	 * @param dynamicLoggerProvider
	 *        function to supply dynamic loggers, or {@code null} for default
	 *        provider
	 * @return a new request factory
	 * @since 1.3
	 */
	public static ClientHttpRequestFactory requestFactory(final boolean dynamicUrlLoggers,
			final Function<String, Logger> dynamicLoggerProvider) {
		return requestFactory(new SimpleClientHttpRequestFactory(), dynamicUrlLoggers);
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
	public static ClientHttpRequestFactory requestFactory(
			final ClientHttpRequestFactory requestFactory) {
		return requestFactory(requestFactory, false);
	}

	/**
	 * Wrap a client request factory with logging support if either request or
	 * response logging is enabled.
	 *
	 * <p>
	 * This method will check if either {@code REQ_LOG} or {@code RES_LOG} have
	 * {@code TRACE} (if {@code dynamiceUrlLoggers} is {@code false}) or
	 * {@code DEBUG} (if {@code dynamicUrlLoggers} is {@code true}) level
	 * logging enabled, and if so return a
	 * {@link BufferingClientHttpRequestFactory} suitable for logging. Otherwise
	 * {@code requestFactory} is returned directly.
	 * </p>
	 *
	 * @param requestFactory
	 *        request factory to possibly wrap
	 * @param dynamicUrlLoggers
	 *        {@code true} to dynamically add URL components to the resolved
	 *        request/response loggers
	 * @return the request factory
	 * @since 1.3
	 */
	public static ClientHttpRequestFactory requestFactory(final ClientHttpRequestFactory requestFactory,
			final boolean dynamicUrlLoggers) {
		ClientHttpRequestFactory reqFactory = requestFactory;
		if ( (dynamicUrlLoggers && (REQ_LOG.isDebugEnabled() || RES_LOG.isDebugEnabled()))
				|| (!dynamicUrlLoggers && (REQ_LOG.isTraceEnabled() || RES_LOG.isTraceEnabled())) ) {
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
	public static boolean supportsLogging(final ClientHttpRequestFactory reqFactory) {
		return (reqFactory instanceof BufferingClientHttpRequestFactory);
	}

	private String dynamicLoggerName(URI uri) {
		if ( !dynamicUrlLoggers ) {
			return null;
		}

		StringBuilder buf = new StringBuilder();

		String comp = uri.getHost();
		if ( comp != null && !comp.isEmpty() ) {
			buf.append('.');
			buf.append(comp);
		}

		if ( uri.getPort() >= 0 ) {
			comp = String.valueOf(uri.getPort());
			if ( buf.charAt(buf.length() - 1) != '.' ) {
				buf.append('.');
			}
			buf.append(comp);
		}

		comp = uri.getPath();
		if ( comp != null && !comp.isBlank() ) {
			String[] comps = PATH_SPLIT_REGEX.split(comp, 0);
			for ( String c : comps ) {
				if ( !c.isEmpty() ) {
					buf.append('.');
					buf.append(c);
				}
			}
		}
		return buf.toString();
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body,
			ClientHttpRequestExecution execution) throws IOException {
		final String dynamicLoggerName = dynamicLoggerName(request.getURI());
		final Logger requestLog = dynamicLoggerName != null
				? dynamicLoggerProvider.apply(REQUEST_LOG_NAME + dynamicLoggerName)
				: REQ_LOG;
		final Logger responseLog = dynamicLoggerName != null
				? dynamicLoggerProvider.apply(RESPONSE_LOG_NAME + dynamicLoggerName)
				: RES_LOG;
		final String exchangeId = requestIdProvider.apply(request);
		if ( requestLog.isTraceEnabled() ) {
			traceRequest(requestLog, exchangeId, request, body);
		}
		try {
			ClientHttpResponse response = execution.execute(request, body);
			if ( responseLog.isTraceEnabled() ) {
				traceResponse(responseLog, exchangeId, response);
			}
			return response;
		} catch ( HttpStatusCodeException e ) {
			// reflection-access to work with Spring 5 and 6, where the method signature changed
			HttpStatusCode statusCode = e.getStatusCode();
			traceResponse(responseLog, exchangeId, statusCode, e.getStatusText(), e.getResponseHeaders(),
					new ByteArrayInputStream(e.getResponseBodyAsByteArray()), null);
			throw e;
		} catch ( IOException e ) {
			traceResponse(responseLog, exchangeId, e);
			throw e;
		} catch ( RuntimeException e ) {
			traceResponse(responseLog, exchangeId, e);
			throw e;
		}
	}

	private void traceRequest(final Logger log, final String exchangeId, final HttpRequest request,
			final byte[] body) throws IOException {
		URI uri = request.getURI();
		StringBuilder buf = new StringBuilder("Begin request to: ");
		buf.append(uri).append('\n');
		buf.append(">>>>>>>>>> request begin ").append(exchangeId)
				.append(" >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n");
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
		buf.append(">>>>>>>>>> request end   ").append(exchangeId)
				.append(" >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		log.trace(buf.toString());
	}

	private void traceResponse(Logger log, String exchangeId, Throwable e) {
		try {
			traceResponse(log, exchangeId, null, null, null, null, e);
		} catch ( Exception ex ) {
			log.trace("{} tracing response", ex.getClass().getName(), ex);
		}
	}

	private void traceResponse(Logger log, String exchangeId, ClientHttpResponse response) {
		try {
			traceResponse(log, exchangeId, response.getStatusCode().value(), response.getStatusText(),
					response.getHeaders(), response.getBody(), null);
		} catch ( Exception ex ) {
			log.trace("{} tracing response", ex.getClass().getName(), ex);
		}
	}

	private void traceResponse(Logger log, String exchangeId, Object statusCode, String statusText,
			HttpHeaders headers, InputStream in, Throwable exception) throws IOException {
		final StringBuilder buf = new StringBuilder("Request response:\n");
		buf.append("<<<<<<<<<< response begin ").append(exchangeId)
				.append(" <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<\n");
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
		buf.append("<<<<<<<<<< response end   ").append(exchangeId)
				.append(" <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
		log.trace(buf.toString());
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

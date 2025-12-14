/* ==================================================================
 * LoggingHttpRequestInterceptorTests.java - 5/08/2025 6:07:52 am
 *
 * Copyright 2025 SolarNetwork.net Dev Team
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringWhiteSpace;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
import org.junit.Test;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.LegacyAbstractLogger;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import net.solarnetwork.test.http.AbstractHttpServerTests;
import net.solarnetwork.test.http.TestHttpHandler;
import net.solarnetwork.web.jakarta.support.LoggingHttpRequestInterceptor;

/**
 * Test cases for the {@link LoggingHttpRequestInterceptor} class.
 *
 * @author matt
 * @version 1.0
 */
public class LoggingHttpRequestInterceptorTests extends AbstractHttpServerTests {

	private RestTemplate restTemplate(LoggingHttpRequestInterceptor interceptor) {
		RestTemplate restTemplate = WebTestUtils.testRestTemplate();
		RestTemplate debugTemplate = new RestTemplate(
				new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
		debugTemplate.setInterceptors(List.of(interceptor));
		debugTemplate.setMessageConverters(restTemplate.getMessageConverters());
		return debugTemplate;
	}

	private record LogEvent(Level level, String message, Object[] args, Throwable throwable) {

	}

	private static class InMemoryLogger extends LegacyAbstractLogger {

		private static final long serialVersionUID = -7467060751423930571L;

		private final List<LogEvent> events = new ArrayList<>();

		private InMemoryLogger(String name) {
			super();
			this.name = name;
		}

		@Override
		public boolean isTraceEnabled() {
			return true;
		}

		@Override
		public boolean isDebugEnabled() {
			return true;
		}

		@Override
		public boolean isInfoEnabled() {
			return true;
		}

		@Override
		public boolean isWarnEnabled() {
			return true;
		}

		@Override
		public boolean isErrorEnabled() {
			return true;
		}

		@Override
		protected String getFullyQualifiedCallerName() {
			return "";
		}

		@Override
		protected void handleNormalizedLoggingCall(Level level, Marker marker, String messagePattern,
				Object[] arguments, Throwable throwable) {
			events.add(new LogEvent(level, messagePattern, arguments, throwable));
		}

	}

	private static String dynamicRequestLoggerName(String dynamicLoggerName) {
		return "net.solarnetwork.http.REQ" + dynamicLoggerName;
	}

	private static String dynamicResponseLoggerName(String dynamicLoggerName) {
		return "net.solarnetwork.http.RES" + dynamicLoggerName;
	}

	@Test
	public void get_staticPath() {
		// GIVEN
		final String responseText = UUID.randomUUID().toString();
		TestHttpHandler handler = new TestHttpHandler() {

			@Override
			protected boolean handleInternal(Request request, Response response, Callback callback)
					throws Exception {
				respondWithText(request, response, responseText);
				return true;
			}

		};
		addHandler(handler);

		final Map<String, InMemoryLogger> loggers = new ConcurrentHashMap<>(2);
		final String requestId = UUID.randomUUID().toString();

		final var service = new LoggingHttpRequestInterceptor(false,
				(logName) -> loggers.computeIfAbsent(logName, (name) -> new InMemoryLogger(name)),
				(request) -> requestId);
		final var rest = restTemplate(service);

		// WHEN
		final URI uri = URI.create(getHttpServerBaseUrl() + "/foo/bar?a=b");
		final String result = rest.getForObject(uri, String.class);

		// THEN
		assertThat("Result returned from server", result, is(equalTo(responseText)));
		assertThat("No dynamic loggers created for request/response", loggers.keySet(), hasSize(0));
	}

	@Test
	public void get_dynamicPath() {
		// GIVEN
		final String responseText = UUID.randomUUID().toString();
		TestHttpHandler handler = new TestHttpHandler() {

			@Override
			protected boolean handleInternal(Request request, Response response, Callback callback)
					throws Exception {
				respondWithText(request, response, responseText);
				return true;
			}

		};
		addHandler(handler);

		final Map<String, InMemoryLogger> loggers = new ConcurrentHashMap<>(2);
		final String requestId = UUID.randomUUID().toString();

		final var service = new LoggingHttpRequestInterceptor(true,
				(logName) -> loggers.computeIfAbsent(logName, (name) -> new InMemoryLogger(name)),
				(request) -> requestId);
		final var rest = restTemplate(service);

		// WHEN
		final URI uri = URI.create(getHttpServerBaseUrl() + "/foo/bar?a=b");
		final String result = rest.getForObject(uri, String.class);

		try {
			Thread.sleep(200L);
		} catch ( InterruptedException e ) {
			// continue
		}

		// THEN
		assertThat("Result returned from server", result, is(equalTo(responseText)));
		assertThat("Dynamic loggers created for request/response", loggers.keySet(), hasSize(2));

		final String expectedDynamicLoggerName = ".localhost." + getHttpServerPort() + ".foo.bar";
		final String expectedRequestLoggerName = dynamicRequestLoggerName(expectedDynamicLoggerName);
		final String expectedResponseLoggerName = dynamicResponseLoggerName(expectedDynamicLoggerName);
		assertThat("Dynamic request logger created for host, port, path", loggers,
				hasKey(expectedRequestLoggerName));
		final InMemoryLogger requestLogger = loggers.get(expectedRequestLoggerName);
		assertThat("Request log event created", requestLogger.events, hasSize(1));
		final LogEvent requestEvent = requestLogger.events.get(0);
		assertThat("Request log event is TRACE level", requestEvent.level, is(equalTo(Level.TRACE)));
		assertThat("Request log message", requestEvent.message, is(equalToIgnoringWhiteSpace("""
				Begin request to: http://localhost:%d/foo/bar?a=b
				>>>>>>>>>> request begin %s >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
				GET /foo/bar?a=b
				Host: localhost:%1$d
				Accept: text/plain, application/json, application/*+json, */*
				Content-Length: 0


				>>>>>>>>>> request end   %2$s >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
				""".formatted(getHttpServerPort(), requestId))));

		assertThat("Dynamic response logger created for host, port, path", loggers,
				hasKey(expectedResponseLoggerName));
		final InMemoryLogger responseLogger = loggers.get(expectedResponseLoggerName);
		assertThat("Response log event created", responseLogger.events, hasSize(1));
		final LogEvent responseEvent = responseLogger.events.get(0);
		assertThat("Response log event is TRACE level", responseEvent.level, is(equalTo(Level.TRACE)));
		assertThat("Response log message",
				responseEvent.message.replaceAll("Date:[ a-zA-Z0-9:,-]+\\n", "Date: -----\n"),
				is(equalToIgnoringWhiteSpace("""
						Request response:
						<<<<<<<<<< response begin %s <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
						200 OK
						Server: Jetty(12.0.22)
						Date: -----
						Content-Type: text/plain; charset=utf-8
						Transfer-Encoding: chunked

						%s
						<<<<<<<<<< response end   %1$s <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
						""".formatted(requestId, responseText))));
	}

}

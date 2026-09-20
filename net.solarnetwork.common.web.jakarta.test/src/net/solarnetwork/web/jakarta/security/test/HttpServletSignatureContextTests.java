/* ==================================================================
 * HttpServletSignatureContextTests.java - 20/09/2026 2:48:32 pm
 *
 * Copyright 2026 SolarNetwork.net Dev Team
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

package net.solarnetwork.web.jakarta.security.test;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import jakarta.servlet.http.HttpServletRequest;
import net.solarnetwork.web.jakarta.security.HttpServletSignatureContext;

/**
 * Test cases for the {@link HttpServletSignatureContext} class.
 *
 * @author matt
 * @version 1.0
 */
public class HttpServletSignatureContextTests {

	private HttpServletRequest request;

	@Before
	public void setup() {
		request = mock(HttpServletRequest.class);
	}

	private HttpServletSignatureContext context() {
		return new HttpServletSignatureContext(request, null);
	}

	private HttpServletSignatureContext context(String explicitHost) {
		return new HttpServletSignatureContext(request, explicitHost);
	}

	@Test
	public void method() {
		// GIVEN
		given(request.getMethod()).willReturn("POST");

		// THEN
		// @formatter:off
		then(context().method())
			.as("Method taken from the request")
			.isEqualTo("POST")
			;
		// @formatter:on
	}

	@Test
	public void path() {
		// GIVEN
		given(request.getRequestURI()).willReturn("/api/v1/sec/datum/list");

		// THEN
		// @formatter:off
		then(context().path())
			.as("Path taken from the request URI")
			.isEqualTo("/api/v1/sec/datum/list")
			;
		// @formatter:on
	}

	@Test
	public void path_null() {
		// GIVEN
		given(request.getRequestURI()).willReturn(null);

		// THEN
		// @formatter:off
		then(context().path())
			.as("A request with no URI is treated as the root path, per RFC 9421 2.2.6")
			.isEqualTo("/")
			;
		// @formatter:on
	}

	@Test
	public void path_empty() {
		// GIVEN
		given(request.getRequestURI()).willReturn("");

		// THEN
		// @formatter:off
		then(context().path())
			.as("An empty request URI is treated as the root path, per RFC 9421 2.2.6")
			.isEqualTo("/")
			;
		// @formatter:on
	}

	@Test
	public void query() {
		// GIVEN
		given(request.getQueryString()).willReturn("nodeId=123&sourceId=a%2Fb");

		// THEN
		// @formatter:off
		then(context().query())
			.as("Query includes the leading question mark, per RFC 9421 2.2.7")
			.isEqualTo("?nodeId=123&sourceId=a%2Fb")
			;
		// @formatter:on
	}

	@Test
	public void query_none() {
		// GIVEN
		given(request.getQueryString()).willReturn(null);

		// THEN
		// @formatter:off
		then(context().query())
			.as("A request with no query is a bare question mark, per RFC 9421 2.2.7")
			.isEqualTo("?")
			;
		// @formatter:on
	}

	@Test
	public void requestTarget() {
		// GIVEN
		given(request.getRequestURI()).willReturn("/api/v1/sec/datum/list");
		given(request.getQueryString()).willReturn("nodeId=123");

		// THEN
		// @formatter:off
		then(context().requestTarget())
			.as("Request target is the path and query")
			.isEqualTo("/api/v1/sec/datum/list?nodeId=123")
			;
		// @formatter:on
	}

	@Test
	public void requestTarget_noQuery() {
		// GIVEN
		given(request.getRequestURI()).willReturn("/api/v1/sec/datum/list");
		given(request.getQueryString()).willReturn(null);

		// THEN
		// @formatter:off
		then(context().requestTarget())
			.as("Request target of a query-less request is just the path")
			.isEqualTo("/api/v1/sec/datum/list")
			;
		// @formatter:on
	}

	@Test
	public void targetUri() {
		// GIVEN
		given(request.getScheme()).willReturn("https");
		given(request.getHeader("host")).willReturn("data.solarnetwork.net");
		given(request.getRequestURI()).willReturn("/api/v1/sec/datum/list");
		given(request.getQueryString()).willReturn("nodeId=123");

		// THEN
		// @formatter:off
		then(context().targetUri())
			.as("Target URI assembles the scheme, authority, path and query")
			.isEqualTo("https://data.solarnetwork.net/api/v1/sec/datum/list?nodeId=123")
			;
		// @formatter:on
	}

	@Test
	public void targetUri_noQuery() {
		// GIVEN
		given(request.getScheme()).willReturn("http");
		given(request.getHeader("host")).willReturn("data.solarnetwork.net");
		given(request.getRequestURI()).willReturn("/api/v1/sec/datum/list");
		given(request.getQueryString()).willReturn(null);

		// THEN
		// @formatter:off
		then(context().targetUri())
			.as("Target URI of a query-less request has no query part")
			.isEqualTo("http://data.solarnetwork.net/api/v1/sec/datum/list")
			;
		// @formatter:on
	}

	@Test
	public void scheme_fromRequest() {
		// GIVEN
		given(request.getScheme()).willReturn("HTTPS");

		// THEN
		// @formatter:off
		then(context().scheme())
			.as("Scheme from the request is lower-cased")
			.isEqualTo("https")
			;
		// @formatter:on
	}

	@Test
	public void scheme_null() {
		// GIVEN
		given(request.getScheme()).willReturn(null);

		// THEN
		// @formatter:off
		then(context().scheme())
			.as("A request with no scheme falls back to http")
			.isEqualTo("http")
			;
		// @formatter:on
	}

	@Test
	public void scheme_forwardedProto() {
		// GIVEN
		given(request.getHeader("X-Forwarded-Proto")).willReturn(" HTTPS ");
		given(request.getScheme()).willReturn("http");

		// THEN
		// @formatter:off
		then(context().scheme())
			.as("A proxy forwarded protocol wins over the container scheme, trimmed and lower-cased")
			.isEqualTo("https")
			;
		// @formatter:on
	}

	@Test
	public void scheme_forwardedProto_empty() {
		// GIVEN
		given(request.getHeader("X-Forwarded-Proto")).willReturn("");
		given(request.getScheme()).willReturn("http");

		// THEN
		// @formatter:off
		then(context().scheme())
			.as("An empty forwarded protocol falls back to the container scheme")
			.isEqualTo("http")
			;
		// @formatter:on
	}

	@Test
	public void authority_explicitHost() {
		// GIVEN
		given(request.getScheme()).willReturn("https");
		given(request.getHeader("host")).willReturn("internal.example.com:8080");

		// THEN
		// @formatter:off
		then(context("Data.SolarNetwork.NET").authority())
			.as("An explicit host overrides the Host header, and is normalized")
			.isEqualTo("data.solarnetwork.net")
			;
		// @formatter:on
	}

	@Test
	public void authority_hostHeader() {
		// GIVEN
		given(request.getScheme()).willReturn("http");
		given(request.getHeader("host")).willReturn("Data.SolarNetwork.NET");

		// THEN
		// @formatter:off
		then(context().authority())
			.as("Authority is the lower-cased Host header, per RFC 9421 2.2.3")
			.isEqualTo("data.solarnetwork.net")
			;
		// @formatter:on
	}

	@Test
	public void authority_hostHeader_nonDefaultPort() {
		// GIVEN
		given(request.getScheme()).willReturn("https");
		given(request.getHeader("host")).willReturn("data.solarnetwork.net:8443");

		// THEN
		// @formatter:off
		then(context().authority())
			.as("A non-default port is preserved")
			.isEqualTo("data.solarnetwork.net:8443")
			;
		// @formatter:on
	}

	@Test
	public void authority_hostHeader_defaultHttpsPort() {
		// GIVEN
		given(request.getScheme()).willReturn("https");
		given(request.getHeader("host")).willReturn("data.solarnetwork.net:443");

		// THEN
		// @formatter:off
		then(context().authority())
			.as("The default https port is omitted, per RFC 9421 2.2.3")
			.isEqualTo("data.solarnetwork.net")
			;
		// @formatter:on
	}

	@Test
	public void authority_hostHeader_defaultHttpPort() {
		// GIVEN
		given(request.getScheme()).willReturn("http");
		given(request.getHeader("host")).willReturn("data.solarnetwork.net:80");

		// THEN
		// @formatter:off
		then(context().authority())
			.as("The default http port is omitted, per RFC 9421 2.2.3")
			.isEqualTo("data.solarnetwork.net")
			;
		// @formatter:on
	}

	@Test
	public void authority_hostHeader_forwardedPort() {
		// GIVEN
		given(request.getScheme()).willReturn("http");
		given(request.getHeader("host")).willReturn("data.solarnetwork.net");
		given(request.getHeader("X-Forwarded-Port")).willReturn("8443");

		// THEN
		// @formatter:off
		then(context().authority())
			.as("A port a proxy stripped from the Host header is restored")
			.isEqualTo("data.solarnetwork.net:8443")
			;
		// @formatter:on
	}

	@Test
	public void authority_hostHeader_forwardedProtoImpliesHttpsPort() {
		// GIVEN
		given(request.getHeader("X-Forwarded-Proto")).willReturn("https");
		given(request.getHeader("host")).willReturn("data.solarnetwork.net");

		// THEN
		// @formatter:off
		then(context().authority())
			.as("A forwarded https request implies port 443, which is then omitted as the default")
			.isEqualTo("data.solarnetwork.net")
			;
		// @formatter:on
	}

	@Test
	public void authority_noHostHeader() {
		// GIVEN
		given(request.getScheme()).willReturn("https");
		given(request.getHeader("host")).willReturn(null);
		given(request.getServerName()).willReturn("Data.SolarNetwork.NET");
		given(request.getServerPort()).willReturn(8443);

		// THEN
		// @formatter:off
		then(context().authority())
			.as("Without a Host header the container resolved name and port are used")
			.isEqualTo("data.solarnetwork.net:8443")
			;
		// @formatter:on
	}

	@Test
	public void authority_emptyHostHeader_noServerPort() {
		// GIVEN
		given(request.getScheme()).willReturn("https");
		given(request.getHeader("host")).willReturn("");
		given(request.getServerName()).willReturn("data.solarnetwork.net");
		given(request.getServerPort()).willReturn(0);

		// THEN
		// @formatter:off
		then(context().authority())
			.as("An empty Host header falls back to the container name, with no port to add")
			.isEqualTo("data.solarnetwork.net")
			;
		// @formatter:on
	}

	@Test
	public void authority_noHostHeader_noServerName() {
		// GIVEN
		given(request.getScheme()).willReturn("https");
		given(request.getHeader("host")).willReturn(null);
		given(request.getServerName()).willReturn(null);
		given(request.getServerPort()).willReturn(8443);

		// THEN
		// @formatter:off
		then(context().authority())
			.as("With nothing to resolve a host name from, only the port remains")
			.isEqualTo(":8443")
			;
		// @formatter:on
	}

	@Test
	public void authority_ipv6Literal() {
		// GIVEN
		given(request.getScheme()).willReturn("https");
		given(request.getHeader("host")).willReturn("[2001:DB8::1]");

		// THEN
		// @formatter:off
		then(context().authority())
			.as("A colon within an IPv6 literal is not mistaken for a port delimiter")
			.isEqualTo("[2001:db8::1]")
			;
		// @formatter:on
	}

	@Test
	public void authority_ipv6LiteralWithPort() {
		// GIVEN
		given(request.getScheme()).willReturn("https");
		given(request.getHeader("host")).willReturn("[2001:db8::1]:8443");

		// THEN
		// @formatter:off
		then(context().authority())
			.as("A port after an IPv6 literal is preserved")
			.isEqualTo("[2001:db8::1]:8443")
			;
		// @formatter:on
	}

	@Test
	public void fieldValues() {
		// GIVEN
		given(request.getHeaders("X-SN-Foo"))
				.willReturn(Collections.enumeration(List.of("one", "two")));

		// WHEN
		final List<String> result = context().fieldValues("X-SN-Foo");

		// THEN
		// @formatter:off
		then(result)
			.as("Every value of a repeated field is returned, in order")
			.containsExactly("one", "two")
			;
		thenThrownBy(() -> result.add("three"))
			.as("The returned list is unmodifiable")
			.isInstanceOf(UnsupportedOperationException.class)
			;
		// @formatter:on
	}

	@Test
	public void fieldValues_missing() {
		// GIVEN
		given(request.getHeaders("X-SN-Foo")).willReturn(null);

		// THEN
		// @formatter:off
		then(context().fieldValues("X-SN-Foo"))
			.as("A container that returns no enumeration for a missing field yields no values")
			.isEmpty()
			;
		// @formatter:on
	}

	@Test
	public void queryParamValues() {
		// GIVEN
		given(request.getQueryString()).willReturn("nodeId=123&nodeId=234&sourceId=a");

		// THEN
		// @formatter:off
		then(context().queryParamValues("nodeId"))
			.as("Every value of a repeated query parameter is returned, in order")
			.containsExactly("123", "234")
			;
		// @formatter:on
	}

	@Test
	public void queryParamValues_noQuery() {
		// GIVEN
		given(request.getQueryString()).willReturn(null);

		// THEN
		// @formatter:off
		then(context().queryParamValues("nodeId"))
			.as("A request with no query has no query parameter values")
			.isEmpty()
			;
		// @formatter:on
	}

}

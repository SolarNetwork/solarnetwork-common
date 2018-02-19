/* ==================================================================
 * AuthenticationDataV2Tests.java - 2/03/2017 12:29:20 PM
 * 
 * Copyright 2007-2017 SolarNetwork.net Dev Team
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

package net.solarnetwork.web.security.test;

import static net.solarnetwork.web.security.AuthenticationData.nullSafeHeaderValue;
import static net.solarnetwork.web.security.AuthenticationUtils.uriEncode;
import static net.solarnetwork.web.security.test.SecurityWebTestUtils.computeHMACSHA256;
import static net.solarnetwork.web.security.test.SecurityWebTestUtils.httpDate;
import static net.solarnetwork.web.security.test.SecurityWebTestUtils.iso8601Date;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.util.StringUtils;
import net.solarnetwork.web.security.AuthenticationDataV2;
import net.solarnetwork.web.security.AuthenticationScheme;
import net.solarnetwork.web.security.AuthorizationV2Builder;
import net.solarnetwork.web.security.SecurityHttpServletRequestWrapper;
import net.solarnetwork.web.security.WebConstants;

/**
 * Unit tests for the {@link AuthenticationDataV2} class.
 * 
 * @author matt
 * @version 1.2
 */
public class AuthenticationDataV2Tests {

	private static final String HTTP_HEADER_AUTH = "Authorization";
	private static final String HTTP_HEADER_HOST = "Host";
	private static final String TEST_HOST = "host.example.com";
	private static final String TEST_AUTH_TOKEN = "12345678901234567890";
	private static final String TEST_PASSWORD = "lsdjfpse9jfoeijfe09j";
	private static final long TEST_MAX_DATE_SKEW = 15 * 60 * 1000L;

	private static String[] lowercaseSortedArray(String[] headerNames) {
		String[] sortedHeaderNames = new String[headerNames.length];
		for ( int i = 0; i < headerNames.length; i++ ) {
			sortedHeaderNames[i] = headerNames[i].toLowerCase();
		}
		Arrays.sort(sortedHeaderNames);
		return sortedHeaderNames;
	}

	private static void appendHeaders(HttpServletRequest request, String[] headerNames,
			StringBuilder buf) {
		for ( String headerName : lowercaseSortedArray(headerNames) ) {
			buf.append(headerName).append(':').append(nullSafeHeaderValue(request, headerName).trim())
					.append('\n');
		}
	}

	private static void appendQueryParameters(HttpServletRequest request, StringBuilder buf) {
		Set<String> paramKeys = request.getParameterMap().keySet();
		if ( paramKeys.size() < 1 ) {
			buf.append('\n');
			return;
		}
		String[] keys = paramKeys.toArray(new String[paramKeys.size()]);
		Arrays.sort(keys);
		boolean first = true;
		for ( String key : keys ) {
			if ( first ) {
				first = false;
			} else {
				buf.append('&');
			}
			buf.append(uriEncode(key)).append('=').append(uriEncode(request.getParameter(key)));
		}
		buf.append('\n');
	}

	private static void appendSignedHeaderNames(String[] headerNames, StringBuilder buf) {
		boolean first = true;
		for ( String headerName : lowercaseSortedArray(headerNames) ) {
			if ( first ) {
				first = false;
			} else {
				buf.append(';');
			}
			buf.append(headerName);
		}
		buf.append('\n');
	}

	private static void appendContentSHA256(SecurityHttpServletRequestWrapper request, StringBuilder buf)
			throws IOException {
		byte[] digest = request.getContentSHA256();
		buf.append(digest == null ? WebConstants.EMPTY_STRING_SHA256_HEX : Hex.encodeHexString(digest));
	}

	private static String computeCanonicalRequestData(SecurityHttpServletRequestWrapper request,
			String[] headerNames) throws IOException {
		// 1: HTTP verb
		StringBuilder buf = new StringBuilder(request.getMethod()).append('\n');

		// 2: Canonical URI
		buf.append(request.getRequestURI()).append('\n');

		// 3: Canonical query string
		appendQueryParameters(request, buf);

		// 4: Canonical headers
		appendHeaders(request, headerNames, buf);

		// 5: Signed headers
		appendSignedHeaderNames(headerNames, buf);

		// 6: Content SHA256
		appendContentSHA256(request, buf);

		return buf.toString();

	}

	private static byte[] computeSigningKey(String secretKey, Date date) {
		/*- signing key is like:
		 
		HMACSHA256(HMACSHA256("SNWS2"+secretKey, "20160301"), "snws2_request")
		*/
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		return computeHMACSHA256(
				computeHMACSHA256(AuthenticationScheme.V2.getSchemeName() + secretKey, sdf.format(date)),
				"snws2_request");
	}

	private static String computeSignatureData(String canonicalRequestData, Date date) {
		/*- signature data is like:
		 
		 	SNWS2-HMAC-SHA256\n
		 	20170301T120000Z\n
		 	Hex(SHA256(canonicalRequestData))
		*/
		return "SNWS2-HMAC-SHA256\n" + iso8601Date(date) + "\n"
				+ Hex.encodeHexString(DigestUtils.sha256(canonicalRequestData));
	}

	/**
	 * Create an {@code Authorization} HTTP header using the
	 * {@link AuthenticationScheme#V2} scheme and no body content.
	 * 
	 * @param authTokenId
	 *        The auth token ID.
	 * @param authTokenSecret
	 *        The auth token secret.
	 * @param request
	 *        The HTTP request.
	 * @param date
	 *        The date to use.
	 * @return The {@code Authorization} HTTP header value.
	 * @throws IOException
	 *         If an IO error occurs.
	 */
	static String createAuthorizationHeaderV2Value(String token, String secret,
			MockHttpServletRequest request, Date date) throws IOException {
		return createAuthorizationHeaderV2Value(token, secret, request, date, null);
	}

	/**
	 * Create an {@code Authorization} HTTP header using the
	 * {@link AuthenticationScheme#V2} scheme.
	 * 
	 * @param authTokenId
	 *        The auth token ID.
	 * @param authTokenSecret
	 *        The auth token secret.
	 * @param request
	 *        The HTTP request.
	 * @param date
	 *        The date to use.
	 * @param contentType
	 *        The content type.
	 * @return The {@code Authorization} HTTP header value.
	 * @throws IOException
	 *         If an IO error occurs.
	 */
	static String createAuthorizationHeaderV2Value(String authTokenId, String authTokenSecret,
			MockHttpServletRequest request, Date date, String contentType) throws IOException {
		return createAuthorizationHeaderV2Value(authTokenId, authTokenSecret, request, date, date,
				contentType);
	}

	/**
	 * Create an {@code Authorization} HTTP header using the
	 * {@link AuthenticationScheme#V2} scheme.
	 * 
	 * @param authTokenId
	 *        The auth token ID.
	 * @param authTokenSecret
	 *        The auth token secret.
	 * @param request
	 *        The HTTP request.
	 * @param date
	 *        The request date to use.
	 * @param signDate
	 *        The signing date to use. If not provided, the {@code date} value
	 *        will be used.
	 * @param contentType
	 *        The content type.
	 * @return The {@code Authorization} HTTP header value.
	 * @throws IOException
	 *         If an IO error occurs.
	 */
	static String createAuthorizationHeaderV2Value(String authTokenId, String authTokenSecret,
			MockHttpServletRequest request, Date date, Date signDate, String contentType)
			throws IOException {
		if ( request.getHeader(HTTP_HEADER_HOST) == null ) {
			request.addHeader(HTTP_HEADER_HOST, TEST_HOST);
		}
		List<String> headerNames = new ArrayList<String>(3);
		headerNames.add("Host");
		if ( request.getHeader("X-SN-Date") != null ) {
			headerNames.add("X-SN-Date");
		} else if ( request.getHeader("Date") != null ) {
			headerNames.add("Date");
		}
		if ( request.getHeader("Content-MD5") != null ) {
			headerNames.add("Content-MD5");
		}
		if ( request.getHeader("Content-Type") != null ) {
			headerNames.add("Content-Type");
		}
		if ( request.getHeader("Digest") != null ) {
			headerNames.add("Digest");
		}
		final String[] sortedHeaderNames = lowercaseSortedArray(
				headerNames.toArray(new String[headerNames.size()]));
		final SecurityHttpServletRequestWrapper secRequest = new SecurityHttpServletRequestWrapper(
				request, 1024);
		final byte[] signingKey = computeSigningKey(authTokenSecret, signDate != null ? signDate : date);
		final String signatureData = computeSignatureData(computeCanonicalRequestData(secRequest,
				headerNames.toArray(new String[headerNames.size()])), date);
		final String signature = Hex.encodeHexString(computeHMACSHA256(signingKey, signatureData));
		final StringBuilder buf = new StringBuilder(AuthenticationScheme.V2.getSchemeName());
		buf.append(' ');
		buf.append("Credential=").append(authTokenId);
		buf.append(",SignedHeaders=").append(StringUtils.arrayToDelimitedString(sortedHeaderNames, ";"));
		buf.append(",Signature=").append(signature);
		return buf.toString();
	}

	private AuthenticationDataV2 verifyRequest(HttpServletRequest request, String secretKey)
			throws IOException {
		AuthenticationDataV2 authData = new AuthenticationDataV2(
				new SecurityHttpServletRequestWrapper(request, 1024), request.getHeader(HTTP_HEADER_AUTH)
						.substring(AuthenticationScheme.V2.getSchemeName().length() + 1));
		Assert.assertTrue("The date skew is OK", authData.isDateValid(TEST_MAX_DATE_SKEW));
		String computedDigest = authData.computeSignatureDigest(secretKey);
		Assert.assertEquals(computedDigest, authData.getSignatureDigest());
		return authData;
	}

	private AuthenticationDataV2 verifyRequest(HttpServletRequest request, String secretKey,
			Date signDate, String expectedDigest) throws IOException {
		AuthenticationDataV2 authData = new AuthenticationDataV2(
				new SecurityHttpServletRequestWrapper(request, 1024), request.getHeader(HTTP_HEADER_AUTH)
						.substring(AuthenticationScheme.V2.getSchemeName().length() + 1));
		String computedDigest = authData.computeSignatureDigest(secretKey, signDate);
		Assert.assertEquals(expectedDigest, computedDigest);
		return authData;
	}

	@Test
	public void missingDate() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/mock/path/here");
		final String authHeader = createAuthorizationHeaderV2Value(TEST_AUTH_TOKEN, TEST_PASSWORD,
				request, new Date());
		try {
			new AuthenticationDataV2(new SecurityHttpServletRequestWrapper(request, 1024), authHeader);
			Assert.fail("Should have thrown BadCredentialsException");
		} catch ( BadCredentialsException e ) {
			Assert.assertEquals("Missing or invalid HTTP Date header value", e.getMessage());
		}
	}

	@Test
	public void badPassword() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/mock/path/here");
		final Date now = new Date();
		request.addHeader("Date", httpDate(now));
		String authHeader = createAuthorizationHeaderV2Value(TEST_AUTH_TOKEN, "foobar", request, now);
		request.addHeader(HTTP_HEADER_AUTH, authHeader);
		AuthenticationDataV2 authData = new AuthenticationDataV2(
				new SecurityHttpServletRequestWrapper(request, 1024),
				authHeader.substring(AuthenticationScheme.V2.getSchemeName().length() + 1));
		Assert.assertTrue("The date skew is OK", authData.isDateValid(TEST_MAX_DATE_SKEW));
		String computedDigest = authData.computeSignatureDigest(TEST_PASSWORD);
		Assert.assertNotEquals(computedDigest, authData.getSignatureDigest());
	}

	@Test
	public void tooMuchSkew() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/mock/path/here");
		final Date now = new Date(System.currentTimeMillis() - 16L * 60L * 1000L);
		request.addHeader("Date", httpDate(now));
		String authHeader = createAuthorizationHeaderV2Value(TEST_AUTH_TOKEN, TEST_PASSWORD, request,
				now);
		request.addHeader(HTTP_HEADER_AUTH, authHeader);
		AuthenticationDataV2 authData = new AuthenticationDataV2(
				new SecurityHttpServletRequestWrapper(request, 1024),
				authHeader.substring(AuthenticationScheme.V2.getSchemeName().length() + 1));
		Assert.assertFalse("The date skew is too large.", authData.isDateValid(TEST_MAX_DATE_SKEW));
	}

	@Test
	public void simplePath() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/mock/path/here");
		final Date now = new Date();
		request.addHeader("Date", httpDate(now));
		String authHeader = createAuthorizationHeaderV2Value(TEST_AUTH_TOKEN, TEST_PASSWORD, request,
				now);
		request.addHeader(HTTP_HEADER_AUTH, authHeader);
		verifyRequest(request, TEST_PASSWORD);

		final AuthorizationV2Builder builder = new AuthorizationV2Builder(TEST_AUTH_TOKEN);
		String builderAuthHeader = builder.date(now).host(TEST_HOST).path(request.getRequestURI())
				.build(TEST_PASSWORD);
		Assert.assertEquals("Builder header equal to manual header", authHeader, builderAuthHeader);
	}

	@Test
	public void simplePathNonStandardPort() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/mock/path/here");
		request.addHeader("Host", "localhost:8080");
		final Date now = new Date(1502340082000L); // Thu, 10 Aug 2017 04:41:22 GMT
		request.addHeader("X-SN-Date", httpDate(now));
		String authHeader = createAuthorizationHeaderV2Value(TEST_AUTH_TOKEN, TEST_PASSWORD, request,
				now);
		request.addHeader(HTTP_HEADER_AUTH, authHeader);
		verifyRequest(request, TEST_PASSWORD, now,
				"0574baabdb8c97c0961d04f1a3e9a328dde3219b3898982af18b0c9675463fb1");
	}

	@Test
	public void simplePathHttpsThroughProxy() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/mock/path/here");
		request.addHeader("Host", "localhost");
		request.addHeader("X-Forwarded-Proto", "https");
		request.addHeader("X-Forwarded-Port", "443");
		final Date now = new Date(1502340082000L); // Thu, 10 Aug 2017 04:41:22 GMT
		request.addHeader("X-SN-Date", httpDate(now));
		String authHeader = createAuthorizationHeaderV2Value(TEST_AUTH_TOKEN, TEST_PASSWORD, request,
				now);
		request.addHeader(HTTP_HEADER_AUTH, authHeader);
		verifyRequest(request, TEST_PASSWORD, now,
				"85028cb8b15bc3a7557aa9607c419697e0a942a95511728318e290c1d27ffd41");
	}

	@Test
	public void simplePathHttpsNonStandardPortThroughProxy() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/mock/path/here");
		request.addHeader("Host", "localhost");
		request.addHeader("X-Forwarded-Proto", "https");
		request.addHeader("X-Forwarded-Port", "8443");
		final Date now = new Date(1502340082000L); // Thu, 10 Aug 2017 04:41:22 GMT
		request.addHeader("X-SN-Date", httpDate(now));
		String authHeader = createAuthorizationHeaderV2Value(TEST_AUTH_TOKEN, TEST_PASSWORD, request,
				now);
		request.addHeader(HTTP_HEADER_AUTH, authHeader);
		verifyRequest(request, TEST_PASSWORD, now,
				"409bc3bd69fd95d6b67fe7cde94ae412221d61a92ee52dc3bfc82ae535f9f8a9");
	}

	@Test
	public void simplePathHttpThroughProxy() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/mock/path/here");
		request.addHeader("Host", "localhost");
		request.addHeader("X-Forwarded-Proto", "http");
		request.addHeader("X-Forwarded-Port", "80");
		final Date now = new Date(1502340082000L); // Thu, 10 Aug 2017 04:41:22 GMT
		request.addHeader("X-SN-Date", httpDate(now));
		String authHeader = createAuthorizationHeaderV2Value(TEST_AUTH_TOKEN, TEST_PASSWORD, request,
				now);
		request.addHeader(HTTP_HEADER_AUTH, authHeader);
		verifyRequest(request, TEST_PASSWORD, now,
				"9584e63cd1c3e8e6fd007b982f99ffb63a6feb8a9db06bd3f4879bf7867a290f");
	}

	@Test
	public void simplePathHttpNonStandardPortThroughProxy() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/mock/path/here");
		request.addHeader("Host", "localhost");
		request.addHeader("X-Forwarded-Proto", "http");
		request.addHeader("X-Forwarded-Port", "8080");
		final Date now = new Date(1502340082000L); // Thu, 10 Aug 2017 04:41:22 GMT
		request.addHeader("X-SN-Date", httpDate(now));
		String authHeader = createAuthorizationHeaderV2Value(TEST_AUTH_TOKEN, TEST_PASSWORD, request,
				now);
		request.addHeader(HTTP_HEADER_AUTH, authHeader);
		verifyRequest(request, TEST_PASSWORD, now,
				"0574baabdb8c97c0961d04f1a3e9a328dde3219b3898982af18b0c9675463fb1");
	}

	@Test
	public void simplePathSignedAcrossSevenDays() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/mock/path/here");
		final Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		final Date now = cal.getTime();
		request.addHeader("Date", httpDate(now));

		final AuthorizationV2Builder builder = new AuthorizationV2Builder(TEST_AUTH_TOKEN);

		for ( int i = 0; i < 7; i++, cal.add(Calendar.DATE, -1) ) {
			String authHeader = createAuthorizationHeaderV2Value(TEST_AUTH_TOKEN, TEST_PASSWORD, request,
					now, cal.getTime(), null);

			String builderAuthHeader = builder.reset().date(cal.getTime()).saveSigningKey(TEST_PASSWORD)
					.date(now).host(TEST_HOST).path(request.getRequestURI()).build();
			Assert.assertEquals("Builder header equal to manual header", authHeader, builderAuthHeader);

			request.addHeader(HTTP_HEADER_AUTH, authHeader);
			verifyRequest(request, TEST_PASSWORD);
		}
	}

	@Test
	public void simplePathSignedMoreThanSevenDays() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/mock/path/here");
		final Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		final Date now = cal.getTime();
		cal.add(Calendar.DATE, -8);
		request.addHeader("Date", httpDate(now));
		String authHeader = createAuthorizationHeaderV2Value(TEST_AUTH_TOKEN, TEST_PASSWORD, request,
				now, cal.getTime(), null);
		request.addHeader(HTTP_HEADER_AUTH, authHeader);
		AuthenticationDataV2 authData = new AuthenticationDataV2(
				new SecurityHttpServletRequestWrapper(request, 1024),
				authHeader.substring(AuthenticationScheme.V2.getSchemeName().length() + 1));
		Assert.assertTrue("The date skew is OK", authData.isDateValid(TEST_MAX_DATE_SKEW));
		String computedDigest = authData.computeSignatureDigest(TEST_PASSWORD);
		Assert.assertNotEquals(computedDigest, authData.getSignatureDigest());
	}

	@Test
	public void simplePathWithXDate() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/mock/path/here");
		final Date now = new Date();
		request.addHeader("X-SN-Date", httpDate(now));
		String authHeader = createAuthorizationHeaderV2Value(TEST_AUTH_TOKEN, TEST_PASSWORD, request,
				now);
		request.addHeader(HTTP_HEADER_AUTH, authHeader);
		verifyRequest(request, TEST_PASSWORD);

		final AuthorizationV2Builder builder = new AuthorizationV2Builder(TEST_AUTH_TOKEN);
		String builderAuthHeader = builder.date(now).host(TEST_HOST).path(request.getRequestURI())
				.header(WebConstants.HEADER_DATE, httpDate(now)).build(TEST_PASSWORD);
		Assert.assertEquals("Builder header equal to manual header", authHeader, builderAuthHeader);
	}

	@Test
	public void pathWithQueryParams() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/mock/path/here");
		Map<String, String> params = new LinkedHashMap<String, String>();
		params.put("foo", "bar");
		params.put("bar", "foo");
		params.put("zog", "dog");
		request.setParameters(params);
		final Date now = new Date();
		request.addHeader("Date", httpDate(now));
		String authHeader = createAuthorizationHeaderV2Value(TEST_AUTH_TOKEN, TEST_PASSWORD, request,
				now);
		request.addHeader(HTTP_HEADER_AUTH, authHeader);
		verifyRequest(request, TEST_PASSWORD);

		final AuthorizationV2Builder builder = new AuthorizationV2Builder(TEST_AUTH_TOKEN);
		String builderAuthHeader = builder.date(now).host(TEST_HOST).path(request.getRequestURI())
				.queryParams(params).build(TEST_PASSWORD);
		Assert.assertEquals("Builder header equal to manual header", authHeader, builderAuthHeader);
	}

	@Test
	public void pathWithQueryParamsNeedingURIEscape() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/mock/path/here");
		Map<String, String> params = new LinkedHashMap<String, String>();
		params.put("/foo/bar", "bam!");
		params.put("bar.bim[1][blah]", "foo yes!");
		params.put("zog", "dog");
		request.setParameters(params);
		final Date now = new Date();
		request.addHeader("Date", httpDate(now));
		String authHeader = createAuthorizationHeaderV2Value(TEST_AUTH_TOKEN, TEST_PASSWORD, request,
				now);
		request.addHeader(HTTP_HEADER_AUTH, authHeader);
		verifyRequest(request, TEST_PASSWORD);

		final AuthorizationV2Builder builder = new AuthorizationV2Builder(TEST_AUTH_TOKEN);
		String builderAuthHeader = builder.date(now).host(TEST_HOST).path(request.getRequestURI())
				.queryParams(params).build(TEST_PASSWORD);
		Assert.assertEquals("Builder header equal to manual header", authHeader, builderAuthHeader);
	}

	@Test
	public void contentType() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/mock/path/here");
		request.setContentType("application/x-www-form-urlencoded; charset=UTF-8");
		Map<String, String> params = new LinkedHashMap<String, String>();
		params.put("foo", "bar");
		params.put("bar", "foo");
		params.put("zog", "dog");
		request.setParameters(params);
		final Date now = new Date();
		request.addHeader("Date", httpDate(now));
		String authHeader = createAuthorizationHeaderV2Value(TEST_AUTH_TOKEN, TEST_PASSWORD, request,
				now, "application/x-www-form-urlencoded; charset=UTF-8");
		request.addHeader(HTTP_HEADER_AUTH, authHeader);
		verifyRequest(request, TEST_PASSWORD);

		final AuthorizationV2Builder builder = new AuthorizationV2Builder(TEST_AUTH_TOKEN);
		String builderAuthHeader = builder.date(now).host(TEST_HOST).method(HttpMethod.POST)
				.path(request.getRequestURI())
				.header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
				.queryParams(params).build(TEST_PASSWORD);
		Assert.assertEquals("Builder header equal to manual header", authHeader, builderAuthHeader);
	}

	@Test
	public void contentMD5Hex() throws ServletException, IOException {
		final String contentType = "application/json; charset=UTF-8";
		final String content = "{\"foo\":\"bar\"}";
		final String contentMD5 = "9bb58f26192e4ba00f01e2e7b136bbd8";
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/mock/path/here");
		request.setContentType(contentType);
		request.setContent(content.getBytes("UTF-8"));
		request.addHeader("Content-MD5", contentMD5);
		final Date now = new Date();
		request.addHeader("Date", httpDate(now));
		String authHeader = createAuthorizationHeaderV2Value(TEST_AUTH_TOKEN, TEST_PASSWORD, request,
				now, contentType);
		request.addHeader(HTTP_HEADER_AUTH, authHeader);
		verifyRequest(request, TEST_PASSWORD);

		final AuthorizationV2Builder builder = new AuthorizationV2Builder(TEST_AUTH_TOKEN);
		String builderAuthHeader = builder.date(now).host(TEST_HOST).method(HttpMethod.POST)
				.path(request.getRequestURI()).header("Content-Type", contentType)
				.header("Content-MD5", contentMD5).contentSHA256(DigestUtils.sha256(content))
				.build(TEST_PASSWORD);
		Assert.assertEquals("Builder header equal to manual header", authHeader, builderAuthHeader);
	}

	@Test
	public void contentMD5Base64() throws ServletException, IOException {
		final String contentType = "application/json; charset=UTF-8";
		final String content = "{\"foo\":\"bar\"}";
		final String contentMD5 = "m7WPJhkuS6APAeLnsTa72A==";
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/mock/path/here");
		request.setContentType(contentType);
		request.setContent(content.getBytes("UTF-8"));
		request.addHeader("Content-MD5", contentMD5);
		final Date now = new Date();
		request.addHeader("Date", httpDate(now));
		String authHeader = createAuthorizationHeaderV2Value(TEST_AUTH_TOKEN, TEST_PASSWORD, request,
				now, contentType);
		request.addHeader(HTTP_HEADER_AUTH, authHeader);
		verifyRequest(request, TEST_PASSWORD);

		final AuthorizationV2Builder builder = new AuthorizationV2Builder(TEST_AUTH_TOKEN);
		String builderAuthHeader = builder.date(now).host(TEST_HOST).method(HttpMethod.POST)
				.path(request.getRequestURI()).header("Content-Type", contentType)
				.header("Content-MD5", contentMD5).contentSHA256(DigestUtils.sha256(content))
				.build(TEST_PASSWORD);
		Assert.assertEquals("Builder header equal to manual header", authHeader, builderAuthHeader);
	}
}

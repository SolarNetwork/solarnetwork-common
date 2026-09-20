/* ==================================================================
 * SignatureBaseTests.java - 19/09/2026 11:48:33 am
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

package net.solarnetwork.security.http.sig.test;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;
import java.util.Map;
import org.junit.Test;
import net.solarnetwork.security.http.sig.FieldCanonicalizer;
import net.solarnetwork.security.http.sig.FieldCanonicalizer.StructuredType;
import net.solarnetwork.security.http.sig.HttpSignatureBuilder;
import net.solarnetwork.security.http.sig.HttpSignatureException;
import net.solarnetwork.security.http.sig.SignatureBase;
import net.solarnetwork.security.http.sig.SignatureComponent;
import net.solarnetwork.security.http.sig.SignatureParameters;

/**
 * Test cases for the {@link SignatureBase} class, using the non-normative
 * examples from RFC 9421 sections 2.1 and 2.2.
 *
 * @author matt
 * @version 1.0
 */
public class SignatureBaseTests {

	private static HttpSignatureBuilder exampleRequest() {
		// the example message fragment from RFC 9421 section 2.1
		// @formatter:off
		return new HttpSignatureBuilder("test")
				.uri("https://www.example.com/")
				.header("Host", "www.example.com")
				.header("Date", "Tue, 20 Apr 2021 02:07:56 GMT")
				.header("X-OWS-Header", "  Leading and trailing whitespace.  ")
				.header("X-Obs-Fold-Header", "Obsolete\n    line folding.")
				.header("Cache-Control", "max-age=60", "   must-revalidate")
				.header("Example-Dict", " a=1,    b=2;x=1;y=2,   c=(a   b   c), d")
				.header("X-Empty-Header", "")
				;
		// @formatter:on
	}

	private static String baseLineFor(HttpSignatureBuilder request, SignatureComponent component,
			FieldCanonicalizer canonicalizer) {
		final SignatureParameters params = SignatureParameters.of(java.util.List.of(component),
				Map.of());
		return SignatureBase.compute(request, params, canonicalizer).lines().get(0);
	}

	@Test
	public void fieldCanonicalization_rfc9421_section21() {
		// GIVEN
		final HttpSignatureBuilder request = exampleRequest();
		final FieldCanonicalizer canonicalizer = new FieldCanonicalizer();

		// WHEN
		final String host = baseLineFor(request, SignatureComponent.of("host"), canonicalizer);
		final String date = baseLineFor(request, SignatureComponent.of("date"), canonicalizer);
		final String ows = baseLineFor(request, SignatureComponent.of("x-ows-header"), canonicalizer);
		final String obsFold = baseLineFor(request, SignatureComponent.of("x-obs-fold-header"),
				canonicalizer);
		final String cacheControl = baseLineFor(request, SignatureComponent.of("cache-control"),
				canonicalizer);
		final String dict = baseLineFor(request, SignatureComponent.of("example-dict"), canonicalizer);
		final String empty = baseLineFor(request, SignatureComponent.of("x-empty-header"),
				canonicalizer);

		// THEN
		// @formatter:off
		then(host)
			.as("A simple field value is used as-is")
			.isEqualTo("\"host\": www.example.com")
			;
		then(date)
			.as("A field value containing commas and spaces is used as-is")
			.isEqualTo("\"date\": Tue, 20 Apr 2021 02:07:56 GMT")
			;
		then(ows)
			.as("Leading and trailing whitespace is stripped")
			.isEqualTo("\"x-ows-header\": Leading and trailing whitespace.")
			;
		then(obsFold)
			.as("Obsolete line folding is replaced with a single space")
			.isEqualTo("\"x-obs-fold-header\": Obsolete line folding.")
			;
		then(cacheControl)
			.as("Multiple field instances are joined with a comma and a single space")
			.isEqualTo("\"cache-control\": max-age=60, must-revalidate")
			;
		then(dict)
			.as("Internal whitespace is preserved without the 'sf' parameter")
			.isEqualTo("\"example-dict\": a=1,    b=2;x=1;y=2,   c=(a   b   c), d")
			;
		then(empty)
			.as("An empty field value canonicalizes to an empty string")
			.isEqualTo("\"x-empty-header\": ")
			;
		// @formatter:on
	}

	@Test
	public void fieldCanonicalization_strictSerialization_rfc9421_section211() {
		// GIVEN
		final HttpSignatureBuilder request = exampleRequest();
		final FieldCanonicalizer canonicalizer = new FieldCanonicalizer()
				.withStructuredType("example-dict", StructuredType.Dictionary);
		final SignatureComponent component = SignatureComponent.of("example-dict",
				Map.of(SignatureComponent.PARAM_SF, Boolean.TRUE));

		// WHEN
		final String line = baseLineFor(request, component, canonicalizer);

		// THEN
		// @formatter:off
		then(line)
			.as("The 'sf' parameter re-serializes the value with strict serialization")
			.isEqualTo("\"example-dict\";sf: a=1, b=2;x=1;y=2, c=(a b c), d")
			;
		// @formatter:on
	}

	@Test
	public void fieldCanonicalization_dictionaryMembers_rfc9421_section212() {
		// GIVEN
		final HttpSignatureBuilder request = exampleRequest();
		final FieldCanonicalizer canonicalizer = new FieldCanonicalizer();

		// WHEN
		final String a = baseLineFor(request, dictKey("a"), canonicalizer);
		final String b = baseLineFor(request, dictKey("b"), canonicalizer);
		final String c = baseLineFor(request, dictKey("c"), canonicalizer);
		final String d = baseLineFor(request, dictKey("d"), canonicalizer);

		// THEN
		// @formatter:off
		then(a)
			.as("An Integer member is serialized as an Integer")
			.isEqualTo("\"example-dict\";key=\"a\": 1")
			;
		then(b)
			.as("Member parameters are included, but not the Dictionary key")
			.isEqualTo("\"example-dict\";key=\"b\": 2;x=1;y=2")
			;
		then(c)
			.as("An Inner List member is re-serialized strictly")
			.isEqualTo("\"example-dict\";key=\"c\": (a b c)")
			;
		then(d)
			.as("A member with no value is serialized as a Boolean")
			.isEqualTo("\"example-dict\";key=\"d\": ?1")
			;
		// @formatter:on
	}

	private static SignatureComponent dictKey(String key) {
		return SignatureComponent.of("example-dict", Map.of(SignatureComponent.PARAM_KEY, key));
	}

	@Test
	public void fieldCanonicalization_binaryWrapped_rfc9421_section213() {
		// GIVEN
		final FieldCanonicalizer canonicalizer = new FieldCanonicalizer();
		final SignatureComponent component = SignatureComponent.of("example-header",
				Map.of(SignatureComponent.PARAM_BS, Boolean.TRUE));
		final HttpSignatureBuilder multiple = new HttpSignatureBuilder("test")
				.uri("https://www.example.com/")
				.header("Example-Header", "value, with, lots", "of, commas");
		final HttpSignatureBuilder single = new HttpSignatureBuilder("test")
				.uri("https://www.example.com/")
				.header("Example-Header", "value, with, lots, of, commas");

		// WHEN
		final String multipleLine = baseLineFor(multiple, component, canonicalizer);
		final String singleLine = baseLineFor(single, component, canonicalizer);

		// THEN
		// @formatter:off
		then(multipleLine)
			.as("Each field instance is wrapped as a Byte Sequence")
			.isEqualTo("\"example-header\";bs: :dmFsdWUsIHdpdGgsIGxvdHM=:, :b2YsIGNvbW1hcw==:")
			;
		then(singleLine)
			.as("A single instance produces a distinct value, preventing a collision")
			.isEqualTo("\"example-header\";bs: :dmFsdWUsIHdpdGgsIGxvdHMsIG9mLCBjb21tYXM=:")
			;
		// @formatter:on
	}

	@Test
	public void derivedComponents_rfc9421_section22() {
		// GIVEN
		// @formatter:off
		final HttpSignatureBuilder request = new HttpSignatureBuilder("test")
				.method("POST")
				.uri("https://www.example.com/path?param=value&foo=bar&baz=bat%2Dman")
				;
		final SignatureParameters params = SignatureParameters.of(java.util.List.of(
				SignatureComponent.of(SignatureComponent.METHOD),
				SignatureComponent.of(SignatureComponent.TARGET_URI),
				SignatureComponent.of(SignatureComponent.AUTHORITY),
				SignatureComponent.of(SignatureComponent.SCHEME),
				SignatureComponent.of(SignatureComponent.REQUEST_TARGET),
				SignatureComponent.of(SignatureComponent.PATH),
				SignatureComponent.of(SignatureComponent.QUERY)), Map.of());
		// @formatter:on

		// WHEN
		final SignatureBase base = SignatureBase.compute(request, params, new FieldCanonicalizer());

		// THEN
		// @formatter:off
		then(base.lines().subList(0, 7))
			.as("Derived component values match the RFC 9421 section 2.2 examples")
			.containsExactly(
					"\"@method\": POST",
					"\"@target-uri\": https://www.example.com/path?param=value&foo=bar&baz=bat%2Dman",
					"\"@authority\": www.example.com",
					"\"@scheme\": https",
					"\"@request-target\": /path?param=value&foo=bar&baz=bat%2Dman",
					"\"@path\": /path",
					"\"@query\": ?param=value&foo=bar&baz=bat%2Dman")
			;
		// @formatter:on
	}

	@Test
	public void derivedComponents_emptyQuery() {
		// GIVEN
		final HttpSignatureBuilder request = new HttpSignatureBuilder("test")
				.uri("https://www.example.com");

		// WHEN
		final String query = baseLineFor(request, SignatureComponent.of(SignatureComponent.QUERY),
				new FieldCanonicalizer());
		final String path = baseLineFor(request, SignatureComponent.of(SignatureComponent.PATH),
				new FieldCanonicalizer());

		// THEN
		// @formatter:off
		then(query)
			.as("An absent query string canonicalizes to a lone question mark")
			.isEqualTo("\"@query\": ?")
			;
		then(path)
			.as("An empty path canonicalizes to a single slash")
			.isEqualTo("\"@path\": /")
			;
		// @formatter:on
	}

	@Test
	public void derivedComponents_authorityOmitsDefaultPort() {
		// GIVEN
		final HttpSignatureBuilder secure = new HttpSignatureBuilder("test")
				.uri("https://WWW.Example.COM:443/path");
		final HttpSignatureBuilder other = new HttpSignatureBuilder("test")
				.uri("https://www.example.com:8443/path");

		// WHEN
		final String secureLine = baseLineFor(secure,
				SignatureComponent.of(SignatureComponent.AUTHORITY), new FieldCanonicalizer());
		final String otherLine = baseLineFor(other, SignatureComponent.of(SignatureComponent.AUTHORITY),
				new FieldCanonicalizer());

		// THEN
		// @formatter:off
		then(secureLine)
			.as("The host is lower-cased and the default port omitted")
			.isEqualTo("\"@authority\": www.example.com")
			;
		then(otherLine)
			.as("A non-default port is included")
			.isEqualTo("\"@authority\": www.example.com:8443")
			;
		// @formatter:on
	}

	@Test
	public void queryParam_rfc9421_section228_encoding() {
		// GIVEN
		// @formatter:off
		final HttpSignatureBuilder request = new HttpSignatureBuilder("test")
				.uri("https://www.example.com/parameters?var=this%20is%20a%20big%0Amultiline%20value"
						+ "&bar=with+plus+whitespace&fa%C3%A7ade%22%3A%20=something")
				;
		// @formatter:on
		final FieldCanonicalizer canonicalizer = new FieldCanonicalizer();

		// WHEN
		final String var = baseLineFor(request, queryParam("var"), canonicalizer);
		final String bar = baseLineFor(request, queryParam("bar"), canonicalizer);
		final String facade = baseLineFor(request, queryParam("fa%C3%A7ade%22%3A%20"), canonicalizer);

		// THEN
		// @formatter:off
		then(var)
			.as("Problematic characters are percent-encoded")
			.isEqualTo("\"@query-param\";name=\"var\": this%20is%20a%20big%0Amultiline%20value")
			;
		then(bar)
			.as("A plus-encoded space is re-encoded as %20")
			.isEqualTo("\"@query-param\";name=\"bar\": with%20plus%20whitespace")
			;
		then(facade)
			.as("A percent-encoded parameter name is matched after decoding")
			.isEqualTo("\"@query-param\";name=\"fa%C3%A7ade%22%3A%20\": something")
			;
		// @formatter:on
	}

	@Test
	public void queryParam_emptyValue() {
		// GIVEN
		final HttpSignatureBuilder request = new HttpSignatureBuilder("test")
				.uri("https://www.example.com/path?param=value&foo=bar&baz=batman&qux=");

		// WHEN
		final String qux = baseLineFor(request, queryParam("qux"), new FieldCanonicalizer());
		final String baz = baseLineFor(request, queryParam("baz"), new FieldCanonicalizer());

		// THEN
		// @formatter:off
		then(qux)
			.as("An empty parameter value canonicalizes to an empty string")
			.isEqualTo("\"@query-param\";name=\"qux\": ")
			;
		then(baz)
			.as("A simple parameter value is used as-is")
			.isEqualTo("\"@query-param\";name=\"baz\": batman")
			;
		// @formatter:on
	}

	private static SignatureComponent queryParam(String name) {
		return SignatureComponent.of(SignatureComponent.QUERY_PARAM,
				Map.of(SignatureComponent.PARAM_NAME, name));
	}

	@Test
	public void error_missingField() {
		// GIVEN
		final HttpSignatureBuilder request = exampleRequest();

		// WHEN
		// THEN
		// @formatter:off
		thenThrownBy(() -> baseLineFor(request, SignatureComponent.of("x-not-here"),
				new FieldCanonicalizer()))
			.as("A covered field that is not in the request is an error")
			.isInstanceOf(HttpSignatureException.class)
			.hasMessageContaining("x-not-here")
			;
		// @formatter:on
	}

	@Test
	public void error_trailerParameterRejected() {
		// GIVEN
		final HttpSignatureBuilder request = exampleRequest();
		final SignatureComponent component = SignatureComponent.of("date",
				Map.of(SignatureComponent.PARAM_TR, Boolean.TRUE));

		// WHEN
		// THEN
		// @formatter:off
		thenThrownBy(() -> baseLineFor(request, component, new FieldCanonicalizer()))
			.as("Trailer fields are not supported")
			.isInstanceOf(HttpSignatureException.class)
			.hasMessageContaining("trailer")
			;
		// @formatter:on
	}

	@Test
	public void error_requestParameterRejected() {
		// GIVEN
		final HttpSignatureBuilder request = exampleRequest();
		final SignatureComponent component = SignatureComponent.of("date",
				Map.of(SignatureComponent.PARAM_REQ, Boolean.TRUE));

		// WHEN
		// THEN
		// @formatter:off
		thenThrownBy(() -> baseLineFor(request, component, new FieldCanonicalizer()))
			.as("The 'req' parameter is not valid for a request signature")
			.isInstanceOf(HttpSignatureException.class)
			.hasMessageContaining("'req'")
			;
		// @formatter:on
	}

	@Test
	public void error_statusComponentRejected() {
		// GIVEN
		final HttpSignatureBuilder request = exampleRequest();

		// WHEN
		// THEN
		// @formatter:off
		thenThrownBy(() -> baseLineFor(request, SignatureComponent.of(SignatureComponent.STATUS),
				new FieldCanonicalizer()))
			.as("A response-only derived component is an error on a request")
			.isInstanceOf(HttpSignatureException.class)
			.hasMessageContaining("response")
			;
		// @formatter:on
	}

	@Test
	public void error_unknownDerivedComponent() {
		// GIVEN
		final HttpSignatureBuilder request = exampleRequest();

		// WHEN
		// THEN
		// @formatter:off
		thenThrownBy(() -> baseLineFor(request, SignatureComponent.of("@nope"),
				new FieldCanonicalizer()))
			.as("An unknown derived component is an error")
			.isInstanceOf(HttpSignatureException.class)
			.hasMessageContaining("not supported")
			;
		// @formatter:on
	}

	@Test
	public void error_duplicateComponent() {
		// GIVEN
		final HttpSignatureBuilder request = exampleRequest();
		final SignatureParameters params = SignatureParameters.of(
				java.util.List.of(SignatureComponent.of("date"), SignatureComponent.of("date")),
				Map.of());

		// WHEN
		// THEN
		// @formatter:off
		thenThrownBy(() -> SignatureBase.compute(request, params, new FieldCanonicalizer()))
			.as("A component covered more than once is an error")
			.isInstanceOf(HttpSignatureException.class)
			.hasMessageContaining("more than once")
			;
		// @formatter:on
	}

	@Test
	public void error_binaryWrappedWithStrictSerialization() {
		// GIVEN
		final HttpSignatureBuilder request = exampleRequest();
		final SignatureComponent component = SignatureComponent.of("example-dict", Map.of(
				SignatureComponent.PARAM_BS, Boolean.TRUE, SignatureComponent.PARAM_SF, Boolean.TRUE));

		// WHEN
		// THEN
		// @formatter:off
		thenThrownBy(() -> baseLineFor(request, component, new FieldCanonicalizer()))
			.as("The 'bs' parameter cannot be combined with 'sf'")
			.isInstanceOf(HttpSignatureException.class)
			.hasMessageContaining("'bs'")
			;
		// @formatter:on
	}

	@Test
	public void error_missingDictionaryMember() {
		// GIVEN
		final HttpSignatureBuilder request = exampleRequest();

		// WHEN
		// THEN
		// @formatter:off
		thenThrownBy(() -> baseLineFor(request, dictKey("nope"), new FieldCanonicalizer()))
			.as("A covered Dictionary member that is not present is an error")
			.isInstanceOf(HttpSignatureException.class)
			.hasMessageContaining("nope")
			;
		// @formatter:on
	}

	@Test
	public void error_missingQueryParam() {
		// GIVEN
		final HttpSignatureBuilder request = new HttpSignatureBuilder("test")
				.uri("https://www.example.com/path?param=value");

		// WHEN
		// THEN
		// @formatter:off
		thenThrownBy(() -> baseLineFor(request, queryParam("nope"), new FieldCanonicalizer()))
			.as("A covered query parameter that is not present is an error")
			.isInstanceOf(HttpSignatureException.class)
			.hasMessageContaining("not present")
			;
		// @formatter:on
	}

	@Test
	public void error_repeatedQueryParam() {
		// GIVEN
		final HttpSignatureBuilder request = new HttpSignatureBuilder("test")
				.uri("https://www.example.com/path?a=1&a=2");

		// WHEN
		// THEN
		// @formatter:off
		thenThrownBy(() -> baseLineFor(request, queryParam("a"), new FieldCanonicalizer()))
			.as("A query parameter that occurs more than once must not be covered this way")
			.isInstanceOf(HttpSignatureException.class)
			.hasMessageContaining("more than once")
			;
		// @formatter:on
	}

}

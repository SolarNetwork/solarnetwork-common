/* ==================================================================
 * MapPathMatcherTests.java - 27/11/2020 10:44:14 am
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

package net.solarnetwork.support.test;

import static net.solarnetwork.support.MapPathMatcher.matches;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;
import org.springframework.util.FileCopyUtils;
import net.solarnetwork.codec.JsonUtils;
import net.solarnetwork.support.MapPathMatcher;

/**
 * Test cases for the {@link MapPathMatcher} class.
 * 
 * @author matt
 * @version 1.2
 */
public class MapPathMatcherTests {

	@Test
	public void noMatchEmptyObj() {
		// GIVEN
		Map<String, Object> obj = new LinkedHashMap<>();

		// THEN
		assertThat("Filter does not match", matches(obj, "(/foo=bar)"), equalTo(false));
	}

	@Test
	public void simpleMatch() {
		// GIVEN
		Map<String, ?> obj = JsonUtils.getStringMap("{\"foo\":\"bar\"}");

		// THEN
		assertThat("Filter matches", matches(obj, "(/foo=bar)"), equalTo(true));
	}

	@Test
	public void simpleMatch_not() {
		// GIVEN
		Map<String, ?> obj = JsonUtils.getStringMap("{\"foo\":\"bar\"}");

		// THEN
		assertThat("Filter matches", matches(obj, "(!(/foo=bar))"), equalTo(false));
	}

	@Test
	public void simpleNoMatchingProp() {
		// GIVEN
		Map<String, ?> obj = JsonUtils.getStringMap("{\"foo\":\"bar\"}");

		// THEN
		assertThat("Filter does not match", matches(obj, "(/bar=bam)"), equalTo(false));
	}

	@Test
	public void simpleNoMatchingProp_not() {
		// GIVEN
		Map<String, ?> obj = JsonUtils.getStringMap("{\"foo\":\"bar\"}");

		// THEN
		assertThat("Filter does match", matches(obj, "(!(/bar=bam))"), equalTo(true));
	}

	@Test
	public void simpleNoMatchingVal() {
		// GIVEN
		Map<String, ?> obj = JsonUtils.getStringMap("{\"foo\":\"bar\"}");

		// THEN
		assertThat("Filter does not match", matches(obj, "(/foo=no)"), equalTo(false));
	}

	@Test
	public void nestedNoMatchingProp() {
		// GIVEN
		Map<String, ?> obj = JsonUtils.getStringMap("{\"foo\":{\"bim\":{\"bam\":\"baz\"}}}");

		// THEN
		assertThat("Filter does not match", matches(obj, "(/foo/bim/no=bam)"), equalTo(false));
	}

	@Test
	public void singleAndMatch() {
		// GIVEN
		Map<String, ?> obj = JsonUtils.getStringMap("{\"foo\":\"bar\"}");

		// THEN
		assertThat("Filter matches", matches(obj, "(&(/foo=bar))"), equalTo(true));
	}

	@Test
	public void multiAndMatch() {
		// GIVEN
		Map<String, ?> obj = JsonUtils.getStringMap("{\"foo\":\"bar\", \"ping\":\"pong\"}");

		// THEN
		assertThat("Filter matches", matches(obj, "(&(/foo=bar)(ping=pong)"), equalTo(true));
	}

	@Test
	public void multiAndNoMatch() {
		// GIVEN
		Map<String, ?> obj = JsonUtils.getStringMap("{\"foo\":\"bar\", \"ping\":\"pong\"}");

		// THEN
		assertThat("Filter does not match", matches(obj, "(&(/foo=NO)(ping=pong)"), equalTo(false));
		assertThat("Filter does not match", matches(obj, "(&(/foo=bar)(ping=NO)"), equalTo(false));
	}

	@Test
	public void nestedPathMultiAndMatch() {
		// GIVEN
		Map<String, ?> obj = JsonUtils.getStringMap("{\"foo\":{\"ping\":\"pong\"}, \"bam\":\"mab\"}");

		// THEN
		assertThat("Filter matches", matches(obj, "(&(/foo/ping=pong)(/bam=mab)"), equalTo(true));
	}

	@Test
	public void nestedPathMultiAndMatch_not() {
		// GIVEN
		Map<String, ?> obj = JsonUtils.getStringMap("{\"foo\":{\"ping\":\"pong\"}, \"bam\":\"mab\"}");

		// THEN
		assertThat("Filter matches", matches(obj, "(!(&(/foo/ping=pong)(/bam=mab))"), equalTo(false));
	}

	@Test
	public void nestedPathMultiAndNoMatch() {
		// GIVEN
		Map<String, ?> obj = JsonUtils.getStringMap("{\"foo\":{\"ping\":\"pong\"}, \"bam\":\"mab\"}");

		// THEN
		assertThat("Filter does not match", matches(obj, "(&(/foo/ping=pong)(/bam=NO)"), equalTo(false));
		assertThat("Filter does not match", matches(obj, "(&(/foo/ping=NO)(/bam=mab)"), equalTo(false));
	}

	@Test
	public void nestedPathMultiAndNoMatch_not() {
		// GIVEN
		Map<String, ?> obj = JsonUtils.getStringMap("{\"foo\":{\"ping\":\"pong\"}, \"bam\":\"mab\"}");

		// THEN
		assertThat("Filter does not match", matches(obj, "(!(&(/foo/ping=pong)(/bam=NO))"),
				equalTo(true));
		assertThat("Filter does not match", matches(obj, "(!(&(/foo/ping=NO)(/bam=mab))"),
				equalTo(true));
	}

	@Test
	public void wildPathMatch() {
		// GIVEN
		Map<String, ?> obj = JsonUtils
				.getStringMap("{\"foo\":{\"a\":\"boo\", \"b\":\"bar\", \"c\":\"nah\"}}");

		// THEN
		assertThat("Filter matches", matches(obj, "(/foo/*=boo)"), equalTo(true));
		assertThat("Filter matches", matches(obj, "(/foo/*=bar)"), equalTo(true));
		assertThat("Filter matches", matches(obj, "(/foo/*=nah)"), equalTo(true));
		assertThat("Filter does not match", matches(obj, "(/foo/*=NO)"), equalTo(false));
	}

	@Test
	public void wildPathMatch_not() {
		// GIVEN
		Map<String, ?> obj = JsonUtils
				.getStringMap("{\"foo\":{\"a\":\"boo\", \"b\":\"bar\", \"c\":\"nah\"}}");

		// THEN
		assertThat("Filter matches", matches(obj, "(!(/foo/*=boo))"), equalTo(false));
		assertThat("Filter matches", matches(obj, "(!(/foo/*=bar))"), equalTo(false));
		assertThat("Filter matches", matches(obj, "(!(/foo/*=nah))"), equalTo(false));
		assertThat("Filter does not match", matches(obj, "(!(/foo/*=NO))"), equalTo(true));
	}

	@Test
	public void wildPathMatchWithAnd() {
		// GIVEN
		Map<String, ?> obj = JsonUtils
				.getStringMap("{\"foo\":{\"a\":\"boo\", \"b\":\"bar\", \"c\":\"nah\"}}");

		// THEN
		assertThat("Filter matches", matches(obj, "(&(/foo/*=boo)(/foo/*=bar)(/foo/*=nah))"),
				equalTo(true));
		assertThat("Filter does not match", matches(obj, "(&(/foo/*=boo)(/foo/*=bar)(/foo/*=NO))"),
				equalTo(false));
	}

	@Test
	public void wildPathMatchWithOr() {
		// GIVEN
		Map<String, ?> obj = JsonUtils
				.getStringMap("{\"foo\":{\"a\":\"boo\", \"b\":\"bar\", \"c\":\"nah\"}}");

		// THEN
		assertThat("Filter matches", matches(obj, "(|(/foo/*=NO)(/foo/*=bar)(/foo/*=NOPE))"),
				equalTo(true));
		assertThat("Filter does not match", matches(obj, "(|(/foo/*=NO)(/foo/*=NADDA)(/foo/*=NOPE))"),
				equalTo(false));
	}

	@Test
	public void anyPathWildMatch() {
		// GIVEN
		Map<String, ?> obj = JsonUtils.getStringMap(
				"{\"foo\":{\"a\":{\"foo\":\"boo\"}, \"b\":{\"foo\":\"bar\"}, \"c\":{\"foo\":\"nah\"}}}");

		// THEN
		assertThat("Filter matches", matches(obj, "(/**/foo=boo)"), equalTo(true));
		assertThat("Filter matches", matches(obj, "(/**/foo=bar)"), equalTo(true));
		assertThat("Filter matches", matches(obj, "(/**/foo=nah)"), equalTo(true));
		assertThat("Filter does not match", matches(obj, "(/**/foo=NO)"), equalTo(false));
	}

	@Test
	public void anyPathWildMatch_not() {
		// GIVEN
		Map<String, ?> obj = JsonUtils.getStringMap(
				"{\"foo\":{\"a\":{\"foo\":\"boo\"}, \"b\":{\"foo\":\"bar\"}, \"c\":{\"foo\":\"nah\"}}}");

		// THEN
		assertThat("Filter matches", matches(obj, "(!(/**/foo=boo))"), equalTo(false));
		assertThat("Filter matches", matches(obj, "(!(/**/foo=bar))"), equalTo(false));
		assertThat("Filter matches", matches(obj, "(!(/**/foo=nah))"), equalTo(false));
		assertThat("Filter does not match", matches(obj, "(!(/**/foo=NO))"), equalTo(true));
	}

	@Test
	public void anyPathWildAndWildMatch() {
		// GIVEN
		Map<String, ?> obj = JsonUtils.getStringMap(
				"{\"foo\":{\"a\":{\"foo\":\"boo\"}, \"b\":{\"foo\":\"bar\"}, \"c\":{\"foo\":\"nah\"}}}");

		// THEN
		assertThat("Filter matches", matches(obj, "(/**/*=boo)"), equalTo(true));
		assertThat("Filter matches", matches(obj, "(/**/*=bar)"), equalTo(true));
		assertThat("Filter matches", matches(obj, "(/**/*=nah)"), equalTo(true));
		assertThat("Filter does not match", matches(obj, "(/**/*=NO)"), equalTo(false));
	}

	@Test
	public void anyPathWildSubpath() {
		// GIVEN
		Map<String, ?> obj = JsonUtils.getStringMap(
				"{\"foo\":{\"a\":{\"foo\":\"boo\"}, \"b\":{\"foo\":\"bar\"}, \"c\":{\"foo\":\"nah\"}}}");

		// THEN
		assertThat("Filter matches", matches(obj, "(/foo/**/*=boo)"), equalTo(true));
		assertThat("Filter matches", matches(obj, "(/foo/**/*=bar)"), equalTo(true));
		assertThat("Filter matches", matches(obj, "(/foo/**/*=nah)"), equalTo(true));
		assertThat("Filter does not match", matches(obj, "(/foo/**/*=NO)"), equalTo(false));
	}

	private Map<String, Object> jsonResource(String name) {
		try (InputStream in = getClass().getResourceAsStream(name)) {
			String json = FileCopyUtils
					.copyToString(new InputStreamReader(in, Charset.forName("UTF-8")));
			return JsonUtils.getStringMap(json);
		} catch ( IOException e ) {
			throw new RuntimeException("Error loading JSON resource [" + name + "]: " + e.toString());
		}
	}

	@Test
	public void anyPathWildSubpathLarge() {
		// GIVEN
		Map<String, ?> obj = jsonResource("meta-01.json");

		// THEN
		assertThat("Filter matches",
				matches(obj, "(/pm/esi-resource/**/characteristics/responseTime/maxMillis<70000)"),
				equalTo(true));
	}

	@Test
	public void andWithNestedOr() {
		// GIVEN
		Map<String, ?> obj = JsonUtils.getStringMap(
				"{\"boo\":\"ya\", \"foo\":{\"a\":{\"foo\":\"boo\", \"bim\":\"bam\"}, \"b\":{\"foo\":\"bar\"}, \"c\":{\"foo\":\"nah\"}}}");

		// THEN
		assertThat("Filter matches", matches(obj, "(&(|(/foo/a/bim=bam)(/boo=NO))(/foo/c/foo=nah))"),
				equalTo(true));
		assertThat("Filter matches", matches(obj, "(&(|(/foo/a/bim=NO)(/boo=ya))(/foo/c/foo=nah))"),
				equalTo(true));
		assertThat("Filter does not match",
				matches(obj, "(&(|(/foo/a/bim=NO)(/boo=NOPE))(/foo/c/foo=nah))"), equalTo(false));
		assertThat("Filter does not match",
				matches(obj, "(&(|(/foo/a/bim=bam)(/boo=NO))(/foo/c/foo=NO))"), equalTo(false));
	}

	@Test
	public void andWithNestedOr_not() {
		// GIVEN
		Map<String, ?> obj = JsonUtils.getStringMap(
				"{\"boo\":\"ya\", \"foo\":{\"a\":{\"foo\":\"boo\", \"bim\":\"bam\"}, \"b\":{\"foo\":\"bar\"}, \"c\":{\"foo\":\"nah\"}}}");

		// THEN
		assertThat("Filter does not match",
				matches(obj, "(&(!(|(/foo/a/bim=bam)(/boo=NO)))(/foo/c/foo=nah))"), equalTo(false));
		assertThat("Filter does not match",
				matches(obj, "(&(!(|(/foo/a/bim=NO)(/boo=ya)))(/foo/c/foo=nah))"), equalTo(false));
		assertThat("Filter does match",
				matches(obj, "(&(!(|(/foo/a/bim=NO)(/boo=NOPE)))(/foo/c/foo=nah))"), equalTo(true));
		assertThat("Filter does not match",
				matches(obj, "(&(|(/foo/a/bim=bam)(/boo=NO))(!(/foo/c/foo=NO)))"), equalTo(true));
	}

	@Test
	public void andWithNestedOrNestedAnd() {
		// GIVEN
		Map<String, ?> obj = JsonUtils.getStringMap(
				"{\"boo\":\"ya\", \"foo\":{\"a\":{\"foo\":\"boo\", \"bim\":\"bam\"}, \"b\":{\"foo\":\"bar\"}, \"c\":{\"foo\":\"nah\"}}}");

		// THEN
		assertThat("Filter matches",
				matches(obj, "(&(|(/foo/a/bim=NO)(&(/boo=ya)(/foo/a/foo=boo)))(/foo/c/foo=nah))"),
				equalTo(true));
		assertThat("Filter does not match",
				matches(obj, "(&(|(/foo/a/bim=NO)(&(/boo=ya)(/foo/a/foo=NO)))(/foo/c/foo=nah))"),
				equalTo(false));
		assertThat("Filter does not match",
				matches(obj, "(&(|(/foo/a/bim=NO)(&(/boo=ya)(/foo/a/foo=boo)))(/foo/c/foo=NO))"),
				equalTo(false));
	}

	@Test
	public void arrayMatchSingle() {
		// GIVEN
		Map<String, ?> obj = JsonUtils.getStringMap("{\"foo\":[\"one\",\"two\",\"three\"]}");

		// THEN
		assertThat("Filter matches", matches(obj, "(/foo=one)"), equalTo(true));
		assertThat("Filter matches", matches(obj, "(/foo=two)"), equalTo(true));
		assertThat("Filter matches", matches(obj, "(/foo=three)"), equalTo(true));
		assertThat("Filter does not match", matches(obj, "(/foo=NO)"), equalTo(false));
	}

	@Test
	public void arrayMatchNested() {
		// GIVEN
		Map<String, ?> obj = JsonUtils.getStringMap("{\"foo\":{\"bar\":[\"one\",\"two\",\"three\"]}}");

		// THEN
		assertThat("Filter matches", matches(obj, "(/foo/bar=one)"), equalTo(true));
		assertThat("Filter matches", matches(obj, "(/foo/bar=two)"), equalTo(true));
		assertThat("Filter matches", matches(obj, "(/foo/bar=three)"), equalTo(true));
		assertThat("Filter does not match", matches(obj, "(/foo/bar=NO)"), equalTo(false));
	}

	@Test
	public void arrayTryWalk() {
		// GIVEN
		Map<String, ?> obj = JsonUtils.getStringMap("{\"foo\":[\"one\",\"two\",\"three\"]}");

		// THEN
		assertThat("Filter does not match", matches(obj, "(/foo/bar=one)"), equalTo(false));
	}

	@Test
	public void arrayMatchAnd() {
		// GIVEN
		Map<String, ?> obj = JsonUtils.getStringMap("{\"foo\":[\"one\",\"two\",\"three\"]}");

		// THEN
		assertThat("Filter matches", matches(obj, "(&(/foo=one)(/foo=two))"), equalTo(true));
		assertThat("Filter matches", matches(obj, "(&(/foo=one)(/foo=two)(/foo=three))"), equalTo(true));
		assertThat("Filter does not match", matches(obj, "(&(/foo=one)(/foo=NO)(/foo=three))"),
				equalTo(false));
	}

	@Test
	public void arrayMatchOr() {
		// GIVEN
		Map<String, ?> obj = JsonUtils.getStringMap("{\"foo\":[\"one\",\"two\",\"three\"]}");

		// THEN
		assertThat("Filter matches", matches(obj, "(|(/foo=A)(/foo=two))"), equalTo(true));
		assertThat("Filter matches", matches(obj, "(|(/foo=A)(/foo=two)(/foo=three))"), equalTo(true));
		assertThat("Filter does not match", matches(obj, "(|(/foo=A)(/foo=B)(/foo=C))"), equalTo(false));
	}

	@Test
	public void arrayMatchNot() {
		// GIVEN
		Map<String, ?> obj = JsonUtils.getStringMap("{\"foo\":[\"one\",\"two\",\"three\"]}");

		// THEN
		assertThat("Filter matches", matches(obj, "(!(/foo=A))"), equalTo(true));
		assertThat("Filter does not matche", matches(obj, "(!(/foo=one))"), equalTo(false));
	}

	@Test
	public void regexMatch() {
		// GIVEN
		Map<String, ?> obj = JsonUtils.getStringMap("{\"foo\":\"bar\"}");

		// THEN
		assertThat("Filter matches", matches(obj, "(/foo~=ba.*)"), equalTo(true));
		assertThat("Filter does not match", matches(obj, "(/foo~=ba.*m)"), equalTo(false));
	}

	@Test
	public void regexMatchArray() {
		// GIVEN
		Map<String, ?> obj = JsonUtils.getStringMap("{\"foo\":[\"bar\",\"bam\",\"pow\"]}");

		// THEN
		assertThat("Filter matches", matches(obj, "(/foo~=ba.*)"), equalTo(true));
		assertThat("Filter matches", matches(obj, "(/foo~=^p.*w$)"), equalTo(true));
		assertThat("Filter does not match", matches(obj, "(/foo~=d.*)"), equalTo(false));
	}

	@Test
	public void regexMatchObject() {
		// GIVEN
		Map<String, ?> obj = JsonUtils.getStringMap("{\"foo\":{\"bar\":\"bam\"}}");

		// THEN
		assertThat("Filter matches", matches(obj, "(/foo~=.*)"), equalTo(true));
		assertThat("Filter matches", matches(obj, "(/foo~=.*bar.*)"), equalTo(true));
		assertThat("Filter matches", matches(obj, "(/foo~=.*bam.*)"), equalTo(true));
		assertThat("Filter does not match", matches(obj, "(/foo~=.*pow.*)"), equalTo(false));
	}

}

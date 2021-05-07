/* ==================================================================
 * GeneralNodeDatumSamplesTest.java - Aug 29, 2014 1:09:38 PM
 * 
 * Copyright 2007-2014 SolarNetwork.net Dev Team
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

package net.solarnetwork.domain.test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.solarnetwork.domain.GeneralDatumMetadata;

/**
 * Test cases for {@link GeneralDatumMetadata}.
 * 
 * @author matt
 * @version 1.1
 */
public class GeneralDatumMetadataTest {

	private ObjectMapper objectMapper;

	@Before
	public void setup() {
		objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(Include.NON_NULL);
		objectMapper.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);
	}

	private GeneralDatumMetadata getTestInstance() {
		GeneralDatumMetadata meta = new GeneralDatumMetadata();

		Map<String, Object> info = new HashMap<String, Object>(2);
		info.put("msg", "Hello, world.");
		meta.setInfo(info);

		meta.addTag("test");

		return meta;
	}

	@Test
	public void serializeJson() throws Exception {
		String json = objectMapper.writeValueAsString(getTestInstance());
		assertThat(json, equalTo("{\"m\":{\"msg\":\"Hello, world.\"},\"t\":[\"test\"]}"));
	}

	@Test
	public void serializeJsonWithPropertyMeta() throws Exception {
		GeneralDatumMetadata meta = getTestInstance();
		meta.putInfoValue("watts", "unit", "W");
		String json = objectMapper.writeValueAsString(meta);
		assertThat(json, equalTo(
				"{\"m\":{\"msg\":\"Hello, world.\"},\"pm\":{\"watts\":{\"unit\":\"W\"}},\"t\":[\"test\"]}"));
	}

	@Test
	public void deserializeJson() throws Exception {
		String json = "{\"m\":{\"ploc\":2502287},\"t\":[\"test\"]}";
		GeneralDatumMetadata samples = objectMapper.readValue(json, GeneralDatumMetadata.class);
		assertThat(samples, notNullValue());
		assertThat(samples.getInfoLong("ploc"), equalTo(2502287L));
		assertThat("Tag exists", samples.hasTag("test"), equalTo(true));
	}

	@Test
	public void deserializeJsonWithPropertyMeta() throws Exception {
		String json = "{\"m\":{\"ploc\":2502287},\"pm\":{\"watts\":{\"unit\":\"W\"}},\"t\":[\"test\"]}";
		GeneralDatumMetadata samples = objectMapper.readValue(json, GeneralDatumMetadata.class);
		assertThat(samples, notNullValue());
		assertThat(samples.getInfoLong("ploc"), equalTo(2502287L));
		assertThat(samples.getInfoString("watts", "unit"), equalTo("W"));
		assertThat("Tag exists", samples.hasTag("test"), equalTo(true));
	}

	@Test
	public void deserializeJsonWithNestedMeta() throws Exception {
		String json = "{\"m\":{\"map\":{\"foo\":1,\"bar\":\"bam\"}}}";
		GeneralDatumMetadata samples = objectMapper.readValue(json, GeneralDatumMetadata.class);
		Assert.assertNotNull(samples);
		Object map = samples.getInfo().get("map");
		assertThat("Nested map parsed", map, instanceOf(Map.class));
		@SuppressWarnings("unchecked")
		Map<String, ?> stringMap = (Map<String, ?>) map;
		assertThat("Nested number", stringMap.get("foo"), equalTo((Object) 1));
		assertThat("Nested string", stringMap.get("bar"), equalTo((Object) "bam"));
	}

	@Test
	public void removeInfoKey() {
		GeneralDatumMetadata meta = getTestInstance();
		meta.putInfoValue("msg", null);
		meta.putInfoValue("does.not.exist", null);
		meta.putInfoValue("foo", "bar", "bam");
		meta.putInfoValue("foo", "bar", null);
		assertThat(meta.getInfoString("msg"), nullValue());
		assertThat(meta.getInfoString("foo", "bar"), nullValue());
	}

	@Test
	public void copyConstructorInfo() {
		GeneralDatumMetadata meta = getTestInstance();
		GeneralDatumMetadata copy = new GeneralDatumMetadata(meta);
		assertThat(copy.getInfo(), notNullValue());
		assertThat(copy.getInfo(), equalTo(meta.getInfo()));
		assertThat(copy.getInfo(), not(sameInstance(meta.getInfo())));
	}

	@Test
	public void copyConstructorPropertyInfo() {
		GeneralDatumMetadata meta = getTestInstance();
		meta.putInfoValue("foo", "bar", "bam");
		GeneralDatumMetadata copy = new GeneralDatumMetadata(meta);
		assertThat(copy.getPropertyInfo(), notNullValue());
		assertThat(copy.getPropertyInfo(), equalTo(meta.getPropertyInfo()));
		assertThat(copy.getPropertyInfo(), not(sameInstance(meta.getPropertyInfo())));
		assertThat(copy.getPropertyInfo().get("foo"), equalTo(meta.getPropertyInfo().get("foo")));
		assertThat(copy.getPropertyInfo().get("foo"),
				not(sameInstance(meta.getPropertyInfo().get("foo"))));
	}

	@Test
	public void metadataAtPath_info() {
		// GIVEN
		GeneralDatumMetadata meta = getTestInstance();

		// WHEN
		String s = meta.metadataAtPath("/m/msg", String.class);

		// THEN
		assertThat("Resolved metadata", s, equalTo(meta.getInfoString("msg")));
	}

	@Test
	public void metadataAtPath_info_wrongType() {
		// GIVEN
		GeneralDatumMetadata meta = getTestInstance();

		// WHEN
		Integer i = meta.metadataAtPath("/m/msg", Integer.class);

		// THEN
		assertThat("Metadata of wrong type", i, nullValue());
	}

	@Test
	public void metadataAtPath_info_root() {
		// GIVEN
		GeneralDatumMetadata meta = getTestInstance();

		// WHEN
		@SuppressWarnings("unchecked")
		Map<String, Object> m = meta.metadataAtPath("/m", Map.class);

		// THEN
		assertThat("Resolved metadata", m, sameInstance(meta.getInfo()));
	}

	@Test
	public void metadataAtPath_info_notFound() {
		// GIVEN
		GeneralDatumMetadata meta = getTestInstance();

		// WHEN
		String s = meta.metadataAtPath("/m/msg_nope", String.class);

		// THEN
		assertThat("Metadata not found", s, nullValue());
	}

	@Test
	public void metadataAtPath_propInfo() {
		// GIVEN
		GeneralDatumMetadata meta = getTestInstance();
		meta.putInfoValue("foo", "bar", "bam");

		// WHEN
		String s = meta.metadataAtPath("/pm/foo/bar", String.class);

		// THEN
		assertThat("Resolved metadata", s, equalTo(meta.getInfoString("foo", "bar")));
	}

	@Test
	public void metadataAtPath_propInfo_wrongType() {
		// GIVEN
		GeneralDatumMetadata meta = getTestInstance();
		meta.putInfoValue("foo", "bar", "bam");

		// WHEN
		Integer i = meta.metadataAtPath("/pm/foo/bar", Integer.class);

		// THEN
		assertThat("Metadata of wrong type", i, nullValue());
	}

	@Test
	public void metadataAtPath_propInfo_root() {
		// GIVEN
		GeneralDatumMetadata meta = getTestInstance();
		meta.putInfoValue("foo", "bar", "bam");

		// WHEN
		@SuppressWarnings("unchecked")
		Map<String, Map<String, Object>> m = meta.metadataAtPath("/pm", Map.class);

		// THEN
		assertThat("Resolved metadata", m, sameInstance(meta.getPropertyInfo()));
	}

	@Test
	public void metadataAtPath_propInfo_nestedRoot() {
		// GIVEN
		GeneralDatumMetadata meta = getTestInstance();
		meta.putInfoValue("foo", "bar", "bam");

		// WHEN
		@SuppressWarnings("unchecked")
		Map<String, Object> m = meta.metadataAtPath("/pm/foo", Map.class);

		// THEN
		assertThat("Resolved metadata", m, sameInstance(meta.getPropertyInfo().get("foo")));
	}

	@Test
	public void metadataAtPath_propInfo_notFound() {
		// GIVEN
		GeneralDatumMetadata meta = getTestInstance();
		meta.putInfoValue("foo", "bar", "bam");

		// WHEN
		String s = meta.metadataAtPath("/m/foo/bar_nope", String.class);

		// THEN
		assertThat("Metadata not found", s, nullValue());
	}

	@Test
	public void metadataAtPath_propInfo_notFound_propMap() {
		// GIVEN
		GeneralDatumMetadata meta = getTestInstance();
		meta.putInfoValue("foo", "bar", "bam");

		// WHEN
		String s = meta.metadataAtPath("/m/foo_nope/bar", String.class);

		// THEN
		assertThat("Metadata not found", s, nullValue());
	}

	@Test
	public void metadataAtPath_tags_root() {
		// GIVEN
		GeneralDatumMetadata meta = getTestInstance();

		// WHEN
		@SuppressWarnings("unchecked")
		Set<String> s = meta.metadataAtPath("/t", Set.class);

		// THEN
		assertThat("Resolved metadata", s, sameInstance(meta.getTags()));
	}

	@Test
	public void metadataAtPath_tags_present() {
		// GIVEN
		GeneralDatumMetadata meta = getTestInstance();

		// WHEN
		String t = meta.metadataAtPath("/t/test", String.class);

		// THEN
		assertThat("Resolved tag", t, equalTo("test"));
	}

	@Test
	public void metadataAtPath_tags_notPresent() {
		// GIVEN
		GeneralDatumMetadata meta = getTestInstance();

		// WHEN
		String t = meta.metadataAtPath("/t/test_nope", String.class);

		// THEN
		assertThat("No tag found", t, nullValue());
	}

	@Test
	public void metadataAtPath_invalidPath() {
		// GIVEN
		GeneralDatumMetadata meta = getTestInstance();

		// WHEN
		String t = meta.metadataAtPath("/not/valid", String.class);

		// THEN
		assertThat("No value found", t, nullValue());
	}

	@Test
	public void metadataAtPath_emptyPath() {
		// GIVEN
		GeneralDatumMetadata meta = getTestInstance();

		// WHEN
		String t = meta.metadataAtPath("", String.class);

		// THEN
		assertThat("No value found", t, nullValue());
	}

	@Test
	public void metadataAtPath_nullPath() {
		// GIVEN
		GeneralDatumMetadata meta = getTestInstance();

		// WHEN
		String t = meta.metadataAtPath(null, String.class);

		// THEN
		assertThat("No value found", t, nullValue());
	}

}

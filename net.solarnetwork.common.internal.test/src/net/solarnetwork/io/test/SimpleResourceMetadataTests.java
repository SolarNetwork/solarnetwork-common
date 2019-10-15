/* ==================================================================
 * SimpleResourceMetadataTests.java - 16/10/2019 7:25:40 am
 * 
 * Copyright 2019 SolarNetwork.net Dev Team
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

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.springframework.util.MimeType;
import net.solarnetwork.io.ResourceMetadata;
import net.solarnetwork.io.SimpleResourceMetadata;

/**
 * Test cases for the {@link SimpleResourceMetadata} class.
 * 
 * @author matt
 * @version 1.0
 */
public class SimpleResourceMetadataTests {

	@Test
	public void construct_modified() {
		// GIVEN
		Date d = new Date();

		// WHEN
		SimpleResourceMetadata m = new SimpleResourceMetadata(d);

		// THEN
		assertThat("Modified date preserved", m.getModified(), sameInstance(d));
		assertThat("Content type defaulted", m.getContentType(),
				sameInstance(ResourceMetadata.DEFAULT_CONTENT_TYPE));

		Map<String, Object> expectedMeta = new HashMap<>(4);
		expectedMeta.put(ResourceMetadata.MODIFIED_KEY, d);
		expectedMeta.put(ResourceMetadata.CONTENT_TYPE_KEY, ResourceMetadata.DEFAULT_CONTENT_TYPE);
		assertThat("Metadata map", m.asMap(), equalTo(expectedMeta));
	}

	@Test
	public void construct_modifiedAndContentType() {
		// GIVEN
		Date d = new Date();
		MimeType contentType = MimeType.valueOf("text/plain");

		// WHEN
		SimpleResourceMetadata m = new SimpleResourceMetadata(d, contentType);

		// THEN
		assertThat("Modified date preserved", m.getModified(), sameInstance(d));
		assertThat("Content type preserved", m.getContentType(), sameInstance(contentType));

		Map<String, Object> expectedMeta = new HashMap<>(4);
		expectedMeta.put(ResourceMetadata.MODIFIED_KEY, d);
		expectedMeta.put(ResourceMetadata.CONTENT_TYPE_KEY, contentType);
		assertThat("Metadata map", m.asMap(), equalTo(expectedMeta));
	}

	@Test
	public void construct_extended() {
		// GIVEN
		Date d = new Date();
		MimeType contentType = MimeType.valueOf("text/plain");
		Map<String, Object> extended = new HashMap<>(4);
		extended.put("foo", "bar");
		extended.put("n", Math.random());

		// WHEN
		SimpleResourceMetadata m = new SimpleResourceMetadata(d, contentType, extended);

		// THEN
		assertThat("Modified date preserved", m.getModified(), sameInstance(d));
		assertThat("Content type preserved", m.getContentType(), sameInstance(contentType));
		assertThat("Extended copied", m.getExtendedMetadata(),
				allOf(not(sameInstance(extended)), equalTo(extended)));

		Map<String, Object> expectedMeta = new HashMap<>(4);
		expectedMeta.put(ResourceMetadata.MODIFIED_KEY, d);
		expectedMeta.put(ResourceMetadata.CONTENT_TYPE_KEY, contentType);
		expectedMeta.putAll(extended);
		assertThat("Metadata map", m.asMap(), equalTo(expectedMeta));
	}

}

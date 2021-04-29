/* ==================================================================
 * S3ObjectMetaTests.java - 16/10/2019 9:58:21 am
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

package net.solarnetwork.common.s3.test;

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
import net.solarnetwork.common.s3.S3ObjectMeta;
import net.solarnetwork.common.s3.S3ObjectMetadata;
import net.solarnetwork.io.ResourceMetadata;

/**
 * Test cases for the {@link S3ObjectMeta} class.
 * 
 * @author matt
 * @version 1.0
 */
public class S3ObjectMetaTests {

	@Test
	public void construct_extended() {
		// GIVEN
		Date d = new Date();
		MimeType contentType = MimeType.valueOf("text/plain");
		long contentLength = 12;
		Map<String, Object> extended = new HashMap<>(4);
		extended.put("Content-Disposition", "attachment; filename=\"foo.txt\"");

		// WHEN
		S3ObjectMeta m = new S3ObjectMeta(contentLength, d, contentType, extended);

		// THEN
		assertThat("Size preserved", m.getSize(), equalTo(contentLength));
		assertThat("Modified date preserved", m.getModified(), sameInstance(d));
		assertThat("Content type preserved", m.getContentType(), sameInstance(contentType));
		assertThat("Extended copied", m.getExtendedMetadata(),
				allOf(not(sameInstance(extended)), equalTo(extended)));

		Map<String, Object> expectedMeta = new HashMap<>(4);
		expectedMeta.put(S3ObjectMetadata.SIZE_KEY, contentLength);
		expectedMeta.put(ResourceMetadata.MODIFIED_KEY, d);
		expectedMeta.put(ResourceMetadata.CONTENT_TYPE_KEY, contentType);
		expectedMeta.put(S3ObjectMetadata.STORAGE_CLASS_KEY, "STANDARD");
		expectedMeta.putAll(extended);
		assertThat("Metadata map", m.asMap(), equalTo(expectedMeta));

		assertThat("Custom metadata map", m.asCustomMap(), equalTo(extended));

	}

}

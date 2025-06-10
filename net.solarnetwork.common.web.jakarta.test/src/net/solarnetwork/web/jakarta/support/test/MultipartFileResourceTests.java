/* ==================================================================
 * MultipartFileResourceTests.java - 9/11/2018 10:43:19 AM
 * 
 * Copyright 2018 SolarNetwork.net Dev Team
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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.FileCopyUtils;
import net.solarnetwork.web.jakarta.support.MultipartFileResource;

/**
 * Test cases for the {@link MultipartFileResource} class.
 * 
 * @author matt
 * @version 2.0
 */
public class MultipartFileResourceTests {

	@Test(expected = IllegalArgumentException.class)
	public void constructWithNull() {
		new MultipartFileResource(null);
	}

	@Test
	public void construct() throws IOException {
		String data = "Hello, " + UUID.randomUUID().toString() + ".";
		MockMultipartFile mpf = new MockMultipartFile("foobar", "test-file.txt", "text/plain",
				data.getBytes(StandardCharsets.UTF_8));
		MultipartFileResource r = new MultipartFileResource(mpf);
		assertThat("Content length", r.contentLength(), equalTo((long) data.length()));
		assertThat("Filename", r.getFilename(), equalTo("test-file.txt"));

		ByteArrayOutputStream byos = new ByteArrayOutputStream();
		FileCopyUtils.copy(r.getInputStream(), byos);
		assertThat("Content", new String(byos.toByteArray(), "UTF-8"), equalTo(data));
	}

	@Test
	public void transfer() throws IOException {
		String data = "Hello, " + UUID.randomUUID().toString() + ".";
		MockMultipartFile mpf = new MockMultipartFile("foobar", "test-file.txt", "text/plain",
				data.getBytes(StandardCharsets.UTF_8));
		MultipartFileResource r = new MultipartFileResource(mpf);

		File tmpFile = File.createTempFile("data-", ".txt");
		r.transferTo(tmpFile);
		assertThat("File transferred", tmpFile.exists(), equalTo(true));

		ByteArrayOutputStream byos = new ByteArrayOutputStream();
		FileCopyUtils.copy(new FileInputStream(tmpFile), byos);
		assertThat("Content", new String(byos.toByteArray(), "UTF-8"), equalTo(data));

		tmpFile.delete();
	}

}

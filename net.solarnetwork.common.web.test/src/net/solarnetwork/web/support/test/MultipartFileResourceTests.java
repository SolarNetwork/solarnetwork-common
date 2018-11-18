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

package net.solarnetwork.web.support.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.junit.Test;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import net.solarnetwork.web.support.MultipartFileResource;

/**
 * Test cases for the {@link MultipartFileResource} class.
 * 
 * @author matt
 * @version 1.0
 */
public class MultipartFileResourceTests {

	@Test(expected = IllegalArgumentException.class)
	public void constructWithNull() {
		new MultipartFileResource(null);
	}

	@Test
	public void construct() throws IOException {
		FileItem f = new DiskFileItemFactory().createItem("foobar", "text/plain", false,
				"test-file.txt");
		String data = "Hello, " + UUID.randomUUID().toString() + ".";
		FileCopyUtils.copy(data.getBytes("UTF-8"), f.getOutputStream());
		CommonsMultipartFile mpf = new CommonsMultipartFile(f);
		MultipartFileResource r = new MultipartFileResource(mpf);
		assertThat("Content length", r.contentLength(), equalTo((long) data.length()));
		assertThat("Filename", r.getFilename(), equalTo("test-file.txt"));

		ByteArrayOutputStream byos = new ByteArrayOutputStream();
		FileCopyUtils.copy(r.getInputStream(), byos);
		assertThat("Content", new String(byos.toByteArray(), "UTF-8"), equalTo(data));

		f.delete();
	}

	@Test
	public void transfer() throws IOException {
		FileItem f = new DiskFileItemFactory().createItem("foobar", "text/plain", false,
				"test-file.txt");
		String data = "Hello, " + UUID.randomUUID().toString() + ".";
		FileCopyUtils.copy(data.getBytes("UTF-8"), f.getOutputStream());
		CommonsMultipartFile mpf = new CommonsMultipartFile(f);
		MultipartFileResource r = new MultipartFileResource(mpf);

		File tmpFile = File.createTempFile("data-", ".txt");
		r.transferTo(tmpFile);
		assertThat("File transferred", tmpFile.exists(), equalTo(true));

		ByteArrayOutputStream byos = new ByteArrayOutputStream();
		FileCopyUtils.copy(new FileInputStream(tmpFile), byos);
		assertThat("Content", new String(byos.toByteArray(), "UTF-8"), equalTo(data));

		tmpFile.delete();
		f.delete();
	}

}

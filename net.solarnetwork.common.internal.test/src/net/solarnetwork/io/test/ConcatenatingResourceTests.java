/* ==================================================================
 * ConcatenatingResourceTests.java - 24/05/2018 7:59:26 AM
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

package net.solarnetwork.io.test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import org.junit.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import net.solarnetwork.io.ConcatenatingInputStream;
import net.solarnetwork.io.ConcatenatingResource;

/**
 * Test cases for the {@link ConcatenatingResource} class.
 * 
 * @author matt
 * @version 1.0
 */
public class ConcatenatingResourceTests {

	@Test
	public void description() throws IOException {
		Resource r1 = new ByteArrayResource("Hello\n".getBytes("UTF-8"), "Hello");
		Resource r2 = new ByteArrayResource("world!".getBytes("UTF-8"), "World");
		ConcatenatingResource r = new ConcatenatingResource(Arrays.asList(r1, r2));
		assertThat("Description", r.getDescription(), equalTo(
				"ConcatenatingResource{Byte array resource [Hello], Byte array resource [World]}"));
	}

	@Test
	public void contentLength() throws IOException {
		Resource r1 = new ByteArrayResource("Hello\n".getBytes("UTF-8"), "Hello");
		Resource r2 = new ByteArrayResource("world!".getBytes("UTF-8"), "World");
		ConcatenatingResource r = new ConcatenatingResource(Arrays.asList(r1, r2));
		assertThat("Content length", r.contentLength(), equalTo(12L));
	}

	@Test
	public void inputStream() throws IOException {
		Resource r1 = new ByteArrayResource("Hello\n".getBytes("UTF-8"), "Hello");
		Resource r2 = new ByteArrayResource("world!".getBytes("UTF-8"), "World");
		ConcatenatingResource r = new ConcatenatingResource(Arrays.asList(r1, r2));
		InputStream in = r.getInputStream();
		assertThat("Stream", in, instanceOf(ConcatenatingInputStream.class));
		String s = FileCopyUtils.copyToString(new InputStreamReader(in, "UTF-8"));
		assertThat("Content", s, equalTo("Hello\nworld!"));
	}
}

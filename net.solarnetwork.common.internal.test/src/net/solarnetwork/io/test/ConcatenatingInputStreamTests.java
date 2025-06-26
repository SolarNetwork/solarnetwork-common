/* ==================================================================
 * ConcatenatingInputStreamTests.java - 24/05/2018 7:52:18 AM
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.junit.Test;
import org.springframework.util.FileCopyUtils;
import net.solarnetwork.io.ConcatenatingInputStream;

/**
 * Test cases for the {@link ConcatenatingInputStream} class.
 * 
 * @author matt
 * @version 1.0
 */
public class ConcatenatingInputStreamTests {

	@Test
	public void concatenate() throws IOException {
		InputStream in1 = new ByteArrayInputStream("Hello\n".getBytes("UTF-8"));
		InputStream in2 = new ByteArrayInputStream("world!".getBytes("UTF-8"));
		ConcatenatingInputStream in = new ConcatenatingInputStream(new InputStream[] { in1, in2 });
		String out = FileCopyUtils.copyToString(new InputStreamReader(in, "UTF-8"));
		assertThat("Concatenated value", out, equalTo("Hello\nworld!"));
	}

	@Test
	public void concatenateWithEmptyStream() throws IOException {
		InputStream in1 = new ByteArrayInputStream(new byte[0]);
		InputStream in2 = new ByteArrayInputStream("Hello".getBytes("UTF-8"));
		ConcatenatingInputStream in = new ConcatenatingInputStream(new InputStream[] { in1, in2 });
		String out = FileCopyUtils.copyToString(new InputStreamReader(in, "UTF-8"));
		assertThat("Concatenated value", out, equalTo("Hello"));
	}

}

/* ==================================================================
 * DeleteOnCloseFileInputStreamTests.java - 23/04/2018 12:27:33 PM
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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import java.io.File;
import java.io.IOException;
import org.junit.Test;
import net.solarnetwork.io.DeleteOnCloseFileInputStream;

/**
 * Test cases for the {@link DeleteOnCloseFileInputStream} class.
 * 
 * @author matt
 * @version 1.0
 */
public class DeleteOnCloseFileInputStreamTests {

	@Test
	public void deleteOnClose() throws IOException {
		File tempFile = File.createTempFile("foo-", ".bar");
		assertThat("File exists", tempFile.exists(), equalTo(true));
		DeleteOnCloseFileInputStream in = null;
		try {
			in = new DeleteOnCloseFileInputStream(tempFile);
		} finally {
			if ( in != null ) {
				in.close();
			}
		}
		assertThat("File deleted", tempFile.exists(), equalTo(false));
	}

}

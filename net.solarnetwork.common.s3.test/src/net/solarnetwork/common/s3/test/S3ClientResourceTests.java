/* ==================================================================
 * S3ClientResourceTests.java - 15/10/2019 11:33:38 am
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

import static org.easymock.EasyMock.expect;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import java.util.Objects;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import net.solarnetwork.common.s3.S3Client;
import net.solarnetwork.common.s3.S3ClientResource;
import net.solarnetwork.common.s3.S3ObjectRef;

/**
 * Test cases for the {@link S3ClientResource} class.
 * 
 * @author matt
 * @version 1.0
 */
public class S3ClientResourceTests {

	private static final String TEST_CLIENT_UID = "client.uid";
	private S3Client s3Client;

	@Before
	public void setup() {
		s3Client = EasyMock.createMock(S3Client.class);
	}

	@After
	public void teardown() {
		EasyMock.verify(s3Client);
	}

	private void replayAll(Object... extra) {
		EasyMock.replay(s3Client);
		if ( extra != null ) {
			EasyMock.replay(extra);
		}
	}

	@Test
	public void hash() {
		// GIVEN
		expect(s3Client.getSettingUid()).andReturn(TEST_CLIENT_UID).anyTimes();

		// WHEN
		replayAll();
		S3ObjectRef ref = new S3ObjectRef("foo");
		S3ClientResource r = new S3ClientResource(s3Client, ref);
		int result = r.hashCode();

		// THEN
		assertThat("Computed hash code value derived from setting UID and object ref", result,
				equalTo(Objects.hash(TEST_CLIENT_UID, ref)));
	}

	@Test
	public void equals() {
		// GIVEN
		expect(s3Client.getSettingUid()).andReturn(TEST_CLIENT_UID).anyTimes();

		// WHEN
		replayAll();
		S3ObjectRef ref = new S3ObjectRef("foo");
		S3ClientResource r = new S3ClientResource(s3Client, ref);

		// THEN
		assertThat("Different resource with same setting UID and object ref are equal", r,
				equalTo(new S3ClientResource(s3Client, new S3ObjectRef("foo"))));
	}

	@Test
	public void equals_differentRef() {
		// GIVEN
		expect(s3Client.getSettingUid()).andReturn(TEST_CLIENT_UID).anyTimes();

		// WHEN
		replayAll();
		S3ObjectRef ref = new S3ObjectRef("foo");
		S3ClientResource r = new S3ClientResource(s3Client, ref);

		// THEN
		assertThat("Different resource with same setting UID and object ref are equal", r,
				not(equalTo(new S3ClientResource(s3Client, new S3ObjectRef("FOOBAR")))));
	}

	@Test
	public void equals_differentClient() {
		// GIVEN
		expect(s3Client.getSettingUid()).andReturn(TEST_CLIENT_UID).anyTimes();
		S3Client c2 = EasyMock.createMock(S3Client.class);
		expect(c2.getSettingUid()).andReturn("other.client").anyTimes();

		// WHEN
		replayAll(c2);
		S3ObjectRef ref = new S3ObjectRef("foo");
		S3ClientResource r = new S3ClientResource(s3Client, ref);

		// THEN
		assertThat("Different resource with same setting UID and object ref are equal", r,
				not(equalTo(new S3ClientResource(c2, new S3ObjectRef("foo")))));
	}

}

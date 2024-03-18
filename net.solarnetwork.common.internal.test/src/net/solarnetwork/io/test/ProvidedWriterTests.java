/* ==================================================================
 * ProvidedWriterTests.java - 19/03/2024 7:00:31 am
 *
 * Copyright 2024 SolarNetwork.net Dev Team
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

import static org.easymock.EasyMock.expect;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.UUID;
import java.util.function.Supplier;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import net.solarnetwork.io.ProvidedWriter;

/**
 * Test cases for the {@link ProvidedWriter} class.
 *
 * @author matt
 * @version 1.0
 */
public class ProvidedWriterTests {

	private Supplier<Writer> supplier;

	@SuppressWarnings("unchecked")
	@Before
	public void setup() {
		supplier = EasyMock.createMock(Supplier.class);
	}

	private void replayAll() {
		EasyMock.replay(supplier);
	}

	@After
	public void teardown() {
		EasyMock.verify(supplier);
	}

	@Test
	public void write() throws IOException {
		// GIVEN
		StringWriter dest = new StringWriter();
		expect(supplier.get()).andReturn(dest);

		// WHEN
		replayAll();

		String msg = UUID.randomUUID().toString();
		try (ProvidedWriter out = new ProvidedWriter(supplier)) {
			out.write(msg);
		}

		// THEN
		assertThat("Message written to destination", dest.toString(), is(equalTo(msg)));
	}

	@Test
	public void neverWrite() throws IOException {
		// GIVEN

		// WHEN
		replayAll();

		try (ProvidedWriter out = new ProvidedWriter(supplier)) {
			// don't do anything here
		}

		// THEN
		// mock verification will prove supplier never invoked
	}

	@Test
	public void supplierIllegalStateException() throws IOException {
		// GIVEN
		StringWriter dest = new StringWriter();
		expect(supplier.get()).andThrow(new IllegalStateException("Writer already obtained."));

		// WHEN
		replayAll();

		String msg = UUID.randomUUID().toString();
		try (ProvidedWriter out = new ProvidedWriter(supplier)) {
			out.write(msg);
		}

		// THEN
		assertThat("Message not written to destination", dest.toString(), is(equalTo("")));
	}

	@Test
	public void supplierReturnsNull() throws IOException {
		// GIVEN
		expect(supplier.get()).andReturn(null);

		// WHEN
		replayAll();

		String msg = UUID.randomUUID().toString();
		try (ProvidedWriter out = new ProvidedWriter(supplier)) {
			out.write(msg);
		}

		// THEN
		// no error thrown
	}

}

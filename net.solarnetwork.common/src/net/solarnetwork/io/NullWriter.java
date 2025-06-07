/* ==================================================================
 * NullWriter.java - 19/03/2024 6:50:25 am
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

package net.solarnetwork.io;

import java.io.IOException;
import java.io.Writer;

/**
 * Writer that does not write anything.
 *
 * <p>
 * Writing with this class is similar to writing to {@code /dev/null}.
 * </p>
 *
 * @author matt
 * @version 1.0
 * @since 3.8
 */
public class NullWriter extends Writer {

	/** A default instance. */
	public static final NullWriter INSTANCE = new NullWriter();

	/**
	 * Constructor.
	 */
	public NullWriter() {
		super();
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		// ignore
	}

	@Override
	public void flush() throws IOException {
		// ignore
	}

	@Override
	public void close() throws IOException {
		// ignore
	}

	@Override
	public void write(int c) throws IOException {
		// ignore
	}

	@Override
	public void write(char[] cbuf) throws IOException {
		// ignore
	}

	@Override
	public void write(String str) throws IOException {
		// ignore
	}

	@Override
	public void write(String str, int off, int len) throws IOException {
		// ignore
	}

	@Override
	public Writer append(CharSequence csq) throws IOException {
		return this;
	}

	@Override
	public Writer append(CharSequence csq, int start, int end) throws IOException {
		return this;
	}

	@Override
	public Writer append(char c) throws IOException {
		return this;
	}

}

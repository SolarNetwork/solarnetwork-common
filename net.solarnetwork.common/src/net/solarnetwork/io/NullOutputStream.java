/* ==================================================================
 * NullOutputStream.java - 19/03/2024 7:21:26 am
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
import java.io.OutputStream;

/**
 * Output stream that does not write anything.
 *
 * <p>
 * Writing with this class is similar to writing to {@code /dev/null}.
 * </p>
 *
 * @author matt
 * @version 1.0
 * @since 3.8
 */
public class NullOutputStream extends OutputStream {

	/** A default instance. */
	public static final NullOutputStream INSTANCE = new NullOutputStream();

	/**
	 * Constructor.
	 */
	public NullOutputStream() {
		super();
	}

	@Override
	public void write(int b) throws IOException {
		// ignore
	}

	@Override
	public void write(byte[] b) throws IOException {
		// ignore
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
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

}

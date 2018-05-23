/* ==================================================================
 * ConcatenatingInputStream.java - 24/05/2018 6:56:03 AM
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

package net.solarnetwork.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

/**
 * An {@link InputStream} that is the concatenation of multiple streams.
 * 
 * @author matt
 * @version 1.0
 * @since 1.44
 */
public class ConcatenatingInputStream extends InputStream {

	private final Queue<InputStream> streams;

	/**
	 * Construct from a collection of input streams.
	 * 
	 * @param streams
	 *        the input streams
	 */
	public ConcatenatingInputStream(Collection<InputStream> streams) {
		super();
		if ( streams instanceof Queue ) {
			this.streams = (Queue<InputStream>) streams;
		} else {
			this.streams = new LinkedList<InputStream>(streams);
		}
	}

	/**
	 * Construct from an array of input streams.
	 * 
	 * @param streams
	 *        the input streams
	 */
	public ConcatenatingInputStream(InputStream[] streams) {
		this.streams = new LinkedList<InputStream>();
		for ( InputStream stream : streams ) {
			this.streams.add(stream);
		}
	}

	@Override
	public int read() throws IOException {
		while ( !streams.isEmpty() ) {
			InputStream stream = streams.peek();
			int res = stream.read();
			if ( res != -1 ) {
				return res;
			}
			// pop off done stream, close it, and try next stream
			streams.remove();
			stream.close();
		}
		return -1;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		while ( !streams.isEmpty() ) {
			InputStream stream = streams.peek();
			int res = stream.read(b, off, len);
			if ( res != -1 ) {
				return res;
			}
			// pop off done stream, close it, and try next stream
			streams.remove();
			stream.close();
		}
		return -1;
	}

	@Override
	public void close() throws IOException {
		while ( !streams.isEmpty() ) {
			InputStream stream = streams.remove();
			stream.close();
		}
	}

}

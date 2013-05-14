/* ==================================================================
 * RFC1924OutputStream.java - May 15, 2013 6:42:26 AM
 * 
 * Copyright 2007-2013 SolarNetwork.net Dev Team
 * 
 * Adapted from libxjava original source.
 * 
 * Copyright (c) 2010 Marcel Patzlaff (marcel.patzlaff@gmail.com)
 *
 * This library is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * ==================================================================
 */

package net.solarnetwork.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Encode binary data into ASCII85 form based on RFC 1924.
 * 
 * @author matt
 * @version 1.0
 */
public class RFC1924OutputStream extends FilterOutputStream {

	static final byte[] ENCODABET = new byte[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A',
			'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S',
			'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k',
			'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '!', '#', '$',
			'%', '&', '(', ')', '*', '+', '-', ';', '<', '=', '>', '?', '@', '^', '_', '`', '{', '|',
			'}', '~' };

	static final long[] FACTORS = { 1, // 85^0
			85, // 85^1
			7225, // 85^2
			614125, // 85^3
			52200625 // 85^4
	};

	private final int[] tuple = new int[4];
	private int bIndex = 0;

	/**
	 * Construct with OutputStream to filter.
	 * 
	 * @param out
	 *        the output stream
	 */
	public RFC1924OutputStream(OutputStream out) {
		super(out);
	}

	private void writeTuple() throws IOException {
		int bytes;
		long sum = 0;
		for ( bytes = 0; bytes < bIndex; bytes++ ) {
			sum = (sum << 8) | tuple[bytes];
		}
		if ( bytes == 4 ) {
			for ( int e = 4; e >= 0; e-- ) {
				super.write(ENCODABET[(int) (sum / FACTORS[e])]);
				sum %= FACTORS[e];
			}
			sum = 0;
			bytes = 0;
		} else if ( bytes > 0 ) {
			for ( int e = bytes; e >= 0; e-- ) {
				super.write(ENCODABET[(int) (sum / FACTORS[e])]);
				sum %= FACTORS[e];
			}
		}
	}

	@Override
	public void write(int b) throws IOException {
		tuple[bIndex++] = b & 0xFF;
		if ( bIndex >= tuple.length ) {
			writeTuple();
			bIndex = 0;
		}
	}

	@Override
	public void close() throws IOException {
		if ( bIndex > 0 && bIndex < tuple.length ) {
			writeTuple();
			bIndex = -1;
			flush();
		}
	}

}

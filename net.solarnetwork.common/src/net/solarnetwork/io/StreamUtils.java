/* ==================================================================
 * StreamUtils.java - 9/03/2022 2:07:16 PM
 * 
 * Copyright 2022 SolarNetwork.net Dev Team
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
import java.io.PushbackInputStream;
import java.util.zip.GZIPInputStream;

/**
 * Stream utilities.
 * 
 * @author matt
 * @version 1.0
 * @since 2.3
 */
public final class StreamUtils {

	/** The "magic bytes" to look for at the start of a GZIP stream. */
	public static final int GZIP_MAGIC = 0x1f8b;

	/**
	 * Get an {@link InputStream} for a given stream with automatic GZIP
	 * detection.
	 * 
	 * <p>
	 * If {@code in} is already a {@link GZIPInputStream} then it will be
	 * returned directly. Otherwise he given stream will be read just enough to
	 * determine if it looks like a GZIP stream. If it does, then a new
	 * {@link GZIPInputStream} will be returned.
	 * </p>
	 * 
	 * @param in
	 *        the input stream to wrap
	 * @return the appropriate input stream, never {@literal null}
	 * @throws IOException
	 *         if any IO error occurs
	 */
	public static InputStream inputStreamForPossibleGzipStream(InputStream in) throws IOException {
		// checking for GZIP
		if ( in instanceof GZIPInputStream ) {
			return in;
		}
		PushbackInputStream s = new PushbackInputStream(in, 2);
		int count = 0;
		byte[] magic = new byte[] { -1, -1 };
		while ( count < 2 ) {
			int readCount = s.read(magic, count, 2 - count);
			if ( readCount < 0 ) {
				break;
			}
			count += readCount;
		}
		s.unread(magic, 0, count);
		if ( magic[0] == (byte) (GZIP_MAGIC >> 8) && magic[1] == (byte) (GZIP_MAGIC) ) {
			return new GZIPInputStream(s);
		}
		return s;
	}

}

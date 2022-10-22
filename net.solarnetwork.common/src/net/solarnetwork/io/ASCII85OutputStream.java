
package net.solarnetwork.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * The ASCII85InputStream encodes binary data as ASCII base-85 encoding. The
 * exact definition of ASCII base-85 encoding can be found in the PostScript
 * Language Reference (3rd ed.) chapter 3.13.3.
 * 
 * <p>
 * This adaptation from the original source omits the ASCII85 "armor" {@code <~}
 * and {@code ~>}.
 * </p>
 * 
 * @author Mark Donszelmann
 * @version 1.4 2003/04/13 05:34:45
 */
public class ASCII85OutputStream extends FilterOutputStream {

	/** The maximum number of characters per line. */
	public final static int MAX_CHARS_PER_LINE = 80;

	/** The ASCII-85 point 1 value. */
	public static long a85p1 = 85;

	/** The ASCII-85 point 2 value. */
	public static long a85p2 = a85p1 * a85p1;

	/** The ASCII-85 point 3 value. */
	public static long a85p3 = a85p2 * a85p1;

	/** The ASCII-85 point 4 value. */
	public static long a85p4 = a85p3 * a85p1;

	private boolean end;
	private int characters;
	private final int b[] = new int[4];
	private int bIndex;
	private final int c[] = new int[5];

	/**
	 * Constructor.
	 * 
	 * @param out
	 *        the output stream to write to
	 */
	public ASCII85OutputStream(OutputStream out) {
		super(out);
		characters = MAX_CHARS_PER_LINE;
		end = false;
		bIndex = 0;
	}

	@Override
	public void write(int a) throws IOException {
		b[bIndex] = a & 0x00FF;
		bIndex++;
		if ( bIndex >= b.length ) {
			writeTuple();
			bIndex = 0;
		}
	}

	/**
	 * Finish the stream without closing.
	 * 
	 * @throws IOException
	 *         if any error occurs
	 */
	public void finish() throws IOException {
		if ( !end ) {
			end = true;
			if ( bIndex > 0 ) {
				writeTuple();
			}
			flush();
		}
	}

	@Override
	public void close() throws IOException {
		finish();
		super.close();
	}

	private void writeTuple() throws IOException {
		// fill the rest
		for ( int i = bIndex; i < b.length; i++ ) {
			b[i] = 0;
		}

		// convert
		long d = ((b[0] << 24) | (b[1] << 16) | (b[2] << 8) | b[3]) & 0x00000000FFFFFFFFL;

		c[0] = (int) (d / a85p4 + '!');
		d = d % a85p4;
		c[1] = (int) (d / a85p3 + '!');
		d = d % a85p3;
		c[2] = (int) (d / a85p2 + '!');
		d = d % a85p2;
		c[3] = (int) (d / a85p1 + '!');
		c[4] = (int) (d % a85p1 + '!');

		// convert !!!!! to z
		if ( (bIndex >= b.length) && (c[0] == '!') && (c[1] == '!') && (c[2] == '!') && (c[3] == '!')
				&& (c[4] == '!') ) {
			writeChar('z');
		} else {
			for ( int i = 0; i < bIndex + 1; i++ ) {
				writeChar(c[i]);
			}
		}
	}

	private void writeChar(int b) throws IOException {
		if ( characters == 0 ) {
			characters = MAX_CHARS_PER_LINE;
			super.write('\n');
		}
		characters--;
		super.write(b);
	}
}

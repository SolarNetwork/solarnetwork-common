/* ==================================================================
 * ByteArrayJavaClass.java - 20/04/2021 2:35:38 PM
 * 
 * Copyright 2021 SolarNetwork.net Dev Team
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

package net.solarnetwork.common.protobuf.protoc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import javax.tools.SimpleJavaFileObject;

/**
 * Byte array implementation of {@link SimpleJavaFileObject}.
 * 
 * <p>
 * Adapted from Core Java, Volume II--Advanced Features, 10th Edition.
 * </p>
 * 
 * @author matt
 * @version 1.0
 */
public class ByteArrayJavaClass extends SimpleJavaFileObject {

	private final ByteArrayOutputStream stream;

	/**
	 * Constructor.
	 * 
	 * @param name
	 *        the name of the class file
	 */
	public ByteArrayJavaClass(String name) {
		super(URI.create("bytes:///" + name), Kind.CLASS);
		stream = new ByteArrayOutputStream();
	}

	@Override
	public OutputStream openOutputStream() throws IOException {
		return stream;
	}

	public byte[] getBytes() {
		return stream.toByteArray();
	}
}

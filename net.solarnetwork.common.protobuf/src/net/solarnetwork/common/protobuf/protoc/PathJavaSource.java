/* ==================================================================
 * PathJavaSource.java - 20/04/2021 2:46:41 PM
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import org.springframework.util.FileCopyUtils;

/**
 * Java source based on a path.
 * 
 * @author matt
 * @version 1.0
 */
public class PathJavaSource extends SimpleJavaFileObject {

	private final Path path;

	/**
	 * Constructor.
	 * 
	 * @param path
	 *        the source path
	 */
	public PathJavaSource(Path path) {
		super(path.toUri(), JavaFileObject.Kind.SOURCE);
		this.path = path;
	}

	@Override
	public InputStream openInputStream() throws IOException {
		return Files.newInputStream(path);
	}

	@Override
	public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
		return FileCopyUtils.copyToString(new InputStreamReader(openInputStream(), "UTF-8"));
	}

}

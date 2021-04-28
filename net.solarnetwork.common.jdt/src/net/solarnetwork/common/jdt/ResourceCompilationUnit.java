/* ==================================================================
 * ResourceCompilationUnit.java - 27/04/2021 3:15:16 PM
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

package net.solarnetwork.common.jdt;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

/**
 * Compilation unit for a {@link Resource}.
 * 
 * @author matt
 * @version 1.0
 */
@SuppressWarnings("restriction")
public class ResourceCompilationUnit implements ICompilationUnit {

	private static final Logger log = LoggerFactory.getLogger(ResourceCompilationUnit.class);

	private final Path rootPath;
	private final Resource resource;

	/**
	 * Constructor.
	 * 
	 * @param resource
	 *        the resource
	 * @param rootPath
	 *        the root path, from which packages are determined
	 * @throws IllegalArgumentException
	 *         if any argument is {@literal null}
	 */
	public ResourceCompilationUnit(Resource resource, Path rootPath) {
		super();
		if ( resource == null || rootPath == null ) {
			throw new IllegalArgumentException("The resource argument must not be null.");
		}
		this.resource = resource;
		this.rootPath = rootPath;
	}

	@Override
	public char[] getFileName() {
		String name = resource.getFilename();
		return (name != null ? name.toCharArray() : null);
	}

	@Override
	public char[] getContents() {
		try (InputStream in = resource.getInputStream()) {
			String s = StreamUtils.copyToString(in, Charset.defaultCharset());
			return s.toCharArray();
		} catch ( IOException e ) {
			log.warn("Error reading contents of resource [{}] to string: {}", resource.getFilename(),
					e.toString());
		}
		return null;
	}

	@Override
	public char[] getMainTypeName() {
		String name = resource.getFilename();
		if ( name != null ) {
			int dot = name.lastIndexOf('.');
			if ( dot > 0 ) {
				return name.substring(0, dot).toCharArray();
			}
		}
		return null;
	}

	@Override
	public char[][] getPackageName() {
		try {
			Path path = Paths.get(resource.getURI());
			Path relPath = rootPath.relativize(path);
			if ( relPath != null ) {
				int count = relPath.getNameCount() - 1;
				char[][] result = new char[count][];
				for ( int i = 0; i < count; i++ ) {
					result[i] = relPath.getName(i).toString().toCharArray();
				}
				return result;
			}
		} catch ( IOException e ) {
			log.debug("Error determining package of resource [{}]: {}", resource.getFilename(),
					e.toString());
		}
		return null;
	}

	@Override
	public boolean ignoreOptionalProblems() {
		return true;
	}

}

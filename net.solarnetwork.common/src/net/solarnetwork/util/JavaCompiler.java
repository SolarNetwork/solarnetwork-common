/* ==================================================================
 * JavaCompiler.java - 27/04/2021 12:09:38 PM
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

package net.solarnetwork.util;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import org.springframework.core.io.Resource;

/**
 * API for a service to compile Java sources into classes.
 * 
 * <p>
 * This API is designed for <i>small</i> compilation tasks, to support dynamic
 * classes within the runtime.
 * </p>
 * 
 * @author matt
 * @version 1.0
 * @since 1.69
 */
public interface JavaCompiler {

	/**
	 * Parameter key for a {@link ClassLoader} instance to use during
	 * compilation.
	 */
	String CLASSLOADER_PARAM = "classLoader";

	/**
	 * Compile a set of Java source resources into classes, returning a
	 * {@link ClassLoader} for the compiled output.
	 * 
	 * @param javaResources
	 *        the Java resources to compile
	 * @param root
	 *        the root path to treat as the root of the Java package hierarchy
	 * @param parameters
	 *        optional compilation parameters
	 * @return a class loaded with the compiled output
	 * @throws IOException
	 *         if any compilation error occurs
	 */
	ClassLoader compileResources(Iterable<Resource> javaResources, Path root, Map<String, ?> parameters)
			throws IOException;

}

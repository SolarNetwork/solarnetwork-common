/* ==================================================================
 * ClassLoaderNameEnvironment.java - 27/04/2021 4:41:38 PM
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

import static net.solarnetwork.common.jdt.CompilerUtils.nameForType;
import static net.solarnetwork.common.jdt.CompilerUtils.populateNameForType;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StreamUtils;

/**
 * Implementation of {@link INameEnvironment} that uses a {@link ClassLoader} to
 * resolve resources.
 * 
 * @author matt
 * @version 1.0
 */
@SuppressWarnings("restriction")
public class ClassLoaderNameEnvironment implements INameEnvironment {

	private static final Logger log = LoggerFactory.getLogger(ClassLoaderNameEnvironment.class);

	private final ClassLoader classLoader;
	private final Set<String> targetClassNames;

	/**
	 * Constructor.
	 * 
	 * @param classLoader
	 *        the class loader
	 * @param targets
	 *        the target compilations
	 */
	public ClassLoaderNameEnvironment(ClassLoader classLoader, ICompilationUnit[] targets) {
		super();
		this.classLoader = classLoader;
		this.targetClassNames = targetClassNames(targets);
	}

	private Set<String> targetClassNames(ICompilationUnit[] targets) {
		Set<String> result = new HashSet<>(targets.length);
		for ( ICompilationUnit target : targets ) {
			StringBuilder buf = new StringBuilder();
			populateNameForType(buf, target.getPackageName());
			if ( buf.length() > 0 ) {
				buf.append('.');
			}
			buf.append(target.getMainTypeName());
			String name = buf.toString();
			result.add(name);
		}
		return result;
	}

	@Override
	public NameEnvironmentAnswer findType(char[][] compoundTypeName) {
		String result = nameForType(compoundTypeName);
		return findType(result);
	}

	@Override
	public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName) {
		StringBuilder buf = new StringBuilder();
		populateNameForType(buf, packageName);
		if ( buf.length() > 0 ) {
			buf.append('.');
		}
		buf.append(typeName);
		String result = buf.toString();
		return findType(result);
	}

	@Override
	public boolean isPackage(char[][] parentPackageName, char[] packageName) {
		StringBuilder buf = new StringBuilder();
		populateNameForType(buf, parentPackageName);

		if ( Character.isUpperCase(packageName[0]) ) {
			if ( !isPackage(buf.toString()) ) {
				return false;
			}
		}

		if ( buf.length() > 0 ) {
			buf.append('.');
		}
		buf.append(packageName);
		return isPackage(buf.toString());
	}

	@Override
	public void cleanup() {
		// nothing
	}

	private boolean isPackage(String result) {
		if ( targetClassNames.contains(result) ) {
			return false;
		}
		String resourceName = result.replace('.', '/') + ".class";
		try (InputStream is = classLoader.getResourceAsStream(resourceName)) {
			return is == null;
		} catch ( IOException e ) {
			log.warn("Error checking for package [{}]: {}", result, e.getMessage());
			return false;
		}
	}

	private NameEnvironmentAnswer findType(String className) {
		String resourceName = className.replace('.', '/') + ".class";
		try (InputStream is = classLoader.getResourceAsStream(resourceName)) {
			if ( is != null ) {
				// read bytes from input stream
				byte[] classBytes = StreamUtils.copyToByteArray(is);
				ClassFileReader classFileReader = new ClassFileReader(classBytes,
						className.toCharArray(), true);
				return new NameEnvironmentAnswer(classFileReader, null);
			}
		} catch ( ClassFormatException | IOException e ) {
			log.warn("Error reading class [{}]: {}", className, e.getMessage());
		}
		return null;
	}

}

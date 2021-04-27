/* ==================================================================
 * JdtJavaCompiler.java - 27/04/2021 12:15:35 PM
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
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.StreamSupport;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import net.solarnetwork.util.JavaCompiler;

/**
 * A {@link JavaCompiler} that uses the Eclipse JDT compiler implementation.
 * 
 * @author matt
 * @version 1.0
 */
@SuppressWarnings("restriction")
public class JdtJavaCompiler implements JavaCompiler {

	private static final Logger log = LoggerFactory.getLogger(JdtJavaCompiler.class);

	@Override
	public ClassLoader compileResources(Iterable<Resource> javaResources, Path root,
			Map<String, ?> parameters) throws IOException {
		if ( javaResources == null ) {
			return null;
		}
		final ClassLoader compilerClassLoader = compilerClassLoader(parameters);
		final IErrorHandlingPolicy policy = DefaultErrorHandlingPolicies.proceedWithAllProblems();
		final IProblemFactory problemFactory = new DefaultProblemFactory(Locale.getDefault());
		final CollectingCompilerRequestor requestor = new CollectingCompilerRequestor();
		final ICompilationUnit[] compilationUnits = StreamSupport
				.stream(javaResources.spliterator(), false)
				.map(r -> (ICompilationUnit) new ResourceCompilationUnit(r, root))
				.toArray(ICompilationUnit[]::new);
		final INameEnvironment env = new ClassLoaderNameEnvironment(compilerClassLoader,
				compilationUnits);

		final Map<String, String> settings = new HashMap<>(8);
		settings.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.IGNORE);
		final CompilerOptions options = new CompilerOptions(settings);

		Compiler compiler = new Compiler(env, policy, options, requestor, problemFactory);
		compiler.compile(compilationUnits);

		Map<String, byte[]> classMap = requestor.getClassMap();
		if ( classMap.isEmpty() ) {
			StringBuilder buf = new StringBuilder();
			buf.append("Error compiling Java resources ").append(javaResources).append(":\n");
			for ( String msg : requestor.getMessages() ) {
				buf.append("\n").append(msg).append("\n");
			}
			String msg = buf.toString();
			log.debug(msg);
			throw new IOException(msg);
		}

		return new MapClassLoader(classMap, compilerClassLoader);
	}

	private ClassLoader compilerClassLoader(Map<String, ?> parameters) {
		if ( parameters != null ) {
			Object param = parameters.get(CLASSLOADER_PARAM);
			if ( param instanceof ClassLoader ) {
				return (ClassLoader) param;
			}
		}
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		if ( cl != null ) {
			return cl;
		}
		return getClass().getClassLoader();
	}

}

/* ==================================================================
 * CollectingCompilerRequestor.java - 27/04/2021 4:24:47 PM
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;

/**
 * Implementation of {@link ICompilerRequestor} that collects the compiler
 * messages and resulting classes.
 * 
 * @author matt
 * @version 1.0
 */
@SuppressWarnings("restriction")
public class CollectingCompilerRequestor implements ICompilerRequestor {

	private final List<String> messages = new ArrayList<>(4);

	private final Map<String, byte[]> classMap = new LinkedHashMap<>(4);

	@Override
	public void acceptResult(CompilationResult result) {
		if ( result.hasProblems() ) {
			IProblem[] problems = result.getProblems();
			for ( IProblem problem : problems ) {
				if ( problem.isError() ) {
					String name = new String(problem.getOriginatingFileName());
					String message = problem.getMessage();
					int lineNumber = problem.getSourceLineNumber();
					String type = (problem.isError() ? "Error"
							: problem.isWarning() ? "Warning" : "Info");
					messages.add(
							String.format("%s compiling %s @ %d: %s", type, name, lineNumber, message));
				}
			}
		}
		if ( !result.hasErrors() ) {
			ClassFile[] classFiles = result.getClassFiles();
			for ( ClassFile classFile : classFiles ) {
				String name = CompilerUtils.nameForType(classFile.getCompoundName());
				byte[] bytes = classFile.getBytes();
				classMap.put(name, bytes);
			}
		}
	}

	/**
	 * Get the collected compiler messages.
	 * 
	 * @return the messages
	 */
	public List<String> getMessages() {
		return messages;
	}

	/**
	 * Get the resulting compiled classes as a Map of class names to associated
	 * bytes.
	 * 
	 * @return the compiled classes
	 */
	public Map<String, byte[]> getClassMap() {
		return classMap;
	}
}

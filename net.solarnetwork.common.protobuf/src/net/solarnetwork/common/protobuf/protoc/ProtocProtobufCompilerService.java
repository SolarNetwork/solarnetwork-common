/* ==================================================================
 * ProtocProtobufCompilerService.java - 20/04/2021 1:19:13 PM
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import net.solarnetwork.common.protobuf.ProtobufCompilerService;
import net.solarnetwork.support.BasicIdentifiable;

/**
 * Implementation of {@link ProtobufCompilerService} that uses the
 * {@literal protoc} command line tool along with the ECJ compiler.
 * 
 * @author matt
 * @version 1.0
 */
public class ProtocProtobufCompilerService extends BasicIdentifiable implements ProtobufCompilerService {

	/** The default {@code protocPath} property value. */
	public static final String DEFAULT_PROTOC_PATH = "/usr/bin/protoc";

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final JavaCompiler compiler;
	private String protocPath = DEFAULT_PROTOC_PATH;

	public ProtocProtobufCompilerService() {
		super();
		this.compiler = compiler();
	}

	@SuppressWarnings("restriction")
	private static JavaCompiler compiler() {
		return new org.eclipse.jdt.internal.compiler.tool.EclipseCompiler();
	}

	/**
	 * Get the compiler instance.
	 * 
	 * @return the compiler
	 */
	public JavaCompiler getJavaCompiler() {
		return compiler;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ClassLoader compileProtobufResources(Iterable<Resource> protobufResources,
			Map<String, ?> parameters) throws IOException {
		final Path tmpDir = Files.createTempDirectory("protoc-");
		final Path protoDir = Files.createDirectory(tmpDir.resolve("proto"));
		final Path javaDir = Files.createDirectory(tmpDir.resolve("gen"));
		List<String> options = null;
		if ( parameters != null && parameters.containsKey("compilerOptions") ) {
			options = (List<String>) parameters.get("compilerOptions");
		}
		try {
			executeProtoc(protobufResources, protoDir, javaDir);
			return compileJava(javaDir, options);
		} finally {
			try (Stream<Path> walk = Files.walk(tmpDir)) {
				walk.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
			}
		}
	}

	private void executeProtoc(Iterable<Resource> protobufResources, Path protoDir, Path javaDir)
			throws IOException {
		final List<Path> protoPaths = new ArrayList<>();
		for ( Resource r : protobufResources ) {
			if ( !r.getFilename().endsWith(".proto") ) {
				continue;
			}
			Path outPath = protoDir.resolve(r.getFilename());
			log.debug("Copying protobuf resource {} -> {}", r.getFilename(), outPath);
			try (OutputStream o = Files.newOutputStream(outPath)) {
				FileCopyUtils.copy(r.getInputStream(), o);
			}
			protoPaths.add(outPath);
		}

		final List<String> cmd = new ArrayList<>(2 + protoPaths.size());
		cmd.add(protocPath);
		cmd.add("--proto_path=" + protoDir.toAbsolutePath().toString());
		cmd.add("--java_out=" + javaDir.toAbsolutePath().toString());
		for ( Path p : protoPaths ) {
			cmd.add(p.toAbsolutePath().toString());
		}

		ProcessBuilder pb = new ProcessBuilder(cmd);
		pb.redirectErrorStream(true);
		Process pr = pb.start();
		try (BufferedReader in = new BufferedReader(new InputStreamReader(pr.getInputStream()))) {
			String line = null;
			while ( (line = in.readLine()) != null ) {
				log.trace("protoc> ", line);
			}
		}
		try {
			pr.waitFor();
		} catch ( InterruptedException e ) {
			log.warn("Interrupted waiting for tar command to complete");
		}
		if ( pr.exitValue() != 0 ) {
			log.error("protoc command returned non-zero exit code {}", pr.exitValue());
			throw new IOException("protoc command returned non-zero exit code " + pr.exitValue());
		}
	}

	private ClassLoader compileJava(Path javaDir, List<String> compileOptions) throws IOException {
		final List<JavaFileObject> sources;
		try (Stream<Path> walk = Files.walk(javaDir)) {
			sources = walk.filter(p -> p.getFileName().toString().endsWith(".java"))
					.map(p -> new PathJavaSource(p)).collect(Collectors.toList());
		}

		final List<ByteArrayJavaClass> classFileObjects = new ArrayList<>();

		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

		try (JavaFileManager fileManager = new ForwardingJavaFileManager<JavaFileManager>(
				compiler.getStandardFileManager(diagnostics, null, null)) {

			@Override
			public JavaFileObject getJavaFileForOutput(Location location, final String className,
					JavaFileObject.Kind kind, FileObject sibling) throws IOException {
				ByteArrayJavaClass fileObject = new ByteArrayJavaClass(className);
				classFileObjects.add(fileObject);
				return fileObject;
			}

		}) {
			StringWriter out = new StringWriter();
			JavaCompiler.CompilationTask task = compiler.getTask(out, fileManager, diagnostics,
					compileOptions, null, sources);
			Boolean result = task.call();
			StringBuilder buf = new StringBuilder();
			if ( !result ) {
				buf.append("Error compiling Protobuf Java sources: ");
				buf.append(sources.stream().map(e -> e.getName()).collect(Collectors.toList()));
				buf.append("\n\n");
				String outMessage = out.toString();
				if ( !outMessage.isEmpty() ) {
					if ( !result ) {
						buf.append(outMessage).append("\n");
					}
				}
				if ( !diagnostics.getDiagnostics().isEmpty() ) {
					String fmt = "%7s: %s @ %d: %s\n";
					for ( Diagnostic<? extends JavaFileObject> d : diagnostics.getDiagnostics() ) {
						buf.append(String.format(fmt, d.getKind(), d.getSource().getName(),
								d.getLineNumber(), d.getMessage(null)));
					}
				}
				log.error(buf.toString());
			}
			if ( !result ) {
				throw new IOException(buf.toString());
			}
			Map<String, byte[]> byteCodeMap = new HashMap<>();
			for ( ByteArrayJavaClass cl : classFileObjects ) {
				byteCodeMap.put(cl.getName().substring(1).replace('/', '.'), cl.getBytes());
			}
			return new MapClassLoader(byteCodeMap);
		}
	}

	/**
	 * Get the file system path to the {@literal protoc} binary.
	 * 
	 * @return the path; defaults to {@link #DEFAULT_PROTOC_PATH}
	 */
	public String getProtocPath() {
		return protocPath;
	}

	/**
	 * Set the file system path to the {@literal protoc} binary.
	 * 
	 * @param protocPath
	 *        the protocPath to set
	 */
	public void setProtocPath(String protocPath) {
		this.protocPath = protocPath;
	}

}

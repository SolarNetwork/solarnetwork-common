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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import net.solarnetwork.common.protobuf.ProtobufCompilerService;
import net.solarnetwork.support.BasicIdentifiable;
import net.solarnetwork.util.JavaCompiler;

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

	/**
	 * Constructor.
	 * 
	 * @param compiler
	 *        the compiler
	 */
	public ProtocProtobufCompilerService(JavaCompiler compiler) {
		super();
		this.compiler = compiler;
	}

	/**
	 * Get the compiler instance.
	 * 
	 * @return the compiler
	 */
	public JavaCompiler getJavaCompiler() {
		return compiler;
	}

	@Override
	public ClassLoader compileProtobufResources(Iterable<Resource> protobufResources,
			Map<String, ?> parameters) throws IOException {
		final Path tmpDir = Files.createTempDirectory("protoc-");
		final Path protoDir = Files.createDirectory(tmpDir.resolve("proto"));
		final Path javaDir = Files.createDirectory(tmpDir.resolve("gen"));
		try {
			executeProtoc(protobufResources, protoDir, javaDir);
			return compileJava(javaDir, parameters);
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

	private ClassLoader compileJava(Path javaDir, Map<String, ?> parameters) throws IOException {
		final List<Resource> sources;
		try (Stream<Path> walk = Files.walk(javaDir)) {
			sources = walk.filter(p -> p.getFileName().toString().endsWith(".java"))
					.map(p -> new FileSystemResource(p.toFile())).collect(Collectors.toList());
		}

		return compiler.compileResources(sources, javaDir, parameters);
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

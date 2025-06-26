/* ==================================================================
 * JdtJavaCompilerTests.java - 27/04/2021 4:50:45 PM
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

package net.solarnetwork.common.jdt.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import net.solarnetwork.common.jdt.JdtJavaCompiler;

/**
 * Test cases for the {@link JdtJavaCompiler} class.
 *
 * @author matt
 * @version 1.0
 */
public class JdtJavaCompilerTests {

	private Path tmpDir;
	private Path pkgDir;
	private JdtJavaCompiler compiler;

	@Before
	public void setup() throws IOException {
		compiler = new JdtJavaCompiler();

		tmpDir = Files.createTempDirectory(String.format("%s-", getClass().getSimpleName()));
		pkgDir = tmpDir.resolve("net/solarnetwork/common/jdt/test");
		Files.createDirectories(pkgDir);
	}

	@After
	public void teardown() {
		if ( tmpDir != null ) {
			try {
				try (Stream<Path> walk = Files.walk(tmpDir)) {
					walk.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
				}
			} catch ( IOException e ) {
				// ignore
			}
		}
	}

	@Test
	public void compile_multi() throws Exception {
		// GIVEN
		Path javaSource = pkgDir.resolve("Hello.java");
		FileCopyUtils.copy(new ClassPathResource("Hello.java.txt", getClass()).getInputStream(),
				new FileOutputStream(javaSource.toFile()));
		Resource testJavaSource = new FileSystemResource(javaSource.toFile());

		Path rootJavaSource = tmpDir.resolve("Root.java");
		FileCopyUtils.copy(new ClassPathResource("Root.java.txt", getClass()).getInputStream(),
				new FileOutputStream(rootJavaSource.toFile()));
		Resource testRootJavaSource = new FileSystemResource(rootJavaSource.toFile());

		// WHEN
		List<Resource> rsrcs = Arrays.asList(testJavaSource, testRootJavaSource);
		ClassLoader result = compiler.compileResources(rsrcs, tmpDir, null);

		// THEN
		assertThat("Result available", result, notNullValue());

		Class<?> clazz = result.loadClass("net.solarnetwork.common.jdt.test.Hello");
		Method helloMethod = clazz.getDeclaredMethod("hello");
		Object response = helloMethod.invoke(clazz.getDeclaredConstructor().newInstance());
		assertThat("Greeting received", response, equalTo("world"));

		clazz = result.loadClass("Root");
		helloMethod = clazz.getDeclaredMethod("hello");
		response = helloMethod.invoke(clazz.getDeclaredConstructor().newInstance());
		assertThat("Greeting received", response, equalTo("I am root."));
	}

}

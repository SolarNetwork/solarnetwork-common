/* ==================================================================
 * ResourceCompilationUnitTests.java - 27/04/2021 3:33:23 PM
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

import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import net.solarnetwork.common.jdt.ResourceCompilationUnit;

/**
 * Test cases for the {@link ResourceCompilationUnit} class.
 * 
 * @author matt
 * @version 1.0
 */
public class ResourceCompilationUnitTests {

	private Path tmpDir;
	private Resource testJavaSource;
	private Resource testRootJavaSource;

	@Before
	public void setup() throws IOException {
		tmpDir = Files.createTempDirectory(String.format("%s-", getClass().getSimpleName()));
		Path pkgDir = tmpDir.resolve("net/solarnetwork/common/jdt/test");
		Files.createDirectories(pkgDir);

		Path javaSource = pkgDir.resolve("Hello.java");
		FileCopyUtils.copy(new ClassPathResource("Hello.java.txt", getClass()).getInputStream(),
				new FileOutputStream(javaSource.toFile()));
		testJavaSource = new FileSystemResource(javaSource.toFile());

		Path rootJavaSource = tmpDir.resolve("Root.java");
		FileCopyUtils.copy(new ClassPathResource("Root.java.txt", getClass()).getInputStream(),
				new FileOutputStream(rootJavaSource.toFile()));
		testRootJavaSource = new FileSystemResource(rootJavaSource.toFile());
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
	public void packageName() {
		// GIVEN
		ResourceCompilationUnit unit = new ResourceCompilationUnit(testJavaSource, tmpDir);

		// WHEN
		char[][] packages = unit.getPackageName();

		// THEN
		assertThat("Packages returned", packages, arrayWithSize(5));
		for ( int i = 0; i < 5; i++ ) {
			String expected;
			switch (i) {
				case 0:
					expected = "net";
					break;

				case 1:
					expected = "solarnetwork";
					break;

				case 2:
					expected = "common";
					break;

				case 3:
					expected = "jdt";
					break;

				case 4:
					expected = "test";
					break;

				default:
					throw new IllegalArgumentException("Should not be be here.");
			}
			assertThat("Package " + i, new String(packages[i]), equalTo(expected));
		}
	}

	@Test
	public void packageName_default() {
		// GIVEN
		ResourceCompilationUnit unit = new ResourceCompilationUnit(testRootJavaSource, tmpDir);

		// WHEN
		char[][] packages = unit.getPackageName();

		// THEN
		assertThat("Packages returned", packages, arrayWithSize(0));
	}

	@Test
	public void mainTypeName() {
		// GIVEN
		ResourceCompilationUnit unit = new ResourceCompilationUnit(testJavaSource, tmpDir);

		// WHEN
		char[] name = unit.getMainTypeName();

		// THEN
		assertThat("Main type name", new String(name), equalTo("Hello"));
	}

	@Test
	public void contents() throws IOException {
		// GIVEN
		ResourceCompilationUnit unit = new ResourceCompilationUnit(testRootJavaSource, tmpDir);

		// WHEN
		char[] content = unit.getContents();

		// THEN
		String expected = FileCopyUtils.copyToString(new InputStreamReader(
				new ClassPathResource("Root.java.txt", getClass()).getInputStream()));
		assertThat("Content", new String(content), equalTo(expected));
	}

	@Test
	public void filename() {
		// GIVEN
		ResourceCompilationUnit unit = new ResourceCompilationUnit(testJavaSource, tmpDir);

		// WHEN
		char[] name = unit.getFileName();

		// THEN
		assertThat("Content", new String(name), equalTo("Hello.java"));
	}

}

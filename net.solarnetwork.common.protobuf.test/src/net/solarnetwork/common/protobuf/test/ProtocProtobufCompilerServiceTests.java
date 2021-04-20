/* ==================================================================
 * ProtocProtobufCompilerServiceTests.java - 20/04/2021 1:24:31 PM
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

package net.solarnetwork.common.protobuf.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javax.lang.model.SourceVersion;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import net.solarnetwork.common.protobuf.protoc.ProtocProtobufCompilerService;
import net.solarnetwork.test.SystemPropertyMatchTestRule;

/**
 * Test cases for the {@link ProtocProtobufCompilerService} class.
 * 
 * @author matt
 * @version 1.0
 */
public class ProtocProtobufCompilerServiceTests {

	/** Only run when the {@code protoc-int} system property is defined. */
	@ClassRule
	public static SystemPropertyMatchTestRule PROFILE_RULE = new SystemPropertyMatchTestRule(
			"protoc-int");

	private static Properties TEST_PROPS;

	private final Logger log = LoggerFactory.getLogger(getClass());

	private ProtocProtobufCompilerService service;

	@BeforeClass
	public static void setupClass() {
		Properties p = new Properties();
		try {
			InputStream in = ProtocProtobufCompilerServiceTests.class.getClassLoader()
					.getResourceAsStream("protobuf.properties");
			if ( in != null ) {
				p.load(in);
				in.close();
			}
		} catch ( IOException e ) {
			throw new RuntimeException(e);
		}
		TEST_PROPS = p;
	}

	@Before
	public void setup() {
		service = new ProtocProtobufCompilerService();
		if ( TEST_PROPS.containsKey("protoc.path") ) {
			service.setProtocPath(TEST_PROPS.getProperty("protoc.path"));
		}
	}

	@Test
	public void printVersions() {
		Set<SourceVersion> supportedJavaTargets = service.getJavaCompiler().getSourceVersions();
		log.info("Supported compiler versions: {}", supportedJavaTargets);
	}

	@Test
	public void compile() throws IOException {
		// GIVEN
		List<Resource> protos = Arrays.asList(new ClassPathResource("dinosaur.proto", getClass()),
				new ClassPathResource("period.proto", getClass()));
		// WHEN
		ClassLoader cl = service.compileProtobufResources(protos, null);

		// THEN
		// TODO
	}

}

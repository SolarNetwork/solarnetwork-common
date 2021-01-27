/* ==================================================================
 * ClassUtilsTests.java - 25/09/2017 9:44:13 AM
 * 
 * Copyright 2017 SolarNetwork.net Dev Team
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

package net.solarnetwork.util.test;

import static org.junit.Assert.assertThat;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import net.solarnetwork.domain.Location;
import net.solarnetwork.domain.Request;
import net.solarnetwork.domain.SimpleLocation;
import net.solarnetwork.util.ClassUtils;

/**
 * Test cases for the {@link ClassUtils} class.
 * 
 * @author matt
 * @version 1.1
 */
public class ClassUtilsTests {

	private static final class SerializableSimpleLocation extends SimpleLocation
			implements Serializable {

		private static final long serialVersionUID = -2811665308323747576L;

	}

	public interface SerializableLocation extends Location, Serializable {

	}

	private static final class SimpleSerializableLocation extends SimpleLocation
			implements SerializableLocation {

		private static final long serialVersionUID = -2811665308323747576L;

	}

	@Test
	public void getAllInterfacesExcludingJavaPackagesBasic() {
		Set<Class<?>> interfaces = ClassUtils
				.getAllNonJavaInterfacesForClassAsSet(SerializableSimpleLocation.class);
		Assert.assertEquals(Collections.singleton(Location.class), interfaces);
	}

	@Test
	public void getAllInterfacesExcludingJavaPackagesSuperInterface() {
		Set<Class<?>> interfaces = ClassUtils
				.getAllNonJavaInterfacesForClassAsSet(SimpleSerializableLocation.class);
		Assert.assertEquals(
				new LinkedHashSet<Class<?>>(
						Arrays.<Class<?>> asList(SerializableLocation.class, Location.class)),
				interfaces);
	}

	@Test
	public void getAllInterfacesExcludingJavaPackagesNoInterface() {
		Set<Class<?>> interfaces = ClassUtils.getAllNonJavaInterfacesForClassAsSet(Request.class);
		Assert.assertEquals(Collections.emptySet(), interfaces);
	}

	@Test
	public void getResourceAsString() {
		// WHEN
		String s = ClassUtils.getResourceAsString("test-file.txt", getClass());

		// THEN
		assertThat("File loaded", s, Matchers.equalTo("Hello, world.\nGoodbye."));
	}

	/** Regex for a line starting with a {@literal #} comment character. */
	public static final Pattern HASH_COMMENT = Pattern.compile("^\\s*#");

	@Test
	public void getResourceAsString_skipComments() {
		// WHEN
		String s = ClassUtils.getResourceAsString("test-file-with-comments.txt", getClass(),
				HASH_COMMENT);

		// THEN
		assertThat("File loaded", s, Matchers.equalTo("Hello, world.\nGoodbye."));
	}

}

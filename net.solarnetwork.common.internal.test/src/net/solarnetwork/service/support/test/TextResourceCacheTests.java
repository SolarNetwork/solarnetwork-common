/* ==================================================================
 * TextResourceCacheTests.java - 8/12/2020 4:55:34 pm
 * 
 * Copyright 2020 SolarNetwork.net Dev Team
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

package net.solarnetwork.service.support.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import java.util.Collections;
import org.junit.Test;
import net.solarnetwork.service.support.TextResourceCache;
import net.solarnetwork.util.test.ClassUtilsTests;

/**
 * Test cases for the {@link TextResourceCache} class.
 * 
 * @author matt
 * @version 1.0
 */
public class TextResourceCacheTests {

	@Test
	public void load() {
		// WHEN
		String s = new TextResourceCache().getResourceAsString("test-file-with-comments.txt",
				ClassUtilsTests.class, ClassUtilsTests.HASH_COMMENT);

		// THEN
		assertThat("String loaded", s, equalTo("Hello, world.\nGoodbye."));
	}

	@Test
	public void loadWithTemplates() {
		// WHEN
		String s = new TextResourceCache().getResourceAsString("test-file-with-parameters.txt",
				getClass(), Collections.singletonMap("name", "Bob"));

		// THEN
		assertThat("String loaded with template parameters", s, equalTo("Hello, Bob."));
	}

	@Test
	public void loadWithTemplates_varyParameters() {
		// GIVEN
		TextResourceCache cache = new TextResourceCache();

		// WHEN
		String s1 = cache.getResourceAsString("test-file-with-parameters.txt", getClass(),
				Collections.singletonMap("name", "Bob"));
		String s2 = cache.getResourceAsString("test-file-with-parameters.txt", getClass(),
				Collections.singletonMap("name", "Joe"));

		// THEN
		assertThat("String loaded with template parameters", s1, equalTo("Hello, Bob."));
		assertThat("String loaded with different template parameters", s2, equalTo("Hello, Joe."));
	}

	@Test
	public void loadWithTemplates_varyParameters_repeat() {
		// GIVEN
		TextResourceCache cache = new TextResourceCache();

		for ( int i = 0; i < 5; i++ ) {
			// WHEN
			String s1 = cache.getResourceAsString("test-file-with-parameters.txt", getClass(),
					Collections.singletonMap("name", "Bob"));
			String s2 = cache.getResourceAsString("test-file-with-parameters.txt", getClass(),
					Collections.singletonMap("name", "Joe"));

			// THEN
			assertThat("String loaded with template parameters @ " + i, s1, equalTo("Hello, Bob."));
			assertThat("String loaded with different template parameters @ " + i, s2,
					equalTo("Hello, Joe."));
		}
	}
}

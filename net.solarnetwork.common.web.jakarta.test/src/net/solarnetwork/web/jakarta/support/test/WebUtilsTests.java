/* ==================================================================
 * WebUtilsTests.java - 22 Oct 2026 2:01:28 pm
 *
 * Copyright 2026 SolarNetwork.net Dev Team
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

package net.solarnetwork.web.jakarta.support.test;

import static net.solarnetwork.test.CommonTestUtils.randomString;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenIllegalStateException;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import net.solarnetwork.web.jakarta.support.WebUtils;

/**
 * Test cases for the {@link WebUtils} class.
 *
 * @author matt
 * @version 1.0
 */
public class WebUtilsTests {

	@Test
	public void resolveViewFromUrlExtension_fromUri() {
		// GIVEN
		final var request = new MockHttpServletRequest("GET", "http://loalhost/foo.xml");

		// WHEN
		final String result = WebUtils.resolveViewFromUrlExtension(request, null);

		// THEN
		// @formatter:off
		then(result)
			.as("View name resolved from URI")
			.isNotNull()
			.as("View name is resolved as URI file extension")
			.isEqualTo("xml")
			;
		// @formatter:on
	}

	@Test
	public void resolveViewFromUrlExtension_fromUri_noExtension() {
		// GIVEN
		final var request = new MockHttpServletRequest("GET", "http://loalhost/foo");

		// @formatter:off
		thenIllegalStateException()
			.as("IllegalState thrown if URI path has no extension")
			.isThrownBy(() -> {
				WebUtils.resolveViewFromUrlExtension(request, null);
			})
			;
		// @formatter:on
	}

	@Test
	public void resolveViewFromUrlExtension_fromViewName() {
		// GIVEN
		final var request = new MockHttpServletRequest("GET", "http://loalhost/foo.xml");
		final var viewName = randomString();

		// WHEN
		final String result = WebUtils.resolveViewFromUrlExtension(request, viewName);

		// THEN
		// @formatter:off
		then(result)
			.as("View name is resolved as direct view name given")
			.isEqualTo(viewName)
			;
		// @formatter:on
	}

}

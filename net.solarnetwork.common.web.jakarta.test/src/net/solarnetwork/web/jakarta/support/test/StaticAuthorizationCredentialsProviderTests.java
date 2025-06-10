/* ==================================================================
 * StaticAuthorizationCredentialsProviderTests.java - 13/08/2019 10:51:53 am
 * 
 * Copyright 2019 SolarNetwork.net Dev Team
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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Test;
import net.solarnetwork.web.jakarta.support.StaticAuthorizationCredentialsProvider;

/**
 * Test cases for the {@link StaticAuthorizationCredentialsProviderTests} class.
 * 
 * @author matt
 * @version 1.0
 */
public class StaticAuthorizationCredentialsProviderTests {

	@Test
	public void accessors() {
		// given
		final String id = "foo";
		final String s = "bar";

		// when
		StaticAuthorizationCredentialsProvider p = new StaticAuthorizationCredentialsProvider(id, s);

		// then
		assertThat("Auth ID", p.getAuthorizationId(), equalTo(id));
		assertThat("Auth secret", p.getAuthorizationSecret(), equalTo(s));
	}

}

/* ==================================================================
 * SnsAuthorizationInfoTests.java - 16/08/2021 9:14:58 AM
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

package net.solarnetwork.security.test;

import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import net.solarnetwork.security.SnsAuthorizationInfo;

/**
 * Test cases for the {@link SnsAuthorizationInfo} class.
 * 
 * @author matt
 * @version 1.0
 */
public class SnsAuthorizationInfoTests {

	@Test
	public void parseAuthorization_ok() {
		// GIVEN
		String auth = "SNS Credential=foo,SignedHeaders=date;destination,Signature=bar";

		// WHEN
		SnsAuthorizationInfo info = SnsAuthorizationInfo.forAuthorizationHeader(auth);

		// THEN
		assertThat("Info returned", info, is(notNullValue()));
		assertThat("Info scheme parsed", info.getScheme(), is("SNS"));
		assertThat("Info credential parsed", info.getIdentifier(), is("foo"));
		assertThat("Info headers parsed", info.getHeaderNames(),
				is(arrayContaining("date", "destination")));
		assertThat("Info signature parsed", info.getSignature(), is("bar"));
	}

	@Test
	public void parseAuthorization_ok_otherOrder() {
		// GIVEN
		String auth = "SNS Signature=bar,Credential=foo,SignedHeaders=date;destination";

		// WHEN
		SnsAuthorizationInfo info = SnsAuthorizationInfo.forAuthorizationHeader(auth);

		// THEN
		assertThat("Info returned", info, is(notNullValue()));
		assertThat("Info scheme parsed", info.getScheme(), is("SNS"));
		assertThat("Info credential parsed", info.getIdentifier(), is("foo"));
		assertThat("Info headers parsed", info.getHeaderNames(),
				is(arrayContaining("date", "destination")));
		assertThat("Info signature parsed", info.getSignature(), is("bar"));
	}

}

/* ==================================================================
 * ChargePointSessionIdentityTests.java - 4/07/2024 10:51:53 am
 *
 * Copyright 2024 SolarNetwork.net Dev Team
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

package net.solarnetwork.ocpp.domain.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import org.junit.Test;
import net.solarnetwork.ocpp.domain.ChargePointIdentity;
import net.solarnetwork.ocpp.domain.ChargePointSessionIdentity;

/**
 * Test cases for the {@link ChargePointSessionIdentity} class.
 *
 * @author matt
 * @version 1.0
 */
public class ChargePointSessionIdentityTests {

	@Test
	public void equals() {
		// GIVEN
		ChargePointSessionIdentity a = new ChargePointSessionIdentity(new ChargePointIdentity("a", 1L),
				"a");
		ChargePointSessionIdentity b = new ChargePointSessionIdentity(new ChargePointIdentity("a", 1L),
				"a");

		// THEN
		assertThat("Objects are equal when identity and session ID are equal", a, is(equalTo(b)));
	}

	@Test
	public void notEquals_session() {
		// GIVEN
		ChargePointSessionIdentity a = new ChargePointSessionIdentity(new ChargePointIdentity("a", 1L),
				"a");
		ChargePointSessionIdentity b = new ChargePointSessionIdentity(a.getIdentity(), "b");

		// THEN
		assertThat("Objects are not equal when session IDs differ", a, is(not(equalTo(b))));
	}

	@Test
	public void notEquals_identity() {
		// GIVEN
		ChargePointSessionIdentity a = new ChargePointSessionIdentity(new ChargePointIdentity("a", 1L),
				"a");
		ChargePointSessionIdentity b = new ChargePointSessionIdentity(new ChargePointIdentity("b", 1L),
				"a");

		// THEN
		assertThat("Objects are not equal when identities differ", a, is(not(equalTo(b))));
	}

	@Test
	public void compare_equals() {
		// GIVEN
		ChargePointSessionIdentity a = new ChargePointSessionIdentity(new ChargePointIdentity("a", 1L),
				"a");
		ChargePointSessionIdentity b = new ChargePointSessionIdentity(new ChargePointIdentity("a", 1L),
				"a");

		// THEN
		assertThat("Objects compare equally when equal", a.compareTo(b), is(equalTo(0)));
		assertThat("Objects inverse compare equally when equal", b.compareTo(a), is(equalTo(0)));
	}

	@Test
	public void compare_less_session() {
		// GIVEN
		ChargePointSessionIdentity a = new ChargePointSessionIdentity(new ChargePointIdentity("a", 1L),
				"a");
		ChargePointSessionIdentity b = new ChargePointSessionIdentity(a.getIdentity(), "b");

		// THEN
		assertThat("Objects compare less than based on session ID", a.compareTo(b), is(equalTo(-1)));
		assertThat("Objects inverse compare greater than based on session ID", b.compareTo(a),
				is(equalTo(1)));
	}

	@Test
	public void identifier() {
		// GIVEN
		final ChargePointIdentity ident = new ChargePointIdentity("a", 1L);
		ChargePointSessionIdentity a = new ChargePointSessionIdentity(ident, "a");

		// THEN
		assertThat("Identifier from identity returned", a.getIdentifier(),
				is(sameInstance(ident.getIdentifier())));
	}

	@Test
	public void userIdentifier() {
		// GIVEN
		final ChargePointIdentity ident = new ChargePointIdentity("a", 1L);
		ChargePointSessionIdentity a = new ChargePointSessionIdentity(ident, "a");

		// THEN
		assertThat("User identifier from identity returned", a.getUserIdentifier(),
				is(sameInstance(ident.getUserIdentifier())));
	}

	@Test
	public void boundary() {
		// GIVEN
		final ChargePointIdentity ident = new ChargePointIdentity("a", 1L);
		ChargePointSessionIdentity boundary = ChargePointSessionIdentity.boundaryKey(ident);

		// THEN
		assertThat("Boundary object has given identity", boundary.getIdentity(),
				is(sameInstance(ident)));
		assertThat("Boundary object has empty session ID", boundary.getSessionId(), is(equalTo("")));
	}

	@Test
	public void boundary_compare_less_session() {
		// GIVEN
		ChargePointSessionIdentity a = new ChargePointSessionIdentity(new ChargePointIdentity("a", 1L),
				"a");
		ChargePointSessionIdentity b = new ChargePointSessionIdentity(a.getIdentity(), "b");

		// THEN
		assertThat("Objects compare less than based on session ID", a.compareTo(b), is(equalTo(-1)));
		assertThat("Objects inverse compare greater than based on session ID", b.compareTo(a),
				is(equalTo(1)));
	}

}

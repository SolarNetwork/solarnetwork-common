/* ==================================================================
 * PingTest.java - 25/05/2015 10:20:29 am
 * 
 * Copyright 2007-2015 SolarNetwork.net Dev Team
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

package net.solarnetwork.service;

import java.util.Map;

/**
 * API for a service that be used to verify the status of some specific part of
 * the SolarNetwork system.
 * 
 * @author matt
 * @version 1.0
 * @since 1.52
 */
public interface PingTest {

	/**
	 * Ping test result API.
	 */
	interface Result {

		/**
		 * Test if the test was successful.
		 * 
		 * @return {@literal true} if the test is successful (passed),
		 *         {@literal false} otherwise
		 */
		boolean isSuccess();

		/**
		 * Get a descriptive message
		 * 
		 * @return the message
		 */
		String getMessage();

		/**
		 * Get optional properties related to the test.
		 * 
		 * @return optional test properties
		 */
		Map<String, ?> getProperties();

	}

	/**
	 * Get some globally-unique ID for this test instance.
	 * 
	 * @return The globally-unique ID of this test instance.
	 */
	String getPingTestId();

	/**
	 * Get display name for this test.
	 * 
	 * @return The name of the test.
	 */
	String getPingTestName();

	/**
	 * Get the maximum number of milliseconds to wait for the ping test to
	 * execute before considering the test a failure.
	 * 
	 * @return The maximum execution milliseconds.
	 */
	long getPingTestMaximumExecutionMilliseconds();

	/**
	 * Perform the test, and return the results of the test.
	 * 
	 * @throws Exception
	 *         if any error occurs
	 * @return The test results.
	 */
	Result performPingTest() throws Exception;

}

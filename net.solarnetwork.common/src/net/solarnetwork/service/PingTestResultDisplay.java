/* ==================================================================
 * PingTestResultDisplay.java - 25/05/2015 10:40:07 am
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

import static net.solarnetwork.util.ObjectUtils.requireNonNullArgument;
import java.time.Duration;
import java.time.Instant;

/**
 * Extension of {@link PingTestResult} to support a UI layer.
 * 
 * @author matt
 * @version 2.0
 * @since 1.52
 */
public class PingTestResultDisplay extends PingTestResult {

	private final String pingTestId;
	private final String pingTestName;
	private final Instant start;
	private final Instant end;

	/**
	 * Construct from a test and a result.
	 * 
	 * @param test
	 *        The test.
	 * @param result
	 *        The result.
	 * @param start
	 *        The time the test started.
	 * @throws IllegalArgumentException
	 *         if any argument is {@literal null}
	 */
	public PingTestResultDisplay(PingTest test, PingTest.Result result, Instant start) {
		super(requireNonNullArgument(result, "result").isSuccess(), result.getMessage(),
				result.getProperties());
		this.pingTestId = requireNonNullArgument(test, "test").getPingTestId();
		this.pingTestName = test.getPingTestName();
		this.start = requireNonNullArgument(start, "start");
		this.end = Instant.now();
	}

	public String getPingTestId() {
		return pingTestId;
	}

	public String getPingTestName() {
		return pingTestName;
	}

	public Instant getStart() {
		return start;
	}

	public Instant getEnd() {
		return end;
	}

	public Duration getDuration() {
		return Duration.between(start, end);
	}

}
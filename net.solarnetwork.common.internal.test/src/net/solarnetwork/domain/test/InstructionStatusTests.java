/* ==================================================================
 * InstructionStatusTests.java - 10/07/2026 12:48:53 pm
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

package net.solarnetwork.domain.test;

import static org.assertj.core.api.BDDAssertions.from;
import static org.assertj.core.api.BDDAssertions.then;
import java.time.Instant;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.junit.Test;
import net.solarnetwork.domain.InstructionStatus;
import net.solarnetwork.domain.InstructionStatus.InstructionState;

/**
 * Test cases for the {@link InstructionStatus} interface.
 *
 * @author matt
 * @version 1.0
 */
public class InstructionStatusTests {

	private static class TestInstructionStatus implements InstructionStatus {

		private final InstructionState instructionState;

		private TestInstructionStatus(InstructionState instructionState) {
			super();
			this.instructionState = instructionState;
		}

		@Override
		public @Nullable Long getInstructionId() {
			return null;
		}

		@Override
		public InstructionState getInstructionState() {
			return instructionState;
		}

		@Override
		public @Nullable Instant getStatusDate() {
			return null;
		}

		@Override
		public @Nullable Map<String, ?> getResultParameters() {
			return null;
		}

		@Override
		public InstructionStatus newCopyWithState(InstructionState newState,
				@Nullable Map<String, ?> resultParameters) {
			return null;
		}

	}

	@Test
	public void isDone_completed() {
		// @formatter:off
		then(new TestInstructionStatus(InstructionState.Completed))
			.as("Completed state is done")
			.returns(true, from(InstructionStatus::isDone))
			.as("Completed state is complete")
			.returns(true, from(InstructionStatus::isCompleted))
			;
		// @formatter:on
	}

	@Test
	public void isDone_declined() {
		// @formatter:off
		then(new TestInstructionStatus(InstructionState.Declined))
			.as("Declined state is done")
			.returns(true, from(InstructionStatus::isDone))
			.as("Declined state is not complete")
			.returns(false, from(InstructionStatus::isCompleted))
			;
		// @formatter:on
	}

	@Test
	public void isDone_unknown() {
		// @formatter:off
		then(new TestInstructionStatus(InstructionState.Unknown))
			.as("Unknown state is done")
			.returns(false, from(InstructionStatus::isDone))
			.as("Unknown state is not complete")
			.returns(false, from(InstructionStatus::isCompleted))
			;
		// @formatter:on
	}

	@Test
	public void isDone_received() {
		// @formatter:off
		then(new TestInstructionStatus(InstructionState.Received))
			.as("Received state is done")
			.returns(false, from(InstructionStatus::isDone))
			.as("Receivedw state is not complete")
			.returns(false, from(InstructionStatus::isCompleted))
			;
		// @formatter:on
	}

}

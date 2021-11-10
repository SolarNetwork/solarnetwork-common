/* ==================================================================
 * InstructionStatus.java - Feb 28, 2011 10:50:38 AM
 * 
 * Copyright 2007 SolarNetwork.net Dev Team
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

package net.solarnetwork.domain;

import java.time.Instant;
import java.util.Map;

/**
 * Status information for a single Instruction.
 * 
 * @author matt
 * @version 1.0
 * @since 2.0
 */
public interface InstructionStatus {

	/**
	 * An enumeration of instruction states.
	 */
	enum InstructionState {

		/**
		 * The instruction state is not known.
		 */
		Unknown,

		/**
		 * The instruction has been queued on the sender for the recipient.
		 */
		Queued,

		/**
		 * The instruction is in the process of being queued, potentially
		 * jumping to the received state if an immediate acknowledgement is
		 * possible.
		 */
		Queuing,

		/**
		 * The recipient has acknowledged receipt of the instruction, but has
		 * not been processed yet. It will be processed at as soon as possible.
		 */
		Received,

		/**
		 * The instruction has been received and is being executed by the
		 * recipient currently.
		 */
		Executing,

		/**
		 * The instruction was received but the recipient has declined to
		 * execute the instruction.
		 */
		Declined,

		/**
		 * The instruction was received and has been executed.
		 */
		Completed;

	}

	/**
	 * A standard result parameter key for a message (typically an error
	 * message).
	 */
	String MESSAGE_RESULT_PARAM = "message";

	/** A standard result parameter key for an error code. */
	String ERROR_CODE_RESULT_PARAM = "code";

	/**
	 * Get the ID of the instruction this state is associated with.
	 * 
	 * @return the primary key
	 */
	Long getInstructionId();

	/**
	 * Get the current instruction state.
	 * 
	 * @return the current instruction state
	 */
	InstructionState getInstructionState();

	/**
	 * Get the date/time the instruction state was queried.
	 * 
	 * @return the status date
	 */
	Instant getStatusDate();

	/**
	 * Get result parameters.
	 * 
	 * @return the result parameters, or {@literal null} if none available
	 */
	Map<String, ?> getResultParameters();

	/**
	 * Create a new InstructionStatus copy with a new state.
	 * 
	 * @param newState
	 *        the new state
	 * @return the new instance
	 */
	default InstructionStatus newCopyWithState(InstructionState newState) {
		return newCopyWithState(newState, null);
	}

	/**
	 * Create a new InstructionStatus copy with a new state and result
	 * parameters.
	 * 
	 * @param newState
	 *        the new state
	 * @param resultParameters
	 *        the result parameters
	 * @return the new instance
	 * @since 1.1
	 */
	InstructionStatus newCopyWithState(InstructionState newState, Map<String, ?> resultParameters);

	/**
	 * Create a new status for a given instruction.
	 * 
	 * @param instruction
	 *        the instruction, or {@literal null}
	 * @param state
	 *        the new state
	 * @param date
	 *        the status date
	 * @param resultParameters
	 *        the optional result parameters
	 * @return the status, never {@literal null}
	 */
	static InstructionStatus createStatus(Instruction instruction, InstructionState state, Instant date,
			Map<String, ?> resultParameters) {
		final InstructionStatus status = (instruction != null ? instruction.getStatus() : null);
		return (status != null ? status.newCopyWithState(state, resultParameters)
				: new BasicInstructionStatus(instruction != null ? instruction.getId() : null, state,
						date != null ? date : Instant.now(), resultParameters));
	}

}

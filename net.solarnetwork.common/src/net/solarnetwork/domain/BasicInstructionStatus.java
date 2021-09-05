/* ==================================================================
 * BasicInstructionStatus.java - Feb 28, 2011 11:28:09 AM
 * 
 * Copyright 2007-2011 SolarNetwork.net Dev Team
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

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Basic implementation of {@link InstructionStatus}.
 * 
 * @author matt
 * @version 1.0
 * @since 2.0
 */
public class BasicInstructionStatus implements InstructionStatus, Serializable {

	private static final long serialVersionUID = 5423487100585905801L;

	private final Long instructionId;
	private final InstructionState instructionState;
	private final Instant statusDate;
	private final Map<String, ?> resultParameters;

	/**
	 * Constructor.
	 * 
	 * @param instructionId
	 *        the instruction ID
	 * @param instructionState
	 *        the instruction state
	 * @param statusDate
	 *        the status date
	 */
	public BasicInstructionStatus(Long instructionId, InstructionState instructionState,
			Instant statusDate) {
		this(instructionId, instructionState, statusDate, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param instructionId
	 *        the instruction ID
	 * @param instructionState
	 *        the instruction state
	 * @param statusDate
	 *        the status date
	 * @param resultParameters
	 *        the result parameters
	 */
	public BasicInstructionStatus(Long instructionId, InstructionState instructionState,
			Instant statusDate, Map<String, ?> resultParameters) {
		this.instructionId = instructionId;
		this.instructionState = instructionState;
		this.statusDate = statusDate;
		this.resultParameters = resultParameters;
	}

	@Override
	public int hashCode() {
		return Objects.hash(instructionId, instructionState, resultParameters, statusDate);
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj )
			return true;
		if ( obj == null )
			return false;
		if ( getClass() != obj.getClass() )
			return false;
		BasicInstructionStatus other = (BasicInstructionStatus) obj;
		return Objects.equals(instructionId, other.instructionId)
				&& instructionState == other.instructionState
				&& Objects.equals(resultParameters, other.resultParameters)
				&& Objects.equals(statusDate, other.statusDate);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BasicInstructionStatus{");
		if ( instructionId != null ) {
			builder.append("instructionId=");
			builder.append(instructionId);
			builder.append(", ");
		}
		if ( instructionState != null ) {
			builder.append("instructionState=");
			builder.append(instructionState);
			builder.append(", ");
		}
		if ( statusDate != null ) {
			builder.append("statusDate=");
			builder.append(statusDate);
			builder.append(", ");
		}
		if ( resultParameters != null ) {
			builder.append("resultParameters=");
			builder.append(resultParameters);
		}
		builder.append("}");
		return builder.toString();
	}

	@Override
	public InstructionStatus newCopyWithState(InstructionState newState) {
		return new BasicInstructionStatus(this.instructionId, newState, this.statusDate,
				this.resultParameters);
	}

	@Override
	public InstructionStatus newCopyWithState(InstructionState newState,
			Map<String, ?> resultParameters) {
		return new BasicInstructionStatus(this.instructionId, newState, this.statusDate,
				resultParameters);
	}

	@Override
	public Long getInstructionId() {
		return instructionId;
	}

	@Override
	public InstructionState getInstructionState() {
		return instructionState;
	}

	@Override
	public Instant getStatusDate() {
		return statusDate;
	}

	@Override
	public Map<String, ?> getResultParameters() {
		return resultParameters;
	}

}

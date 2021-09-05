/* ==================================================================
 * Instruction.java - 5/09/2021 4:20:41 PM
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

package net.solarnetwork.domain;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * API for a single, immutable instruction with associated parameters.
 * 
 * <p>
 * An instruction is like a single name (the {@code topic}) with an arbitrary
 * number of named key/value parameters. All parameter names and values are
 * Strings. Each parameter can have any number of associated values.
 * </p>
 * 
 * @author matt
 * @version 1.0
 * @since 2.0
 */
public interface Instruction {

	/**
	 * Get a unique ID for the instruction.
	 * 
	 * @return local unique ID
	 */
	Long getId();

	/**
	 * Get the topic of the instruction -- a unique identifier for the
	 * instruction type.
	 * 
	 * @return the topic name
	 */
	String getTopic();

	/**
	 * Get the date/time the instruction was requested.
	 * 
	 * @return the date
	 */
	Instant getInstructionDate();

	/**
	 * Get an Iterator of all unique instruction parameter names.
	 * 
	 * @return iterator, never {@literal null}
	 */
	Iterable<String> getParameterNames();

	/**
	 * Test if a specific parameter has an associated value available.
	 * 
	 * @param parameterName
	 *        the parameter name to test for
	 * @return {@literal true} if the parameter name has at least one value
	 *         available
	 */
	boolean isParameterAvailable(String parameterName);

	/**
	 * Get a single parameter value for a specific parameter name.
	 * 
	 * @param parameterName
	 *        the parameter name to get the value for
	 * @return the first available parameter name, or {@literal null} if not
	 *         available
	 */
	String getParameterValue(String parameterName);

	/**
	 * Get all parameter values for a specific parameter name;
	 * 
	 * @param parameterName
	 *        the parameter name to get the values for
	 * @return all available parameter values, or {@literal null} if not
	 *         available
	 */
	String[] getAllParameterValues(String parameterName);

	/**
	 * Get the instruction status.
	 * 
	 * @return the status
	 */
	InstructionStatus getStatus();

	/**
	 * Get the instruction state.
	 * 
	 * @return the state, or {@literal null} if none available
	 */
	default InstructionStatus.InstructionState getInstructionState() {
		InstructionStatus status = getStatus();
		return (status != null ? status.getInstructionState() : null);
	}

	/**
	 * Get a single-valued map of all parameter values.
	 * 
	 * @return the parameters as a map, never {@literal null}
	 */
	default Map<String, String> getParameterMap() {
		Map<String, String> result = null;
		for ( String paramName : getParameterNames() ) {
			if ( result == null ) {
				result = new LinkedHashMap<>(4);
			}
			result.put(paramName, getParameterValue(paramName));
		}
		return (result != null ? result : Collections.emptyMap());
	}

	/**
	 * Get a multi-valued map of all parameter values.
	 * 
	 * @return the parameters as a multi-valued map, never {@literal null}
	 */
	default Map<String, List<String>> getParameterMultiMap() {
		Map<String, List<String>> result = null;
		for ( String paramName : getParameterNames() ) {
			if ( result == null ) {
				result = new LinkedHashMap<>(4);
			}
			result.put(paramName, Arrays.asList(getAllParameterValues(paramName)));
		}
		return (result != null ? result : Collections.emptyMap());
	}

}

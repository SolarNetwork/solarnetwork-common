/* ==================================================================
 * BasicInstruction.java - Feb 28, 2011 10:36:05 AM
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Basic implementation of {@link Instruction}.
 * 
 * @author matt
 * @version 1.0
 * @since 2.0
 */
public class BasicInstruction implements Instruction, Serializable {

	private static final long serialVersionUID = 5522509637377814131L;

	/** The instruction ID. */
	private final Long id;

	/** The topic name. */
	private final String topic;

	/** The instruction date. */
	private final Instant instructionDate;

	/** The instruction status. */
	private final InstructionStatus status;

	/** The instruction parameters. */
	private final Map<String, List<String>> parameters;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *        the local instruction ID
	 * @param topic
	 *        the instruction topic
	 * @param instructionDate
	 *        the instruction date
	 * @param status
	 *        the status, or {@literal null}
	 */
	public BasicInstruction(Long id, String topic, Instant instructionDate, InstructionStatus status) {
		super();
		this.id = id;
		this.topic = topic;
		this.instructionDate = instructionDate;
		this.status = status;
		this.parameters = new LinkedHashMap<>();
	}

	/**
	 * Copy constructor.
	 * 
	 * @param other
	 *        the instruction to copy
	 * @param id
	 *        if provided, the ID to use
	 * @param status
	 *        if provided, the new status to use
	 */
	public BasicInstruction(Instruction other, Long id, InstructionStatus status) {
		this((id != null ? id : other.getId()), other.getTopic(), other.getInstructionDate(),

				(status != null ? status : other.getStatus()));
		Map<String, List<String>> otherParams = other.getParameterMultiMap();
		if ( otherParams != null ) {
			this.parameters.putAll(otherParams);
		}
	}

	/**
	 * Copy constructor.
	 * 
	 * @param other
	 *        the instruction to copy
	 * @param status
	 *        if provided, the new status to use
	 */
	public BasicInstruction(Instruction other, InstructionStatus status) {
		this(other, null, status);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BasicInstruction{topic=");
		builder.append(topic);
		builder.append(",id=");
		builder.append(id);
		builder.append(",status=");
		builder.append(status);
		builder.append("}");
		return builder.toString();
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public String getTopic() {
		return topic;
	}

	@Override
	public Instant getInstructionDate() {
		return instructionDate;
	}

	@Override
	public Iterable<String> getParameterNames() {
		return Collections.unmodifiableSet(parameters.keySet());
	}

	@Override
	public boolean isParameterAvailable(String parameterName) {
		List<String> values = parameters.get(parameterName);
		return values != null;
	}

	@Override
	public String getParameterValue(String parameterName) {
		List<String> values = parameters.get(parameterName);
		return values == null ? null : values.get(0);
	}

	@Override
	public String[] getAllParameterValues(String parameterName) {
		List<String> values = parameters.get(parameterName);
		if ( values != null ) {
			return values.toArray(new String[values.size()]);
		}
		return null;
	}

	@Override
	public InstructionStatus getStatus() {
		return status;
	}

	/**
	 * Add a new parameter value.
	 * 
	 * @param name
	 *        the parameter name
	 * @param value
	 *        the parameter value
	 */
	public void addParameter(String name, String value) {
		assert name != null && value != null;
		List<String> values = parameters.get(name);
		if ( values == null ) {
			values = new ArrayList<String>(3);
			parameters.put(name, values);
		}
		values.add(value);
	}

	/**
	 * Add a list of parameter values.
	 * 
	 * @param name
	 *        the parameter name
	 * @param values
	 *        the parameter values
	 */
	public void putParameters(String name, List<String> values) {
		assert name != null && values != null;
		parameters.put(name, values);
	}

	@Override
	public Map<String, List<String>> getParameterMultiMap() {
		return Collections.unmodifiableMap(parameters);
	}

}

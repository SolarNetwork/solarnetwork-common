/* ==================================================================
 * BasicNodeControlInfo.java - 3/09/2021 3:36:43 PM
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

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Basic implementation of {@link NodeControlInfo}.
 *
 * @author matt
 * @version 1.1
 * @since 2.0
 */
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(builder = BasicNodeControlInfo.Builder.class)
@tools.jackson.databind.annotation.JsonDeserialize(builder = BasicNodeControlInfo.Builder.class)
@JsonPropertyOrder({ "controlId", "type", "propertyName", "value", "readonly", "unit" })
public class BasicNodeControlInfo implements NodeControlInfo {

	private final String controlId;
	private final NodeControlPropertyType type;
	private final String value;
	private final Boolean readonly;
	private final String unit;
	private final String propertyName;

	/**
	 * Constructor.
	 *
	 * @param controlId
	 *        the control ID
	 * @param type
	 *        the control type
	 * @param value
	 *        the control value
	 * @param readonly
	 *        the readonly flag
	 * @param unit
	 *        the unit
	 * @param propertyName
	 *        the property name
	 */
	public BasicNodeControlInfo(String controlId, NodeControlPropertyType type, String value,
			Boolean readonly, String unit, String propertyName) {
		this(builder().withControlId(controlId).withType(type).withValue(value).withReadonly(readonly)
				.withUnit(unit).withPropertyName(propertyName));
	}

	private BasicNodeControlInfo(Builder builder) {
		this.controlId = builder.controlId;
		this.type = builder.type;
		this.value = builder.value;
		this.readonly = builder.readonly;
		this.unit = builder.unit;
		this.propertyName = builder.propertyName;
	}

	/**
	 * Creates builder to build {@link BasicNodeControlInfo}.
	 *
	 * @return created builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Creates a builder to build {@link NodeControlInfo} and initialize it with
	 * the given object.
	 *
	 * @param info
	 *        the object to initialize the builder with
	 * @return created builder
	 */
	public static Builder builderFrom(NodeControlInfo info) {
		return new Builder(info);
	}

	/**
	 * Builder to build {@link BasicNodeControlInfo}.
	 */
	public static final class Builder {

		private String controlId;
		private NodeControlPropertyType type;
		private String value;
		private Boolean readonly;
		private String unit;
		private String propertyName;

		private Builder() {
			super();
		}

		private Builder(NodeControlInfo info) {
			super();
			this.controlId = info.getControlId();
			this.type = info.getType();
			this.value = info.getValue();
			this.readonly = info.getReadonly();
			this.unit = info.getUnit();
			this.propertyName = info.getPropertyName();
		}

		/**
		 * Configure a control ID.
		 *
		 * @param controlId
		 *        the control ID
		 * @return this instance
		 */
		public Builder withControlId(String controlId) {
			this.controlId = controlId;
			return this;
		}

		/**
		 * Configure a property type.
		 *
		 * @param type
		 *        the property type
		 * @return this instance
		 */
		public Builder withType(NodeControlPropertyType type) {
			this.type = type;
			return this;
		}

		/**
		 * Configure a value.
		 *
		 * @param value
		 *        the value
		 * @return this instance
		 */
		public Builder withValue(String value) {
			this.value = value;
			return this;
		}

		/**
		 * Configure a read-only flag.
		 *
		 * @param readonly
		 *        {@literal true} for a read-only property
		 * @return this instance
		 */
		public Builder withReadonly(Boolean readonly) {
			this.readonly = readonly;
			return this;
		}

		/**
		 * Configure a unit.
		 *
		 * @param unit
		 *        the unit
		 * @return this instance
		 */
		public Builder withUnit(String unit) {
			this.unit = unit;
			return this;
		}

		/**
		 * Configure a property name.
		 *
		 * @param propertyName
		 *        the property name
		 * @return this instance
		 */
		public Builder withPropertyName(String propertyName) {
			this.propertyName = propertyName;
			return this;
		}

		/**
		 * Create a new instance from this builder.
		 *
		 * @return the new instance
		 */
		public BasicNodeControlInfo build() {
			return new BasicNodeControlInfo(this);
		}
	}

	@Override
	public String getControlId() {
		return controlId;
	}

	@Override
	public String getPropertyName() {
		return propertyName;
	}

	@Override
	public NodeControlPropertyType getType() {
		return type;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public Boolean getReadonly() {
		return readonly;
	}

	@Override
	public String getUnit() {
		return unit;
	}

}

/* ==================================================================
 * DeviceOperatingStatus.java - 18/02/2019 10:54:36 am
 * 
 * Copyright 2019 SolarNetwork.net Dev Team
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

import java.util.Collections;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * A device operating state combined with vendor-specific states.
 * 
 * <p>
 * This class is designed to support operating states that are hardware or
 * vendor specific and compliment the standardized states offered by
 * {@link DeviceOperatingState}.
 * </p>
 * 
 * @author matt
 * @version 1.0
 * @since 1.50
 */
@JsonDeserialize(builder = DeviceOperatingStatus.Builder.class)
@JsonPropertyOrder({ "state", "stateCode", "deviceStatesCode", "deviceStates" })
public class DeviceOperatingStatus<C extends Bitmaskable> {

	private final DeviceOperatingState state;
	private final Set<C> deviceStates;

	/**
	 * Constructor.
	 * 
	 * <p>
	 * The {@code deviceStates} property will be set to {@literal null}.
	 * </p>
	 * 
	 * @param state
	 *        the state
	 */
	public DeviceOperatingStatus(DeviceOperatingState state) {
		this(state, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param state
	 *        the state
	 * @param deviceStates
	 *        device specific states ({@literal null} allowed)
	 */
	public DeviceOperatingStatus(DeviceOperatingState state, Set<C> deviceStates) {
		super();
		this.state = (state != null ? state : DeviceOperatingState.Unknown);
		this.deviceStates = (deviceStates == null || deviceStates.isEmpty() ? null
				: Collections.unmodifiableSet(deviceStates));
	}

	/**
	 * Get the device operating state.
	 * 
	 * @return the state, never {@literal null}
	 */
	public DeviceOperatingState getState() {
		return state;
	}

	/**
	 * Get the device operating state code value.
	 * 
	 * @return the state code value
	 */
	public int getStateCode() {
		return state.getCode();
	}

	/**
	 * Get the device states.
	 * 
	 * @return the immutable device states, or {@literal null}
	 */
	public Set<C> getDeviceStates() {
		return deviceStates;
	}

	/**
	 * Get the device states bitmask value.
	 * 
	 * @return the device states bitmask value
	 */
	public int getDeviceStatesCode() {
		return Bitmaskable.bitmaskValue(deviceStates);
	}

	/**
	 * Creates builder to build {@link GenericDeviceOperatingStatus}.
	 * 
	 * @return created builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder to build {@link DeviceOperatingStatus}.
	 */
	public static final class Builder {

		private DeviceOperatingState state;
		private Set<GenericDeviceOperatingState> deviceStates = Collections.emptySet();

		private Builder() {
		}

		/**
		 * Configure a state value.
		 * 
		 * @param state
		 *        the state to set
		 * @return this builder
		 */
		public Builder withState(DeviceOperatingState state) {
			this.state = state;
			return this;
		}

		/**
		 * Configure a code value.
		 * 
		 * @param code
		 *        the code to set
		 * @return this builder
		 */
		public Builder withStateCode(int code) {
			DeviceOperatingState state = null;
			try {
				state = DeviceOperatingState.forCode(code);
			} catch ( IllegalArgumentException e ) {
				// ignore
			}
			return withState(state);
		}

		/**
		 * Configure a device states code value.
		 * 
		 * @param mask
		 *        the mask to set
		 * @return this instance
		 */
		public Builder withDeviceStatesCode(int mask) {
			this.deviceStates = Bitmaskable.setForBitmask(mask, GenericDeviceOperatingState.values());
			return this;
		}

		/**
		 * Create a new operating status instance based on this builder.
		 * 
		 * @return the new instance
		 */
		public GenericDeviceOperatingStatus build() {
			return new GenericDeviceOperatingStatus(state, deviceStates);
		}
	}

	/**
	 * Creates builder to build {@link DeviceOperatingStatus} using an enum for
	 * device state values.
	 * 
	 * @param <C>
	 *        the enum type
	 * @param clazz
	 *        the enum type class
	 * @return created builder
	 */
	public static <C extends Enum<C> & Bitmaskable> EnumBuilder<C> enumBuilder(Class<C> clazz) {
		return new EnumBuilder<>(clazz);
	}

	/**
	 * Builder to build {@link DeviceOperatingStatus} using an enum of device
	 * state values.
	 */
	public static final class EnumBuilder<C extends Enum<C> & Bitmaskable> {

		private final Class<C> clazz;
		private DeviceOperatingState state;
		private Set<C> deviceStates = Collections.emptySet();

		private EnumBuilder(Class<C> clazz) {
			super();
			this.clazz = clazz;
		}

		/**
		 * Configure a state value.
		 * 
		 * @param state
		 *        the state to set
		 * @return this builder
		 */
		public EnumBuilder<C> withState(DeviceOperatingState state) {
			this.state = state;
			return this;
		}

		/**
		 * Configure a state code.
		 * 
		 * @param code
		 *        the code to set
		 * @return this instance
		 */
		public EnumBuilder<C> withStateCode(int code) {
			DeviceOperatingState state = null;
			try {
				state = DeviceOperatingState.forCode(code);
			} catch ( IllegalArgumentException e ) {
				// ignore
			}
			return withState(state);
		}

		/**
		 * Configure a states code.
		 * 
		 * @param mask
		 *        the mask to set
		 * @return this instance
		 */
		public EnumBuilder<C> withDeviceStatesCode(int mask) {
			this.deviceStates = Bitmaskable.setForBitmask(mask, clazz);
			return this;
		}

		/**
		 * Create a new instance status instance from this builder.
		 * 
		 * @return the new instance
		 */
		public DeviceOperatingStatus<C> build() {
			return new DeviceOperatingStatus<>(state, deviceStates);
		}
	}

}

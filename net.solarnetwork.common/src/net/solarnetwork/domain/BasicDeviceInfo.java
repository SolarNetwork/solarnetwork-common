/* ==================================================================
 * BasicDeviceInfo.java - 9/07/2021 6:36:07 AM
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

import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Basic implementation of {@link DeviceInfo}.
 *
 * @author matt
 * @version 1.2
 * @since 1.75
 */
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(builder = BasicDeviceInfo.Builder.class)
@tools.jackson.databind.annotation.JsonDeserialize(builder = BasicDeviceInfo.Builder.class)
@JsonPropertyOrder({ "name", "manufacturer", "modelName", "version", "serialNumber", "manufactureDate",
		"deviceAddress", "nameplateRatings" })
public class BasicDeviceInfo implements DeviceInfo {

	private final @Nullable String name;
	private final @Nullable String manufacturer;
	private final @Nullable String modelName;
	private final @Nullable String version;
	private final @Nullable String serialNumber;
	private final @Nullable LocalDate manufactureDate;
	private final @Nullable String deviceAddress;
	private final @Nullable Map<String, ?> nameplateRatings;

	/**
	 * Constructor.
	 *
	 * @param manufacturer
	 *        the manufacturer
	 * @param modelName
	 *        the model name
	 * @param version
	 *        the version
	 * @param serialNumber
	 *        the serial number
	 * @param manufactureDate
	 *        the manufacture date
	 */
	public BasicDeviceInfo(@Nullable String manufacturer, @Nullable String modelName,
			@Nullable String version, @Nullable String serialNumber,
			@Nullable LocalDate manufactureDate) {
		this(manufacturer, modelName, version, serialNumber, manufactureDate, null);
	}

	/**
	 * Constructor.
	 *
	 * @param manufacturer
	 *        the manufacturer
	 * @param modelName
	 *        the model name
	 * @param version
	 *        the version
	 * @param serialNumber
	 *        the serial number
	 * @param manufactureDate
	 *        the manufacture date
	 * @param nameplateRatings
	 *        the nameplate ratings
	 * @since 1.1
	 */
	public BasicDeviceInfo(@Nullable String manufacturer, @Nullable String modelName,
			@Nullable String version, @Nullable String serialNumber, @Nullable LocalDate manufactureDate,
			@Nullable Map<String, ?> nameplateRatings) {
		// @formatter:off
		this(builder().withManufacturer(manufacturer)
				.withModelName(modelName)
				.withVersion(version)
				.withSerialNumber(serialNumber)
				.withManufactureDate(manufactureDate)
				.withNameplateRatings(nameplateRatings)
				);
		// @formatter:on
	}

	@Override
	public int hashCode() {
		return Objects.hash(deviceAddress, manufactureDate, manufacturer, modelName, name,
				nameplateRatings, serialNumber, version);
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( !(obj instanceof BasicDeviceInfo other) ) {
			return false;
		}
		// @formatter:off
		return Objects.equals(deviceAddress, other.deviceAddress)
				&& Objects.equals(manufactureDate, other.manufactureDate)
				&& Objects.equals(manufacturer, other.manufacturer)
				&& Objects.equals(modelName, other.modelName)
				&& Objects.equals(name, other.name)
				&& Objects.equals(nameplateRatings, other.nameplateRatings)
				&& Objects.equals(serialNumber, other.serialNumber)
				&& Objects.equals(version, other.version);
		// @formatter:on
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("BasicDeviceInfo{");
		if ( name != null ) {
			buf.append("name=");
			buf.append(name);
			buf.append(", ");
		}
		if ( manufacturer != null ) {
			buf.append("manufacturer=");
			buf.append(manufacturer);
			buf.append(", ");
		}
		if ( modelName != null ) {
			buf.append("modelName=");
			buf.append(modelName);
			buf.append(", ");
		}
		if ( version != null ) {
			buf.append("version=");
			buf.append(version);
			buf.append(", ");
		}
		if ( serialNumber != null ) {
			buf.append("serialNumber=");
			buf.append(serialNumber);
			buf.append(", ");
		}
		if ( manufactureDate != null ) {
			buf.append("manufactureDate=");
			buf.append(manufactureDate);
			buf.append(", ");
		}
		if ( deviceAddress != null ) {
			buf.append("deviceAddress=");
			buf.append(deviceAddress);
			buf.append(", ");
		}
		if ( nameplateRatings != null ) {
			buf.append("nameplateRatings=");
			buf.append(nameplateRatings);
		}
		buf.append("}");
		return buf.toString();
	}

	private BasicDeviceInfo(Builder builder) {
		this.name = builder.name;
		this.manufacturer = builder.manufacturer;
		this.modelName = builder.modelName;
		this.version = builder.version;
		this.serialNumber = builder.serialNumber;
		this.manufactureDate = builder.manufactureDate;
		this.deviceAddress = builder.deviceAddress;
		this.nameplateRatings = builder.nameplateRatings;
	}

	/**
	 * Creates builder to build {@link BasicDeviceInfo}.
	 *
	 * @return created builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Creates a builder to build {@link BasicDeviceInfo} and initialize it with
	 * the given object.
	 *
	 * @param basicDeviceInfo
	 *        to initialize the builder with
	 * @return created builder
	 */
	public static Builder builderFrom(DeviceInfo basicDeviceInfo) {
		return new Builder(basicDeviceInfo);
	}

	/**
	 * Builder to build {@link BasicDeviceInfo}.
	 */
	public static final class Builder {

		private @Nullable String name;
		private @Nullable String manufacturer;
		private @Nullable String modelName;
		private @Nullable String version;
		private @Nullable String serialNumber;
		private @Nullable LocalDate manufactureDate;
		private @Nullable String deviceAddress;
		private @Nullable Map<String, ?> nameplateRatings;

		private Builder() {
		}

		private Builder(DeviceInfo basicDeviceInfo) {
			this.name = basicDeviceInfo.getName();
			this.manufacturer = basicDeviceInfo.getManufacturer();
			this.modelName = basicDeviceInfo.getModelName();
			this.version = basicDeviceInfo.getVersion();
			this.serialNumber = basicDeviceInfo.getSerialNumber();
			this.manufactureDate = basicDeviceInfo.getManufactureDate();
			this.deviceAddress = basicDeviceInfo.getDeviceAddress();
			this.nameplateRatings = basicDeviceInfo.getNameplateRatings();
		}

		/**
		 * Configure a name.
		 *
		 * @param name
		 *        the value to set
		 * @return this instance
		 */
		public Builder withName(@Nullable String name) {
			this.name = name;
			return this;
		}

		/**
		 * Configure a manufacturer.
		 *
		 * @param manufacturer
		 *        the value to set
		 * @return this instance
		 */
		public Builder withManufacturer(@Nullable String manufacturer) {
			this.manufacturer = manufacturer;
			return this;
		}

		/**
		 * Configure a model name.
		 *
		 * @param modelName
		 *        the value to set
		 * @return this instance
		 */
		public Builder withModelName(@Nullable String modelName) {
			this.modelName = modelName;
			return this;
		}

		/**
		 * Configure a version.
		 *
		 * @param version
		 *        the value to set
		 * @return this instance
		 */
		public Builder withVersion(@Nullable String version) {
			this.version = version;
			return this;
		}

		/**
		 * Configure a serial number.
		 *
		 * @param serialNumber
		 *        the value to set
		 * @return this instance
		 */
		public Builder withSerialNumber(@Nullable String serialNumber) {
			this.serialNumber = serialNumber;
			return this;
		}

		/**
		 * Configure a manufacture date.
		 *
		 * @param manufactureDate
		 *        the value to set
		 * @return this instance
		 */
		public Builder withManufactureDate(@Nullable LocalDate manufactureDate) {
			this.manufactureDate = manufactureDate;
			return this;
		}

		/**
		 * Configure a device address.
		 *
		 * @param deviceAddress
		 *        the value to set
		 * @return this instance
		 */
		public Builder withDeviceAddress(@Nullable String deviceAddress) {
			this.deviceAddress = deviceAddress;
			return this;
		}

		/**
		 * Configure a nameplate ratings map.
		 *
		 * @param nameplateRatings
		 *        the value to set
		 * @return this instance
		 * @since 1.1
		 */
		public Builder withNameplateRatings(@Nullable Map<String, ?> nameplateRatings) {
			this.nameplateRatings = nameplateRatings;
			return this;
		}

		/**
		 * Create a new instance from this builder.
		 *
		 * @return the new instance
		 */
		public BasicDeviceInfo build() {
			return new BasicDeviceInfo(this);
		}

		/**
		 * Test if all fields are {@code null} or empty.
		 *
		 * @return {@literal true} if all fields are {@code null} or empty
		 */
		public boolean isEmpty() {
			// @formatter:off
			return ((name == null || name.isEmpty())
					&& (manufacturer == null || manufacturer.isEmpty())
					&& (modelName == null || modelName.isEmpty())
					&& (version == null || version.isEmpty())
					&& (serialNumber == null || serialNumber.isEmpty())
					&& manufactureDate == null
					&& (deviceAddress == null || deviceAddress.isEmpty())
					&& (nameplateRatings == null || nameplateRatings.isEmpty())
					);
			// @formatter:on
		}
	}

	@Override
	public @Nullable String getName() {
		return name;
	}

	@Override
	public @Nullable String getManufacturer() {
		return manufacturer;
	}

	@Override
	public @Nullable String getModelName() {
		return modelName;
	}

	@Override
	public @Nullable String getVersion() {
		return version;
	}

	@Override
	public @Nullable String getSerialNumber() {
		return serialNumber;
	}

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Override
	public @Nullable LocalDate getManufactureDate() {
		return manufactureDate;
	}

	@Override
	public @Nullable String getDeviceAddress() {
		return deviceAddress;
	}

	@Override
	public final @Nullable Map<String, ?> getNameplateRatings() {
		return nameplateRatings;
	}

}

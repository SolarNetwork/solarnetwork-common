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
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Basic implementation of {@link DeviceInfo}.
 * 
 * @author matt
 * @version 1.0
 * @since 1.75
 */
@JsonDeserialize(builder = BasicDeviceInfo.Builder.class)
@JsonPropertyOrder({ "name", "manufacturer", "modelName", "version", "serialNumber", "manufactureDate",
		"deviceAddress" })
public class BasicDeviceInfo implements DeviceInfo {

	private final String name;
	private final String manufacturer;
	private final String modelName;
	private final String version;
	private final String serialNumber;
	private final LocalDate manufactureDate;
	private final String deviceAddress;

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
	public BasicDeviceInfo(String manufacturer, String modelName, String version, String serialNumber,
			LocalDate manufactureDate) {
		// @formatter:off
		this(builder().withManufacturer(manufacturer)
				.withModelName(modelName)
				.withVersion(version)
				.withSerialNumber(serialNumber)
				.withManufactureDate(manufactureDate));
		// @formatter:on
	}

	private BasicDeviceInfo(Builder builder) {
		this.name = builder.name;
		this.manufacturer = builder.manufacturer;
		this.modelName = builder.modelName;
		this.version = builder.version;
		this.serialNumber = builder.serialNumber;
		this.manufactureDate = builder.manufactureDate;
		this.deviceAddress = builder.deviceAddress;
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
	public static Builder builderFrom(BasicDeviceInfo basicDeviceInfo) {
		return new Builder(basicDeviceInfo);
	}

	/**
	 * Builder to build {@link BasicDeviceInfo}.
	 */
	public static final class Builder {

		private String name;
		private String manufacturer;
		private String modelName;
		private String version;
		private String serialNumber;
		private LocalDate manufactureDate;
		private String deviceAddress;

		private Builder() {
		}

		private Builder(BasicDeviceInfo basicDeviceInfo) {
			this.name = basicDeviceInfo.name;
			this.manufacturer = basicDeviceInfo.manufacturer;
			this.modelName = basicDeviceInfo.modelName;
			this.version = basicDeviceInfo.version;
			this.serialNumber = basicDeviceInfo.serialNumber;
			this.manufactureDate = basicDeviceInfo.manufactureDate;
			this.deviceAddress = basicDeviceInfo.deviceAddress;
		}

		public Builder withName(String name) {
			this.name = name;
			return this;
		}

		public Builder withManufacturer(String manufacturer) {
			this.manufacturer = manufacturer;
			return this;
		}

		public Builder withModelName(String modelName) {
			this.modelName = modelName;
			return this;
		}

		public Builder withVersion(String version) {
			this.version = version;
			return this;
		}

		public Builder withSerialNumber(String serialNumber) {
			this.serialNumber = serialNumber;
			return this;
		}

		public Builder withManufactureDate(LocalDate manufactureDate) {
			this.manufactureDate = manufactureDate;
			return this;
		}

		public Builder withDeviceAddress(String deviceAddress) {
			this.deviceAddress = deviceAddress;
			return this;
		}

		public BasicDeviceInfo build() {
			return new BasicDeviceInfo(this);
		}

		/**
		 * Test if all fields are {@literal null} or empty.
		 * 
		 * @return {@literal true} if all fields are {@literal null} or empty
		 */
		public boolean isEmpty() {
			// @formatter:off
			return ((name == null || name.isEmpty()) 
					&& (manufacturer == null || manufacturer.isEmpty())
					&& (modelName == null || modelName.isEmpty())
					&& (version == null || version.isEmpty())
					&& (serialNumber == null || serialNumber.isEmpty())
					&& manufactureDate == null
					&& (deviceAddress == null || deviceAddress.isEmpty()));
			// @formatter:on
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getManufacturer() {
		return manufacturer;
	}

	@Override
	public String getModelName() {
		return modelName;
	}

	@Override
	public String getVersion() {
		return version;
	}

	@Override
	public String getSerialNumber() {
		return serialNumber;
	}

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Override
	public LocalDate getManufactureDate() {
		return manufactureDate;
	}

	@Override
	public String getDeviceAddress() {
		return deviceAddress;
	}

}

/* ==================================================================
 * ChargePointInfo.java - 7/02/2020 7:36:53 am
 *
 * Copyright 2020 SolarNetwork.net Dev Team
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

package net.solarnetwork.ocpp.domain;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Information about a Charge Point.
 *
 * @author matt
 * @version 1.0
 */
public class ChargePointInfo {

	private @Nullable String id;
	private @Nullable String chargePointVendor;
	private @Nullable String chargePointModel;
	private @Nullable String chargePointSerialNumber;
	private @Nullable String chargeBoxSerialNumber;
	private @Nullable String firmwareVersion;
	private @Nullable String iccid;
	private @Nullable String imsi;
	private @Nullable String meterType;
	private @Nullable String meterSerialNumber;

	/**
	 * Constructor.
	 */
	public ChargePointInfo() {
		super();
	}

	/**
	 * Constructor.
	 *
	 * @param id
	 *        the ID to use
	 */
	public ChargePointInfo(@Nullable String id) {
		super();
		setId(id);
	}

	/**
	 * Constructor.
	 *
	 * @param id
	 *        the ID to use
	 * @param chargePointVendor
	 *        the vendor
	 * @param chargePointModel
	 *        the model
	 */
	public ChargePointInfo(@Nullable String id, @Nullable String chargePointVendor,
			@Nullable String chargePointModel) {
		super();
		this.id = id;
		this.chargePointVendor = chargePointVendor;
		this.chargePointModel = chargePointModel;
	}

	/**
	 * Copy constructor.
	 *
	 * @param other
	 *        the info to copy
	 */
	public ChargePointInfo(@Nullable ChargePointInfo other) {
		super();
		copyFrom(other);
	}

	/**
	 * Copy the properties of another info onto this instance.
	 *
	 * @param other
	 *        the properties to copy
	 */
	public void copyFrom(@Nullable ChargePointInfo other) {
		if ( other == null ) {
			return;
		}
		this.id = other.id;
		this.chargePointVendor = other.chargePointVendor;
		this.chargePointModel = other.chargePointModel;
		this.chargePointSerialNumber = other.chargePointSerialNumber;
		this.chargeBoxSerialNumber = other.chargeBoxSerialNumber;
		this.firmwareVersion = other.firmwareVersion;
		this.iccid = other.iccid;
		this.imsi = other.imsi;
		this.meterType = other.meterType;
		this.meterSerialNumber = other.meterSerialNumber;
	}

	/**
	 * Test if the properties of another info are the same as in this instance.
	 *
	 * <p>
	 * The {@code id} properties are not compared by this method.
	 * </p>
	 *
	 * @param other
	 *        the other info to compare to
	 * @return {@literal true} if the properties of this instance are equal to
	 *         the other
	 */
	public boolean isSameAs(@Nullable ChargePointInfo other) {
		if ( other == null ) {
			return false;
		}
		// @formatter:off
		return Objects.equals(chargePointVendor, other.chargePointVendor)
				&& Objects.equals(chargePointModel, other.chargePointModel)
				&& Objects.equals(chargePointSerialNumber, other.chargePointSerialNumber)
				&& Objects.equals(chargeBoxSerialNumber, other.chargeBoxSerialNumber)
				&& Objects.equals(firmwareVersion, other.firmwareVersion)
				&& Objects.equals(iccid, other.iccid)
				&& Objects.equals(imsi, other.imsi)
				&& Objects.equals(meterType, other.meterType)
				&& Objects.equals(meterSerialNumber, other.meterSerialNumber);
		// @formatter:on
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ChargePointInfo{");
		if ( id != null ) {
			builder.append("id=");
			builder.append(id);
			builder.append(", ");
		}
		if ( chargePointVendor != null ) {
			builder.append("chargePointVendor=");
			builder.append(chargePointVendor);
			builder.append(", ");
		}
		if ( chargePointModel != null ) {
			builder.append("chargePointModel=");
			builder.append(chargePointModel);
			builder.append(", ");
		}
		if ( chargePointSerialNumber != null ) {
			builder.append("chargePointSerialNumber=");
			builder.append(chargePointSerialNumber);
			builder.append(", ");
		}
		if ( chargeBoxSerialNumber != null ) {
			builder.append("chargeBoxSerialNumber=");
			builder.append(chargeBoxSerialNumber);
			builder.append(", ");
		}
		if ( firmwareVersion != null ) {
			builder.append("firmwareVersion=");
			builder.append(firmwareVersion);
			builder.append(", ");
		}
		if ( iccid != null ) {
			builder.append("iccid=");
			builder.append(iccid);
			builder.append(", ");
		}
		if ( imsi != null ) {
			builder.append("imsi=");
			builder.append(imsi);
			builder.append(", ");
		}
		if ( meterType != null ) {
			builder.append("meterType=");
			builder.append(meterType);
			builder.append(", ");
		}
		if ( meterSerialNumber != null ) {
			builder.append("meterSerialNumber=");
			builder.append(meterSerialNumber);
		}
		builder.append("}");
		return builder.toString();
	}

	/**
	 * Get the unique ID of the charge point.
	 *
	 * @return the id
	 */
	public final @Nullable String getId() {
		return id;
	}

	/**
	 * Set the unique ID of the charge point.
	 *
	 * @param id
	 *        the id to set
	 */
	public final void setId(@Nullable String id) {
		this.id = id;
	}

	/**
	 * Get the charge point vendor.
	 *
	 * @return the vendor
	 */
	public final @Nullable String getChargePointVendor() {
		return chargePointVendor;
	}

	/**
	 * Set the charge point vendor.
	 *
	 * @param chargePointVendor
	 *        the vendor to set
	 */
	public final void setChargePointVendor(@Nullable String chargePointVendor) {
		this.chargePointVendor = chargePointVendor;
	}

	/**
	 * Get the charge point model.
	 *
	 * @return the model
	 */
	public final @Nullable String getChargePointModel() {
		return chargePointModel;
	}

	/**
	 * Set the charge point model.
	 *
	 * @param chargePointModel
	 *        the model to set
	 */
	public final void setChargePointModel(@Nullable String chargePointModel) {
		this.chargePointModel = chargePointModel;
	}

	/**
	 * Get the charge point serial number.
	 *
	 * @return the serial number
	 */
	public final @Nullable String getChargePointSerialNumber() {
		return chargePointSerialNumber;
	}

	/**
	 * Set the charge point serial number.
	 *
	 * @param chargePointSerialNumber
	 *        the serial number to set
	 */
	public final void setChargePointSerialNumber(@Nullable String chargePointSerialNumber) {
		this.chargePointSerialNumber = chargePointSerialNumber;
	}

	/**
	 * Get the charge box serial number.
	 *
	 * @return the serial number
	 */
	public final @Nullable String getChargeBoxSerialNumber() {
		return chargeBoxSerialNumber;
	}

	/**
	 * Set the charge box serial number.
	 *
	 * @param chargeBoxSerialNumber
	 *        the serial number to set
	 */
	public final void setChargeBoxSerialNumber(@Nullable String chargeBoxSerialNumber) {
		this.chargeBoxSerialNumber = chargeBoxSerialNumber;
	}

	/**
	 * Get the firmware version.
	 *
	 * @return the firmware version
	 */
	public final @Nullable String getFirmwareVersion() {
		return firmwareVersion;
	}

	/**
	 * Set the firmware version.
	 *
	 * @param firmwareVersion
	 *        the firmware version to set
	 */
	public final void setFirmwareVersion(@Nullable String firmwareVersion) {
		this.firmwareVersion = firmwareVersion;
	}

	/**
	 * Get the ICC ID.
	 *
	 * @return the ID
	 */
	public final @Nullable String getIccid() {
		return iccid;
	}

	/**
	 * Set the ICC ID.
	 *
	 * @param iccid
	 *        the ID to set
	 */
	public final void setIccid(@Nullable String iccid) {
		this.iccid = iccid;
	}

	/**
	 * Get the IMSI.
	 *
	 * @return the imsi
	 */
	public final @Nullable String getImsi() {
		return imsi;
	}

	/**
	 * Set the IMSI.
	 *
	 * @param imsi
	 *        the imsi to set
	 */
	public final void setImsi(@Nullable String imsi) {
		this.imsi = imsi;
	}

	/**
	 * Get the meter type.
	 *
	 * @return the meterType
	 */
	public final @Nullable String getMeterType() {
		return meterType;
	}

	/**
	 * Set the meter type.
	 *
	 * @param meterType
	 *        the meterType to set
	 */
	public final void setMeterType(@Nullable String meterType) {
		this.meterType = meterType;
	}

	/**
	 * Get the meter serial number.
	 *
	 * @return the serial number
	 */
	public final @Nullable String getMeterSerialNumber() {
		return meterSerialNumber;
	}

	/**
	 * Set the meter serial number.
	 *
	 * @param meterSerialNumber
	 *        the serial number to set
	 */
	public final void setMeterSerialNumber(@Nullable String meterSerialNumber) {
		this.meterSerialNumber = meterSerialNumber;
	}

}

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

/**
 * Information about a Charge Point.
 * 
 * @author matt
 * @version 1.0
 */
public class ChargePointInfo {

	private String id;
	private String chargePointVendor;
	private String chargePointModel;
	private String chargePointSerialNumber;
	private String chargeBoxSerialNumber;
	private String firmwareVersion;
	private String iccid;
	private String imsi;
	private String meterType;
	private String meterSerialNumber;

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
	public ChargePointInfo(String id) {
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
	public ChargePointInfo(String id, String chargePointVendor, String chargePointModel) {
		super();
		setId(id);
		setChargePointVendor(chargePointVendor);
		setChargePointModel(chargePointModel);
	}

	/**
	 * Copy constructor.
	 * 
	 * @param other
	 *        the info to copy
	 */
	public ChargePointInfo(ChargePointInfo other) {
		super();
		copyFrom(other);
	}

	/**
	 * Copy the properties of another info onto this instance.
	 * 
	 * @param info
	 *        the properties to copy
	 */
	public void copyFrom(ChargePointInfo other) {
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
	public boolean isSameAs(ChargePointInfo other) {
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
	public String getId() {
		return id;
	}

	/**
	 * Set the unique ID of the charge point.
	 * 
	 * @param id
	 *        the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the chargePointVendor
	 */
	public String getChargePointVendor() {
		return chargePointVendor;
	}

	/**
	 * @param chargePointVendor
	 *        the chargePointVendor to set
	 */
	public void setChargePointVendor(String chargePointVendor) {
		this.chargePointVendor = chargePointVendor;
	}

	/**
	 * @return the chargePointModel
	 */
	public String getChargePointModel() {
		return chargePointModel;
	}

	/**
	 * @param chargePointModel
	 *        the chargePointModel to set
	 */
	public void setChargePointModel(String chargePointModel) {
		this.chargePointModel = chargePointModel;
	}

	/**
	 * @return the chargePointSerialNumber
	 */
	public String getChargePointSerialNumber() {
		return chargePointSerialNumber;
	}

	/**
	 * @param chargePointSerialNumber
	 *        the chargePointSerialNumber to set
	 */
	public void setChargePointSerialNumber(String chargePointSerialNumber) {
		this.chargePointSerialNumber = chargePointSerialNumber;
	}

	/**
	 * @return the chargeBoxSerialNumber
	 */
	public String getChargeBoxSerialNumber() {
		return chargeBoxSerialNumber;
	}

	/**
	 * @param chargeBoxSerialNumber
	 *        the chargeBoxSerialNumber to set
	 */
	public void setChargeBoxSerialNumber(String chargeBoxSerialNumber) {
		this.chargeBoxSerialNumber = chargeBoxSerialNumber;
	}

	/**
	 * @return the firmwareVersion
	 */
	public String getFirmwareVersion() {
		return firmwareVersion;
	}

	/**
	 * @param firmwareVersion
	 *        the firmwareVersion to set
	 */
	public void setFirmwareVersion(String firmwareVersion) {
		this.firmwareVersion = firmwareVersion;
	}

	/**
	 * @return the iccid
	 */
	public String getIccid() {
		return iccid;
	}

	/**
	 * @param iccid
	 *        the iccid to set
	 */
	public void setIccid(String iccid) {
		this.iccid = iccid;
	}

	/**
	 * @return the imsi
	 */
	public String getImsi() {
		return imsi;
	}

	/**
	 * @param imsi
	 *        the imsi to set
	 */
	public void setImsi(String imsi) {
		this.imsi = imsi;
	}

	/**
	 * @return the meterType
	 */
	public String getMeterType() {
		return meterType;
	}

	/**
	 * @param meterType
	 *        the meterType to set
	 */
	public void setMeterType(String meterType) {
		this.meterType = meterType;
	}

	/**
	 * @return the meterSerialNumber
	 */
	public String getMeterSerialNumber() {
		return meterSerialNumber;
	}

	/**
	 * @param meterSerialNumber
	 *        the meterSerialNumber to set
	 */
	public void setMeterSerialNumber(String meterSerialNumber) {
		this.meterSerialNumber = meterSerialNumber;
	}

}

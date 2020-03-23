/* ==================================================================
 * Measurand.java - 10/02/2020 11:08:23 am
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

import net.solarnetwork.domain.CodedValue;

/**
 * A type of sampled value.
 * 
 * @author matt
 * @version 1.0
 */
public enum Measurand implements CodedValue {

	/** An unknown measurand. */
	Unknown(0),

	/** Instantaneous current flow from EV. */
	CurrentExport(1),

	/** Instantaneous current flow to EV. */
	CurrentImport(2),

	/** Maximum current offered to EV. */
	CurrentOffered(17),

	/**
	 * Numerical value read from the "active electrical energy" (Wh or kWh)
	 * register of the most authoritative electrical meter measuring energy
	 * exported to the grid.
	 */
	EnergyActiveExportInterval(3),

	/**
	 * Numerical value read from the "active electrical energy" (Wh or kWh)
	 * register of the (most authoritative) electrical meter measuring energy
	 * imported (from the grid supply).
	 */
	EnergyActiveExportRegister(4),

	/**
	 * Numerical value read from the "reactive electrical energy" (VARh or
	 * kVARh) register of the (most authoritative) electrical meter measuring
	 * energy exported (to the grid).
	 */
	EnergyActiveImportInterval(5),

	/**
	 * Numerical value read from the "reactive electrical energy" (VARh or
	 * kVARh) register of the (most authoritative) electrical meter measuring
	 * energy imported (from the grid supply).
	 */
	EnergyActiveImportRegister(6),

	/**
	 * Absolute amount of "active electrical energy" (Wh or kWh) exported (to
	 * the grid) during an associated time "interval", specified by a
	 * Metervalues ReadingContext, and applicable interval duration
	 * configuration values (in seconds) for "ClockAlignedDataInterval" and
	 * "MeterValueSampleInterval".
	 */
	EnergyReactiveExportInterval(7),

	/**
	 * Absolute amount of "active electrical energy" (Wh or kWh) imported (from
	 * the grid supply) during an associated time "interval", specified by a
	 * Metervalues ReadingContext, and applicable interval duration
	 * configuration values (in seconds) for "ClockAlignedDataInterval" and
	 * "MeterValueSampleInterval".
	 */
	EnergyReactiveExportRegister(8),

	/**
	 * Absolute amount of "active electrical energy" (Wh or kWh) imported (from
	 * the grid supply) during an associated time "interval", specified by a
	 * Metervalues ReadingContext, and applicable interval duration
	 * configuration values (in seconds) for "ClockAlignedDataInterval" and
	 * "MeterValueSampleInterval".
	 */
	EnergyReactiveImportInterval(9),

	/**
	 * Absolute amount of "reactive electrical energy" (VARh or kVARh) imported
	 * (from the grid supply) during an associated time "interval", specified by
	 * a Metervalues ReadingContext, and applicable interval duration
	 * configuration values (in seconds) for "ClockAlignedDataInterval" and
	 * "MeterValueSampleInterval".
	 */
	EnergyReactiveImportRegister(10),

	/**
	 * Instantaneous reading of powerline frequency. NOTE: OCPP 1.6 does not
	 * have a UnitOfMeasure for frequency, the UnitOfMeasure for any
	 * SampledValue with measurand: Frequency is Hertz.
	 */
	Frequency(18),

	/** Instantaneous active power exported by EV (W or kW). */
	PowerActiveExport(11),

	/** Instantaneous active power imported by EV (W or kW). */
	PowerActiveImport(12),

	/** Instantaneous power factor of total energy flow. */
	PowerFactor(19),

	/** Maximum power offered to EV. */
	PowerOffered(20),

	/** Instantaneous reactive power exported by EV (var or kvar). */
	PowerReactiveExport(13),

	/** Instantaneous reactive power imported by EV (var or kvar). */
	PowerReactiveImport(14),

	/** Fan speed in RPM. */
	RPM(21),

	/** State of charge of charging vehicle in percentage. */
	SoC(22),

	/** Temperature reading inside Charge Point. */
	Temperature(15),

	/** Instantaneous AC RMS supply voltage. */
	Voltage(16);

	private final byte code;

	private Measurand(int code) {
		this.code = (byte) code;
	}

	/**
	 * Get the code value.
	 * 
	 * @return the code value
	 */
	@Override
	public int getCode() {
		return code & 0xFF;
	}

	/**
	 * Get an enumeration value for a code value.
	 * 
	 * @param code
	 *        the code
	 * @return the status, never {@literal null} and set to {@link #Unknown} if
	 *         not any other valid code
	 */
	public static Measurand forCode(int code) {
		final byte c = (byte) code;
		for ( Measurand v : values() ) {
			if ( v.code == c ) {
				return v;
			}
		}
		return Measurand.Unknown;
	}

}

/* ==================================================================
 * AcEnergyDatum.java - Apr 2, 2014 7:08:15 AM
 * 
 * Copyright 2007-2014 SolarNetwork.net Dev Team
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

package net.solarnetwork.domain.datum;

import static net.solarnetwork.domain.datum.DatumSamplesType.Instantaneous;
import static net.solarnetwork.domain.datum.DatumSamplesType.Status;

/**
 * Standardized API for alternating current related energy datum to implement.
 * 
 * <p>
 * This API represents a single phase, either a direct phase measurement or an
 * average or total measurement.
 * </p>
 * 
 * @author matt
 * @version 2.0
 */
public interface AcEnergyDatum extends EnergyDatum {

	/**
	 * An status sample key for {@link #getAcPhase()} values.
	 */
	String PHASE_KEY = "phase";

	/**
	 * An instantaneous sample key for {@link #getRealPower()} values.
	 */
	String REAL_POWER_KEY = "realPower";

	/**
	 * An instantaneous sample key for {@link #getApparentPower()} values.
	 */
	String APPARENT_POWER_KEY = "apparentPower";

	/**
	 * An instantaneous sample key for {@link #getReactivePower()} values.
	 */
	String REACTIVE_POWER_KEY = "reactivePower";

	/**
	 * An instantaneous sample key for {@link #getPowerFactor()} values.
	 */
	String POWER_FACTOR_KEY = "powerFactor";

	/**
	 * An instantaneous sample key for {@link #getEffectivePowerFactor()}
	 * values.
	 */
	String EFFECTIVE_POWER_FACTOR_KEY = "effectivePowerFactor";

	/**
	 * An instantaneous sample key for {@link #getFrequency()} values.
	 */
	String FREQUENCY_KEY = "frequency";

	/**
	 * An instantaneous sample key for {@link #getVoltage()} values.
	 */
	String VOLTAGE_KEY = "voltage";

	/**
	 * An instantaneous sample key for {@link #getCurrent()} values.
	 */
	String CURRENT_KEY = "current";

	/**
	 * An instantaneous sample key for {@link #getPhaseVoltage()} values.
	 */
	String PHASE_VOLTAGE_KEY = "phaseVoltage";

	/**
	 * An instantaneous sample key for {@link #getLineVoltage()} values.
	 */
	String LINE_VOLTAGE_KEY = "lineVoltage";

	/**
	 * An instantaneous sample key for {@link #getNeutralCurrent()} values.
	 */
	String NEUTRAL_CURRENT_KEY = "neutralCurrent";

	/**
	 * Get the phase measured by this datum.
	 * 
	 * @return the phase, if known
	 */
	default AcPhase getAcPhase() {
		String p = asSampleOperations().getSampleString(Status, PHASE_KEY);
		return (p == null ? null : AcPhase.valueOf(p));
	}

	/**
	 * Get the instantaneous real power, in watts (W).
	 * 
	 * <p>
	 * This should return the same value as {@link EnergyDatum#getWatts()} but
	 * has this method to be explicit.
	 * </p>
	 * 
	 * @return the real power in watts, or {@literal null} if not available
	 */
	default Integer getRealPower() {
		return asSampleOperations().getSampleInteger(Instantaneous, REAL_POWER_KEY);
	}

	/**
	 * Get the instantaneous apparent power, in volt-amperes (VA).
	 * 
	 * @return the apparent power in volt-amperes, or {@literal null} if not
	 *         available
	 */
	default Integer getApparentPower() {
		return asSampleOperations().getSampleInteger(Instantaneous, APPARENT_POWER_KEY);

	}

	/**
	 * Get the instantaneous reactive power, in reactive volt-amperes (var).
	 * 
	 * @return the reactive power in reactive volt-amperes, or {@literal null}
	 *         if not available
	 */
	default Integer getReactivePower() {
		return asSampleOperations().getSampleInteger(Instantaneous, REACTIVE_POWER_KEY);

	}

	/**
	 * Get the effective instantaneous power factor, as a value between
	 * {@code -1} and {@code 1}. If the phase angle is positive (current leads
	 * voltage) this method returns a positive value. If the phase angle is
	 * negative (current lags voltage) this method returns a negative value.
	 * 
	 * @return the effective power factor
	 */
	default Float getEffectivePowerFactor() {
		return asSampleOperations().getSampleFloat(Instantaneous, EFFECTIVE_POWER_FACTOR_KEY);
	}

	/**
	 * Get the instantaneous frequency, in hertz (Hz).
	 * 
	 * @return the frequency, or {@literal null} if not known
	 */
	default Float getFrequency() {
		return asSampleOperations().getSampleFloat(Instantaneous, FREQUENCY_KEY);
	}

	/**
	 * Get the instantaneous neutral voltage.
	 * 
	 * @return the volts, or {@literal null} if not known
	 */
	default Float getVoltage() {
		return asSampleOperations().getSampleFloat(Instantaneous, VOLTAGE_KEY);
	}

	/**
	 * Get the instantaneous phase-to-neutral line voltage for a specific phase.
	 * 
	 * @param phase
	 *        the phase
	 * @return the volts, or {@literal null} if not known
	 */
	default Float getVoltage(AcPhase phase) {
		return asSampleOperations().getSampleFloat(Instantaneous, phase.withKey(VOLTAGE_KEY));
	}

	/**
	 * Get the instantaneous current, in amps.
	 * 
	 * <p>
	 * This method is equivalent to calling
	 * {@code datum.getCurrent(datum.getPhase())}.
	 * </p>
	 * 
	 * @return the amps, or {@literal null} if not known
	 */
	default Float getCurrent() {
		return asSampleOperations().getSampleFloat(Instantaneous, CURRENT_KEY);
	}

	/**
	 * Get the instantaneous current, in amps, for a specific phase.
	 * 
	 * @param phase
	 *        the phase
	 * @return the phase
	 */
	default Float getCurrent(AcPhase phase) {
		return asSampleOperations().getSampleFloat(Instantaneous, phase.withKey(CURRENT_KEY));
	}

	/**
	 * Get the instantaneous neutral current, in amps.
	 * 
	 * @return the amps, or {@literal null} if not known
	 */
	default Float getNeutralCurrent() {
		return asSampleOperations().getSampleFloat(Instantaneous, NEUTRAL_CURRENT_KEY);
	}

	/**
	 * Get the instantaneous phase-to-neutral line voltage.
	 * 
	 * <p>
	 * This metnod is equivalent to calling
	 * {@code datum.getPhaseVoltage(datum.getPhase())}.
	 * </p>
	 * 
	 * @return the volts, or {@literal null} if not known
	 */
	default Float getPhaseVoltage() {
		return asSampleOperations().getSampleFloat(Instantaneous, PHASE_VOLTAGE_KEY);
	}

	/**
	 * Get the instantaneous phase-to-phase line voltage.
	 * 
	 * <p>
	 * For the {@link #getAcPhase()}, this value represents the difference
	 * between this phase and the <i>next</i> phase, in {@literal a},
	 * {@literal b}, {@literal c} order, with {@code PhaseC} wrapping around
	 * back to {@code PhaseA}. Thus the possible values represent:
	 * </p>
	 * 
	 * <dl>
	 * <dt>{@code PhaseA}</dt>
	 * <dd>Vab</dd>
	 * <dt>{@code PhaseB}</dt>
	 * <dd>Vbc</dd>
	 * <dt>{@code PhaseC}</dt>
	 * <dd>Vca</dd>
	 * </dl>
	 * 
	 * <p>
	 * This metnod is equivalent to calling
	 * {@code datum.getLineVoltage(datum.getPhase())}.
	 * </p>
	 * 
	 * @return the line voltage
	 * @see #getLineVoltage(AcPhase)
	 */
	default Float getLineVoltage() {
		return asSampleOperations().getSampleFloat(Instantaneous, LINE_VOLTAGE_KEY);
	}

	/**
	 * Get the instantaneous phase-to-phase line voltage for a specific phase.
	 * 
	 * @param phase
	 *        the phase (first)
	 * @return the line voltage
	 */
	default Float getLineVoltage(AcPhase phase) {
		return asSampleOperations().getSampleFloat(Instantaneous, phase.withLineKey(VOLTAGE_KEY));

	}

	/**
	 * Get the instantaneous power factor.
	 * 
	 * @return the power factor, or {@literal null} if not known
	 */
	default Float getPowerFactor() {
		return asSampleOperations().getSampleFloat(Instantaneous, POWER_FACTOR_KEY);
	}

}

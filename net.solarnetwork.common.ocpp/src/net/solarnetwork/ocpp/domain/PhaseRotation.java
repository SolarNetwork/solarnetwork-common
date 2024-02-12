/* ==================================================================
 * PhaseRotation.java - 31/01/2020 10:34:56 am
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

/**
 * Enumeration of phase rotation values.
 * 
 * <p>
 * {@literal R} can be identified as phase 1 (L1), {@literal S} as phase 2 (L2),
 * {@literal T} as phase 3 (L3).
 * </p>
 * 
 * @author matt
 * @version 1.0
 */
public enum PhaseRotation {

	/** Phases not applicable, for single phase or DC Charge Points. */
	NotApplicable,

	/** Not (yet) known. */
	Unknown,

	/** Standard Reference Phasing. */
	RST,

	/** Reversed Reference Phasing. */
	RTS,

	/** Reversed 240 degree rotation. */
	SRT,

	/** Standard 120 degree rotation. */
	STR,

	/** Standard 240 degree rotation. */
	TRS,

	/** Reversed 120 degree rotation. */
	TSR;
}

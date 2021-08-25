/* ==================================================================
 * GeneralDatumSamplesContainer.java - 21/08/2021 3:30:58 PM
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

package net.solarnetwork.domain.datum;

/**
 * API for something that acts as a container for a {@link GeneralDatumSamples}
 * instance.
 * 
 * @author matt
 * @version 2.0
 * @since 1.78
 */
public interface DatumSamplesContainer {

	/**
	 * Create a copy this instance.
	 * 
	 * @param samples
	 *        the samples to use for the copy
	 * @return a new instance
	 */
	DatumSamplesContainer copyWithSamples(DatumSamples samples);

	/**
	 * Get the general datum samples held by this container.
	 * 
	 * @return the samples
	 */
	DatumSamples getSamples();

}

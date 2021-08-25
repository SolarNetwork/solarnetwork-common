/* ==================================================================
 * GeneralNodeDatumSamples.java - Aug 22, 2014 6:26:13 AM
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

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * A collection of different types of sample data, grouped by logical sample
 * type.
 * 
 * @author matt
 * @version 2.0
 */
@JsonPropertyOrder({ "i", "a", "s", "t" })
public class GeneralNodeDatumSamples extends DatumSamples implements Serializable {

	private static final long serialVersionUID = 1412166842747615064L;

}

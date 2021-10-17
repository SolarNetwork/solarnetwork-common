/* ==================================================================
 * DatumFilterService.java - 15/03/2019 9:41:46 am
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

package net.solarnetwork.service;

import java.util.Map;
import net.solarnetwork.domain.datum.Datum;
import net.solarnetwork.domain.datum.DatumSamplesOperations;

/**
 * A service API for transforming and filtering datum samples.
 * 
 * @author matt
 * @version 1.0
 * @since 2.0
 */
public interface DatumFilterService extends Identifiable {

	/**
	 * A parameter key signaling the transform is for testing purposes only, and
	 * as such should not have any persistent side-effects.
	 * 
	 * <p>
	 * The mere presence of this parameter should be treated as testing mode
	 * should be used. The parameter value can be anything.
	 * </p>
	 */
	String PARAM_TEST_ONLY = "test";

	/**
	 * Transform a samples instance.
	 * 
	 * <p>
	 * Generally this method is not meant to make changes to the passed in
	 * {@code samples} instance. Rather it should apply changes to a copy of
	 * {@code samples} and return the copy. If no changes are necessary then the
	 * {@code samples} instance may be returned.
	 * </p>
	 * 
	 * <p>
	 * This method may also return {@literal null} to indicate the
	 * {@code samples} instance should not be processed, or that there is
	 * essentially no data to associate with this particular {@code datum}.
	 * </p>
	 * 
	 * @param datum
	 *        the datum associated with {@code samples}
	 * @param samples
	 *        the samples object to transform
	 * @param parameters
	 *        optional implementation-specific parameters to pass to the
	 *        transformer
	 * @return the transformed samples instance, which may be the
	 *         {@code samples} instance or a new instance, or {@literal null} to
	 *         indicate the samples should be discarded
	 */
	DatumSamplesOperations filter(Datum datum, DatumSamplesOperations samples,
			Map<String, Object> parameters);

	/**
	 * Get a description of this transformer.
	 * 
	 * <p>
	 * This method should return a meaningful description of this service,
	 * suitable for users to see.
	 * </p>
	 * 
	 * @return a friendly description
	 */
	default String getDescription() {
		String uid = getUid();
		if ( uid != null ) {
			return String.format("%s (%s)", uid, getClass().getSimpleName());
		}
		return getClass().getSimpleName();
	}

}

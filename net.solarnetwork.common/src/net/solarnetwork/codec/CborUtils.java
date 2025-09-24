/* ==================================================================
 * CborUtils.java - 24/09/2025 4:05:59 pm
 *
 * Copyright 2025 SolarNetwork.net Dev Team
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

package net.solarnetwork.codec;

import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.dataformat.cbor.CBORFactoryBuilder;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import com.fasterxml.jackson.dataformat.cbor.CBORParser;

/**
 * CBOR helper methods.
 *
 * @author matt
 * @version 1.0
 * @since 4.5
 */
public final class CborUtils {

	private CborUtils() {
		// not available
	}

	/**
	 * Construct a standard CBORFactory.
	 *
	 * <p>
	 * The
	 * {@code CBORGenerator.Feature.ENCODE_USING_STANDARD_NEGATIVE_BIGINT_ENCODING}
	 * and
	 * {@code CBORParser.Feature.DECODE_USING_STANDARD_NEGATIVE_BIGINT_ENCODING}
	 * are both enabled in the returned factory.
	 * </p>
	 *
	 * @return the new factory instance
	 */
	public static CBORFactory cborFactory() {
		return new CBORFactoryBuilder(new CBORFactory())
				.configure(CBORGenerator.Feature.ENCODE_USING_STANDARD_NEGATIVE_BIGINT_ENCODING, true)
				.configure(CBORParser.Feature.DECODE_USING_STANDARD_NEGATIVE_BIGINT_ENCODING, true)
				.build();
	}

}

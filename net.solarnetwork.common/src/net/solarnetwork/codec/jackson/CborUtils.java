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

package net.solarnetwork.codec.jackson;

import tools.jackson.dataformat.cbor.CBORFactory;
import tools.jackson.dataformat.cbor.CBORFactoryBuilder;
import tools.jackson.dataformat.cbor.CBORMapper;
import tools.jackson.dataformat.cbor.CBORReadFeature;
import tools.jackson.dataformat.cbor.CBORWriteFeature;

/**
 * CBOR helper methods.
 *
 * @author matt
 * @version 1.2
 * @since 4.13
 */
public final class CborUtils {

	private CborUtils() {
		// not available
	}

	/**
	 * A default mapper for CBOR.
	 *
	 * <p>
	 * This mapper contains the {@link JsonDateUtils#JAVA_TIME_MODULE} and
	 * {@link JsonUtils#CORE_MODULE} modules.
	 * </p>
	 *
	 * @since 1.1
	 */
	public static final CBORMapper CBOR_OBJECT_MAPPER;
	static {
		var builder = CBORMapper.builder(cborFactory());
		JsonUtils.setupMapperBuilder(builder, JsonDateUtils.JAVA_TIME_MODULE, JsonUtils.CORE_MODULE);
		CBOR_OBJECT_MAPPER = builder.build();
	}

	/**
	 * Construct a standard CBORFactory.
	 *
	 * <p>
	 * The
	 * {@link CBORWriteFeature#ENCODE_USING_STANDARD_NEGATIVE_BIGINT_ENCODING}
	 * and
	 * {@link CBORReadFeature#DECODE_USING_STANDARD_NEGATIVE_BIGINT_ENCODING}
	 * are both enabled in the returned factory.
	 * </p>
	 *
	 * @return the new factory instance
	 */
	public static CBORFactory cborFactory() {
		return new CBORFactoryBuilder(new CBORFactory())
				.configure(CBORWriteFeature.ENCODE_USING_STANDARD_NEGATIVE_BIGINT_ENCODING, true)
				.configure(CBORReadFeature.DECODE_USING_STANDARD_NEGATIVE_BIGINT_ENCODING, true).build();
	}

}

/* ==================================================================
 * GeneralDatumMetadataSerializer.java - 13/11/2016 12:25:06 PM
 *
 * Copyright 2007-2016 SolarNetwork.net Dev Team
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

import java.io.IOException;
import java.io.Serial;
import org.jspecify.annotations.Nullable;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import net.solarnetwork.domain.datum.GeneralDatumMetadata;

/**
 * JSON serializer for {@link GeneralDatumMetadata}.
 *
 * @author matt
 * @version 1.0
 * @since 4.55
 */
public class GeneralDatumMetadataSerializer extends StdSerializer<GeneralDatumMetadata> {

	@Serial
	private static final long serialVersionUID = 1877189885521795135L;

	/** A default instance. */
	public static final GeneralDatumMetadataSerializer INSTANCE = new GeneralDatumMetadataSerializer();

	/**
	 * Constructor.
	 */
	public GeneralDatumMetadataSerializer() {
		super(GeneralDatumMetadata.class);
	}

	@Override
	public void serialize(@Nullable GeneralDatumMetadata value, JsonGenerator gen,
			SerializerProvider provider) throws IOException {
		if ( value == null ) {
			gen.writeNull();
			return;
		}

		// @formatter:off
		final int size =
				  (value.getM() != null ? 1 : 0)
				+ (value.getPm() != null ? 1 : 0)
				+ (value.getT() != null ? 1 : 0)
				;
		// @formatter:on
		gen.writeStartObject(value, size);
		JsonUtils.writeMetadata(gen, value);
		gen.writeEndObject();
	}

}

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
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.util.NameTransformer;
import net.solarnetwork.domain.datum.GeneralDatumMetadata;

/**
 * JSON serializer for {@link GeneralDatumMetadata}.
 *
 * <p>
 * This serializer supports {@code @JsonUnwrapped} properties, by writing the
 * metadata properties directly into the containing object.
 * </p>
 *
 * @author matt
 * @version 1.1
 * @since 4.55
 */
public class GeneralDatumMetadataSerializer extends StdSerializer<GeneralDatumMetadata> {

	@Serial
	private static final long serialVersionUID = 1877189885521795135L;

	/** A default instance. */
	public static final GeneralDatumMetadataSerializer INSTANCE = new GeneralDatumMetadataSerializer();

	/**
	 * The unwrapped property name transformer, or {@code null} if not
	 * unwrapping.
	 */
	private final @Nullable NameTransformer unwrapper;

	/**
	 * Constructor.
	 */
	public GeneralDatumMetadataSerializer() {
		this(null);
	}

	private GeneralDatumMetadataSerializer(@Nullable NameTransformer unwrapper) {
		super(GeneralDatumMetadata.class);
		this.unwrapper = unwrapper;
	}

	@Override
	public boolean isUnwrappingSerializer() {
		return (unwrapper != null);
	}

	@Override
	public JsonSerializer<GeneralDatumMetadata> unwrappingSerializer(NameTransformer unwrapper) {
		return new GeneralDatumMetadataSerializer(
				this.unwrapper != null ? NameTransformer.chainedTransformer(unwrapper, this.unwrapper)
						: unwrapper);
	}

	@Override
	public void serialize(@Nullable GeneralDatumMetadata value, JsonGenerator gen,
			SerializerProvider provider) throws IOException {
		if ( unwrapper != null ) {
			// write the properties into the containing object
			JsonUtils.writeMetadata(gen, value, unwrapper);
			return;
		}
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

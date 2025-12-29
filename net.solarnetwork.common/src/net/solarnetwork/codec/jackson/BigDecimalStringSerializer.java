/* ==================================================================
 * BigDecimalStringSerializer.java - 28/08/2017 9:10:48 AM
 *
 * Copyright 2017 SolarNetwork.net Dev Team
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

import java.math.BigDecimal;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonToken;
import tools.jackson.core.type.WritableTypeId;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import tools.jackson.databind.jsontype.TypeSerializer;
import tools.jackson.databind.ser.std.StdScalarSerializer;

/**
 * Specialized serializer of {@link BigDecimal} to string values.
 *
 * @author matt
 * @version 1.0
 * @since 4.13
 */
public class BigDecimalStringSerializer extends StdScalarSerializer<BigDecimal> {

	/**
	 * Singleton instance to use.
	 */
	public final static BigDecimalStringSerializer INSTANCE = new BigDecimalStringSerializer();

	/**
	 * Default constructor.
	 */
	public BigDecimalStringSerializer() {
		super(BigDecimal.class);
	}

	@Override
	public boolean isEmpty(SerializationContext prov, BigDecimal value) {
		if ( value == null ) {
			return true;
		}
		String str = value.toString();
		return str.isEmpty();
	}

	@Override
	public void serialize(BigDecimal value, JsonGenerator gen, SerializationContext provider)
			throws JacksonException {
		gen.writeString(value.toPlainString());
	}

	@Override
	public void serializeWithType(BigDecimal value, JsonGenerator gen, SerializationContext provider,
			TypeSerializer typeSer) throws JacksonException {
		WritableTypeId typeIdDef = typeSer.writeTypePrefix(gen, provider,
				typeSer.typeId(value, JsonToken.VALUE_STRING));
		serialize(value, gen, provider);
		typeSer.writeTypeSuffix(gen, provider, typeIdDef);
	}

	@Override
	public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint)
			throws JacksonException {
		if ( visitor != null ) {
			visitor.expectStringFormat(typeHint);
		}
	}
}

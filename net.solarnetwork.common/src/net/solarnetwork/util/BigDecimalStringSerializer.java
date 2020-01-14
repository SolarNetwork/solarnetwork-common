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

package net.solarnetwork.util;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * Specialized serializer of {@link BigDecimal} to string values.
 * 
 * @author matt
 * @version 1.1
 * @since 1.37
 */
public class BigDecimalStringSerializer extends StdSerializer<BigDecimal> {

	private static final long serialVersionUID = 4462532770316408808L;

	/**
	 * Singleton instance to use.
	 */
	public final static BigDecimalStringSerializer INSTANCE = new BigDecimalStringSerializer();

	/**
	 * Default constructor.
	 * <p>
	 * Note: usually you should NOT create new instances, but instead use
	 * {@link #INSTANCE} which is stateless and fully thread-safe. However,
	 * there are cases where constructor is needed; for example, when using
	 * explicit serializer annotations like
	 * {@link com.fasterxml.jackson.databind.annotation.JsonSerialize#using}.
	 * </p>
	 */
	public BigDecimalStringSerializer() {
		super(BigDecimal.class);
	}

	/**
	 * Construct with specific class.
	 * 
	 * @param handledType
	 *        the type to use
	 */
	public BigDecimalStringSerializer(Class<? extends BigDecimal> handledType) {
		super(handledType, false);
	}

	@Override
	public boolean isEmpty(SerializerProvider prov, BigDecimal value) {
		if ( value == null ) {
			return true;
		}
		String str = value.toString();
		return str.isEmpty();
	}

	@Override
	public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider provider)
			throws IOException {
		gen.writeString(value.toPlainString());
	}

	@Override
	public void serializeWithType(BigDecimal value, JsonGenerator gen, SerializerProvider provider,
			TypeSerializer typeSer) throws IOException {
		WritableTypeId typeIdDef = typeSer.writeTypePrefix(gen,
				typeSer.typeId(value, JsonToken.VALUE_STRING));
		serialize(value, gen, provider);
		typeSer.writeTypeSuffix(gen, typeIdDef);
	}

	@Override
	public JsonNode getSchema(SerializerProvider provider, Type typeHint) throws JsonMappingException {
		return createSchemaNode("string", true);
	}

	@Override
	public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint)
			throws JsonMappingException {
		if ( visitor != null ) {
			visitor.expectStringFormat(typeHint);
		}
	}
}

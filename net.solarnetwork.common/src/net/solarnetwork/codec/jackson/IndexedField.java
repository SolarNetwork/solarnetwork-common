/* ==================================================================
 * IndexedField.java - 6/06/2021 5:52:45 PM
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

package net.solarnetwork.codec.jackson;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.SerializationContext;

/**
 * API for a JSON field that is ordered by an index value for the purposes of
 * serialization.
 *
 * <p>
 * This API is designed to be implemented by {@code Enum} types, to facilitate a
 * single definition of JSON object field names and their order when serializing
 * and deserializing a related object into/from JSON, without using reflection.
 * See
 * {@link JsonUtils#parseIndexedFieldsObject(JsonParser, DeserializationContext, Object[], Map)}
 * for help in parsing JSON using this structure. The intention is that a JSON
 * serializer and deserializer class pair would share a common {@code Enum} that
 * implements this interface to help ensure a consistent JSON structure is used
 * by both.
 * </p>
 *
 * @author matt
 * @version 1.0
 * @since 4.13
 */
public interface IndexedField {

	/**
	 * Get the index.
	 *
	 * @return the index
	 */
	int getIndex();

	/**
	 * Get the field name.
	 *
	 * @return the fieldName
	 */
	String getFieldName();

	/**
	 * Parse a value from a parser.
	 *
	 * @param parser
	 *        the parser
	 * @param ctxt
	 *        the context
	 * @return the parsed object
	 * @throws JacksonException
	 *         if any JSON processing error occurs
	 */
	Object parseValue(JsonParser parser, DeserializationContext ctxt) throws JacksonException;

	/**
	 * Write a value to a generator.
	 *
	 * @param generator
	 *        the generator
	 * @param ctxt
	 *        the provider
	 * @param value
	 *        the value to write
	 * @throws JacksonException
	 *         if any JSON processing error occurs
	 */
	void writeValue(JsonGenerator generator, SerializationContext ctxt, Object value)
			throws JacksonException;

	/**
	 * Get a mapping of field names to {@link IndexedField} instances from an
	 * enumeration that implements {@link IndexedField}.
	 *
	 * @param <E>
	 *        the enum type
	 * @param clazz
	 *        the enum class
	 * @return the map
	 */
	static <E extends Enum<E> & IndexedField> Map<String, E> fieldMap(Class<E> clazz) {
		return Collections.unmodifiableMap(Arrays.stream(clazz.getEnumConstants())
				.collect(Collectors.toMap(e -> e.getFieldName(), Function.identity())));
	}

}

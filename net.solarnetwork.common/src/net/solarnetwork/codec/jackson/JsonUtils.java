/* ==================================================================
 * JsonUtils.java - 15/05/2015 11:46:24 am
 *
 * Copyright 2007 SolarNetwork.net Dev Team
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
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalQuery;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.annotation.JsonInclude;
import net.solarnetwork.domain.BasicIdentityLocation;
import net.solarnetwork.domain.Bitmaskable;
import net.solarnetwork.domain.Instruction;
import net.solarnetwork.domain.InstructionStatus;
import net.solarnetwork.domain.Location;
import net.solarnetwork.domain.SecurityPolicy;
import net.solarnetwork.domain.datum.Datum;
import net.solarnetwork.domain.datum.GeneralDatumMetadata;
import net.solarnetwork.domain.datum.ObjectDatumStreamDataSet;
import net.solarnetwork.domain.datum.ObjectDatumStreamMetadata;
import net.solarnetwork.domain.datum.ObjectDatumStreamMetadataId;
import net.solarnetwork.domain.datum.StreamDatum;
import net.solarnetwork.util.Half;
import net.solarnetwork.util.NumberUtils;
import net.solarnetwork.util.StringUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.TreeNode;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.cfg.MapperBuilder;
import tools.jackson.databind.exc.InvalidFormatException;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

/**
 * Utilities for JSON data.
 *
 * <p>
 * The {@link ObjectMapper} used internally by this class supports:
 * </p>
 *
 * <ul>
 * <li>{@code java.time} date/time values, serialized as strings using the RFC
 * 3339 profile of ISO-8601 with a space separator between date/time sections
 * instead of a {@literal T} character.</li>
 * <li>{@literal null} values are not serialized.</li>
 * <li>Floating point numbers are deserialized as {@link java.math.BigDecimal}
 * instances.</li>
 * </ul>
 *
 * @author matt
 * @version 1.2
 * @since 4.13
 */
public final class JsonUtils {

	private static final Logger LOG = LoggerFactory.getLogger(JsonUtils.class);

	/** A type reference for a Map with string keys. */
	public static final TypeReference<LinkedHashMap<String, Object>> STRING_MAP_TYPE = new StringMapTypeReference();

	/**
	 * A module for handling core objects.
	 */
	public static final JacksonModule CORE_MODULE;
	static {
		SimpleModule m = new SimpleModule("SolarNetwork Core");
		m.addSerializer(BasicLocationSerializer.INSTANCE);
		m.addSerializer(BasicInstructionSerializer.INSTANCE);
		m.addSerializer(BasicInstructionStatusSerializer.INSTANCE);
		m.addSerializer(SecurityPolicySerializer.INSTANCE);
		m.addDeserializer(BasicIdentityLocation.class, BasicIdentityLocationDeserializer.INSTANCE);
		m.addDeserializer(Location.class, BasicLocationDeserializer.INSTANCE);
		m.addDeserializer(Instruction.class, BasicInstructionDeserializer.INSTANCE);
		m.addDeserializer(InstructionStatus.class, BasicInstructionStatusDeserializer.INSTANCE);
		m.addDeserializer(SecurityPolicy.class, BasicSecurityPolicyDeserializer.INSTANCE);
		CORE_MODULE = m;
	}

	/**
	 * A module for handling datum objects.
	 */
	public static final JacksonModule DATUM_MODULE;
	static {
		SimpleModule m = new SimpleModule("SolarNetwork Datum");
		m.addSerializer(BasicGeneralDatumSerializer.INSTANCE);
		m.addSerializer(BasicObjectDatumStreamMetadataSerializer.INSTANCE);
		m.addSerializer(BasicStreamDatumArraySerializer.INSTANCE);
		m.addSerializer(ObjectDatumStreamMetadataId.class,
				BasicObjectDatumStreamMetadataIdSerializer.INSTANCE);
		m.addSerializer(BasicObjectDatumStreamDataSetSerializer.INSTANCE);

		m.addDeserializer(Datum.class, BasicGeneralDatumDeserializer.INSTANCE);
		m.addDeserializer(ObjectDatumStreamMetadata.class,
				BasicObjectDatumStreamMetadataDeserializer.INSTANCE);
		m.addDeserializer(StreamDatum.class, BasicStreamDatumArrayDeserializer.INSTANCE);
		m.addDeserializer(ObjectDatumStreamMetadataId.class,
				BasicObjectDatumStreamMetadataIdDeserializer.INSTANCE);
		m.addDeserializer(ObjectDatumStreamDataSet.class,
				BasicObjectDatumStreamDataSetDeserializer.INSTANCE);
		DATUM_MODULE = m;
	}

	/**
	 * A default mapper for JSON.
	 *
	 * <p>
	 * This mapper contains the {@link JsonDateUtils#JAVA_TIME_MODULE} and
	 * {@link #CORE_MODULE} modules.
	 * </p>
	 */
	public static final JsonMapper JSON_OBJECT_MAPPER;
	static {
		var builder = JsonMapper.builder();
		setupMapperBuilder(builder, JsonDateUtils.JAVA_TIME_MODULE, CORE_MODULE);
		JSON_OBJECT_MAPPER = builder.build();
	}

	/**
	 * Configure a mapper builder with SolarNetwork standard settings.
	 *
	 * @param <M>
	 *        the mapper type
	 * @param <B>
	 *        the builder type
	 * @param builder
	 *        the builder
	 * @param modules
	 *        optional modules to register; can be completely omitted and
	 *        individual elements are allowed to be {@literal null} (e.g.
	 *        optionally missing modules)
	 * @throws JacksonException
	 *         if any error occurs
	 */
	public static final <M extends ObjectMapper, B extends MapperBuilder<M, B>> void setupMapperBuilder(
			MapperBuilder<M, B> builder, JacksonModule... modules) throws JacksonException {
		// @formatter:off
		builder.changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
				.changeDefaultPropertyInclusion(incl -> incl.withContentInclusion(JsonInclude.Include.NON_NULL))
				.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
				.disable(DateTimeFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS,
						DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS,
						DateTimeFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
				.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
				.defaultDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS'Z'", Locale.ENGLISH))
				.defaultTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC))
				;
		// @formatter:on

		if ( modules != null ) {
			for ( JacksonModule module : modules ) {
				if ( module != null ) {
					builder.addModule(module);
				}
			}
		}
	}

	private static final class StringMapTypeReference
			extends TypeReference<LinkedHashMap<String, Object>> {

		public StringMapTypeReference() {
			super();
		}

	}

	// don't construct me
	private JsonUtils() {
		super();
	}

	/**
	 * Convert an object to a JSON string.
	 *
	 * @param o
	 *        the object to serialize to JSON
	 * @return the JSON string, or {@literal null} if {@code o} is
	 *         {@literal null} or any error occurs serializing the object to
	 *         JSON
	 * @see #getJSONString(Object, String)
	 * @since 2.3
	 */
	public static String getJSONString(final Object o) {
		return getJSONString(o, null);
	}

	/**
	 * Convert an object to a JSON string.
	 *
	 * <p>
	 * This is designed for simple values. An internal {@link ObjectMapper} will
	 * be used, and null values will not be included in the output. All
	 * exceptions while serializing the object are caught and ignored.
	 * </p>
	 *
	 * @param o
	 *        the object to serialize to JSON
	 * @param defaultValue
	 *        a default value to use if {@code o} is {@literal null} or if any
	 *        error occurs serializing the object to JSON
	 * @return the JSON string
	 */
	public static String getJSONString(final Object o, final String defaultValue) {
		String result = defaultValue;
		if ( o != null ) {
			try {
				return JSON_OBJECT_MAPPER.writeValueAsString(o);
			} catch ( Exception e ) {
				LOG.warn("Exception marshalling {} to JSON", o, e);
			}
		}
		return result;
	}

	/**
	 * Convert a JSON string to an object.
	 *
	 * <p>
	 * This is designed for simple values. An internal {@link ObjectMapper} will
	 * be used, and all floating point values will be converted to
	 * {@link BigDecimal} values to faithfully represent the data. All
	 * exceptions while deserializing the object are caught and ignored.
	 * </p>
	 *
	 * @param <T>
	 *        the desired object type
	 * @param json
	 *        the JSON string to convert
	 * @param clazz
	 *        the type of Object to map the JSON into
	 * @return the object
	 */
	public static <T> T getObjectFromJSON(final String json, Class<T> clazz) {
		T result = null;
		if ( json != null ) {
			try {
				result = JSON_OBJECT_MAPPER.readValue(json, clazz);
			} catch ( Exception e ) {
				LOG.warn("Exception deserialzing json {}", json, e);
			}
		}
		return result;
	}

	/**
	 * Convert a JSON string to a Map with string keys.
	 *
	 * <p>
	 * This is designed for simple values. An internal {@link ObjectMapper} will
	 * be used, and all floating point values will be converted to
	 * {@link BigDecimal} values to faithfully represent the data. All
	 * exceptions while deserializing the object are caught and ignored.
	 * </p>
	 *
	 * @param json
	 *        the JSON to convert
	 * @return the map, or {@literal null} if {@code json} is {@literal null} or
	 *         empty, or any exception occurs generating the JSON
	 */
	public static Map<String, Object> getStringMap(final String json) {
		if ( json == null || json.length() < 1 ) {
			return null;
		}
		try {
			return JSON_OBJECT_MAPPER.readValue(json, STRING_MAP_TYPE);
		} catch ( Exception e ) {
			LOG.warn("Exception deserialzing JSON {} to Map<String, Object>", json, e);
		}
		return null;
	}

	/**
	 * Convert a JSON tree object to a Map with string keys.
	 *
	 * <p>
	 * This is designed for simple values. An internal {@link ObjectMapper} will
	 * be used, and all floating point values will be converted to
	 * {@link BigDecimal} values to faithfully represent the data. All
	 * exceptions while deserializing the object are caught and ignored.
	 * </p>
	 *
	 * @param node
	 *        the JSON object to convert
	 * @return the map, or {@literal null} if {@code node} is not a JSON object,
	 *         is {@literal null}, or any exception occurs generating the JSON
	 */
	public static Map<String, Object> getStringMapFromTree(final JsonNode node) {
		if ( node == null || !node.isObject() ) {
			return null;
		}
		try {
			return JSON_OBJECT_MAPPER.readValue(JSON_OBJECT_MAPPER.treeAsTokens(node), STRING_MAP_TYPE);
		} catch ( Exception e ) {
			LOG.warn("Exception deserialzing JSON node {} to Map<String, Object>", node, e);
		}
		return null;
	}

	/**
	 * Convert an object into a JSON tree.
	 *
	 * @param o
	 *        the object to convert
	 * @return the JSON tree, or {@literal null} if {@code o} is
	 *         {@literal null}, or any exception occurs generating the JSON
	 */
	public static JsonNode getTreeFromObject(final Object o) {
		if ( o == null ) {
			return null;
		}
		try {
			return JSON_OBJECT_MAPPER.valueToTree(o);
		} catch ( Exception e ) {
			LOG.warn("Exception serialzing object {} to JsonNode", o, e);
		}
		return null;
	}

	/**
	 * Convert an object into a Map with string keys.
	 *
	 * @param o
	 *        the object to convert
	 * @return the map, or {@literal null} if {@code node} is not a JSON object,
	 *         is {@literal null}, or any exception occurs generating the JSON
	 */
	public static Map<String, Object> getStringMapFromObject(final Object o) {
		return getStringMapFromTree(getTreeFromObject(o));
	}

	/**
	 * Write metadata to a JSON generator.
	 *
	 * @param generator
	 *        The generator to write to.
	 * @param meta
	 *        The metadata to write.
	 * @throws JacksonException
	 *         if any IO error occurs
	 */
	public static void writeMetadata(JsonGenerator generator, GeneralDatumMetadata meta)
			throws JacksonException {
		if ( meta == null ) {
			return;
		}
		Map<String, Object> m = meta.getM();
		if ( m != null ) {
			generator.writePOJOProperty("m", m);
		}

		Map<String, Map<String, Object>> pm = meta.getPm();
		if ( pm != null ) {
			generator.writePOJOProperty("pm", pm);
		}

		Set<String> t = meta.getT();
		if ( t != null ) {
			generator.writeArrayPropertyStart("t");
			for ( String tag : t ) {
				generator.writeString(tag);
			}
			generator.writeEndArray();
		}
	}

	/**
	 * Parse a BigDecimal from a JSON object attribute value.
	 *
	 * @param node
	 *        the JSON node (e.g. object)
	 * @param key
	 *        the attribute key to obtain from {@code node}
	 * @return the parsed {@link BigDecimal}, or {@literal null} if an error
	 *         occurs or the specified attribute {@code key} is not available
	 */
	public static BigDecimal parseBigDecimalAttribute(JsonNode node, String key) {
		BigDecimal num = null;
		if ( node != null ) {
			JsonNode attrNode = node.get(key);
			if ( attrNode != null && !attrNode.isNull() ) {
				String txt = attrNode.asString();
				if ( txt.indexOf('.') < 0 ) {
					txt += ".0"; // force to decimal notation, so round-trip into samples doesn't result in int
				}
				try {
					num = new BigDecimal(txt);
				} catch ( NumberFormatException e ) {
					LOG.debug("Error parsing decimal attribute [{}] value [{}]: {}",
							new Object[] { key, attrNode, e.getMessage() });
				}
			}
		}
		return num;
	}

	/**
	 * Parse a date from a JSON object attribute value.
	 *
	 * <p>
	 * If the date cannot be parsed, {@literal null} will be returned.
	 * </p>
	 *
	 * @param <T>
	 *        the date type
	 * @param node
	 *        the JSON node (e.g. object)
	 * @param key
	 *        the attribute key to obtain from {@code node}
	 * @param dateFormat
	 *        the date format to use to parse the date string
	 * @param query
	 *        the temporal query, e.g. {@code Instant::from}
	 * @return the parsed date instance, or {@literal null} if an error occurs
	 *         or the specified attribute {@code key} is not available
	 */
	public static <T> T parseDateAttribute(TreeNode node, String key, DateTimeFormatter dateFormat,
			TemporalQuery<T> query) {
		T result = null;
		if ( node != null ) {
			TreeNode attrNode = node.get(key);
			if ( attrNode instanceof JsonNode && !((JsonNode) attrNode).isNull() ) {
				try {
					String dateString = ((JsonNode) attrNode).asString();

					// replace "midnight" with 12:00am
					dateString = dateString.replaceAll("(?i)midnight", "12:00am");

					// replace "noon" with 12:00pm
					dateString = dateString.replaceAll("(?i)noon", "12:00pm");

					result = dateFormat.parse(dateString, query);
				} catch ( DateTimeParseException e ) {
					LOG.debug("Error parsing date attribute [{}] value [{}] using pattern {}: {}", key,
							attrNode, dateFormat, e.getMessage());
				}
			}
		}
		return result;
	}

	/**
	 * Parse a Integer from a JSON object attribute value.
	 *
	 * If the Integer cannot be parsed, {@literal null} will be returned.
	 *
	 * @param node
	 *        the JSON node (e.g. object)
	 * @param key
	 *        the attribute key to obtain from {@code node} node
	 * @return the parsed {@link Integer}, or {@literal null} if an error occurs
	 *         or the specified attribute {@code key} is not available
	 */
	public static Integer parseIntegerAttribute(JsonNode node, String key) {
		Integer num = null;
		if ( node != null ) {
			JsonNode attrNode = node.get(key);
			if ( attrNode != null && !attrNode.isNull() ) {
				if ( attrNode.isIntegralNumber() ) {
					num = attrNode.asInt();
				} else {
					String s = attrNode.asString();
					if ( s != null ) {
						s = s.trim();
					}
					try {
						num = Integer.valueOf(s);
					} catch ( NumberFormatException e ) {
						LOG.debug("Error parsing integer attribute [{}] value [{}]: {}",
								new Object[] { key, attrNode, e.getMessage() });
					}
				}
			}
		}
		return num;
	}

	/**
	 * Parse a Long from a JSON object attribute value.
	 *
	 * If the Long cannot be parsed, {@literal null} will be returned.
	 *
	 * @param node
	 *        the JSON node (e.g. object)
	 * @param key
	 *        the attribute key to obtain from {@code node} node
	 * @return the parsed {@link Long}, or {@literal null} if an error occurs or
	 *         the specified attribute {@code key} is not available
	 */
	public static Long parseLongAttribute(JsonNode node, String key) {
		Long num = null;
		if ( node != null ) {
			JsonNode attrNode = node.get(key);
			if ( attrNode != null && !attrNode.isNull() ) {
				if ( attrNode.isIntegralNumber() ) {
					num = attrNode.asLong();
				} else {
					try {
						num = Long.valueOf(attrNode.asString());
					} catch ( NumberFormatException e ) {
						LOG.debug("Error parsing long attribute [{}] value [{}]: {}",
								new Object[] { key, attrNode, e.getMessage() });
					}
				}
			}
		}
		return num;
	}

	/**
	 * Parse a String from a JSON object attribute value.
	 *
	 * If the String cannot be parsed, {@literal null} will be returned.
	 *
	 * @param node
	 *        the JSON node (e.g. object)
	 * @param key
	 *        the attribute key to obtain from {@code node} node
	 * @return the parsed {@link String}, or {@literal null} if an error occurs
	 *         or the specified attribute {@code key} is not available
	 */
	public static String parseStringAttribute(JsonNode node, String key) {
		String s = null;
		if ( node != null ) {
			JsonNode attrNode = node.get(key);
			if ( attrNode != null && !attrNode.isNull() ) {
				s = attrNode.asString();
			}
		}
		return s;
	}

	/**
	 * Parse a String from a JSON object attribute value.
	 *
	 * If the String cannot be parsed, {@literal null} will be returned.
	 *
	 * @param node
	 *        the JSON node (e.g. object)
	 * @param key
	 *        the attribute key to obtain from {@code node} node
	 * @return the parsed {@link String}, or {@literal null} if an error occurs
	 *         or the specified attribute {@code key} is not available
	 */
	public static String parseNonEmptyStringAttribute(JsonNode node, String key) {
		return StringUtils.nonEmptyString(parseStringAttribute(node, key));
	}

	/**
	 * Parse a JSON array of scalar values into a string array.
	 *
	 * @param p
	 *        the parser
	 * @return the parsed string array
	 * @throws JacksonException
	 *         if any IO error occurs
	 */
	public static String[] parseStringArray(JsonParser p) throws JacksonException {
		JsonToken t = p.nextToken();
		if ( p.isExpectedStartArrayToken() ) {
			List<String> l = new ArrayList<>(8);
			do {
				t = p.nextToken();
				if ( t != null ) {
					if ( t.isScalarValue() ) {
						l.add(p.getValueAsString());
					} else if ( t != JsonToken.END_ARRAY ) {
						// assume null
						l.add(null);
					}
				}
			} while ( t != null && t != JsonToken.END_ARRAY );
			return l.toArray(new String[l.size()]);
		}
		return null;
	}

	/**
	 * Parse a JSON array of scalar values into a long array.
	 *
	 * @param p
	 *        the parser
	 * @return the parsed long array
	 * @throws JacksonException
	 *         if any IO error occurs
	 */
	public static Long[] parseLongArray(JsonParser p) throws JacksonException {
		JsonToken t = p.nextToken();
		if ( p.isExpectedStartArrayToken() ) {
			List<Long> l = new ArrayList<>(8);
			do {
				t = p.nextToken();
				if ( t != null ) {
					if ( t.isNumeric() ) {
						l.add(p.getValueAsLong());
					} else if ( t != JsonToken.END_ARRAY ) {
						// assume null
						l.add(null);
					}
				}
			} while ( t != null && t != JsonToken.END_ARRAY );
			return l.toArray(new Long[l.size()]);
		}
		return null;
	}

	/**
	 * Write a string array as a JSON array of strings.
	 *
	 * @param generator
	 *        the generator to write to
	 * @param array
	 *        the array to write
	 * @throws JacksonException
	 *         if any IO error occurs
	 */
	public static void writeStringArray(JsonGenerator generator, String[] array)
			throws JacksonException {
		if ( array != null && array.length > 0 ) {
			generator.writeStartArray(array, array.length);
			for ( int i = 0; i < array.length; i++ ) {
				String s = array[i];
				if ( s != null ) {
					generator.writeString(array[i]);
				} else {
					generator.writeNull();
				}
			}
			generator.writeEndArray();
		} else {
			generator.writeNull();
		}
	}

	/**
	 * Write a string array as a JSON object field that is an array of strings.
	 *
	 * @param generator
	 *        the generator to write to
	 * @param fieldName
	 *        the field name
	 * @param array
	 *        the array to write
	 * @throws JacksonException
	 *         if any IO error occurs
	 */
	public static void writeStringArrayField(JsonGenerator generator, String fieldName, String[] array)
			throws JacksonException {
		if ( array != null && array.length > 0 ) {
			generator.writeName(fieldName);
			writeStringArray(generator, array);
		}
	}

	/**
	 * Write a fixed number of string array values as JSON array numbers.
	 *
	 * <p>
	 * This method does not write any starting or ending JSON array, it only
	 * writes the values. It always writes {@code count} values, regardless of
	 * the length of {@code array}. JSON {@literal null} values will be written
	 * for any missing {@code array} values.
	 * </p>
	 *
	 * @param generator
	 *        the generator to write to
	 * @param array
	 *        the array values to write
	 * @param count
	 *        the number of string values to write
	 * @throws JacksonException
	 *         if any IO error occurs
	 */
	public static void writeStringArrayValues(final JsonGenerator generator, final String[] array,
			final int count) throws JacksonException {
		int i;
		final int arrayLen = (array != null ? array.length : 0);
		for ( i = 0; i < count && i < arrayLen; i++ ) {
			if ( array[i] != null ) {
				generator.writeString(array[i]);
			} else {
				generator.writeNull();
			}
		}
		for ( ; i < count; i++ ) {
			generator.writeNull();
		}
	}

	/**
	 * Write a string array as a JSON array of numbers.
	 *
	 * @param generator
	 *        the generator to write to
	 * @param array
	 *        the array to write
	 * @throws JacksonException
	 *         if any IO error occurs
	 */
	public static void writeDecimalArray(JsonGenerator generator, BigDecimal[] array)
			throws JacksonException {
		if ( array != null && array.length > 0 ) {
			generator.writeStartArray(array, array.length);
			for ( int i = 0; i < array.length; i++ ) {
				BigDecimal s = array[i];
				if ( s != null ) {
					generator.writeNumber(array[i]);
				} else {
					generator.writeNull();
				}
			}
			generator.writeEndArray();
		} else {
			generator.writeNull();
		}
	}

	/**
	 * Write a fixed number of decimal array values as JSON array numbers.
	 *
	 * <p>
	 * This method does not write any starting or ending JSON array, it only
	 * writes the values. It always writes {@code count} values, regardless of
	 * the length of {@code array}. JSON {@literal null} values will be written
	 * for any missing {@code array} values.
	 * </p>
	 *
	 * @param generator
	 *        the generator to write to
	 * @param array
	 *        the array values to write
	 * @param count
	 *        the number of string values to write
	 * @throws JacksonException
	 *         if any IO error occurs
	 */
	public static void writeDecimalArrayValues(final JsonGenerator generator, final BigDecimal[] array,
			final int count) throws JacksonException {
		int i;
		final int arrayLen = (array != null ? array.length : 0);
		for ( i = 0; i < count && i < arrayLen; i++ ) {
			if ( array[i] != null ) {
				generator.writeNumber(array[i]);
			} else {
				generator.writeNull();
			}
		}
		for ( ; i < count; i++ ) {
			generator.writeNull();
		}
	}

	/**
	 * Write a number field value using the smallest possible number type.
	 *
	 * <p>
	 * If {@code value} is {@literal null} then <b>nothing</b> will be
	 * generated.
	 * </p>
	 *
	 * @param gen
	 *        the JSON generator
	 * @param fieldName
	 *        the field name
	 * @param value
	 *        the number value
	 * @throws JacksonException
	 *         if any IO error occurs
	 */
	public static void writeNumberField(JsonGenerator gen, String fieldName, Number value)
			throws JacksonException {
		if ( value == null ) {
			return;
		}
		if ( value instanceof Double ) {
			gen.writeNumberProperty(fieldName, (Double) value);
		} else if ( value instanceof Float ) {
			gen.writeNumberProperty(fieldName, (Float) value);
		} else if ( value instanceof Long ) {
			gen.writeNumberProperty(fieldName, (Long) value);
		} else if ( value instanceof Integer ) {
			gen.writeNumberProperty(fieldName, (Integer) value);
		} else if ( value instanceof Short ) {
			gen.writeNumberProperty(fieldName, (Short) value);
		} else if ( value instanceof BigInteger ) {
			gen.writeNumberProperty(fieldName, (BigInteger) value);
		} else {
			BigDecimal d = NumberUtils.bigDecimalForNumber(value);
			if ( d != null ) {
				gen.writeNumberProperty(fieldName, d);
			}
		}
	}

	/**
	 * Write a timestamp field value in ISO 8601 form.
	 *
	 * <p>
	 * If {@code value} is {@literal null} then <b>nothing</b> will be
	 * generated.
	 * </p>
	 *
	 * @param gen
	 *        the JSON generator
	 * @param fieldName
	 *        the field name
	 * @param value
	 *        the instant value
	 * @throws JacksonException
	 *         if any IO error occurs
	 */
	public static void writeIso8601Timestamp(JsonGenerator gen, String fieldName, Instant value)
			throws JacksonException {
		if ( value == null ) {
			return;
		}
		gen.writeStringProperty(fieldName, DateTimeFormatter.ISO_INSTANT.format(value));
	}

	/**
	 * Write a bitmask set as a field number value.
	 *
	 * <p>
	 * If {@code value} is {@literal null} or empty then <b>nothing</b> will be
	 * generated.
	 * </p>
	 *
	 * @param gen
	 *        the JSON generator
	 * @param fieldName
	 *        the field name
	 * @param value
	 *        the instant value
	 * @throws JacksonException
	 *         if any IO error occurs
	 */
	public static void writeBitmaskValue(JsonGenerator gen, String fieldName,
			Set<? extends Bitmaskable> value) throws JacksonException {
		int v = Bitmaskable.bitmaskValue(value);
		if ( v > 0 ) {
			gen.writeNumberProperty(fieldName, v);
		}
	}

	/**
	 * Parse a JSON numeric value into a {@link BigDecimal}.
	 *
	 * @param p
	 *        the parser
	 * @return the decimal
	 * @throws JacksonException
	 *         if any IO error occurs
	 */
	public static BigDecimal parseDecimal(JsonParser p) throws JacksonException {
		JsonToken t = p.nextToken();
		if ( t != null ) {
			if ( t.isNumeric() ) {
				return p.getDecimalValue();
			} else if ( t == JsonToken.VALUE_STRING ) {
				// try to parse number string
				try {
					return new BigDecimal(p.getValueAsString());
				} catch ( NumberFormatException | ArithmeticException e ) {
					String msg = e.getMessage();
					if ( msg == null || msg.isEmpty() ) {
						msg = "Invalid number value: " + p.getValueAsString();
					}
					throw new InvalidFormatException(p, msg, p.getValueAsString(), BigDecimal.class);
				}
			}
		}
		return null;
	}

	/**
	 * Parse a JSON numeric value into a {@link Long}.
	 *
	 * @param p
	 *        the parser
	 * @return the long
	 * @throws JacksonException
	 *         if any IO error occurs
	 */
	public static Long parseLong(JsonParser p) throws JacksonException {
		JsonToken t = p.nextToken();
		if ( t != null ) {
			if ( t.isNumeric() ) {
				return p.getLongValue();
			} else if ( t == JsonToken.VALUE_STRING ) {
				// try to parse number string
				try {
					return Long.valueOf(p.getValueAsString());
				} catch ( NumberFormatException | ArithmeticException e ) {
					String msg = e.getMessage();
					if ( msg == null || msg.isEmpty() ) {
						msg = "Invalid number value: " + p.getValueAsString();
					}
					throw new InvalidFormatException(p, msg, p.getValueAsString(), Long.class);
				}
			}
		}
		return null;
	}

	/**
	 * Parse a JSON array of numeric values into a {@link BigDecimal} array.
	 *
	 * @param p
	 *        the parser
	 * @return the decimal array
	 * @throws JacksonException
	 *         if any IO error occurs
	 */
	public static BigDecimal[] parseDecimalArray(JsonParser p) throws JacksonException {
		JsonToken t = p.nextToken();
		if ( p.isExpectedStartArrayToken() ) {
			List<BigDecimal> l = new ArrayList<>(8);
			do {
				BigDecimal n = parseDecimal(p);
				if ( n != null ) {
					l.add(n);
				} else {
					t = p.currentToken();
					if ( t != JsonToken.END_ARRAY ) {
						l.add(null);
					}
				}
			} while ( t != null && t != JsonToken.END_ARRAY );
			return l.toArray(new BigDecimal[l.size()]);
		}
		return null;
	}

	/**
	 * Parse a simple Map from a JSON object.
	 *
	 * @param p
	 *        the parser
	 * @return the Map, or {@literal null} if no Map can be parsed
	 * @throws JacksonException
	 *         if any IO error occurs
	 */
	public static Map<String, ?> parseSimpleMap(JsonParser p) throws JacksonException {
		JsonToken t = p.nextToken();
		Map<String, Object> result = null;
		if ( p.isExpectedStartObjectToken() ) {
			result = new LinkedHashMap<>(8);
			String f;
			while ( (f = p.nextName()) != null ) {
				t = p.nextToken();
				Object v = null;
				if ( t.isNumeric() ) {
					v = p.getNumberValue();
				} else if ( t.isScalarValue() ) {
					v = p.getString();
				}
				if ( v != null ) {
					result.put(f, v);
				}
			}
		}
		return result;
	}

	/**
	 * Write a simple Map as a JSON object.
	 *
	 * <p>
	 * Only primitive object values are supported.
	 * </p>
	 *
	 * @param generator
	 *        the generator to write to
	 * @param value
	 *        the map to write
	 * @throws JacksonException
	 *         if any IO error occurs
	 */
	public static void writeSimpleMap(JsonGenerator generator, Map<String, ?> value)
			throws JacksonException {
		assert value != null;
		generator.writeStartObject(value, value.size());
		for ( Entry<String, ?> me : value.entrySet() ) {
			String f = me.getKey();
			Object v = me.getValue();
			if ( v == null ) {
				generator.writeNullProperty(f);
			} else if ( v instanceof Long ) {
				generator.writeNumberProperty(f, ((Long) v).longValue());
			} else if ( v instanceof Integer ) {
				generator.writeNumberProperty(f, ((Integer) v).intValue());
			} else if ( v instanceof BigDecimal ) {
				generator.writeNumberProperty(f, (BigDecimal) v);
			} else if ( v instanceof BigInteger ) {
				generator.writeNumberProperty(f, (BigInteger) v);
			} else if ( v instanceof Half ) {
				generator.writeNumberProperty(f, ((Half) v).floatValue());
			} else {
				generator.writeStringProperty(f, v.toString());
			}
		}
		generator.writeEndObject();
	}

	/**
	 * Parse a JSON object using a map of {@link IndexedField} values.
	 *
	 * @param p
	 *        the parser
	 * @param ctxt
	 *        the context
	 * @param data
	 *        the data array to populate, based on each
	 *        {@link IndexedField#getIndex()} value
	 * @param fields
	 *        the mapping of field names to associated fields
	 * @throws JacksonException
	 *         if any IO error occurs
	 */
	public static void parseIndexedFieldsObject(JsonParser p, DeserializationContext ctxt, Object[] data,
			Map<String, ? extends IndexedField> fields) throws JacksonException {
		String f = null;
		final int len = data.length;
		while ( (f = p.nextName()) != null ) {
			final IndexedField field = fields.get(f);
			if ( field == null ) {
				p.nextValue(); // skip to next field
				continue;
			}
			final int index = field.getIndex();
			if ( !(index < len) ) {
				p.nextValue(); // skip to next field
				continue;
			}
			Object o = field.parseValue(p, ctxt);
			if ( o != null ) {
				data[index] = o;
			}
		}
	}

}

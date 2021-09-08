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

package net.solarnetwork.codec;

import static java.util.Arrays.asList;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.module.SimpleModule;
import net.solarnetwork.domain.Instruction;
import net.solarnetwork.domain.InstructionStatus;
import net.solarnetwork.domain.Location;
import net.solarnetwork.domain.datum.Datum;
import net.solarnetwork.domain.datum.GeneralDatumMetadata;
import net.solarnetwork.domain.datum.ObjectDatumStreamMetadata;
import net.solarnetwork.domain.datum.StreamDatum;
import net.solarnetwork.util.Half;

/**
 * Utilities for JSON data.
 * 
 * <p>
 * The {@link ObjectMapper} used internally by this class supports:
 * </p>
 * 
 * <ul>
 * <li>Joda and java.time date/time values, serialized as strings using the RFC
 * 3339 profile of ISO-8601 with a space separator between date/time sections
 * instead of a {@literal T} character.</li>
 * <li>{@literal null} values are not serialized.</li>
 * <li>Floating point numbers are deserialized as {@link java.math.BigDecimal}
 * instances.</li>
 * </ul>
 * 
 * @author matt
 * @version 2.0
 * @since 1.72
 */
public final class JsonUtils {

	/** A type reference for a Map with string keys. */
	public static final TypeReference<LinkedHashMap<String, Object>> STRING_MAP_TYPE = new StringMapTypeReference();

	private static final Logger LOG = LoggerFactory.getLogger(JsonUtils.class);

	private static final ObjectMapper OBJECT_MAPPER = createObjectMapper();

	private static final ObjectMapper createObjectMapper() {
		return createObjectMapper(null);
	}

	private static final ObjectMapper createObjectMapper(JsonFactory jsonFactory) {
		ObjectMapperFactoryBean factory = new ObjectMapperFactoryBean();
		if ( jsonFactory != null ) {
			factory.setJsonFactory(jsonFactory);
		}
		factory.setSerializationInclusion(Include.NON_NULL);
		factory.setFeaturesToDisable(asList((Object) DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
				(Object) SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS,
				(Object) SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
		factory.setFeaturesToEnable(asList((Object) DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS));

		registerOptionalModule(factory, javaTimeModule());

		try {
			ObjectMapper mapper = factory.getObject();
			mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS'Z'"));
			return mapper;
		} catch ( RuntimeException e ) {
			throw e;
		} catch ( Exception e ) {
			throw new RuntimeException(e);
		}
	}

	private static SimpleModule createOptionalModule(String className,
			Consumer<SimpleModule> configuror) {
		try {
			Class<? extends SimpleModule> clazz = JsonUtils.class.getClassLoader().loadClass(className)
					.asSubclass(SimpleModule.class);
			SimpleModule m = clazz.newInstance();
			if ( configuror != null ) {
				configuror.accept(m);
			}
			return m;
		} catch ( ClassNotFoundException | InstantiationException | IllegalAccessException e ) {
			LOG.info("Optional JSON module {} not available ({})", className, e.toString());
			return null;
		}
	}

	private static void registerOptionalModule(ObjectMapperFactoryBean factory, SimpleModule m) {
		if ( m != null ) {
			List<Module> modules = factory.getModules();
			if ( modules == null ) {
				modules = new ArrayList<>(2);
			}
			modules.add(m);
			factory.setModules(modules);
		}
	}

	/**
	 * Create a module for handling {@code java.time} objects.
	 * 
	 * @return the module, or {@literal null} if support is not available
	 */
	public static SimpleModule javaTimeModule() {
		return createOptionalModule("com.fasterxml.jackson.datatype.jsr310.JavaTimeModule", m -> {
			// replace default timestamp JsonSerializer with one that supports spaces
			m.addSerializer(Instant.class, loadOptionalSerializerInstance(
					"net.solarnetwork.codec.JsonDateUtils$InstantSerializer"));
			m.addSerializer(ZonedDateTime.class, loadOptionalSerializerInstance(
					"net.solarnetwork.codec.JsonDateUtils$ZonedDateTimeSerializer"));
			m.addSerializer(LocalDateTime.class, loadOptionalSerializerInstance(
					"net.solarnetwork.codec.JsonDateUtils$LocalDateTimeSerializer"));
			m.addDeserializer(Instant.class, loadOptionalDeserializerInstance(
					"net.solarnetwork.codec.JsonDateUtils$InstantDeserializer"));
			m.addDeserializer(Instant.class, loadOptionalDeserializerInstance(
					"net.solarnetwork.codec.JsonDateUtils$ZonedDateTimeDeserializer"));
			m.addDeserializer(Instant.class, loadOptionalDeserializerInstance(
					"net.solarnetwork.codec.JsonDateUtils$LocalDateTimeDeserializer"));
		});
	}

	/**
	 * A module for handling datum objects.
	 * 
	 * @since 2.0
	 */
	public static final com.fasterxml.jackson.databind.Module DATUM_MODULE;
	static {
		SimpleModule m = new SimpleModule("SolarNetwork Datum");
		m.addSerializer(BasicGeneralDatumSerializer.INSTANCE);
		m.addSerializer(BasicLocationSerializer.INSTANCE);
		m.addSerializer(BasicObjectDatumStreamMetadataSerializer.INSTANCE);
		m.addSerializer(BasicStreamDatumArraySerializer.INSTANCE);
		m.addSerializer(BasicInstructionSerializer.INSTANCE);
		m.addSerializer(BasicInstructionStatusSerializer.INSTANCE);
		m.addDeserializer(Datum.class, BasicGeneralDatumDeserializer.INSTANCE);
		m.addDeserializer(Location.class, BasicLocationDeserializer.INSTANCE);
		m.addDeserializer(ObjectDatumStreamMetadata.class,
				BasicObjectDatumStreamMetadataDeserializer.INSTANCE);
		m.addDeserializer(StreamDatum.class, BasicStreamDatumArrayDeserializer.INSTANCE);
		m.addDeserializer(Instruction.class, BasicInstructionDeserializer.INSTANCE);
		m.addDeserializer(InstructionStatus.class, BasicInstructionStatusDeserializer.INSTANCE);
		DATUM_MODULE = m;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static final <T> JsonSerializer<T> loadOptionalSerializerInstance(String className) {
		try {
			Class<? extends JsonSerializer> clazz = JsonUtils.class.getClassLoader().loadClass(className)
					.asSubclass(JsonSerializer.class);
			Field f = clazz.getDeclaredField("INSTANCE");
			return (JsonSerializer<T>) f.get(null);
		} catch ( ClassNotFoundException | IllegalAccessException | NoSuchFieldException e ) {
			LOG.info("Optional JSON serializer {} not available ({})", className, e.toString());
			return null;
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static final <T> JsonDeserializer<T> loadOptionalDeserializerInstance(String className) {
		try {
			Class<? extends JsonDeserializer> clazz = JsonUtils.class.getClassLoader()
					.loadClass(className).asSubclass(JsonDeserializer.class);
			Field f = clazz.getDeclaredField("INSTANCE");
			return (JsonDeserializer<T>) f.get(null);
		} catch ( ClassNotFoundException | IllegalAccessException | NoSuchFieldException e ) {
			LOG.info("Optional JSON deserializer {} not available ({})", className, e.toString());
			return null;
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
	 * <p>
	 * This is designed for simple values. An internal {@link ObjectMapper} will
	 * be used, and null values will not be included in the output. All
	 * exceptions while serializing the object are caught and ignored.
	 * </p>
	 * 
	 * @param o
	 *        the object to serialize to JSON
	 * @param defaultValue
	 *        a default value to use if {@code o} is <em>null</em> or if any
	 *        error occurs serializing the object to JSON
	 * @return the JSON string
	 */
	public static String getJSONString(final Object o, final String defaultValue) {
		String result = defaultValue;
		if ( o != null ) {
			try {
				return OBJECT_MAPPER.writeValueAsString(o);
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
	 * @since 1.1
	 */
	public static <T> T getObjectFromJSON(final String json, Class<T> clazz) {
		T result = null;
		if ( json != null ) {
			try {
				result = OBJECT_MAPPER.readValue(json, clazz);
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
			return OBJECT_MAPPER.readValue(json, STRING_MAP_TYPE);
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
			return OBJECT_MAPPER.readValue(OBJECT_MAPPER.treeAsTokens(node), STRING_MAP_TYPE);
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
	 * @since 1.1
	 */
	public static JsonNode getTreeFromObject(final Object o) {
		if ( o == null ) {
			return null;
		}
		try {
			return OBJECT_MAPPER.valueToTree(o);
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
	 * @since 1.1
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
	 * @throws IOException
	 *         if any IO error occurs
	 */
	public static void writeMetadata(JsonGenerator generator, GeneralDatumMetadata meta)
			throws IOException {
		if ( meta == null ) {
			return;
		}
		Map<String, Object> m = meta.getM();
		if ( m != null ) {
			generator.writeObjectField("m", m);
		}

		Map<String, Map<String, Object>> pm = meta.getPm();
		if ( pm != null ) {
			generator.writeObjectField("pm", pm);
		}

		Set<String> t = meta.getT();
		if ( t != null ) {
			generator.writeArrayFieldStart("t");
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
	 * @return the parsed {@link BigDecimal}, or <em>null</em> if an error
	 *         occurs or the specified attribute {@code key} is not available
	 */
	public static BigDecimal parseBigDecimalAttribute(JsonNode node, String key) {
		BigDecimal num = null;
		if ( node != null ) {
			JsonNode attrNode = node.get(key);
			if ( attrNode != null && !attrNode.isNull() ) {
				String txt = attrNode.asText();
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
	 * Parse a Date from a JSON object attribute value.
	 * 
	 * If the date cannot be parsed, <em>null</em> will be returned.
	 * 
	 * @param node
	 *        the JSON node (e.g. object)
	 * @param key
	 *        the attribute key to obtain from {@code node}
	 * @param dateFormat
	 *        the date format to use to parse the date string
	 * @return the parsed {@link Date} instance, or <em>null</em> if an error
	 *         occurs or the specified attribute {@code key} is not available
	 */
	public static Date parseDateAttribute(JsonNode node, String key, DateFormat dateFormat) {
		Date result = null;
		if ( node != null ) {
			JsonNode attrNode = node.get(key);
			if ( attrNode != null && !attrNode.isNull() ) {
				try {
					String dateString = attrNode.asText();

					// replace "midnight" with 12:00am
					dateString = dateString.replaceAll("(?i)midnight", "12:00am");

					// replace "noon" with 12:00pm
					dateString = dateString.replaceAll("(?i)noon", "12:00pm");

					result = dateFormat.parse(dateString);
				} catch ( ParseException e ) {
					LOG.debug("Error parsing date attribute [{}] value [{}] using pattern {}: {}",
							new Object[] { key, attrNode,
									(dateFormat instanceof SimpleDateFormat
											? ((SimpleDateFormat) dateFormat).toPattern()
											: dateFormat.toString()),
									e.getMessage() });
				}
			}
		}
		return result;
	}

	/**
	 * Parse a Integer from a JSON object attribute value.
	 * 
	 * If the Integer cannot be parsed, <em>null</em> will be returned.
	 * 
	 * @param node
	 *        the JSON node (e.g. object)
	 * @param key
	 *        the attribute key to obtain from {@code node} node
	 * @return the parsed {@link Integer}, or <em>null</em> if an error occurs
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
					String s = attrNode.asText();
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
	 * If the Long cannot be parsed, <em>null</em> will be returned.
	 * 
	 * @param node
	 *        the JSON node (e.g. object)
	 * @param key
	 *        the attribute key to obtain from {@code node} node
	 * @return the parsed {@link Long}, or <em>null</em> if an error occurs or
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
						num = Long.valueOf(attrNode.asText());
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
	 * If the String cannot be parsed, <em>null</em> will be returned.
	 * 
	 * @param node
	 *        the JSON node (e.g. object)
	 * @param key
	 *        the attribute key to obtain from {@code node} node
	 * @return the parsed {@link String}, or <em>null</em> if an error occurs or
	 *         the specified attribute {@code key} is not available
	 */
	public static String parseStringAttribute(JsonNode node, String key) {
		String s = null;
		if ( node != null ) {
			JsonNode attrNode = node.get(key);
			if ( attrNode != null && !attrNode.isNull() ) {
				s = attrNode.asText();
			}
		}
		return s;
	}

	/**
	 * Create a new {@link ObjectMapper} based on the internal configuration
	 * used by other methods in this class.
	 * 
	 * @return a new {@link ObjectMapper}
	 * @since 1.1
	 */
	public static ObjectMapper newObjectMapper() {
		return OBJECT_MAPPER.copy();
	}

	/**
	 * Create a new {@link ObjectMapper} based on the internal configuration
	 * used by other methods in this class.
	 * 
	 * @return a new {@link ObjectMapper}
	 * @since 2.0
	 */
	public static ObjectMapper newDatumObjectMapper() {
		ObjectMapper mapper = OBJECT_MAPPER.copy();
		mapper.registerModule(DATUM_MODULE);
		return mapper;
	}

	/**
	 * Create a new {@link ObjectMapper} based on the internal configuration
	 * used by other methods in this class.
	 * 
	 * @param the
	 *        JSON factory to use
	 * @return a new {@link ObjectMapper}
	 * @since 2.0
	 */
	public static ObjectMapper newObjectMapper(JsonFactory jsonFactory) {
		return createObjectMapper(jsonFactory);
	}

	/**
	 * Create a new {@link ObjectMapper} based on the internal configuration
	 * used by other methods in this class.
	 * 
	 * @param the
	 *        JSON factory to use
	 * @return a new {@link ObjectMapper}
	 * @since 2.0
	 */
	public static ObjectMapper newDatumObjectMapper(JsonFactory jsonFactory) {
		ObjectMapper mapper = newObjectMapper(jsonFactory);
		mapper.registerModule(DATUM_MODULE);
		return mapper;
	}

	/**
	 * Parse a JSON array of scalar values into a string array.
	 * 
	 * @param p
	 *        the parser
	 * @return the parsed string array
	 * @throws IOException
	 *         if any IO error occurs
	 * @throws JsonProcessingException
	 *         if any processing exception occurs
	 */
	public static String[] parseStringArray(JsonParser p) throws IOException, JsonProcessingException {
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
	 * Write a string array as a JSON array of strings.
	 * 
	 * @param generator
	 *        the generator to write to
	 * @param array
	 *        the array to write
	 * @throws IOException
	 *         if any IO error occurs
	 * @throws JsonGenerationException
	 *         if any generation exception occurs
	 */
	public static void writeStringArray(JsonGenerator generator, String[] array)
			throws IOException, JsonGenerationException {
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
	 * @throws IOException
	 *         if any IO error occurs
	 * @throws JsonGenerationException
	 *         if any generation exception occurs
	 */
	public static void writeStringArrayField(JsonGenerator generator, String fieldName, String[] array)
			throws IOException, JsonGenerationException {
		if ( array != null && array.length > 0 ) {
			generator.writeFieldName(fieldName);
			writeStringArray(generator, array);
		}
	}

	/**
	 * Write a string array as a JSON array of numbers.
	 * 
	 * @param generator
	 *        the generator to write to
	 * @param array
	 *        the array to write
	 * @throws IOException
	 *         if any IO error occurs
	 * @throws JsonGenerationException
	 *         if any generation exception occurs
	 */
	public static void writeDecimalArray(JsonGenerator generator, BigDecimal[] array)
			throws IOException, JsonGenerationException {
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
	 * Parse a JSON numeric value into a {@link BigDecimal}.
	 * 
	 * @param p
	 *        the parser
	 * @return the decimal array
	 * @throws IOException
	 *         if any IO error occurs
	 * @throws JsonProcessingException
	 *         if any processing exception occurs
	 */
	public static BigDecimal parseDecimal(JsonParser p) throws IOException, JsonProcessingException {
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
	 * Parse a JSON array of numeric values into a {@link BigDecimal} array.
	 * 
	 * @param p
	 *        the parser
	 * @return the decimal array
	 * @throws IOException
	 *         if any IO error occurs
	 * @throws JsonProcessingException
	 *         if any processing exception occurs
	 */
	public static BigDecimal[] parseDecimalArray(JsonParser p)
			throws IOException, JsonProcessingException {
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
	 * @throws IOException
	 *         if any IO error occurs
	 * @throws JsonProcessingException
	 *         if any processing exception occurs
	 * @since 2.0
	 */
	public static Map<String, ?> parseSimpleMap(JsonParser p)
			throws IOException, JsonProcessingException {
		JsonToken t = p.nextToken();
		Map<String, Object> result = null;
		if ( p.isExpectedStartObjectToken() ) {
			result = new LinkedHashMap<>(8);
			String f;
			while ( (f = p.nextFieldName()) != null ) {
				t = p.nextToken();
				Object v = null;
				if ( t.isNumeric() ) {
					v = p.getNumberValue();
				} else if ( t.isScalarValue() ) {
					v = p.getText();
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
	 * @throws IOException
	 *         if any IO error occurs
	 * @throws JsonGenerationException
	 *         if any generation exception occurs
	 * @since 2.0
	 */
	public static void writeSimpleMap(JsonGenerator generator, Map<String, ?> value)
			throws IOException, JsonGenerationException {
		assert value != null;
		generator.writeStartObject(value, value.size());
		for ( Entry<String, ?> me : value.entrySet() ) {
			String f = me.getKey();
			Object v = me.getValue();
			if ( v == null ) {
				continue;
			}
			if ( v instanceof Long ) {
				generator.writeNumberField(f, ((Long) v).longValue());
			} else if ( v instanceof Integer ) {
				generator.writeNumberField(f, ((Integer) v).intValue());
			} else if ( v instanceof BigDecimal ) {
				generator.writeNumberField(f, (BigDecimal) v);
			} else if ( v instanceof BigInteger ) {
				generator.writeFieldName(f);
				generator.writeNumber((BigInteger) v);
			} else if ( v instanceof Half ) {
				generator.writeNumberField(f, ((Half) v).floatValue());
			} else {
				generator.writeStringField(f, v.toString());
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
	 * @throws IOException
	 *         if any IO error occurs
	 * @throws JsonProcessingException
	 *         if any processing exception occurs
	 */
	public static void parseIndexedFieldsObject(JsonParser p, DeserializationContext ctxt, Object[] data,
			Map<String, ? extends IndexedField> fields) throws IOException, JsonProcessingException {
		String f = null;
		final int len = data.length;
		while ( (f = p.nextFieldName()) != null ) {
			final IndexedField field = fields.get(f);
			if ( field == null ) {
				continue;
			}
			final int index = field.getIndex();
			if ( !(index < len) ) {
				continue;
			}
			Object o = field.parseValue(p, ctxt);
			if ( o != null ) {
				data[index] = o;
			}
		}
	}

}

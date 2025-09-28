/* ==================================================================
 * OcppUtils.java - 9/02/2024 2:51:33 pm
 *
 * Copyright 2024 SolarNetwork.net Dev Team
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

package net.solarnetwork.ocpp.v201.util;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.networknt.schema.AbsoluteIri;
import com.networknt.schema.JsonMetaSchema;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.NonValidationKeyword;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.ValidationMessage;
import net.solarnetwork.ocpp.domain.AuthorizationStatus;
import net.solarnetwork.ocpp.domain.ChargeSessionEndReason;
import net.solarnetwork.ocpp.domain.Location;
import net.solarnetwork.ocpp.domain.Measurand;
import net.solarnetwork.ocpp.domain.Phase;
import net.solarnetwork.ocpp.domain.ReadingContext;
import net.solarnetwork.ocpp.domain.SampledValue;
import net.solarnetwork.ocpp.domain.SchemaValidationException;
import net.solarnetwork.ocpp.domain.UnitOfMeasure;
import ocpp.v201.ReasonEnum;

/**
 * Utilities for OCPP v2.
 *
 * @author matt
 * @version 1.2
 */
public final class OcppUtils {

	private OcppUtils() {
		// not available
	}

	/** The action suffix for request messages. */
	public static final String ACTION_REQUEST_SUFFIX = "Request";

	/** The action suffix for response messages. */
	public static final String ACTION_RESPONSE_SUFFIX = "Response";

	/**
	 * A function that converts an OCPP action name to an equivalent JSON schema
	 * name.
	 */
	public static final Function<String, String> OCPP_ACTION_TO_JSON_SCHEMA_NAME = (s) -> {
		if ( s == null ) {
			return null;
		}
		return "urn:OCPP:Cp:2:2020:3:".concat(s);
	};

	/**
	 * A mapping of OCPP schema names to associated resources.
	 */
	public static final Map<AbsoluteIri, Resource> OCPP_ACTION_SCHEMA_RESOURCES;
	static {
		Map<AbsoluteIri, Resource> uriMappings = new HashMap<>();
		PathMatchingResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver(
				OcppUtils.class.getClassLoader());
		try {
			for ( Resource r : resourceResolver
					.getResources("classpath:schema/json/ocpp/v201/*.json") ) {
				String name = r.getFilename();
				name = name.substring(0, name.lastIndexOf('.'));
				uriMappings.put(AbsoluteIri.of(OCPP_ACTION_TO_JSON_SCHEMA_NAME.apply(name)), r);
			}
		} catch ( IOException e ) {
			throw new IllegalStateException(
					"Error loading OCPP 2.0.1 JSON schema resources from classpath:schema/json/ocpp/v201/*.json: "
							+ e.getMessage(),
					e);

		}
		OCPP_ACTION_SCHEMA_RESOURCES = Collections.unmodifiableMap(uriMappings);
	}

	/**
	 * A mapping of OCPP action names to associated schema locations, derived
	 * from {@link #OCPP_ACTION_SCHEMA_RESOURCES} keys.
	 */
	public static final Map<String, SchemaLocation> OCPP_ACTION_SCHEMA_LOCATIONS;

	/**
	 * A mapping of OCPP action names to associated classes, derived from
	 * {@link #OCPP_ACTION_SCHEMA_RESOURCES} keys.
	 */
	public static final Map<String, Class<?>> OCPP_ACTION_CLASSES;
	static {
		Map<String, Class<?>> classMappings = new HashMap<>();
		Map<String, SchemaLocation> locationMappings = new HashMap<>();
		for ( AbsoluteIri iri : OCPP_ACTION_SCHEMA_RESOURCES.keySet() ) {
			String action = iri.toString();
			action = action.substring(action.lastIndexOf(':') + 1);
			String className = "ocpp.v201." + action;
			try {
				classMappings.put(action, OcppUtils.class.getClassLoader().loadClass(className));
			} catch ( ClassNotFoundException e ) {
				throw new IllegalStateException(
						"Error loading OCPP 2.0.1 action class " + className + ": " + e.getMessage(), e);

			}
			locationMappings.put(action, SchemaLocation.of(iri.toString()));
		}
		OCPP_ACTION_CLASSES = Collections.unmodifiableMap(classMappings);
		OCPP_ACTION_SCHEMA_LOCATIONS = Collections.unmodifiableMap(locationMappings);
	}

	private static final ObjectMapper OBJECT_MAPPER = newObjectMapper();

	/**
	 * Create a new ObjectMapper suitable for OCPP v2.0.1 messages.
	 *
	 * @return the mapper
	 * @since 1.1
	 */
	public static ObjectMapper newObjectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		mapper.setDateFormat(new StdDateFormat().withColonInTimeZone(true));
		mapper.setDefaultPropertyInclusion(Include.NON_EMPTY);
		return mapper;
	}

	/**
	 * Get a JSON schema validator for OCPP 2.0.1.
	 *
	 * @return the validator
	 */
	public static JsonSchemaFactory ocppSchemaFactory_v201() {
		JsonMetaSchema baseSchema = JsonMetaSchema.getV6();
		// @formatter:off
		JsonMetaSchema metaSchema = JsonMetaSchema.builder(baseSchema.getUri(), baseSchema)
				.addKeywords(Arrays.asList(
						// OCPP schemas include these non-validating keywords we want to ignore
						new NonValidationKeyword("comment"),
						new NonValidationKeyword("javaType")))
				.build();
		// @formatter:on
		return JsonSchemaFactory.builder().defaultMetaSchemaURI(metaSchema.getUri())
				.addMetaSchema(metaSchema).schemaLoaders((l) -> {
					l.add((iri) -> {
						Resource r = OCPP_ACTION_SCHEMA_RESOURCES.get(iri);
						if ( r == null ) {
							return null;
						}
						return () -> r.getInputStream();
					});
				}).build();
	}

	/**
	 * Get the action class name for a request or response.
	 *
	 * @param action
	 *        the action name, for example "StatusNotification"
	 * @param request
	 *        {@literal true} if the message represents a request,
	 *        {@literal false} for a response
	 * @return the action class name
	 */
	public static String actionClassName(final String action, final boolean request) {
		return action + (request ? ACTION_REQUEST_SUFFIX : ACTION_RESPONSE_SUFFIX);
	}

	/**
	 * Parse and optionally validate an OCPP message.
	 *
	 * <p>
	 * A default {@link ObjectMapper} will be used.
	 * </p>
	 *
	 * @param action
	 *        the OCPP action, for example "StatusNotification"
	 * @param request
	 *        {@literal true} if the message represents a request,
	 *        {@literal false} for a response
	 * @param message
	 *        the OCPP message content
	 * @param validator
	 *        if provided, validate the message content using the schema
	 *        associated with {@code action}
	 * @return the parsed OCPP action instance
	 * @throws IOException
	 *         if a parsing error occurs
	 * @throws IllegalArgumentException
	 *         if the action is not known
	 * @throws SchemaValidationException
	 *         if validation fails
	 * @see #parseOcppMessage(String, boolean, String, JsonSchemaFactory,
	 *      ObjectMapper)
	 */
	public static Object parseOcppMessage(final String action, final boolean request,
			final String message, JsonSchemaFactory validator) throws IOException {
		return parseOcppMessage(action, request, message, validator, OBJECT_MAPPER);
	}

	/**
	 * Parse and optionally validate an OCPP message.
	 *
	 * @param action
	 *        the OCPP action, for example "StatusNotification"
	 * @param request
	 *        {@literal true} if the message represents a request,
	 *        {@literal false} for a response
	 * @param message
	 *        the OCPP message content
	 * @param validator
	 *        if provided, validate the message content using the schema
	 *        associated with {@code action}
	 * @param objectMapper
	 *        the mapper to use
	 * @return the parsed OCPP action instance
	 * @throws IOException
	 *         if a parsing error occurs
	 * @throws IllegalArgumentException
	 *         if the action is not known
	 * @throws SchemaValidationException
	 *         if validation fails
	 * @see #parseOcppMessage(String, boolean, ObjectNode, JsonSchemaFactory,
	 *      ObjectMapper)
	 * @since 1.1
	 */
	public static Object parseOcppMessage(final String action, final boolean request,
			final String message, JsonSchemaFactory validator, ObjectMapper objectMapper)
			throws IOException {
		JsonNode jsonNode = objectMapper.readTree(message);
		ObjectNode jsonPayload;
		if ( jsonNode.isNull() ) {
			jsonPayload = null;
		} else if ( jsonNode instanceof ObjectNode ) {
			jsonPayload = (ObjectNode) jsonNode;
		} else {
			throw new IOException("OCPP message must be a JSON object.");
		}
		return parseOcppMessage(action, request, jsonPayload, validator, objectMapper);
	}

	/**
	 * Parse and optionally validate an OCPP message.
	 *
	 * @param action
	 *        the OCPP action
	 * @param request
	 *        {@literal true} if the message represents a request,
	 *        {@literal false} for a response
	 * @param message
	 *        the OCPP message content
	 * @param validator
	 *        if provided, validate the message content using the schema
	 *        associated with {@code action}
	 * @return the parsed OCPP action instance
	 * @throws IllegalArgumentException
	 *         if the action is not known
	 * @throws SchemaValidationException
	 *         if validation fails
	 * @see #parseOcppMessage(String, boolean, ObjectNode, JsonSchemaFactory,
	 *      ObjectMapper)
	 */
	public static Object parseOcppMessage(final String action, final boolean request,
			final ObjectNode message, JsonSchemaFactory validator) {
		return parseOcppMessage(action, request, message, validator, OBJECT_MAPPER);
	}

	/**
	 * Parse and optionally validate an OCPP message.
	 *
	 * @param action
	 *        the OCPP action
	 * @param request
	 *        {@literal true} if the message represents a request,
	 *        {@literal false} for a response
	 * @param message
	 *        the OCPP message content
	 * @param validator
	 *        if provided, validate the message content using the schema
	 *        associated with {@code action}
	 * @param objectMapper
	 *        the mapper to use
	 * @return the parsed OCPP action instance
	 * @throws IllegalArgumentException
	 *         if the action is not known
	 * @throws SchemaValidationException
	 *         if validation fails
	 * @since 1.1
	 */
	public static Object parseOcppMessage(final String action, final boolean request,
			final ObjectNode message, JsonSchemaFactory validator, ObjectMapper objectMapper) {
		String actionClassName = actionClassName(action, request);
		Class<?> actionClass = OCPP_ACTION_CLASSES.get(actionClassName);
		if ( actionClass == null ) {
			throw new IllegalArgumentException("Unknown OCPP action [" + action + "]");
		}
		try {
			if ( validator != null ) {
				SchemaLocation loc = OCPP_ACTION_SCHEMA_LOCATIONS.get(actionClassName);
				if ( loc != null ) {
					JsonSchema schema = validator.getSchema(loc);
					Set<ValidationMessage> errors = schema.validate(message);
					if ( !errors.isEmpty() ) {
						throw new SchemaValidationException(message,
								String.format("JSON schema validation error on [%s] OCPP action: %s.",
										actionClassName, errors.stream().map(Object::toString)
												.collect(Collectors.joining(", "))));
					}
				}
			}
			return objectMapper.treeToValue(message, actionClass);
		} catch ( IOException e ) {
			throw new IllegalArgumentException(String.format("Invalid JSON for [%s] OCPP action: %s",
					actionClassName, e.getMessage()));
		}
	}

	/**
	 * Get a {@link ocpp.v201.AuthorizationStatusEnum} for an
	 * {@link AuthorizationStatus}.
	 *
	 * @param status
	 *        the status to translate
	 * @return the status, never {@literal null}
	 */
	public static ocpp.v201.AuthorizationStatusEnum statusForStatus(AuthorizationStatus status) {
		switch (status) {
			case Accepted:
				return ocpp.v201.AuthorizationStatusEnum.ACCEPTED;

			case Blocked:
				return ocpp.v201.AuthorizationStatusEnum.BLOCKED;

			case ConcurrentTx:
				return ocpp.v201.AuthorizationStatusEnum.CONCURRENT_TX;

			case Expired:
				return ocpp.v201.AuthorizationStatusEnum.EXPIRED;

			case Invalid:
				return ocpp.v201.AuthorizationStatusEnum.INVALID;

			default:
				return ocpp.v201.AuthorizationStatusEnum.UNKNOWN;
		}
	}

	/**
	 * Convert a {@link ocpp.v201.SampledValue} into a {@link SampledValue}.
	 *
	 * @param chargeSessionId
	 *        the charge session ID associated with the sample
	 * @param timestamp
	 *        the timestamp associated with the sample
	 * @param value
	 *        the value to translate
	 * @return the value, never {@literal null}
	 */
	public static SampledValue sampledValue(UUID chargeSessionId, Instant timestamp,
			ocpp.v201.SampledValue value) {
		// @formatter:off
		SampledValue.Builder result = SampledValue.builder()
				.withSessionId(chargeSessionId)
				.withTimestamp(timestamp)
				.withContext(readingContext(value.getContext()))
				.withLocation(location(value.getLocation()))
				.withMeasurand(measurand(value.getMeasurand()))
				.withPhase(phase(value.getPhase()))
				.withUnit(unit(value.getUnitOfMeasure()))
				;
		// @formatter:on
		if ( value.getValue() != null ) {
			// use BigDecimal to strip trailing zeros in string form
			BigDecimal d = BigDecimal.valueOf(value.getValue());
			result.withValue(d.stripTrailingZeros().toPlainString());
		}
		return result.build();
	}

	/**
	 * Convert a {@link ocpp.v201.UnitOfMeasure} into a {@link UnitOfMeasure}.
	 *
	 * @param unit
	 *        the unit to translate
	 * @return the unit, never {@literal null}
	 */
	public static UnitOfMeasure unit(ocpp.v201.UnitOfMeasure unit) {
		try {
			return UnitOfMeasure.valueOf(unit.getUnit());
		} catch ( IllegalArgumentException | NullPointerException e ) {
			return UnitOfMeasure.Unknown;
		}
	}

	/**
	 * Convert a {@link ocpp.v201.PhaseEnum} into a {@link Phase}.
	 *
	 * @param phase
	 *        the phase to translate
	 * @return the phase, never {@literal null}
	 */
	public static Phase phase(ocpp.v201.PhaseEnum phase) {
		if ( phase == null ) {
			return null;
		}
		try {
			return Phase.valueOf(phase.value().replace("-", ""));
		} catch ( IllegalArgumentException | NullPointerException e ) {
			return Phase.Unknown;
		}
	}

	/**
	 * Convert a {@link ocpp.v201.MeasurandEnum} into a {@link Measurand}.
	 *
	 * @param measurand
	 *        the measurand to translate
	 * @return the measurand, never {@literal null}
	 */
	public static Measurand measurand(ocpp.v201.MeasurandEnum measurand) {
		try {
			return Measurand.valueOf(measurand.value().replace(".", ""));
		} catch ( IllegalArgumentException | NullPointerException e ) {
			return Measurand.Unknown;
		}
	}

	/**
	 * Convert a {@link ocpp.v201.LocationEnum} into a {@link Location}.
	 *
	 * @param location
	 *        the location to translate
	 * @return the location, never {@literal null}
	 */
	public static Location location(ocpp.v201.LocationEnum location) {
		try {
			return Location.valueOf(location.value());
		} catch ( IllegalArgumentException | NullPointerException e ) {
			return Location.Outlet;
		}

	}

	/**
	 * Convert a {@link ocpp.v201.ReadingContextEnum} into a
	 * {@link ReadingContext}.
	 *
	 * @param context
	 *        the context to translate
	 * @return the context, never {@literal null}
	 */
	public static ReadingContext readingContext(ocpp.v201.ReadingContextEnum context) {
		try {
			return ReadingContext.valueOf(context.value().replace(".", ""));
		} catch ( IllegalArgumentException | NullPointerException e ) {
			return ReadingContext.Unknown;
		}
	}

	/**
	 * Convert a {@link ocpp.v201.ReasonEnum} into a
	 * {@link ChargeSessionEndReason}.
	 *
	 * @param reason
	 *        the reason to translate
	 * @return the reason, never {@literal null}
	 */
	public static ChargeSessionEndReason reason(ReasonEnum reason) {
		if ( reason == null ) {
			return ChargeSessionEndReason.Local;
		}
		try {
			return ChargeSessionEndReason.valueOf(reason.value());
		} catch ( IllegalArgumentException e ) {
			return ChargeSessionEndReason.Unknown;
		}
	}
}

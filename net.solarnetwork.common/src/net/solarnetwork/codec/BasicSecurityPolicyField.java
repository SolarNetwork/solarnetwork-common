/* ==================================================================
 * BasicSecurityPolicyField.java - 27/09/2025 11:15:58 am
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

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.solarnetwork.domain.BasicSecurityPolicy;
import net.solarnetwork.domain.LocationPrecision;
import net.solarnetwork.domain.datum.Aggregation;

/**
 * Fields for {@link BasicSecurityPolicy} de/serialization.
 *
 * @author matt
 * @version 1.0
 */
public enum BasicSecurityPolicyField implements IndexedField {

	/** The node ID array. */
	NodeIds(0, "nodeIds"),

	/** The source ID array. */
	SourceIds(1, "sourceIds"),

	/** The minimum aggregation. */
	MinAggregation(2, "minAggregation"),

	/** The aggregation array. */
	Aggregations(3, "aggregations"),

	/** The minimum location precision. */
	MinLocationPrecision(4, "minLocationPrecision"),

	/** The location precision array. */
	LocationPrecisions(5, "locationPrecisions"),

	/** The node metadata path array. */
	NodeMetadataPaths(6, "nodeMetadataPaths"),

	/** The user metadata path array. */
	UserMetadataPaths(7, "userMetadataPaths"),

	/** The API path array. */
	ApiPaths(8, "apiPaths"),

	/** The expiration date as epoch milliseconds. */
	NotAfter(9, "notAfter"),

	/** The "refresh allowed" mode. */
	RefreshAllowed(10, "refreshAllowed"),

	;

	/** A field map. */
	public static final Map<String, BasicSecurityPolicyField> FIELD_MAP = IndexedField
			.fieldMap(BasicSecurityPolicyField.class);

	private final int index;
	private final String fieldName;

	private BasicSecurityPolicyField(int index, String fieldName) {
		this.index = index;
		this.fieldName = fieldName;
	}

	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public String getFieldName() {
		return fieldName;
	}

	@Override
	public Object parseValue(JsonParser parser, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		switch (this) {
			case NodeIds: {
				Long[] array = JsonUtils.parseLongArray(parser);
				if ( array != null && array.length > 0 ) {
					return new LinkedHashSet<>(Arrays.asList(array));
				}
				return null;
			}

			case MinAggregation: {
				String s = parser.nextTextValue();
				try {
					return Aggregation.forKey(s);
				} catch ( IllegalArgumentException e ) {
					// ignore
					return null;
				}
			}

			case Aggregations: {
				String[] array = JsonUtils.parseStringArray(parser);
				if ( array != null && array.length > 0 ) {
					Set<Aggregation> set = new LinkedHashSet<>();
					for ( String s : array ) {
						try {
							set.add(Aggregation.forKey(s));
						} catch ( IllegalArgumentException e ) {
							// ignore
						}
					}
					return (!set.isEmpty() ? set : null);
				}
			}

			case MinLocationPrecision: {
				String s = parser.nextTextValue();
				try {
					return LocationPrecision.valueOf(s);
				} catch ( IllegalArgumentException e ) {
					// ignore
					return null;
				}
			}

			case LocationPrecisions: {
				String[] array = JsonUtils.parseStringArray(parser);
				if ( array != null && array.length > 0 ) {
					Set<LocationPrecision> set = new LinkedHashSet<>();
					for ( String s : array ) {
						try {
							set.add(LocationPrecision.valueOf(s));
						} catch ( IllegalArgumentException e ) {
							// ignore
						}
					}
					return (!set.isEmpty() ? set : null);
				}
			}

			case SourceIds:
			case NodeMetadataPaths:
			case UserMetadataPaths:
			case ApiPaths: {
				String[] array = JsonUtils.parseStringArray(parser);
				if ( array != null && array.length > 0 ) {
					return new LinkedHashSet<>(Arrays.asList(array));
				}
				return null;
			}

			case NotAfter: {
				long date = parser.nextLongValue(0);
				return (date > 0 ? Instant.ofEpochMilli(date) : null);
			}

			case RefreshAllowed:
				return parser.nextBooleanValue();

			default:
				return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void writeValue(JsonGenerator generator, SerializerProvider provider, Object value)
			throws IOException, JsonProcessingException {
		if ( value == null ) {
			return;
		}
		switch (this) {
			case NodeIds: {
				Set<Long> set = (Set<Long>) value;
				if ( !set.isEmpty() ) {
					generator.writeArrayFieldStart(fieldName);
					// maintain node IDs in natural sort order
					Long[] ids = ((Set<Long>) value).toArray(Long[]::new);
					Arrays.sort(ids);
					for ( Long id : ids ) {
						generator.writeNumber(id);
					}
					generator.writeEndArray();
				}
			}
				break;

			case SourceIds:
			case NodeMetadataPaths:
			case UserMetadataPaths:
			case ApiPaths: {
				Set<String> set = (Set<String>) value;
				if ( !set.isEmpty() ) {
					generator.writeArrayFieldStart(fieldName);
					for ( String id : ((Set<String>) value) ) {
						generator.writeString(id);
					}
					generator.writeEndArray();
				}
			}
				break;

			case MinAggregation:
				generator.writeStringField(fieldName, ((Aggregation) value).name());
				break;

			case Aggregations: {
				Set<Aggregation> set = (Set<Aggregation>) value;
				if ( !set.isEmpty() ) {
					generator.writeArrayFieldStart(fieldName);
					for ( Aggregation val : set ) {
						generator.writeString(val.name());
					}
					generator.writeEndArray();
				}
			}
				break;

			case MinLocationPrecision:
				generator.writeStringField(fieldName, ((LocationPrecision) value).name());
				break;

			case LocationPrecisions: {
				Set<LocationPrecision> set = (Set<LocationPrecision>) value;
				if ( !set.isEmpty() ) {
					generator.writeArrayFieldStart(fieldName);
					for ( LocationPrecision val : set ) {
						generator.writeString(val.name());
					}
					generator.writeEndArray();
				}
			}
				break;

			case NotAfter:
				generator.writeNumberField(fieldName, ((Instant) value).toEpochMilli());
				break;

			case RefreshAllowed:
				generator.writeBooleanField(fieldName, (Boolean) value);
				break;

			default:
				// nothing
		}
	}

}

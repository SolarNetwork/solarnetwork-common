/* ==================================================================
 * BasicObjectDatumStreamDataSetDeserializer.java - 30/04/2022 9:42:57 am
 *
 * Copyright 2022 SolarNetwork.net Dev Team
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

import static java.time.Instant.ofEpochMilli;
import static net.solarnetwork.codec.jackson.BasicObjectDatumStreamDataSetSerializer.DATA_FIELD_NAME;
import static net.solarnetwork.codec.jackson.BasicObjectDatumStreamDataSetSerializer.META_FIELD_NAME;
import static net.solarnetwork.codec.jackson.BasicObjectDatumStreamDataSetSerializer.RETURNED_RESULT_COUNT_FIELD_NAME;
import static net.solarnetwork.codec.jackson.BasicObjectDatumStreamDataSetSerializer.STARTING_OFFSET_FIELD_NAME;
import static net.solarnetwork.codec.jackson.BasicObjectDatumStreamDataSetSerializer.TOTAL_RESULT_COUNT_FIELD_NAME;
import static net.solarnetwork.domain.datum.DatumProperties.propertiesOf;
import static net.solarnetwork.domain.datum.DatumPropertiesStatistics.statisticsOf;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import net.solarnetwork.domain.datum.AggregateStreamDatum;
import net.solarnetwork.domain.datum.BasicAggregateStreamDatum;
import net.solarnetwork.domain.datum.BasicObjectDatumStreamDataSet;
import net.solarnetwork.domain.datum.BasicStreamDatum;
import net.solarnetwork.domain.datum.DatumSamplesType;
import net.solarnetwork.domain.datum.ObjectDatumStreamDataSet;
import net.solarnetwork.domain.datum.ObjectDatumStreamMetadata;
import net.solarnetwork.domain.datum.StreamDatum;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.exc.MismatchedInputException;

/**
 * Deserializer for {@link ObjectDatumStreamDataSet}.
 *
 * <p>
 * Note that {@link AggregateStreamDatum} instances will be returned when
 * appropriate.
 * </p>
 *
 * @author matt
 * @version 1.0
 * @since 4.13
 */
public class BasicObjectDatumStreamDataSetDeserializer
		extends StdDeserializer<ObjectDatumStreamDataSet<StreamDatum>> {

	/** A default instance. */
	public static final ValueDeserializer<ObjectDatumStreamDataSet<StreamDatum>> INSTANCE = new BasicObjectDatumStreamDataSetDeserializer();

	/**
	 * Constructor.
	 */
	public BasicObjectDatumStreamDataSetDeserializer() {
		super(ObjectDatumStreamDataSet.class);
	}

	@Override
	public ObjectDatumStreamDataSet<StreamDatum> deserialize(JsonParser p, DeserializationContext ctxt)
			throws JacksonException {
		JsonToken t = p.currentToken();
		if ( t == JsonToken.VALUE_NULL ) {
			return null;
		} else if ( p.isExpectedStartObjectToken() ) {
			Integer returnedResultCount = null;
			Integer startingOffset = null;
			Long totalResultCount = null;
			final List<ObjectDatumStreamMetadata> metadatas = new ArrayList<>(4);
			final List<StreamDatum> data = new ArrayList<>();
			while ( (t = p.nextToken()) != JsonToken.END_OBJECT ) {
				if ( RETURNED_RESULT_COUNT_FIELD_NAME.toString().equals(p.currentName()) ) {
					returnedResultCount = p.getIntValue();
				} else if ( STARTING_OFFSET_FIELD_NAME.toString().equals(p.currentName()) ) {
					startingOffset = p.getIntValue();
				} else if ( TOTAL_RESULT_COUNT_FIELD_NAME.toString().equals(p.currentName()) ) {
					totalResultCount = p.getLongValue();
				} else if ( META_FIELD_NAME.toString().equals(p.currentName()) ) {
					if ( p.nextToken() == JsonToken.START_ARRAY ) {
						for ( t = p.nextToken(); t != null
								&& t != JsonToken.END_ARRAY; t = p.nextToken() ) {
							ObjectDatumStreamMetadata meta = BasicObjectDatumStreamMetadataDeserializer.INSTANCE
									.deserialize(p, ctxt);
							metadatas.add(meta);
						}
					}
				} else if ( DATA_FIELD_NAME.toString().equals(p.currentName()) ) {
					if ( p.nextToken() == JsonToken.START_ARRAY ) {
						while ( p.nextToken() == JsonToken.START_ARRAY ) {
							int i = -2;
							ObjectDatumStreamMetadata meta = null;
							long ts = 0;
							long tsEnd = -1;

							String[] iNames = null;
							String[] aNames = null;
							String[] sNames = null;
							int iLen = 0;
							int aLen = 0;
							int sLen = 0;
							int aStart = 0;
							int sStart = 0;
							int tStart = 0;

							BigDecimal[] iData = null;
							BigDecimal[] aData = null;
							String[] sData = null;
							List<String> tags = null;
							BigDecimal[][] iStats = null;
							BigDecimal[][] aStats = null;

							for ( t = p.nextToken(); t != null
									&& t != JsonToken.END_ARRAY; t = p.nextToken(), i++ ) {
								if ( i == -2 ) {
									int idx = p.getIntValue();
									if ( idx >= metadatas.size() ) {
										throw MismatchedInputException.from(p,
												"Missing metadata index %d".formatted(idx));
									}
									meta = metadatas.get(idx);
									iNames = meta.propertyNamesForType(DatumSamplesType.Instantaneous);
									aNames = meta.propertyNamesForType(DatumSamplesType.Accumulating);
									sNames = meta.propertyNamesForType(DatumSamplesType.Status);
									iLen = (iNames != null ? iNames.length : 0);
									aLen = (aNames != null ? aNames.length : 0);
									sLen = (sNames != null ? sNames.length : 0);
									aStart = iLen;
									sStart = aStart + aLen;
									tStart = sStart + sLen;

									iData = (iLen > 0 ? new BigDecimal[iLen] : null);
									aData = (aLen > 0 ? new BigDecimal[aLen] : null);
									sData = (sLen > 0 ? new String[sLen] : null);
								} else if ( i == -1 ) {
									if ( t == JsonToken.START_ARRAY ) {
										// we have a 2-element start/end array
										t = p.nextToken();
										ts = p.getLongValue();
										t = p.nextToken();
										if ( t != JsonToken.VALUE_NULL ) {
											tsEnd = p.getLongValue();
										} else {
											tsEnd = 0;
										}
										t = p.nextToken(); // consume end array
										iStats = new BigDecimal[iLen][];
										aStats = new BigDecimal[aLen][];
									} else {
										ts = p.getLongValue();
									}
								} else if ( iLen > 0 && i < aStart ) {
									if ( t == JsonToken.START_ARRAY ) {
										BigDecimal[] stats = new BigDecimal[4];
										iStats[i] = stats;
										int j = 0;
										for ( t = p.nextToken(); t != null
												&& t != JsonToken.END_ARRAY; t = p.nextToken(), j++ ) {
											BigDecimal val = (t == JsonToken.VALUE_NULL ? null
													: p.getDecimalValue());
											if ( j == 0 ) {
												iData[i] = val;
											}
											if ( j < 4 ) {
												stats[j] = val;
											}
										}
									} else {
										iData[i] = (t == JsonToken.VALUE_NULL ? null
												: p.getDecimalValue());
									}
								} else if ( aLen > 0 && i < sStart ) {
									if ( t == JsonToken.START_ARRAY ) {
										BigDecimal[] stats = new BigDecimal[3];
										aStats[i - aStart] = stats;
										int j = 0;
										for ( t = p.nextToken(); t != null
												&& t != JsonToken.END_ARRAY; t = p.nextToken(), j++ ) {
											BigDecimal val = (t == JsonToken.VALUE_NULL ? null
													: p.getDecimalValue());
											if ( j == 0 ) {
												aData[i - aStart] = val;
											}
											if ( j < 3 ) {
												stats[j] = val;
											}
										}
									} else {
										aData[i - aStart] = (t == JsonToken.VALUE_NULL ? null
												: p.getDecimalValue());
									}
								} else if ( sLen > 0 && i < tStart ) {
									sData[i - sStart] = (t == JsonToken.VALUE_NULL ? null
											: p.getString());
								} else if ( t == JsonToken.VALUE_STRING ) {
									if ( tags == null ) {
										tags = new ArrayList<>(4);
									}
									tags.add(p.getString());
								}
							}
							if ( tsEnd > -1 ) {
								data.add(new BasicAggregateStreamDatum(meta.getStreamId(),
										ofEpochMilli(ts),
										propertiesOf(iData, aData, sData,
												tags != null ? tags.toArray(new String[tags.size()])
														: null),
										tsEnd > 0 ? ofEpochMilli(tsEnd) : null,
										statisticsOf(iStats, aStats)));
							} else {
								data.add(new BasicStreamDatum(meta.getStreamId(), ofEpochMilli(ts),
										propertiesOf(iData, aData, sData,
												tags != null ? tags.toArray(new String[tags.size()])
														: null)));
							}
						}
					}
				}
			}
			return BasicObjectDatumStreamDataSet.dataSet(metadatas, data, totalResultCount,
					startingOffset, returnedResultCount);
		}
		throw MismatchedInputException.from(p,
				"Unable to parse ObjectDatumStreamDataSet (not an array)");
	}

}

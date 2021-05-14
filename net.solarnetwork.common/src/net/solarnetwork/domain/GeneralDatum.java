/* ==================================================================
 * GeneralDatum.java - 14/05/2021 10:21:26 AM
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

package net.solarnetwork.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.solarnetwork.domain.DatumStreamId.LocationDatumStreamId;
import net.solarnetwork.domain.DatumStreamId.NodeDatumStreamId;
import net.solarnetwork.domain.datum.Datum;

/**
 * A basic implementation of {@link net.solarnetwork.domain.datum.GeneralDatum}.
 * 
 * @author matt
 * @version 1.0
 * @since 1.71
 */
public class GeneralDatum extends BasicIdentity<DatumStreamId>
		implements Identity<DatumStreamId>, net.solarnetwork.domain.datum.GeneralDatum, Serializable,
		Cloneable, MutableGeneralDatumSamplesOperations {

	private static final long serialVersionUID = 1934830001340995747L;

	private final GeneralDatumSamples samples;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *        the ID
	 * @param samples
	 *        the samples; if {@literal null} a new instance will be created
	 */
	public GeneralDatum(DatumStreamId id, GeneralDatumSamples samples) {
		super(id);
		this.samples = (samples != null ? samples : new GeneralDatumSamples());
	}

	/**
	 * Constructor.
	 * 
	 * <p>
	 * This creates a {@literal null} {@code objectId} and sets the timestamp to
	 * the system time.
	 * </p>
	 * </p>
	 * 
	 * @param sourceId
	 *        the source ID
	 */
	public GeneralDatum(String sourceId) {
		this(new DatumStreamId(null, sourceId, Instant.now()), null);
	}

	/**
	 * Constructor.
	 * 
	 * <p>
	 * This creates a {@literal null} {@code objectId}.
	 * </p>
	 * 
	 * @param sourceId
	 *        the source ID
	 * @param timestamp
	 *        the timestamp
	 */
	public GeneralDatum(String sourceId, Instant timestamp) {
		this(new DatumStreamId(null, sourceId, timestamp), null);
	}

	/**
	 * Constructor.
	 * 
	 * <p>
	 * This creates a {@literal null} {@code objectId}.
	 * </p>
	 * 
	 * @param sourceId
	 *        the source ID
	 * @param timestamp
	 *        the timestamp
	 * @param samples
	 *        the samples
	 */
	public GeneralDatum(String sourceId, Instant timestamp, GeneralDatumSamples samples) {
		this(new DatumStreamId(null, sourceId, timestamp), samples);
	}

	/**
	 * Constructor.
	 * 
	 * @param objectId
	 *        the object ID
	 * @param sourceId
	 *        the source ID
	 * @param timestamp
	 *        the timestamp
	 * @param samples
	 *        the samples
	 */
	public GeneralDatum(Long objectId, String sourceId, Instant timestamp, GeneralDatumSamples samples) {
		this(new DatumStreamId(objectId, sourceId, timestamp), samples);
	}

	/**
	 * Create a datum with a {@link NodeDatumStreamId}.
	 * 
	 * @param nodeId
	 *        the node ID
	 * @param sourceId
	 *        the source ID
	 * @param timestamp
	 *        the timestamp
	 * @param samples
	 *        the samples
	 * @return the new instance
	 */
	public static GeneralDatum nodeDatum(Long nodeId, String sourceId, Instant timestamp,
			GeneralDatumSamples samples) {
		return new GeneralDatum(DatumStreamId.nodeId(nodeId, sourceId, timestamp), samples);
	}

	/**
	 * Create a datum with a {@link LocationDatumStreamId}.
	 * 
	 * @param locationId
	 *        the location ID
	 * @param sourceId
	 *        the source ID
	 * @param timestamp
	 *        the timestamp
	 * @param samples
	 *        the samples
	 * @return the new instance
	 */
	public static GeneralDatum locationDatum(Long locationId, String sourceId, Instant timestamp,
			GeneralDatumSamples samples) {
		return new GeneralDatum(DatumStreamId.locationId(locationId, sourceId, timestamp), samples);
	}

	@Override
	public GeneralDatum clone() {
		return (GeneralDatum) super.clone();
	}

	/**
	 * Get the object ID.
	 * 
	 * @return the object ID
	 */
	public Long getObjectId() {
		final DatumStreamId id = getId();
		return (id != null ? id.getObjectId() : null);
	}

	@Override
	public String getSourceId() {
		final DatumStreamId id = getId();
		return (id != null ? id.getSourceId() : null);
	}

	@Override
	public Instant getTimestamp() {
		final DatumStreamId id = getId();
		return (id != null ? id.getTimestamp() : null);
	}

	@Override
	public Map<String, ?> getSampleData() {
		final GeneralDatumSamples s = samples;
		return (s != null ? s.getSampleData() : null);
	}

	@Override
	public Map<String, ?> asSimpleMap() {
		return createSimpleMap();
	}

	/**
	 * Create a map of simple property data out of this object.
	 * 
	 * <p>
	 * This method will populate the properties of this class and the
	 * {@link Datum#DATUM_TYPE_PROPERTY} and {@link Datum#DATUM_TYPES_PROPERTY}
	 * properties with the result from calling {@link #datumTypes()}. It will
	 * then call {@link #getSampleData()} and add all those values to the
	 * returned result.
	 * </p>
	 * 
	 * @return a map of simple property data
	 */
	protected Map<String, Object> createSimpleMap() {
		final Map<String, Object> map = new LinkedHashMap<String, Object>();
		final Instant timestamp = getTimestamp();
		if ( timestamp != null ) {
			map.put(TIMESTAMP, timestamp.toEpochMilli());
		}
		final String sourceId = getSourceId();
		if ( sourceId != null ) {
			map.put(SOURCE_ID, sourceId);
		}
		String[] datumTypes = datumTypes();
		if ( datumTypes != null && datumTypes.length > 0 ) {
			map.put(DATUM_TYPE_PROPERTY, datumTypes[0]);
			map.put(DATUM_TYPES_PROPERTY, datumTypes);
		}
		Map<String, ?> sampleData = getSampleData();
		if ( sampleData != null ) {
			map.putAll(sampleData);
		}
		return map;
	}

	protected String[] datumTypes() {
		return null;
	}

	@Override
	public GeneralDatumSamplesOperations asSampleOperations() {
		return samples;
	}

	@Override
	public MutableGeneralDatumSamplesOperations asMutableSampleOperations() {
		return samples;
	}

	/**
	 * Test if this datum has any sample property values.
	 * 
	 * @return {@literal true} if the samples is not empty
	 * @see net.solarnetwork.domain.GeneralDatumSamples#isEmpty()
	 */
	public boolean isEmpty() {
		return samples.isEmpty();
	}

	@Override
	public Map<String, ?> getSampleData(GeneralDatumSamplesType type) {
		return samples.getSampleData(type);
	}

	@Override
	public void setSampleData(GeneralDatumSamplesType type, Map<String, ?> data) {
		samples.setSampleData(type, data);
	}

	@Override
	public Integer getSampleInteger(GeneralDatumSamplesType type, String key) {
		return samples.getSampleInteger(type, key);
	}

	@Override
	public Long getSampleLong(GeneralDatumSamplesType type, String key) {
		return samples.getSampleLong(type, key);
	}

	@Override
	public Float getSampleFloat(GeneralDatumSamplesType type, String key) {
		return samples.getSampleFloat(type, key);
	}

	@Override
	public Double getSampleDouble(GeneralDatumSamplesType type, String key) {
		return samples.getSampleDouble(type, key);
	}

	@Override
	public BigDecimal getSampleBigDecimal(GeneralDatumSamplesType type, String key) {
		return samples.getSampleBigDecimal(type, key);
	}

	@Override
	public String getSampleString(GeneralDatumSamplesType type, String key) {
		return samples.getSampleString(type, key);
	}

	@Override
	public <V> V getSampleValue(GeneralDatumSamplesType type, String key) {
		return samples.getSampleValue(type, key);
	}

	/**
	 * Get the sample tags.
	 * 
	 * @return the tags
	 * @see net.solarnetwork.domain.GeneralDatumSupport#getTags()
	 */
	public Set<String> getTags() {
		return samples.getTags();
	}

	/**
	 * Set the sample tags.
	 * 
	 * @param tags
	 *        the tags to set
	 * @see net.solarnetwork.domain.GeneralDatumSupport#setTags(java.util.Set)
	 */
	public void setTags(Set<String> tags) {
		samples.setTags(tags);
	}

	@Override
	public <V> V findSampleValue(String key) {
		return samples.findSampleValue(key);
	}

	/**
	 * Test if a sample tag exists.
	 * 
	 * @param tag
	 *        the tag to test
	 * @return {@literal true} if the sample tag exists
	 * @see net.solarnetwork.domain.GeneralDatumSupport#hasTag(java.lang.String)
	 */
	public boolean hasTag(String tag) {
		return samples.hasTag(tag);
	}

	/**
	 * Add a sample tag.
	 * 
	 * @param tag
	 *        the tag to add
	 * @see net.solarnetwork.domain.GeneralDatumSupport#addTag(java.lang.String)
	 */
	public void addTag(String tag) {
		samples.addTag(tag);
	}

	@Override
	public boolean hasSampleValue(String key) {
		return samples.hasSampleValue(key);
	}

	@Override
	public void putSampleValue(GeneralDatumSamplesType type, String key, Object value) {
		samples.putSampleValue(type, key, value);
	}

	/**
	 * Remove a sample tag.
	 * 
	 * @param tag
	 *        the tag to remove.
	 * @see net.solarnetwork.domain.GeneralDatumSupport#removeTag(java.lang.String)
	 */
	public void removeTag(String tag) {
		samples.removeTag(tag);
	}

	@Override
	public boolean hasSampleValue(GeneralDatumSamplesType type, String key) {
		return samples.hasSampleValue(type, key);
	}

	/**
	 * Get the samples instance.
	 * 
	 * @return the samples, never {@literal null}
	 */
	public GeneralDatumSamples getSamples() {
		return samples;
	}

}

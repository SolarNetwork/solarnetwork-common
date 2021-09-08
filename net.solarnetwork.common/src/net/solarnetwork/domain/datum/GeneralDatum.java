/* ==================================================================
 * GeneralDatum.java - 14/05/2021 10:21:26 AM
 * 
 * Copyright 2021 SolarNetwork.net Dev Team
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU  Public License as 
 * published by the Free Software Foundation; either version 2 of 
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 *  Public License for more details.
 * 
 * You should have received a copy of the GNU  Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
 * 02111-1307 USA
 * ==================================================================
 */

package net.solarnetwork.domain.datum;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.solarnetwork.domain.BasicIdentity;
import net.solarnetwork.domain.Identity;

/**
 * A basic implementation of {@link MutableDatum}.
 * 
 * @author matt
 * @version 2.0
 * @since 1.71
 */
public class GeneralDatum extends BasicIdentity<DatumId> implements Datum, DatumSamplesContainer,
		MutableDatum, MutableDatumSamplesOperations, Identity<DatumId>, Serializable, Cloneable {

	private static final long serialVersionUID = 1934830001340995747L;

	private final DatumSamples samples;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *        the ID
	 * @param samples
	 *        the samples; if {@literal null} a new instance will be created
	 */
	public GeneralDatum(DatumId id, DatumSamples samples) {
		super(id);
		this.samples = (samples != null ? samples : new DatumSamples());
	}

	/**
	 * Constructor.
	 * 
	 * <p>
	 * This creates a {@literal null} {@code kind} and {@code objectId} and sets
	 * the timestamp to the system time.
	 * </p>
	 * 
	 * @param sourceId
	 *        the source ID
	 */
	public GeneralDatum(String sourceId) {
		this(new DatumId(null, null, sourceId, Instant.now()), null);
	}

	/**
	 * Constructor.
	 * 
	 * <p>
	 * This creates a {@literal null} {@code kind} and {@code objectId}.
	 * </p>
	 * 
	 * @param sourceId
	 *        the source ID
	 * @param timestamp
	 *        the timestamp
	 */
	public GeneralDatum(String sourceId, Instant timestamp) {
		this(new DatumId(null, null, sourceId, timestamp), null);
	}

	/**
	 * Constructor.
	 * 
	 * <p>
	 * This creates a {@literal null} {@code kind} and {@code objectId}.
	 * </p>
	 * 
	 * @param sourceId
	 *        the source ID
	 * @param timestamp
	 *        the timestamp
	 * @param samples
	 *        the samples
	 */
	public GeneralDatum(String sourceId, Instant timestamp, DatumSamples samples) {
		this(new DatumId(null, null, sourceId, timestamp), samples);
	}

	/**
	 * Constructor.
	 * 
	 * <p>
	 * This creates a {@literal null} {@code kind}.
	 * </p>
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
	public GeneralDatum(Long objectId, String sourceId, Instant timestamp, DatumSamples samples) {
		this(new DatumId(null, objectId, sourceId, timestamp), samples);
	}

	/**
	 * Create a node datum.
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
			DatumSamples samples) {
		return new GeneralDatum(DatumId.nodeId(nodeId, sourceId, timestamp), samples);
	}

	/**
	 * Create a location datum.
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
			DatumSamples samples) {
		return new GeneralDatum(DatumId.locationId(locationId, sourceId, timestamp), samples);
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder("Datum{kind=");
		buf.append(getKind());
		Long objectId = getObjectId();
		if ( objectId != null ) {
			buf.append(",objectId=").append(objectId);
		}
		String sourceId = getSourceId();
		if ( sourceId != null ) {
			buf.append(",sourceId=").append(sourceId);
		}
		Instant ts = getTimestamp();
		if ( ts != null ) {
			buf.append(",ts=").append(ts);
		}
		if ( !isEmpty() ) {
			buf.append(",data=").append(samples);
		}
		buf.append("}");
		return buf.toString();
	}

	@Override
	public GeneralDatum clone() {
		return (GeneralDatum) super.clone();
	}

	@Override
	public Datum copyWithSamples(DatumSamplesOperations samples) {
		GeneralDatum copy = clone();
		copy.clear();
		copy.samples.copyFrom(samples);
		return copy;
	}

	@Override
	public Datum copyWithId(DatumId id) {
		return new GeneralDatum(id, samples);
	}

	/**
	 * Get the object kind.
	 * 
	 * @return the kind
	 */
	@Override
	public ObjectDatumKind getKind() {
		final DatumId id = getId();
		return (id != null ? id.getKind() : null);
	}

	/**
	 * Get the object ID.
	 * 
	 * @return the object ID
	 */
	@Override
	public Long getObjectId() {
		final DatumId id = getId();
		return (id != null ? id.getObjectId() : null);
	}

	@Override
	public String getSourceId() {
		final DatumId id = getId();
		return (id != null ? id.getSourceId() : null);
	}

	@Override
	public Instant getTimestamp() {
		final DatumId id = getId();
		return (id != null ? id.getTimestamp() : null);
	}

	@Override
	public Map<String, ?> getSampleData() {
		final DatumSamples s = samples;
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
	public DatumSamplesOperations asSampleOperations() {
		return samples;
	}

	@Override
	public MutableDatumSamplesOperations asMutableSampleOperations() {
		return samples;
	}

	/**
	 * Test if this datum has any sample property values.
	 * 
	 * @return {@literal true} if the samples is not empty
	 * @see net.solarnetwork.domain.datum.DatumSamples#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return samples.isEmpty();
	}

	@Override
	public Map<String, ?> getSampleData(DatumSamplesType type) {
		return samples.getSampleData(type);
	}

	@Override
	public void setSampleData(DatumSamplesType type, Map<String, ?> data) {
		samples.setSampleData(type, data);
	}

	@Override
	public Integer getSampleInteger(DatumSamplesType type, String key) {
		return samples.getSampleInteger(type, key);
	}

	@Override
	public Long getSampleLong(DatumSamplesType type, String key) {
		return samples.getSampleLong(type, key);
	}

	@Override
	public Float getSampleFloat(DatumSamplesType type, String key) {
		return samples.getSampleFloat(type, key);
	}

	@Override
	public Double getSampleDouble(DatumSamplesType type, String key) {
		return samples.getSampleDouble(type, key);
	}

	@Override
	public BigDecimal getSampleBigDecimal(DatumSamplesType type, String key) {
		return samples.getSampleBigDecimal(type, key);
	}

	@Override
	public String getSampleString(DatumSamplesType type, String key) {
		return samples.getSampleString(type, key);
	}

	@Override
	public <V> V getSampleValue(DatumSamplesType type, String key) {
		return samples.getSampleValue(type, key);
	}

	@Override
	public Set<String> getTags() {
		return samples.getTags();
	}

	@Override
	public void clear() {
		samples.clear();
	}

	/**
	 * Set the sample tags.
	 * 
	 * @param tags
	 *        the tags to set
	 * @see net.solarnetwork.domain.datum.DatumSupport#setTags(java.util.Set)
	 */
	@Override
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
	 * @see net.solarnetwork.domain.datum.DatumSupport#hasTag(java.lang.String)
	 */
	@Override
	public boolean hasTag(String tag) {
		return samples.hasTag(tag);
	}

	/**
	 * Add a sample tag.
	 * 
	 * @param tag
	 *        the tag to add
	 * @see net.solarnetwork.domain.datum.DatumSupport#addTag(java.lang.String)
	 */
	@Override
	public boolean addTag(String tag) {
		return samples.addTag(tag);
	}

	@Override
	public boolean hasSampleValue(String key) {
		return samples.hasSampleValue(key);
	}

	@Override
	public void putSampleValue(DatumSamplesType type, String key, Object value) {
		samples.putSampleValue(type, key, value);
	}

	/**
	 * Remove a sample tag.
	 * 
	 * @param tag
	 *        the tag to remove.
	 * @see net.solarnetwork.domain.datum.DatumSupport#removeTag(java.lang.String)
	 */
	public void removeTag(String tag) {
		samples.removeTag(tag);
	}

	@Override
	public boolean hasSampleValue(DatumSamplesType type, String key) {
		return samples.hasSampleValue(type, key);
	}

	/**
	 * Get the samples instance.
	 * 
	 * @return the samples, never {@literal null}
	 */
	@Override
	public DatumSamples getSamples() {
		return samples;
	}

}

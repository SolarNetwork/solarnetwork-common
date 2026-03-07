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

import static net.solarnetwork.util.ObjectUtils.requireNonNullArgument;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import net.solarnetwork.domain.BasicSerializableIdentity;
import net.solarnetwork.domain.CopyingIdentity;

/**
 * A basic implementation of {@link MutableDatum}.
 *
 * @author matt
 * @version 3.0
 * @since 1.71
 */
public class GeneralDatum extends BasicSerializableIdentity<DatumId>
		implements Datum, DatumSamplesContainer, MutableDatum, MutableDatumSamplesOperations,
		CopyingIdentity<GeneralDatum, DatumId>, Serializable, Cloneable {

	private static final long serialVersionUID = 1934830001340995747L;

	/** The samples. */
	private final DatumSamples samples;

	/**
	 * Constructor.
	 *
	 * @param id
	 *        the ID; if {@code null} a new instance will be created
	 * @param samples
	 *        the samples; if {@code null} a new instance will be created
	 */
	public GeneralDatum(DatumId id, @Nullable DatumSamples samples) {
		super(id != null ? id : new DatumId(null, null, null, null));
		this.samples = (samples != null ? samples : new DatumSamples());
	}

	/**
	 * Constructor.
	 *
	 * <p>
	 * This creates a {@code null} {@code kind} and {@code objectId} and sets
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
	 * This creates a {@code null} {@code kind} and {@code objectId}.
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
	 * This creates a {@code null} {@code kind} and {@code objectId}.
	 * </p>
	 *
	 * @param sourceId
	 *        the source ID
	 * @param timestamp
	 *        the timestamp
	 * @param samples
	 *        the samples; if {@code null} a new instance will be created
	 */
	public GeneralDatum(String sourceId, Instant timestamp, @Nullable DatumSamples samples) {
		this(new DatumId(null, null, sourceId, timestamp), samples);
	}

	/**
	 * Constructor.
	 *
	 * <p>
	 * This creates a {@code null} {@code kind}.
	 * </p>
	 *
	 * @param objectId
	 *        the object ID
	 * @param sourceId
	 *        the source ID
	 * @param timestamp
	 *        the timestamp
	 * @param samples
	 *        the samples; if {@code null} a new instance will be created
	 */
	public GeneralDatum(Long objectId, String sourceId, Instant timestamp,
			@Nullable DatumSamples samples) {
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
	 *        the samples; if {@code null} a new instance will be created
	 * @return the new instance
	 */
	public static GeneralDatum nodeDatum(Long nodeId, String sourceId, Instant timestamp,
			@Nullable DatumSamples samples) {
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
	 *        the samples; if {@code null} a new instance will be created
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
	public GeneralDatum copyWithSamples(DatumSamplesOperations samples) {
		final var id = requireNonNullArgument(getId(), "id");
		GeneralDatum copy = new GeneralDatum(id, new DatumSamples());
		copy.samples.copyFrom(samples);
		return copy;
	}

	@Override
	public GeneralDatum copyWithId(@Nullable DatumId id) {
		return new GeneralDatum(requireNonNullArgument(id, "id"), samples);
	}

	@Override
	public void copyTo(@Nullable GeneralDatum other) {
		if ( other == null ) {
			return;
		}
		other.samples.copyFrom(this.samples);
	}

	@SuppressWarnings("NullAway")
	@JsonIgnore
	@Override
	public final DatumId datumId() {
		return getId();
	}

	@Override
	public @Nullable Map<String, ?> getSampleData() {
		final DatumSamples s = samples;
		return (s != null ? s.getSampleData() : null);
	}

	@Override
	public final Map<String, ?> asSimpleMap() {
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
		final Map<String, Object> map = new LinkedHashMap<>();
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

	/**
	 * Get the datum types.
	 *
	 * @return the datum types
	 */
	protected String @Nullable [] datumTypes() {
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
	public @Nullable Map<String, ?> getSampleData(DatumSamplesType type) {
		return samples.getSampleData(type);
	}

	@Override
	public void setSampleData(DatumSamplesType type, @Nullable Map<String, ?> data) {
		samples.setSampleData(type, data);
	}

	@Override
	public @Nullable Integer getSampleInteger(DatumSamplesType type, String key) {
		return samples.getSampleInteger(type, key);
	}

	@Override
	public @Nullable Long getSampleLong(DatumSamplesType type, String key) {
		return samples.getSampleLong(type, key);
	}

	@Override
	public @Nullable Float getSampleFloat(DatumSamplesType type, String key) {
		return samples.getSampleFloat(type, key);
	}

	@Override
	public @Nullable Double getSampleDouble(DatumSamplesType type, String key) {
		return samples.getSampleDouble(type, key);
	}

	@Override
	public @Nullable BigDecimal getSampleBigDecimal(DatumSamplesType type, String key) {
		return samples.getSampleBigDecimal(type, key);
	}

	@Override
	public @Nullable String getSampleString(DatumSamplesType type, String key) {
		return samples.getSampleString(type, key);
	}

	@Override
	public <V> @Nullable V getSampleValue(DatumSamplesType type, String key) {
		return samples.getSampleValue(type, key);
	}

	@Override
	public @Nullable Set<String> getTags() {
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
	public void setTags(@Nullable Set<String> tags) {
		samples.setTags(tags);
	}

	@Override
	public <V> @Nullable V findSampleValue(String key) {
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
	public void putSampleValue(DatumSamplesType type, String key, @Nullable Object value) {
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
	 * @return the samples, never {@code null}
	 */
	@Override
	public final DatumSamples getSamples() {
		return samples;
	}

}

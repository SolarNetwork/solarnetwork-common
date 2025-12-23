/* ==================================================================
 * MutableDatumSamplesWithMetadata.java - 23/12/2025 12:49:33 pm
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

package net.solarnetwork.domain.datum;

import static net.solarnetwork.util.ObjectUtils.requireNonNullArgument;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import net.solarnetwork.util.NumberUtils;

/**
 * Combine samples with metadata datum samples type support.
 *
 * <p>
 * The {@link DatumSamplesType#Metadata} type is supported by invoking
 * {@link MutableDatumMetadataOperations#populate(String, Object)} with the
 * given key.
 * </p>
 *
 * @author matt
 * @version 1.0
 */
public class MutableDatumSamplesWithMetadata implements MutableDatumSamplesOperations {

	private final MutableDatumSamplesOperations samplesDelegate;
	private final MutableDatumMetadataOperations metadataDelegate;

	/**
	 * Constructor.
	 *
	 * @param samplesDelegate
	 *        the samples delegate
	 * @param metadataDelegate
	 *        the metadata delegate
	 * @throws IllegalArgumentException
	 *         if any argument is {@code null}
	 */
	public MutableDatumSamplesWithMetadata(MutableDatumSamplesOperations samplesDelegate,
			MutableDatumMetadataOperations metadataDelegate) {
		super();
		this.samplesDelegate = requireNonNullArgument(samplesDelegate, "samplesDelegate");
		this.metadataDelegate = requireNonNullArgument(metadataDelegate, "metadataDelegate");
	}

	@Override
	public Map<String, ?> getSampleData(DatumSamplesType type) {
		return samplesDelegate.getSampleData(type);
	}

	@Override
	public void clear() {
		samplesDelegate.clear();
		metadataDelegate.clear();
	}

	@Override
	public void putSampleValue(DatumSamplesType type, String key, Object value) {
		if ( type == DatumSamplesType.Metadata ) {
			metadataDelegate.populate(key, value);
		} else {
			samplesDelegate.putSampleValue(type, key, value);
		}
	}

	@Override
	public Integer getSampleInteger(DatumSamplesType type, String key) {
		if ( type == DatumSamplesType.Metadata ) {
			Number n = metadataDelegate.metadataAtPath(key, Number.class);
			return (n != null ? n.intValue() : null);
		}
		return samplesDelegate.getSampleInteger(type, key);
	}

	@Override
	public Long getSampleLong(DatumSamplesType type, String key) {
		if ( type == DatumSamplesType.Metadata ) {
			Number n = metadataDelegate.metadataAtPath(key, Number.class);
			return (n != null ? n.longValue() : null);
		}
		return samplesDelegate.getSampleLong(type, key);
	}

	@Override
	public Float getSampleFloat(DatumSamplesType type, String key) {
		if ( type == DatumSamplesType.Metadata ) {
			Number n = metadataDelegate.metadataAtPath(key, Number.class);
			return (n != null ? n.floatValue() : null);
		}
		return samplesDelegate.getSampleFloat(type, key);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void setSampleData(DatumSamplesType type, Map<String, ?> data) {
		if ( type == DatumSamplesType.Metadata ) {
			metadataDelegate.setInfo((Map) data);
		}
		samplesDelegate.setSampleData(type, data);
	}

	@Override
	public Double getSampleDouble(DatumSamplesType type, String key) {
		if ( type == DatumSamplesType.Metadata ) {
			Number n = metadataDelegate.metadataAtPath(key, Number.class);
			return (n != null ? n.doubleValue() : null);
		}
		return samplesDelegate.getSampleDouble(type, key);
	}

	@Override
	public void setTags(Set<String> tags) {
		samplesDelegate.setTags(tags);
	}

	@Override
	public BigDecimal getSampleBigDecimal(DatumSamplesType type, String key) {
		if ( type == DatumSamplesType.Metadata ) {
			Number n = metadataDelegate.metadataAtPath(key, Number.class);
			return NumberUtils.bigDecimalForNumber(n);
		}
		return samplesDelegate.getSampleBigDecimal(type, key);
	}

	@Override
	public boolean addTag(String tag) {
		return samplesDelegate.addTag(tag);
	}

	@Override
	public String getSampleString(DatumSamplesType type, String key) {
		if ( type == DatumSamplesType.Metadata ) {
			Object val = metadataDelegate.metadataAtPath(key);
			return (val != null ? val.toString() : null);
		}
		return samplesDelegate.getSampleString(type, key);
	}

	@Override
	public boolean removeTag(String... tags) {
		return samplesDelegate.removeTag(tags);
	}

	@Override
	public void copyFrom(DatumSamplesOperations other) {
		samplesDelegate.copyFrom(other);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V> V getSampleValue(DatumSamplesType type, String key) {
		if ( type == DatumSamplesType.Metadata ) {
			return (V) metadataDelegate.metadataAtPath(key);
		}
		return samplesDelegate.getSampleValue(type, key);
	}

	@Override
	public boolean hasSampleValue(DatumSamplesType type, String key) {
		if ( type == DatumSamplesType.Metadata ) {
			return metadataDelegate.hasMetadataAtPath(key);
		}
		return samplesDelegate.hasSampleValue(type, key);
	}

	@Override
	public void mergeFrom(DatumSamplesOperations other) {
		samplesDelegate.mergeFrom(other);
	}

	@Override
	public void mergeFrom(DatumSamplesOperations other, boolean overwrite) {
		samplesDelegate.mergeFrom(other, overwrite);
	}

	@Override
	public <V> V findSampleValue(String key) {
		return samplesDelegate.findSampleValue(key);
	}

	@Override
	public boolean hasSampleValue(String key) {
		return samplesDelegate.hasSampleValue(key);
	}

	@Override
	public Set<String> getTags() {
		return samplesDelegate.getTags();
	}

	@Override
	public boolean hasTag(String tag) {
		return samplesDelegate.hasTag(tag);
	}

	@Override
	public boolean isEmpty() {
		return samplesDelegate.isEmpty();
	}

	@Override
	public boolean differsFrom(DatumSamplesOperations other) {
		return samplesDelegate.differsFrom(other);
	}

}

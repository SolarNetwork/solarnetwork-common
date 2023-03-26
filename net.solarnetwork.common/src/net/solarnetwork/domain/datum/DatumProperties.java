/* ==================================================================
 * DatumProperties.java - 22/10/2020 10:38:32 am
 * 
 * Copyright 2020 SolarNetwork.net Dev Team
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A collection of property values for a datum.
 * 
 * <p>
 * The properties are stored as ordered arrays of values. The meaning of the
 * values depends on external {@link DatumStreamMetadata}. {@literal null}
 * values are allowed both as the array fields of this class and as values
 * within array instances.
 * </p>
 * 
 * @author matt
 * @version 1.2
 * @since 1.72
 */
public class DatumProperties implements Serializable {

	private static final long serialVersionUID = -2647856276610023629L;

	/** The instantaneous values. */
	private BigDecimal[] instantaneous;

	/** The accumulating values. */
	private BigDecimal[] accumulating;

	/** The status values. */
	private String[] status;

	/** The tags. */
	private String[] tags;

	/**
	 * Constructor.
	 */
	public DatumProperties() {
		super();
	}

	/**
	 * Create a datum properties instance.
	 * 
	 * @param instantaneous
	 *        the instantaneous values
	 * @param accumulating
	 *        the accumulating values
	 * @param status
	 *        the status values
	 * @param tags
	 *        the tag values
	 * @return the new instance, never {@literal null}
	 */
	public static DatumProperties propertiesOf(BigDecimal[] instantaneous, BigDecimal[] accumulating,
			String[] status, String[] tags) {
		DatumProperties s = new DatumProperties();
		s.instantaneous = instantaneous;
		s.accumulating = accumulating;
		s.status = status;
		s.tags = tags;
		return s;
	}

	/**
	 * Create a new instance out of a general datum and associated stream
	 * metadata.
	 * 
	 * <p>
	 * Note that trailing {@literal null} values will be removed from the
	 * instantaneous, accumulating, and status data arrays.
	 * </p>
	 * 
	 * @param datum
	 *        the datum to create properties from
	 * @param meta
	 *        the metadata that defines the property order
	 * @return the properties, or {@literal null} if {@code datum} is
	 *         {@literal null}
	 * @throws IllegalArgumentException
	 *         if the metadata does not support a property found on the datum
	 */
	public static DatumProperties propertiesFrom(Datum datum, ObjectDatumStreamMetadata meta)
			throws IllegalArgumentException {
		if ( datum == null ) {
			return null;
		}
		if ( meta == null ) {
			throw new IllegalArgumentException("No stream metadata available for datum " + datum);
		}
		DatumSamplesOperations ops = datum.asSampleOperations();
		if ( ops == null ) {
			return null;
		}
		BigDecimal[] data_i = decimalPropertiesFrom(meta, ops, DatumSamplesType.Instantaneous);
		BigDecimal[] data_a = decimalPropertiesFrom(meta, ops, DatumSamplesType.Accumulating);
		String[] data_s = stringPropertiesFrom(meta, ops, DatumSamplesType.Status);
		Set<String> tags = ops.getTags();
		String[] data_t = (tags != null && !tags.isEmpty() ? tags.toArray(new String[tags.size()])
				: null);
		return DatumProperties.propertiesOf(data_i, data_a, data_s, data_t);
	}

	private static BigDecimal[] decimalPropertiesFrom(ObjectDatumStreamMetadata meta,
			DatumSamplesOperations ops, DatumSamplesType type) {
		String[] propNames = meta.propertyNamesForType(type);
		int len = (propNames != null ? propNames.length : 0);
		BigDecimal[] data = null;
		Map<String, ?> map = ops.getSampleData(type);
		if ( len < 1 ) {
			if ( map != null && !map.isEmpty() ) {
				// unknown property; cannot map to stream
				throw new IllegalArgumentException(
						"Datum stream unknown " + type + " properties encountered: " + map.keySet());
			}
		} else if ( map != null && !map.isEmpty() ) {
			Set<String> props = new HashSet<>(map.keySet());
			data = new BigDecimal[propNames.length];
			int nonNullLength = 0;
			for ( int i = 0; i < len; i++ ) {
				BigDecimal n = ops.getSampleBigDecimal(type, propNames[i]);
				if ( n != null ) {
					data[i] = n;
					nonNullLength = i + 1;
				}
				props.remove(propNames[i]);
			}
			if ( !props.isEmpty() ) {
				// unknown property; cannot map to stream
				throw new IllegalArgumentException(
						"Datum stream unknown " + type + " properties encountered: " + props);
			}
			if ( nonNullLength < len ) {
				// optimization: trim trailing null values into shorter array
				BigDecimal[] trimmedData = new BigDecimal[nonNullLength];
				System.arraycopy(data, 0, trimmedData, 0, nonNullLength);
				data = trimmedData;
			}
		}
		return data;
	}

	private static String[] stringPropertiesFrom(ObjectDatumStreamMetadata meta,
			DatumSamplesOperations ops, DatumSamplesType type) {
		String[] propNames = meta.propertyNamesForType(type);
		int len = (propNames != null ? propNames.length : 0);
		String[] data = null;
		Map<String, ?> map = ops.getSampleData(type);
		if ( len < 1 ) {
			if ( map != null && !map.isEmpty() ) {
				// unknown property; cannot map to stream
				throw new IllegalArgumentException(
						"Datum stream unknown " + type + " properties encountered: " + map.keySet());
			}
		} else if ( map != null && !map.isEmpty() ) {
			Set<String> props = new HashSet<>(map.keySet());
			data = new String[propNames.length];
			int nonNullLength = 0;
			for ( int i = 0; i < len; i++ ) {
				String s = ops.getSampleString(type, propNames[i]);
				if ( s != null ) {
					data[i] = s;
					nonNullLength = i + 1;
				}
				props.remove(propNames[i]);
			}
			if ( !props.isEmpty() ) {
				// unknown property; cannot map to stream
				throw new IllegalArgumentException(
						"Datum stream unknown " + type + " properties encountered: " + props);
			}
			if ( nonNullLength < len ) {
				// optimization: trim trailing null values into shorter array
				String[] trimmedData = new String[nonNullLength];
				System.arraycopy(data, 0, trimmedData, 0, nonNullLength);
				data = trimmedData;
			}
		}

		return data;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(accumulating);
		result = prime * result + Arrays.hashCode(instantaneous);
		result = prime * result + Arrays.hashCode(status);
		result = prime * result + Arrays.hashCode(tags);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( !(obj instanceof DatumProperties) ) {
			return false;
		}
		DatumProperties other = (DatumProperties) obj;
		return Arrays.equals(accumulating, other.accumulating)
				&& Arrays.equals(instantaneous, other.instantaneous)
				&& Arrays.equals(status, other.status) && Arrays.equals(tags, other.tags);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DatumProperties{");
		if ( instantaneous != null ) {
			builder.append("instantaneous=");
			builder.append(Arrays.toString(instantaneous));
			builder.append(", ");
		}
		if ( accumulating != null ) {
			builder.append("accumulating=");
			builder.append(Arrays.toString(accumulating));
			builder.append(", ");
		}
		if ( status != null ) {
			builder.append("status=");
			builder.append(Arrays.toString(status));
			builder.append(", ");
		}
		if ( tags != null ) {
			builder.append("tags=");
			builder.append(Arrays.toString(tags));
		}
		builder.append("}");
		return builder.toString();
	}

	/**
	 * Get the overall number of array property values.
	 * 
	 * <p>
	 * This returns the sum of the length of all the array fields of this class.
	 * </p>
	 * 
	 * @return the number of values (including {@literal null} values)
	 */
	public int getLength() {
		return getInstantaneousLength() + getAccumulatingLength() + getStatusLength() + getTagsLength();
	}

	/**
	 * Get the instantaneous values array length.
	 * 
	 * @return the number of instantaneous values (including {@literal null}
	 *         values)
	 */
	public int getInstantaneousLength() {
		BigDecimal[] array = getInstantaneous();
		return (array != null ? array.length : 0);
	}

	/**
	 * Get the instantaneous values.
	 * 
	 * @return the instantaneous sample values
	 */
	public BigDecimal[] getInstantaneous() {
		return instantaneous;
	}

	/**
	 * Set the instantaneous values.
	 * 
	 * @param values
	 *        the values to set
	 */
	public void setInstantaneous(BigDecimal[] values) {
		this.instantaneous = values;
	}

	/**
	 * Get the value of a specific instantaneous property by index.
	 * 
	 * @param index
	 *        the property index to return
	 * @return the value, or {@literal null} if not available
	 * @since 1.2
	 */
	public BigDecimal instantaneousValue(int index) {
		final BigDecimal[] values = getInstantaneous();
		if ( values != null && index < values.length ) {
			return values[index];
		}
		return null;
	}

	/**
	 * Get the accumulating values array length.
	 * 
	 * @return the number of accumulating values (including {@literal null}
	 *         values)
	 */
	public int getAccumulatingLength() {
		BigDecimal[] array = getAccumulating();
		return (array != null ? array.length : 0);
	}

	/**
	 * Get the accumulating values.
	 * 
	 * @return the accumulating sample values
	 */
	public BigDecimal[] getAccumulating() {
		return accumulating;
	}

	/**
	 * Set the accumulating values.
	 * 
	 * @param values
	 *        the values to set
	 */
	public void setAccumulating(BigDecimal[] values) {
		this.accumulating = values;
	}

	/**
	 * Get the value of a specific accumulating property by index.
	 * 
	 * @param index
	 *        the property index to return
	 * @return the value, or {@literal null} if not available
	 * @since 1.2
	 */
	public BigDecimal accumulatingValue(int index) {
		final BigDecimal[] values = getAccumulating();
		if ( values != null && index < values.length ) {
			return values[index];
		}
		return null;
	}

	/**
	 * Get the status values array length.
	 * 
	 * @return the number of status values (including {@literal null} values)
	 */
	public int getStatusLength() {
		String[] array = getStatus();
		return (array != null ? array.length : 0);
	}

	/**
	 * Get the status values.
	 * 
	 * @return the status sample values
	 */
	public String[] getStatus() {
		return status;
	}

	/**
	 * Set the status values.
	 * 
	 * @param status
	 *        the values to set
	 */
	public void setStatus(String[] status) {
		this.status = status;
	}

	/**
	 * Get the value of a specific status property by index.
	 * 
	 * @param index
	 *        the property index to return
	 * @return the value, or {@literal null} if not available
	 * @since 1.2
	 */
	public String statusValue(int index) {
		final String[] values = getStatus();
		if ( values != null && index < values.length ) {
			return values[index];
		}
		return null;
	}

	/**
	 * Get the tags array length.
	 * 
	 * @return the number of tags (including {@literal null} values)
	 */
	public int getTagsLength() {
		String[] array = getTags();
		return (array != null ? array.length : 0);
	}

	/**
	 * Get the tag values.
	 * 
	 * @return the tag values
	 */
	public String[] getTags() {
		return tags;
	}

	/**
	 * Set the tag values.
	 * 
	 * @param tags
	 *        the tags to set
	 */
	public void setTags(String[] tags) {
		this.tags = tags;
	}

	/**
	 * Test if a specific tag exits (case-insensitive).
	 * 
	 * @param tag
	 *        the tag to test for
	 * @return {@literal true} if the given tag is present in the
	 *         {@link #getTags()} array
	 * @since 1.2
	 */
	public boolean hasTag(String tag) {
		final String[] tags = getTags();
		if ( tags != null ) {
			for ( int i = 0, len = tags.length; i < len; i++ ) {
				if ( tag.equalsIgnoreCase(tags[i]) ) {
					return true;
				}
			}
		}
		return false;
	}

}

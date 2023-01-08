/* ==================================================================
 * GeneralNodeDatumSamples.java - Aug 22, 2014 6:26:13 AM
 * 
 * Copyright 2007-2014 SolarNetwork.net Dev Team
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

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import net.solarnetwork.domain.KeyValuePair;
import net.solarnetwork.domain.SerializeIgnore;
import net.solarnetwork.util.NumberUtils;
import net.solarnetwork.util.StringUtils;

/**
 * Metadata about general node datum streams of data.
 * 
 * @author matt
 * @version 2.1
 */
@JsonPropertyOrder({ "m", "pm", "t" })
@JsonIgnoreProperties({ "empty", "infoKeys" })
public class GeneralDatumMetadata extends DatumSupport
		implements DatumMetadataOperations, MutableDatumMetadataOperations, Serializable {

	private static final long serialVersionUID = -2571643375746163527L;

	/** The info values. */
	private Map<String, Object> info;

	/** The property info values. */
	private Map<String, Map<String, Object>> propertyInfo;

	/**
	 * Default constructor.
	 */
	public GeneralDatumMetadata() {
		super();
	}

	/**
	 * Copy constructor.
	 * 
	 * <p>
	 * This constructor will copy all the top-level collections into new
	 * collection instances, but preserving all value instances.
	 * </p>
	 * 
	 * @param other
	 *        the metadata to copy
	 */
	public GeneralDatumMetadata(GeneralDatumMetadata other) {
		super();
		if ( other.getTags() != null ) {
			setTags(new LinkedHashSet<String>(other.getTags()));
		}
		if ( other.info != null ) {
			info = new LinkedHashMap<String, Object>(other.info);
		}
		if ( other.propertyInfo != null ) {
			propertyInfo = new LinkedHashMap<String, Map<String, Object>>(other.propertyInfo.size());
			for ( Map.Entry<String, Map<String, Object>> me : other.propertyInfo.entrySet() ) {
				propertyInfo.put(me.getKey(), new LinkedHashMap<String, Object>(me.getValue()));
			}
		}
	}

	/**
	 * Construct with values.
	 * 
	 * @param info
	 *        the info data
	 */
	public GeneralDatumMetadata(Map<String, Object> info) {
		super();
		this.info = info;
	}

	/**
	 * Construct with values.
	 * 
	 * @param info
	 *        the info data
	 * @param propertyInfo
	 *        the property info data
	 */
	public GeneralDatumMetadata(Map<String, Object> info,
			Map<String, Map<String, Object>> propertyInfo) {
		super();
		this.info = info;
		this.propertyInfo = propertyInfo;
	}

	/**
	 * Populate metadata values based on a list of {@link KeyValuePair}
	 * instances.
	 * 
	 * <p>
	 * Each pair's key will be used as a general metadata key, unless it starts
	 * with a {@literal /} character in which case a path is assumed. Values
	 * that can be coerced to number types will be.
	 * </p>
	 * 
	 * @param data
	 *        the data to populate
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void populate(final KeyValuePair[] data) {
		final int len = (data != null ? data.length : 0);
		for ( int i = 0; i < len; i++ ) {
			KeyValuePair kv = data[i];
			if ( kv == null ) {
				continue;
			}
			String key = kv.getKey();
			if ( key != null ) {
				key = key.trim();
			}
			if ( key == null || key.isEmpty() ) {
				continue;
			}
			String val = kv.getValue();
			if ( val != null ) {
				val = val.trim();
			}
			// treat as a number if we can
			Number numVal = NumberUtils.narrow(StringUtils.numberValue(val), 2);
			if ( key.startsWith("/") ) {
				String[] components = key.split("/");
				int idx = 0;
				if ( components[0].isEmpty() ) {
					idx += 1;
				}
				Map<String, Object> root = null;
				if ( "m".equals(components[idx]) && idx + 1 < components.length ) {
					root = getInfo();
					if ( root == null ) {
						root = new LinkedHashMap<>(8);
						setInfo(root);
					}
					idx++;
				} else if ( "pm".equals(components[idx]) && idx + 2 < components.length ) {
					root = (Map) getPropertyInfo();
					if ( root == null ) {
						root = new LinkedHashMap<>();
						setPropertyInfo((Map) root);
					}
					idx++;
				} else if ( "t".equals(components[idx]) ) {
					Set<String> tags = getTags();
					if ( tags == null ) {
						tags = new LinkedHashSet<>(8);
						setTags(tags);
					}
					tags.add(val);
				}
				if ( root != null ) {
					putMetadataAtPath(components, idx, root, numVal != null ? numVal : val);
				}
			} else {
				putInfoValue(key, numVal != null ? numVal : val);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void putMetadataAtPath(String[] components, int idx, Map<String, Object> root,
			Object value) {
		if ( idx + 1 < components.length ) {
			Object nested = root.get(components[idx]);
			if ( !(nested instanceof Map) ) {
				nested = new LinkedHashMap<>(8);
				root.put(components[idx], nested);
			}
			putMetadataAtPath(components, idx + 1, (Map<String, Object>) nested, value);
			return;
		}
		root.put(components[idx], value);
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("GeneralDatumMetadata{");
		if ( info != null && !info.isEmpty() ) {
			buf.append("m=").append(info).append(", ");
		}
		if ( propertyInfo != null && !propertyInfo.isEmpty() ) {

			buf.append("pm=").append(propertyInfo).append(", ");
		}
		if ( getTags() != null && !getTags().isEmpty() ) {
			buf.append("t=").append(getTags());
		}
		buf.append("}");
		return buf.toString();
	}

	@Override
	public void clear() {
		super.clear();
		info = null;
		propertyInfo = null;
	}

	@JsonIgnore
	@Override
	public Set<String> getPropertyInfoKeys() {
		return (propertyInfo != null ? propertyInfo.keySet() : Collections.emptySet());
	}

	@JsonIgnore
	@Override
	public Map<String, ?> getPropertyInfo(String key) {
		return (propertyInfo != null ? propertyInfo.get(key) : null);
	}

	@Override
	public void merge(final DatumMetadataOperations meta, final boolean replace) {
		if ( meta.getTags() != null ) {
			for ( String tag : meta.getTags() ) {
				addTag(tag);
			}
		}
		if ( meta.getInfo() != null ) {
			for ( Map.Entry<String, ?> me : meta.getInfo().entrySet() ) {
				// only overwrite keys, if replace is true
				if ( replace || getInfo() == null || getInfo().containsKey(me.getKey()) == false ) {
					putInfoValue(me.getKey(), me.getValue());
				}
			}
		}
		Set<String> propKeys = meta.getPropertyInfoKeys();
		if ( propKeys != null ) {
			Map<String, Map<String, Object>> gdmPropertyMeta = getPropertyInfo();
			if ( gdmPropertyMeta == null ) {
				for ( String propKey : propKeys ) {
					setInfo(propKey, new LinkedHashMap<>(meta.getPropertyInfo(propKey)));
				}
			} else {
				for ( String propKey : propKeys ) {
					if ( gdmPropertyMeta.get(propKey) == null ) {
						gdmPropertyMeta.put(propKey, new LinkedHashMap<>(meta.getPropertyInfo(propKey)));
					} else {
						Map<String, ?> pi = meta.getPropertyInfo(propKey);
						if ( pi != null ) {
							for ( Entry<String, ?> pme : pi.entrySet() ) {
								if ( replace == false
										&& gdmPropertyMeta.get(propKey).containsKey(pme.getKey()) ) {
									continue;
								}
								putInfoValue(propKey, pme.getKey(), pme.getValue());
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void putInfoValue(String key, Object value) {
		Map<String, Object> m = info;
		if ( m == null ) {
			if ( value == null ) {
				return;
			}
			m = new LinkedHashMap<String, Object>(4);
			info = m;
		}
		if ( value == null ) {
			m.remove(key);
		} else {
			m.put(key, value);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((info == null) ? 0 : info.hashCode());
		result = prime * result + ((propertyInfo == null) ? 0 : propertyInfo.hashCode());
		result = prime * result + ((getTags() == null) ? 0 : getTags().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( obj == null ) {
			return false;
		}
		if ( getClass() != obj.getClass() ) {
			return false;
		}
		GeneralDatumMetadata other = (GeneralDatumMetadata) obj;
		if ( info == null ) {
			if ( other.info != null ) {
				return false;
			}
		} else if ( !info.equals(other.info) ) {
			return false;
		}
		if ( propertyInfo == null ) {
			if ( other.propertyInfo != null ) {
				return false;
			}
		} else if ( !propertyInfo.equals(other.propertyInfo) ) {
			return false;
		}
		if ( getTags() == null ) {
			if ( other.getTags() != null ) {
				return false;
			}
		} else if ( !getTags().equals(other.getTags()) ) {
			return false;
		}
		return true;
	}

	@Override
	@JsonIgnore
	@SerializeIgnore
	public Map<String, Object> getInfo() {
		return info;
	}

	@Override
	public void setInfo(Map<String, Object> info) {
		this.info = info;
	}

	/**
	 * Shortcut for {@link #getInfo()}.
	 * 
	 * @return map
	 */
	public Map<String, Object> getM() {
		return getInfo();
	}

	/**
	 * Shortcut for {@link #setInfo(Map)}.
	 * 
	 * @param map
	 *        the Map to set
	 */
	public void setM(Map<String, Object> map) {
		setInfo(map);
	}

	/**
	 * Get a map of <em>property info</em> maps. Each top-level key represents a
	 * property name and the associated map the metadata for that property.
	 * 
	 * @return map
	 */
	@JsonIgnore
	@SerializeIgnore
	public Map<String, Map<String, Object>> getPropertyInfo() {
		return propertyInfo;
	}

	@Override
	public void setInfo(String key, Map<String, Object> info) {
		Map<String, Map<String, Object>> map = getPropertyInfo();
		if ( map == null ) {
			map = new LinkedHashMap<>(8);
			setPropertyInfo(map);
		}
		if ( info == null ) {
			map.remove(key);
		} else {
			map.put(key, info);
		}
	}

	/**
	 * Set the property info.
	 * 
	 * @param propertyInfo
	 *        the property info to set
	 */
	public void setPropertyInfo(Map<String, Map<String, Object>> propertyInfo) {
		this.propertyInfo = propertyInfo;
	}

	/**
	 * Shortcut for {@link #getPropertyInfo()}.
	 * 
	 * @return the map
	 */
	public Map<String, Map<String, Object>> getPm() {
		return getPropertyInfo();
	}

	/**
	 * Shortcut for {@link GeneralDatumMetadata#setPropertyInfo(Map)}.
	 * 
	 * @param map
	 *        the map to set
	 */
	public void setPm(Map<String, Map<String, Object>> map) {
		setPropertyInfo(map);
	}

	@Override
	public void putInfoValue(String property, String key, Object value) {
		Map<String, Map<String, Object>> pm = propertyInfo;
		if ( pm == null ) {
			if ( value == null ) {
				return;
			}
			pm = new LinkedHashMap<String, Map<String, Object>>(4);
			propertyInfo = pm;
		}
		Map<String, Object> m = pm.get(property);
		if ( m == null ) {
			if ( value == null ) {
				return;
			}
			m = new LinkedHashMap<String, Object>(4);
			pm.put(property, m);
		}
		if ( value == null ) {
			m.remove(key);
		} else {
			m.put(key, value);
		}
	}

	@Override
	public Object metadataAtPath(String path) {
		return metadataAtPath(path, this);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T metadataAtPath(String path, Class<T> clazz) {
		Object o = metadataAtPath(path);
		if ( o != null && clazz.isAssignableFrom(o.getClass()) ) {
			return (T) o;
		}
		return null;
	}

	/**
	 * Get a metadata value at a given path.
	 * 
	 * <p>
	 * The {@code path} syntax is that of URL paths, using a {@literal /}
	 * delimiter between nested metadata objects. The top-level path component
	 * must be one of {@literal /m} for {@link #getInfo()} data, {@literal /pm}
	 * for {@link #getPropertyInfo()} data, or {@literal /t} for
	 * {@link #getTags()} data.
	 * </p>
	 * 
	 * <p>
	 * For example, the path {@literal /m/foo} would return the value associated
	 * with the "foo" key in the {@code Map} returned from {@link #getInfo()}.
	 * The path {@literal /pm/foo/bar} would return the "bar" key in the
	 * {@code Map} associated with the "foo" key in the {@code Map} returned
	 * from {@link #getPropertyInfo()}.
	 * </p>
	 * 
	 * <p>
	 * For tags, using the {@literal /t} path will return the complete
	 * {@code Set} of tags returned by {@link #getTags()}. If the path has
	 * another component, then the next component value will be returned if a
	 * tag matching that component value exists. For example the path
	 * {@literal /t/foo} would return {@literal foo} if {@link #getTags()}
	 * contains {@literal foo}, otherwise {@literal null}.
	 * </p>
	 * 
	 * @param path
	 *        the path of the metadata object to get
	 * @param meta
	 *        the metadata to look in
	 * @return the metadata value, or {@literal null} if none exists at the
	 *         given path
	 * @since 1.3
	 */
	public static Object metadataAtPath(String path, GeneralDatumMetadata meta) {
		if ( path == null || path.isEmpty() || meta == null ) {
			return null;
		}
		Object result = null;
		String[] components = path.split("/");
		int idx = 0;
		if ( components[0].isEmpty() ) {
			idx += 1;
		}
		if ( "m".equals(components[idx]) ) {
			return metadataAtPath(components, idx + 1, meta.getM());
		} else if ( "pm".equals(components[idx]) ) {
			return metadataAtPath(components, idx + 1, meta.getPm());
		} else if ( "t".equals(components[idx]) ) {
			Set<String> tags = meta.getT();
			if ( idx + 1 < components.length ) {
				String tag = components[idx + 1];
				return tags.contains(tag) ? tag : null;
			}
			return tags;
		}
		return result;
	}

	/**
	 * Get a metadata value of a given type at a given path.
	 * 
	 * @param <T>
	 *        the expected return type
	 * @param path
	 *        the path of the metadata object to get
	 * @param meta
	 *        the metadata to look in
	 * @param clazz
	 *        the expected class of the return type
	 * @return the metadata, or {@literal null} if none exists at the given path
	 *         or is not of type {@code T}
	 * @see GeneralDatumMetadata#metadataAtPath(String, GeneralDatumMetadata,
	 *      Class)
	 * @since 1.3
	 */
	@SuppressWarnings("unchecked")
	public static <T> T metadataAtPath(String path, GeneralDatumMetadata meta, Class<T> clazz) {
		Object o = metadataAtPath(path, meta);
		if ( o != null && clazz.isAssignableFrom(o.getClass()) ) {
			return (T) o;
		}
		return null;

	}

	private static Object metadataAtPath(String[] pathComponents, int idx, Map<String, ?> data) {
		if ( data == null ) {
			return null;
		}
		if ( idx >= pathComponents.length ) {
			// can happen if requesting a root path
			return data;
		}
		Object v = data.get(pathComponents[idx]);
		if ( idx == pathComponents.length - 1 ) {
			return v;
		}
		if ( v instanceof Map<?, ?> ) {
			@SuppressWarnings("unchecked")
			Map<String, ?> m = (Map<String, ?>) v;
			return metadataAtPath(pathComponents, idx + 1, m);
		}
		return null;
	}

}

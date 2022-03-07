/* ==================================================================
 * BasicIdentifiable.java - 26/11/2019 6:39:24 am
 * 
 * Copyright 2019 SolarNetwork.net Dev Team
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

package net.solarnetwork.service.support;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static net.solarnetwork.util.ArrayUtils.arrayWithLength;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.context.MessageSource;
import net.solarnetwork.domain.KeyValuePair;
import net.solarnetwork.service.Identifiable;
import net.solarnetwork.settings.SettingSpecifier;
import net.solarnetwork.settings.support.BasicGroupSettingSpecifier;
import net.solarnetwork.settings.support.BasicTextFieldSettingSpecifier;
import net.solarnetwork.settings.support.SettingUtils;
import net.solarnetwork.util.NumberUtils;
import net.solarnetwork.util.StringUtils;

/**
 * Basic implementation of {@link Identifiable}.
 * 
 * @author matt
 * @version 2.1
 * @since 1.56
 */
public class BasicIdentifiable implements Identifiable {

	private String uid;
	private String groupUid;
	private String displayName;
	private MessageSource messageSource;
	private KeyValuePair[] metadata;

	/**
	 * Get settings for the configurable properties of
	 * {@link BasicIdentifiable}.
	 * 
	 * <p>
	 * Empty strings are used for the {@code prefix}, default {@code uid} and
	 * default {@code groupUid} setting values.
	 * </p>
	 * 
	 * @return the settings
	 * @see #basicIdentifiableSettings(String, String, String)
	 * @since 1.1
	 */
	public static List<SettingSpecifier> basicIdentifiableSettings() {
		return basicIdentifiableSettings("", "", "");
	}

	/**
	 * Get settings for the configurable {@code uid}/{@code groupUid} properties
	 * of {@link BasicIdentifiable}.
	 * 
	 * <p>
	 * Empty strings are used for the default {@code uid} and {@code groupUid}
	 * setting values.
	 * </p>
	 * 
	 * @param prefix
	 *        an optional prefix to include in all setting keys
	 * @return the settings
	 * @see #basicIdentifiableSettings(String, String, String)
	 * @since 1.1
	 */
	public static List<SettingSpecifier> basicIdentifiableSettings(String prefix) {
		return basicIdentifiableSettings(prefix, "", "");
	}

	/**
	 * Get settings for the configurable {@code uid}/{@code groupUid} properties
	 * of {@link BasicIdentifiable}.
	 * 
	 * @param prefix
	 *        an optional prefix to include in all setting keys
	 * @param defaultUid
	 *        the default {@code uid} value to use
	 * @param defaultGroupUid
	 *        the default {@code groupUid} value to use
	 * @return the settings
	 * @since 1.1
	 */
	public static List<SettingSpecifier> basicIdentifiableSettings(String prefix, String defaultUid,
			String defaultGroupUid) {
		if ( prefix == null ) {
			prefix = "";
		}
		List<SettingSpecifier> results = new ArrayList<>(8);
		results.add(new BasicTextFieldSettingSpecifier(prefix + "uid", defaultUid));
		results.add(new BasicTextFieldSettingSpecifier(prefix + "groupUid", defaultGroupUid));
		return results;
	}

	/**
	 * Get settings for the configurable {@code metadata} list property.
	 * 
	 * @param prefix
	 *        an optional prefix to include in all setting keys
	 * @param metadata
	 *        the metadata to get settings for
	 * @return the settings
	 * @see #basicIdentifiableSettings(String, String, String)
	 * @since 2.1
	 */
	public static List<SettingSpecifier> basicIdentifiableMetadataSettings(String prefix,
			KeyValuePair[] metadata) {
		if ( prefix == null ) {
			prefix = "";
		}
		List<KeyValuePair> exprConfsList = (metadata != null ? asList(metadata) : emptyList());
		return singletonList(SettingUtils.dynamicListSettingSpecifier(prefix + "metadata", exprConfsList,
				new SettingUtils.KeyedListCallback<KeyValuePair>() {

					@Override
					public Collection<SettingSpecifier> mapListSettingKey(KeyValuePair value, int index,
							String key) {
						List<SettingSpecifier> g = new ArrayList<>(2);
						g.add(new BasicTextFieldSettingSpecifier(key + ".key",
								value != null ? value.getKey() : null));
						g.add(new BasicTextFieldSettingSpecifier(key + ".value",
								value != null ? value.getValue() : null));
						return singletonList(new BasicGroupSettingSpecifier(g));
					}
				}));
	}

	/**
	 * Get a specific metadata value for a given key.
	 * 
	 * @param key
	 *        the metadata key to get the value for
	 * @return the value found on the first matching metadata key, or
	 *         {@literal null} if not found
	 * @since 2.1
	 */
	public String metadataValue(String key) {
		if ( key == null ) {
			return null;
		}
		final KeyValuePair[] data = getMetadata();
		if ( data != null ) {
			for ( KeyValuePair p : data ) {
				if ( p == null ) {
					continue;
				}
				if ( key.equals(p.getKey()) ) {
					return p.getValue();
				}
			}
		}
		return null;
	}

	/**
	 * Get a specific metadata value for a given key, coerced to a number if
	 * possible.
	 * 
	 * @param key
	 *        the metadata key to get the value for
	 * @return the value found on the first matching metadata key, or
	 *         {@literal null} if not found
	 * @since 2.1
	 */
	public Object smartMetadataValue(String key) {
		String val = metadataValue(key);
		if ( val != null ) {
			Number n = NumberUtils.narrow(StringUtils.numberValue(val), 2);
			if ( n != null ) {
				return n;
			}
		}
		return val;
	}

	/**
	 * Get a specific metadata value for a given key as a number.
	 * 
	 * @param key
	 *        the metadata key to get the value for
	 * @return the value found on the first matching metadata key, or
	 *         {@literal null} if not found or the value cannot be coerced to a
	 *         number
	 * @since 2.1
	 */
	public Number numberMetadataValue(String key) {
		Object val = smartMetadataValue(key);
		return (val instanceof Number ? (Number) val : null);
	}

	/**
	 * Get a specific metadata value for a given key as an integer.
	 * 
	 * @param key
	 *        the metadata key to get the value for
	 * @return the value found on the first matching metadata key, or
	 *         {@literal null} if not found or the value cannot be coerced to an
	 *         integer
	 * @since 2.1
	 */
	public Integer integerMetadataValue(String key) {
		Object val = smartMetadataValue(key);
		return (val instanceof Integer ? (Integer) val
				: val instanceof Number ? ((Number) val).intValue() : null);
	}

	/**
	 * Get a specific metadata value for a given key as a double.
	 * 
	 * @param key
	 *        the metadata key to get the value for
	 * @return the value found on the first matching metadata key, or
	 *         {@literal null} if not found or the value cannot be coerced to a
	 *         double
	 * @since 2.1
	 */
	public Double doubleMetadataValue(String key) {
		Object val = smartMetadataValue(key);
		return (val instanceof Double ? (Double) val
				: val instanceof Number ? ((Double) val).doubleValue() : null);
	}

	/**
	 * Save a specific metadata value for a given key, optionally adding a new
	 * {@link KeyValuePair} if not found.
	 * 
	 * @param key
	 *        the metadata key to get the value for
	 * @param value
	 *        the value to save
	 * @param create
	 *        {@literal true} if a new {@link KeyValuePair} instance should be
	 *        added if one with {@code key} does not already exist
	 * @since 2.1
	 */
	public void saveMetadataValue(String key, Object value, boolean create) {
		if ( key == null ) {
			return;
		}
		String val = (value != null ? value.toString() : null);
		KeyValuePair[] data = getMetadata();
		if ( data != null ) {
			for ( KeyValuePair p : data ) {
				if ( p != null && key.equals(p.getKey()) ) {
					p.setValue(val);
					return;
				}
			}
		}
		if ( create ) {
			data = arrayWithLength(data, (data == null ? 0 : data.length) + 1, KeyValuePair.class,
					KeyValuePair::new);
			data[data.length - 1].setKey(key);
			data[data.length - 1].setValue(val);
			setMetadata(data);
		}
	}

	/**
	 * Save a specific metadata value for a given key, optionally adding a new
	 * {@link KeyValuePair} if not found.
	 * 
	 * @param index
	 *        a specific index to save the metadata to
	 * @param key
	 *        the metadata key to use
	 * @param value
	 *        the value to save
	 * @param create
	 *        {@literal true} if a new {@link KeyValuePair} instance should be
	 *        added if one with {@code key} does not already exist
	 * @since 2.1
	 */
	public void saveMetadataValue(int index, String key, Object value, boolean create) {
		if ( index < 0 ) {
			throw new ArrayIndexOutOfBoundsException(index);
		}
		String val = (value != null ? value.toString() : null);
		KeyValuePair[] data = getMetadata();
		if ( data != null && index < data.length ) {
			KeyValuePair p = data[index];
			if ( p == null && create ) {
				p = new KeyValuePair(key, val);
				data[index] = p;
			} else if ( p != null ) {
				p.setKey(key);
				p.setValue(val);
			}
		} else if ( create ) {
			data = arrayWithLength(data, index + 1, KeyValuePair.class, KeyValuePair::new);
			data[index].setKey(key);
			data[index].setValue(val);
			setMetadata(data);
		}
	}

	@Override
	public String getUid() {
		return uid;
	}

	/**
	 * Set the UID.
	 * 
	 * @param uid
	 *        the UID to set
	 */
	public void setUid(String uid) {
		this.uid = uid;
	}

	@Override
	public String getGroupUid() {
		return groupUid;
	}

	/**
	 * Set the group UID.
	 * 
	 * @param groupUid
	 *        the group UID to set
	 */
	public void setGroupUid(String groupUid) {
		this.groupUid = groupUid;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Set the display name.
	 * 
	 * @param displayName
	 *        the displayName to set
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * Get a message source, to use for localizing this service with.
	 * 
	 * @return a message source
	 */
	public MessageSource getMessageSource() {
		return messageSource;
	}

	/**
	 * Set a message source, to use for localizing this service with.
	 * 
	 * @param messageSource
	 *        the message source to use
	 */
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	/*-----
	 * The following methods are here for backwards-compatibility.
	 *----- */

	/**
	 * Alias for {@link #getUid()}.
	 * 
	 * @return the UID
	 * @deprecated since 2.0, use {@link #getUid()}
	 */
	@Deprecated
	public String getUID() {
		return getUid();
	}

	/**
	 * Set the UID.
	 * 
	 * <p>
	 * This is an alias for {@link #setUid(String)}, but only if a value has not
	 * already been set.
	 * </p>
	 * 
	 * @param uid
	 *        the UID to set
	 * @deprecated since 2.0, use {@link #setUid(String)}
	 */
	@Deprecated
	public void setUID(String uid) {
		String currUid = getUid();
		if ( currUid == null || currUid.isEmpty() ) {
			setUid(uid);
		}
	}

	/**
	 * Alias for {@link #getGroupUid()}.
	 * 
	 * @return the group UID
	 * @deprecated since 2.0, use {@link #getGroupUid()}
	 */
	@Deprecated
	public String getGroupUID() {
		return getGroupUid();
	}

	/**
	 * Set the group UID.
	 * 
	 * <p>
	 * This is an alias for {@link #setGroupUid(String)}, but only if a value
	 * has not already been set.
	 * </p>
	 * 
	 * @param groupUid
	 *        the group UID to set
	 * @deprecated since 2.0, use {@link #setGroupUid(String)}
	 */
	@Deprecated
	public void setGroupUID(String groupUid) {
		String currUid = getGroupUid();
		if ( currUid == null || currUid.isEmpty() ) {
			setGroupUid(groupUid);
		}
	}

	/**
	 * Get a list of metadata values.
	 * 
	 * @return the metadata, or {@literal null}
	 * @since 2.1
	 */
	public KeyValuePair[] getMetadata() {
		return metadata;
	}

	/**
	 * Set a list of metadata values.
	 * 
	 * @param metadata
	 *        the metadata to set, or {@literal null}
	 * @since 2.1
	 */
	public void setMetadata(KeyValuePair[] metadata) {
		this.metadata = metadata;
	}

	/**
	 * Get the number of configured {@code metadata} elements.
	 *
	 * @return the number of {@code metadata} elements
	 * @since 2.1
	 */
	public int getMetadataCount() {
		final KeyValuePair[] vals = getMetadata();
		return (vals == null ? 0 : vals.length);
	}

	/**
	 * Adjust the number of configured {@code metadata} elements.
	 *
	 * <p>
	 * Any newly added element values will be set to new {@link KeyValuePair}
	 * instances.
	 * </p>
	 *
	 * @param count
	 *        the desired number of {@code metadata} elements
	 * @since 2.1
	 */
	public void setMetadataCount(int count) {
		setMetadata(arrayWithLength(getMetadata(), count, KeyValuePair.class, KeyValuePair::new));
	}

}

/* ==================================================================
 * SettingUtils.java - 16/04/2018 9:32:39 AM
 * 
 * Copyright 2018 SolarNetwork.net Dev Team
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

package net.solarnetwork.settings.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import net.solarnetwork.settings.GroupSettingSpecifier;
import net.solarnetwork.settings.KeyedSettingSpecifier;
import net.solarnetwork.settings.MappableSpecifier;
import net.solarnetwork.settings.ParentSettingSpecifier;
import net.solarnetwork.settings.RadioGroupSettingSpecifier;
import net.solarnetwork.settings.SettingSpecifier;
import net.solarnetwork.settings.SettingSpecifierProvider;
import net.solarnetwork.settings.SliderSettingSpecifier;
import net.solarnetwork.settings.TextAreaSettingSpecifier;
import net.solarnetwork.settings.TextFieldSettingSpecifier;
import net.solarnetwork.settings.TitleSettingSpecifier;
import net.solarnetwork.settings.ToggleSettingSpecifier;

/**
 * Helper utilities for settings.
 * 
 * @author matt
 * @version 1.2
 * @since 1.43
 */
public final class SettingUtils {

	private SettingUtils() {
		// Do not construct me.
	}

	/**
	 * API to map a list element into a set of {@link SettingSpecifier} objects.
	 * 
	 * @param <T>
	 *        The collection type.
	 */
	public interface KeyedListCallback<T> {

		/**
		 * Map a single list element value into one or more
		 * {@link SettingSpecifier} objects.
		 * 
		 * @param value
		 *        The list element value.
		 * @param index
		 *        The list element index.
		 * @param key
		 *        An indexed key prefix to use for the grouped settings.
		 * @return The settings.
		 */
		public Collection<SettingSpecifier> mapListSettingKey(T value, int index, String key);

	}

	/**
	 * Get a dynamic list {@link GroupSettingSpecifier}.
	 * 
	 * @param <T>
	 *        the group item type
	 * @param key
	 *        the group setting key
	 * @param collection
	 *        The collection to turn into settings.
	 * @param mapper
	 *        A helper to map individual elements into settings.
	 * @return The resulting {@link GroupSettingSpecifier}.
	 */
	public static <T> BasicGroupSettingSpecifier dynamicListSettingSpecifier(String key,
			Collection<T> collection, KeyedListCallback<T> mapper) {
		List<SettingSpecifier> listStringGroupSettings;
		if ( collection == null ) {
			listStringGroupSettings = Collections.emptyList();
		} else {
			final int len = collection.size();
			listStringGroupSettings = new ArrayList<SettingSpecifier>(len);
			int i = 0;
			for ( T value : collection ) {
				Collection<SettingSpecifier> res = mapper.mapListSettingKey(value, i,
						key + "[" + i + "]");
				i++;
				if ( res != null ) {
					listStringGroupSettings.addAll(res);
				}
			}
		}
		return new BasicGroupSettingSpecifier(key, listStringGroupSettings, true);
	}

	/**
	 * Get a set of setting keys that require secure handling.
	 * 
	 * <p>
	 * This method considers the following settings for secure handling :
	 * </p>
	 * 
	 * <ol>
	 * <li>{@link TextFieldSettingSpecifier#isSecureTextEntry()} that returns
	 * {@literal true}</li>
	 * </ol>
	 * 
	 * <p>
	 * The returned set maintains the same iteration order as {@code settings}.
	 * </p>
	 * 
	 * @param settings
	 *        the settings to check ({@literal null} allowed)
	 * @return the set of secure entry keys, never {@literal null}
	 */
	public static Set<String> secureKeys(List<SettingSpecifier> settings) {
		if ( settings == null || settings.isEmpty() ) {
			return Collections.emptySet();
		}
		Set<String> secureProps = null;
		for ( SettingSpecifier setting : settings ) {
			if ( setting instanceof TextFieldSettingSpecifier ) {
				TextFieldSettingSpecifier text = (TextFieldSettingSpecifier) setting;
				if ( text.isSecureTextEntry() ) {
					String key = text.getKey();
					if ( secureProps == null ) {
						secureProps = new LinkedHashSet<String>(4);
					}
					secureProps.add(key);
				}
			}
		}
		return (secureProps != null ? secureProps : Collections.<String> emptySet());
	}

	/**
	 * Add a prefix to the keys of all {@link MappableSpecifier} settings.
	 * 
	 * @param settings
	 *        the settings to map
	 * @param prefix
	 *        the prefix to add to all {@link MappableSpecifier} settings
	 * @return list of mapped settings, or {@literal null} if {@code settings}
	 *         is {@literal null}
	 * @since 1.1
	 */
	public static List<SettingSpecifier> mappedWithPrefix(List<SettingSpecifier> settings,
			String prefix) {
		if ( settings == null || settings.isEmpty() || prefix == null || prefix.isEmpty() ) {
			return settings;
		}
		List<SettingSpecifier> result = new ArrayList<>(settings.size());
		for ( SettingSpecifier setting : settings ) {
			if ( setting instanceof MappableSpecifier ) {
				result.add(((MappableSpecifier) setting).mappedTo(prefix));
			} else {
				result.add(setting);
			}
		}
		return result;
	}

	/**
	 * Extract all {@link KeyedSettingSpecifier} keys and associated default
	 * values from a list of settings.
	 * 
	 * <p>
	 * Both {@link GroupSettingSpecifier#getGroupSettings()} and
	 * {@link ParentSettingSpecifier#getChildSettings()} will be included in the
	 * returned map.
	 * </p>
	 * 
	 * @param settings
	 *        the settings to extract the keyed defaults from
	 * @return a map of keyed setting keys to associated default values, never
	 *         {@literal null}
	 * @since 1.1
	 */
	public static Map<String, Object> keyedSettingDefaults(List<SettingSpecifier> settings) {
		if ( settings == null || settings.isEmpty() ) {
			return Collections.emptyMap();
		}
		Map<String, Object> result = new LinkedHashMap<>(settings.size());
		addKeyedSettingDefaults(settings, result);
		return result;
	}

	private static void addKeyedSettingDefaults(List<SettingSpecifier> settings,
			Map<String, Object> result) {
		if ( settings == null || settings.isEmpty() ) {
			return;
		}
		for ( SettingSpecifier setting : settings ) {
			if ( setting instanceof KeyedSettingSpecifier<?> ) {
				KeyedSettingSpecifier<?> keyed = (KeyedSettingSpecifier<?>) setting;
				result.put(keyed.getKey(), keyed.getDefaultValue());
			} else if ( setting instanceof GroupSettingSpecifier ) {
				addKeyedSettingDefaults(((GroupSettingSpecifier) setting).getGroupSettings(), result);
			} else if ( setting instanceof ParentSettingSpecifier ) {
				addKeyedSettingDefaults(((ParentSettingSpecifier) setting).getChildSettings(), result);
			}
		}
	}

	/**
	 * Create a simple template data structure representing all setting
	 * specifiers of a setting specifier provider.
	 * 
	 * <p>
	 * The data structure is designed to be a basic representation of all
	 * {@link SettingSpecifier} instances returned by
	 * {@link SettingSpecifierProvider#templateSettingSpecifiers()}.
	 * </p>
	 * 
	 * @param provider
	 *        the provider to generate the template specification for
	 * @param callback
	 *        an optional callback to modify the generated template
	 *        specifications, including handling of custom specification types
	 * @return the list of specification objects
	 * @see #populateTemplateSpecification(SettingSpecifier, List, BiConsumer)
	 * @since 1.2
	 */
	public static List<Map<String, Object>> templateSpecification(
			final SettingSpecifierProvider provider,
			final BiConsumer<SettingSpecifier, Map<String, Object>> callback) {
		List<SettingSpecifier> specs = provider.templateSettingSpecifiers();
		List<Map<String, Object>> resultSettings = new ArrayList<>(specs.size());
		for ( SettingSpecifier spec : specs ) {
			populateTemplateSpecification(spec, resultSettings, null);
		}
		return resultSettings;
	}

	/**
	 * Add a template specification for a given {@link SettingSpecifier} to a
	 * result list.
	 * 
	 * @param spec
	 *        the specifier to create the template specification for
	 * @param result
	 *        the list to add the newly created template specification to
	 * @param callback
	 *        an optional callback to modify the generated template
	 *        specifications, including handling of custom specification types
	 * @since 1.2
	 */
	public static void populateTemplateSpecification(final SettingSpecifier spec,
			final List<Map<String, Object>> result,
			final BiConsumer<SettingSpecifier, Map<String, Object>> callback) {
		Map<String, Object> props = null;
		if ( spec instanceof GroupSettingSpecifier ) {
			GroupSettingSpecifier group = (GroupSettingSpecifier) spec;
			props = createBaseTemplateSpecification(spec, 4);
			if ( group.isDynamic() ) {
				props.put("dynamic", group.isDynamic());
			}
			if ( group.getKey() != null && !group.getKey().isEmpty() ) {
				props.put("key", group.getKey());
			}
			if ( group.getFooterText() != null && !group.getFooterText().isEmpty() ) {
				props.put("footerText", group.getFooterText());
			}
			List<Map<String, Object>> nested = new ArrayList<>(8);
			for ( SettingSpecifier groupSpec : group.getGroupSettings() ) {
				populateTemplateSpecification(groupSpec, nested, callback);
			}
			props.put("groupSettings", nested);
		} else if ( spec instanceof ParentSettingSpecifier ) {
			ParentSettingSpecifier parent = (ParentSettingSpecifier) spec;
			props = createBaseTemplateSpecification(spec, 4);
			List<Map<String, Object>> nested = new ArrayList<>(8);
			for ( SettingSpecifier groupSpec : parent.getChildSettings() ) {
				populateTemplateSpecification(groupSpec, nested, callback);
			}
			props.put("childSettings", nested);
		} else if ( spec instanceof KeyedSettingSpecifier<?> ) {
			KeyedSettingSpecifier<?> keyedSpec = (KeyedSettingSpecifier<?>) spec;
			if ( keyedSpec.isTransient() ) {
				return;
			}
			props = createBaseTemplateSpecification(spec, 8);
			props.put("key", keyedSpec.getKey());

			Object defaultValue = keyedSpec.getDefaultValue();
			if ( defaultValue != null ) {
				if ( !((defaultValue instanceof String) && ((String) defaultValue).isEmpty()) ) {
					props.put("defaultValue", keyedSpec.getDefaultValue());
				}
			}

			Object[] descArgs = keyedSpec.getDescriptionArguments();
			if ( descArgs != null && descArgs.length > 0 ) {
				props.put("descriptionArguments", descArgs);
			}

			if ( spec instanceof SliderSettingSpecifier ) {
				SliderSettingSpecifier slider = (SliderSettingSpecifier) spec;
				props.put("minimumValue", slider.getMinimumValue());
				props.put("maximumValue", slider.getMaximumValue());
				props.put("step", slider.getStep());
			} else if ( spec instanceof TextAreaSettingSpecifier ) {
				TextAreaSettingSpecifier area = (TextAreaSettingSpecifier) spec;
				if ( area.isDirect() ) {
					props.put("direct", area.isDirect());
				}
			} else if ( spec instanceof TitleSettingSpecifier ) {
				TitleSettingSpecifier title = (TitleSettingSpecifier) spec;
				if ( title.isMarkup() ) {
					props.put("markup", title.isMarkup());
				}
				if ( title.getValueTitles() != null ) {
					props.put("valueTitles", title.getValueTitles());
				}
				if ( spec instanceof TextFieldSettingSpecifier ) {
					TextFieldSettingSpecifier text = (TextFieldSettingSpecifier) spec;
					if ( text.isSecureTextEntry() ) {
						props.put("secureTextEntry", text.isSecureTextEntry());
					}
					if ( spec instanceof RadioGroupSettingSpecifier ) {
						RadioGroupSettingSpecifier radio = (RadioGroupSettingSpecifier) spec;
						if ( radio.getFooterText() != null && !radio.getFooterText().isEmpty() ) {
							props.put("footerText", radio.getFooterText());
						}
					}
				}
			} else if ( spec instanceof ToggleSettingSpecifier ) {
				ToggleSettingSpecifier toggle = (ToggleSettingSpecifier) spec;
				props.put("falseValue", toggle.getFalseValue());
				props.put("trueValue", toggle.getTrueValue());
			}
		} else {
			props = createBaseTemplateSpecification(spec, 2);
		}
		if ( callback != null ) {
			callback.accept(spec, props);
		}
		if ( !props.isEmpty() ) {
			result.add(props);
		}
	}

	/**
	 * Create a new template specification with only the base
	 * {@link SettingSpecifier} properties populated.
	 * 
	 * @param spec
	 *        the specifier to create the specification map for
	 * @param capacity
	 *        the estimated number of properties that will be added to the
	 *        specification
	 * @return the new map, never {@literal null}
	 * @since 1.2
	 */
	public static Map<String, Object> createBaseTemplateSpecification(SettingSpecifier spec,
			int capacity) {
		Map<String, Object> props = new LinkedHashMap<>(8);
		props.put("type", spec.getType());
		String title = spec.getTitle();
		if ( title != null && !title.isEmpty() ) {
			props.put("title", title);
		}
		return props;
	}

}

/* ==================================================================
 * IdentifiableConfiguration.java - 21/03/2018 11:29:48 AM
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

package net.solarnetwork.service;

import static net.solarnetwork.settings.SettingSpecifierProvider.settingsForService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import net.solarnetwork.settings.SettingSpecifier;
import net.solarnetwork.settings.SettingSpecifierProvider;
import net.solarnetwork.settings.support.SettingUtils;
import net.solarnetwork.util.ClassUtils;
import net.solarnetwork.util.StringUtils;

/**
 * API for a user-supplied set of configuration to use with some
 * {@link Identifiable} service.
 * 
 * @author matt
 * @version 1.1
 * @since 1.42
 */
public interface IdentifiableConfiguration {

	/**
	 * Get a name for this configuration.
	 * 
	 * <p>
	 * This is expected to be a user-supplied name.
	 * </p>
	 * 
	 * @return a configuration name
	 */
	String getName();

	/**
	 * Get the unique identifier for the service this configuration is
	 * associated with.
	 * 
	 * <p>
	 * This value will correspond to some {@link Identifiable#getUid()} value.
	 * </p>
	 * 
	 * @return the service type identifier
	 */
	String getServiceIdentifier();

	/**
	 * Get a map of properties to pass to the service in order to perform
	 * actions.
	 * 
	 * <p>
	 * It is expected this map would contain user-supplied runtime configuration
	 * such as credentials to use, host name, etc.
	 * </p>
	 * 
	 * @return the runtime properties to pass to the service
	 */
	Map<String, ?> getServiceProperties();

	/**
	 * Mask sensitive information in a set of configurations.
	 * 
	 * @param <T>
	 *        the configuration type
	 * @param configurations
	 *        the configurations
	 * @param serviceSettings
	 *        a service settings cache
	 * @param settingProviderFunction
	 *        a function to apply to settings to perform the masking
	 * @return the masked configurations
	 * @since 1.1
	 */
	static <T extends IdentifiableConfiguration> List<T> maskConfigurations(List<T> configurations,
			ConcurrentMap<String, List<SettingSpecifier>> serviceSettings,
			Function<Void, Iterable<? extends SettingSpecifierProvider>> settingProviderFunction) {
		if ( configurations == null || configurations.isEmpty() ) {
			return Collections.emptyList();
		}
		List<T> result = new ArrayList<>(configurations.size());
		for ( T config : configurations ) {
			T maskedConfig = maskConfiguration(config, serviceSettings, settingProviderFunction);
			if ( maskedConfig != null ) {
				result.add(maskedConfig);
			}
		}
		return result;
	}

	/**
	 * Mask sensitive information in a set of configurations.
	 * 
	 * @param <T>
	 *        the configuration type
	 * @param config
	 *        the configuration
	 * @param serviceSettings
	 *        a service settings cache
	 * @param settingProviderFunction
	 *        a function to apply to settings to perform the masking
	 * @return the masked configuration
	 * @since 1.1
	 */
	@SuppressWarnings("unchecked")
	static <T extends IdentifiableConfiguration> T maskConfiguration(T config,
			ConcurrentMap<String, List<SettingSpecifier>> serviceSettings,
			Function<Void, Iterable<? extends SettingSpecifierProvider>> settingProviderFunction) {
		String id = config.getServiceIdentifier();
		if ( id == null ) {
			return null;
		}
		List<SettingSpecifier> settings = serviceSettings.get(id);
		if ( settings == null ) {
			settings = settingsForService(id, settingProviderFunction.apply(null));
			if ( settings != null ) {
				serviceSettings.put(id, settings);
			}
		}
		if ( settings != null ) {
			Map<String, ?> serviceProps = config.getServiceProperties();
			Map<String, Object> maskedServiceProps = StringUtils.sha256MaskedMap(
					(Map<String, Object>) serviceProps, SettingUtils.secureKeys(settings));
			if ( maskedServiceProps != null ) {
				ClassUtils.setBeanProperties(config,
						Collections.singletonMap("serviceProps", maskedServiceProps), true);
			}
		}
		return config;
	}
}

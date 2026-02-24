/* ==================================================================
 * SettingSpecifierProvider.java - Mar 12, 2012 9:11:50 AM
 *
 * Copyright 2007-2012 SolarNetwork.net Dev Team
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

package net.solarnetwork.settings;

import java.util.List;
import java.util.Locale;
import org.jspecify.annotations.Nullable;
import org.springframework.context.MessageSource;
import net.solarnetwork.settings.support.BasicSettingSpecifierProviderInfo;

/**
 * API for a provider of {@link SettingSpecifier} instances, to publish
 * application-managed settings.
 *
 * @author matt
 * @version 2.2
 */
public interface SettingSpecifierProvider {

	/**
	 * Get a unique, application-wide setting ID.
	 *
	 * <p>
	 * This ID must be unique across all setting providers registered within the
	 * system.
	 * </p>
	 *
	 * @return unique ID
	 */
	String getSettingUid();

	/**
	 * Get a non-localized display name.
	 *
	 * @return non-localized display name
	 */
	@Nullable
	String getDisplayName();

	/**
	 * Get a MessageSource to localize the setting text.
	 *
	 * <p>
	 * This method can return {@code null} if the provider does not have any
	 * localized resources.
	 * </p>
	 *
	 * @return the MessageSource, or {@code null}
	 */
	@Nullable
	MessageSource getMessageSource();

	/**
	 * Get a list of {@link SettingSpecifier} instances.
	 *
	 * @return list of {@link SettingSpecifier}
	 */
	List<SettingSpecifier> getSettingSpecifiers();

	/**
	 * Get a template list of {@link SettingSpecifier} instances.
	 *
	 * <p>
	 * This method differs from {@link #getSettingSpecifiers()} in that the
	 * specifiers for dynamic nested group collections are also included, so
	 * that the returned list serves as a "template" definition for configuring
	 * this provider.
	 * </p>
	 *
	 * <p>
	 * This default method simply returns {@link #getSettingSpecifiers()};
	 * extending implementations can override this to implement more specific
	 * behavior.
	 * </p>
	 *
	 * @return a "template" list of {@link SettingSpecifier}
	 * @since 2.1
	 */
	default List<SettingSpecifier> templateSettingSpecifiers() {
		return getSettingSpecifiers();
	}

	/**
	 * Get the settings for a specific service.
	 *
	 * @param id
	 *        the ID of the service to get the settings for
	 * @param providers
	 *        the available services
	 * @return the settings, or {@code null} if not available
	 * @since 1.1
	 */
	static @Nullable List<SettingSpecifier> settingsForService(String id,
			@Nullable Iterable<? extends SettingSpecifierProvider> providers) {
		if ( providers == null ) {
			return null;
		}
		for ( SettingSpecifierProvider provider : providers ) {
			if ( id.equals(provider.getSettingUid()) ) {
				return provider.getSettingSpecifiers();
			}
		}
		return null;
	}

	/**
	 * Get a localized info object for this provider.
	 *
	 * @param locale
	 *        the desired locale
	 * @param uid
	 *        the unique ID
	 * @param groupUid
	 *        the group ID
	 * @return the provider info, never {@code null}
	 * @throws IllegalArgumentException
	 *         if any argument except {@code groupUid} is {@code null}
	 * @since 2.2
	 */
	default SettingSpecifierProviderInfo localizedInfo(@Nullable Locale locale, String uid,
			@Nullable String groupUid) {
		final String settingUid = getSettingUid();
		String displayName = getDisplayName();
		final MessageSource messageSource = getMessageSource();
		if ( messageSource != null ) {
			displayName = messageSource.getMessage("title", null, displayName, locale);
		}
		return new BasicSettingSpecifierProviderInfo(settingUid, displayName, uid, groupUid);
	}

	/**
	 * Unwrap this provider as another type, if possible.
	 *
	 * @param <T>
	 *        the type to unwrap to
	 * @param type
	 *        the class to unwrap as
	 * @return the given type, or {@code null} if the provider not compatible
	 *         with {@code type}
	 * @since 2.2
	 */
	@SuppressWarnings("unchecked")
	default <T> @Nullable T unwrap(Class<T> type) {
		if ( type.isAssignableFrom(getClass()) ) {
			return (T) this;
		}
		return null;
	}

}

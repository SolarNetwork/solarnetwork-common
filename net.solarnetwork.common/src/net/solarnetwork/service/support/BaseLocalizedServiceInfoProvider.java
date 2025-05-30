/* ==================================================================
 * BaseLocalizedServiceInfoProvider.java - 11/04/2018 4:57:28 PM
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

package net.solarnetwork.service.support;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import net.solarnetwork.domain.BasicLocalizedServiceInfo;
import net.solarnetwork.domain.BasicUnique;
import net.solarnetwork.domain.LocalizedServiceInfo;
import net.solarnetwork.service.LocalizedServiceInfoProvider;

/**
 * A base implementation of {@link LocalizedServiceInfoProvider} that also
 * implements {@link net.solarnetwork.domain.Identity}.
 *
 * @author matt
 * @version 2.0
 * @since 1.43
 */
public abstract class BaseLocalizedServiceInfoProvider extends BasicUnique<String>
		implements LocalizedServiceInfoProvider {

	/** A class-level logger. */
	protected final Logger log = LoggerFactory.getLogger(getClass());

	private MessageSource messageSource;

	/**
	 * Constructor.
	 *
	 * @param id
	 *        the identity of this provider
	 */
	public BaseLocalizedServiceInfoProvider(String id) {
		super(id);
	}

	@Override
	public LocalizedServiceInfo getLocalizedServiceInfo(Locale locale) {
		locale = (locale != null ? locale : Locale.getDefault());
		String id = (getId() != null ? getId().toString() : "");
		String title = null;
		String desc = null;
		Map<String, String> msgs = Collections.emptyMap();
		MessageSource ms = getMessageSource();
		if ( ms != null ) {
			title = ms.getMessage("title", null, null, locale);
			desc = ms.getMessage("desc", null, null, locale);
			msgs = resolveInfoMessages(locale);
		}
		return new BasicLocalizedServiceInfo(id, locale, title, desc, msgs);
	}

	/**
	 * Resolve the localized info messages.
	 *
	 * <p>
	 * This method returns an empty map. Extending classes can override.
	 * </p>
	 *
	 * @param locale
	 *        the locale to resolve mesages for
	 * @return the map, never {@literal null}
	 */
	protected Map<String, String> resolveInfoMessages(Locale locale) {
		return Collections.emptyMap();
	}

	/**
	 * Set a message source to resolve messages with.
	 *
	 * @return the message source
	 */
	public MessageSource getMessageSource() {
		return messageSource;
	}

	/**
	 * Get the message source to resolve messages with.
	 *
	 * @param messageSource
	 *        the message source
	 */
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

}

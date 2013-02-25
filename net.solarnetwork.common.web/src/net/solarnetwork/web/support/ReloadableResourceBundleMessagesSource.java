/* ==================================================================
 * ReloadableResourceBundleMessagesSource.java - Jun 18, 2011 12:14:11 PM
 * 
 * Copyright 2007-2011 SolarNetwork.net Dev Team
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
 * $Id$
 * ==================================================================
 */

package net.solarnetwork.web.support;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Set;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

/**
 * Extension of {@link ReloadableResourceBundleMessagesSource} to allow finding
 * all keys for all messages.
 * 
 * <p>
 * Adapted from {@code magoffin.matt.xweb.util.ResourceBundleMessagesSource}.
 * </p>
 * 
 * @author matt
 * @version $Revision$
 */
public class ReloadableResourceBundleMessagesSource extends ReloadableResourceBundleMessageSource
		implements MessageSource, MessagesSource {

	/**
	 * Private copy of basenames, as parent class does not provide a way to
	 * access this.
	 */
	private String[] basenames;
	private MessagesSource parent;

	@Override
	public void setBasenames(String... basenames) {
		super.setBasenames(basenames);
		this.basenames = basenames;
	}

	@Override
	public void registerMessageResource(String resource) {
		String[] newBasenames = new String[basenames.length + 1];
		System.arraycopy(basenames, 0, newBasenames, 0, basenames.length);
		newBasenames[newBasenames.length - 1] = resource;
		super.setBasenames(newBasenames);
		this.basenames = newBasenames;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Enumeration<String> getKeys(Locale locale) {
		PropertiesHolder propHolder = super.getMergedProperties(locale);
		return Collections.enumeration((Set) propHolder.getProperties().keySet());
	}

	@Override
	protected String getMessageInternal(String code, Object[] args, Locale locale) {
		String msg = super.getMessageInternal(code, args, locale);
		if ( msg != null ) {
			return msg;
		}
		if ( parent != null ) {
			return parent.getMessage(code, args, locale);
		}
		return null;
	}

	/**
	 * @return the parent
	 */
	public MessagesSource getParent() {
		return parent;
	}

	/**
	 * @param parent
	 *        the parent to set
	 */
	public void setParent(MessagesSource parent) {
		this.parent = parent;
	}

}

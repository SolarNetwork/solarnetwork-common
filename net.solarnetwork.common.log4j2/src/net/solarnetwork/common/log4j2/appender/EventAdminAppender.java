/* ==================================================================
 * EventAdminAppender.java - 20/10/2022 9:06:59 am
 * 
 * Copyright 2022 SolarNetwork.net Dev Team
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

package net.solarnetwork.common.log4j2.appender;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.impl.ThrowableProxy;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

/**
 * Publish log events to an {@link EventAdmin}.
 * 
 * @author matt
 * @version 1.0
 */
@Plugin(name = "EventAdminAppender", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE)
public class EventAdminAppender extends AbstractAppender {

	/** The EventAdmin event topic for log events. */
	public static final String LOG_EVENT_TOPIC = "net/solarnetwork/Log";

	/**
	 * The log Event property name for a millisecond timestamp, as a
	 * {@code Long}.
	 */
	public static final String TIMESTAMP_EVENT_PROP = "ts";

	/** The log Event property name for the message, as a {@code String}. */
	public static final String MESSAGE_EVENT_PROP = "msg";

	/** The log Event property name for the logger name, as a {@code String}. */
	public static final String NAME_EVENT_PROP = "name";

	/**
	 * The log Event property name for the level value, as an {@code Integer}.
	 */
	public static final String PRIORITY_EVENT_PROP = "priority";

	/** The log Event property name for the level name, as a {@code String}. */
	public static final String LEVEL_EVENT_PROP = "level";

	/**
	 * The log Event property name for an exception message, as a
	 * {@code String}.
	 */
	public static final String EXCEPTION_MESSAGE_EVENT_PROP = "exMsg";

	/**
	 * The log Event property name for an exception stacktrace, as a
	 * {@code String[]} of at most 20 elements.
	 */
	public static final String EXCEPTION_STACKTRACE_EVENT_PROP = "exSt";

	/**
	 * Factory creator.
	 * 
	 * @param name
	 *        the appender name
	 * @param ignoreExceptions
	 *        {@literal true} to ignore appender exceptions
	 * @param layout
	 *        the layout
	 * @param filter
	 *        the filter
	 * @return
	 */
	@PluginFactory
	public static EventAdminAppender createAppender(@PluginAttribute(NAME_EVENT_PROP) String name,
			@PluginAttribute("ignoreExceptions") boolean ignoreExceptions,
			@PluginElement("Layout") Layout<? extends Serializable> layout,
			@PluginElement("Filters") Filter filter) {
		return new EventAdminAppender(name, filter, layout, ignoreExceptions);
	}

	private volatile static BundleContext ctx;
	private volatile static ServiceReference<EventAdmin> eventAdminRef;

	/**
	 * Constructor.
	 * 
	 * @param name
	 *        the name
	 * @param filter
	 *        the filter
	 */
	protected EventAdminAppender(String name, Filter filter, Layout<? extends Serializable> layout,
			boolean ignoreExceptions) {
		super(name, filter, null, ignoreExceptions, Property.EMPTY_ARRAY);
	}

	@Override
	public void append(LogEvent event) {
		if ( ctx == null ) {
			Bundle bundle = FrameworkUtil.getBundle(AbstractAppender.class);
			if ( bundle == null ) {
				return;
			}
			ctx = bundle.getBundleContext();
			if ( ctx == null ) {
				return;
			}
		}

		if ( eventAdminRef == null ) {
			try {
				eventAdminRef = ctx.getServiceReference(EventAdmin.class);
			} catch ( IllegalStateException e ) {
				// context no longer valid; ignore
			}
			if ( eventAdminRef == null ) {
				return;
			}
		}

		EventAdmin ea = null;
		try {
			ea = ctx.getService(eventAdminRef);
		} catch ( IllegalStateException | IllegalArgumentException e ) {
			// no longer valid
		}
		if ( ea == null ) {
			return;
		}

		Map<String, Object> evtData = new LinkedHashMap<>(8);
		evtData.put(TIMESTAMP_EVENT_PROP, event.getTimeMillis());
		evtData.put(LEVEL_EVENT_PROP, event.getLevel().name());
		evtData.put(PRIORITY_EVENT_PROP, event.getLevel().intLevel());
		evtData.put(NAME_EVENT_PROP, event.getLoggerName());
		evtData.put(MESSAGE_EVENT_PROP, event.getMessage().getFormattedMessage());
		if ( event.getThrownProxy() != null ) {
			ThrowableProxy t = event.getThrownProxy();
			evtData.put(EXCEPTION_MESSAGE_EVENT_PROP, t.getMessage());
			StackTraceElement[] sts = t.getStackTrace();
			if ( sts != null ) {
				String[] stStrings = new String[Math.min(20, sts.length)];
				for ( int i = 0; i < stStrings.length; i++ ) {
					stStrings[i] = sts[i].toString();
				}
				evtData.put(EXCEPTION_STACKTRACE_EVENT_PROP, stStrings);
			}
		}
		Event evt = new Event(LOG_EVENT_TOPIC, evtData);
		ea.postEvent(evt);
	}

}

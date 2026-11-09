/* ==================================================================
 * SimpleEventAdmin.java - 13/06/2017 10:17:40 PM
 *
 * Copyright 2017 SolarNetwork.net Dev Team
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

package net.solarnetwork.common.osgi.event;

import static net.solarnetwork.util.ObjectUtils.requireNonNullArgument;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executor;
import org.jspecify.annotations.Nullable;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

/**
 * Adapts the OSGi {@link EventAdmin} API to a non-OSGi, Spring based
 * application context environment.
 *
 * <p>
 * Handler topics are Ant-style patterns, matched by {@link AntPathMatcher}, not
 * OSGi topic wildcards: for example {@code a/*} matches {@code a/b} but not
 * {@code a/b/c}, which {@code a/**} matches. A handler receives each event
 * once, even if more than one of its topics match. An exception thrown by a
 * handler is logged, and does not stop delivery to other handlers.
 * </p>
 *
 * @author matt
 * @version 1.2
 * @since 1.36
 */
public class SimpleEventAdmin
		implements EventAdmin, DestructionAwareBeanPostProcessor, EventHandlerRegistrar {

	private static final Logger log = LoggerFactory.getLogger(SimpleEventAdmin.class);

	private final PathMatcher pathMatcher = new AntPathMatcher();
	private final ConcurrentMap<String, Set<EventHandler>> eventHandlers = new ConcurrentHashMap<>();
	private final Executor executor;

	/**
	 * Constructor.
	 *
	 * @param executor
	 *        the executor to handle asynchronous events with
	 * @throws IllegalArgumentException
	 *         if any argument is {@code null}
	 */
	public SimpleEventAdmin(Executor executor) {
		super();
		this.executor = requireNonNullArgument(executor, "executor");
	}

	@Override
	public @Nullable Object postProcessBeforeInitialization(Object bean, String beanName)
			throws BeansException {
		return bean;
	}

	@Override
	public @Nullable Object postProcessAfterInitialization(Object bean, String beanName)
			throws BeansException {
		return bean;
	}

	@Override
	public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
		if ( bean instanceof EventHandler ) {
			deregisterEventHandler((EventHandler) bean);
		}
	}

	@Override
	public boolean requiresDestruction(Object bean) {
		return (bean instanceof EventHandler);
	}

	@Override
	public void registerEventHandler(EventHandler handler, String... topics) {
		for ( String topic : topics ) {
			eventHandlers.computeIfAbsent(topic, k -> new CopyOnWriteArraySet<>()).add(handler);
		}
	}

	@Override
	public void deregisterEventHandler(EventHandler handler) {
		for ( Set<EventHandler> handlers : eventHandlers.values() ) {
			handlers.remove(handler);
		}
	}

	@Override
	public void postEvent(final Event event) {
		for ( final EventHandler handler : matchingHandlers(event) ) {
			executor.execute(() -> handleEvent(handler, event));
		}
	}

	@Override
	public void sendEvent(Event event) {
		for ( EventHandler handler : matchingHandlers(event) ) {
			handleEvent(handler, event);
		}
	}

	/**
	 * Get the handlers with a topic matching an event.
	 *
	 * @param event
	 *        the event
	 * @return the matching handlers, without duplicates
	 */
	private Set<EventHandler> matchingHandlers(Event event) {
		final String topic = event.getTopic();
		final Set<EventHandler> result = new LinkedHashSet<>(8);
		for ( Map.Entry<String, Set<EventHandler>> me : eventHandlers.entrySet() ) {
			if ( pathMatcher.match(me.getKey(), topic) ) {
				result.addAll(me.getValue());
			}
		}
		return result;
	}

	private static void handleEvent(EventHandler handler, Event event) {
		try {
			handler.handleEvent(event);
		} catch ( RuntimeException e ) {
			// like OSGi EventAdmin, log the exception and continue delivery to other handlers
			log.error("Event handler {} threw exception handling event {}", handler, event.getTopic(),
					e);
		}
	}

}

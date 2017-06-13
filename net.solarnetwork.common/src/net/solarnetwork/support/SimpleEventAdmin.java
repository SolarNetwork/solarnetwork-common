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

package net.solarnetwork.support;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executor;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventHandler;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

/**
 * Adapts the OSGi {@link EventAdmin} API to a non-OSGi, Spring based
 * application context environment.
 * 
 * @author matt
 * @version 1.0
 * @since 1.36
 */
public class SimpleEventAdmin
		implements EventAdmin, DestructionAwareBeanPostProcessor, EventHandlerRegistrar {

	private final PathMatcher pathMatcher = new AntPathMatcher();
	private final ConcurrentMap<String, Set<EventHandler>> eventHandlers = new ConcurrentHashMap<String, Set<EventHandler>>();
	private final Executor executor;

	/**
	 * Constructor.
	 * 
	 * @param executor
	 *        the executor to handle asynchronous events with
	 */
	public SimpleEventAdmin(Executor executor) {
		super();
		this.executor = executor;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
		if ( bean instanceof EventHandler ) {
			deregisterEventHandler((EventHandler) bean);
		}
	}

	@Override
	public void registerEventHandler(EventHandler handler, String... topics) {
		for ( String topic : topics ) {
			// note: in 1.8, computeIfAbsent would be better; for now assuming this is called during
			// application initialization (by Spring) and from a single thread only
			Set<EventHandler> handlers = eventHandlers.get(topic);
			if ( handlers == null ) {
				handlers = new CopyOnWriteArraySet<EventHandler>();
				eventHandlers.put(topic, handlers);
			}
			handlers.add(handler);
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
		for ( Map.Entry<String, Set<EventHandler>> me : eventHandlers.entrySet() ) {
			String topic = event.getTopic();
			if ( pathMatcher.match(me.getKey(), topic) ) {
				for ( final EventHandler handler : me.getValue() ) {
					executor.execute(new Runnable() {

						@Override
						public void run() {
							handler.handleEvent(event);
						}
					});
				}
			}
		}
	}

	@Override
	public void sendEvent(Event event) {
		for ( Map.Entry<String, Set<EventHandler>> me : eventHandlers.entrySet() ) {
			String topic = event.getTopic();
			if ( pathMatcher.match(me.getKey(), topic) ) {
				for ( EventHandler handler : me.getValue() ) {
					handler.handleEvent(event);
				}
			}
		}
	}
}

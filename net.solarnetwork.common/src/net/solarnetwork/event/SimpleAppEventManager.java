/* ==================================================================
 * SimpleEventHandlerRegistrar.java - 9/11/2021 10:50:05 AM
 * 
 * Copyright 2021 SolarNetwork.net Dev Team
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

package net.solarnetwork.event;

import static net.solarnetwork.util.ObjectUtils.requireNonNullArgument;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

/**
 * Simple implementation of {@link AppEventHandlerRegistrar}.
 * 
 * @author matt
 * @version 1.0
 * @since 2.0
 */
public class SimpleAppEventManager implements AppEventHandlerRegistrar, AppEventPublisher {

	private final PathMatcher pathMatcher;
	private final ConcurrentMap<String, Set<AppEventHandler>> eventHandlers;
	private final Executor executor;

	/**
	 * Constructor.
	 * 
	 * <p>
	 * An Ant-style path matcher and work-stealing pool will be used.
	 * </p>
	 */
	public SimpleAppEventManager() {
		this(new AntPathMatcher(), Executors.newWorkStealingPool());
	}

	/**
	 * Constructor.
	 * 
	 * <p>
	 * An Ant-style path matcher will be used.
	 * </p>
	 * 
	 * @param executor
	 *        the executor
	 * @throws IllegalArgumentException
	 *         if any argument is {@literal null}
	 */
	public SimpleAppEventManager(Executor executor) {
		this(new AntPathMatcher(), executor);
	}

	/**
	 * Constructor.
	 * 
	 * @param pathMatcher
	 *        the path matcher to use for topic matching
	 * @param executor
	 *        the executor
	 * @throws IllegalArgumentException
	 *         if any argument is {@literal null}
	 */
	public SimpleAppEventManager(PathMatcher pathMatcher, Executor executor) {
		this(pathMatcher, executor, new ConcurrentHashMap<>(32, 0.9f, 4));
	}

	/**
	 * Constructor.
	 * 
	 * @param pathMatcher
	 *        the path matcher to use for topic matching
	 * @param executor
	 *        the executor
	 * @param handlerMap
	 *        the map to use for handler registration
	 * @throws IllegalArgumentException
	 *         if any argument is {@literal null}
	 */
	public SimpleAppEventManager(PathMatcher pathMatcher, Executor executor,
			ConcurrentMap<String, Set<AppEventHandler>> handlerMap) {
		super();
		this.pathMatcher = requireNonNullArgument(pathMatcher, "pathMatcher");
		this.executor = requireNonNullArgument(executor, "executor");
		this.eventHandlers = requireNonNullArgument(handlerMap, "handlerMap");
	}

	@Override
	public void registerEventHandler(AppEventHandler handler, String... topics) {
		if ( handler == null ) {
			return;
		}
		for ( String topic : topics ) {
			eventHandlers.computeIfAbsent(topic, k -> new CopyOnWriteArraySet<>()).add(handler);
		}
	}

	@Override
	public void deregisterEventHandler(AppEventHandler handler) {
		for ( Set<AppEventHandler> handlers : eventHandlers.values() ) {
			handlers.remove(handler);
		}
	}

	@Override
	public void postEvent(AppEvent event) {
		for ( Entry<String, Set<AppEventHandler>> me : eventHandlers.entrySet() ) {
			String topic = event.getTopic();
			if ( pathMatcher.match(me.getKey(), topic) ) {
				for ( final AppEventHandler handler : me.getValue() ) {
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

}

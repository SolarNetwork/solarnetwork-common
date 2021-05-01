/* ==================================================================
 * BasicMqttTopicAliases.java - 1/05/2021 4:53:24 PM
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

package net.solarnetwork.common.mqtt;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

/**
 * Basic implementation of {@link MqttTopicAliases}.
 * 
 * @author matt
 * @version 1.0
 * @since 2.2
 */
public class BasicMqttTopicAliases implements MqttTopicAliases {

	private int maximumAliasCount;
	private final ConcurrentMap<String, Integer> topicAliases;
	private final ConcurrentMap<Integer, String> aliasedTopics;

	/**
	 * Constructor.
	 * 
	 * @param maximumAliasCount
	 *        the maximum alias count
	 */
	public BasicMqttTopicAliases(int maximumAliasCount) {
		this(maximumAliasCount, new ConcurrentHashMap<>(maximumAliasCount, 0.9f, 2),
				new ConcurrentHashMap<>(maximumAliasCount, 0.9f, 2));
	}

	/**
	 * Constructor.
	 * 
	 * @param maximumAliasCount
	 *        the maximum alias count
	 * @param topicAliases
	 *        the map to use for topic aliases
	 * @param aliasedTopics
	 *        the map to use for aliased topics
	 * @throws IllegalArgumentException
	 *         if any argument is {@literal null}
	 */
	public BasicMqttTopicAliases(int maximumAliasCount, ConcurrentMap<String, Integer> topicAliases,
			ConcurrentMap<Integer, String> aliasedTopics) {
		super();
		this.maximumAliasCount = maximumAliasCount;
		if ( topicAliases == null ) {
			throw new IllegalArgumentException("The topicAliases parameter must not be null.");
		}
		this.topicAliases = topicAliases;
		if ( aliasedTopics == null ) {
			throw new IllegalArgumentException("The aliasedTopics parameter must not be null.");
		}
		this.aliasedTopics = aliasedTopics;
	}

	@Override
	public int getMaximumAliasCount() {
		return maximumAliasCount;
	}

	@Override
	public void setMaximumAliasCount(int maximumAliasCount) {
		this.maximumAliasCount = maximumAliasCount;
	}

	@Override
	public void clear() {
		synchronized ( aliasedTopics ) {
			topicAliases.clear();
			aliasedTopics.clear();
		}
	}

	@Override
	public String topicAlias(String topic, Consumer<Integer> aliasConsumer) {
		Integer topicAlias = topicAliases.get(topic);
		final int maxCount = this.maximumAliasCount;
		if ( topicAlias == null && maxCount > 0 ) {
			// assign topic alias now
			final String topicToAlias = topic;
			synchronized ( aliasedTopics ) {
				for ( int i = 1; i < maxCount; i++ ) {
					String aliasedTopic = aliasedTopics.computeIfAbsent(i, k -> topicToAlias);
					if ( aliasedTopic.equals(topic) ) {
						topicAlias = i;
						aliasedTopics.put(topicAlias, topic);
						topicAliases.put(topic, topicAlias);
					}
				}
			}
		} else if ( topicAlias != null ) {
			// set existing aliased topic to ""
			topic = "";
		}
		if ( topicAlias != null && aliasConsumer != null ) {
			aliasConsumer.accept(topicAlias);
		}
		return topic;
	}

	@Override
	public String aliasedTopic(String topic, Integer alias) {
		if ( topic == null || topic.isEmpty() ) {
			return aliasedTopics.get(alias);
		}
		synchronized ( aliasedTopics ) {
			aliasedTopics.put(alias, topic);
			topicAliases.put(topic, alias);
		}
		return topic;
	}

}

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
 * @version 1.1
 * @since 2.2
 */
public class BasicMqttTopicAliases implements MqttTopicAliases {

	private int maximumAliasCount;
	private final ConcurrentMap<String, Integer> topicAliases;
	private final ConcurrentMap<Integer, String> aliasedTopics;
	private final ConcurrentMap<Integer, MqttProperties> topicAliasProperties;

	/**
	 * Constructor.
	 * 
	 * @param maximumAliasCount
	 *        the maximum alias count
	 */
	public BasicMqttTopicAliases(int maximumAliasCount) {
		this(maximumAliasCount, new ConcurrentHashMap<>(maximumAliasCount, 0.9f, 2),
				new ConcurrentHashMap<>(maximumAliasCount, 0.9f, 2),
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
	 * @param topicAliasProperties
	 *        the map to use for alias properties
	 * @throws IllegalArgumentException
	 *         if any argument is {@literal null}
	 */
	public BasicMqttTopicAliases(int maximumAliasCount, ConcurrentMap<String, Integer> topicAliases,
			ConcurrentMap<Integer, String> aliasedTopics,
			ConcurrentMap<Integer, MqttProperties> topicAliasProperties) {
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
		if ( topicAliasProperties == null ) {
			throw new IllegalArgumentException("The topicAliasProperties parameter must not be null.");
		}
		this.topicAliasProperties = topicAliasProperties;
	}

	@Override
	public int getMaximumAliasCount() {
		return maximumAliasCount;
	}

	@Override
	public void setMaximumAliasCount(int maximumAliasCount) {
		synchronized ( aliasedTopics ) {
			this.maximumAliasCount = maximumAliasCount;
			if ( maximumAliasCount < 1 || topicAliases.size() > maximumAliasCount ) {
				// clear any existing aliases if we have reset
				clear();
			}
		}
	}

	@Override
	public void clear() {
		synchronized ( aliasedTopics ) {
			topicAliases.clear();
			aliasedTopics.clear();
			topicAliasProperties.clear();
		}
	}

	@Override
	public String topicAlias(String topic, Consumer<Integer> aliasConsumer) {
		Integer topicAlias = null;
		synchronized ( aliasedTopics ) {
			final int maxCount = getMaximumAliasCount();
			if ( maxCount < 1 ) {
				return topic;
			}
			topicAlias = topicAliases.get(topic);
			if ( topicAlias == null ) {
				// assign topic alias now
				final String topicToAlias = topic;
				for ( int i = 1; i <= maxCount; i++ ) {
					String aliasedTopic = aliasedTopics.computeIfAbsent(i, k -> topicToAlias);
					if ( aliasedTopic.equals(topic) ) {
						topicAlias = i;
						aliasedTopics.put(topicAlias, topic);
						// we initially assign the negative of the topic alias here, until it is confirmed
						topicAliases.put(topic, -topicAlias);
						break;
					}
				}
			} else if ( topicAlias.intValue() < 0 ) {
				// alias has not been confirmed yet, so must return given topic and not provide alias yet
				topicAlias = -topicAlias.intValue();
			} else {
				// alias has been confirmed, so set resulting topic to ""
				topic = "";
			}
		}
		if ( topicAlias != null && aliasConsumer != null ) {
			aliasConsumer.accept(topicAlias);
		}
		return topic;
	}

	@Override
	public boolean confirmTopicAlias(String topic) {
		// sync on aliasedTopics like other methods are doing
		synchronized ( aliasedTopics ) {
			Integer alias = topicAliases.computeIfPresent(topic, (k, v) -> {
				int a = v.intValue();
				if ( a < 0 ) {
					return -a;
				}
				return v;
			});
			return alias != null;
		}
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

	@Override
	public MqttProperties propertiesForAliasedTopic(Integer alias) {
		return topicAliasProperties.computeIfAbsent(alias, a -> {
			return (aliasedTopics.containsKey(a)
					? SingletonProperties.property(MqttPropertyType.TOPIC_ALIAS, alias)
					: null);
		});
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BasicMqttTopicAliases{max=");
		builder.append(maximumAliasCount);
		builder.append(", aliases=");
		builder.append(topicAliases);
		builder.append("}");
		return builder.toString();
	}

}

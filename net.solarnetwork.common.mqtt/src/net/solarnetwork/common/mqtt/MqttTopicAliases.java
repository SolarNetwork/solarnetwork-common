/* ==================================================================
 * MqttTopicAliases.java - 1/05/2021 4:40:15 PM
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

import java.util.function.Consumer;

/**
 * API for managing MQTT 5+ topic aliaes.
 * 
 * <p>
 * Implementations of this API are meant to be thread-safe.
 * </p>
 * 
 * @author matt
 * @version 1.0
 * @since 2.2
 */
public interface MqttTopicAliases {

	/**
	 * Get the maximum number of topic aliases supported by this instance.
	 * 
	 * @return the count, zero or more
	 */
	int getMaximumAliasCount();

	/**
	 * Set the maximum number of topic aliases supported by this instance.
	 * 
	 * <p>
	 * If {@code maximumAliasCount} is smaller than the number of currently
	 * aliased topics, the aliases should be cleared, as if {@link #clear()}
	 * were also called.
	 * </p>
	 * 
	 * @param maximumAliasCount
	 *        the count, zero or more
	 */
	void setMaximumAliasCount(int maximumAliasCount);

	/**
	 * Remove any existing topic aliases.
	 */
	void clear();

	/**
	 * Get a topic alias.
	 * 
	 * @param topic
	 *        the topic to get an alias for
	 * @param aliasConsumer
	 *        the consumer of the alias; if no alias is available or allowed,
	 *        the consumer will not be called
	 * @return the resulting topic to use; will be an empty string if an alias
	 *         already exists for the given {@code topic} or {@code topic}
	 *         otherwise
	 */
	String topicAlias(String topic, Consumer<Integer> aliasConsumer);

	/**
	 * Get the topic for an alias.
	 * 
	 * @param topic
	 *        the given topic
	 * @param alias
	 *        the given alias
	 * @return the topic for the given alias
	 */
	String aliasedTopic(String topic, Integer alias);

	/**
	 * Get a {@link MqttProperties} that includes a topic alias property.
	 * 
	 * @param alias
	 *        the topic alias
	 * @return the properties, or {@literal null} if {@code alias} is not
	 *         already aliased
	 */
	MqttProperties propertiesForAliasedTopic(Integer alias);

	/**
	 * Convenience method to create a new message with a topic alias.
	 * 
	 * @param topic
	 *        the topic
	 * @param retained
	 *        the retained flag
	 * @param qosLevel
	 *        the QOS level
	 * @param payload
	 *        the payload
	 * @return the new message, never {@literal null}
	 */
	default MqttMessage mssageWithTopicAlias(String topic, boolean retained, MqttQos qosLevel,
			byte[] payload) {
		// using array so can mutate within callback
		Integer[] alias = new Integer[] { null };
		topic = topicAlias(topic, a -> {
			alias[0] = a;
		});
		MqttProperties props = null;
		if ( alias[0] != null ) {
			props = propertiesForAliasedTopic(alias[0]);
		}
		return new BasicMqttMessage(topic, retained, qosLevel, payload, props);
	}

}

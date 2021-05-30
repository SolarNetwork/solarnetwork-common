/* ==================================================================
 * NoOpMqttTopicAliases.java - 2/05/2021 3:31:08 PM
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
 * Implementation of {@link MqttTopicAliases} that does not perform any
 * aliasing.
 * 
 * <p>
 * This is designed to be used when MQTT less than v5 is being used.
 * </p>
 * 
 * @author matt
 * @version 1.1
 * @since 2.2
 */
public final class NoOpMqttTopicAliases implements MqttTopicAliases {

	@Override
	public int getMaximumAliasCount() {
		return 0;
	}

	@Override
	public void setMaximumAliasCount(int maximumAliasCount) {
		// ignore
	}

	@Override
	public void clear() {
		// nothing to do
	}

	@Override
	public String topicAlias(String topic, Consumer<Integer> aliasConsumer) {
		return topic;
	}

	@Override
	public boolean confirmTopicAlias(String topic) {
		return false;
	}

	@Override
	public String aliasedTopic(String topic, Integer alias) {
		return topic;
	}

	@Override
	public MqttProperties propertiesForAliasedTopic(Integer alias) {
		return null;
	}

}

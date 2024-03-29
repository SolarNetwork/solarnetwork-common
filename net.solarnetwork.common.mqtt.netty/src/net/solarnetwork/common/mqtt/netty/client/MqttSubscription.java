/**
 * Copyright 2019 SolarNetwork.net Dev Team
 * Copyright © 2016-2019 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.solarnetwork.common.mqtt.netty.client;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.solarnetwork.common.mqtt.MqttMessageHandler;

final class MqttSubscription {

	/** Pattern to match a MQTT 5-style shared subscription topic prefix. */
	public static final Pattern SHARE_SUBSCRIPTION_PREFIX = Pattern.compile("(\\$share/[^/]+/).*",
			Pattern.CASE_INSENSITIVE);

	private final String topic;
	private final Pattern topicRegex;
	private final MqttMessageHandler handler;

	private final boolean once;

	private boolean called;

	MqttSubscription(String topic, MqttMessageHandler handler, boolean once) {
		if ( topic == null ) {
			throw new NullPointerException("topic");
		}
		if ( handler == null ) {
			throw new NullPointerException("handler");
		}
		this.topic = topic;
		this.handler = handler;
		this.once = once;
		Matcher shareMatch = SHARE_SUBSCRIPTION_PREFIX.matcher(topic);
		if ( shareMatch.matches() ) {
			topic = topic.substring(shareMatch.end(1));
		}
		this.topicRegex = Pattern.compile(topic.replace("+", "[^/]+").replace("#", ".+") + "$");
	}

	String getTopic() {
		return topic;
	}

	public MqttMessageHandler getHandler() {
		return handler;
	}

	boolean isOnce() {
		return once;
	}

	boolean isCalled() {
		return called;
	}

	boolean matches(String topic) {
		return this.topicRegex.matcher(topic).matches();
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o )
			return true;
		if ( o == null || getClass() != o.getClass() )
			return false;

		MqttSubscription that = (MqttSubscription) o;

		return once == that.once && topic.equals(that.topic) && handler.equals(that.handler);
	}

	@Override
	public int hashCode() {
		int result = topic.hashCode();
		result = 31 * result + handler.hashCode();
		result = 31 * result + (once ? 1 : 0);
		return result;
	}

	void setCalled(boolean called) {
		this.called = called;
	}
}

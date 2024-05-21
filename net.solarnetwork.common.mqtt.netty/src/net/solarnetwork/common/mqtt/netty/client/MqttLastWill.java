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

import io.netty.handler.codec.mqtt.MqttQoS;

/**
 * A "last will" message.
 *
 * @author matt
 * @version 1.0
 */
public final class MqttLastWill {

	private final String topic;
	private final String message;
	private final boolean retain;
	private final MqttQoS qos;

	/**
	 * Constructor.
	 *
	 * @param topic
	 *        the message topic
	 * @param message
	 *        the message
	 * @param retain
	 *        the retain flag
	 * @param qos
	 *        the QoS level
	 */
	public MqttLastWill(String topic, String message, boolean retain, MqttQoS qos) {
		if ( topic == null ) {
			throw new NullPointerException("topic");
		}
		if ( message == null ) {
			throw new NullPointerException("message");
		}
		if ( qos == null ) {
			throw new NullPointerException("qos");
		}
		this.topic = topic;
		this.message = message;
		this.retain = retain;
		this.qos = qos;
	}

	/**
	 * Get the message topic.
	 *
	 * @return the message topic
	 */
	public String getTopic() {
		return topic;
	}

	/**
	 * Get the message.
	 *
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Get the retain flag.
	 *
	 * @return {@code true} to set the "retained" message flag
	 */
	public boolean isRetain() {
		return retain;
	}

	/**
	 * Get the quality of service level.
	 *
	 * @return the quality of service level
	 */
	public MqttQoS getQos() {
		return qos;
	}

	/**
	 * Get a builder.
	 *
	 * @return the builder
	 */
	public static MqttLastWill.Builder builder() {
		return new MqttLastWill.Builder();
	}

	/**
	 * A last will builder.
	 */
	public static final class Builder {

		private String topic;
		private String message;
		private boolean retain;
		private MqttQoS qos;

		/**
		 * Constructor.
		 */
		public Builder() {
			super();
		}

		/**
		 * Get the message topic.
		 *
		 * @return the topic
		 */
		public String getTopic() {
			return topic;
		}

		/**
		 * Set the message topic.
		 *
		 * @param topic
		 *        the topic to set
		 * @return this object
		 */
		public Builder setTopic(String topic) {
			if ( topic == null ) {
				throw new NullPointerException("topic");
			}
			this.topic = topic;
			return this;
		}

		/**
		 * Get the message.
		 *
		 * @return the message
		 */
		public String getMessage() {
			return message;
		}

		/**
		 * Set the message.
		 *
		 * @param message
		 *        the message to set
		 * @return this object
		 */
		public Builder setMessage(String message) {
			if ( message == null ) {
				throw new NullPointerException("message");
			}
			this.message = message;
			return this;
		}

		/**
		 * Get the retain flag.
		 *
		 * @return the retain flag
		 */
		public boolean isRetain() {
			return retain;
		}

		/**
		 * Set the retain flag.
		 *
		 * @param retain
		 *        {@code true} to set the "retained" message flag
		 * @return this object
		 */
		public Builder setRetain(boolean retain) {
			this.retain = retain;
			return this;
		}

		/**
		 * Get the quality of service level.
		 *
		 * @return the quality of service level
		 */
		public MqttQoS getQos() {
			return qos;
		}

		/**
		 * Set the quality of service level.
		 *
		 * @param qos
		 *        the level to set
		 * @return this object
		 */
		public Builder setQos(MqttQoS qos) {
			if ( qos == null ) {
				throw new NullPointerException("qos");
			}
			this.qos = qos;
			return this;
		}

		/**
		 * Build the last will instance.
		 *
		 * @return the new instance
		 */
		public MqttLastWill build() {
			return new MqttLastWill(topic, message, retain, qos);
		}
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o )
			return true;
		if ( o == null || getClass() != o.getClass() )
			return false;

		MqttLastWill that = (MqttLastWill) o;

		if ( retain != that.retain )
			return false;
		if ( !topic.equals(that.topic) )
			return false;
		if ( !message.equals(that.message) )
			return false;
		return qos == that.qos;

	}

	@Override
	public int hashCode() {
		int result = topic.hashCode();
		result = 31 * result + message.hashCode();
		result = 31 * result + (retain ? 1 : 0);
		result = 31 * result + qos.hashCode();
		return result;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("MqttLastWill{");
		sb.append("topic='").append(topic).append('\'');
		sb.append(", message='").append(message).append('\'');
		sb.append(", retain=").append(retain);
		sb.append(", qos=").append(qos.name());
		sb.append('}');
		return sb.toString();
	}
}

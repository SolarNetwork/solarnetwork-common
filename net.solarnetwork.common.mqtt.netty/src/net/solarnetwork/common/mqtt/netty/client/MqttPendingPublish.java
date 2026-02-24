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

import static net.solarnetwork.util.ObjectUtils.requireNonNullProperty;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;
import io.netty.buffer.ByteBuf;
import io.netty.channel.EventLoop;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.util.concurrent.Promise;

/**
 * An in-flight publish message.
 *
 * @version 1.1
 */
final class MqttPendingPublish {

	private final int messageId;
	private final Promise<Void> future;
	private final ByteBuf payload;
	private final MqttPublishMessage message;
	private final MqttQoS qos;
	private final long date;

	private final @Nullable RetransmissionHandler<MqttPublishMessage> publishRetransmissionHandler;
	private final @Nullable RetransmissionHandler<MqttMessage> pubrelRetransmissionHandler;

	private boolean sent = false;

	MqttPendingPublish(int messageId, Promise<Void> future, ByteBuf payload, MqttPublishMessage message,
			MqttQoS qos, boolean enableRetransmission) {
		this.messageId = messageId;
		this.future = future;
		this.payload = payload;
		this.message = message;
		this.qos = qos;
		if ( qos != MqttQoS.AT_MOST_ONCE && enableRetransmission ) {
			this.publishRetransmissionHandler = new RetransmissionHandler<>();
			this.publishRetransmissionHandler.setOriginalMessage(message);
			this.pubrelRetransmissionHandler = (qos == MqttQoS.EXACTLY_ONCE
					? new RetransmissionHandler<>()
					: null);
		} else {
			this.publishRetransmissionHandler = null;
			this.pubrelRetransmissionHandler = null;
		}
		this.date = System.currentTimeMillis();
	}

	int getMessageId() {
		return messageId;
	}

	Promise<Void> getFuture() {
		return future;
	}

	ByteBuf getPayload() {
		return payload;
	}

	boolean isSent() {
		return sent;
	}

	void setSent(boolean sent) {
		this.sent = sent;
	}

	MqttPublishMessage getMessage() {
		return message;
	}

	MqttQoS getQos() {
		return qos;
	}

	/**
	 * Get the instance creation date.
	 *
	 * @return the instance creation date
	 * @since 1.1
	 */
	long getDate() {
		return date;
	}

	void startPublishRetransmissionTimer(EventLoop eventLoop, Consumer<Object> sendPacket) {
		final RetransmissionHandler<MqttPublishMessage> publishRetransmissionHandler = requireNonNullProperty(
				this.publishRetransmissionHandler, "Retransmission");
		publishRetransmissionHandler.setHandler(
				((fixedHeader, originalMessage) -> sendPacket.accept(new MqttPublishMessage(fixedHeader,
						originalMessage.variableHeader(), this.payload.retain()))));
		publishRetransmissionHandler.start(eventLoop);
	}

	void onPubackReceived() {
		if ( publishRetransmissionHandler != null ) {
			publishRetransmissionHandler.stop();
		}
	}

	void setPubrelMessage(MqttMessage pubrelMessage) {
		if ( pubrelRetransmissionHandler != null ) {
			pubrelRetransmissionHandler.setOriginalMessage(pubrelMessage);
		}
	}

	void startPubrelRetransmissionTimer(EventLoop eventLoop, Consumer<Object> sendPacket) {
		final RetransmissionHandler<MqttMessage> pubrelRetransmissionHandler = requireNonNullProperty(
				this.pubrelRetransmissionHandler, "Retransmission");
		pubrelRetransmissionHandler.setHandler((fixedHeader, originalMessage) -> sendPacket
				.accept(new MqttMessage(fixedHeader, originalMessage.variableHeader())));
		pubrelRetransmissionHandler.start(eventLoop);
	}

	void onPubcompReceived() {
		if ( pubrelRetransmissionHandler != null ) {
			pubrelRetransmissionHandler.stop();
		}
	}

	/**
	 * Stop all retransmission.
	 *
	 * @since 1.1
	 */
	void stop() {
		if ( publishRetransmissionHandler != null ) {
			publishRetransmissionHandler.stop();
		}
		if ( pubrelRetransmissionHandler != null ) {
			pubrelRetransmissionHandler.stop();
		}
	}
}

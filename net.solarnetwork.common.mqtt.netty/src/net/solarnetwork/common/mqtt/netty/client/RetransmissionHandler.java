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

import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.netty.channel.EventLoop;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.util.concurrent.ScheduledFuture;

final class RetransmissionHandler<T extends MqttMessage> {

	private static final Logger log = LoggerFactory.getLogger(RetransmissionHandler.class);

	private ScheduledFuture<?> timer;
	private int timeout = 10;
	private BiConsumer<MqttFixedHeader, T> handler;
	private T originalMessage;

	void start(EventLoop eventLoop) {
		if ( eventLoop == null ) {
			throw new NullPointerException("eventLoop");
		}
		if ( this.handler == null ) {
			throw new NullPointerException("handler");
		}
		this.timeout = 10;
		this.startTimer(eventLoop);
	}

	private void startTimer(EventLoop eventLoop) {
		this.timer = eventLoop.schedule(() -> {
			if ( log.isDebugEnabled() ) {
				MqttMessageIdVariableHeader idHeader = (this.originalMessage
						.variableHeader() instanceof MqttMessageIdVariableHeader
								? (MqttMessageIdVariableHeader) this.originalMessage.variableHeader()
								: null);
				log.debug("Retransmitting {} message ID {} after timeout of {} seconds",
						this.originalMessage.fixedHeader().messageType(),
						idHeader != null ? idHeader.messageId() : "?", this.timeout);
			}
			this.timeout += 5;
			MqttFixedHeader fixedHeader = new MqttFixedHeader(
					this.originalMessage.fixedHeader().messageType(), true,
					this.originalMessage.fixedHeader().qosLevel(),
					this.originalMessage.fixedHeader().isRetain(),
					this.originalMessage.fixedHeader().remainingLength());
			handler.accept(fixedHeader, originalMessage);
			startTimer(eventLoop);
		}, timeout, TimeUnit.SECONDS);
	}

	void stop() {
		if ( this.timer != null ) {
			this.timer.cancel(true);
		}
	}

	void setHandler(BiConsumer<MqttFixedHeader, T> runnable) {
		this.handler = runnable;
	}

	void setOriginalMessage(T originalMessage) {
		this.originalMessage = originalMessage;
	}
}

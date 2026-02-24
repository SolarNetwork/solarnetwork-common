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

import static net.solarnetwork.util.ObjectUtils.requireNonNullArgument;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.netty.channel.EventLoop;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.util.concurrent.ScheduledFuture;

final class RetransmissionHandler<T extends MqttMessage> {

	private static final Logger log = LoggerFactory.getLogger(RetransmissionHandler.class);

	private @Nullable ScheduledFuture<?> timer;
	private int timeout = 10;
	private @Nullable BiConsumer<MqttFixedHeader, T> handler;
	private @Nullable T originalMessage;

	private volatile boolean keepGoing;

	void start(final EventLoop eventLoop) {
		if ( eventLoop == null ) {
			throw new IllegalStateException("The eventLoop property is not configured.");
		}
		final BiConsumer<MqttFixedHeader, T> handler = this.handler;
		if ( handler == null ) {
			throw new IllegalStateException("The handler property is not configured.");
		}
		this.timeout = 10;
		this.keepGoing = true;
		// requireNonNullArgument() used to avoid NullAway warning
		this.startTimer(eventLoop, requireNonNullArgument(handler, "handler"));
	}

	private void startTimer(final EventLoop eventLoop, final BiConsumer<MqttFixedHeader, T> handler) {
		this.timer = eventLoop.schedule(() -> {
			final T originalMessage = this.originalMessage;
			if ( originalMessage != null ) {
				if ( log.isDebugEnabled() ) {
					MqttMessageIdVariableHeader idHeader = (originalMessage
							.variableHeader() instanceof MqttMessageIdVariableHeader
									? (MqttMessageIdVariableHeader) originalMessage.variableHeader()
									: null);
					log.debug("Retransmitting {} message ID {} after timeout of {} seconds",
							originalMessage.fixedHeader().messageType(),
							idHeader != null ? idHeader.messageId() : "?", this.timeout);
				}
				this.timeout += 5;
				MqttFixedHeader fixedHeader = new MqttFixedHeader(
						originalMessage.fixedHeader().messageType(), true,
						originalMessage.fixedHeader().qosLevel(),
						originalMessage.fixedHeader().isRetain(),
						originalMessage.fixedHeader().remainingLength());
				handler.accept(fixedHeader, originalMessage);
			}
			if ( keepGoing ) {
				startTimer(eventLoop, handler);
			}
		}, timeout, TimeUnit.SECONDS);
	}

	void stop() {
		this.keepGoing = false;
		final ScheduledFuture<?> timer = this.timer;
		if ( timer != null && !timer.isDone() ) {
			timer.cancel(true);
			this.timer = null;
		}
	}

	void setHandler(BiConsumer<MqttFixedHeader, T> runnable) {
		this.handler = requireNonNullArgument(runnable, "runnable");
	}

	void setOriginalMessage(T originalMessage) {
		this.originalMessage = requireNonNullArgument(originalMessage, "originalMessage");
	}

}

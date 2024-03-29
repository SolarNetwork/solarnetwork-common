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
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.ScheduledFuture;

final class MqttPingHandler extends ChannelInboundHandlerAdapter {

	private final int keepaliveSeconds;
	private final boolean closeOnReaderIdle;

	private ScheduledFuture<?> pingRespTimeout;

	MqttPingHandler(int keepaliveSeconds) {
		this(keepaliveSeconds, true);
	}

	MqttPingHandler(int keepaliveSeconds, boolean closeOnReaderIdle) {
		super();
		this.keepaliveSeconds = keepaliveSeconds;
		this.closeOnReaderIdle = closeOnReaderIdle;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if ( !(msg instanceof MqttMessage) ) {
			ctx.fireChannelRead(msg);
			return;
		}
		MqttMessage message = (MqttMessage) msg;
		if ( message.fixedHeader().messageType() == MqttMessageType.PINGREQ ) {
			this.handlePingReq(ctx.channel());
		} else if ( message.fixedHeader().messageType() == MqttMessageType.PINGRESP ) {
			this.handlePingResp();
		} else {
			ctx.fireChannelRead(ReferenceCountUtil.retain(msg));
		}
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		super.userEventTriggered(ctx, evt);

		if ( evt instanceof IdleStateEvent ) {
			IdleStateEvent event = (IdleStateEvent) evt;
			switch (event.state()) {
				case READER_IDLE:
					if ( this.closeOnReaderIdle ) {
						// reader timeout: assume network connection lost (half open maybe?) so force closed
						// because on a healthy connection we should never get here, even from a connection
						// not explicitly publishing/receiving messages because PINGREQ message are
						// automatically published below from writer timeout and we assume the writer timeout
						// is less than the reader timeout
						ctx.close();
					} else {
						// perhaps a publish-only connection, so just push a PINGREQ to ensure the connection
						// is actually fully open 
						this.sendPingReq(ctx.channel());
					}
					break;
				case WRITER_IDLE:
					this.sendPingReq(ctx.channel());
					break;
				default:
					// nothing
			}
		}
	}

	private void sendPingReq(Channel channel) {
		MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PINGREQ, false,
				MqttQoS.AT_MOST_ONCE, false, 0);
		channel.writeAndFlush(new MqttMessage(fixedHeader));

		if ( this.pingRespTimeout != null ) {
			this.pingRespTimeout = channel.eventLoop().schedule(() -> {
				MqttFixedHeader fixedHeader2 = new MqttFixedHeader(MqttMessageType.DISCONNECT, false,
						MqttQoS.AT_MOST_ONCE, false, 0);
				channel.writeAndFlush(new MqttMessage(fixedHeader2))
						.addListener(ChannelFutureListener.CLOSE);
				//TODO: what do when the connection is closed ?
			}, this.keepaliveSeconds, TimeUnit.SECONDS);
		}
	}

	private void handlePingReq(Channel channel) {
		MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PINGRESP, false,
				MqttQoS.AT_MOST_ONCE, false, 0);
		channel.writeAndFlush(new MqttMessage(fixedHeader));
	}

	private void handlePingResp() {
		if ( this.pingRespTimeout != null && !this.pingRespTimeout.isCancelled()
				&& !this.pingRespTimeout.isDone() ) {
			this.pingRespTimeout.cancel(true);
			this.pingRespTimeout = null;
		}
	}
}

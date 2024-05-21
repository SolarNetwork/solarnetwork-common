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

import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;

/**
 * A connection result.
 *
 * @author matt
 * @version 1.0
 */
public final class MqttConnectResult {

	private final boolean success;
	private final MqttConnectReturnCode returnCode;
	private final ChannelFuture closeFuture;

	/**
	 * Constructor.
	 *
	 * @param success
	 *        {@code true} on success
	 * @param returnCode
	 *        the return code
	 * @param closeFuture
	 *        the channel close future
	 */
	MqttConnectResult(boolean success, MqttConnectReturnCode returnCode, ChannelFuture closeFuture) {
		this.success = success;
		this.returnCode = returnCode;
		this.closeFuture = closeFuture;
	}

	/**
	 * Get the success flag
	 *
	 * @return {@code true} on success
	 */
	public boolean isSuccess() {
		return success;
	}

	/**
	 * Get the return code.
	 *
	 * @return the return code.
	 */
	public MqttConnectReturnCode getReturnCode() {
		return returnCode;
	}

	/**
	 * Get the channel close future.
	 *
	 * @return the channel close future
	 */
	public ChannelFuture getCloseFuture() {
		return closeFuture;
	}
}

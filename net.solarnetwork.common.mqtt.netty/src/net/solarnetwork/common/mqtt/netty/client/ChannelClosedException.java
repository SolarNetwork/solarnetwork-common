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

/**
 * Created by Valerii Sosliuk on 12/26/2017.
 */
public class ChannelClosedException extends RuntimeException {

	private static final long serialVersionUID = 6266638352424706909L;

	/**
	 * Constructor.
	 */
	public ChannelClosedException() {
	}

	/**
	 * Constructor.
	 *
	 * @param message
	 *        the message
	 */
	public ChannelClosedException(String message) {
		super(message);
	}

	/**
	 * Constructor.
	 *
	 * @param message
	 *        the message
	 * @param cause
	 *        the cause
	 */
	public ChannelClosedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor.
	 *
	 * @param cause
	 *        the cause
	 */
	public ChannelClosedException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor.
	 *
	 * @param message
	 *        the message
	 * @param cause
	 *        the cause
	 * @param enableSuppression
	 *        {@code true} to enable suppression
	 * @param writableStackTrace
	 *        {@code true} for a writable stack trace
	 */
	public ChannelClosedException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}

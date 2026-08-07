/* ==================================================================
 * MessageSizeLimitExceeded.java - 8 Aug 2026 11:17:34 am
 *
 * Copyright 2026 SolarNetwork.net Dev Team
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

import java.io.Serial;
import org.jspecify.annotations.Nullable;

/**
 * Thrown to indicate the message size is too large.
 *
 * @author matt
 * @version 1.0
 * @since 6.2
 */
public class MessageSizeLimitExceeded extends IllegalArgumentException {

	@Serial
	private static final long serialVersionUID = -5370525682548341303L;

	/** The actual message size. */
	private final long messageSize;

	/** The maximum message size allowed. */
	private final long maximumSize;

	/**
	 * Constructor.
	 *
	 * @param message
	 *        the message
	 * @param messageSize
	 *        the attempted message size
	 * @param maximumSize
	 *        the maximum size allowed
	 */
	public MessageSizeLimitExceeded(String message, long messageSize, long maximumSize) {
		super(message);
		this.messageSize = messageSize;
		this.maximumSize = maximumSize;
	}

	/**
	 * Constructor.
	 *
	 * @param messageSize
	 *        the attempted message size
	 * @param maximumSize
	 *        the maximum size allowed
	 * @param cause
	 *        the cause
	 */
	public MessageSizeLimitExceeded(long messageSize, long maximumSize, @Nullable Throwable cause) {
		super(cause);
		this.messageSize = messageSize;
		this.maximumSize = maximumSize;
	}

	/**
	 * Constructor.
	 *
	 * @param message
	 *        the message
	 * @param messageSize
	 *        the attempted message size
	 * @param maximumSize
	 *        the maximum size allowed
	 * @param cause
	 *        the cause
	 */
	public MessageSizeLimitExceeded(String message, long messageSize, long maximumSize,
			@Nullable Throwable cause) {
		super(message, cause);
		this.messageSize = messageSize;
		this.maximumSize = maximumSize;
	}

	/**
	 * Get the attempted message size.
	 *
	 * @return the message size
	 */
	public final long getMessageSize() {
		return messageSize;
	}

	/**
	 * Get the maximum message size allowed.
	 *
	 * @return the maximum size allowed
	 */
	public final long getMaximumSize() {
		return maximumSize;
	}

}

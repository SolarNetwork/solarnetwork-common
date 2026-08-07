/* ==================================================================
 * InvalidTopicNameException.java - 8 Aug 2026 11:19:05 am
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
 * Thrown to indicate a topic name contains invalid characters, is too long, or
 * empty.
 *
 * @author matt
 * @version 1.0
 * @since 6.2
 */
public class InvalidTopicNameException extends IllegalArgumentException {

	@Serial
	private static final long serialVersionUID = -4958454200158336617L;

	/**
	 * Constructor.
	 *
	 * @param message
	 *        the message
	 */
	public InvalidTopicNameException(String message) {
		super(message);
	}

	/**
	 * Constructor.
	 *
	 * @param cause
	 *        the cause
	 */
	public InvalidTopicNameException(@Nullable Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor.
	 *
	 * @param message
	 *        the message
	 * @param cause
	 *        the cause
	 */
	public InvalidTopicNameException(String message, @Nullable Throwable cause) {
		super(message, cause);
	}

}

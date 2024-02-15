/* ==================================================================
 * AuthorizationException.java - 14/02/2020 1:57:20 pm
 * 
 * Copyright 2020 SolarNetwork.net Dev Team
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

package net.solarnetwork.ocpp.service;

import net.solarnetwork.ocpp.domain.AuthorizationInfo;

/**
 * Authorization related exception.
 * 
 * @author matt
 * @version 2.0
 */
public class AuthorizationException extends RuntimeException {

	private static final long serialVersionUID = -5586850023205320485L;

	/** The authorization information. */
	private final AuthorizationInfo info;

	/** An optional transaction ID. */
	private final String transactionId;

	/**
	 * Constructor.
	 * 
	 * @param info
	 *        the authorization info
	 */
	public AuthorizationException(AuthorizationInfo info) {
		this(null, info, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param message
	 *        a message
	 * @param info
	 *        the authorization info
	 */
	public AuthorizationException(String message, AuthorizationInfo info) {
		this(message, info, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param info
	 *        the authorization info
	 * @param transactionId
	 *        the optional transaction ID
	 * @since 1.1
	 */
	public AuthorizationException(AuthorizationInfo info, String transactionId) {
		this(null, info, transactionId);
	}

	/**
	 * Constructor.
	 * 
	 * @param message
	 *        a message
	 * @param info
	 *        the authorization info
	 * @param transactionId
	 *        the optional transaction ID
	 * @since 2.0
	 */
	public AuthorizationException(String message, AuthorizationInfo info, String transactionId) {
		super(message);
		this.info = info;
		this.transactionId = transactionId;
	}

	/**
	 * Get the authorization info.
	 * 
	 * @return the info
	 */
	public AuthorizationInfo getInfo() {
		return info;
	}

	/**
	 * Get the optional transaction ID.
	 * 
	 * @return the transaction ID
	 * @since 2.0
	 */
	public String getTransactionId() {
		return transactionId;
	}

}

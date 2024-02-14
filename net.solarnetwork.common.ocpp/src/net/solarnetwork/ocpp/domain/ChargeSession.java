/* ==================================================================
 * ChargeSession.java - 10/02/2020 8:03:23 am
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

package net.solarnetwork.ocpp.domain;

import java.time.Instant;
import java.util.UUID;
import net.solarnetwork.dao.BasicUuidEntity;

/**
 * An entity for tracking an OCPP transaction, which represents a single
 * charging cycle from authorization to end of charging.
 * 
 * @author matt
 * @version 1.2
 */
public class ChargeSession extends BasicUuidEntity {

	private static final long serialVersionUID = 1726642518109338069L;

	/** The authorization ID. */
	private final String authId;

	/** The charge point ID. */
	private final long chargePointId;

	/** The EVSE ID. */
	private final int evseId;

	/** The charge point connector ID. */
	private final int connectorId;

	/** The transaction ID. */
	private final int transactionId;

	/** The end date. */
	private Instant ended;

	/** The end authorization ID. */
	private String endAuthId;

	/** The end reason. */
	private ChargeSessionEndReason endReason;

	/** The date posted to the Central System. */
	private Instant posted;

	/**
	 * Constructor.
	 * 
	 * @param authId
	 *        the authorization ID
	 * @param chargePointId
	 *        the Charge Point ID
	 * @param connectorId
	 *        the Charge Point connector ID
	 * @param transactionId
	 *        the transactionID
	 */
	public ChargeSession(String authId, long chargePointId, int connectorId, int transactionId) {
		this(null, null, authId, chargePointId, connectorId, transactionId);
	}

	/**
	 * Constructor.
	 * 
	 * @param authId
	 *        the authorization ID
	 * @param chargePointId
	 *        the Charge Point ID
	 * @param evseId
	 *        the EVSE ID
	 * @param connectorId
	 *        the Charge Point connector ID
	 * @param transactionId
	 *        the transactionID
	 * @since 1.2
	 */
	public ChargeSession(String authId, long chargePointId, int evseId, int connectorId,
			int transactionId) {
		this(null, null, authId, chargePointId, evseId, connectorId, transactionId);
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *        the primary key
	 * @param created
	 *        the created date
	 * @param authId
	 *        the authorization ID
	 * @param chargePointId
	 *        the Charge Point ID
	 * @param connectorId
	 *        the Charge Point connector ID
	 * @param transactionId
	 *        the transactionID
	 */
	public ChargeSession(UUID id, Instant created, String authId, long chargePointId, int connectorId,
			int transactionId) {
		this(id, created, authId, chargePointId, 0, connectorId, transactionId);
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *        the primary key
	 * @param created
	 *        the created date
	 * @param authId
	 *        the authorization ID
	 * @param chargePointId
	 *        the Charge Point ID
	 * @param evseId
	 *        the EVSE ID
	 * @param connectorId
	 *        the Charge Point connector ID
	 * @param transactionId
	 *        the transactionID
	 * @since 1.2
	 */
	public ChargeSession(UUID id, Instant created, String authId, long chargePointId, int evseId,
			int connectorId, int transactionId) {
		super(id, created);
		this.authId = authId;
		this.chargePointId = chargePointId;
		this.evseId = evseId;
		this.connectorId = connectorId;
		this.transactionId = transactionId;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ChargeSession{id=");
		builder.append(getId());
		builder.append(", chargePointId=");
		builder.append(chargePointId);
		builder.append(", evseId=");
		builder.append(evseId);
		builder.append(", connectorId=");
		builder.append(connectorId);
		builder.append(", transactionId=");
		builder.append(transactionId);
		builder.append(", ");
		if ( ended != null ) {
			builder.append("ended=");
			builder.append(ended);
		}
		builder.append("}");
		return builder.toString();
	}

	/**
	 * Get the {@link Authorization} ID.
	 * 
	 * @return the authorization ID
	 */
	public String getAuthId() {
		return authId;
	}

	/**
	 * Get the Charge Point ID.
	 * 
	 * @return the Charge Point ID
	 */
	public long getChargePointId() {
		return chargePointId;
	}

	/**
	 * Get the EVSE ID.
	 * 
	 * @return the EVSE ID
	 * @since 1.2
	 */
	public int getEvseId() {
		return evseId;
	}

	/**
	 * Get the Charge Point connection ID.
	 * 
	 * @return the Charge Point connection ID
	 */
	public int getConnectorId() {
		return connectorId;
	}

	/**
	 * Get the assigned transaction ID.
	 * 
	 * @return the transaction ID
	 */
	public int getTransactionId() {
		return transactionId;
	}

	/**
	 * Get the session end date.
	 * 
	 * @return the ended the end date, or {@literal null} if not ended
	 */
	public Instant getEnded() {
		return ended;
	}

	/**
	 * Set the session end date.
	 * 
	 * @param ended
	 *        the end date to set
	 */
	public void setEnded(Instant ended) {
		this.ended = ended;
	}

	/**
	 * Get the session end reason.
	 * 
	 * @return the the end reason
	 */
	public ChargeSessionEndReason getEndReason() {
		return endReason;
	}

	/**
	 * Set the session end reason.
	 * 
	 * @param endReason
	 *        the reason to set
	 */
	public void setEndReason(ChargeSessionEndReason endReason) {
		this.endReason = endReason;
	}

	/**
	 * The authorization ID used to end the transaction.
	 * 
	 * @return the ending authorization ID, or {@literal null}
	 */
	public String getEndAuthId() {
		return endAuthId;
	}

	/**
	 * Set the authorization ID used to end the transaction.
	 * 
	 * @param endAuthId
	 *        the ending authorization ID to set
	 */
	public void setEndAuthId(String endAuthId) {
		this.endAuthId = endAuthId;
	}

	/**
	 * Get the session posted date.
	 * 
	 * <p>
	 * The posted date represents a date when this transaction has been posted
	 * to some external service.
	 * </p>
	 * 
	 * @return the posted date or {@literal null} if not posted
	 */
	public Instant getPosted() {
		return posted;
	}

	/**
	 * Set the session posted date.
	 * 
	 * @param posted
	 *        the posted to set
	 */
	public void setPosted(Instant posted) {
		this.posted = posted;
	}

}

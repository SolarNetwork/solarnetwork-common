/* ==================================================================
 * NetworkAssociationDetails.java - Sep 6, 2011 8:04:08 PM
 *
 * Copyright 2007-2011 SolarNetwork.net Dev Team
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

package net.solarnetwork.domain;

import java.io.Serializable;
import java.time.Instant;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Command object for initial SolarNode and SolarNet association data.
 *
 * @author matt
 * @version 2.1
 */
@JsonPropertyOrder({ "host", "port", "forceTLS", "networkServiceURLs", "identityKey", "termsOfService",
		"confirmationKey", "username", "expiration", "securityPhrase", "networkId", "networkCertificate",
		"networkCertificateStatus", "networkCertificateSubjectDN", "keystorePassword" })
@JsonIgnoreProperties({ "solarQueryServiceURL", "solarUserServiceURL", "solarInMqttServiceURL" })
public class NetworkAssociationDetails extends BasicNetworkIdentity
		implements NetworkAssociation, NetworkCertificate, Serializable {

	private static final long serialVersionUID = -6264228260215100345L;

	/** The confirmation key. */
	private String confirmationKey;

	/** The username. */
	private String username;

	/** The expiration. */
	private Instant expiration;

	/** The security phrase. */
	private String securityPhrase;

	/** The network ID. */
	private Long networkId;

	/** The certificate. */
	private String networkCertificate;

	/** The certificate status. */
	private String networkCertificateStatus;

	/** The certificate distinguished name. */
	private String networkCertificateSubjectDN;

	/** The keystore password. */
	private String keystorePassword;

	/**
	 * Default constructor.
	 */
	public NetworkAssociationDetails() {
		super();
	}

	/**
	 * Copy constructor.
	 *
	 * @param other
	 *        the NetworkAssociation to copy
	 */
	public NetworkAssociationDetails(NetworkAssociation other) {
		super();
		setConfirmationKey(other.getConfirmationKey());
		setHost(other.getHost());
		setIdentityKey(other.getIdentityKey());
		setPort(other.getPort());
		setSecurityPhrase(other.getSecurityPhrase());
		setTermsOfService(other.getTermsOfService());
		setForceTLS(other.isForceTLS());
	}

	/**
	 * Construct with association details.
	 *
	 * @param username
	 *        the username
	 * @param confirmationKey
	 *        the confirmation key
	 * @param keystorePassword
	 *        the keystore password
	 * @since 1.1
	 */
	public NetworkAssociationDetails(String username, String confirmationKey, String keystorePassword) {
		super();
		setUsername(username);
		setConfirmationKey(confirmationKey);
		setKeystorePassword(keystorePassword);
	}

	@Override
	public String toString() {
		return "NetworkAssociationDetails{host=" + getHost() + ",username=" + username + ",networkId="
				+ networkId + '}';
	}

	@Override
	public String getUsername() {
		return username;
	}

	/**
	 * Set the username.
	 *
	 * @param username
	 *        the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Get the expiration.
	 *
	 * @return the expiration
	 */
	public Instant getExpiration() {
		return expiration;
	}

	/**
	 * Set the expiration.
	 *
	 * @param expiration
	 *        the expiration to set
	 */
	public void setExpiration(Instant expiration) {
		this.expiration = expiration;
	}

	@Override
	public String getConfirmationKey() {
		return confirmationKey;
	}

	/**
	 * Set the confirmation key.
	 *
	 * @param confirmationKey
	 *        the confirmation key to set
	 */
	public void setConfirmationKey(String confirmationKey) {
		this.confirmationKey = confirmationKey;
	}

	@Override
	public String getSecurityPhrase() {
		return securityPhrase;
	}

	/**
	 * Set the security phrase.
	 *
	 * @param secretPhrase
	 *        the phrase to set
	 */
	public void setSecurityPhrase(String secretPhrase) {
		this.securityPhrase = secretPhrase;
	}

	@Override
	public Long getNetworkId() {
		return networkId;
	}

	/**
	 * Set the network ID.
	 *
	 * @param networkId
	 *        the network ID to set
	 */
	public void setNetworkId(Long networkId) {
		this.networkId = networkId;
	}

	@Override
	public String getNetworkCertificate() {
		return networkCertificate;
	}

	/**
	 * Set the network certificate.
	 *
	 * @param networkCertificate
	 *        the network certificate to set
	 */
	public void setNetworkCertificate(String networkCertificate) {
		this.networkCertificate = networkCertificate;
	}

	@Override
	public String getNetworkCertificateStatus() {
		return networkCertificateStatus;
	}

	/**
	 * Set the network certificate status.
	 *
	 * @param networkCertificateStatus
	 *        the status to set
	 */
	public void setNetworkCertificateStatus(String networkCertificateStatus) {
		this.networkCertificateStatus = networkCertificateStatus;
	}

	@Override
	public String getNetworkCertificateSubjectDN() {
		return networkCertificateSubjectDN;
	}

	/**
	 * Set the network certificate subject DN.
	 *
	 * @param networkCertificateSubjectDN
	 *        the DN to set
	 */
	public void setNetworkCertificateSubjectDN(String networkCertificateSubjectDN) {
		this.networkCertificateSubjectDN = networkCertificateSubjectDN;
	}

	@Override
	public String getKeystorePassword() {
		return keystorePassword;
	}

	/**
	 * Set the keystore password.
	 *
	 * @param keystorePassword
	 *        the password to set
	 */
	public void setKeystorePassword(String keystorePassword) {
		this.keystorePassword = keystorePassword;
	}

}

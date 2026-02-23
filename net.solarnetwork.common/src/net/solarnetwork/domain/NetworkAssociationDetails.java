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
import org.jspecify.annotations.Nullable;
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
	private @Nullable String confirmationKey;

	/** The username. */
	private @Nullable String username;

	/** The expiration. */
	private @Nullable Instant expiration;

	/** The security phrase. */
	private @Nullable String securityPhrase;

	/** The network ID. */
	private @Nullable Long networkId;

	/** The certificate. */
	private @Nullable String networkCertificate;

	/** The certificate status. */
	private @Nullable String networkCertificateStatus;

	/** The certificate distinguished name. */
	private @Nullable String networkCertificateSubjectDN;

	/** The keystore password. */
	private @Nullable String keystorePassword;

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
	public NetworkAssociationDetails(@Nullable String username, @Nullable String confirmationKey,
			@Nullable String keystorePassword) {
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
	public final @Nullable String getUsername() {
		return username;
	}

	/**
	 * Set the username.
	 *
	 * @param username
	 *        the username to set
	 */
	public final void setUsername(@Nullable String username) {
		this.username = username;
	}

	/**
	 * Get the expiration.
	 *
	 * @return the expiration
	 */
	public final @Nullable Instant getExpiration() {
		return expiration;
	}

	/**
	 * Set the expiration.
	 *
	 * @param expiration
	 *        the expiration to set
	 */
	public final void setExpiration(@Nullable Instant expiration) {
		this.expiration = expiration;
	}

	@Override
	public final @Nullable String getConfirmationKey() {
		return confirmationKey;
	}

	/**
	 * Set the confirmation key.
	 *
	 * @param confirmationKey
	 *        the confirmation key to set
	 */
	public final void setConfirmationKey(@Nullable String confirmationKey) {
		this.confirmationKey = confirmationKey;
	}

	@Override
	public final @Nullable String getSecurityPhrase() {
		return securityPhrase;
	}

	/**
	 * Set the security phrase.
	 *
	 * @param secretPhrase
	 *        the phrase to set
	 */
	public final void setSecurityPhrase(@Nullable String secretPhrase) {
		this.securityPhrase = secretPhrase;
	}

	@Override
	public final @Nullable Long getNetworkId() {
		return networkId;
	}

	/**
	 * Set the network ID.
	 *
	 * @param networkId
	 *        the network ID to set
	 */
	public final void setNetworkId(@Nullable Long networkId) {
		this.networkId = networkId;
	}

	@Override
	public final @Nullable String getNetworkCertificate() {
		return networkCertificate;
	}

	/**
	 * Set the network certificate.
	 *
	 * @param networkCertificate
	 *        the network certificate to set
	 */
	public final void setNetworkCertificate(@Nullable String networkCertificate) {
		this.networkCertificate = networkCertificate;
	}

	@Override
	public final @Nullable String getNetworkCertificateStatus() {
		return networkCertificateStatus;
	}

	/**
	 * Set the network certificate status.
	 *
	 * @param networkCertificateStatus
	 *        the status to set
	 */
	public final void setNetworkCertificateStatus(@Nullable String networkCertificateStatus) {
		this.networkCertificateStatus = networkCertificateStatus;
	}

	@Override
	public final @Nullable String getNetworkCertificateSubjectDN() {
		return networkCertificateSubjectDN;
	}

	/**
	 * Set the network certificate subject DN.
	 *
	 * @param networkCertificateSubjectDN
	 *        the DN to set
	 */
	public final void setNetworkCertificateSubjectDN(@Nullable String networkCertificateSubjectDN) {
		this.networkCertificateSubjectDN = networkCertificateSubjectDN;
	}

	@Override
	public final @Nullable String getKeystorePassword() {
		return keystorePassword;
	}

	/**
	 * Set the keystore password.
	 *
	 * @param keystorePassword
	 *        the password to set
	 */
	public final void setKeystorePassword(@Nullable String keystorePassword) {
		this.keystorePassword = keystorePassword;
	}

}

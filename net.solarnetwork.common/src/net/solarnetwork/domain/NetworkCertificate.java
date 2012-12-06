/* ==================================================================
 * NetworkCertificate.java - Nov 30, 2012 8:24:23 AM
 * 
 * Copyright 2007-2012 SolarNetwork.net Dev Team
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

/**
 * API for a network certificate.
 * 
 * @author matt
 * @version 1.0
 */
public interface NetworkCertificate {

	/**
	 * Get an ID associated with this certificate.
	 * 
	 * @return a unique ID, e.g. node ID, never <em>null</em>
	 */
	Long getNetworkId();

	/**
	 * Get a confirmation key, which can be used to later retrieve the network
	 * certificate if not immediately available in
	 * {@link #getNetworkCertificate()}.
	 * 
	 * @return confirmation key, never <em>null</em>
	 */
	String getConfirmationKey();

	/**
	 * Get a status associated with the certificate.
	 * 
	 * @return the status, which may indicate if the certificate is being
	 *         processed, etc
	 */
	String getNetworkCertificateStatus();

	/**
	 * Get the value of the node's expected public key certificate subject name.
	 * 
	 * <p>
	 * The node must generate a certificate signing request (CSR) using this
	 * subject name and then install the signed certificate when granted by the
	 * SolarNet certification authority (CA).
	 * </p>
	 * 
	 * @return the node's subject DN
	 */
	String getNetworkCertificateSubjectDN();

	/**
	 * Get the certificate, as Base64-encoded string.
	 * 
	 * @return the certificate, or <em>null</em> if the certificate is not
	 *         available yet
	 */
	String getNetworkCertificate();

}

/* ==================================================================
 * CACertificateService.java - Jan 24, 2015 10:00:43 PM
 * 
 * Copyright 2007-2015 SolarNetwork.net Dev Team
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

package net.solarnetwork.support;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

/**
 * Certification authority service.
 * 
 * @author matt
 * @version 1.0
 */
public interface CertificationAuthorityService {

	/**
	 * Sign a CSR with a given key.
	 * 
	 * @param csr
	 *        the PKCS10 encoded certificate signing request.
	 * @param caCert
	 *        The certification authority certificate to sign the CSR with.
	 * @param privateKey
	 *        The certification authority's private key to sign the CSR with.
	 * @return The generated, signed certificate.
	 * @throws CertificateException
	 *         If any error occurs.
	 */
	X509Certificate signCertificate(String csr, X509Certificate caCert, PrivateKey privateKey)
			throws CertificateException;

}

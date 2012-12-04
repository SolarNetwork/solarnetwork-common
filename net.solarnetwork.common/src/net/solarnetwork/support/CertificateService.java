/* ==================================================================
 * CertificateService.java - Dec 5, 2012 6:34:45 AM
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

package net.solarnetwork.support;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * API for PKI support.
 * 
 * @author matt
 * @version 1.0
 */
public interface CertificateService {

	/**
	 * Generate a certificate request for a given DN, public key, and private
	 * key.
	 * 
	 * @param dn
	 *        the request subject name, e.g.
	 *        {@code CN=John Doe, OU=Accounting, O=Big Organization, C=US}
	 * @param publicKey
	 *        the public key to include in the request
	 * @param privateKey
	 *        the private key to sign the request with
	 * @return the request, as a PKCS#10 encoded request
	 * @throws CertificateException
	 *         if any error occurrs
	 */
	byte[] generatePKCS10CertificateRequest(String dn, PublicKey publicKey, PrivateKey privateKey)
			throws CertificateException;

	/**
	 * Generate a certificate request for a given DN, public key, and private
	 * key, formatted as a Base64-encoded request string.
	 * 
	 * @param dn
	 *        the request subject name, e.g.
	 *        {@code CN=John Doe, OU=Accounting, O=Big Organization, C=US}
	 * @param publicKey
	 *        the public key to include in the request
	 * @param privateKey
	 *        the private key to sign the request with
	 * @return the request, as a Base64-encoded PKCS#10 request
	 * @throws CertificateException
	 *         if any error occurrs
	 */
	String generatePKCS10CertificateRequestString(String dn, PublicKey publicKey, PrivateKey privateKey)
			throws CertificateException;

}

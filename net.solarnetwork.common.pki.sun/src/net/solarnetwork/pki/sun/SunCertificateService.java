/* ==================================================================
 * SunCertificateService.java - Dec 5, 2012 6:30:01 AM
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

package net.solarnetwork.pki.sun;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import net.solarnetwork.support.CertificateException;
import net.solarnetwork.support.CertificateService;
import sun.security.pkcs.PKCS10;
import sun.security.x509.X500Name;
import sun.security.x509.X500Signer;

/**
 * Manage certificates using the Sun JDK6 provider classes.
 * 
 * @author matt
 * @version 1.0
 */
public class SunCertificateService implements CertificateService {

	private String pkcs10SignatureAlgorithm = "MD5WithRSA";

	private PKCS10 createPKCS10(String dn, PublicKey publicKey, PrivateKey privateKey) {
		PKCS10 pkcs10 = new PKCS10(publicKey);
		try {
			Signature signature = Signature.getInstance(pkcs10SignatureAlgorithm);
			signature.initSign(privateKey);
			X500Name x500Name = new X500Name(dn);
			pkcs10.encodeAndSign(new X500Signer(signature, x500Name));
		} catch ( NoSuchAlgorithmException e ) {
			throw new CertificateException(e);
		} catch ( InvalidKeyException e ) {
			throw new CertificateException(e);
		} catch ( IOException e ) {
			throw new CertificateException(e);
		} catch ( java.security.cert.CertificateException e ) {
			throw new CertificateException(e);
		} catch ( SignatureException e ) {
			throw new CertificateException(e);
		}
		return pkcs10;
	}

	@Override
	public byte[] generatePKCS10CertificateRequest(String dn, PublicKey publicKey, PrivateKey privateKey)
			throws CertificateException {
		PKCS10 pkcs10 = createPKCS10(dn, publicKey, privateKey);
		return pkcs10.getEncoded();
	}

	@Override
	public String generatePKCS10CertificateRequestString(String dn, PublicKey publicKey,
			PrivateKey privateKey) throws CertificateException {
		PKCS10 pkcs10 = createPKCS10(dn, publicKey, privateKey);
		ByteArrayOutputStream byos = new ByteArrayOutputStream();
		PrintStream ps = null;
		String result = null;
		try {
			ps = new PrintStream(byos, false, "US-ASCII");
			pkcs10.print(ps);
			ps.flush();
			result = byos.toString("US-ASCII");
		} catch ( SignatureException e ) {
			throw new CertificateException(e);
		} catch ( IOException e ) {
			throw new CertificateException(e);
		} finally {
			if ( ps != null ) {
				ps.close();
			}
			try {
				byos.close();
			} catch ( IOException e ) {
				// ignore this
			}
		}
		return result;
	}

	public void setPkcs10SignatureAlgorithm(String pkcs10SignatureAlgorithm) {
		this.pkcs10SignatureAlgorithm = pkcs10SignatureAlgorithm;
	}

}

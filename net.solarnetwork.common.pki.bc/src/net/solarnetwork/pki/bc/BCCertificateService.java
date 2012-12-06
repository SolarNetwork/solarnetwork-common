/* ==================================================================
 * BCCertificateService.java - Dec 5, 2012 10:42:17 AM
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

package net.solarnetwork.pki.bc;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;
import javax.security.auth.x500.X500Principal;
import net.solarnetwork.support.CertificateException;
import net.solarnetwork.support.CertificateService;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

/**
 * Bouncy Castle implementation of {@link CertificateService}.
 * 
 * @author matt
 * @version 1.0
 */
public class BCCertificateService implements CertificateService {

	private final AtomicLong counter = new AtomicLong(System.currentTimeMillis());

	private int certificateExpireDays = 730;
	private String signatureAlgorithm = "SHA256WithRSA";

	@Override
	public X509Certificate generateCertificate(String dn, PublicKey publicKey, PrivateKey privateKey) {
		X500Principal issuer = new X500Principal(dn);
		Date now = new Date();
		Date expire = new Date(now.getTime() + (1000L * 60L * 60L * 24L * certificateExpireDays));
		JcaX509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(issuer, new BigInteger(
				String.valueOf(counter.incrementAndGet())), now, expire, issuer, publicKey);
		JcaContentSignerBuilder signerBuilder = new JcaContentSignerBuilder(signatureAlgorithm);
		ContentSigner signer;
		try {
			signer = signerBuilder.build(privateKey);
		} catch ( OperatorCreationException e ) {
			throw new CertificateException("Error signing certificate", e);
		}
		X509CertificateHolder holder = builder.build(signer);
		JcaX509CertificateConverter converter = new JcaX509CertificateConverter();
		try {
			return converter.getCertificate(holder);
		} catch ( java.security.cert.CertificateException e ) {
			throw new CertificateException("Error creating certificate", e);
		}
	}

	@Override
	public String generatePKCS10CertificateRequestString(X509Certificate cert, PrivateKey privateKey)
			throws CertificateException {
		X509CertificateHolder holder;
		try {
			holder = new JcaX509CertificateHolder(cert);
		} catch ( CertificateEncodingException e ) {
			throw new CertificateException("Error creating CSR", e);
		}
		PKCS10CertificationRequestBuilder builder = new PKCS10CertificationRequestBuilder(
				holder.getSubject(), holder.getSubjectPublicKeyInfo());
		JcaContentSignerBuilder signerBuilder = new JcaContentSignerBuilder(signatureAlgorithm);
		ContentSigner signer;
		try {
			signer = signerBuilder.build(privateKey);
		} catch ( OperatorCreationException e ) {
			throw new CertificateException("Error signing certificate request", e);
		}
		PKCS10CertificationRequest csr = builder.build(signer);
		StringWriter writer = new StringWriter();
		PemWriter pemWriter = new PemWriter(writer);
		try {
			pemWriter.writeObject(new PemObject("CERTIFICATE REQUEST", csr.getEncoded()));
		} catch ( IOException e ) {
			throw new CertificateException("Error signing certificate", e);
		} finally {
			try {
				pemWriter.flush();
				pemWriter.close();
				writer.close();
			} catch ( IOException e ) {
				// ignore this
			}
		}
		return writer.toString();
	}

	public void setCertificateExpireDays(int certificateExpireDays) {
		this.certificateExpireDays = certificateExpireDays;
	}

	public void setSignatureAlgorithm(String signatureAlgorithm) {
		this.signatureAlgorithm = signatureAlgorithm;
	}

}

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

package net.solarnetwork.common.pki.bc;

import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;
import javax.security.auth.x500.X500Principal;
import net.solarnetwork.support.CertificateException;
import net.solarnetwork.support.CertificateService;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

/**
 * Bouncy Castle implementation of {@link CertificateService}.
 * 
 * @author matt
 * @version 1.0
 */
public class BCCertificateService implements CertificateService {

	private final AtomicLong counter = new AtomicLong(System.currentTimeMillis());
	private final int certificateExpireDays = 730;
	private final String signatureAlgorithm = "SHA1WithRSA";

	@Override
	public Certificate generateCertificate(String dn, PublicKey publicKey, PrivateKey privateKey) {
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
	public byte[] generatePKCS10CertificateRequest(String dn, PublicKey publicKey, PrivateKey privateKey)
			throws CertificateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String generatePKCS10CertificateRequestString(String dn, PublicKey publicKey,
			PrivateKey privateKey) throws CertificateException {
		// TODO Auto-generated method stub
		return null;
	}

}

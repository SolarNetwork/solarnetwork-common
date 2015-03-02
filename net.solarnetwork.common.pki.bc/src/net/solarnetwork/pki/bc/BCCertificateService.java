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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import javax.security.auth.x500.X500Principal;
import net.solarnetwork.support.CertificateException;
import net.solarnetwork.support.CertificateService;
import net.solarnetwork.support.CertificationAuthorityService;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX500NameUtil;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.OperatorException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.util.io.pem.PemWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bouncy Castle implementation of {@link CertificateService}.
 * 
 * @author matt
 * @version 1.0
 */
public class BCCertificateService implements CertificateService, CertificationAuthorityService {

	private final AtomicLong counter = new AtomicLong(System.currentTimeMillis());

	private int certificateExpireDays = 730;
	private int authorityExpireDays = 7300;
	private String signatureAlgorithm = "SHA256WithRSA";

	private final Logger log = LoggerFactory.getLogger(getClass());

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
	public X509Certificate generateCertificationAuthorityCertificate(String dn, PublicKey publicKey,
			PrivateKey privateKey) {
		X500Principal issuer = new X500Principal(dn);
		Date now = new Date();
		Date expire = new Date(now.getTime() + (1000L * 60L * 60L * 24L * authorityExpireDays));
		JcaX509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(issuer,
				new BigInteger("0"), now, expire, issuer, publicKey);
		JcaContentSignerBuilder signerBuilder = new JcaContentSignerBuilder(signatureAlgorithm);
		DefaultDigestAlgorithmIdentifierFinder digestAlgFinder = new DefaultDigestAlgorithmIdentifierFinder();
		ContentSigner signer;
		try {
			DigestCalculatorProvider digestCalcProvider = new JcaDigestCalculatorProviderBuilder()
					.setProvider(new BouncyCastleProvider()).build();
			JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils(
					digestCalcProvider.get(digestAlgFinder.find("SHA-256")));
			builder.addExtension(X509Extension.basicConstraints, true, new BasicConstraints(true));
			builder.addExtension(X509Extension.subjectKeyIdentifier, false,
					extUtils.createSubjectKeyIdentifier(publicKey));
			builder.addExtension(X509Extension.keyUsage, true, new KeyUsage(KeyUsage.digitalSignature
					| KeyUsage.nonRepudiation | KeyUsage.keyCertSign | KeyUsage.cRLSign));
			builder.addExtension(X509Extension.authorityKeyIdentifier, false,
					extUtils.createAuthorityKeyIdentifier(publicKey));

			signer = signerBuilder.build(privateKey);
		} catch ( OperatorCreationException e ) {
			log.error("Error generating CA certificate [{}]", dn, e);
			throw new CertificateException("Error signing CA certificate", e);
		} catch ( CertIOException e ) {
			log.error("Error generating CA certificate [{}]", dn, e);
			throw new CertificateException("Error signing CA certificate", e);
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
	public X509Certificate signCertificate(String csrPEM, X509Certificate caCert, PrivateKey privateKey)
			throws CertificateException {
		if ( !csrPEM.matches("(?is)^\\s*-----BEGIN.*") ) {
			// let's throw in the guards
			csrPEM = "-----BEGIN CERTIFICATE REQUEST-----\n" + csrPEM
					+ "\n-----END CERTIFICATE REQUEST-----\n";
		}
		PemReader reader = null;
		try {
			reader = new PemReader(new StringReader(csrPEM));
			PemObject pemObj = reader.readPemObject();
			log.debug("Parsed PEM type {}", pemObj.getType());
			PKCS10CertificationRequest csr = new PKCS10CertificationRequest(pemObj.getContent());

			Date now = new Date();
			Date expire = new Date(now.getTime() + (1000L * 60L * 60L * 24L * certificateExpireDays));
			X509v3CertificateBuilder builder = new X509v3CertificateBuilder(
					JcaX500NameUtil.getIssuer(caCert), new BigInteger(String.valueOf(counter
							.incrementAndGet())), now, expire, csr.getSubject(),
					csr.getSubjectPublicKeyInfo());

			JcaContentSignerBuilder signerBuilder = new JcaContentSignerBuilder(signatureAlgorithm);
			ContentSigner signer;
			DefaultDigestAlgorithmIdentifierFinder digestAlgFinder = new DefaultDigestAlgorithmIdentifierFinder();
			try {
				DigestCalculatorProvider digestCalcProvider = new JcaDigestCalculatorProviderBuilder()
						.setProvider(new BouncyCastleProvider()).build();
				JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils(
						digestCalcProvider.get(digestAlgFinder.find("SHA-256")));
				builder.addExtension(X509Extension.basicConstraints, false, new BasicConstraints(false));
				builder.addExtension(X509Extension.subjectKeyIdentifier, false,
						extUtils.createSubjectKeyIdentifier(csr.getSubjectPublicKeyInfo()));
				builder.addExtension(X509Extension.authorityKeyIdentifier, false,
						extUtils.createAuthorityKeyIdentifier(caCert));

				signer = signerBuilder.build(privateKey);
			} catch ( OperatorException e ) {
				log.error("Error signing CSR {}", csr.getSubject(), e);
				throw new CertificateException("Error signing CSR" + csr.getSubject() + ": "
						+ e.getMessage());
			} catch ( CertificateEncodingException e ) {
				log.error("Error signing CSR {}", csr.getSubject().toString(), e);
				throw new CertificateException("Error signing CSR" + csr.getSubject() + ": "
						+ e.getMessage());
			}

			X509CertificateHolder holder = builder.build(signer);
			JcaX509CertificateConverter converter = new JcaX509CertificateConverter();
			try {
				return converter.getCertificate(holder);
			} catch ( java.security.cert.CertificateException e ) {
				throw new CertificateException("Error creating certificate", e);
			}
		} catch ( IOException e ) {
			throw new CertificateException("Error signing CSR", e);
		} finally {
			if ( reader != null ) {
				try {
					reader.close();
				} catch ( IOException e2 ) {
					log.warn("IOException closing PemReader", e2);
				}
			}
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

	private void orderCertificateChain(Map<X500Principal, X509Certificate> map,
			List<X509Certificate> results, X509Certificate c) {
		X509Certificate parent = map.get(c.getIssuerX500Principal());
		if ( parent != null ) {
			orderCertificateChain(map, results, parent);
		}

		// find parent in results, or else add to end
		for ( ListIterator<X509Certificate> itr = results.listIterator(); itr.hasNext(); ) {
			X509Certificate p = itr.next();
			if ( p.getSubjectDN().equals(c.getIssuerDN()) ) {
				itr.previous();
				itr.add(c);
				break;
			}
		}
		map.remove(c.getSubjectX500Principal());
	}

	private void orderCertificateChain(Map<X500Principal, X509Certificate> map,
			List<X509Certificate> results) {
		while ( map.size() > 0 ) {
			orderCertificateChain(map, results, map.values().iterator().next());
		}
	}

	@Override
	public X509Certificate[] parsePKCS7CertificateChainString(String pem) throws CertificateException {
		if ( !pem.matches("(?is)^\\s*-----BEGIN.*") ) {
			// let's throw in the guards
			pem = "-----BEGIN CERTIFICATE CHAIN-----\n" + pem + "\n-----END CERTIFICATE CHAIN-----\n";
		}
		PemReader reader = new PemReader(new StringReader(pem));
		List<X509Certificate> results = new ArrayList<X509Certificate>(3);
		try {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			PemObject pemObj = reader.readPemObject();
			log.debug("Parsed PEM type {}", pemObj.getType());
			Collection<? extends Certificate> certs = cf.generateCertificates(new ByteArrayInputStream(
					pemObj.getContent()));

			// OK barf, generateCertificates() and even CertPath doesn't return the chain in order
			// (see http://bugs.sun.com/view_bug.do?bug_id=6238093; but we can't use the Sun-specific
			// workaround listed there). So let's try to order them ourselves
			Map<X500Principal, X509Certificate> map = new LinkedHashMap<X500Principal, X509Certificate>();
			for ( Certificate c : certs ) {
				X509Certificate x509 = (X509Certificate) c;
				if ( x509.getIssuerDN().equals(x509.getSubjectDN()) ) {
					// root CA
					results.add(x509);
				} else {
					map.put(x509.getSubjectX500Principal(), x509);
				}
			}
			if ( results.size() == 0 ) {
				// no root, just add everything to list
				results.addAll(map.values());
			} else {
				orderCertificateChain(map, results);
			}
		} catch ( IOException e ) {
			throw new CertificateException("Error reading certificate", e);
		} catch ( java.security.cert.CertificateException e ) {
			throw new CertificateException("Error loading CertificateFactory", e);
		} finally {
			try {
				reader.close();
			} catch ( IOException e ) {
				// ignore me
			}
		}
		return results.toArray(new X509Certificate[results.size()]);
	}

	@Override
	public String generatePKCS7CertificateChainString(X509Certificate[] chain)
			throws CertificateException {
		try {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			List<X509Certificate> chainList = Arrays.asList(chain);
			CertPath path = cf.generateCertPath(chainList);
			StringWriter out = new StringWriter();
			PemWriter writer = new PemWriter(out);
			PemObject pemObj = new PemObject("CERTIFICATE" + (chain.length > 1 ? " CHAIN" : ""),
					path.getEncoded("PKCS7"));
			writer.writeObject(pemObj);
			writer.flush();
			writer.close();
			out.close();
			String result = out.toString();
			log.debug("Generated cert chain:\n{}", result);
			return result;
		} catch ( IOException e ) {
			throw new CertificateException("Error generating PKCS#7 chain", e);
		} catch ( java.security.cert.CertificateException e ) {
			throw new CertificateException("Error generating PKCS#7 chain", e);
		}
	}

	public void setCertificateExpireDays(int certificateExpireDays) {
		this.certificateExpireDays = certificateExpireDays;
	}

	public void setSignatureAlgorithm(String signatureAlgorithm) {
		this.signatureAlgorithm = signatureAlgorithm;
	}

	public void setAuthorityExpireDays(int authorityExpireDays) {
		this.authorityExpireDays = authorityExpireDays;
	}

}

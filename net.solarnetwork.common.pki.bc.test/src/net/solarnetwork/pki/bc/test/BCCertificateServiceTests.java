/* ==================================================================
 * BCCertificateServiceTest.java - Dec 5, 2012 11:59:36 AM
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

package net.solarnetwork.pki.bc.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import java.io.InputStreamReader;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;
import net.solarnetwork.pki.bc.BCCertificateService;

/**
 * Test cases for the {@link BCCertificateService} class.
 * 
 * @author matt
 * @version 1.0
 */
public class BCCertificateServiceTests {

	private static final String TEST_DN = "UID=1, O=SolarNetwork";
	private static final String TEST_CA_DN = "CN=Test CA, O=SolarTest";
	private BCCertificateService service;
	private PublicKey publicKey;
	private PrivateKey privateKey;

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Before
	public void setup() {
		service = new BCCertificateService();
		KeyPairGenerator keyGen;
		try {
			keyGen = KeyPairGenerator.getInstance("RSA");
		} catch ( NoSuchAlgorithmException e ) {
			throw new RuntimeException(e);
		}
		keyGen.initialize(2048, new SecureRandom());
		KeyPair keypair = keyGen.generateKeyPair();
		publicKey = keypair.getPublic();
		privateKey = keypair.getPrivate();
	}

	@Test
	public void generateSelfSignedCertificate() {
		X509Certificate cert = service.generateCertificate(TEST_DN, publicKey, privateKey);
		assertNotNull(cert);
		log.debug("Got cert: {}", cert);
	}

	@Test
	public void generateCSR() {
		X509Certificate cert = service.generateCertificate(TEST_DN, publicKey, privateKey);
		assertNotNull(cert);
		String csr = service.generatePKCS10CertificateRequestString(cert, privateKey);
		assertNotNull(csr);
		log.debug("Got CSR:\n{}", csr);
	}

	@Test
	public void parsePKCS7() throws Exception {
		String pkcs7Pem = FileCopyUtils.copyToString(
				new InputStreamReader(getClass().getResourceAsStream("test-pkcs7-chain.pem"), "UTF-8"));
		X509Certificate[] chain = service.parsePKCS7CertificateChainString(pkcs7Pem);
		assertNotNull(chain);
		assertEquals(3, chain.length);
	}

	@Test
	public void createCACertificate() throws Exception {
		X509Certificate cert = service.generateCertificationAuthorityCertificate(TEST_CA_DN, publicKey,
				privateKey);
		assertEquals("Is a CA", Integer.MAX_VALUE, cert.getBasicConstraints()); // should be a CA
		assertEquals("Self signed", cert.getIssuerX500Principal(), cert.getSubjectX500Principal());
	}

	@Test
	public void signCertificate() throws Exception {
		X509Certificate cert = service.generateCertificate(TEST_DN, publicKey, privateKey);
		String csr = service.generatePKCS10CertificateRequestString(cert, privateKey);

		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(2048, new SecureRandom());
		KeyPair caKeypair = keyGen.generateKeyPair();
		X509Certificate caCert = service.generateCertificationAuthorityCertificate(TEST_CA_DN,
				caKeypair.getPublic(), caKeypair.getPrivate());

		X509Certificate signed = service.signCertificate(csr, caCert, caKeypair.getPrivate());
		assertEquals("Issuer", caCert.getSubjectX500Principal(), signed.getIssuerX500Principal());
		assertEquals("Subject", cert.getSubjectX500Principal(), signed.getSubjectX500Principal());
	}

	@Test
	public void renewCertificate() throws Exception {
		// GIVEN
		X509Certificate cert = service.generateCertificate(TEST_DN, publicKey, privateKey);
		String csr = service.generatePKCS10CertificateRequestString(cert, privateKey);

		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(2048, new SecureRandom());
		KeyPair caKeypair = keyGen.generateKeyPair();
		X509Certificate caCert = service.generateCertificationAuthorityCertificate(TEST_CA_DN,
				caKeypair.getPublic(), caKeypair.getPrivate());

		X509Certificate signed = service.signCertificate(csr, caCert, caKeypair.getPrivate());

		// WHEN
		final String pkcs7 = service
				.generatePKCS7CertificateChainString(new X509Certificate[] { signed });
		X509Certificate renewed = service.signCertificate(pkcs7, caCert, privateKey);

		// THEN
		assertEquals("Issuer", caCert.getSubjectX500Principal(), renewed.getIssuerX500Principal());
		assertEquals("Subject", cert.getSubjectX500Principal(), renewed.getSubjectX500Principal());
		assertNotSame("Renewed certificate is new certificate", signed, renewed);
	}

}

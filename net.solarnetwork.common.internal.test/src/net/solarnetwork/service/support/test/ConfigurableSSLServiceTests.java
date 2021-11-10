/* ==================================================================
 * ConfigurableSSLServiceTests.java - 2/04/2017 10:58:37 AM
 * 
 * Copyright 2007-2017 SolarNetwork.net Dev Team
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

package net.solarnetwork.service.support.test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLSocketFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import net.solarnetwork.pki.bc.BCCertificateService;
import net.solarnetwork.service.support.ConfigurableSSLService;

/**
 * Unit tests for the {@link ConfigurableSSLService} class.
 * 
 * @author matt
 * @version 1.0
 */
public class ConfigurableSSLServiceTests {

	private static final String TEST_KEY_STORE_PASSWORD = "secret!!";
	private static final String TEST_KEY_STORE_PATH = "var/test-keystore.jks";

	private static final String TEST_TRUST_STORE_PASSWORD = "trustme!";
	private static final String TEST_TRUST_STORE_PATH = "var/test-truststore.jks";

	private static final String TEST_CA_DN = "CN=Test CA, O=SolarTest";
	private static final String TEST_CA_ALIAS = "ca";

	private BCCertificateService certService;
	private KeyPairGenerator keyPairGenerator;
	private KeyPair caKeyPair;

	private TestConfigurableSSLService service;

	private static final class TestConfigurableSSLService extends ConfigurableSSLService {

		private KeyStore getKeyStore() {
			return loadKeyStore();
		}

		private KeyStore getTrustStore() {
			return loadTrustStore();
		}

	}

	@Before
	public void setup() {
		certService = new BCCertificateService();
		try {
			keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		} catch ( NoSuchAlgorithmException e ) {
			throw new RuntimeException(e);
		}
		keyPairGenerator.initialize(2048, new SecureRandom());

		deleteFile(TEST_KEY_STORE_PATH);
		deleteFile(TEST_TRUST_STORE_PATH);

		service = new TestConfigurableSSLService();
		service.setKeyStorePassword(TEST_KEY_STORE_PASSWORD);
		service.setTrustStorePassword(TEST_TRUST_STORE_PASSWORD);
		service.setKeyStorePath(TEST_KEY_STORE_PATH);
		service.setTrustStorePath(TEST_TRUST_STORE_PATH);
	}

	private static final void deleteFile(final String path) {
		File f = new File(path);
		if ( f.exists() ) {
			f.delete();
		}
	}

	protected KeyStore loadKeyStore(String keyStorePath, String passwd) {
		File ksFile = new File(keyStorePath);
		InputStream in = null;
		try {
			if ( ksFile.isFile() ) {
				in = new BufferedInputStream(new FileInputStream(ksFile));
			}
			return ConfigurableSSLService.loadKeyStore(KeyStore.getDefaultType(), in, passwd);
		} catch ( IOException e ) {
			throw new RuntimeException("Error opening keystore file " + keyStorePath, e);
		}
	}

	private void saveKeyStore(KeyStore keyStore, String keyStorePath, String passwd) {
		if ( keyStore == null ) {
			return;
		}
		File ksFile = new File(keyStorePath);
		File ksDir = ksFile.getParentFile();
		if ( !ksDir.isDirectory() && !ksDir.mkdirs() ) {
			throw new RuntimeException("Unable to create KeyStore directory: " + ksFile.getParent());
		}

		try {
			ConfigurableSSLService.saveKeyStore(keyStore, passwd,
					new BufferedOutputStream(new FileOutputStream(ksFile)));
		} catch ( IOException e ) {
			throw new RuntimeException("Error saving keystore to " + ksFile.getPath(), e);
		}
	}

	private void createCA(KeyStore keyStore, KeyStore trustStore) {
		caKeyPair = keyPairGenerator.generateKeyPair();
		X509Certificate cert = certService.generateCertificationAuthorityCertificate(TEST_CA_DN,
				caKeyPair.getPublic(), caKeyPair.getPrivate());
		try {
			trustStore.setCertificateEntry(TEST_CA_ALIAS, cert);
			keyStore.setKeyEntry(TEST_CA_ALIAS, caKeyPair.getPrivate(),
					TEST_KEY_STORE_PASSWORD.toCharArray(), new Certificate[] { cert });
		} catch ( KeyStoreException e ) {
			throw new RuntimeException(e);
		}
	}

	private void setupCA() {
		KeyStore keyStore = loadKeyStore(TEST_KEY_STORE_PATH, TEST_KEY_STORE_PASSWORD);
		KeyStore trustStore = loadKeyStore(TEST_TRUST_STORE_PATH, TEST_TRUST_STORE_PASSWORD);

		createCA(keyStore, trustStore);
		saveKeyStore(keyStore, TEST_KEY_STORE_PATH, TEST_KEY_STORE_PASSWORD);
		saveKeyStore(trustStore, TEST_TRUST_STORE_PATH, TEST_TRUST_STORE_PASSWORD);
	}

	private void verifyCA() {
		try {
			KeyStore keyStore = service.getKeyStore();
			Assert.assertNotNull("Service keystore", keyStore);
			Key key = keyStore.getKey(TEST_CA_ALIAS, TEST_KEY_STORE_PASSWORD.toCharArray());
			Assert.assertNotNull("CA private key", key);
			Assert.assertTrue("CA key is PrivateKey", key instanceof PrivateKey);
			Assert.assertArrayEquals("CA private key", caKeyPair.getPrivate().getEncoded(),
					key.getEncoded());

			keyStore = service.getTrustStore();
			Assert.assertNotNull("Service truststore", keyStore);
			Certificate cert = keyStore.getCertificate(TEST_CA_ALIAS);
			Assert.assertNotNull("CA cert", cert);
			Assert.assertArrayEquals("CA cert public key", caKeyPair.getPublic().getEncoded(),
					cert.getPublicKey().getEncoded());
		} catch ( GeneralSecurityException e ) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void createContext() {
		setupCA();
		SSLSocketFactory sf = service.getSSLSocketFactory();
		Assert.assertNotNull("SSLSocketFactory", sf);
		verifyCA();
	}

	@Test
	public void createSocketFactorySingleton() {
		setupCA();
		SSLSocketFactory sf = service.getSSLSocketFactory();
		SSLSocketFactory sf2 = service.getSSLSocketFactory();
		Assert.assertSame("SSLSocketFactory is a singleton", sf, sf2);
	}

}

/* ==================================================================
 * ConfigurableSSLService.java - 2/04/2017 10:19:45 AM
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

package net.solarnetwork.service.support;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.util.Enumeration;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.solarnetwork.service.CertificateException;
import net.solarnetwork.service.SSLService;

/**
 * Basic implementation of {@link SSLService} that allows configuring a keystore
 * and truststore to use for the {@code SSLSocketFactory} returned by
 * {@link #getSSLSocketFactory()}.
 * 
 * @author matt
 * @version 2.0
 */
public class ConfigurableSSLService implements SSLService {

	/** The default value for the {@code keyStorePath} property. */
	public static final String DEFAULT_KEY_STORE_PATH = "conf/tls/keystore.jks";

	/** The default value for the {@code trustStorePath} property. */
	public static final String DEFAULT_TRUST_STORE_PATH = "conf/tls/trust.jks";

	/** The default password used for all configurable password properties. */
	public static final String DEFAULT_PASSWORD = "changeit";

	private String keyStorePath = DEFAULT_KEY_STORE_PATH;
	private String keyStorePassword = DEFAULT_PASSWORD;
	private String trustStorePath = DEFAULT_TRUST_STORE_PATH;
	private String trustStorePassword = DEFAULT_PASSWORD;
	private String jreTrustStorePassword = DEFAULT_PASSWORD;

	private SSLSocketFactory socketFactory;

	/** A class-level logger to use. */
	protected final Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * Load a keystore from an {@code InputStream}.
	 * 
	 * @param type
	 *        The keystore type, e.g. {@code KeyStore.getDefaultType()}.
	 * @param in
	 *        The stream to load from.
	 * @param password
	 *        The keystore password to use.
	 * @return The keystore.
	 * @throws CertificateException
	 *         if any error occurs
	 */
	public static final KeyStore loadKeyStore(String type, InputStream in, String password) {
		if ( password == null ) {
			password = "";
		}
		KeyStore keyStore = null;
		try {
			keyStore = KeyStore.getInstance(type);
			keyStore.load(in, (password != null ? password.toCharArray() : null));
			return keyStore;
		} catch ( GeneralSecurityException e ) {
			throw new CertificateException("Error loading certificate key store", e);
		} catch ( IOException e ) {
			String msg;
			if ( e.getCause() instanceof UnrecoverableKeyException ) {
				msg = "Invalid password loading key store";
			} else {
				msg = "Error loading certificate key store";
			}
			throw new CertificateException(msg, e);
		} finally {
			if ( in != null ) {
				try {
					in.close();
				} catch ( IOException e ) {
					// ignore this one
				}
			}
		}
	}

	/**
	 * Serialize a {@code KeyStore} to an output stream.
	 * 
	 * @param keyStore
	 *        The keystore to serialize.
	 * @param password
	 *        The password to use.
	 * @param out
	 *        The stream to write to.
	 * @throws CertificateException
	 *         if any error occurs
	 */
	public static final void saveKeyStore(KeyStore keyStore, String password, OutputStream out) {
		if ( password == null ) {
			password = "";
		}
		try {
			keyStore.store(out, password.toCharArray());
		} catch ( KeyStoreException e ) {
			throw new CertificateException("Error saving certificate key store", e);
		} catch ( NoSuchAlgorithmException e ) {
			throw new CertificateException("Error saving certificate key store", e);
		} catch ( java.security.cert.CertificateException e ) {
			throw new CertificateException("Error saving certificate key store", e);
		} catch ( IOException e ) {
			throw new CertificateException("Error saving certificate key store", e);
		} finally {
			if ( out != null ) {
				try {
					out.flush();
					out.close();
				} catch ( IOException e ) {
					throw new CertificateException("Error closing KeyStore stream", e);
				}
			}
		}
	}

	protected synchronized KeyStore loadKeyStore() {
		File ksFile = new File(keyStorePath);
		InputStream in = null;
		String passwd = getKeyStorePassword();
		try {
			if ( ksFile.isFile() ) {
				in = new BufferedInputStream(new FileInputStream(ksFile));
			}
			return loadKeyStore(KeyStore.getDefaultType(), in, passwd);
		} catch ( IOException e ) {
			throw new CertificateException("Error opening file " + keyStorePath, e);
		}
	}

	protected synchronized KeyStore loadTrustStore() {
		// first load in JDK trust store
		File jdkTrustStoreFile = new File(System.getProperty("java.home"), "lib/security/cacerts");
		KeyStore ks = null;
		InputStream in = null;
		if ( jdkTrustStoreFile.canRead() ) {
			try {
				in = new BufferedInputStream(new FileInputStream(jdkTrustStoreFile));
			} catch ( FileNotFoundException e ) {
				// shouldn't really get here after canRead()
			}
		}
		ks = loadKeyStore(KeyStore.getDefaultType(), in, jreTrustStorePassword);

		// now custom trust store
		File snTrustStoreFile = new File(trustStorePath);
		if ( snTrustStoreFile.canRead() ) {
			KeyStore snTrustStore = null;
			try {
				in = new BufferedInputStream(new FileInputStream(snTrustStoreFile));
				snTrustStore = loadKeyStore(KeyStore.getDefaultType(), in, trustStorePassword);
				Enumeration<String> aliases = snTrustStore.aliases();
				while ( aliases.hasMoreElements() ) {
					String alias = aliases.nextElement();
					Certificate cert = snTrustStore.getCertificate(alias);
					if ( cert != null ) {
						ks.setCertificateEntry(alias, cert);
					}
				}
			} catch ( FileNotFoundException e ) {
				// shouldn't really get here after canRead()
			} catch ( KeyStoreException e ) {
				log.warn("Error processing trusted certs in {}: {}", snTrustStoreFile, e.getMessage());
			}
		}

		return ks;
	}

	/**
	 * Clear any cached {@code SSLSocketFactory} so that a subsequent call to
	 * {@link #getSSLSocketFactory()} returns a new instance.
	 */
	protected synchronized void resetSocketFactory() {
		socketFactory = null;
	}

	@Override
	public TrustManagerFactory getTrustManagerFactory() {
		KeyStore trustStore = loadTrustStore();
		try {
			TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("PKIX");
			trustManagerFactory.init(trustStore);
			return trustManagerFactory;
		} catch ( NoSuchAlgorithmException | KeyStoreException e ) {
			throw new CertificateException("Error creating TrustManagerFactory: " + e.toString(), e);
		}
	}

	@Override
	public KeyManagerFactory getKeyManagerFactory() {
		try {
			File ksFile = new File(keyStorePath);
			if ( ksFile.isFile() ) {
				KeyStore keyStore = loadKeyStore();
				KeyManagerFactory keyManagerFactory = KeyManagerFactory
						.getInstance(KeyManagerFactory.getDefaultAlgorithm());
				keyManagerFactory.init(keyStore, getKeyStorePassword().toCharArray());
				return keyManagerFactory;
			}
		} catch ( NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException e ) {
			throw new CertificateException("Error creating KeyManagerFactory: " + e.toString(), e);
		}
		return null;
	}

	@Override
	public synchronized SSLSocketFactory getSSLSocketFactory() {
		if ( socketFactory == null ) {
			try {
				TrustManagerFactory trustManagerFactory = getTrustManagerFactory();

				X509TrustManager x509TrustManager = null;
				for ( TrustManager trustManager : trustManagerFactory.getTrustManagers() ) {
					if ( trustManager instanceof X509TrustManager ) {
						x509TrustManager = (X509TrustManager) trustManager;
						break;
					}
				}

				if ( x509TrustManager == null ) {
					throw new CertificateException("No X509 TrustManager available");
				}

				KeyManager[] keyManagers = null;
				KeyManagerFactory keyManagerFactory = getKeyManagerFactory();
				if ( keyManagerFactory != null ) {
					for ( KeyManager keyManager : keyManagerFactory.getKeyManagers() ) {
						if ( keyManager instanceof X509KeyManager ) {
							keyManagers = new KeyManager[] { keyManager };
						}
					}
				}

				SSLContext sslContext = SSLContext.getInstance("TLS");
				sslContext.init(keyManagers, new TrustManager[] { x509TrustManager }, null);
				socketFactory = sslContext.getSocketFactory();

			} catch ( NoSuchAlgorithmException e ) {
				throw new CertificateException("Error creating SSLContext", e);
			} catch ( KeyManagementException e ) {
				throw new CertificateException("Error creating SSLContext", e);
			}
		}
		return socketFactory;
	}

	/**
	 * Get the path to the keystore.
	 * 
	 * @return the keyStorePath
	 */
	public String getKeyStorePath() {
		return keyStorePath;
	}

	/**
	 * Set the path to the keystore.
	 * 
	 * @param keyStorePath
	 *        the keyStorePath to set
	 */
	public void setKeyStorePath(String keyStorePath) {
		this.keyStorePath = keyStorePath;
	}

	/**
	 * Get the path to the truststore.
	 * 
	 * @return the trustStorePath
	 */
	public String getTrustStorePath() {
		return trustStorePath;
	}

	/**
	 * Set the path to the truststore.
	 * 
	 * @param trustStorePath
	 *        the trustStorePath to set
	 */
	public void setTrustStorePath(String trustStorePath) {
		this.trustStorePath = trustStorePath;
	}

	/**
	 * Get the truststore password.
	 * 
	 * @return the trustStorePassword
	 */
	protected String getTrustStorePassword() {
		return trustStorePassword;
	}

	/**
	 * Set the truststore password.
	 * 
	 * @param trustStorePassword
	 *        the trustStorePassword to set
	 */
	public void setTrustStorePassword(String trustStorePassword) {
		this.trustStorePassword = trustStorePassword;
	}

	/**
	 * Get the JRE truststore password.
	 * 
	 * @return the jreTrustStorePassword
	 */
	protected String getJreTrustStorePassword() {
		return jreTrustStorePassword;
	}

	/**
	 * Set the JRE truststore password.
	 * 
	 * @param jreTrustStorePassword
	 *        the jreTrustStorePassword to set
	 */
	public void setJreTrustStorePassword(String jreTrustStorePassword) {
		this.jreTrustStorePassword = jreTrustStorePassword;
	}

	/**
	 * Get the keystore password.
	 * 
	 * @return The keystore password.
	 */
	protected String getKeyStorePassword() {
		String password = keyStorePassword;
		if ( password != null && password.length() > 0 ) {
			return password;
		}
		return "";
	}

	/**
	 * Set the keystore password.
	 * 
	 * @param keyStorePassword
	 *        the keyStorePassword to set
	 */
	public void setKeyStorePassword(String keyStorePassword) {
		this.keyStorePassword = keyStorePassword;
	}

}

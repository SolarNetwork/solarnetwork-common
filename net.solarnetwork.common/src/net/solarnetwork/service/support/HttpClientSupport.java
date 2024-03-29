/* ==================================================================
 * HttpClientSupport.java - 7/04/2017 6:02:42 PM
 * 
 * Copyright 2017 SolarNetwork.net Dev Team
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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URLConnection;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.solarnetwork.io.UrlUtils;
import net.solarnetwork.service.OptionalService;
import net.solarnetwork.service.SSLService;

/**
 * Basic support for HTTP client actions.
 * 
 * @author matt
 * @version 1.3
 * @since 1.35
 */
public class HttpClientSupport {

	/** A HTTP Accept header value for any text type. */
	public static final String ACCEPT_TEXT = UrlUtils.ACCEPT_TEXT;

	/** A HTTP Accept header value for a JSON type. */
	public static final String ACCEPT_JSON = UrlUtils.ACCEPT_JSON;

	/** The default value for the {@code connectionTimeout} property. */
	public static final int DEFAULT_CONNECTION_TIMEOUT = 15000;

	/** The HTTP method GET. */
	public static final String HTTP_METHOD_GET = UrlUtils.HTTP_METHOD_GET;

	/** The HTTP method POST. */
	public static final String HTTP_METHOD_POST = UrlUtils.HTTP_METHOD_POST;

	private int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
	private OptionalService<SSLService> sslService = null;

	/** A class-level logger. */
	protected final Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * Constructor.
	 */
	public HttpClientSupport() {
		super();
	}

	/**
	 * Get an InputStream from a URLConnection response, handling compression.
	 * 
	 * <p>
	 * This method handles decompressing the response if the encoding is set to
	 * {@code gzip} or {@code deflate}.
	 * </p>
	 * 
	 * @param conn
	 *        the URLConnection
	 * @return the InputStream
	 * @throws IOException
	 *         if any IO error occurs
	 */
	protected InputStream getInputStreamFromURLConnection(URLConnection conn) throws IOException {
		return UrlUtils.getInputStreamFromURLConnection(log, conn);
	}

	/**
	 * Get a Reader for a Unicode encoded URL connection response.
	 * 
	 * <p>
	 * This calls {@link #getInputStreamFromURLConnection(URLConnection)} so
	 * compressed responses are handled appropriately.
	 * </p>
	 * 
	 * @param conn
	 *        the URLConnection
	 * @return the Reader
	 * @throws IOException
	 *         if an IO error occurs
	 */
	protected Reader getUnicodeReaderFromURLConnection(URLConnection conn) throws IOException {
		return UrlUtils.getUnicodeReaderFromURLConnection(log, conn);
	}

	/**
	 * Get a URLConnection for a specific URL and HTTP method.
	 * 
	 * <p>
	 * This defaults to the {@link #ACCEPT_TEXT} accept value.
	 * </p>
	 * 
	 * @param url
	 *        the URL to connect to
	 * @param httpMethod
	 *        the HTTP method
	 * @return the URLConnection
	 * @throws IOException
	 *         if any IO error occurs
	 * @see #getURLConnection(String, String, String)
	 */
	protected URLConnection getURLConnection(String url, String httpMethod) throws IOException {
		return getURLConnection(url, httpMethod, "text/*");
	}

	/**
	 * Get a URLConnection for a specific URL and HTTP method.
	 * 
	 * <p>
	 * If the httpMethod equals {@code POST} then the connection's
	 * {@code doOutput} property will be set to {@literal true}, otherwise it
	 * will be set to {@literal false}. The {@code doInput} property is always
	 * set to {@literal true}.
	 * </p>
	 * 
	 * <p>
	 * This method also sets up the request property
	 * {@code Accept-Encoding: gzip,deflate} so the response can be compressed.
	 * The {@link #getInputStreamFromURLConnection(URLConnection)} automatically
	 * handles compressed responses.
	 * </p>
	 * 
	 * <p>
	 * If the {@link #getSslService()} property is configured and the URL
	 * represents an HTTPS connection, then that factory will be used to for the
	 * connection.
	 * </p>
	 * 
	 * @param url
	 *        the URL to connect to
	 * @param httpMethod
	 *        the HTTP method
	 * @param accept
	 *        the HTTP Accept header value
	 * @return the URLConnection
	 * @throws IOException
	 *         if any IO error occurs
	 */
	protected URLConnection getURLConnection(String url, String httpMethod, String accept)
			throws IOException {
		return UrlUtils.getURLConnection(url, httpMethod, accept, connectionTimeout, sslService());
	}

	/**
	 * Append a URL-escaped key/value pair to a string buffer.
	 * 
	 * @param buf
	 *        the buffer
	 * @param key
	 *        the key
	 * @param value
	 *        the value
	 */
	protected void appendXWWWFormURLEncodedValue(StringBuilder buf, String key, Object value) {
		UrlUtils.appendURLEncodedValue(buf, key, value);
	}

	/**
	 * Encode a map of data into a string suitable for posting to a web server
	 * as the content type {@code application/x-www-form-urlencoded}. Arrays and
	 * Collections of values are supported as well.
	 * 
	 * @param data
	 *        the map of data to encode
	 * @return the encoded data, or an empty string if nothing to encode
	 */
	protected String xWWWFormURLEncoded(Map<String, ?> data) {
		return UrlUtils.urlEncoded(data);
	}

	/**
	 * HTTP POST data as {@code application/x-www-form-urlencoded} (e.g. a web
	 * form) to a URL.
	 * 
	 * @param url
	 *        the URL to post to
	 * @param accept
	 *        the value to use for the Accept HTTP header
	 * @param data
	 *        the data to encode and send as the body of the HTTP POST
	 * @return the URLConnection after the post data has been sent
	 * @throws IOException
	 *         if any IO error occurs
	 * @throws RuntimeException
	 *         if the HTTP response code is not within the 200 - 299 range
	 */
	protected URLConnection postXWWWFormURLEncodedData(String url, String accept, Map<String, ?> data)
			throws IOException {
		return UrlUtils.postXWWWFormURLEncodedData(log, url, accept, data, connectionTimeout,
				sslService());
	}

	/**
	 * HTTP POST data as {@code application/x-www-form-urlencoded} (e.g. a web
	 * form) to a URL and return the response body as a string.
	 * 
	 * <p>
	 * This method accepts text and JSON responses.
	 * </p>
	 * 
	 * @param url
	 *        the URL to post to
	 * @param data
	 *        the data to encode and send as the body of the HTTP POST
	 * @return the response body as a String
	 * @throws IOException
	 *         if any IO error occurs
	 * @throws RuntimeException
	 *         if the HTTP response code is not within the 200 - 299 range
	 * @see #postXWWWFormURLEncodedData(String, String, Map)
	 */
	protected String postXWWWFormURLEncodedDataForString(String url, Map<String, ?> data)
			throws IOException {
		return UrlUtils.postXWWWFormURLEncodedDataForString(log, url, UrlUtils.ACCEPT_TEXT_AND_JSON,
				data, connectionTimeout, sslService());
	}

	private SSLService sslService() {
		OptionalService<SSLService> s = getSslService();
		return (s != null ? s.service() : null);
	}

	/**
	 * Set the connection timeout.
	 * 
	 * @param connectionTimeout
	 *        the timeout to set, in milliseconds
	 */
	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	/**
	 * Get the connection timeout.
	 * 
	 * @return the timeout, in milliseconds
	 */
	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	/**
	 * Get the SSL service.
	 * 
	 * @return the service
	 */
	public OptionalService<SSLService> getSslService() {
		return sslService;
	}

	/**
	 * Set the SSL service.
	 * 
	 * @param sslService
	 *        the service to set
	 */
	public void setSslService(OptionalService<SSLService> sslService) {
		this.sslService = sslService;
	}

}

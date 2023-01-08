/* ==================================================================
 * UrlUtils.java - 20/10/2019 3:55:43 pm
 * 
 * Copyright 2019 SolarNetwork.net Dev Team
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

package net.solarnetwork.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Map;
import java.util.zip.DeflaterInputStream;
import java.util.zip.GZIPInputStream;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;
import net.solarnetwork.service.SSLService;

/**
 * Utilities for supporting URL related tasks.
 * 
 * @author matt
 * @version 2.1
 * @since 1.54
 */
public final class UrlUtils {

	/** The {@literal UTF-8} character set name. */
	public static final String UTF8_CHARSET = "UTF-8";

	/** A HTTP Accept header value for any type. */
	public static final String ACCEPT_ANYTHING = "*/*";

	/** A HTTP Accept header value for any text type. */
	public static final String ACCEPT_TEXT = "text/*";

	/** A HTTP Accept header value for a JSON type. */
	public static final String ACCEPT_JSON = "application/json,text/json";

	/** A HTTP Accept header value for any text and JSON type. */
	public static final String ACCEPT_TEXT_AND_JSON = "text/*,application/json";

	/** A HTTP Accept header value for an XML type. */
	public static final String ACCEPT_XML = "application/xml,text/xml";

	/** A HTTP Accept header value for any text and XML type. */
	public static final String ACCEPT_TEXT_AND_XML = "text/*,application/xml";

	/** A HTTP Accept header value for any text and XML type. */
	public static final String ACCEPT_TEXT_AND_JSON_AND_XML = "text/*,application/json,application/xml";

	/** The HTTP method GET. */
	public static final String HTTP_METHOD_GET = "GET";

	/** The HTTP method PATCH. */
	public static final String HTTP_METHOD_PATCH = "PATCH";

	/** The HTTP method POST. */
	public static final String HTTP_METHOD_POST = "POST";

	/** The HTTP method PUT. */
	public static final String HTTP_METHOD_PUT = "PUT";

	/** A class-level logger. */
	private static final Logger log = LoggerFactory.getLogger(UrlUtils.class);

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
	public static InputStream getInputStreamFromURLConnection(URLConnection conn) throws IOException {
		String enc = conn.getContentEncoding();
		String type = conn.getContentType();

		if ( conn instanceof HttpURLConnection ) {
			HttpURLConnection httpConn = (HttpURLConnection) conn;
			log.trace("HTTP {} {} response code: {}", httpConn.getRequestMethod(), httpConn.getURL(),
					httpConn.getResponseCode());
			if ( httpConn.getResponseCode() < 200 || httpConn.getResponseCode() > 299 ) {
				log.info("Non-200 HTTP response from {} {}: {}", conn.getURL(),
						httpConn.getRequestMethod(), httpConn.getResponseCode());
			}
		}

		log.trace("RESP content type [{}] encoded as [{}]", type, enc);
		InputStream is = conn.getInputStream();
		if ( "gzip".equalsIgnoreCase(enc) ) {
			is = new GZIPInputStream(is);
		} else if ( "deflate".equalsIgnoreCase(enc) ) {
			is = new DeflaterInputStream(is);
		}
		return is;
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
	public static Reader getUnicodeReaderFromURLConnection(URLConnection conn) throws IOException {
		return new BufferedReader(new UnicodeReader(getInputStreamFromURLConnection(conn), null));
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
	 * @param timeout
	 *        if greater than {@literal 0} then set as both the connection
	 *        timeout and read timeout, in milliseconds
	 * @param sslService
	 *        if provided and the URL represents an HTTPS URL, then
	 *        {@link SSLService#getSSLSocketFactory()} will be used as the
	 *        connection's {@code SSLSocketFactory}
	 * @return the URLConnection
	 * @throws IOException
	 *         if any IO error occurs
	 * @see #getURLConnection(String, String, String, int, SSLService)
	 */
	public static URLConnection getURLConnection(String url, String httpMethod, int timeout,
			SSLService sslService) throws IOException {
		return getURLConnection(url, httpMethod, ACCEPT_TEXT, timeout, sslService);
	}

	/**
	 * Get a {@link URLConnection} for a URL and HTTP method.
	 * 
	 * <p>
	 * This method is geared towards HTTP (and HTTPS) connections, but can be
	 * used for arbitrary URLs by passing {@literal null} for the HTTP-specific
	 * arguments.
	 * </p>
	 * 
	 * <p>
	 * If {@code httpMethod} equals {@literal PATCH}, {@literal POST}, or
	 * {@literal PUT} then the connection's {@code doOutput} property will be
	 * set to {@literal true}, otherwise it will be set to {@literal false}. The
	 * {@code doInput} property is always set to {@literal true}.
	 * </p>
	 * 
	 * <p>
	 * This method also sets up the request property
	 * {@code Accept-Encoding: gzip,deflate} so the response can be compressed.
	 * The {@link #getInputStreamFromURLConnection(URLConnection)} automatically
	 * handles compressed responses.
	 * </p>
	 * 
	 * @param url
	 *        the URL to connect to
	 * @param httpMethod
	 *        if provided and the URL is an HTTP URL, the HTTP request method to
	 *        set
	 * @param accept
	 *        if provided, an HTTP {@literal Accept} header value to set
	 * @param timeout
	 *        if greater than {@literal 0} then set as both the connection
	 *        timeout and read timeout, in milliseconds
	 * @param sslService
	 *        if provided and the URL represents an HTTPS URL, then
	 *        {@link SSLService#getSSLSocketFactory()} will be used as the
	 *        connection's {@code SSLSocketFactory}
	 * @return the URLConnection
	 * @throws IOException
	 *         if any IO error occurs
	 */
	public static URLConnection getURLConnection(String url, String httpMethod, String accept,
			int timeout, SSLService sslService) throws IOException {
		URL connUrl;
		try {
			connUrl = new URI(url).toURL();
		} catch ( URISyntaxException e ) {
			throw new IOException("Invalid URL [" + url + "]", e);
		}
		URLConnection conn = connUrl.openConnection();
		if ( httpMethod != null && conn instanceof HttpURLConnection ) {
			HttpURLConnection hConn = (HttpURLConnection) conn;
			hConn.setRequestMethod(httpMethod);
		}
		if ( sslService != null && conn instanceof HttpsURLConnection ) {
			SSLSocketFactory factory = sslService.getSSLSocketFactory();
			if ( factory != null ) {
				HttpsURLConnection hConn = (HttpsURLConnection) conn;
				hConn.setSSLSocketFactory(factory);
			}
		}
		if ( accept != null ) {
			conn.setRequestProperty("Accept", accept);
		}
		conn.setRequestProperty("Accept-Encoding", "gzip,deflate");
		conn.setDoInput(true);
		conn.setDoOutput(HTTP_METHOD_PATCH.equalsIgnoreCase(httpMethod)
				|| HTTP_METHOD_POST.equalsIgnoreCase(httpMethod)
				|| HTTP_METHOD_PUT.equalsIgnoreCase(httpMethod));
		if ( timeout > 0 ) {
			conn.setConnectTimeout(timeout);
			conn.setReadTimeout(timeout);
		}
		return conn;
	}

	/**
	 * Append a URL-escaped key/value pair to a character buffer.
	 * 
	 * @param <T>
	 *        the buffer type
	 * @param buf
	 *        the buffer to append to
	 * @param key
	 *        the key to append
	 * @param value
	 *        the value
	 */
	public static <T extends Appendable & CharSequence> void appendURLEncodedValue(T buf, String key,
			Object value) {
		if ( value == null ) {
			return;
		}
		try {
			if ( buf.length() > 0 ) {
				buf.append('&');
			}
			buf.append(URLEncoder.encode(key, UTF8_CHARSET)).append('=')
					.append(URLEncoder.encode(value.toString(), UTF8_CHARSET));
		} catch ( IOException e ) {
			// should not get here ever
			throw new RuntimeException(e);
		}
	}

	/**
	 * Encode a map of data into a string suitable for posting to a web server
	 * as the content type {@code application/x-www-form-urlencoded}.
	 * 
	 * <p>
	 * Arrays and collections of values are supported as well.
	 * </p>
	 * 
	 * @param data
	 *        the map of data to encode
	 * @return the encoded data, or an empty string if nothing to encode
	 */
	public static String urlEncoded(Map<String, ?> data) {
		if ( data == null || data.size() < 0 ) {
			return "";
		}
		StringBuilder buf = new StringBuilder();
		for ( Map.Entry<String, ?> me : data.entrySet() ) {
			Object val = me.getValue();
			if ( val instanceof Collection<?> ) {
				for ( Object colVal : (Collection<?>) val ) {
					appendURLEncodedValue(buf, me.getKey(), colVal);
				}
			} else if ( val.getClass().isArray() ) {
				for ( Object arrayVal : (Object[]) val ) {
					appendURLEncodedValue(buf, me.getKey(), arrayVal);
				}
			} else {
				appendURLEncodedValue(buf, me.getKey(), val);
			}
		}
		return buf.toString();
	}

	/**
	 * HTTP POST data as {@code application/x-www-form-urlencoded} (e.g. a web
	 * form) to a URL.
	 * 
	 * @param url
	 *        the URL to post to
	 * @param accept
	 *        the value to use for the {@literal Accept} HTTP header
	 * @param data
	 *        the data to encode and send as the body of the HTTP POST
	 * @param timeout
	 *        if greater than {@literal 0} then set as both the connection
	 *        timeout and read timeout, in milliseconds
	 * @param sslService
	 *        if provided and the URL represents an HTTPS URL, then
	 *        {@link SSLService#getSSLSocketFactory()} will be used as the
	 *        connection's {@code SSLSocketFactory}
	 * @return the URLConnection after the post data has been sent
	 * @throws IOException
	 *         if any IO error occurs
	 * @throws ResultStatusException
	 *         if the URL is the HTTP scheme and the HTTP response code is not
	 *         within the 200 - 299 range
	 */
	public static URLConnection postXWWWFormURLEncodedData(String url, String accept,
			Map<String, ?> data, int timeout, SSLService sslService) throws IOException {
		URLConnection conn = getURLConnection(url, HTTP_METHOD_POST, accept, timeout, sslService);
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		String body = urlEncoded(data);
		log.trace("HTTP POST {} for {} with application/x-www-form-urlencoded data: {}", url, accept,
				body);
		OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream(), UTF8_CHARSET);
		FileCopyUtils.copy(new StringReader(body), out);
		if ( conn instanceof HttpURLConnection ) {
			HttpURLConnection http = (HttpURLConnection) conn;
			int status = http.getResponseCode();
			if ( status < 200 || status > 299 ) {
				throw new ResultStatusException(http.getURL(), http.getResponseCode(),
						"HTTP result status not in the 200-299 range: " + http.getResponseCode() + " "
								+ http.getResponseMessage());
			}
		}
		return conn;
	}

	/**
	 * HTTP POST data as {@code application/x-www-form-urlencoded} (e.g. a web
	 * form) to a URL and return the response body as a string.
	 * 
	 * @param url
	 *        the URL to post to
	 * @param accept
	 *        the value to use for the {@literal Accept} HTTP header
	 * @param data
	 *        the data to encode and send as the body of the HTTP POST
	 * @param timeout
	 *        if greater than {@literal 0} then set as both the connection
	 *        timeout and read timeout, in milliseconds
	 * @param sslService
	 *        if provided and the URL represents an HTTPS URL, then
	 *        {@link SSLService#getSSLSocketFactory()} will be used as the
	 *        connection's {@code SSLSocketFactory}
	 * @return the response body as a String
	 * @throws IOException
	 *         if any IO error occurs
	 * @throws ResultStatusException
	 *         if the URL is the HTTP scheme and the HTTP response code is not
	 *         within the 200 - 299 range
	 * @see #postXWWWFormURLEncodedData(String, String, Map, int, SSLService)
	 */
	public static String postXWWWFormURLEncodedDataForString(String url, String accept,
			Map<String, ?> data, int timeout, SSLService sslService) throws IOException {
		URLConnection conn = postXWWWFormURLEncodedData(url, accept, data, timeout, sslService);
		return FileCopyUtils.copyToString(getUnicodeReaderFromURLConnection(conn));
	}

	/**
	 * HTTP GET a URL.
	 * 
	 * @param url
	 *        the URL to post to
	 * @param accept
	 *        the value to use for the {@literal Accept} HTTP header
	 * @param queryParameters
	 *        the data to encode and send as URL query parameters
	 * @param timeout
	 *        if greater than {@literal 0} then set as both the connection
	 *        timeout and read timeout, in milliseconds
	 * @param sslService
	 *        if provided and the URL represents an HTTPS URL, then
	 *        {@link SSLService#getSSLSocketFactory()} will be used as the
	 *        connection's {@code SSLSocketFactory}
	 * @return the URLConnection after the post data has been sent
	 * @throws IOException
	 *         if any IO error occurs
	 * @throws ResultStatusException
	 *         if the URL is the HTTP scheme and the HTTP response code is not
	 *         within the 200 - 299 range
	 */
	public static URLConnection getURL(String url, String accept, Map<String, ?> queryParameters,
			int timeout, SSLService sslService) throws IOException {
		String query = urlEncoded(queryParameters);
		String fullUrl = url;
		if ( query != null ) {
			fullUrl += query;
		}
		URLConnection conn = getURLConnection(fullUrl, HTTP_METHOD_GET, accept, timeout, sslService);
		log.trace("HTTP GET {} for {}", fullUrl, accept);
		if ( conn instanceof HttpURLConnection ) {
			HttpURLConnection http = (HttpURLConnection) conn;
			int status = http.getResponseCode();
			if ( status < 200 || status > 299 ) {
				throw new ResultStatusException(http.getURL(), http.getResponseCode(),
						"HTTP result status not in the 200-299 range: " + http.getResponseCode() + " "
								+ http.getResponseMessage());
			}
		}
		return conn;
	}

	/**
	 * HTTP GET a URL and return the response as a string.
	 * 
	 * @param url
	 *        the URL to post to
	 * @param accept
	 *        the value to use for the {@literal Accept} HTTP header
	 * @param queryParameters
	 *        the data to encode and send as URL query parameters
	 * @param timeout
	 *        if greater than {@literal 0} then set as both the connection
	 *        timeout and read timeout, in milliseconds
	 * @param sslService
	 *        if provided and the URL represents an HTTPS URL, then
	 *        {@link SSLService#getSSLSocketFactory()} will be used as the
	 *        connection's {@code SSLSocketFactory}
	 * @return the URLConnection after the post data has been sent
	 * @throws IOException
	 *         if any IO error occurs
	 * @throws ResultStatusException
	 *         if the URL is the HTTP scheme and the HTTP response code is not
	 *         within the 200 - 299 range
	 */
	public static String getURLForString(String url, String accept, Map<String, ?> queryParameters,
			int timeout, SSLService sslService) throws IOException {
		URLConnection conn = getURL(url, accept, queryParameters, timeout, sslService);
		return FileCopyUtils.copyToString(getUnicodeReaderFromURLConnection(conn));
	}

}

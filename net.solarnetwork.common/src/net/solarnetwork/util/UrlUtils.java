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

package net.solarnetwork.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.net.HttpURLConnection;
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
import net.solarnetwork.support.SSLService;
import net.solarnetwork.support.UnicodeReader;

/**
 * Utilities for supporting URL related tasks.
 * 
 * @author matt
 * @version 1.0
 * @since 1.54
 */
public final class UrlUtils {

	/** A HTTP Accept header value for any text type. */
	public static final String ACCEPT_TEXT = "text/*";

	/** A HTTP Accept header value for a JSON type. */
	public static final String ACCEPT_JSON = "application/json,text/json";

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
			if ( httpConn.getResponseCode() < 200 || httpConn.getResponseCode() > 299 ) {
				log.info("Non-200 HTTP response from {}: {}", conn.getURL(), httpConn.getResponseCode());
			}
		}

		log.trace("Got content type [{}] encoded as [{}]", type, enc);

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
	 *        timeout and read timeout, in seconds
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
		return getURLConnection(url, httpMethod, "text/*", timeout, sslService);
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
	 * The {@link #getInputSourceFromURLConnection(URLConnection)} automatically
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
	 *        timeout and read timeout, in seconds
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
		URL connUrl = new URL(url);
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
			buf.append(URLEncoder.encode(key, "UTF-8")).append('=')
					.append(URLEncoder.encode(value.toString(), "UTF-8"));
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
	 *        timeout and read timeout, in seconds
	 * @param sslService
	 *        if provided and the URL represents an HTTPS URL, then
	 *        {@link SSLService#getSSLSocketFactory()} will be used as the
	 *        connection's {@code SSLSocketFactory}
	 * @return the URLConnection after the post data has been sent
	 * @throws IOException
	 *         if any IO error occurs
	 * @throws RuntimeException
	 *         if the HTTP response code is not within the 200 - 299 range
	 */
	public static URLConnection postXWWWFormURLEncodedData(String url, String accept,
			Map<String, ?> data, int timeout, SSLService sslService) throws IOException {
		URLConnection conn = getURLConnection(url, HTTP_METHOD_POST, accept, timeout, sslService);
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		String body = urlEncoded(data);
		log.trace("Encoded HTTP POST data {} for {} as {}", data, url, body);
		OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
		FileCopyUtils.copy(new StringReader(body), out);
		if ( conn instanceof HttpURLConnection ) {
			HttpURLConnection http = (HttpURLConnection) conn;
			int status = http.getResponseCode();
			if ( status < 200 || status > 299 ) {
				throw new RuntimeException("HTTP result status not in the 200-299 range: "
						+ http.getResponseCode() + " " + http.getResponseMessage());
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
	 * @param data
	 *        the data to encode and send as the body of the HTTP POST
	 * @param timeout
	 *        if greater than {@literal 0} then set as both the connection
	 *        timeout and read timeout, in seconds
	 * @param sslService
	 *        if provided and the URL represents an HTTPS URL, then
	 *        {@link SSLService#getSSLSocketFactory()} will be used as the
	 *        connection's {@code SSLSocketFactory}
	 * @return the response body as a String
	 * @throws IOException
	 *         if any IO error occurs
	 * @throws RuntimeException
	 *         if the HTTP response code is not within the 200 - 299 range
	 * @see #postXWWWFormURLEncodedData(String, String, Map)
	 */
	public static String postXWWWFormURLEncodedDataForString(String url, Map<String, ?> data,
			int timeout, SSLService sslService) throws IOException {
		URLConnection conn = postXWWWFormURLEncodedData(url, "text/*, application/json", data, timeout,
				sslService);
		return FileCopyUtils.copyToString(getUnicodeReaderFromURLConnection(conn));
	}

}

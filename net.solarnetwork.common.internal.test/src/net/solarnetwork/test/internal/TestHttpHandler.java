/* ==================================================================
 * TestHttpHandler.java - 19/05/2017 4:09:05 PM
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

package net.solarnetwork.test.internal;

import static java.nio.charset.StandardCharsets.UTF_8;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StreamUtils;

/**
 * Extension of {@link AbstractHandler} to aid with unit tests.
 *
 * @author matt
 * @version 2.0
 */
public abstract class TestHttpHandler extends Handler.Abstract {

	private boolean handled = false;
	private Throwable exception;

	/** A class-level logger. */
	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Override
	public boolean handle(Request request, Response response, Callback callback) throws Exception {
		log.trace("HTTP request {}", request.getHttpURI());
		HttpFields headers = request.getHeaders();
		for ( HttpField field : headers ) {
			log.trace("HTTP header {} = {}", field.getName(), field.getValue());
		}
		try {
			handled = handleInternal(request, response, callback);
			if ( handled ) {
				callback.completeWith(CompletableFuture.completedFuture(null));
			}
			return handled;
		} catch ( Throwable e ) {
			exception = e;
			callback.completeWith(CompletableFuture.failedFuture(e));
			return true;
		}
	}

	/**
	 * HTTP invocation.
	 *
	 * @param request
	 *        the request
	 * @param response
	 *        the response
	 * @return {@literal true} if the request was handled successfully, and as
	 *         expected.
	 * @throws Exception
	 *         If any problem occurs.
	 */
	protected abstract boolean handleInternal(Request request, Response response, Callback callback)
			throws Exception;

	/**
	 * Respond with JSON content.
	 * <p>
	 * If a {@code Content-Encoding} response header has been set to either
	 * {@code deflate} or {@code gzip} then the resource data will be compressed
	 * accordingly.
	 * </p>
	 *
	 * @oaran request the HTTP request
	 * @param response
	 *        the HTTP response
	 * @param json
	 *        the JSON to respond with
	 * @throws IOException
	 *         if any IO error occurs
	 */
	protected void respondWithJson(Request request, Response response, String json) throws IOException {
		respondWithContent(request, response, "application/json", json.getBytes(UTF_8));
	}

	/**
	 * Respond with a JSON resource.
	 *
	 * <p>
	 * If a {@code Content-Encoding} response header has been set to either
	 * {@code deflate} or {@code gzip} then the resource data will be compressed
	 * accordingly.
	 * </p>
	 *
	 * @oaran request the HTTP request
	 * @param response
	 *        the HTTP response
	 * @param resource
	 *        the resource name
	 * @throws IOException
	 *         if any IO error occurs
	 */
	protected void respondWithJsonResource(Request request, Response response, String resource)
			throws IOException {
		respondWithResource(request, response, resource, "application/json");
	}

	/**
	 * Respond with content.
	 *
	 * <p>
	 * If a {@code Content-Encoding} response header has been set to either
	 * {@code deflate} or {@code gzip} then the resource data will be compressed
	 * accordingly.
	 * </p>
	 *
	 * @oaran request the HTTP request
	 * @param response
	 *        the HTTP response
	 * @param contentType
	 *        the content type
	 * @param data
	 *        the data
	 * @throws IOException
	 *         if any IO error occurs
	 */
	protected void respondWithContent(Request request, Response response, String contentType,
			byte[] data) throws IOException {
		response.getHeaders().put("Content-Type", contentType);
		try (OutputStream out = outputStream(request, response)) {
			StreamUtils.copy(data, out);
		}
	}

	/**
	 * Respond with a resource.
	 *
	 * <p>
	 * If a {@code Content-Encoding} response header has been set to either
	 * {@code deflate} or {@code gzip} then the resource data will be compressed
	 * accordingly.
	 * </p>
	 *
	 * @oaran request the HTTP request
	 * @param response
	 *        the HTTP response
	 * @param resource
	 *        the resource name
	 * @param contentType
	 *        the resource content type
	 * @throws IOException
	 *         if any IO error occurs
	 */
	protected void respondWithResource(Request request, Response response, String resource,
			String contentType) throws IOException {
		response.getHeaders().put("Content-Type", contentType);
		try (InputStream in = getClass().getResourceAsStream(resource)) {
			if ( in == null ) {
				throw new FileNotFoundException(
						"Resource [" + resource + "] not found from class " + getClass().getName());
			}
			try (OutputStream out = outputStream(request, response)) {
				StreamUtils.copy(in, out);
			}
		}
	}

	private OutputStream outputStream(Request request, Response response) throws IOException {
		OutputStream out = Response.asBufferedOutputStream(request, response);
		String enc = response.getHeaders().get("Content-Encoding");
		if ( "gzip".equals(enc) ) {
			out = new GZIPOutputStream(out);
		} else if ( "deflate".equals(enc) ) {
			out = new DeflaterOutputStream(out);
		}
		return out;
	}

	/**
	 * Test if the handler was called.
	 *
	 * @return boolean
	 * @throws Throwable
	 *         if the handler threw an exception or JUnit assertion
	 */
	public boolean isHandled() throws Exception {
		if ( exception != null ) {
			if ( exception instanceof Error ) {
				throw (Error) exception;
			} else if ( exception instanceof Exception ) {
				throw (Exception) exception;
			}
			throw new RuntimeException(exception);
		}
		return handled;
	}

}

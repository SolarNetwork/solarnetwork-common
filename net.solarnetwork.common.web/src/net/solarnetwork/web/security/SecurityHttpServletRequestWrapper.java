/* ==================================================================
 * SecurityHttpServletRequestWrapper.java - Oct 4, 2014 3:54:59 PM
 * 
 * Copyright 2007-2014 SolarNetwork.net Dev Team
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

package net.solarnetwork.web.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * {@link HttpServletRequestWrapper} to aid in computing hash values for the
 * request content.
 * 
 * @author matt
 * @version 1.1
 * @since 1.11
 */
public class SecurityHttpServletRequestWrapper extends HttpServletRequestWrapper {

	/** The default value for the {@code minimumCompressLength} property. */
	public static final int DEFAULT_MINIMUM_COMPRESS_LENGTH = 4096;

	private final int maximumLength;
	private final boolean compressBody;
	private final int minimumCompressLength;

	private boolean requestBodyCached;
	private boolean cachedRequestBodyCompressed;
	private byte[] cachedRequestBody; // TODO: support writing to temp file if body > maximumLength!

	private byte[] cachedMD5 = null;
	private byte[] cachedSHA1 = null;
	private byte[] cachedSHA256 = null;

	/**
	 * Construct from a request.
	 * 
	 * @param request
	 *        the request to wrap
	 * @param maxLength
	 *        the maximum body length allowed (in bytes)
	 */
	public SecurityHttpServletRequestWrapper(HttpServletRequest request, int maxLength) {
		this(request, maxLength, true);
	}

	/**
	 * Construct from a request.
	 * 
	 * @param request
	 *        the request to wrap
	 * @param maxLength
	 *        the maximum body length allowed (in bytes)
	 * @param compressBody
	 *        {@literal true} to compress the cached body in memory,
	 *        {@literal false} to not compress
	 * @since 1.1
	 */
	public SecurityHttpServletRequestWrapper(HttpServletRequest request, int maxLength,
			boolean compressBody) {
		this(request, maxLength, compressBody, DEFAULT_MINIMUM_COMPRESS_LENGTH);
	}

	/**
	 * Construct from a request.
	 * 
	 * @param request
	 *        the request to wrap
	 * @param maxLength
	 *        the maximum body length allowed (in bytes)
	 * @param compressBody
	 *        {@literal true} to compress the cached body in memory,
	 *        {@literal false} to not compress
	 * @param minimumCompressLength
	 *        The minimum size (in bytes) a request body must be before
	 *        compressing it.
	 * @since 1.1
	 */
	public SecurityHttpServletRequestWrapper(HttpServletRequest request, int maxLength,
			boolean compressBody, int minimumCompressLength) {
		super(request);
		this.maximumLength = maxLength;
		this.compressBody = compressBody;
		this.minimumCompressLength = minimumCompressLength;
	}

	private void cacheRequestBody() throws IOException {
		if ( requestBodyCached ) {
			return;
		}
		requestBodyCached = true;

		// save the request body as gzip data to reduce RAM use and allow for larger request body sizes
		InputStream in = super.getInputStream();
		ByteArrayOutputStream byos = new ByteArrayOutputStream(4096);
		GZIPOutputStream zip = null;
		try {
			int byteCount = 0;
			byte[] buffer = new byte[4096];
			int bytesRead = -1;
			while ( (bytesRead = in.read(buffer)) != -1 ) {
				if ( zip != null ) {
					zip.write(buffer, 0, bytesRead);
				} else {
					byos.write(buffer, 0, bytesRead);
				}
				byteCount += bytesRead;
				if ( byteCount >= minimumCompressLength && compressBody && zip == null ) {
					// switch to compression now
					byte[] currBytes = byos.toByteArray();
					byos.reset();
					zip = new GZIPOutputStream(byos);
					zip.write(currBytes);
					cachedRequestBodyCompressed = true;
				}
				if ( byteCount > this.maximumLength ) {
					throw new SecurityException("Request body too large.");
				}
			}
			if ( zip != null ) {
				zip.flush();
				zip.finish();
			}
			cachedRequestBody = byos.toByteArray();
		} finally {
			try {
				in.close();
			} catch ( IOException ex ) {
			}
			try {
				if ( zip != null ) {
					zip.close();
				}
			} catch ( IOException ex ) {
			}
			try {
				byos.close();
			} catch ( IOException ex ) {
			}
		}
	}

	private byte[] computeCachedReqeustBodyDigest(MessageDigest digest) throws IOException {
		if ( cachedRequestBody == null || cachedRequestBody.length < 1 ) {
			return null;
		}
		InputStream in = null;
		try {
			in = new ByteArrayInputStream(cachedRequestBody);
			if ( cachedRequestBodyCompressed ) {
				in = new GZIPInputStream(in);
			}
			DigestUtils.updateDigest(digest, in);
		} finally {
			try {
				in.close();
			} catch ( IOException e ) {
			}
		}
		return digest.digest();
	}

	/**
	 * Compute the MD5 hash of the request body.
	 * 
	 * @return the MD5 hash, or {@literal null} if there is no request content
	 * @throws IOException
	 *         if an IO exception occurs
	 * @throws SecurityException
	 *         if the request content length is larger than the configured
	 *         {@code maximumLength}
	 */
	public byte[] getContentMD5() throws IOException {
		byte[] digest = cachedMD5;
		if ( digest != null ) {
			return digest;
		}
		cacheRequestBody();
		if ( cachedRequestBody == null || cachedRequestBody.length < 1 ) {
			return null;
		}
		digest = computeCachedReqeustBodyDigest(DigestUtils.getMd5Digest());
		cachedMD5 = digest;
		return digest;
	}

	/**
	 * Compute the SHA1 hash of the request body.
	 * 
	 * @return the SHA1 hash, or {@literal null} if there is no request content
	 * @throws IOException
	 *         if an IO exception occurs
	 * @throws SecurityException
	 *         if the request content length is larger than the configured
	 *         {@code maximumLength}
	 */
	public byte[] getContentSHA1() throws IOException {
		byte[] digest = cachedSHA1;
		if ( digest != null ) {
			return digest;
		}
		cacheRequestBody();
		if ( cachedRequestBody == null || cachedRequestBody.length < 1 ) {
			return null;
		}
		digest = computeCachedReqeustBodyDigest(DigestUtils.getSha1Digest());
		cachedSHA1 = digest;
		return digest;
	}

	/**
	 * Compute the SHA256 hash of the request body.
	 * 
	 * @return the SHA256 hash, or {@literal null} if there is no request content
	 * @throws IOException
	 *         if an IO exception occurs
	 * @throws SecurityException
	 *         if the request content length is larger than the configured
	 *         {@code maximumLength}
	 */
	public byte[] getContentSHA256() throws IOException {
		byte[] digest = cachedSHA256;
		if ( digest != null ) {
			return digest;
		}
		cacheRequestBody();
		if ( cachedRequestBody == null || cachedRequestBody.length < 1 ) {
			return null;
		}
		digest = computeCachedReqeustBodyDigest(DigestUtils.getSha256Digest());
		cachedSHA256 = digest;
		return digest;
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		if ( requestBodyCached ) {
			return new ServletInputStream() {

				final private InputStream is = (cachedRequestBodyCompressed
						? new GZIPInputStream(new ByteArrayInputStream(cachedRequestBody))
						: new ByteArrayInputStream(cachedRequestBody));

				@Override
				public int read() throws IOException {
					return is.read();
				}

				@Override
				public int read(byte[] b) throws IOException {
					return is.read(b);
				}

				@Override
				public int read(byte[] b, int off, int len) throws IOException {
					return is.read(b, off, len);
				}

				@Override
				public boolean isFinished() {
					try {
						return is.available() < 1;
					} catch ( IOException e ) {
						return false;
					}
				}

				@Override
				public boolean isReady() {
					return true;
				}

				@Override
				public void setReadListener(ReadListener listener) {
					// ignore
				}
			};
		}
		return super.getInputStream();
	}

}

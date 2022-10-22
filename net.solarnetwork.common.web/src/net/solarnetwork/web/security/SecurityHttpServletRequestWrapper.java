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

import static java.nio.file.Files.createTempFile;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.util.StreamUtils;

/**
 * {@link HttpServletRequestWrapper} to aid in computing hash values for the
 * request content.
 * 
 * @author matt
 * @version 1.3
 * @since 1.11
 */
public class SecurityHttpServletRequestWrapper extends HttpServletRequestWrapper {

	/** The {@code minimumCompressLength} property default value. */
	public static final int DEFAULT_MINIMUM_COMPRESS_LENGTH = 4096;

	/**
	 * The {@code minimumSpoolLength} property default value.
	 * 
	 * @since 1.2
	 */
	public static final int DEFAULT_MINIMUM_SPOOL_LENGTH = 1024 * 64;

	/**
	 * The {@code compressibleContentTypePattern} property default value.
	 * 
	 * @since 1.2
	 */
	public static final Pattern DEFAULT_COMPRESSIBLE_CONTENT_PATTERN = Pattern.compile(
			"(?:text/.*|application/(?:.*\\+)?(?:json|xml|x-www-form-urlencoded)\\b.*|multipart/form-data\\b.*)",
			Pattern.CASE_INSENSITIVE);

	private static Path defaultSpoolDirectory() {
		String p = System.getProperty("java.io.tmpdir");
		if ( p == null ) {
			p = "/var/tmp";
		}
		return Paths.get(p);
	}

	private final int maximumLength;
	private final boolean compressBody;
	private final int minimumCompressLength;
	private final Pattern compressibleContentTypePattern;
	private final int minimumSpoolLength;
	private final Path spoolDirectory;

	private boolean requestBodyCached;
	private boolean cachedRequestBodyCompressed;
	private byte[] cachedRequestBody;
	private Path cachedRequestFile;

	private byte[] cachedMD5 = null;
	private byte[] cachedSHA1 = null;
	private byte[] cachedSHA256 = null;
	private byte[] cachedSHA512 = null;

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
	 * <p>
	 * The {@link #DEFAULT_COMPRESSIBLE_CONTENT_PATTERN} pattern will be used
	 * and the {@link #DEFAULT_MINIMUM_SPOOL_LENGTH} value will be used with the
	 * default spool directory set to the value of the {@literal java.io.tmpdir}
	 * system property.
	 * </p>
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
		this(request, maxLength, compressBody, minimumCompressLength,
				DEFAULT_COMPRESSIBLE_CONTENT_PATTERN, DEFAULT_MINIMUM_SPOOL_LENGTH,
				defaultSpoolDirectory());
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
	 * @param compressibleContentTypePattern
	 *        A pattern to match against content type values to allow
	 *        compressing, or {@literal null} to compress anything.
	 * @param minimumSpoolLength
	 *        The minimum size (in bytes) a request body must be before spooling
	 *        to content to disk.
	 * @param spoolDirectory
	 *        the directory to create spooled (temporary) files, or
	 *        {@literal null} to never spool to disk
	 * @since 1.2
	 */
	public SecurityHttpServletRequestWrapper(HttpServletRequest request, int maxLength,
			boolean compressBody, int minimumCompressLength, Pattern compressibleContentTypePattern,
			int minimumSpoolLength, Path spoolDirectory) {
		super(request);
		this.maximumLength = maxLength;
		this.compressBody = compressBody;
		this.minimumCompressLength = minimumCompressLength;
		this.compressibleContentTypePattern = compressibleContentTypePattern;
		this.minimumSpoolLength = minimumSpoolLength;
		this.spoolDirectory = spoolDirectory;
	}

	private boolean canCompressContent() {
		boolean result = true;
		if ( compressibleContentTypePattern != null ) {
			String contentType = getContentType();
			if ( contentType != null ) {
				result = compressibleContentTypePattern.matcher(contentType).matches();
			}
		}
		return result;
	}

	private void cacheRequestBody() throws IOException {
		if ( requestBodyCached ) {
			return;
		}
		requestBodyCached = true;

		final boolean canCompressBody = canCompressContent();

		// compute and cache all digest values while we read the stream
		final MessageDigest md5 = DigestUtils.getMd5Digest();
		final MessageDigest sha1 = DigestUtils.getSha1Digest();
		final MessageDigest sha256 = DigestUtils.getSha256Digest();
		final MessageDigest sha512 = DigestUtils.getSha512Digest();
		final MessageDigest[] digests = new MessageDigest[] { md5, sha1, sha256, sha512 };

		// save the request body as gzip data to reduce RAM use and allow for larger request body sizes
		InputStream in = super.getInputStream();
		ByteArrayOutputStream ramBuffer = new ByteArrayOutputStream(4096);
		OutputStream spool = null;
		GZIPOutputStream zip = null;
		try {
			int byteCount = 0;
			byte[] buffer = new byte[4096];
			int bytesRead = -1;
			while ( (bytesRead = in.read(buffer)) != -1 ) {
				for ( MessageDigest md : digests ) {
					md.update(buffer, 0, bytesRead);
				}
				OutputStream out = (zip != null ? zip : spool != null ? spool : ramBuffer);
				out.write(buffer, 0, bytesRead);
				byteCount += bytesRead;
				if ( byteCount > this.maximumLength ) {
					throw new SecurityException("Request body too large.");
				}
				if ( spool == null && spoolDirectory != null && byteCount >= minimumSpoolLength ) {
					// switch to spool file now; first write current buffer
					cachedRequestFile = createTempFile(spoolDirectory,
							"SecurityHttpServletRequestWrapper-", ".dat");
					spool = new BufferedOutputStream(Files.newOutputStream(cachedRequestFile));
					InputStream tmpIn = null;
					GZIPOutputStream newZip = null;
					try {
						if ( compressBody && canCompressBody ) {
							newZip = new GZIPOutputStream(spool);
						}
						if ( zip != null ) {
							// have to re-compress to spool file now
							zip.flush();
							zip.finish();
							tmpIn = new GZIPInputStream(
									new ByteArrayInputStream(ramBuffer.toByteArray()));
						} else {
							tmpIn = new ByteArrayInputStream(ramBuffer.toByteArray());
						}
						StreamUtils.copy(tmpIn, newZip != null ? newZip : spool);
					} finally {
						if ( tmpIn != null ) {
							try {
								tmpIn.close();
							} catch ( IOException tmpEx ) {
								// ignore
							}
						}
						if ( newZip != null ) {
							zip = newZip;
						}
					}
				} else if ( zip == null && spool == null && compressBody && canCompressBody
						&& byteCount >= minimumCompressLength ) {
					// switch to compression now
					byte[] currBytes = ramBuffer.toByteArray();
					ramBuffer.reset();
					zip = new GZIPOutputStream(ramBuffer);
					zip.write(currBytes);
					cachedRequestBodyCompressed = true;
				}
			}
			if ( zip != null ) {
				zip.flush();
				zip.finish();
			}
			cachedMD5 = md5.digest();
			cachedSHA1 = sha1.digest();
			cachedSHA256 = sha256.digest();
			cachedSHA512 = sha512.digest();
			if ( cachedRequestFile == null ) {
				cachedRequestBody = ramBuffer.toByteArray();
			}
		} finally {
			try {
				in.close();
			} catch ( IOException ex ) {
			}
			if ( zip != null ) {
				try {
					zip.close();
				} catch ( IOException ex ) {
				}
			}
			try {
				ramBuffer.close();
			} catch ( IOException ex ) {
			}
			if ( spool != null ) {
				try {
					spool.close();
				} catch ( IOException ex ) {
				}
			}
		}
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
		if ( cachedMD5 != null ) {
			return cachedMD5;
		}
		cacheRequestBody();
		return cachedMD5;
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
		if ( cachedSHA1 != null ) {
			return cachedSHA1;
		}
		cacheRequestBody();
		return cachedSHA1;
	}

	/**
	 * Compute the SHA-256 hash of the request body.
	 * 
	 * @return the SHA-256 hash, or {@literal null} if there is no request
	 *         content
	 * @throws IOException
	 *         if an IO exception occurs
	 * @throws SecurityException
	 *         if the request content length is larger than the configured
	 *         {@code maximumLength}
	 */
	public byte[] getContentSHA256() throws IOException {
		if ( cachedSHA256 != null ) {
			return cachedSHA256;
		}
		cacheRequestBody();
		return cachedSHA256;
	}

	/**
	 * Compute the SHA-512 hash of the request body.
	 * 
	 * @return the SHA-512 hash, or {@literal null} if there is no request
	 *         content
	 * @throws IOException
	 *         if an IO exception occurs
	 * @throws SecurityException
	 *         if the request content length is larger than the configured
	 *         {@code maximumLength}
	 * @since 1.3
	 */
	public byte[] getContentSHA512() throws IOException {
		if ( cachedSHA512 != null ) {
			return cachedSHA512;
		}
		cacheRequestBody();
		return cachedSHA512;
	}

	private InputStream cachedRequestInputStream() throws IOException {
		InputStream in = (cachedRequestFile != null
				? new BufferedInputStream(Files.newInputStream(cachedRequestFile))
				: new ByteArrayInputStream(cachedRequestBody));
		return (cachedRequestBodyCompressed ? new GZIPInputStream(in) : in);
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		if ( requestBodyCached ) {
			return new ServletInputStream() {

				final private InputStream is = cachedRequestInputStream();

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

	/**
	 * Immediately delete any cached request body content.
	 * 
	 * @throws IOException
	 *         if any IO error occurs
	 */
	public void deleteCachedContent() throws IOException {
		if ( cachedRequestFile != null ) {
			Files.deleteIfExists(cachedRequestFile);
		}
		if ( cachedRequestBody != null ) {
			cachedRequestBody = null;
		}
		requestBodyCached = false;
	}

}

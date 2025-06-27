/* ==================================================================
 * SecurityHttpServletRequestWrapperTests.java - 18/03/2022 11:46:41 AM
 * 
 * Copyright 2022 SolarNetwork.net Dev Team
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

package net.solarnetwork.web.jakarta.security.test;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.String.format;
import static net.solarnetwork.web.jakarta.security.SecurityHttpServletRequestWrapper.DEFAULT_COMPRESSIBLE_CONTENT_PATTERN;
import static net.solarnetwork.web.jakarta.security.SecurityHttpServletRequestWrapper.DEFAULT_MINIMUM_COMPRESS_LENGTH;
import static net.solarnetwork.web.jakarta.security.SecurityHttpServletRequestWrapper.DEFAULT_MINIMUM_SPOOL_LENGTH;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import net.solarnetwork.web.jakarta.security.SecurityHttpServletRequestWrapper;

/**
 * Test cases for the {@link SecurityHttpServletRequestWrapper} class.
 * 
 * @author matt
 * @version 1.0
 */
public class SecurityHttpServletRequestWrapperTests {

	private Path spoolDir;

	@Before
	public void setup() throws IOException {
		spoolDir = Files.createTempDirectory("SecurityHttpServletRequestWrapperTests");
	}

	@After
	public void teardown() throws IOException {
		if ( spoolDir != null ) {
			Files.walkFileTree(spoolDir, new SimpleFileVisitor<Path>() {

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
						throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

			});
		}
	}

	private Path createTempFileOfSize(int size) throws IOException {
		Path tmp = Files.createTempFile(spoolDir, "SecurityHttpServletRequestWrapperTests-data-",
				".dat");
		int count = 0;
		byte[] buf = new byte[4096];
		Arrays.fill(buf, (byte) '0');
		try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(tmp))) {
			while ( count < size ) {
				int len = buf.length;
				if ( count + len > size ) {
					len = size - count;
				}
				out.write(buf, 0, len);
				count += len;
			}
		}
		return tmp;
	}

	private void assertDigests(String msg, SecurityHttpServletRequestWrapper wrapper, String md5,
			String sha1, String sha256, String sha512) throws IOException {
		String actualMd5 = Hex.encodeHexString(wrapper.getContentMD5());
		String actualSha1 = Hex.encodeHexString(wrapper.getContentSHA1());
		String actualSha256 = Hex.encodeHexString(wrapper.getContentSHA256());
		String actualSha512 = Hex.encodeHexString(wrapper.getContentSHA512());

		assertThat(format("%s MD5", msg), actualMd5, is(md5));
		assertThat(format("%s SHA1", msg), actualSha1, is(sha1));
		assertThat(format("%s SHA-256", msg), actualSha256, is(sha256));
		assertThat(format("%s SHA-512", msg), actualSha512, is(sha512));

		// now verify returned stream content actually matches
		String streamActualSha256 = Hex.encodeHexString(DigestUtils.sha256(wrapper.getInputStream()));
		assertThat(format("%s InputStream SHA-256", msg), streamActualSha256, is(sha256));
	}

	@Test
	public void requestSmallerThanMinCompressLength() throws IOException {
		// GIVEN
		Path tmp = createTempFileOfSize(1024);
		try (InputStream in = Files.newInputStream(tmp)) {
			TestingHttpServletReqeust req = new TestingHttpServletReqeust("GET", "/foo/bar");
			req.setContentType(MediaType.APPLICATION_JSON_VALUE);
			req.setContentStream(in);
			SecurityHttpServletRequestWrapper wrapper = new SecurityHttpServletRequestWrapper(req,
					MAX_VALUE, true, DEFAULT_MINIMUM_COMPRESS_LENGTH,
					DEFAULT_COMPRESSIBLE_CONTENT_PATTERN, DEFAULT_MINIMUM_SPOOL_LENGTH, spoolDir);

			// WHEN
			// @formatter:off
			assertDigests("Digest", wrapper, 
					"9d0ef2e3d00a0793bd4c5f31b8ad9e8a", 
					"a0a32b159feca49e7b13b9a49ae0127ade587f8b", 
					"35ae5091b37e8f0f306833ef57a635f9dc06738d7f4e563a610eec2adb26fe28",
					"0b9222097e0efcf1beac88426ac09170482809784a82d201946c17a522d898a52633c183a5f99f44d4eea3cfe0c222a633e0e5a46bb09ef4d9aa8d27aadc01e9");
			// @formatter:on
		}
	}

	@Test
	public void requestSmallerThanSpoolLength_compressible() throws IOException {
		// GIVEN
		Path tmp = createTempFileOfSize(8192);
		try (InputStream in = Files.newInputStream(tmp)) {
			TestingHttpServletReqeust req = new TestingHttpServletReqeust("GET", "/foo/bar");
			req.setContentType(MediaType.APPLICATION_JSON_VALUE);
			req.setContentStream(in);
			SecurityHttpServletRequestWrapper wrapper = new SecurityHttpServletRequestWrapper(req,
					MAX_VALUE, true, DEFAULT_MINIMUM_COMPRESS_LENGTH,
					DEFAULT_COMPRESSIBLE_CONTENT_PATTERN, DEFAULT_MINIMUM_SPOOL_LENGTH, spoolDir);

			// WHEN
			// @formatter:off
			assertDigests("Digest", wrapper, 
					"c421804369c8b3777d33c46d7655abea", 
					"bd5fdf6bf5aa7db12d8cb6a4ee066adad41dc0d6", 
					"fc25464cfa116ccfe8bfcf9e8bc095b1e4cdcfc40e26ade2be58884bb6b648f2",
					"6ea807e6dbdd316882841cbddb4bbf7dc964598314da6d1e8559c797d917d6c9ffc7e04c93f58c4429829e741e6a70fc50f5ba57319c7caf4180442d524dccd5");
			// @formatter:on
		}
	}

	@Test
	public void requestSmallerThanSpoolLength_notCompressible() throws IOException {
		// GIVEN
		Path tmp = createTempFileOfSize(8192);
		try (InputStream in = Files.newInputStream(tmp)) {
			TestingHttpServletReqeust req = new TestingHttpServletReqeust("GET", "/foo/bar");
			req.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
			req.setContentStream(in);
			SecurityHttpServletRequestWrapper wrapper = new SecurityHttpServletRequestWrapper(req,
					MAX_VALUE, true, DEFAULT_MINIMUM_COMPRESS_LENGTH,
					DEFAULT_COMPRESSIBLE_CONTENT_PATTERN, DEFAULT_MINIMUM_SPOOL_LENGTH, spoolDir);

			// WHEN
			// @formatter:off
			assertDigests("Digest", wrapper, 
					"c421804369c8b3777d33c46d7655abea", 
					"bd5fdf6bf5aa7db12d8cb6a4ee066adad41dc0d6", 
					"fc25464cfa116ccfe8bfcf9e8bc095b1e4cdcfc40e26ade2be58884bb6b648f2",
					"6ea807e6dbdd316882841cbddb4bbf7dc964598314da6d1e8559c797d917d6c9ffc7e04c93f58c4429829e741e6a70fc50f5ba57319c7caf4180442d524dccd5");
			// @formatter:on
		}
	}

	@Test
	public void requestSpools_compressible() throws IOException {
		// GIVEN
		Path tmp = createTempFileOfSize(1024 * 17);
		try (InputStream in = Files.newInputStream(tmp)) {
			TestingHttpServletReqeust req = new TestingHttpServletReqeust("GET", "/foo/bar");
			req.setContentType(MediaType.APPLICATION_JSON_VALUE);
			req.setContentStream(in);
			SecurityHttpServletRequestWrapper wrapper = new SecurityHttpServletRequestWrapper(req,
					MAX_VALUE, true, DEFAULT_MINIMUM_COMPRESS_LENGTH,
					DEFAULT_COMPRESSIBLE_CONTENT_PATTERN, 1024 * 16, spoolDir);

			// WHEN
			// @formatter:off
			assertDigests("Digest", wrapper, 
					"cfb1011114536da94b4e5d36f17aa1a1", 
					"454494d4e8b50a9e37de3fef36a37ec4d2e105de", 
					"cd4c234eeedb5c80f4ef34ad4776b35fd3accf76f5df1d95703087a50508ff5c",
					"99c29c637c47f64719dcd18f8bfdc332e55149722d825b73fa9e8f7e3001c842034e30a4174c8553c1bcbd4fcac8211c57f2c9efac4a92f0c622c620ac5a5eba");
			// @formatter:on
		}
	}

	@Test
	public void requestSpools_notCompressible() throws IOException {
		// GIVEN
		Path tmp = createTempFileOfSize(1024 * 17);
		try (InputStream in = Files.newInputStream(tmp)) {
			TestingHttpServletReqeust req = new TestingHttpServletReqeust("GET", "/foo/bar");
			req.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
			req.setContentStream(in);
			SecurityHttpServletRequestWrapper wrapper = new SecurityHttpServletRequestWrapper(req,
					MAX_VALUE, true, DEFAULT_MINIMUM_COMPRESS_LENGTH,
					DEFAULT_COMPRESSIBLE_CONTENT_PATTERN, 1024 * 16, spoolDir);

			// WHEN
			// @formatter:off
			assertDigests("Digest", wrapper, 
					"cfb1011114536da94b4e5d36f17aa1a1", 
					"454494d4e8b50a9e37de3fef36a37ec4d2e105de", 
					"cd4c234eeedb5c80f4ef34ad4776b35fd3accf76f5df1d95703087a50508ff5c",
					"99c29c637c47f64719dcd18f8bfdc332e55149722d825b73fa9e8f7e3001c842034e30a4174c8553c1bcbd4fcac8211c57f2c9efac4a92f0c622c620ac5a5eba");
			// @formatter:on
		}
	}

}

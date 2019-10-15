/* ==================================================================
 * SdkS3ClientIntegrationTests.java - 15/10/2019 3:20:06 pm
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

package net.solarnetwork.common.s3.sdk.test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.FileCopyUtils;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest.KeyVersion;
import com.amazonaws.services.s3.model.DeleteObjectsResult;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import net.solarnetwork.common.s3.S3ObjectMeta;
import net.solarnetwork.common.s3.S3ObjectMetadata;
import net.solarnetwork.common.s3.S3ObjectRef;
import net.solarnetwork.common.s3.S3ObjectReference;
import net.solarnetwork.common.s3.sdk.SdkS3Client;
import net.solarnetwork.test.SystemPropertyMatchTestRule;
import net.solarnetwork.util.ProgressListener;

/**
 * Test cases for {@link SdkS3Client} that tests actual network operations.
 * 
 * @author matt
 * @version 1.0
 */
public class SdkS3ClientIntegrationTests {

	/** Only run when the {@code s3-int} system property is defined. */
	@ClassRule
	public static SystemPropertyMatchTestRule PROFILE_RULE = new SystemPropertyMatchTestRule("s3-int");

	private static Properties TEST_PROPS;

	private final Logger log = LoggerFactory.getLogger(getClass());

	private AmazonS3 s3;
	private SdkS3Client client;

	@BeforeClass
	public static void setupClass() {
		Properties p = new Properties();
		try {
			InputStream in = SdkS3ClientIntegrationTests.class.getClassLoader()
					.getResourceAsStream("s3-int.properties");
			if ( in != null ) {
				p.load(in);
				in.close();
			}
		} catch ( IOException e ) {
			throw new RuntimeException(e);
		}
		TEST_PROPS = p;
	}

	@Before
	public void setup() {
		client = new SdkS3Client();
		client.setBucketName(getBucketName());
		client.setRegionName(getRegionName());
		client.setAccessToken(TEST_PROPS.getProperty("accessKey"));
		client.setAccessSecret(TEST_PROPS.getProperty("secretKey"));
		client.configurationChanged(null);
	}

	@After
	public void teardown() {
		if ( s3 != null ) {
			clearS3(s3);
		}
	}

	private String getBucketName() {
		AmazonS3URI uri = new AmazonS3URI(TEST_PROPS.getProperty("path"));
		return uri.getBucket();
	}

	private String getRegionName() {
		AmazonS3URI uri = new AmazonS3URI(TEST_PROPS.getProperty("path"));
		return uri.getRegion();
	}

	private String getObjectKeyPrefix() {
		AmazonS3URI uri = new AmazonS3URI(TEST_PROPS.getProperty("path"));
		String keyPrefix = uri.getURI().getPath();
		if ( keyPrefix.startsWith("/") ) {
			keyPrefix = keyPrefix.substring(1);
		}
		int bucketIdx = keyPrefix.indexOf('/');
		if ( bucketIdx > 0 ) {
			keyPrefix = keyPrefix.substring(bucketIdx + 1);
		}
		return keyPrefix;
	}

	private String objectKey(String path) {
		return getObjectKeyPrefix() + "/" + path;
	}

	private AmazonS3 getS3() {
		AmazonS3URI uri = new AmazonS3URI(TEST_PROPS.getProperty("path"));
		AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard().withRegion(uri.getRegion());
		String accessKey = TEST_PROPS.getProperty("accessKey");
		String secretKey = TEST_PROPS.getProperty("secretKey");
		builder = builder.withCredentials(
				new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)));
		return builder.build();
	}

	private String objectAsString(AmazonS3 client, String key) {
		AmazonS3URI uri = new AmazonS3URI(TEST_PROPS.getProperty("path"));
		S3Object obj = client.getObject(uri.getBucket(), key);
		try {
			return FileCopyUtils.copyToString(new InputStreamReader(obj.getObjectContent(), "UTF-8"));
		} catch ( IOException e ) {
			throw new RuntimeException(e);
		}
	}

	private void putStringOject(AmazonS3 client, String key, String content) {
		AmazonS3URI uri = new AmazonS3URI(TEST_PROPS.getProperty("path"));
		client.putObject(uri.getBucket(), key, content);
	}

	private ListObjectsV2Result listS3(AmazonS3 client) {
		AmazonS3URI uri = new AmazonS3URI(TEST_PROPS.getProperty("path"));
		return client.listObjectsV2(uri.getBucket(), uri.getKey());
	}

	private void clearS3(AmazonS3 client) {
		AmazonS3URI uri = new AmazonS3URI(TEST_PROPS.getProperty("path"));

		ListObjectsV2Result result = listS3(client);

		DeleteObjectsRequest req = new DeleteObjectsRequest(uri.getBucket());
		if ( result.getKeyCount() > 0 ) {
			for ( S3ObjectSummary obj : result.getObjectSummaries() ) {
				if ( uri.getKey() != null && uri.getKey().equals(obj.getKey()) ) {
					continue;
				}
				req.getKeys().add(new KeyVersion(obj.getKey()));
			}
			if ( !req.getKeys().isEmpty() ) {
				DeleteObjectsResult deleteResult = client.deleteObjects(req);
				if ( deleteResult.getDeletedObjects() != null ) {
					log.info("Deleted objects from S3: " + deleteResult.getDeletedObjects().stream()
							.map(o -> o.getKey()).collect(Collectors.toList()));
				}
			}
		}
	}

	@Test
	public void putObject() throws Exception {
		// GIVEN
		s3 = getS3();
		final String uniqueKey = objectKey(UUID.randomUUID().toString());
		final String data = "Hello, world.";
		final ByteArrayResource r = new ByteArrayResource(data.getBytes(Charset.forName("UTF-8")));
		final S3ObjectMeta meta = new S3ObjectMeta(r.contentLength(), new Date());

		// WHEN
		S3ObjectReference result = client.putObject(uniqueKey, r.getInputStream(), meta, null, null);

		// THEN
		assertThat("Result success", result,
				equalTo(new S3ObjectRef(uniqueKey, meta.getSize(), meta.getModified())));
		assertThat("Remote content", objectAsString(s3, uniqueKey), equalTo(data));
	}

	private Path createTempFile(String data, int count) throws IOException {
		final Path tmpFile = Files.createTempFile("s3-client-test-", ".txt");
		try (PrintWriter out = new PrintWriter(
				Files.newBufferedWriter(tmpFile, Charset.forName("UTF-8")))) {
			for ( int i = 0; i < 1000; i++ ) {
				out.println(data);
			}
		}
		return tmpFile;
	}

	@Test
	public void putObject_withProgress() throws Exception {
		// GIVEN
		final String data = "All work and no play makes Jack a dull boy.";
		final Path tmpFile = createTempFile(data, 1000);
		final S3ObjectMeta meta = new S3ObjectMeta(Files.size(tmpFile),
				new Date(Files.getLastModifiedTime(tmpFile).toMillis()));
		final String uniqueKey = objectKey(UUID.randomUUID().toString());
		final List<Double> progressAmounts = new ArrayList<Double>(4);
		final ProgressListener<Path> listener = new ProgressListener<Path>() {

			@Override
			public void progressChanged(Path context, double amountComplete) {
				assertThat("Progress context is expected", context, sameInstance(tmpFile));
				progressAmounts.add(amountComplete);
			}
		};
		s3 = getS3();

		// WHEN
		S3ObjectReference result = client.putObject(uniqueKey, Files.newInputStream(tmpFile), meta,
				listener, tmpFile);

		// THEN
		assertThat("Result success", result,
				equalTo(new S3ObjectRef(uniqueKey, meta.getSize(), meta.getModified())));
		log.debug("Upload progress values: {}", progressAmounts);
		assertThat("Progress obtained", progressAmounts.size(), greaterThan(0));
	}

	@Test
	public void getObject() throws Exception {
		// GIVEN
		s3 = getS3();
		final long start = System.currentTimeMillis();
		final String uniqueKey = objectKey(UUID.randomUUID().toString());
		final String data = "Hello, world.";
		putStringOject(s3, uniqueKey, data);

		// WHEN
		net.solarnetwork.common.s3.S3Object obj = client.getObject(uniqueKey, null, null);

		// THEN
		assertThat("Object returned", obj, notNullValue());
		assertThat("Object content",
				FileCopyUtils.copyToString(
						new InputStreamReader(obj.getInputStream(), Charset.forName("UTF-8"))),
				equalTo(data));

		S3ObjectMetadata meta = obj.getMetadata();
		assertThat("Metadata returned", meta, notNullValue());
		assertThat("Metadata modified date returned", meta.getModified(), notNullValue());
		assertThat("Metadata mod date", meta.getModified().getTime(), greaterThanOrEqualTo(start));
		assertThat("Metadata content length", meta.getSize(),
				equalTo((long) data.getBytes(Charset.forName("UTF-8")).length));
	}

	@Test
	public void getObject_withProgress() throws Exception {
		// GIVEN
		s3 = getS3();
		final long start = System.currentTimeMillis();
		final String uniqueKey = objectKey(UUID.randomUUID().toString());
		final String data = "All work and no play makes Jack a dull boy.";
		final Path tmpFile = createTempFile(data, 1000);
		final List<Double> progressAmounts = new ArrayList<Double>(4);
		final ProgressListener<Path> listener = new ProgressListener<Path>() {

			@Override
			public void progressChanged(Path context, double amountComplete) {
				assertThat("Progress context is expected", context, sameInstance(tmpFile));
				progressAmounts.add(amountComplete);
			}
		};
		final AmazonS3URI uri = new AmazonS3URI(TEST_PROPS.getProperty("path"));
		s3.putObject(uri.getBucket(), uniqueKey, tmpFile.toFile());

		// WHEN
		net.solarnetwork.common.s3.S3Object obj = client.getObject(uniqueKey, listener, tmpFile);

		// THEN
		assertThat("Object returned", obj, notNullValue());

		log.debug("Download progress values: {}", progressAmounts);
		assertThat("Progress does not start until open input stream", progressAmounts.size(),
				equalTo(0));

		Path tmpFile2 = Files.createTempFile("s3-client-test-", ".txt");
		FileCopyUtils.copy(obj.getInputStream(),
				new BufferedOutputStream(Files.newOutputStream(tmpFile2)));
		assertThat("Object content", DigestUtils.sha1Hex(Files.newInputStream(tmpFile)),
				equalTo(DigestUtils.sha1Hex(Files.newInputStream(tmpFile2))));

		S3ObjectMetadata meta = obj.getMetadata();
		assertThat("Metadata returned", meta, notNullValue());
		assertThat("Metadata modified date returned", meta.getModified(), notNullValue());
		assertThat("Metadata mod date", meta.getModified().getTime(), greaterThanOrEqualTo(start));
		assertThat("Metadata content length", meta.getSize(), equalTo(Files.size(tmpFile)));

		log.debug("Download progress values: {}", progressAmounts);
		assertThat("Progress obtained", progressAmounts.size(), greaterThan(0));
	}

}

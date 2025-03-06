/* ==================================================================
 * Sdk2S3ClientIntegrationTests.java - 16/06/2024 6:22:23 pm
 *
 * Copyright 2024 SolarNetwork.net Dev Team
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

package net.solarnetwork.common.s3.sdk2.test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.MimeType;
import net.solarnetwork.common.s3.S3ObjectMeta;
import net.solarnetwork.common.s3.S3ObjectMetadata;
import net.solarnetwork.common.s3.S3ObjectRef;
import net.solarnetwork.common.s3.S3ObjectReference;
import net.solarnetwork.common.s3.sdk2.Sdk2S3Client;
import net.solarnetwork.service.ProgressListener;
import net.solarnetwork.test.SystemPropertyMatchTestRule;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.apache.ApacheSdkHttpService;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Uri;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.S3Object;

/**
 * Test cases for the {@link Sdk3S3Client} class.
 *
 * @author matt
 * @version 1.0
 */
public class Sdk2S3ClientIntegrationTests {

	/** Only run when the {@code s3-int} system property is defined. */
	@ClassRule
	public static SystemPropertyMatchTestRule PROFILE_RULE = new SystemPropertyMatchTestRule("s3-int");

	private static Properties TEST_PROPS;

	private final Logger log = LoggerFactory.getLogger(getClass());

	private S3Uri s3Uri;
	private S3Client s3;
	private ExecutorService executor;
	private Sdk2S3Client client;

	@BeforeClass
	public static void setupClass() {
		Properties p = new Properties();
		try {
			InputStream in = Sdk2S3ClientIntegrationTests.class.getClassLoader()
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

	private String region() {
		String region = TEST_PROPS.getProperty("region", null);
		if ( region == null ) {
			String path = TEST_PROPS.getProperty("path");
			Matcher m = Pattern.compile("://s3\\.([^.]+)\\.").matcher(path);
			if ( m.find() ) {
				region = m.group(1);
			} else {
				throw new IllegalStateException("region property not defined");
			}
		}
		return region;
	}

	@Before
	public void setup() {
		executor = Executors.newCachedThreadPool();

		s3Uri = S3Utilities.builder().region(Region.of(region())).build()
				.parseUri(URI.create(TEST_PROPS.getProperty("path")));

		client = new Sdk2S3Client(executor);
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
		return s3Uri.bucket().get();
	}

	private String getRegionName() {
		return s3Uri.region().get().id();
	}

	private String getObjectKeyPrefix() {
		String keyPrefix = s3Uri.uri().getPath();
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

	private S3Client getS3() {
		S3ClientBuilder builder = S3Client.builder()
				.httpClient(new ApacheSdkHttpService().createHttpClientBuilder().build())
				.region(s3Uri.region().get());
		String accessKey = TEST_PROPS.getProperty("accessKey");
		String secretKey = TEST_PROPS.getProperty("secretKey");
		builder = builder.credentialsProvider(
				StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)));
		return builder.build();
	}

	private String objectAsString(S3Client client, String key) {
		try (ResponseInputStream<GetObjectResponse> in = client
				.getObject(r -> r.bucket(s3Uri.bucket().get()))) {
			return FileCopyUtils.copyToString(new InputStreamReader(in, StandardCharsets.UTF_8));
		} catch ( IOException e ) {
			throw new RuntimeException(e);
		}
	}

	private void putStringOject(S3Client client, String key, String content) {
		client.putObject(r -> r.bucket(s3Uri.bucket().get()).key(key),
				RequestBody.fromString(content, StandardCharsets.UTF_8));
	}

	private ListObjectsV2Response listS3(S3Client client) {
		return client.listObjectsV2(r -> r.bucket(s3Uri.bucket().get()).prefix(s3Uri.key().get()));
	}

	private void clearS3(S3Client client) {
		ListObjectsV2Response result = listS3(client);
		if ( result.keyCount() > 0 ) {
			List<ObjectIdentifier> keys = new ArrayList<>();
			for ( S3Object obj : result.contents() ) {
				if ( s3Uri.key() != null && s3Uri.key().equals(obj.key()) ) {
					continue;
				}
				keys.add(ObjectIdentifier.builder().key(obj.key()).build());
			}
			if ( !keys.isEmpty() ) {
				DeleteObjectsResponse deleteResult = client
						.deleteObjects(r -> r.bucket(s3Uri.bucket().get()).delete(d -> d.objects(keys)));
				if ( deleteResult.deleted() != null ) {
					log.info("Deleted objects from S3: " + deleteResult.deleted().stream()
							.map(o -> o.key()).collect(Collectors.toList()));
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
		final ByteArrayResource r = new ByteArrayResource(data.getBytes(UTF_8));
		final S3ObjectMeta meta = new S3ObjectMeta(r.contentLength(), new Date());

		// WHEN
		S3ObjectReference result = client.putObject(uniqueKey, r.getInputStream(), meta, null, null);

		// THEN
		assertThat("Result success", result, equalTo(new S3ObjectRef(uniqueKey)));
		assertThat("Remote content", objectAsString(s3, uniqueKey), equalTo(data));
	}

	@Test
	public void putObject_withContentType() throws Exception {
		// GIVEN
		s3 = getS3();
		final String uniqueKey = objectKey(UUID.randomUUID().toString());
		final String data = "Hello, world.";
		final ByteArrayResource r = new ByteArrayResource(data.getBytes(UTF_8));
		final S3ObjectMeta meta = new S3ObjectMeta(r.contentLength(), new Date(),
				MimeType.valueOf("text/plain; charset=utf-8"));

		// WHEN
		S3ObjectReference result = client.putObject(uniqueKey, r.getInputStream(), meta, null, null);

		// THEN
		assertThat("Result success", result, equalTo(new S3ObjectRef(uniqueKey)));
		GetObjectRequest req = GetObjectRequest.builder().bucket(s3Uri.bucket().get()).key(uniqueKey)
				.build();
		try (ResponseInputStream<GetObjectResponse> obj = s3.getObject(req)) {
			assertThat("Remote content", FileCopyUtils.copyToString(new InputStreamReader(obj, UTF_8)),
					equalTo(data));

			assertThat("Remote content type", obj.response().contentType(),
					equalTo(meta.getContentType().toString()));
		}
	}

	@Test
	public void putObject_withContentDisposition() throws Exception {
		// GIVEN
		s3 = getS3();
		final String uniqueKey = objectKey(UUID.randomUUID().toString());
		final String data = "Hello, world.";
		final ByteArrayResource r = new ByteArrayResource(data.getBytes(UTF_8));
		final String contentDisposition = "attachment; filename=\"foo.txt\"";
		final S3ObjectMeta meta = new S3ObjectMeta(r.contentLength(), new Date(),
				MimeType.valueOf("text/plain; charset=utf-8"),
				Collections.singletonMap("Content-Disposition", contentDisposition));

		// WHEN
		S3ObjectReference result = client.putObject(uniqueKey, r.getInputStream(), meta, null, null);

		// THEN
		assertThat("Result success", result, equalTo(new S3ObjectRef(uniqueKey)));

		GetObjectRequest req = GetObjectRequest.builder().bucket(s3Uri.bucket().get()).key(uniqueKey)
				.build();
		try (ResponseInputStream<GetObjectResponse> obj = s3.getObject(req)) {
			assertThat("Remote content", FileCopyUtils.copyToString(new InputStreamReader(obj, UTF_8)),
					equalTo(data));

			assertThat("Remote content type", obj.response().contentType(),
					equalTo(meta.getContentType().toString()));
			assertThat("Remote Content-Disposition preserved", obj.response().contentDisposition(),
					equalTo(contentDisposition));
		}
	}

	private Path createTempFile(String data, int count) throws IOException {
		final Path tmpFile = Files.createTempFile("s3-client-test-", ".txt");
		try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(tmpFile, UTF_8))) {
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
		assertThat("Result success", result, equalTo(new S3ObjectRef(uniqueKey)));
		log.debug("Upload progress values: {}", progressAmounts);
		assertThat("Progress obtained", progressAmounts.size(), greaterThan(0));
	}

	private URL s3Url(String key) {
		try {
			return new URI(TEST_PROPS.getProperty("url") + "/" + key).toURL();
		} catch ( MalformedURLException | URISyntaxException e ) {
			throw new RuntimeException(e);
		}
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
				FileCopyUtils.copyToString(new InputStreamReader(obj.getInputStream(), UTF_8)),
				equalTo(data));
		assertThat("Object URL", obj.getURL(), equalTo(s3Url(uniqueKey)));

		S3ObjectMetadata meta = obj.getMetadata();
		assertThat("Metadata returned", meta, notNullValue());
		assertThat("Metadata modified date returned", meta.getModified(), notNullValue());
		assertThat("Metadata mod date", meta.getModified().getTime(), greaterThanOrEqualTo(start));
		assertThat("Metadata content length", meta.getSize(),
				equalTo((long) data.getBytes(UTF_8).length));
		assertThat("Metadata content type", meta.getContentType(),
				equalTo(MimeType.valueOf("text/plain; charset=utf-8")));
	}

	@Test
	public void getObject_withContentType() throws Exception {
		// GIVEN
		s3 = getS3();
		final long start = System.currentTimeMillis();
		final String uniqueKey = objectKey(UUID.randomUUID().toString());
		final String data = "Hello, world.";
		final MimeType contentType = MimeType.valueOf("text/plain; charset=utf-8");

		Map<String, String> objMeta = new LinkedHashMap<>(4);
		objMeta.put("Content-Length", String.valueOf(data.length()));
		objMeta.put("Content-Type", contentType.toString());
		s3.putObject(r -> r.bucket(s3Uri.bucket().get()).key(uniqueKey).metadata(objMeta),
				RequestBody.fromString(data));

		// WHEN
		net.solarnetwork.common.s3.S3Object obj = client.getObject(uniqueKey, null, null);

		// THEN
		assertThat("Object returned", obj, notNullValue());
		assertThat("Object content",
				FileCopyUtils.copyToString(new InputStreamReader(obj.getInputStream(), UTF_8)),
				equalTo(data));
		assertThat("Object URL", obj.getURL(), equalTo(s3Url(uniqueKey)));

		S3ObjectMetadata meta = obj.getMetadata();
		assertThat("Metadata returned", meta, notNullValue());
		assertThat("Metadata modified date returned", meta.getModified(), notNullValue());
		assertThat("Metadata mod date", meta.getModified().getTime(), greaterThanOrEqualTo(start));
		assertThat("Metadata content length", meta.getSize(),
				equalTo((long) data.getBytes(UTF_8).length));
		assertThat("Metadata content type", meta.getContentType(), equalTo(contentType));

		Map<String, ?> mm = meta.asMap();
		log.debug("Got meta map: {}", mm);
	}

	@Test
	public void getObjectAsString() throws Exception {
		// GIVEN
		s3 = getS3();
		final String uniqueKey = objectKey(UUID.randomUUID().toString());
		final String data = "Hello, world.";
		putStringOject(s3, uniqueKey, data);

		// WHEN
		String result = client.getObjectAsString(uniqueKey);

		// THEN
		assertThat("String returned", result, equalTo(data));
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
		s3.putObject(r -> r.bucket(s3Uri.bucket().get()).key(uniqueKey),
				RequestBody.fromFile(tmpFile.toFile()));

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
		assertThat("Object URL", obj.getURL(), equalTo(s3Url(uniqueKey)));

		S3ObjectMetadata meta = obj.getMetadata();
		assertThat("Metadata returned", meta, notNullValue());
		assertThat("Metadata modified date returned", meta.getModified(), notNullValue());
		assertThat("Metadata mod date", meta.getModified().getTime(), greaterThanOrEqualTo(start));
		assertThat("Metadata content length", meta.getSize(), equalTo(Files.size(tmpFile)));

		log.debug("Download progress values: {}", progressAmounts);
		assertThat("Progress obtained", progressAmounts.size(), greaterThan(0));
	}

	@Test
	public void listObjects() throws Exception {
		// GIVEN
		s3 = getS3();
		final String data = "Hello, world.";
		Set<String> keys = new LinkedHashSet<>(3);
		for ( int i = 0; i < 4; i++ ) {
			final String uniqueKey = objectKey(i + "_" + UUID.randomUUID().toString());
			putStringOject(s3, uniqueKey, data);
			keys.add(uniqueKey);
		}

		// WHEN
		Set<S3ObjectReference> results = client.listObjects(getObjectKeyPrefix());

		// THEN
		assertThat("Results returned", results, hasSize(4));
		assertThat("Result keys", results.stream().map(r -> r.getKey()).collect(Collectors.toSet()),
				equalTo(keys));
		for ( S3ObjectReference ref : results ) {
			assertThat("Object URL", ref.getURL(), equalTo(s3Url(ref.getKey())));
		}
	}

	@Test
	public void listObjects_empty() throws Exception {
		// GIVEN
		s3 = getS3();

		// WHEN
		Set<S3ObjectReference> results = client.listObjects(getObjectKeyPrefix());

		// THEN
		assertThat("Results returned", results, hasSize(0));
	}

	@Test
	public void deleteObjects_empty() throws Exception {
		// GIVEN

		// WHEN
		Set<String> deletedKeys = client.deleteObjects(Collections.emptySet());

		// THEN
		assertThat("Empty set returned", deletedKeys, hasSize(0));

	}

	@Test
	public void deleteObjects_single() throws Exception {
		// GIVEN
		s3 = getS3();
		final String uniqueKey = objectKey(UUID.randomUUID().toString());
		final String data = "Hello, world.";
		putStringOject(s3, uniqueKey, data);

		// WHEN
		Set<String> deletedKeys = client.deleteObjects(Collections.singleton(uniqueKey));

		// THEN
		assertThat("Object returned", deletedKeys, notNullValue());
		assertThat("Key deleted", deletedKeys, containsInAnyOrder(uniqueKey));

		try {
			s3.getObject(r -> r.bucket(s3Uri.bucket().get()).key(uniqueKey));
			Assert.fail("Expected not-found exception.");
		} catch ( AwsServiceException e ) {
			assertThat("Exception is not-found", e.statusCode(), equalTo(404));
		}
	}

	@Test
	public void deleteObjects_multiple() throws Exception {
		// GIVEN
		s3 = getS3();
		final String data = "Hello, world.";
		Set<String> keys = new LinkedHashSet<>(4);
		for ( int i = 0; i < 4; i++ ) {
			final String uniqueKey = objectKey(i + "_" + UUID.randomUUID().toString());
			putStringOject(s3, uniqueKey, data);
			keys.add(uniqueKey);
		}

		// WHEN
		Set<String> deletedKeys = client.deleteObjects(keys);

		// THEN
		assertThat("Object returned", deletedKeys, notNullValue());
		assertThat("Keys deleted", deletedKeys, equalTo(keys));

		ListObjectsV2Response res = s3
				.listObjectsV2(r -> r.bucket(s3Uri.bucket().get()).prefix(getObjectKeyPrefix()));
		Set<String> remainingKeys = res.contents().stream().map(s -> s.key())
				.collect(Collectors.toSet());
		for ( String k : keys ) {
			assertThat("Deleted key not remaining", remainingKeys, not(hasItem(k)));
		}
	}

	@Test
	public void deleteObjects_multiple_mixed() throws Exception {
		// GIVEN
		s3 = getS3();
		final String data = "Hello, world.";
		Set<String> keysToDelete = new LinkedHashSet<>(4);
		Set<String> keysToPreserve = new LinkedHashSet<>(4);
		for ( int i = 0; i < 4; i++ ) {
			final String uniqueKey = objectKey(i + "_" + UUID.randomUUID().toString());
			putStringOject(s3, uniqueKey, data);
			if ( i % 2 == 0 ) {
				keysToDelete.add(uniqueKey);
			} else {
				keysToPreserve.add(uniqueKey);
			}
		}

		// WHEN
		Set<String> deletedKeys = client.deleteObjects(keysToDelete);

		// THEN
		assertThat("Object returned", deletedKeys, notNullValue());
		assertThat("Keys deleted", deletedKeys, equalTo(keysToDelete));

		ListObjectsV2Response res = s3
				.listObjectsV2(r -> r.bucket(s3Uri.bucket().get()).prefix(getObjectKeyPrefix()));
		Set<String> remainingKeys = res.contents().stream().map(s -> s.key())
				.collect(Collectors.toSet());
		for ( String k : keysToDelete ) {
			assertThat("Deleted key not remaining", remainingKeys, not(hasItem(k)));
		}
		for ( String k : keysToPreserve ) {
			assertThat("Preserved key remaining", remainingKeys, hasItem(k));
		}
	}
}

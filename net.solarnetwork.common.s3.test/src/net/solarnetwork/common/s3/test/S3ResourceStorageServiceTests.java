/* ==================================================================
 * S3ResourceStorageServiceTests.java - 15/10/2019 7:02:58 am
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

package net.solarnetwork.common.s3.test;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isNull;
import static org.easymock.EasyMock.same;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.springframework.util.FileCopyUtils.copyToString;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import net.solarnetwork.common.s3.S3Client;
import net.solarnetwork.common.s3.S3ClientResource;
import net.solarnetwork.common.s3.S3ObjectMetadata;
import net.solarnetwork.common.s3.S3ObjectRef;
import net.solarnetwork.common.s3.S3ObjectReference;
import net.solarnetwork.common.s3.S3ResourceStorageService;
import net.solarnetwork.common.s3.sdk.SdkS3Client;
import net.solarnetwork.settings.SettingSpecifier;
import net.solarnetwork.settings.support.SettingUtils;
import net.solarnetwork.test.CallingThreadExecutorService;
import net.solarnetwork.util.ProgressListener;

/**
 * Test cases for the {@link S3ResourceStorageService} class.
 * 
 * @author matt
 * @version 1.0
 */
public class S3ResourceStorageServiceTests {

	private S3Client s3Client;
	private Executor executor;
	private S3ResourceStorageService service;

	@Before
	public void setup() {
		s3Client = EasyMock.createMock(S3Client.class);
		executor = new CallingThreadExecutorService();
		service = new S3ResourceStorageService(executor);
		service.setS3Client(s3Client);
	}

	@After
	public void teardown() {
		EasyMock.verify(s3Client);
	}

	private void replayAll() {
		EasyMock.replay(s3Client);
	}

	@Test
	public void settings() {
		// GIVEN
		SdkS3Client sdkClient = new SdkS3Client();
		expect(s3Client.getSettingSpecifiers()).andReturn(sdkClient.getSettingSpecifiers());

		// WHEN
		replayAll();
		List<SettingSpecifier> settings = service.getSettingSpecifiers();

		// THEN
		assertThat("Settings available", settings, notNullValue());
		Map<String, Object> settingData = SettingUtils.keyedSettingDefaults(settings);
		assertThat("Settings count", settingData.keySet(), hasSize(5));
		assertThat("Settings token value", settingData, hasEntry("s3Client.accessToken", ""));
		assertThat("Settings secret value", settingData, hasEntry("s3Client.accessSecret", ""));
		assertThat("Settings region value", settingData,
				hasEntry("s3Client.regionName", SdkS3Client.DEFAULT_REGION_NAME));
		assertThat("Settings bucket value", settingData, hasEntry("s3Client.bucketName", ""));
		assertThat("Settings pagination value", settingData, hasEntry("s3Client.maximumKeysPerRequest",
				String.valueOf(SdkS3Client.DEFAULT_MAXIMUM_KEYS_PER_REQUEST)));
	}

	@Test
	public void configured_ready() throws Exception {
		// GIVEN
		expect(s3Client.isConfigured()).andReturn(true);

		// WHEN
		replayAll();
		boolean result = service.isConfigured();

		// THEN
		assertThat("Configured when client configured", result, equalTo(true));
	}

	@Test
	public void configured_notReady() throws Exception {
		// GIVEN
		expect(s3Client.isConfigured()).andReturn(false);

		// WHEN
		replayAll();
		boolean result = service.isConfigured();

		// THEN
		assertThat("Configured when client configured", result, equalTo(false));
	}

	@Test
	public void listResources() throws Exception {
		// GIVEN
		Set<S3ObjectReference> refs = new LinkedHashSet<>(asList(new S3ObjectRef("prefix/foo")));
		expect(s3Client.listObjects("prefix/")).andReturn(refs);

		// WHEN
		replayAll();
		CompletableFuture<Iterable<Resource>> result = service.listResources("prefix/");

		// THEN
		assertThat("Result returned", result, notNullValue());
		Iterable<Resource> resources = result.get(5, TimeUnit.SECONDS);
		List<Resource> resourceList = stream(resources.spliterator(), false).collect(toList());
		assertThat("Resource list size", resourceList, hasSize(1));
		Resource r = resourceList.get(0);
		assertThat("Resource is S3ClientResource", r, instanceOf(S3ClientResource.class));

		S3ObjectReference ref = ((S3ClientResource) r).getObjectReference();
		assertThat("Reference points to expected object", ref, equalTo(new S3ObjectRef("prefix/foo")));
	}

	@Test
	public void saveResource() throws Exception {
		// GIVEN
		final String data = "Hello, world.";
		final ByteArrayResource r = new ByteArrayResource(data.getBytes());
		final Date date = new Date();
		final String path = "foo";

		Capture<InputStream> inCaptor = new Capture<>();
		Capture<S3ObjectMetadata> metaCaptor = new Capture<>();
		expect(s3Client.putObject(eq(path), capture(inCaptor), capture(metaCaptor), isNull(), same(r)))
				.andReturn(new S3ObjectRef(path, r.contentLength(), date));

		// WHEN
		replayAll();
		CompletableFuture<Boolean> result = service.saveResource(path, r, true, null);

		// THEN
		assertThat("Result returned", result, notNullValue());
		assertThat("Result completed", result.get(5, TimeUnit.SECONDS), equalTo(true));

		assertThat("InputStream provided to client", inCaptor.getValue(), notNullValue());
		assertThat("InputStream content", copyToString(new InputStreamReader(inCaptor.getValue())),
				equalTo(data));
	}

	@Test
	public void saveResource_withProgress() throws Exception {
		// GIVEN
		final String data = "Hello world.";
		final ByteArrayResource r = new ByteArrayResource(data.getBytes());
		final Date date = new Date();
		final String path = "foo";
		final List<Double> progressValues = new ArrayList<Double>(2);
		final ProgressListener<Resource> listener = new ProgressListener<Resource>() {

			@Override
			public void progressChanged(Resource context, double amountComplete) {
				assertThat("Context is upload resource", context, sameInstance(r));
				progressValues.add(amountComplete);
			}
		};

		Capture<InputStream> inCaptor = new Capture<>();
		Capture<S3ObjectMetadata> metaCaptor = new Capture<>();
		expect(s3Client.putObject(eq(path), capture(inCaptor), capture(metaCaptor), same(listener),
				same(r))).andAnswer(new IAnswer<S3ObjectReference>() {

					@Override
					public S3ObjectReference answer() throws Throwable {
						long totalSize = r.contentLength();
						long chunkSize = totalSize / 4;
						long progress = 0;

						while ( progress < totalSize ) {
							progress += chunkSize;
							if ( progress > totalSize ) {
								progress = totalSize;
							}
							listener.progressChanged(r, (double) progress / (double) totalSize);
						}
						return new S3ObjectRef(path, r.contentLength(), date);
					}
				});

		// WHEN
		replayAll();
		CompletableFuture<Boolean> result = service.saveResource(path, r, true, listener);

		// THEN
		assertThat("Result returned", result, notNullValue());
		assertThat("Result completed", result.get(5, TimeUnit.SECONDS), equalTo(true));

		assertThat("InputStream provided to client", inCaptor.getValue(), notNullValue());
		assertThat("InputStream content", copyToString(new InputStreamReader(inCaptor.getValue())),
				equalTo(data));
		assertThat("Progress handled", progressValues, contains(0.25, 0.5, 0.75, 1.0));
	}
}

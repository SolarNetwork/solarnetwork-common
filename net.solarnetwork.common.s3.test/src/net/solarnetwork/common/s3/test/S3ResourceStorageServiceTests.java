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
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isNull;
import static org.easymock.EasyMock.same;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.util.FileCopyUtils.copyToString;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import net.solarnetwork.common.s3.S3Client;
import net.solarnetwork.common.s3.S3ClientResource;
import net.solarnetwork.common.s3.S3ObjectMetadata;
import net.solarnetwork.common.s3.S3ObjectRef;
import net.solarnetwork.common.s3.S3ObjectReference;
import net.solarnetwork.common.s3.S3ResourceStorageService;
import net.solarnetwork.service.ProgressListener;
import net.solarnetwork.service.ResourceStorageService;
import net.solarnetwork.service.StaticOptionalService;
import net.solarnetwork.settings.SettingSpecifier;
import net.solarnetwork.settings.support.BasicTextFieldSettingSpecifier;
import net.solarnetwork.settings.support.SettingUtils;
import net.solarnetwork.test.CallingThreadExecutorService;

/**
 * Test cases for the {@link S3ResourceStorageService} class.
 *
 * @author matt
 * @version 2.0
 */
public class S3ResourceStorageServiceTests {

	private S3Client s3Client;
	private Executor executor;
	private EventAdmin eventAdmin;
	private S3ResourceStorageService service;

	@Before
	public void setup() {
		s3Client = EasyMock.createMock(S3Client.class);
		executor = new CallingThreadExecutorService();
		eventAdmin = EasyMock.createMock(EventAdmin.class);
		service = new S3ResourceStorageService(executor);
		service.setUid(UUID.randomUUID().toString());
		service.setGroupUid(UUID.randomUUID().toString());
		service.setS3Client(s3Client);
		service.setEventAdmin(new StaticOptionalService<>(eventAdmin));
	}

	@After
	public void teardown() {
		EasyMock.verify(s3Client, eventAdmin);
	}

	private void replayAll() {
		EasyMock.replay(s3Client, eventAdmin);
	}

	@Test
	public void settings() {
		// GIVEN
		List<SettingSpecifier> delegateSpecifiers = Arrays
				.asList(new BasicTextFieldSettingSpecifier("foo", ""));
		expect(s3Client.getSettingSpecifiers()).andReturn(delegateSpecifiers);

		// WHEN
		replayAll();
		List<SettingSpecifier> settings = service.getSettingSpecifiers();

		// THEN
		assertThat("Settings available", settings, notNullValue());
		Map<String, Object> settingData = SettingUtils.keyedSettingDefaults(settings);
		assertThat("Settings count", settingData.keySet(), hasSize(2));
		assertThat("Settings token value", settingData, hasEntry("s3Client.foo", ""));
		assertThat("Settings object path prefix", settingData, hasEntry("objectKeyPrefix", ""));
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
	public void listResources_pathPrefix() throws Exception {
		// GIVEN
		final String pathPrefix = "foo/";
		service.setObjectKeyPrefix(pathPrefix);
		final String fullPath = "foo/prefix/foo";

		Set<S3ObjectReference> refs = new LinkedHashSet<>(asList(new S3ObjectRef(fullPath)));
		expect(s3Client.listObjects("foo/prefix/")).andReturn(refs);

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
		assertThat("Reference points to expected object", ref, equalTo(new S3ObjectRef(fullPath)));
	}

	@Test
	public void listResources_pathPrefixSupplied() throws Exception {
		// GIVEN
		final String pathPrefix = "foo/";
		service.setObjectKeyPrefix(pathPrefix);
		final String fullPath = "foo/prefix/foo";

		Set<S3ObjectReference> refs = new LinkedHashSet<>(asList(new S3ObjectRef(fullPath)));
		expect(s3Client.listObjects("foo/prefix/")).andReturn(refs);

		// WHEN
		replayAll();
		CompletableFuture<Iterable<Resource>> result = service.listResources(pathPrefix + "prefix/");

		// THEN
		assertThat("Result returned", result, notNullValue());
		Iterable<Resource> resources = result.get(5, TimeUnit.SECONDS);
		List<Resource> resourceList = stream(resources.spliterator(), false).collect(toList());
		assertThat("Resource list size", resourceList, hasSize(1));
		Resource r = resourceList.get(0);
		assertThat("Resource is S3ClientResource", r, instanceOf(S3ClientResource.class));

		S3ObjectReference ref = ((S3ClientResource) r).getObjectReference();
		assertThat("Reference points to expected object", ref, equalTo(new S3ObjectRef(fullPath)));
	}

	private URL s3Url(String key) {
		try {
			return new URI("https://some-bucket.example.com/" + key).toURL();
		} catch ( MalformedURLException | URISyntaxException e ) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void resourceUrl() throws Exception {
		// GIVEN
		final String path = "foo";

		final URL url = new URI("http://localhost/foo").toURL();
		expect(s3Client.getObjectURL(path)).andReturn(url);

		// WHEN
		replayAll();
		URL result = service.resourceStorageUrl(path);

		// THEN
		assertThat("Result URL expected", result, equalTo(url));
	}

	@Test
	public void resourceUrl_pathPrefix() throws Exception {
		// GIVEN
		final String pathPrefix = "foo/";
		service.setObjectKeyPrefix(pathPrefix);

		final String path = "foo";
		final String fullPath = pathPrefix + path;

		final URL url = new URI("http://localhost/foo/foo").toURL();
		expect(s3Client.getObjectURL(fullPath)).andReturn(url);

		// WHEN
		replayAll();
		URL result = service.resourceStorageUrl(path);

		// THEN
		assertThat("Result URL expected", result, equalTo(url));
	}

	@Test
	public void saveResource() throws Exception {
		// GIVEN
		final String data = "Hello, world.";
		final ByteArrayResource r = new ByteArrayResource(data.getBytes());
		final Date date = new Date();
		final String path = "foo";

		Capture<InputStream> inCaptor = Capture.newInstance();
		Capture<S3ObjectMetadata> metaCaptor = Capture.newInstance();
		expect(s3Client.putObject(eq(path), capture(inCaptor), capture(metaCaptor), isNull(), same(r)))
				.andReturn(new S3ObjectRef(path, r.contentLength(), date, s3Url(path)));

		Capture<Event> eventCaptor = Capture.newInstance();
		eventAdmin.postEvent(capture(eventCaptor));

		// WHEN
		replayAll();
		CompletableFuture<Boolean> result = service.saveResource(path, r, true, null);

		// THEN
		assertThat("Result returned", result, notNullValue());
		assertThat("Result completed", result.get(5, TimeUnit.SECONDS), equalTo(true));

		assertThat("InputStream provided to client", inCaptor.getValue(), notNullValue());
		assertThat("InputStream content", copyToString(new InputStreamReader(inCaptor.getValue())),
				equalTo(data));

		assertThat("Event posted", eventCaptor.getValue(), notNullValue());
		assertThat("Event topic is RESOURCE_SAVED", eventCaptor.getValue().getTopic(),
				equalTo(ResourceStorageService.EVENT_TOPIC_RESOURCE_SAVED));
		assertThat("Event properties present except for URL",
				asList(eventCaptor.getValue().getPropertyNames()),
				containsInAnyOrder("event.topics", "uid", "groupUid", "paths"));
		assertThat("Event property UID", eventCaptor.getValue().getProperty("uid"),
				equalTo(service.getUid()));
		assertThat("Event property group UID", eventCaptor.getValue().getProperty("groupUid"),
				equalTo(service.getGroupUid()));
		assertThat("Event property paths", (Collection<?>) eventCaptor.getValue().getProperty("paths"),
				contains(path));
	}

	@Test
	public void saveResource_file() throws Exception {
		// GIVEN
		final String data = "Hello, world.";
		final Path tmpFile = Files.createTempFile("saveResource-", ".txt");
		FileCopyUtils.copy(data.getBytes(), tmpFile.toFile());
		final FileSystemResource r = new FileSystemResource(tmpFile.toFile());
		final Date date = new Date();
		final String path = "foo";

		Capture<InputStream> inCaptor = Capture.newInstance();
		Capture<S3ObjectMetadata> metaCaptor = Capture.newInstance();
		expect(s3Client.putObject(eq(path), capture(inCaptor), capture(metaCaptor), isNull(), same(r)))
				.andReturn(new S3ObjectRef(path, r.contentLength(), date, s3Url(path)));

		Capture<Event> eventCaptor = Capture.newInstance();
		eventAdmin.postEvent(capture(eventCaptor));

		// WHEN
		replayAll();
		CompletableFuture<Boolean> result = service.saveResource(path, r, true, null);

		// THEN
		assertThat("Result returned", result, notNullValue());
		assertThat("Result completed", result.get(5, TimeUnit.SECONDS), equalTo(true));

		assertThat("InputStream provided to client", inCaptor.getValue(), notNullValue());
		assertThat("InputStream content", copyToString(new InputStreamReader(inCaptor.getValue())),
				equalTo(data));

		assertThat("Event posted", eventCaptor.getValue(), notNullValue());
		assertThat("Event topic is RESOURCE_SAVED", eventCaptor.getValue().getTopic(),
				equalTo(ResourceStorageService.EVENT_TOPIC_RESOURCE_SAVED));
		assertThat("Event properties present including URL",
				asList(eventCaptor.getValue().getPropertyNames()),
				containsInAnyOrder("event.topics", "uid", "groupUid", "paths", "url"));
		assertThat("Event property UID", eventCaptor.getValue().getProperty("uid"),
				equalTo(service.getUid()));
		assertThat("Event property group UID", eventCaptor.getValue().getProperty("groupUid"),
				equalTo(service.getGroupUid()));
		assertThat("Event property paths", (Collection<?>) eventCaptor.getValue().getProperty("paths"),
				contains(path));
		assertThat("Event property url", eventCaptor.getValue().getProperty("url"),
				equalTo(r.getURL().toString()));

		Files.deleteIfExists(tmpFile);
	}

	@Test
	public void saveResource_pathPrefix() throws Exception {
		// GIVEN
		final String pathPrefix = "foo/";
		service.setObjectKeyPrefix(pathPrefix);

		final String data = "Hello, world.";
		final ByteArrayResource r = new ByteArrayResource(data.getBytes());
		final Date date = new Date();
		final String path = "foo";
		final String fullPath = pathPrefix + path;

		Capture<InputStream> inCaptor = Capture.newInstance();
		Capture<S3ObjectMetadata> metaCaptor = Capture.newInstance();
		expect(s3Client.putObject(eq(fullPath), capture(inCaptor), capture(metaCaptor), isNull(),
				same(r))).andReturn(new S3ObjectRef(fullPath, r.contentLength(), date, s3Url(fullPath)));

		Capture<Event> eventCaptor = Capture.newInstance();
		eventAdmin.postEvent(capture(eventCaptor));

		// WHEN
		replayAll();
		CompletableFuture<Boolean> result = service.saveResource(path, r, true, null);

		// THEN
		assertThat("Result returned", result, notNullValue());
		assertThat("Result completed", result.get(5, TimeUnit.SECONDS), equalTo(true));

		assertThat("InputStream provided to client", inCaptor.getValue(), notNullValue());
		assertThat("InputStream content", copyToString(new InputStreamReader(inCaptor.getValue())),
				equalTo(data));

		assertThat("Event topic is RESOURCE_SAVED", eventCaptor.getValue().getTopic(),
				equalTo(ResourceStorageService.EVENT_TOPIC_RESOURCE_SAVED));
		assertThat("Event property paths", (Collection<?>) eventCaptor.getValue().getProperty("paths"),
				contains(path));
	}

	@Test
	public void saveResource_pathPrefixSupplied() throws Exception {
		// GIVEN
		final String pathPrefix = "foo/";
		service.setObjectKeyPrefix(pathPrefix);

		final String data = "Hello, world.";
		final ByteArrayResource r = new ByteArrayResource(data.getBytes());
		final Date date = new Date();
		final String path = "foo";
		final String fullPath = pathPrefix + path;

		Capture<InputStream> inCaptor = Capture.newInstance();
		Capture<S3ObjectMetadata> metaCaptor = Capture.newInstance();
		expect(s3Client.putObject(eq(fullPath), capture(inCaptor), capture(metaCaptor), isNull(),
				same(r))).andReturn(new S3ObjectRef(fullPath, r.contentLength(), date, s3Url(fullPath)));

		Capture<Event> eventCaptor = Capture.newInstance();
		eventAdmin.postEvent(capture(eventCaptor));

		// WHEN
		replayAll();
		CompletableFuture<Boolean> result = service.saveResource(fullPath, r, true, null);

		// THEN
		assertThat("Result returned", result, notNullValue());
		assertThat("Result completed", result.get(5, TimeUnit.SECONDS), equalTo(true));

		assertThat("InputStream provided to client", inCaptor.getValue(), notNullValue());
		assertThat("InputStream content", copyToString(new InputStreamReader(inCaptor.getValue())),
				equalTo(data));

		assertThat("Event topic is RESOURCE_SAVED", eventCaptor.getValue().getTopic(),
				equalTo(ResourceStorageService.EVENT_TOPIC_RESOURCE_SAVED));
		assertThat("Event property paths", (Collection<?>) eventCaptor.getValue().getProperty("paths"),
				contains(fullPath));
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

		Capture<InputStream> inCaptor = Capture.newInstance();
		Capture<S3ObjectMetadata> metaCaptor = Capture.newInstance();
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
						return new S3ObjectRef(path, r.contentLength(), date, s3Url(path));
					}
				});

		Capture<Event> eventCaptor = Capture.newInstance();
		eventAdmin.postEvent(capture(eventCaptor));

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

		assertThat("Event topic is RESOURCE_SAVED", eventCaptor.getValue().getTopic(),
				equalTo(ResourceStorageService.EVENT_TOPIC_RESOURCE_SAVED));
		assertThat("Event property paths", (Collection<?>) eventCaptor.getValue().getProperty("paths"),
				contains(path));
	}

	@Test
	public void deleteResources() throws Exception {
		// GIVEN
		Set<String> pathsToDelete = new LinkedHashSet<>(asList("one", "two", "three"));

		expect(s3Client.deleteObjects(pathsToDelete)).andReturn(pathsToDelete);

		Capture<Event> eventCaptor = Capture.newInstance();
		eventAdmin.postEvent(capture(eventCaptor));

		// WHEN
		replayAll();
		CompletableFuture<Set<String>> result = service.deleteResources(pathsToDelete);

		// THEN
		assertThat("Result returned", result, notNullValue());
		Set<String> notDeleted = result.get(5, TimeUnit.SECONDS);
		assertThat("Result completed", notDeleted, hasSize(0));

		assertThat("Event posted", eventCaptor.getValue(), notNullValue());
		assertThat("Event topic is RESOURCES_DELETED", eventCaptor.getValue().getTopic(),
				equalTo(ResourceStorageService.EVENT_TOPIC_RESOURCES_DELETED));
		assertThat("Event properties present", asList(eventCaptor.getValue().getPropertyNames()),
				containsInAnyOrder("event.topics", "uid", "groupUid", "paths"));
		assertThat("Event property UID", eventCaptor.getValue().getProperty("uid"),
				equalTo(service.getUid()));
		assertThat("Event property group UID", eventCaptor.getValue().getProperty("groupUid"),
				equalTo(service.getGroupUid()));
		assertThat("Event property paths", (Collection<?>) eventCaptor.getValue().getProperty("paths"),
				equalTo(pathsToDelete));
	}

	@Test
	public void deleteResources_pathPrefix() throws Exception {
		// GIVEN
		final String pathPrefix = "foo/";
		service.setObjectKeyPrefix(pathPrefix);

		Set<String> pathsToDelete = new LinkedHashSet<>(asList("one", "two", "three"));
		Set<String> fullPathsToDelete = pathsToDelete.stream().map(s -> pathPrefix + s).collect(toSet());

		expect(s3Client.deleteObjects(fullPathsToDelete)).andReturn(fullPathsToDelete);

		Capture<Event> eventCaptor = Capture.newInstance();
		eventAdmin.postEvent(capture(eventCaptor));

		// WHEN
		replayAll();
		CompletableFuture<Set<String>> result = service.deleteResources(pathsToDelete);

		// THEN
		assertThat("Result returned", result, notNullValue());
		Set<String> notDeleted = result.get(5, TimeUnit.SECONDS);
		assertThat("Result completed", notDeleted, hasSize(0));

		assertThat("Event posted", eventCaptor.getValue(), notNullValue());
		assertThat("Event topic is RESOURCES_DELETED", eventCaptor.getValue().getTopic(),
				equalTo(ResourceStorageService.EVENT_TOPIC_RESOURCES_DELETED));
		assertThat("Event property paths", (Collection<?>) eventCaptor.getValue().getProperty("paths"),
				equalTo(pathsToDelete));
	}

	@Test
	public void deleteResources_pathPrefixSupplied() throws Exception {
		// GIVEN
		final String pathPrefix = "foo/";
		service.setObjectKeyPrefix(pathPrefix);

		Set<String> pathsToDelete = new LinkedHashSet<>(asList("one", "two", "three"));
		Set<String> fullPathsToDelete = pathsToDelete.stream().map(s -> pathPrefix + s).collect(toSet());

		expect(s3Client.deleteObjects(fullPathsToDelete)).andReturn(fullPathsToDelete);

		Capture<Event> eventCaptor = Capture.newInstance();
		eventAdmin.postEvent(capture(eventCaptor));

		// WHEN
		replayAll();
		CompletableFuture<Set<String>> result = service.deleteResources(fullPathsToDelete);

		// THEN
		assertThat("Result returned", result, notNullValue());
		Set<String> notDeleted = result.get(5, TimeUnit.SECONDS);
		assertThat("Result completed", notDeleted, hasSize(0));

		assertThat("Event posted", eventCaptor.getValue(), notNullValue());
		assertThat("Event topic is RESOURCES_DELETED", eventCaptor.getValue().getTopic(),
				equalTo(ResourceStorageService.EVENT_TOPIC_RESOURCES_DELETED));
		assertThat("Event property paths", (Collection<?>) eventCaptor.getValue().getProperty("paths"),
				equalTo(fullPathsToDelete));
	}

	@Test
	public void deleteResources_pathPrefixSuppliedSomewhat() throws Exception {
		// GIVEN
		final String pathPrefix = "foo/";
		service.setObjectKeyPrefix(pathPrefix);

		Set<String> pathsToDelete = new LinkedHashSet<>(asList("one", "two", "three"));
		Set<String> mixedPathsToDelete = new LinkedHashSet<>(asList("one", "two", pathPrefix + "three"));
		Set<String> fullPathsToDelete = pathsToDelete.stream().map(s -> pathPrefix + s).collect(toSet());

		expect(s3Client.deleteObjects(fullPathsToDelete)).andReturn(fullPathsToDelete);

		Capture<Event> eventCaptor = Capture.newInstance();
		eventAdmin.postEvent(capture(eventCaptor));

		// WHEN
		replayAll();
		CompletableFuture<Set<String>> result = service.deleteResources(mixedPathsToDelete);

		// THEN
		assertThat("Result returned", result, notNullValue());
		Set<String> notDeleted = result.get(5, TimeUnit.SECONDS);
		assertThat("Result completed", notDeleted, hasSize(0));

		assertThat("Event posted", eventCaptor.getValue(), notNullValue());
		assertThat("Event topic is RESOURCES_DELETED", eventCaptor.getValue().getTopic(),
				equalTo(ResourceStorageService.EVENT_TOPIC_RESOURCES_DELETED));
		assertThat("Event property paths", (Collection<?>) eventCaptor.getValue().getProperty("paths"),
				equalTo(mixedPathsToDelete));
	}

	@Test
	public void deleteResources_notAllKeysExist() throws Exception {
		// GIVEN
		Set<String> pathsToDelete = new LinkedHashSet<>(asList("one", "two", "three", "four"));
		Set<String> deletedKeys = new LinkedHashSet<>(asList("one", "three"));
		expect(s3Client.deleteObjects(pathsToDelete)).andReturn(deletedKeys);

		Capture<Event> eventCaptor = Capture.newInstance();
		eventAdmin.postEvent(capture(eventCaptor));

		// WHEN
		replayAll();
		CompletableFuture<Set<String>> result = service.deleteResources(pathsToDelete);

		// THEN
		assertThat("Result returned", result, notNullValue());
		Set<String> notDeleted = result.get(5, TimeUnit.SECONDS);
		assertThat("Result completed", notDeleted, containsInAnyOrder("two", "four"));

		assertThat("Event posted", eventCaptor.getValue(), notNullValue());
		assertThat("Event topic is RESOURCES_DELETED", eventCaptor.getValue().getTopic(),
				equalTo(ResourceStorageService.EVENT_TOPIC_RESOURCES_DELETED));
		assertThat("Event property paths", (Collection<?>) eventCaptor.getValue().getProperty("paths"),
				equalTo(pathsToDelete));
	}
}

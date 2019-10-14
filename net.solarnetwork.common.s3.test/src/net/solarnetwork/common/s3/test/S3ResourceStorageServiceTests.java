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
import static org.easymock.EasyMock.expect;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.Resource;
import net.solarnetwork.common.s3.S3Client;
import net.solarnetwork.common.s3.S3ClientResource;
import net.solarnetwork.common.s3.S3ObjectReference;
import net.solarnetwork.common.s3.S3ResourceStorageService;
import net.solarnetwork.common.s3.sdk.SdkS3Client;
import net.solarnetwork.settings.SettingSpecifier;
import net.solarnetwork.settings.support.SettingUtils;
import net.solarnetwork.test.CallingThreadExecutorService;

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
	public void listResources() throws Exception {
		// GIVEN
		Set<S3ObjectReference> refs = new LinkedHashSet<>(asList(new S3ObjectReference("prefix/foo")));
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
		assertThat("Reference points to expected object", ref,
				equalTo(new S3ObjectReference("prefix/foo")));
	}

}

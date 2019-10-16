/* ==================================================================
 * S3ResourceStorageService.java - 14/10/2019 5:31:45 pm
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

package net.solarnetwork.common.s3;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Function;
import org.springframework.core.io.Resource;
import org.springframework.util.MimeType;
import net.solarnetwork.common.s3.sdk.SdkS3Client;
import net.solarnetwork.io.ResourceMetadata;
import net.solarnetwork.io.ResourceMetadataHolder;
import net.solarnetwork.io.ResourceStorageService;
import net.solarnetwork.settings.SettingSpecifier;
import net.solarnetwork.settings.SettingSpecifierProvider;
import net.solarnetwork.settings.SettingsChangeObserver;
import net.solarnetwork.settings.support.BaseSettingsSpecifierLocalizedServiceInfoProvider;
import net.solarnetwork.settings.support.BasicTextFieldSettingSpecifier;
import net.solarnetwork.settings.support.SettingUtils;
import net.solarnetwork.util.ProgressListener;

/**
 * AWS S3 based implementation of {@link ResourceStorageService} using the
 * {@link S3Client} API.
 * 
 * @author matt
 * @version 1.0
 */
public class S3ResourceStorageService extends BaseSettingsSpecifierLocalizedServiceInfoProvider<String>
		implements ResourceStorageService, SettingSpecifierProvider, SettingsChangeObserver {

	private S3Client s3Client;
	private Executor executor;
	private String objectKeyPrefix;

	/**
	 * Constructor.
	 * 
	 * @param executor
	 *        the executor to use
	 * @throws IllegalArgumentException
	 *         if {@code executor} is {@literal null}
	 */
	public S3ResourceStorageService(Executor executor) {
		this(S3ResourceStorageService.class.getName(), executor);
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *        the settings UID to use
	 * @param executor
	 *        the executor to use
	 * @throws IllegalArgumentException
	 *         if {@code executor} is {@literal null}
	 */
	public S3ResourceStorageService(String id, Executor executor) {
		super(id);
		setS3Client(new SdkS3Client());
		setExecutor(executor);
	}

	@Override
	public void configurationChanged(Map<String, Object> properties) {
		S3Client client = getS3Client();
		if ( client instanceof SettingsChangeObserver ) {
			((SettingsChangeObserver) client).configurationChanged(properties);
		}
	}

	private String mapPathPrefix(String prefix, String path) {
		if ( path != null && prefix != null && !path.startsWith(prefix) ) {
			return prefix + path;
		}
		return path;
	}

	private Function<String, String> pathPrefixMapper() {
		final String prefix = getObjectKeyPrefix();
		return s -> mapPathPrefix(prefix, s);
	}

	@Override
	public boolean isConfigured() {
		return s3Client.isConfigured();
	}

	/**
	 * Execute a callable, setting the returned object as a completable future's
	 * result.
	 * 
	 * @param <R>
	 *        the future result value type
	 * @param future
	 *        the completable future to manage
	 * @param task
	 *        the task to execute
	 */
	private <R> void execute(CompletableFuture<R> future, Callable<R> task) {
		final Executor executor = getExecutor();
		try {
			executor.execute(alwaysComplete(future, task));
		} catch ( RejectedExecutionException e ) {
			future.completeExceptionally(e);
		}
	}

	private <R> Runnable alwaysComplete(CompletableFuture<R> future, Callable<R> r) {
		return new Runnable() {

			@Override
			public void run() {
				try {
					R result = r.call();
					future.complete(result);
				} catch ( Throwable t ) {
					future.completeExceptionally(t);
				} finally {
					if ( !future.isDone() ) {
						future.completeExceptionally(new RuntimeException("Task failed to complete!"));
					}
				}
			}
		};
	}

	@Override
	public CompletableFuture<Iterable<Resource>> listResources(String pathPrefix) {
		final String prefix = mapPathPrefix(objectKeyPrefix, pathPrefix);
		final CompletableFuture<Iterable<Resource>> result = new CompletableFuture<>();
		execute(result, new Callable<Iterable<Resource>>() {

			@Override
			public Iterable<Resource> call() throws Exception {
				S3Client c = getS3Client();
				Set<S3ObjectReference> refs = c.listObjects(prefix);
				return refs.stream().map(r -> new S3ClientResource(c, r)).collect(toList());
			}

		});
		return result;
	}

	@Override
	public CompletableFuture<Boolean> saveResource(String path, Resource resource, boolean replace,
			ProgressListener<Resource> progressListener) {
		final String p = mapPathPrefix(objectKeyPrefix, path);
		final CompletableFuture<Boolean> result = new CompletableFuture<>();
		execute(result, new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				S3Client c = getS3Client();
				if ( !replace ) {
					S3Object o = c.getObject(p, null, null);
					if ( o != null ) {
						return false;
					}
				}
				long size = -1;
				Date modified = null;
				MimeType contentType = null;
				Map<String, ?> extendedMetadata = null;

				ResourceMetadata resourceMeta = null;
				if ( resource instanceof ResourceMetadata ) {
					resourceMeta = (ResourceMetadata) resource;
				} else if ( resource instanceof ResourceMetadataHolder ) {
					resourceMeta = ((ResourceMetadataHolder) resource).getMetadata();
				}
				if ( resourceMeta != null ) {
					modified = resourceMeta.getModified();
					contentType = resourceMeta.getContentType();
					if ( resourceMeta instanceof S3ObjectMetadata ) {
						S3ObjectMetadata s3ResourceMeta = (S3ObjectMetadata) resourceMeta;
						size = s3ResourceMeta.getSize();
					}
					extendedMetadata = resourceMeta.asMap();
				}

				// fall back to extracting basic metadata directly from the resource
				if ( size < 0 ) {
					size = resource.contentLength();
				}
				if ( modified == null ) {
					try {
						File file = resource.getFile();
						modified = new Date(file.lastModified());
					} catch ( FileNotFoundException e ) {
						// ignore
					}
				}

				S3ObjectMeta meta = new S3ObjectMeta(size, modified, contentType, extendedMetadata);
				c.putObject(p, resource.getInputStream(), meta, progressListener, resource);
				return true;
			}
		});
		return result;
	}

	private static <T> Set<T> asSet(Iterable<T> iterable) {
		if ( iterable == null ) {
			return Collections.emptySet();
		}
		if ( iterable instanceof Set<?> ) {
			return (Set<T>) iterable;
		}
		return stream(iterable.spliterator(), false).collect(toSet());
	}

	@Override
	public CompletableFuture<Set<String>> deleteResources(Iterable<String> paths) {
		final Set<String> p = stream(paths.spliterator(), false).map(pathPrefixMapper())
				.collect(toSet());
		final CompletableFuture<Set<String>> result = new CompletableFuture<>();
		execute(result, new Callable<Set<String>>() {

			@Override
			public Set<String> call() throws Exception {
				S3Client c = getS3Client();

				Set<String> deletedPaths = c.deleteObjects(p);
				Set<String> notDeleted = new LinkedHashSet<>(asSet(p));
				for ( Iterator<String> itr = notDeleted.iterator(); itr.hasNext(); ) {
					if ( deletedPaths.contains(itr.next()) ) {
						itr.remove();
					}
				}
				return notDeleted;
			}
		});
		return result;
	}

	// SettingSpecifierProvider

	@Override
	public String getDisplayName() {
		return "AWS SDK S3 Resource Storage Service";
	}

	@Override
	public List<SettingSpecifier> getSettingSpecifiers() {
		List<SettingSpecifier> result = new ArrayList<>(8);
		S3Client c = getS3Client();
		if ( c != null ) {
			List<SettingSpecifier> clientSettings = SettingUtils
					.mappedWithPrefix(c.getSettingSpecifiers(), "s3Client.");
			if ( clientSettings != null ) {
				result.addAll(clientSettings);
			}
		}
		result.add(new BasicTextFieldSettingSpecifier("objectKeyPrefix", ""));
		return result;
	}

	// Accessors

	/**
	 * Get the S3 client.
	 * 
	 * @return the client, never {@literal null}
	 */
	public S3Client getS3Client() {
		return s3Client;
	}

	/**
	 * Set the S3 client.
	 * 
	 * @param s3Client
	 *        the client to set
	 * @throws IllegalArgumentException
	 *         if {@code s3Client} is {@literal null}
	 */
	public void setS3Client(S3Client s3Client) {
		if ( s3Client == null ) {
			throw new IllegalArgumentException("The S3 client argument must not be null.");
		}
		this.s3Client = s3Client;
	}

	/**
	 * Get the executor that handles asynchronous operations.
	 * 
	 * @return the executor, never {@literal null}
	 */
	public Executor getExecutor() {
		return executor;
	}

	/**
	 * Set the executor that handles asynchronous operations.
	 * 
	 * @param executor
	 *        the executor to set
	 * @throws IllegalArgumentException
	 *         if {@code Executor} is {@literal null}
	 */
	public void setExecutor(Executor executor) {
		if ( executor == null ) {
			throw new IllegalArgumentException("The executor argument must not be null.");
		}
		this.executor = executor;
	}

	/**
	 * Get the S3 object key prefix.
	 * 
	 * @return the prefix to use, or {@literal null}
	 */
	public String getObjectKeyPrefix() {
		return objectKeyPrefix;
	}

	/**
	 * Set a S3 object key prefix to use.
	 * 
	 * <p>
	 * This can essentially be a folder path to prefix all data with. All keys
	 * passed to all methods that do <b>not</b> already start with this prefix
	 * will have the prefix added before passing the operation to S3.
	 * </p>
	 * 
	 * @param objectKeyPrefix
	 *        the object key prefix to set, or {@literal null} for no prefix
	 */
	public void setObjectKeyPrefix(String objectKeyPrefix) {
		this.objectKeyPrefix = objectKeyPrefix;
	}

}

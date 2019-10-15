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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.stream.Collectors;
import org.springframework.core.io.Resource;
import net.solarnetwork.common.s3.sdk.SdkS3Client;
import net.solarnetwork.io.ResourceStorageService;
import net.solarnetwork.settings.SettingSpecifier;
import net.solarnetwork.settings.SettingSpecifierProvider;
import net.solarnetwork.settings.SettingsChangeObserver;
import net.solarnetwork.settings.support.BaseSettingsSpecifierLocalizedServiceInfoProvider;
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
		final CompletableFuture<Iterable<Resource>> result = new CompletableFuture<>();
		execute(result, new Callable<Iterable<Resource>>() {

			@Override
			public Iterable<Resource> call() throws Exception {
				S3Client c = getS3Client();
				Set<S3ObjectReference> refs = c.listObjects(pathPrefix);
				return refs.stream().map(r -> new S3ClientResource(c, r)).collect(Collectors.toList());
			}

		});
		return result;
	}

	@Override
	public CompletableFuture<Boolean> saveResource(String path, Resource resource, boolean replace,
			ProgressListener<Resource> progressListener) {
		final CompletableFuture<Boolean> result = new CompletableFuture<>();
		execute(result, new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				S3Client c = getS3Client();
				if ( !replace ) {
					S3Object o = c.getObject(path, null, null);
					if ( o != null ) {
						return false;
					}
				}
				long size = resource.contentLength();
				Date modified = null;
				try {
					File file = resource.getFile();
					modified = new Date(file.lastModified());
				} catch ( FileNotFoundException e ) {
					// ignore
				}
				S3ObjectMeta meta = new S3ObjectMeta(size, modified);
				c.putObject(path, resource.getInputStream(), meta, progressListener, resource);
				return true;
			}
		});
		return result;
	}

	@Override
	public CompletableFuture<Set<String>> deleteResources(Iterable<String> paths) {
		// TODO Auto-generated method stub
		return null;
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

}

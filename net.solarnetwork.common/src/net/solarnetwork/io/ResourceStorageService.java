/* ==================================================================
 * ResourceStorageService.java - 14/10/2019 3:12:43 pm
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

package net.solarnetwork.io;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.springframework.core.io.Resource;
import net.solarnetwork.util.ProgressListener;

/**
 * Service API for a repository of resources, much like a very simple virtual
 * file system.
 * 
 * @author matt
 * @version 1.0
 * @since 1.54
 */
public interface ResourceStorageService {

	/**
	 * Test if the service is configured and ready for use.
	 * 
	 * <p>
	 * A service might not be available for use even if registered at runtime.
	 * For example a remote storage service might require credentials to be
	 * configured before it can be used.
	 * </p>
	 * 
	 * @return {@literal true} if the service is configured and ready for use
	 */
	boolean isConfigured();

	/**
	 * Asynchronously get a set of resources.
	 * 
	 * @param pathPrefix
	 *        an optional prefix to limit the listed resources to, or
	 *        {@literal null} for all available resources
	 * @return a future that returns the list of matching resources
	 */
	CompletableFuture<Iterable<Resource>> listResources(String pathPrefix);

	/**
	 * Asynchronously save a resource.
	 * 
	 * @param path
	 *        the unique path to save the resource to
	 * @param resource
	 *        the resource to save
	 * @param replace
	 *        {@literal true} to replace, {@literal false} to skip if resource
	 *        already exists at {@code path}
	 * @param progressListener
	 *        an optional progress listener
	 * @return a future that returns {@literal true} if the resource was saved,
	 *         or {@literal false} if {@code replace} was {@literal false} and a
	 *         resource already existed at {@code path}
	 */
	CompletableFuture<Boolean> saveResource(String path, Resource resource, boolean replace,
			ProgressListener<Resource> progressListener);

	/**
	 * Asynchronously delete a set of resources.
	 * 
	 * @param path
	 *        the unique path of the resource to delete
	 * @return a future that returns the set of {@code path} values that were
	 *         <b>not</b> deleted, or an empty set if resources for all
	 *         {@code paths} were deleted
	 */
	CompletableFuture<Set<String>> deleteResources(Iterable<String> paths);

}
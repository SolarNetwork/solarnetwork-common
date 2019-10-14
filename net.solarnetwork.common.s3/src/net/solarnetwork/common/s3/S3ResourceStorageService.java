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

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.springframework.core.io.Resource;
import net.solarnetwork.io.ResourceStorageService;
import net.solarnetwork.settings.SettingSpecifier;
import net.solarnetwork.settings.SettingSpecifierProvider;
import net.solarnetwork.settings.support.BaseSettingsSpecifierLocalizedServiceInfoProvider;
import net.solarnetwork.util.ProgressListener;

/**
 * AWS S3 based implementation of {@link ResourceStorageService}.
 * 
 * @author matt
 * @version 1.0
 */
public class S3ResourceStorageService extends BaseSettingsSpecifierLocalizedServiceInfoProvider<String>
		implements ResourceStorageService, SettingSpecifierProvider {

	private S3Client s3Client;

	/**
	 * Default constructor.
	 */
	public S3ResourceStorageService() {
		this(S3ResourceStorageService.class.getName());
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *        the settings UID to use
	 */
	public S3ResourceStorageService(String id) {
		super(id);
		s3Client = new SdkS3Client();
	}

	@Override
	public boolean isConfigured() {
		return s3Client.isConfigured();
	}

	@Override
	public CompletableFuture<Iterable<Resource>> listResources(String pathPrefix) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Boolean> saveResource(String path, Resource resource, boolean replace,
			ProgressListener<Resource> progressListener) {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
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

}

/* ==================================================================
 * S3Client.java - 3/10/2017 2:11:58 PM
 * 
 * Copyright 2017 SolarNetwork.net Dev Team
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

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import net.solarnetwork.settings.SettingSpecifierProvider;
import net.solarnetwork.util.ProgressListener;

/**
 * API for accessing S3.
 * 
 * @author matt
 * @version 1.0
 */
public interface S3Client extends SettingSpecifierProvider {

	/**
	 * Test if the client is fully configured.
	 * 
	 * @return {@literal true} if the client is configured
	 */
	boolean isConfigured();

	/**
	 * List all available objects matching a prefix.
	 * 
	 * @param prefix
	 *        the prefix to match
	 * @return the matching objects, never {@literal null}
	 */
	Set<S3ObjectReference> listObjects(String prefix) throws IOException;

	/**
	 * Get the contents of a S3 object as a string.
	 * 
	 * <p>
	 * The S3 object is assumed to use the {@code UTF-8} character set.
	 * </p>
	 * 
	 * @param key
	 *        the key of the object to get
	 * @return the string, or {@literal null} if not found
	 */
	String getObjectAsString(String key) throws IOException;

	/**
	 * Get a S3 object.
	 * 
	 * <p>
	 * Note that the returned object's data may not be fetched from S3 until the
	 * {@code InputStream} returned by {@link S3Object#getInputStream()} is
	 * read. That means the {@code progressListener} might not have any progress
	 * callbacks until that time as well.
	 * </p>
	 * 
	 * @param key
	 *        the key of the object to get
	 * @param progressListener
	 *        an optional progress listener
	 * @param progressContext
	 *        an optional progress context
	 * @return the object, or {@literal null} if not found
	 */
	<P> S3Object getObject(String key, ProgressListener<P> progressListener, P progressContext)
			throws IOException;

	/**
	 * Put an object onto S3.
	 * 
	 * @param <P>
	 *        the progress context type
	 * @param key
	 *        the key of the object to put
	 * @param in
	 *        the object contents
	 * @param objectMetadata
	 *        the object metadata
	 * @param progressListener
	 *        an optional progress listener
	 * @param progressContext
	 *        an optional progress context
	 * @return a reference to the put object
	 * @throws IOException
	 *         if an IO error occurs
	 */
	<P> S3ObjectReference putObject(String key, InputStream in, S3ObjectMetadata objectMetadata,
			ProgressListener<P> progressListener, P progressContext) throws IOException;

	/**
	 * Delete a set of keys from S3.
	 * 
	 * @param keys
	 *        the keys to delete
	 * @return the keys that were deleted
	 */
	Set<String> deleteObjects(Iterable<String> keys) throws IOException;

}

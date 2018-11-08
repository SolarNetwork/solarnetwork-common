/* ==================================================================
 * TransferrableResource.java - 9/11/2018 10:25:29 AM
 * 
 * Copyright 2018 SolarNetwork.net Dev Team
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

import java.io.File;
import java.io.IOException;

/**
 * A resource that can be transferred to a file.
 * 
 * <p>
 * This can be useful in some situations where a resource is only temporary and
 * must be moved somewhere to persist the data durably.
 * </p>
 * 
 * @author matt
 * @version 1.0
 * @since 1.47
 */
public interface TransferrableResource {

	/**
	 * Transfer this resource to the given destination file.
	 * 
	 * <p>
	 * This may either move the file in the filesystem, copy the file in the
	 * filesystem, or save memory-held contents to the destination file. If the
	 * destination file already exists, it will be deleted first.
	 * </p>
	 * <p>
	 * If the file has been moved in the filesystem, this operation cannot be
	 * invoked again. Therefore, call this method just once to be able to work
	 * with any storage mechanism.
	 * </p>
	 * 
	 * @param dest
	 *        the destination file
	 * @throws IOException
	 *         in case of reading or writing errors
	 * @throws IllegalStateException
	 *         if the file has already been moved in the filesystem and is not
	 *         available anymore for another transfer
	 */
	void transferTo(File dest) throws IOException, IllegalStateException;

}

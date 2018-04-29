/* ==================================================================
 * DeleteOnCloseFileResource.java - 23/04/2018 11:54:13 AM
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
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import org.springframework.core.io.Resource;

/**
 * A {@link Resource} that deletes any associated {@link File} once the stream
 * returned by {@link #getInputStream()} is closed.
 * 
 * @author matt
 * @version 1.0
 * @since 1.43
 */
public class DeleteOnCloseFileResource implements Resource {

	private final File file;
	private final Resource delegate;

	/**
	 * Construct from a resource.
	 * 
	 * <p>
	 * The {@link Resource#getFile()} method will determine the file to delete.
	 * </p>
	 * 
	 * @param delegate
	 *        the resource to delegate to
	 * @throws IOException
	 *         if a file cannot be determined for the resource
	 */
	public DeleteOnCloseFileResource(Resource delegate) throws IOException {
		this(delegate, delegate.getFile());
	}

	/**
	 * Construct from a resource and explicit file.
	 * 
	 * @param delegate
	 *        the resource to delegate to
	 * @param file
	 *        the file to delete after reading
	 */
	public DeleteOnCloseFileResource(Resource delegate, File file) {
		super();
		this.delegate = delegate;
		this.file = file;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return new DeleteOnCloseFileInputStream(file);
	}

	@Override
	public boolean exists() {
		return delegate.exists();
	}

	@Override
	public boolean isReadable() {
		return delegate.isReadable();
	}

	@Override
	public boolean isOpen() {
		return delegate.isOpen();
	}

	@Override
	public URL getURL() throws IOException {
		return delegate.getURL();
	}

	@Override
	public URI getURI() throws IOException {
		return delegate.getURI();
	}

	@Override
	public File getFile() throws IOException {
		return delegate.getFile();
	}

	@Override
	public long contentLength() throws IOException {
		return delegate.contentLength();
	}

	@Override
	public long lastModified() throws IOException {
		return delegate.lastModified();
	}

	@Override
	public Resource createRelative(String relativePath) throws IOException {
		return delegate.createRelative(relativePath);
	}

	@Override
	public String getFilename() {
		return delegate.getFilename();
	}

	@Override
	public String getDescription() {
		return delegate.getDescription();
	}

}

/* ==================================================================
 * ConcatenatingResource.java - 24/05/2018 6:45:27 AM
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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;

/**
 * A collection of resources concatenated together into one virtual resource.
 * 
 * <p>
 * The {@link InputStream} returned by this resource will be a concatenating
 * stream of all the configured resources. Other methods will delegate to the
 * first resource only, such as {@link #lastModified()}.
 * 
 * @author matt
 * @version 1.0
 * @since 1.44
 */
public class ConcatenatingResource extends AbstractResource implements Resource {

	private final List<Resource> delegates;

	public ConcatenatingResource(Collection<Resource> resources) {
		super();
		if ( resources == null ) {
			delegates = Collections.emptyList();
		} else if ( resources instanceof List ) {
			delegates = (List<Resource>) resources;
		} else {
			delegates = new ArrayList<Resource>(resources);
		}
	}

	private Resource getDelegate() throws IOException {
		if ( delegates.isEmpty() ) {
			throw new IOException("No resource available");
		}
		return delegates.get(0);
	}

	/**
	 * Get a concatenating input stream of all configured resources combined.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public InputStream getInputStream() throws IOException {
		InputStream[] streams = delegates.toArray(new InputStream[delegates.size()]);
		int i = 0;
		for ( Resource r : delegates ) {
			streams[i] = r.getInputStream();
			i++;
		}
		return new ConcatenatingInputStream(streams);
	}

	/**
	 * Get the total length of all configured resources combined.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public long contentLength() throws IOException {
		long total = 0;
		for ( Resource r : delegates ) {
			total += r.contentLength();
		}
		return total;
	}

	/**
	 * Get the modification date of the first resource.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public long lastModified() throws IOException {
		return getDelegate().lastModified();
	}

	@Override
	public String getDescription() {
		StringBuilder buf = new StringBuilder("ConcatenatingResource{");
		int i = 0;
		for ( Resource r : delegates ) {
			if ( i > 0 ) {
				buf.append(", ");
			}
			buf.append(r.getDescription());
			i++;
		}
		buf.append("}");
		return buf.toString();
	}

	@Override
	public String toString() {
		return getDescription();
	}

}

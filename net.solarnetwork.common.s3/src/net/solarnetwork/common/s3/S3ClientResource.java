/* ==================================================================
 * S3ClientResource.java - 15/10/2019 10:40:01 am
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;

/**
 * Resource implementation for a remote S3 object using the {@link S3Client}
 * API.
 * 
 * @author matt
 * @version 1.0
 */
public class S3ClientResource extends AbstractResource implements Resource {

	private final S3Client client;
	private final S3ObjectReference ref;

	/**
	 * Constructor.
	 * 
	 * @param client
	 *        the client to use
	 * @throws IllegalArgumentException
	 *         if either {@code client} or {@code ref} are {@literal null}
	 */
	public S3ClientResource(S3Client client, S3ObjectReference ref) {
		super();
		if ( client == null ) {
			throw new IllegalArgumentException("The S3Client argument must not be null.");
		}
		if ( ref == null ) {
			throw new IllegalArgumentException("The S3ObjectRef argument must not be null.");
		}
		this.client = client;
		this.ref = ref;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		S3Object obj = client.getObject(ref.getKey(), null, null);
		return obj.getInputStream();
	}

	/**
	 * Get the object reference.
	 * 
	 * @return the reference, never {@literal null}
	 */
	public S3ObjectReference getObjectReference() {
		return ref;
	}

	@Override
	public String getDescription() {
		return "S3Resource{" + ref.getKey() + "}";
	}

	@Override
	public URL getURL() throws IOException {
		return ref.getURL();
	}

	/**
	 * Compute a hash code.
	 * 
	 * <p>
	 * This implementation returns a hash built from
	 * {@link S3Client#getSettingUID()} and {@link S3ObjectRef}.
	 * </p>
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hash(client.getSettingUID(), ref);
	}

	/**
	 * Compare for equality.
	 * 
	 * <p>
	 * This implementation compares the {@link S3Client#getSettingUID()} and
	 * {@link S3ObjectRef} values.
	 * </p>
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( !super.equals(obj) ) {
			return false;
		}
		if ( !(obj instanceof S3ClientResource) ) {
			return false;
		}
		S3ClientResource other = (S3ClientResource) obj;
		return Objects.equals(client.getSettingUID(), other.client.getSettingUID())
				&& Objects.equals(ref, other.ref);
	}

}

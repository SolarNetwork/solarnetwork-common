/* ==================================================================
 * MultipartFileResource.java - 9/11/2018 10:02:24 AM
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

package net.solarnetwork.web.jakarta.support;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import net.solarnetwork.io.TransferrableResource;

/**
 * {@link Resource} that delegates operations to a {@link MultipartFile}.
 * 
 * <p>
 * This class also implements {@link TransferrableResource} so classes that
 * receive this as a {@link Resource} can test for that API and efficiently move
 * the temporary multipart file to some other location.
 * </p>
 * 
 * @author matt
 * @version 1.0
 * @since 1.15
 */
public class MultipartFileResource extends AbstractResource implements Resource, TransferrableResource {

	private final MultipartFile multipart;

	/**
	 * Constructor.
	 * 
	 * @param multipart
	 *        the multipart file to delegate to
	 * @throws IllegalArgumentException
	 *         if {@code multipart} is {@literal null}
	 */
	public MultipartFileResource(MultipartFile multipart) {
		super();
		if ( multipart == null ) {
			throw new IllegalArgumentException("MultipartFile cannot be null.");
		}
		this.multipart = multipart;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return multipart.getInputStream();
	}

	@Override
	public String getDescription() {
		return "MultipartFile{" + multipart.getOriginalFilename() + "}";
	}

	@Override
	public String getFilename() {
		return multipart.getOriginalFilename();
	}

	@Override
	public void transferTo(File dest) throws IOException, IllegalStateException {
		multipart.transferTo(dest);
	}

}

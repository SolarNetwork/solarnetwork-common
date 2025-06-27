/* ==================================================================
 * TemplateRenderer.java - 25/07/2020 3:44:32 PM
 *
 * Copyright 2020 SolarNetwork.net Dev Team
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

package net.solarnetwork.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.util.MimeType;
import net.solarnetwork.domain.Identity;

/**
 * API for a service that can execute a template against some input data to
 * produce formatted output.
 *
 * @author matt
 * @version 1.0
 * @since 1.64
 */
public interface TemplateRenderer extends Identity<String> {

	/**
	 * Test if this service supports a given MIME type.
	 *
	 * @param mimeType
	 *        the type to check if this service supports
	 * @return {@literal true} if this service can render to the given MIME type
	 */
	boolean supportsMimeType(MimeType mimeType);

	/**
	 * Get the MIME types this service is capable of rendering output as.
	 *
	 * @return the supported MIME type, never {@literal null}
	 */
	List<MimeType> supportedMimeTypes();

	/**
	 * Render the template.
	 *
	 * @param parameters
	 *        the input parameters
	 * @param mimeType
	 *        the desired MIME type
	 * @param locale
	 *        the desired locale
	 * @param out
	 *        the destination
	 * @throws IOException
	 *         if any error occurs
	 */
	void render(Locale locale, MimeType mimeType, Map<String, ?> parameters, OutputStream out)
			throws IOException;

}

/* ==================================================================
 * TestingHttpServletReqeust.java - 18/03/2022 1:27:45 PM
 * 
 * Copyright 2022 SolarNetwork.net Dev Team
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

package net.solarnetwork.web.jakarta.security.test;

import java.io.InputStream;
import org.springframework.mock.web.DelegatingServletInputStream;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletInputStream;

/**
 * Extension of {@link MockHttpServletRequest} with support for a delegate
 * {@link InputStream}.
 * 
 * @author matt
 * @version 2.0
 */
public class TestingHttpServletReqeust extends MockHttpServletRequest {

	private InputStream contentStream;

	/**
	 * Create a new {@code MockHttpServletRequest} with a default
	 * {@link MockServletContext}.
	 */
	public TestingHttpServletReqeust() {
		super();
	}

	/**
	 * Create a new {@code MockHttpServletRequest} with a default
	 * {@link MockServletContext}.
	 * 
	 * @param method
	 *        the request method (may be {@code null})
	 * @param requestURI
	 *        the request URI (may be {@code null})
	 */
	public TestingHttpServletReqeust(String method, String requestURI) {
		super(method, requestURI);
	}

	/**
	 * Create a new {@code MockHttpServletRequest} with the supplied
	 * {@link ServletContext}.
	 * 
	 * @param servletContext
	 *        the ServletContext that the request runs in (may be {@code null}
	 *        to use a default {@link MockServletContext})
	 */
	public TestingHttpServletReqeust(ServletContext servletContext) {
		super(servletContext, "", "");
	}

	/**
	 * Create a new {@code MockHttpServletRequest} with the supplied
	 * {@link ServletContext}, {@code method}, and {@code requestURI}.
	 * 
	 * @param servletContext
	 *        the ServletContext that the request runs in (may be {@code null}
	 *        to use a default {@link MockServletContext})
	 * @param method
	 *        the request method (may be {@code null})
	 * @param requestURI
	 *        the request URI (may be {@code null})
	 */
	public TestingHttpServletReqeust(ServletContext servletContext, String method, String requestURI) {
		super(servletContext, method, requestURI);
	}

	@Override
	public ServletInputStream getInputStream() {
		return (contentStream != null ? new DelegatingServletInputStream(contentStream)
				: super.getInputStream());
	}

	/**
	 * Set an input stream to use for content.
	 * 
	 * @param contentStream
	 *        the stream to set
	 */
	public void setContentStream(InputStream contentStream) {
		this.contentStream = contentStream;
	}

}

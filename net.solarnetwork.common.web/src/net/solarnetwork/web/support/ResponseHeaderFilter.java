/* ===================================================================
 * ResponseHeaderFilter.java
 * 
 * Created Aug 13, 2008 1:56:29 PM
 * 
 * Copyright (c) 2008 Solarnetwork.net Dev Team.
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
 * ===================================================================
 * $Id$
 * ===================================================================
 */

package net.solarnetwork.web.support;

import java.io.IOException;
import java.util.Enumeration;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 * Filter for adding arbitrary HTTP headers to requests, to add cache headers to
 * static files.
 * 
 * <p>
 * Adapted from
 * http://www.jspinsider.com/content/dev/afessh/another-filter-every
 * -site-should-have.html
 * </p>
 * 
 * @author matt
 * @version 1.0
 */
public class ResponseHeaderFilter implements Filter {

	private FilterConfig fc;

	/**
	 * Constructor.
	 */
	public ResponseHeaderFilter() {
		super();
	}

	@Override
	public void destroy() {
		this.fc = null;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		// set the provided HTTP response parameters
		for ( Enumeration<String> e = fc.getInitParameterNames(); e.hasMoreElements(); ) {
			String headerName = e.nextElement();
			httpResponse.setHeader(headerName, fc.getInitParameter(headerName));
		}
		// pass the request/response on
		chain.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		this.fc = filterConfig;
	}

}

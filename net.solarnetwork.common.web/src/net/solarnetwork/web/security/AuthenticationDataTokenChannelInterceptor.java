/* ==================================================================
 * AuthenticationDataTokenChannelInterceptor.java - 18/11/2017 7:21:21 AM
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

package net.solarnetwork.web.security;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.ReadListener;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import net.solarnetwork.util.IteratorEnumeration;
import net.solarnetwork.web.support.RequestInfoHandshakeInterceptor;

/**
 * Authentication filter for {@link AuthenticationData} style token
 * authentication on a STOMP message channel.
 * 
 * <p>
 * This class depends on the {@link RequestInfoHandshakeInterceptor} to provide
 * access to some data required for authentication.
 * </p>
 * 
 * @author matt
 * @version 2.0
 * @since 1.14
 */
public class AuthenticationDataTokenChannelInterceptor implements ChannelInterceptor {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final UserDetailsService userDetailsService;
	private final AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource;
	private long maxDateSkew = TimeUnit.MINUTES.toMillis(15);

	/**
	 * Constructor.
	 * 
	 * @param userDetailsService
	 *        the user details service to use
	 */
	public AuthenticationDataTokenChannelInterceptor(UserDetailsService userDetailsService) {
		super();
		this.userDetailsService = userDetailsService;
		this.authenticationDetailsSource = new WebAuthenticationDetailsSource();
	}

	/**
	 * Configure the maximum allowable date skew.
	 * 
	 * @param maxDateSkew
	 *        the maximum date skew, in milliseconds; defaults to 15 minutes
	 */
	public void setMaxDateSkew(long maxDateSkew) {
		this.maxDateSkew = maxDateSkew;
	}

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message,
				StompHeaderAccessor.class);

		if ( StompCommand.CONNECT.equals(accessor.getCommand()) ) {
			Authentication authenticatedUser = authenticateUser(accessor);
			if ( authenticatedUser != null ) {
				accessor.setUser(authenticatedUser);
			}
		}

		return message;
	}

	private Authentication authenticateUser(SimpMessageHeaderAccessor accessor) {
		HttpServletRequest request = new MessageHttpServletRequestAdapter(accessor);
		Authentication authenticatedUser = null;
		SecurityHttpServletRequestWrapper secRequest = new SecurityHttpServletRequestWrapper(request,
				65536);
		AuthenticationData data;
		try {
			data = AuthenticationDataFactory.authenticationDataForAuthorizationHeader(secRequest);
		} catch ( IOException e ) {
			throw new RuntimeException(e);
		}
		if ( data != null ) {
			UserDetails user = userDetailsService.loadUserByUsername(data.getAuthTokenId());
			final String computedDigest = data.computeSignatureDigest(user.getPassword());
			if ( computedDigest.equals(data.getSignatureDigest()) ) {
				if ( data.isDateValid(maxDateSkew) ) {
					authenticatedUser = createSuccessfulAuthentication(request, user);
					log.debug("Authentication success for user: '{}'", user.getUsername());
				} else {
					log.debug("Request date '{}' diff too large: {}", data.getDate(),
							data.getDateSkew());
					throw new BadCredentialsException("Request date skew too large");
				}
			} else {
				log.debug("Expected digest: '{}' but received: '{}'", computedDigest,
						data.getSignatureDigest());
				throw new BadCredentialsException("Bad signature digest");
			}
		} else {
			log.trace("Missing Authorization header or unsupported scheme");
		}
		return authenticatedUser;
	}

	private Authentication createSuccessfulAuthentication(HttpServletRequest request, UserDetails user) {
		PreAuthenticatedAuthenticationToken authRequest = new PreAuthenticatedAuthenticationToken(user,
				null, user.getAuthorities());
		authRequest.eraseCredentials();
		authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
		return authRequest;
	}

	private static final class MessageHttpServletRequestAdapter implements HttpServletRequest {

		private final HttpMethod requestMethod;
		private final URI requestUri;
		private final HttpHeaders requestHeaders;
		private final HttpHeaders nativeHeaders;

		public MessageHttpServletRequestAdapter(SimpMessageHeaderAccessor accessor) {
			super();
			this.requestMethod = getSessionAttribute(accessor,
					RequestInfoHandshakeInterceptor.REQUEST_URI_ATTR, HttpMethod.class);
			this.requestUri = getSessionAttribute(accessor,
					RequestInfoHandshakeInterceptor.REQUEST_URI_ATTR, URI.class);
			this.requestHeaders = getSessionAttribute(accessor,
					RequestInfoHandshakeInterceptor.REQUEST_HEADERS, HttpHeaders.class);
			this.nativeHeaders = new HttpHeaders();
			this.nativeHeaders.putAll(accessor.toNativeHeaderMap());
		}

		@SuppressWarnings("unchecked")
		private <T> T getSessionAttribute(SimpMessageHeaderAccessor accessor, String name,
				Class<T> requiredType) {
			Map<String, Object> sessionAttr = accessor.getSessionAttributes();
			if ( sessionAttr != null ) {
				Object val = sessionAttr.get(name);
				if ( requiredType == null || requiredType.isInstance(val) ) {
					return (T) val;
				}
			}
			return null;
		}

		@Override
		public Object getAttribute(String name) {
			return null;
		}

		@Override
		public Enumeration<String> getAttributeNames() {
			return null;
		}

		@Override
		public String getCharacterEncoding() {
			return null;
		}

		@Override
		public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
		}

		@Override
		public int getContentLength() {
			return 0;
		}

		@Override
		public long getContentLengthLong() {
			return 0;
		}

		@Override
		public String getContentType() {
			return null;
		}

		@Override
		public ServletInputStream getInputStream() throws IOException {
			return new ServletInputStream() {

				@Override
				public int read() throws IOException {
					return -1;
				}

				@Override
				public void setReadListener(ReadListener readListener) {
				}

				@Override
				public boolean isReady() {
					return true;
				}

				@Override
				public boolean isFinished() {
					return true;
				}
			};
		}

		@Override
		public String getParameter(String name) {
			return null;
		}

		@Override
		public Enumeration<String> getParameterNames() {
			return new IteratorEnumeration<String>(Collections.<String> emptyIterator());
		}

		@Override
		public String[] getParameterValues(String name) {
			return null;
		}

		@Override
		public Map<String, String[]> getParameterMap() {
			return Collections.emptyMap();
		}

		@Override
		public String getProtocol() {
			return null;
		}

		@Override
		public String getScheme() {
			return (requestUri != null ? requestUri.getScheme() : "http");
		}

		@Override
		public String getServerName() {
			return (requestUri != null ? requestUri.getHost() : null);
		}

		@Override
		public int getServerPort() {
			int port = (requestUri != null ? requestUri.getPort() : -1);
			if ( port < 1 ) {
				String scheme = requestUri.getScheme();
				port = ("https".equals(scheme) ? 443 : 80);
			}
			return port;
		}

		@Override
		public BufferedReader getReader() throws IOException {
			return new BufferedReader(new StringReader(""));
		}

		@Override
		public String getRemoteAddr() {
			return null;
		}

		@Override
		public String getRemoteHost() {
			return null;
		}

		@Override
		public void setAttribute(String name, Object o) {
		}

		@Override
		public void removeAttribute(String name) {
		}

		@Override
		public Locale getLocale() {
			return null;
		}

		@Override
		public Enumeration<Locale> getLocales() {
			return null;
		}

		@Override
		public boolean isSecure() {
			return false;
		}

		@Override
		public RequestDispatcher getRequestDispatcher(String path) {
			return null;
		}

		@Override
		public String getRealPath(String path) {
			return null;
		}

		@Override
		public int getRemotePort() {
			return 0;
		}

		@Override
		public String getLocalName() {
			return null;
		}

		@Override
		public String getLocalAddr() {
			return null;
		}

		@Override
		public int getLocalPort() {
			return 0;
		}

		@Override
		public ServletContext getServletContext() {
			return null;
		}

		@Override
		public AsyncContext startAsync() throws IllegalStateException {
			return null;
		}

		@Override
		public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse)
				throws IllegalStateException {
			return null;
		}

		@Override
		public boolean isAsyncStarted() {
			return false;
		}

		@Override
		public boolean isAsyncSupported() {
			return false;
		}

		@Override
		public AsyncContext getAsyncContext() {
			return null;
		}

		@Override
		public DispatcherType getDispatcherType() {
			return null;
		}

		@Override
		public String getAuthType() {
			return null;
		}

		@Override
		public Cookie[] getCookies() {
			return null;
		}

		@Override
		public long getDateHeader(String name) {
			long date = nativeHeaders.getFirstDate(name);
			if ( date < 0 && requestHeaders != null ) {
				date = requestHeaders.getFirstDate(name);
			}
			return date;
		}

		@Override
		public String getHeader(String name) {
			String value = nativeHeaders.getFirst(name);
			if ( value == null && requestHeaders != null ) {
				value = requestHeaders.getFirst(name);
			}
			return value;
		}

		@Override
		public Enumeration<String> getHeaders(String name) {
			List<String> str = nativeHeaders.get(name);
			if ( requestHeaders != null ) {
				if ( str == null ) {
					str = requestHeaders.get(name);
				} else {
					List<String> reqStr = requestHeaders.get(name);
					if ( reqStr != null ) {
						str = new ArrayList<String>(str);
						str.addAll(reqStr);
					}
				}
			}
			return new IteratorEnumeration<String>(
					str != null ? str.iterator() : Collections.<String> emptyIterator());
		}

		@Override
		public Enumeration<String> getHeaderNames() {
			Set<String> keys = nativeHeaders.keySet();
			if ( requestHeaders != null ) {
				keys = new LinkedHashSet<String>(keys);
				keys.addAll(requestHeaders.keySet());
			}
			return new IteratorEnumeration<String>(
					keys != null ? keys.iterator() : Collections.<String> emptyIterator());
		}

		@Override
		public int getIntHeader(String name) {
			String str = getHeader(name);
			return (str != null ? Integer.parseInt(str) : -1);
		}

		@Override
		public String getMethod() {
			return (requestMethod == null ? HttpMethod.GET.toString() : requestMethod.toString());
		}

		@Override
		public String getPathInfo() {
			return null;
		}

		@Override
		public String getPathTranslated() {
			return null;
		}

		@Override
		public String getContextPath() {
			return null;
		}

		@Override
		public String getQueryString() {
			return null;
		}

		@Override
		public String getRemoteUser() {
			return null;
		}

		@Override
		public boolean isUserInRole(String role) {
			return false;
		}

		@Override
		public Principal getUserPrincipal() {
			return null;
		}

		@Override
		public String getRequestedSessionId() {
			return null;
		}

		@Override
		public String getRequestURI() {
			return (requestUri != null ? requestUri.getRawPath() : "/");
		}

		@Override
		public StringBuffer getRequestURL() {
			return null;
		}

		@Override
		public String getServletPath() {
			return "";
		}

		@Override
		public HttpSession getSession(boolean create) {
			return null;
		}

		@Override
		public HttpSession getSession() {
			return null;
		}

		@Override
		public String changeSessionId() {
			return null;
		}

		@Override
		public boolean isRequestedSessionIdValid() {
			return false;
		}

		@Override
		public boolean isRequestedSessionIdFromCookie() {
			return false;
		}

		@Override
		public boolean isRequestedSessionIdFromURL() {
			return false;
		}

		@Override
		public boolean isRequestedSessionIdFromUrl() {
			return false;
		}

		@Override
		public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
			return false;
		}

		@Override
		public void login(String username, String password) throws ServletException {
		}

		@Override
		public void logout() throws ServletException {
		}

		@Override
		public Collection<Part> getParts() throws IOException, ServletException {
			return null;
		}

		@Override
		public Part getPart(String name) throws IOException, ServletException {
			return null;
		}

		@Override
		public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass)
				throws IOException, ServletException {
			return null;
		}

	}

}

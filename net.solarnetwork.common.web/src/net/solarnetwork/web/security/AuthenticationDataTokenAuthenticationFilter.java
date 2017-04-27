/* ==================================================================
 * AuthenticationDataTokenAuthenticationFilter.java - 27/04/2017 7:37:01 AM
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

import static net.solarnetwork.web.security.AuthorizationV2Builder.computeMacDigest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Authentication filter for {@link AuthenticationData} style token
 * authentication.
 * 
 * This filter supports the {@literal SolarNetworkWS} and {@literal SNWS2} HTTP
 * authorization schemes. In addition, a JWT encoded cookie named
 * {@literal sntoken} can be generated if a request parameter
 * {@literal sntoken-cookie=true} is passed with the request. That cookie can
 * then be presented on subsequent requests instead of the HTTP authorization.
 * 
 * @author matt
 * @version 1.0
 */
public class AuthenticationDataTokenAuthenticationFilter extends OncePerRequestFilter {

	/**
	 * A request parameter to signal that an authentication cookie should be set
	 * on the response.
	 */
	public static final String REQUEST_PARAM_SET_COOKIE = "sntoken-cookie";

	/** The name of the cookie used for cookie based tokens. */
	public static final String COOKIE_NAME_AUTH_TOKEN = "sntoken";

	private UserDetailsService userDetailsService;

	private AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource = new WebAuthenticationDetailsSource();
	private AuthenticationEntryPoint authenticationEntryPoint;
	private long maxDateSkew = 15 * 60 * 1000; // 15 minutes default

	private final Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * Default constructor.
	 */
	public AuthenticationDataTokenAuthenticationFilter() {
		super();
	}

	/**
	 * Construct with a {@link UserDetailsService}.
	 * 
	 * @param userDetailsService
	 *        The service to use.
	 */
	public AuthenticationDataTokenAuthenticationFilter(UserDetailsService userDetailsService) {
		super();
		this.userDetailsService = userDetailsService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		try {
			doAuthentication(request, response);
			filterChain.doFilter(request, response);
		} catch ( AuthenticationException e ) {
			if ( authenticationEntryPoint != null ) {
				authenticationEntryPoint.commence(request, response, e);
			} else {
				throw e;
			}
		}
	}

	private void doAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		Authentication authenticatedUser = null;

		// first look for Authorization HTTP header
		SecurityHttpServletRequestWrapper secRequest = new SecurityHttpServletRequestWrapper(request,
				65536);
		AuthenticationData data = AuthenticationDataFactory
				.authenticationDataForAuthorizationHeader(secRequest);
		if ( data != null ) {
			UserDetails user = userDetailsService.loadUserByUsername(data.getAuthTokenId());
			final String computedDigest = data.computeSignatureDigest(user.getPassword());
			if ( computedDigest.equals(data.getSignatureDigest()) ) {
				if ( data.isDateValid(maxDateSkew) ) {
					// check if cookie should be set
					if ( "true".equalsIgnoreCase(request.getParameter(REQUEST_PARAM_SET_COOKIE)) ) {
						byte[] secret = computeJWTSigningKey(user.getPassword(),
								data.getDate().getTime());
						AuthenticationDataToken tokenCookie = new AuthenticationDataToken(data, secret);
						Cookie cookie = new Cookie(COOKIE_NAME_AUTH_TOKEN, tokenCookie.cookieValue());
						cookie.setHttpOnly(true);
						cookie.setMaxAge(-1); // expire when browser session ends
						response.addCookie(cookie);
					}

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

		if ( authenticatedUser == null ) {
			// look for a token provided via cookies
			Cookie[] cookies = request.getCookies();
			if ( cookies != null ) {
				for ( Cookie cookie : cookies ) {
					if ( COOKIE_NAME_AUTH_TOKEN.equals(cookie.getName()) ) {
						try {
							AuthenticationDataToken tokenCookie = new AuthenticationDataToken(cookie);
							UserDetails user = userDetailsService
									.loadUserByUsername(tokenCookie.getIdentity());
							byte[] secret = computeJWTSigningKey(user.getPassword(),
									tokenCookie.getIssued() * 1000);
							tokenCookie.verify(secret);
							authenticatedUser = createSuccessfulAuthentication(request, user);
							break;
						} catch ( SecurityException e ) {
							throw new BadCredentialsException(e.getMessage(), e);
						}
					}
				}
			}
		}

		if ( authenticatedUser != null ) {
			SecurityContextHolder.getContext().setAuthentication(authenticatedUser);
		}
	}

	private String formatJWTSigningDate(Calendar cal) {
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DATE);
		StringBuilder buf = new StringBuilder();
		buf.append(year);
		if ( month < 10 ) {
			buf.append('0');
		}
		buf.append(month);
		if ( day < 10 ) {
			buf.append('0');
		}
		buf.append(day);
		return buf.toString();
	}

	private byte[] computeJWTSigningKey(String secret, long date) {
		/*- signing key is like:
		 
		HMACSHA256(HMACSHA256("SNWS"+secretKey, "20160301"), "sntoken")
		*/
		GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		cal.setTimeInMillis(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		String dateStr = formatJWTSigningDate(cal);
		try {
			return computeMacDigest(computeMacDigest("SNWS" + secret, dateStr, "HmacSHA256"),
					COOKIE_NAME_AUTH_TOKEN.getBytes("UTF-8"), "HmacSHA256");
		} catch ( UnsupportedEncodingException e ) {
			// should not get here
			throw new RuntimeException(e);
		}
	}

	private Authentication createSuccessfulAuthentication(HttpServletRequest request, UserDetails user) {
		PreAuthenticatedAuthenticationToken authRequest = new PreAuthenticatedAuthenticationToken(user,
				null, user.getAuthorities());
		authRequest.eraseCredentials();
		authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
		return authRequest;
	}

	/**
	 * Set the details service.
	 * 
	 * The service must return users with valid token identifiers and plain-text
	 * token secret passwords via {@link UserDetails#getUsername()} and
	 * {@link UserDetails#getPassword()}, respectfully.
	 * 
	 * After validating the request authentication, this filter will authorize
	 * the user with Spring Security by calling
	 * {@code SecurityContextHolder.getContext().setAuthentication()}.
	 * 
	 * @param userDetailsService
	 *        the user details service to use
	 */
	public void setUserDetailsService(UserDetailsService userDetailsService) {
		this.userDetailsService = userDetailsService;
	}

	/**
	 * Set the details source to use.
	 * 
	 * This defaults to a {@link WebAuthenticationDetailsSource}.
	 * 
	 * @param authenticationDetailsSource
	 *        the details source to use
	 */
	public void setAuthenticationDetailsSource(
			AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource) {
		this.authenticationDetailsSource = authenticationDetailsSource;
	}

	/**
	 * Set the maximum amount of difference in the supplied HTTP {@code Date}
	 * (or {@literal X-SN-Date}) header value with the current time as reported
	 * by the system. If this difference is exceeded, authorization fails.
	 * 
	 * @param maxDateSkew
	 *        the maximum allowable skew, in milliseconds
	 */
	public void setMaxDateSkew(long maxDateSkew) {
		this.maxDateSkew = maxDateSkew;
	}

	/**
	 * Set an {@link AuthenticationEntryPoint} to handle authentication errors.
	 * 
	 * If this is configured, any {@link AuthenticationException} thrown during
	 * processing will be directed to the configured instance. Otherwise those
	 * exceptions will be re-thrown.
	 * 
	 * @param authenticationEntryPoint
	 *        the authenticationEntryPoint to set
	 */
	public void setAuthenticationEntryPoint(AuthenticationEntryPoint authenticationEntryPoint) {
		this.authenticationEntryPoint = authenticationEntryPoint;
	}

}

/* ==================================================================
 * OcppWebSocketHandshakeInterceptor.java - 31/01/2020 4:19:08 pm
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

package net.solarnetwork.ocpp.web.json;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.SubProtocolCapable;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.server.HandshakeInterceptor;
import net.solarnetwork.ocpp.dao.SystemUserDao;
import net.solarnetwork.ocpp.domain.ChargePointIdentity;
import net.solarnetwork.ocpp.domain.SystemUser;
import net.solarnetwork.support.PasswordEncoder;

/**
 * Intercept the OCPP Charge Point web socket handshake.
 * 
 * <p>
 * This interceptor will extract the Charge Point client ID from the request and
 * save that to the session attribute {@link #CLIENT_ID_ATTR}. If the client ID
 * is not available then a {@link HttpStatus#NOT_FOUND} error will be sent.
 * </p>
 * 
 * @author matt
 * @version 1.1
 */
public class OcppWebSocketHandshakeInterceptor implements HandshakeInterceptor {

	/** The attribute name for the {@link URI} of the HTTP request. */
	public static final String REQUEST_URI_ATTR = "requestUri";

	/** The default {@code clientIdUriPattern} property value. */
	public static final String DEFAULT_CLIENT_ID_URI_PATTERN = "/ocpp/v16/cs/json/(.*)";

	/**
	 * The attribute key for the client ID, as a {@link ChargePointIdentity}.
	 */
	public static final String CLIENT_ID_ATTR = "clientId";

	private static final Logger log = LoggerFactory.getLogger(OcppWebSocketHandshakeInterceptor.class);

	private final SystemUserDao systemUserDao;
	private final PasswordEncoder passwordEncoder;
	private Pattern clientIdUriPattern;
	private String fixedIdentityUsername;

	/**
	 * Constructor.
	 * 
	 * @param systemUserDao
	 *        the DAO to authenticate clients with
	 * @param passwordEncoder
	 *        the password encoder to use
	 */
	public OcppWebSocketHandshakeInterceptor(SystemUserDao systemUserDao,
			PasswordEncoder passwordEncoder) {
		super();
		this.systemUserDao = systemUserDao;
		this.passwordEncoder = passwordEncoder;
		setClientIdUriPattern(Pattern.compile(DEFAULT_CLIENT_ID_URI_PATTERN));
	}

	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
			WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
		URI uri = request.getURI();
		Matcher m = clientIdUriPattern.matcher(uri.getPath());
		if ( !m.find() ) {
			log.debug("OCPP handshake request rejected, client ID not found in URI path: {}",
					uri.getPath());
			response.setStatusCode(HttpStatus.NOT_FOUND);
			return false;
		}

		final String identifier = m.group(1);

		// enforce sub-protocol, as required by OCPP spec
		WebSocketHandler handler = WebSocketHandlerDecorator.unwrap(wsHandler);
		if ( handler instanceof SubProtocolCapable ) {
			List<String> subProtocols = ((SubProtocolCapable) handler).getSubProtocols();
			if ( subProtocols != null && !subProtocols.isEmpty() ) {
				WebSocketHttpHeaders headers = new WebSocketHttpHeaders(request.getHeaders());
				List<String> clientSubProtocols = headers.getSecWebSocketProtocol();
				boolean match = false;
				if ( clientSubProtocols != null ) {
					for ( String clientProtocol : clientSubProtocols ) {
						if ( subProtocols.contains(clientProtocol) ) {
							match = true;
							break;
						}
					}
				}
				if ( !match ) {
					log.debug(
							"OCPP handshake request rejected, supported sub-protocol(s) {}, requested: {}",
							subProtocols, clientSubProtocols);
					response.setStatusCode(HttpStatus.BAD_REQUEST);
					return false;
				}
			}
		}

		// enforce system user authentication
		if ( systemUserDao != null ) {
			String httpAuth = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
			if ( httpAuth == null ) {
				log.warn("OCPP handshake request rejected for {}, Authorization header not provided.",
						identifier);
				response.setStatusCode(HttpStatus.FORBIDDEN);
				return false;
			}
			String[] httpAuthComponents = decodeBasicAuthorizationHeader(httpAuth);
			if ( httpAuthComponents == null ) {
				log.warn(
						"OCPP handshake request rejected for {}, invalid Basic Authorization header provided: [{}]",
						identifier, httpAuth);
				response.setStatusCode(HttpStatus.FORBIDDEN);
				return false;
			}

			final String username = httpAuthComponents[0];
			final String password = httpAuthComponents[1];

			SystemUser user = systemUserDao.getForUsername(username);
			if ( user == null ) {
				log.warn("OCPP handshake request rejected for {}, system user {} not found.", identifier,
						username);
				response.setStatusCode(HttpStatus.FORBIDDEN);
				return false;
			}
			if ( user.getAllowedChargePoints() != null && !user.getAllowedChargePoints().isEmpty()
					&& !user.getAllowedChargePoints().contains(identifier) ) {
				log.warn(
						"OCPP handshake request rejected for {}, system user {} does not allow this charge point.",
						identifier, username);
				response.setStatusCode(HttpStatus.FORBIDDEN);
				return false;
			}
			if ( user.getPassword() != null ) {
				if ( !((passwordEncoder != null && passwordEncoder.matches(password, user.getPassword()))
						|| user.getPassword().equals(password)) ) {
					log.warn(
							"OCPP handshake request rejected for {}, system user {} password does not match.",
							identifier, username);
					response.setStatusCode(HttpStatus.FORBIDDEN);
					return false;
				}
			}
			attributes.putIfAbsent(CLIENT_ID_ATTR, user.chargePointIdentity(identifier));
		}

		return true;
	}

	private String[] decodeBasicAuthorizationHeader(String header) {
		Charset utf8 = Charset.forName("UTF-8");
		// help to work with buggy clients that present scheme as "Basic:"
		int space = header.indexOf(' ');
		if ( space < 0 || space + 1 >= header.length() ) {
			return null;
		}
		byte[] base64Token = header.substring(space + 1).getBytes(utf8);
		byte[] decoded;
		try {
			decoded = java.util.Base64.getDecoder().decode(base64Token);
		} catch ( IllegalArgumentException e ) {
			return null;
		}
		String token = new String(decoded, utf8);
		int delim = token.indexOf(":");
		if ( delim == -1 ) {
			return null;
		}
		return new String[] { token.substring(0, delim), token.substring(delim + 1) };
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
			WebSocketHandler wsHandler, Exception exception) {
		// nothing to do
	}

	/**
	 * Get the Charge Point client ID URI pattern.
	 * 
	 * @return the pattern, never {@literal null}; defaults to
	 *         {@link #DEFAULT_CLIENT_ID_URI_PATTERN}
	 */
	public Pattern getClientIdUriPattern() {
		return clientIdUriPattern;
	}

	/**
	 * Set the Charge Point client ID URI pattern.
	 * 
	 * <p>
	 * This pattern is applied to the handshake request URI path, and should
	 * have a capturing group that returns the Charge Point client ID value.
	 * </p>
	 * 
	 * @param clientIdUriPattern
	 *        the URI pattern for extracting charge point IDs from URIs
	 */
	public void setClientIdUriPattern(Pattern clientIdUriPattern) {
		if ( clientIdUriPattern == null ) {
			throw new IllegalArgumentException("The client ID URI pattern must be provided.");
		}
		this.clientIdUriPattern = clientIdUriPattern;
	}

	/**
	 * Get the fixed {@link ChargePointIdentity} username to use.
	 * 
	 * @return the username to use; defaults to {@code null}
	 */
	public String getFixedIdentityUsername() {
		return fixedIdentityUsername;
	}

	/**
	 * Set the fixed {@link ChargePointIdentity} username to use.
	 * 
	 * <p>
	 * When this property is configured, then the
	 * {@link ChargePointIdentity#getUserIdentifier()} value will always be
	 * saved as this value when populating the {@link #CLIENT_ID_ATTR} session
	 * identity. If <b>not</b> configured then the HTTP BASIC authorization
	 * username will be used. This can be useful in contexts where the
	 * {@link ChargePointIdentity#getIdentifier()} is sufficient to uniquely
	 * identify a charge point, such as in SolarNode; the
	 * {@link ChargePointIdentity#ANY_USER} can be used for that scenario.
	 * </p>
	 * 
	 * @param fixedIdentityUsername
	 *        the fixed identity username to set
	 */
	public void setFixedIdentityUsername(String fixedIdentityUsername) {
		this.fixedIdentityUsername = fixedIdentityUsername;
	}

}

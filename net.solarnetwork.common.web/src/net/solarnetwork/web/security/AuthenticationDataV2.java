/* ==================================================================
 * AuthenticationDataV2.java - 1/03/2017 8:41:00 PM
 * 
 * Copyright 2007-2017 SolarNetwork.net Dev Team
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

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import net.solarnetwork.security.Snws2AuthorizationBuilder;
import net.solarnetwork.util.StringUtils;

/**
 * Version 2 authentication token scheme based on HMAC-SHA256.
 * 
 * Signing keys are treated valid for up to 7 days in the past from the time of
 * the signature calculation in {@link #computeSignatureDigest(String)}.
 * 
 * @author matt
 * @version 1.3
 * @since 1.11
 */
public class AuthenticationDataV2 extends AuthenticationData {

	private static final String HOST_HEADER = "host";

	private static final Logger log = LoggerFactory.getLogger(AuthenticationDataV2.class);

	private static final int SIGNATURE_HEX_LENGTH = 64;

	public static final String TOKEN_COMPONENT_KEY_CREDENTIAL = "Credential";
	public static final String TOKEN_COMPONENT_KEY_SIGNED_HEADERS = "SignedHeaders";
	public static final String TOKEN_COMPONENT_KEY_SIGNATURE = "Signature";

	private final String explicitHost;
	private final String authTokenId;
	private final String signatureDigest;
	private final Set<String> signedHeaderNames;
	private final String[] sortedSignedHeaderNames;
	private final Snws2AuthorizationBuilder builder;

	/**
	 * Constructor.
	 * 
	 * @param request
	 *        the HTTP request
	 * @param headerValue
	 *        the {@literal Authorization} HTTP header value
	 * @throws IOException
	 *         if any IO error occurs
	 */
	public AuthenticationDataV2(SecurityHttpServletRequestWrapper request, String headerValue)
			throws IOException {
		this(request, headerValue, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param request
	 *        the HTTP request
	 * @param headerValue
	 *        the {@literal Authorization} HTTP header value
	 * @param explicitHost
	 *        a fixed value to use instead of the {@literal Host} HTTP header
	 *        value, or {@literal null} to use the header value; this can be
	 *        useful when sitting behind a proxy
	 * @throws IOException
	 *         if any IO error occurs
	 * @since 1.3
	 */
	public AuthenticationDataV2(SecurityHttpServletRequestWrapper request, String headerValue,
			String explicitHost) throws IOException {
		super(AuthenticationScheme.V2, request, headerValue);
		this.explicitHost = explicitHost;

		// the header must be in the form Credential=TOKEN-ID,SignedHeaders=x;y;z,Signature=HMAC-SHA1-SIGNATURE

		Map<String, String> tokenData = tokenStringToMap(headerValue);
		authTokenId = tokenData.get(TOKEN_COMPONENT_KEY_CREDENTIAL);
		if ( authTokenId == null ) {
			throw new BadCredentialsException("Invalid " + TOKEN_COMPONENT_KEY_CREDENTIAL + " value");
		}
		signatureDigest = tokenData.get(TOKEN_COMPONENT_KEY_SIGNATURE);
		if ( signatureDigest == null || signatureDigest.length() != SIGNATURE_HEX_LENGTH ) {
			throw new BadCredentialsException("Invalid " + TOKEN_COMPONENT_KEY_SIGNATURE + " value");
		}

		String signedHeaders = tokenData.get(TOKEN_COMPONENT_KEY_SIGNED_HEADERS);
		signedHeaderNames = StringUtils.delimitedStringToSet(signedHeaders, ";");
		if ( signedHeaderNames == null || signedHeaderNames.size() < 2 ) {
			// a minimum of Host + (Date | X-SN-Date) must be provided
			throw new BadCredentialsException(
					"Invalid " + TOKEN_COMPONENT_KEY_SIGNED_HEADERS + " value");
		}

		sortedSignedHeaderNames = signedHeaderNames.toArray(new String[signedHeaderNames.size()]);
		for ( int i = 0; i < sortedSignedHeaderNames.length; i++ ) {
			sortedSignedHeaderNames[i] = sortedSignedHeaderNames[i].toLowerCase();
		}
		Arrays.sort(sortedSignedHeaderNames);

		validateSignedHeaderNames(request);

		validateContentDigest(request);

		builder = new Snws2AuthorizationBuilder(authTokenId).date(getDate());
		setupBuilder(request);
	}

	private static Map<String, String> tokenStringToMap(final String headerValue) {
		if ( headerValue == null || headerValue.length() < 1 ) {
			return null;
		}
		final Map<String, String> map = new LinkedHashMap<String, String>();
		final String delimitedString = headerValue + ',';
		int prevDelimIdx = 0;
		int delimIdx;
		int splitIdx;
		for ( delimIdx = delimitedString.indexOf(','); delimIdx >= 0; prevDelimIdx = delimIdx
				+ 1, delimIdx = delimitedString.indexOf(',', prevDelimIdx) ) {
			String component = delimitedString.substring(prevDelimIdx, delimIdx);
			splitIdx = component.indexOf('=');
			if ( splitIdx > 0 ) {
				String componentKey = component.substring(0, splitIdx);
				String componentValue = component.substring(splitIdx + 1);
				map.put(componentKey, componentValue);
			}
		}
		return map;
	}

	@Override
	public String computeSignatureDigest(String secretKey) {
		return computeSignatureDigest(secretKey, getDate());
	}

	/**
	 * Compute the signature digest, using a specific signing date.
	 * 
	 * <p>
	 * Generally the current date/time is used to sign the request, which is
	 * what the {@link #computeSignatureDigest(String)} method uses. This method
	 * can be useful for testing purposes.
	 * </p>
	 * 
	 * @param secretKey
	 *        the secret key
	 * @param signDate
	 *        the signature date
	 * @return the computed digest
	 * @see #computeSignatureDigest(String)
	 */
	public String computeSignatureDigest(String secretKey, Instant signDate) {
		// signing keys are valid for 7 days, so starting with today work backwards at most
		// 7 days to see if we get a match
		String result = null;
		for ( int i = 0; i < 7; i++ ) {
			String computed = builder.date(signDate).saveSigningKey(secretKey).date(getDate())
					.buildSignature();
			if ( computed.equals(signatureDigest) ) {
				return computed;
			} else if ( result == null ) {
				// save 1st result as one we return if nothing matches
				result = computed;
			}
			signDate = signDate.minus(1, ChronoUnit.DAYS);
		}
		return result;
	}

	private void setupBuilder(SecurityHttpServletRequestWrapper request) throws IOException {
		// 1: HTTP verb
		builder.method(request.getMethod());

		// 2: URI
		builder.path(request.getRequestURI());

		// 3: Query parameters
		builder.parameterMap(request.getParameterMap());

		// 4: Headers
		setupBuilderHeaders(request);

		// 5: Signed headers
		builder.signedHttpHeaders(signedHeaderNames);

		// 6: Content SHA256
		builder.contentSha256(request.getContentSHA256());

		if ( log.isDebugEnabled() ) {
			log.debug("Canonical req data:\n{}", builder.computeCanonicalRequestMessage());
			log.debug("Signature data:\n{}", getSignatureData());
		}
	}

	private void setupBuilderHeaders(HttpServletRequest request) {
		for ( String headerName : sortedSignedHeaderNames ) {
			String value = nullSafeHeaderValue(request, headerName).trim();
			boolean isHost = HOST_HEADER.equals(headerName);
			if ( isHost && explicitHost != null ) {
				log.trace("Replacing host header [{}] with explicit value {}", value, explicitHost);
				value = explicitHost;
			}
			log.trace("Signed req header: {}: {}", headerName, value);
			if ( isHost && explicitHost == null ) {
				if ( value.length() < 1 ) {
					// no Host value provided
					value = request.getServerName();
					if ( value != null ) {
						int port = request.getServerPort();
						if ( port != 80 ) {
							value += ":" + port;
						}
					}
				} else if ( value.indexOf(":") < 0 ) {
					// look for proxy port
					String port = nullSafeHeaderValue(request, "X-Forwarded-Port").trim();
					log.trace("X-Forwarded-Port header: {}", port);
					if ( port.length() < 1 ) {
						String proto = nullSafeHeaderValue(request, "X-Forwarded-Proto").trim()
								.toLowerCase();
						log.trace("X-Forwarded-Proto header: {}", proto);
						if ( "https".equals(proto) ) {
							port = "443";
						}
					}
					if ( port.length() > 0 && !"80".equals(port) ) {
						value += ":" + port;
					}
				}
			}
			builder.header(headerName, value);
		}
	}

	private void validateSignedHeaderNames(SecurityHttpServletRequestWrapper request) {
		// MUST include host
		if ( !signedHeaderNames.contains(HOST_HEADER) ) {
			throw new BadCredentialsException(
					"The 'Host' HTTP header must be included in SignedHeaders");
		}
		// MUST include one of Date or X-SN-Date
		if ( !(signedHeaderNames.contains(WebConstants.HEADER_DATE.toLowerCase())
				|| signedHeaderNames.contains("date")) ) {
			throw new BadCredentialsException(
					"One of the 'Date' or 'X-SN-Date' HTTP headers must be included in SignedHeaders");
		}
		Enumeration<String> headerNames = request.getHeaderNames();
		final String snHeaderPrefix = WebConstants.HEADER_PREFIX.toLowerCase();
		while ( headerNames.hasMoreElements() ) {
			String headerName = headerNames.nextElement().toLowerCase();
			// ALL X-SN-* headers must be included; also Content-Type, Content-MD5, Digest
			boolean mustInclude = (headerName.startsWith(snHeaderPrefix)
					|| headerName.equals("content-type") || headerName.equals("content-md5")
					|| headerName.equals("digest"));
			if ( mustInclude && !signedHeaderNames.contains(headerName) ) {
				throw new BadCredentialsException(
						"The '" + headerName + "' HTTP header must be included in SignedHeaders");
			}
		}
	}

	@Override
	public String getAuthTokenId() {
		return authTokenId;
	}

	@Override
	public String getSignatureDigest() {
		return signatureDigest;
	}

	@Override
	public String getSignatureData() {
		return builder.computeSignatureData(getDate(), builder.computeCanonicalRequestMessage());
	}

	/**
	 * Get the set of signed header names.
	 * 
	 * @return The signed header names, or {@code null}.
	 */
	public Set<String> getSignedHeaderNames() {
		return signedHeaderNames;
	}

}

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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.authentication.BadCredentialsException;
import net.solarnetwork.util.StringUtils;

/**
 * Version 2 authentication token scheme based on HMAC-SHA256.
 * 
 * Signing keys are treated valid for up to 7 days in the past from the time of
 * the signature calculation in {@link #computeSignatureDigest(String)}.
 * 
 * @author matt
 * @version 1.1
 * @since 1.11
 */
public class AuthenticationDataV2 extends AuthenticationData {

	private static final int SIGNATURE_HEX_LENGTH = 64;

	public static final String TOKEN_COMPONENT_KEY_CREDENTIAL = "Credential";
	public static final String TOKEN_COMPONENT_KEY_SIGNED_HEADERS = "SignedHeaders";
	public static final String TOKEN_COMPONENT_KEY_SIGNATURE = "Signature";

	private final String authTokenId;
	private final String signatureDigest;
	private final String signatureData;
	private final Set<String> signedHeaderNames;
	private final String[] sortedSignedHeaderNames;

	public AuthenticationDataV2(SecurityHttpServletRequestWrapper request, String headerValue)
			throws IOException {
		super(AuthenticationScheme.V2, request, headerValue);

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

		signatureData = computeSignatureData(computeCanonicalRequestData(request));
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
		return computeSignatureDigest(secretKey, new Date());
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
	 * @since 1.1
	 */
	public String computeSignatureDigest(String secretKey, Date signDate) {
		// signing keys are valid for 7 days, so starting with today work backwards at most
		// 7 days to see if we get a match
		Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		cal.setTime(signDate);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		String result = null;
		for ( int i = 0; i < 7; i += 1, cal.add(Calendar.DATE, -1) ) {
			final byte[] signingKey = computeSigningKey(secretKey, cal);
			String computed = Hex.encodeHexString(
					AuthenticationUtils.computeMACDigest(signingKey, signatureData, "HmacSHA256"));
			if ( computed.equals(signatureDigest) ) {
				return computed;
			} else if ( result == null ) {
				// save 1st result as one we return if nothing matches
				result = computed;
			}
		}
		return result;
	}

	private String formatSigningDate(Calendar cal) {
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

	private byte[] computeSigningKey(String secretKey, Calendar cal) {
		/*- signing key is like:
		 
		HMACSHA256(HMACSHA256("SNWS2"+secretKey, "20160301"), "snws2_request")
		*/
		String dateStr = formatSigningDate(cal);
		return AuthenticationUtils.computeMACDigest(
				AuthenticationUtils.computeMACDigest(AuthenticationScheme.V2.getSchemeName() + secretKey,
						dateStr, "HmacSHA256"),
				"snws2_request", "HmacSHA256");
	}

	private String computeSignatureData(String canonicalRequestData) {
		/*- signature data is like:
		 
		 	SNWS2-HMAC-SHA256\n
		 	20170301T120000Z\n
		 	Hex(SHA256(canonicalRequestData))
		*/
		return "SNWS2-HMAC-SHA256\n" + AuthenticationUtils.iso8601Date(getDate()) + "\n"
				+ Hex.encodeHexString(DigestUtils.sha256(canonicalRequestData));
	}

	private String computeCanonicalRequestData(SecurityHttpServletRequestWrapper request)
			throws IOException {
		// 1: HTTP verb
		StringBuilder buf = new StringBuilder(request.getMethod()).append('\n');

		// 2: Canonical URI
		buf.append(request.getRequestURI()).append('\n');

		// 3: Canonical query string
		appendQueryParameters(request, buf);

		// 4: Canonical headers
		appendHeaders(request, buf);

		// 5: Signed headers
		appendSignedHeaderNames(buf);

		// 6: Content SHA256
		appendContentSHA256(request, buf);

		return buf.toString();
	}

	private void appendContentSHA256(SecurityHttpServletRequestWrapper request, StringBuilder buf)
			throws IOException {
		byte[] digest = request.getContentSHA256();
		buf.append(digest == null ? WebConstants.EMPTY_STRING_SHA256_HEX : Hex.encodeHexString(digest));
	}

	private void appendSignedHeaderNames(StringBuilder buf) {
		boolean first = true;
		for ( String headerName : sortedSignedHeaderNames ) {
			if ( first ) {
				first = false;
			} else {
				buf.append(';');
			}
			buf.append(headerName);
		}
		buf.append('\n');
	}

	private void appendHeaders(HttpServletRequest request, StringBuilder buf) {
		for ( String headerName : sortedSignedHeaderNames ) {
			buf.append(headerName).append(':');
			String value = nullSafeHeaderValue(request, headerName).trim();
			buf.append(value);
			if ( "host".equals(headerName) ) {
				if ( value.length() < 1 ) {
					value = request.getServerName();
					if ( value != null ) {
						buf.append(value);
						int port = request.getServerPort();
						if ( port != 80 ) {
							buf.append(':').append(port);
						}
					}
				} else if ( value.indexOf(":") < 0 ) {
					// look for proxy port
					String port = nullSafeHeaderValue(request, "X-Forwarded-Port").trim();
					if ( port.length() < 1 ) {
						String proto = nullSafeHeaderValue(request, "X-Forwarded-Proto").trim()
								.toLowerCase();
						if ( "https".equals(proto) ) {
							port = "443";
						}
					}
					if ( port.length() > 0 && !"80".equals(port) ) {
						buf.append(':').append(port);
					}
				}
			}
			buf.append('\n');
		}
	}

	private void appendQueryParameters(HttpServletRequest request, StringBuilder buf) {
		Set<String> paramKeys = request.getParameterMap().keySet();
		if ( paramKeys.size() < 1 ) {
			buf.append('\n');
			return;
		}
		String[] keys = paramKeys.toArray(new String[paramKeys.size()]);
		Arrays.sort(keys);
		boolean first = true;
		for ( String key : keys ) {
			if ( first ) {
				first = false;
			} else {
				buf.append('&');
			}
			buf.append(AuthenticationUtils.uriEncode(key)).append('=')
					.append(AuthenticationUtils.uriEncode(request.getParameter(key)));
		}
		buf.append('\n');
	}

	private void validateSignedHeaderNames(SecurityHttpServletRequestWrapper request) {
		// MUST include host
		if ( !signedHeaderNames.contains("host") ) {
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
		return signatureData;
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

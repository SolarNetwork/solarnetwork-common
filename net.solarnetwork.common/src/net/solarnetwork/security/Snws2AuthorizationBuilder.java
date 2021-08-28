/* ==================================================================
 * Snws2AuthorizationBuilder.java - 25/04/2017 11:49:43 AM
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

package net.solarnetwork.security;

import static net.solarnetwork.security.AuthorizationUtils.semiColonDelimitedList;
import static net.solarnetwork.security.AuthorizationUtils.uriEncode;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Builder for HTTP {@code Authorization} header values using the SolarNetwork
 * authentication scheme V2.
 * 
 * <p>
 * This builder can be used to calculate a one-off header value, for example:
 * </p>
 * 
 * <pre>
 * <code>
 * String authHeader = new Snws2AuthorizationBuilder("my-token")
 *     .path("/solarquery/api/v1/pub/...")
 *     .build("my-token-secret");
 * </code>
 * </pre>
 * 
 * Or the builder can be re-used for a given token:
 * 
 * <pre>
 * <code>
 * // create a builder for a token
 * Snws2AuthorizationBuilder builder = new Snws2AuthorizationBuilder("my-token");
 * 
 * // elsewhere, re-use the builder for repeated requests
 * builder.reset()
 *     .path("/solarquery/api/v1/pub/...")
 *     .build("my-token-secret");
 * </code>
 * </pre>
 * 
 * Additionally, a signing key can be generated and re-used for up to 7 days:
 * 
 * <pre>
 * <code>
 * // create a builder for a token
 * Snws2AuthorizationBuilder builder = new Snws2AuthorizationBuilder("my-token")
 *   .saveSigningKey("my-token-secret");
 * 
 * // elsewhere, re-use the builder for repeated requests
 * builder.reset()
 *     .path("/solarquery/api/v1/pub/...")
 *     .build(); // note no argument call here, so previously generated key used
 * </code>
 * </pre>
 * 
 * <p>
 * This class is <b>not</b> thread safe and should not be used concurrently.
 * </p>
 * 
 * @author matt
 * @version 1.0
 */
public final class Snws2AuthorizationBuilder
		extends AbstractAuthorizationBuilder<Snws2AuthorizationBuilder> {

	/** The authorization scheme name. */
	public static final String SCHEME_NAME = "SNWS2";

	/** The message used to sign the derived signing key. */
	public static final String SIGNING_KEY_MESSAGE = "snws2_request";

	/** The default {@code host} value. */
	public static final String DEFAULT_HOST = "data.solarnetwork.net:443";

	private MultiValueMap<String, String> parameters;
	private Set<String> signedHeaderNames;
	private boolean useSnDate = false;

	/**
	 * Construct with a token ID.
	 * 
	 * The builder will be initialized and then {@link #reset()} will be called
	 * so default values are configured.
	 * 
	 * @param tokenId
	 *        The token ID to use.
	 */
	public Snws2AuthorizationBuilder(String tokenId) {
		super(tokenId);
	}

	/**
	 * Reset values to their defaults.
	 * 
	 * <p>
	 * All properties will be set to {@code null} except the following:
	 * </p>
	 * 
	 * <dl>
	 * <dt>method</dt>
	 * <dd>Will be set to {@code GET}.</dd>
	 * 
	 * <dt>host</dt>
	 * <dd>Will be set to {@link #DEFAULT_HOST}.</dd>
	 * 
	 * <dt>path</dt>
	 * <dd>Will be set to {@code /}.</dd>
	 * 
	 * <dt>date</dt>
	 * <dd>Will be set to the current time.</dd>
	 * 
	 * <dt>headers</dt>
	 * <dd>Will be cleared.</dd>
	 * 
	 * <dt>parameters</dt>
	 * <dd>Will be cleared.</dd>
	 * 
	 * <dt>contentSha256</dt>
	 * <dd>Will be cleared.</dd>
	 * 
	 * <dt>signingKey</dt>
	 * <dd>This value will <b>not</b> be changed.</dd>
	 * </dl>
	 * 
	 * @return this builder
	 */
	@Override
	public Snws2AuthorizationBuilder reset() {
		super.reset().host(DEFAULT_HOST);
		parameters = null;
		signedHeaderNames = null;
		return this;
	}

	/**
	 * Set the date header style.
	 * 
	 * <p>
	 * The existing date header will be renamed to match the desired header per
	 * {@code useSnDate} if the desired header does not already exist. Either
	 * way, the old header value will be removed if it exists.
	 * </p>
	 * 
	 * @param useSnDate
	 *        {@literal true} to use the {@literal X-SN-Date} header,
	 *        {@literal false} to use {@literal Date}
	 * @return this builder
	 */
	public Snws2AuthorizationBuilder useSnDate(boolean useSnDate) {
		this.useSnDate = useSnDate;
		MultiValueMap<String, String> headers = getHeaders();
		String want, discard;
		if ( useSnDate ) {
			want = "x-sn-date";
			discard = "date";
		} else {
			want = "date";
			discard = "x-sn-date";
		}
		boolean hasDiscard = headers.containsKey(discard);
		if ( !headers.containsKey(want) && hasDiscard ) {
			headers.put(want, headers.get(discard));
		} else if ( hasDiscard ) {
			headers.remove(discard);
		}
		return this;
	}

	/**
	 * Get the date header style.
	 * 
	 * @return {@literal true} to use the {@literal X-SN-Date} header,
	 *         {@literal false} to use {@literal Date}
	 */
	public boolean isUseSnDate() {
		return useSnDate;
	}

	/**
	 * Set the HTTP method.
	 * 
	 * <p>
	 * This is an alias for {@link #verb(String)}.
	 * </p>
	 * 
	 * @param method
	 *        the method
	 * @return this builder
	 */
	public Snws2AuthorizationBuilder method(String method) {
		return verb(method);
	}

	/**
	 * Set the request date.
	 * 
	 * <p>
	 * This will also set the {@literal date} header with the date's formatted
	 * value.
	 * </p>
	 * 
	 * @param date
	 *        the date to use, or {@literal null} for the current system time
	 *        via {@code Instant.now()}; will be truncated to second resolution
	 * @return this builder
	 */
	@Override
	public Snws2AuthorizationBuilder date(Instant date) {
		super.date(date);
		return header(useSnDate ? "x-sn-date" : "date",
				AuthorizationUtils.AUTHORIZATION_DATE_HEADER_FORMATTER.format(getDate()));
	}

	/**
	 * Set the HTTP content type.
	 * 
	 * <p>
	 * This is a shortcut for calling {@code #header(String, String)} with a
	 * {@literal Content-Type} key.
	 * </p>
	 * 
	 * @param contentType
	 *        The content type to use.
	 * @return this builder
	 */
	public Snws2AuthorizationBuilder contentType(String contentType) {
		return header("content-type", contentType);
	}

	/**
	 * Set the HTTP body content MD5 digest.
	 * 
	 * <p>
	 * This is a shortcut for calling {@code #header(String, String)} with a
	 * {@literal Content-MD5} key.
	 * </p>
	 * 
	 * @param md5
	 *        The content MD5 to use.
	 * @return this builder
	 */
	public Snws2AuthorizationBuilder contentMD5(String md5) {
		return header("content-md5", md5);
	}

	/**
	 * Set the HTTP body content digest.
	 * 
	 * <p>
	 * This is a shortcut for calling {@code #headers(String, String)} with a
	 * {@literal Digest} key.
	 * </p>
	 * 
	 * @param digest
	 *        The digest to use.
	 * @return this builder
	 */
	public Snws2AuthorizationBuilder digest(String digest) {
		return header("digest", digest);
	}

	/**
	 * Set the HTTP {@code GET} query parameters, or {@code POST} form-encoded
	 * parameters.
	 * 
	 * @param params
	 *        the request parameters to use
	 * @return this builder
	 */
	public Snws2AuthorizationBuilder parameterMap(Map<String, String[]> params) {
		MultiValueMap<String, String> map = null;
		if ( params != null ) {
			map = new LinkedMultiValueMap<String, String>(params.size());
			for ( Map.Entry<String, String[]> me : params.entrySet() ) {
				for ( String v : me.getValue() ) {
					map.add(me.getKey(), v);
				}
			}
		}
		this.parameters = map;
		return this;
	}

	/**
	 * Set the HTTP {@code GET} query parameters, or {@code POST} form-encoded
	 * parameters, as a simple {@code Map}.
	 * 
	 * @param params
	 *        the parameters to use
	 * @return this builder
	 */
	public Snws2AuthorizationBuilder queryParams(Map<String, String> params) {
		MultiValueMap<String, String> map = null;
		if ( params != null ) {
			map = new LinkedMultiValueMap<String, String>(params.size());
			for ( Map.Entry<String, String> me : params.entrySet() ) {
				map.add(me.getKey(), me.getValue());
			}
		}
		this.parameters = map;
		return this;
	}

	/**
	 * Set additional HTTP header names to sign with the digest.
	 * 
	 * @param signedHeaderNames
	 *        additional HTTP header names to include in the computed digest
	 * @return this builder
	 */
	public Snws2AuthorizationBuilder signedHttpHeaders(Set<String> signedHeaderNames) {
		this.signedHeaderNames = signedHeaderNames;
		return this;
	}

	@Override
	public String[] sortedHeaderNames() {
		SortedSet<String> headerNames = new TreeSet<>();
		final MultiValueMap<String, String> h = getHeaders();
		int count = 0;
		if ( h != null ) {
			for ( String k : h.keySet() ) {
				if ( k != null ) {
					headerNames.add(k.toLowerCase());
					count++;
				}
			}
		}
		if ( signedHeaderNames != null ) {
			for ( String s : signedHeaderNames ) {
				headerNames.add(s.toLowerCase());
				count++;
			}
		}
		return headerNames.toArray(new String[count]);
	}

	private void appendQueryParameters(StringBuilder buf) {
		Set<String> paramKeys = (parameters != null ? parameters.keySet()
				: Collections.<String> emptySet());
		if ( paramKeys.size() < 1 ) {
			buf.append('\n');
			return;
		}
		String[] keys = paramKeys.toArray(new String[paramKeys.size()]);
		Arrays.sort(keys);
		boolean first = true;
		for ( String key : keys ) {
			for ( String val : parameters.get(key) ) {
				if ( first ) {
					first = false;
				} else {
					buf.append('&');
				}
				buf.append(uriEncode(key)).append('=').append(uriEncode(val));
			}
		}
		buf.append('\n');
	}

	@Override
	protected String signingKeyMessageLiteral() {
		return SIGNING_KEY_MESSAGE;
	}

	@Override
	protected String schemeName() {
		return SCHEME_NAME;
	}

	@Override
	protected String computeCanonicalRequestMessage(String[] headerNames) {
		// 1: HTTP verb
		StringBuilder buf = new StringBuilder(getVerb()).append('\n');

		// 2: Canonical URI
		buf.append(getPath()).append('\n');

		// 3: Canonical query string
		appendQueryParameters(buf);

		if ( headerNames == null || headerNames.length < 1 ) {
			buf.append('\n').append('\n');
		} else {
			// 4: Canonical headers
			appendHeaders(headerNames, buf);

			// 5: Signed headers
			buf.append(semiColonDelimitedList(headerNames)).append('\n');
		}

		// 6: Content SHA256
		appendContentSha256(buf);

		return buf.toString();

	}

}

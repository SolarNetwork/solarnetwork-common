/* ==================================================================
 * AuthorizationV2RequestInterceptor.java - 13/08/2019 10:11:07 am
 * 
 * Copyright 2019 SolarNetwork.net Dev Team
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

package net.solarnetwork.web.support;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Map;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import net.solarnetwork.web.security.AuthorizationCredentialsProvider;
import net.solarnetwork.web.security.AuthorizationV2Builder;

/**
 * Interceptor to add an {@literal Authorization} HTTP header using the SNWS2
 * scheme.
 * 
 * <p>
 * This class is initialized with an {@link AuthorizationCredentialsProvider}
 * instance that must provide the credentials used to authorize and sign each
 * request.
 * </p>
 * 
 * @author matt
 * @version 1.1
 * @since 1.16
 */
public class AuthorizationV2RequestInterceptor implements ClientHttpRequestInterceptor {

	private final AuthorizationCredentialsProvider credentialsProvider;

	/**
	 * Constructor.
	 * 
	 * @param credentialsProvider
	 *        the API token credentials provider
	 */
	public AuthorizationV2RequestInterceptor(AuthorizationCredentialsProvider credentialsProvider) {
		super();
		this.credentialsProvider = credentialsProvider;
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body,
			ClientHttpRequestExecution execution) throws IOException {

		AuthorizationV2Builder builder = new AuthorizationV2Builder(
				credentialsProvider.getAuthorizationId());
		URI uri = request.getURI();
		HttpHeaders headers = request.getHeaders();
		if ( !headers.containsKey(HttpHeaders.HOST) ) {
			StringBuilder buf = new StringBuilder(uri.getHost());
			if ( uri.getPort() > 0 && uri.getPort() != 80 ) {
				buf.append(':').append(uri.getPort());
			}
			headers.set(HttpHeaders.HOST, buf.toString());
		}
		MediaType contentType = headers.getContentType();
		Charset charset = (contentType != null && contentType.getCharset() != null
				? contentType.getCharset()
				: Charset.forName("UTF-8"));
		if ( uri.getRawQuery() != null ) {
			builder.queryParams(queryParams(uri.getRawQuery(), charset));
		} else if ( body != null && body.length > 0 && HttpMethod.POST.equals(request.getMethod())
				&& MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(contentType) ) {
			builder.queryParams(queryParams(new String(body, charset.name()), charset));
		}
		builder.method(request.getMethod()).path(uri.getPath()).headers(headers);
		long reqDate = request.getHeaders().getDate();
		if ( reqDate < 1 ) {
			reqDate = System.currentTimeMillis();
			headers.setDate(reqDate);
		}
		builder.date(new Date(reqDate));
		if ( body != null && body.length > 0
				&& !MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(contentType) ) {
			builder.contentSHA256(DigestUtils.sha256(body));
		}
		headers.set(HttpHeaders.AUTHORIZATION,
				builder.build(credentialsProvider.getAuthorizationSecret()));
		return execution.execute(request, body);
	}

	private Map<String, String> queryParams(String body, Charset charset)
			throws UnsupportedEncodingException {
		String[] pairs = StringUtils.tokenizeToStringArray(body, "&");
		MultiValueMap<String, String> result = new LinkedMultiValueMap<String, String>(pairs.length);
		for ( String pair : pairs ) {
			int idx = pair.indexOf('=');
			if ( idx == -1 ) {
				result.add(URLDecoder.decode(pair, charset.name()), null);
			} else {
				String name = URLDecoder.decode(pair.substring(0, idx), charset.name());
				String value = URLDecoder.decode(pair.substring(idx + 1), charset.name());
				result.add(name, value);
			}
		}
		return result.toSingleValueMap();
	}

}

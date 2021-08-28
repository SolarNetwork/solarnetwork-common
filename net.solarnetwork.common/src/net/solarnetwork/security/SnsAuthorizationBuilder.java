/* ==================================================================
 * SnsAuthorizationBuilder.java - 13/08/2021 4:48:45 PM
 * 
 * Copyright 2021 SolarNetwork.net Dev Team
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
import java.time.Instant;

/**
 * Builder for {@code Authorization} header values using the SolarNode Setup
 * (SNS) authentication scheme.
 * 
 * <p>
 * This helper is designed with different communication protocols in mind, such
 * as HTTP and STOMP, so the terms used are generic enough to apply in different
 * contexts.
 * </p>
 * 
 * @author matt
 * @version 1.0
 * @since 1.78
 */
public class SnsAuthorizationBuilder extends AbstractAuthorizationBuilder<SnsAuthorizationBuilder> {

	/** The authorization scheme name. */
	public static final String SCHEME_NAME = "SNS";

	/** The message used to sign the derived signing key. */
	public static final String SIGNING_KEY_MESSAGE = "sns_request";

	/**
	 * Construct with a credential.
	 * 
	 * <p>
	 * The builder will be initialized and then {@link #reset()} will be called
	 * so default values are configured.
	 * </p>
	 * 
	 * @param identifier
	 *        the bearer's identifier, such as a token ID or username
	 */
	public SnsAuthorizationBuilder(String identifier) {
		super(identifier);
	}

	/**
	 * Reset all values to their defaults.
	 * 
	 * <p>
	 * All properties will be set to {@code null} except the following:
	 * </p>
	 * 
	 * <dl>
	 * <dt>verb</dt>
	 * <dd>will be set to {@literal GET}</dd>
	 * 
	 * <dt>path</dt>
	 * <dd>Will be set to {@literal /}</dd>
	 * 
	 * <dt>date</dt>
	 * <dd>will be set to the current time</dd>
	 * 
	 * <dt>signingKey</dt>
	 * <dd>this value will <b>not</b> be changed</dd>
	 * </dl>
	 * 
	 * @return The builder.
	 */
	@Override
	public SnsAuthorizationBuilder reset() {
		return super.reset();
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
	public SnsAuthorizationBuilder date(Instant date) {
		super.date(date);
		return header("date", AuthorizationUtils.AUTHORIZATION_DATE_HEADER_FORMATTER.format(getDate()));
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
	protected String computeCanonicalRequestMessage(final String[] headerNames) {
		// 1: verb
		StringBuilder buf = new StringBuilder(getVerb()).append('\n');

		// 2: path
		buf.append(getPath()).append('\n');

		// 3: headers
		if ( headerNames == null || headerNames.length < 1 ) {
			buf.append('\n').append('\n');
		} else {
			appendHeaders(headerNames, buf);
			buf.append(semiColonDelimitedList(headerNames)).append('\n');
		}

		// 3: Content SHA256
		appendContentSha256(buf);

		return buf.toString();
	}

}

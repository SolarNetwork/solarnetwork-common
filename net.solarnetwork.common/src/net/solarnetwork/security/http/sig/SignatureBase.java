/* ==================================================================
 * SignatureBase.java - 19/09/2026 9:58:07 am
 *
 * Copyright 2026 SolarNetwork.net Dev Team
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

package net.solarnetwork.security.http.sig;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * The signature base of a message signature, per RFC 9421 section 2.5.
 *
 * <p>
 * The signature base is the exact string that both signer and verifier must
 * produce, so the individual lines are kept alongside the joined value: when a
 * signature fails to verify, the line-by-line form is what makes the difference
 * visible.
 * </p>
 *
 * @author matt
 * @version 1.0
 */
public final class SignatureBase {

	private final List<String> lines;
	private final String value;

	private SignatureBase(List<String> lines, String value) {
		this.lines = Collections.unmodifiableList(lines);
		this.value = value;
	}

	/**
	 * Compute the signature base for a request message.
	 *
	 * @param context
	 *        the message component values
	 * @param parameters
	 *        the signature parameters
	 * @param canonicalizer
	 *        the HTTP field canonicalizer
	 * @return the signature base
	 * @throws HttpSignatureException
	 *         if the base cannot be computed
	 */
	public static SignatureBase compute(SignatureContext context, SignatureParameters parameters,
			FieldCanonicalizer canonicalizer) {
		final List<SignatureComponent> components = parameters.components();
		final List<String> lines = new ArrayList<>(components.size() + 1);
		final Set<String> seen = new LinkedHashSet<>(components.size());

		for ( SignatureComponent component : components ) {
			final String identifier = component.identifier();
			if ( !seen.add(identifier) ) {
				// RFC 9421 2.5: a repeated identifier is an error, not a duplicate line
				throw new HttpSignatureException(
						"The component " + identifier + " is covered more than once.");
			}
			lines.add(identifier + ": " + componentValue(context, component, canonicalizer));
		}

		lines.add('"' + SignatureComponent.SIGNATURE_PARAMS + "\": " + parameters.serialize());

		final String value = String.join("\n", lines);
		for ( int i = 0, len = value.length(); i < len; i++ ) {
			if ( value.charAt(i) > 127 ) {
				throw new HttpSignatureException(
						"The signature base contains a non-ASCII character at offset " + i + ".");
			}
		}
		return new SignatureBase(lines, value);
	}

	private static String componentValue(SignatureContext context, SignatureComponent component,
			FieldCanonicalizer canonicalizer) {
		final String name = component.name();
		if ( component.isTr() ) {
			throw new HttpSignatureException("The component " + component.identifier()
					+ " uses the 'tr' parameter, but trailer fields are not supported.");
		}
		if ( component.isReq() ) {
			// RFC 9421 2.4: req MUST NOT be used in a signature targeting a request
			throw new HttpSignatureException("The component " + component.identifier()
					+ " uses the 'req' parameter, which is not valid for a request signature.");
		}
		if ( !component.isDerived() ) {
			return canonicalizer.canonicalize(component, context.fieldValues(name));
		}
		return switch (name) {
			case SignatureComponent.METHOD -> context.method();
			case SignatureComponent.TARGET_URI -> context.targetUri();
			case SignatureComponent.AUTHORITY -> context.authority();
			case SignatureComponent.SCHEME -> context.scheme();
			case SignatureComponent.REQUEST_TARGET -> context.requestTarget();
			case SignatureComponent.PATH -> context.path();
			case SignatureComponent.QUERY -> context.query();
			case SignatureComponent.QUERY_PARAM -> queryParamValue(context, component);
			case SignatureComponent.STATUS -> throw new HttpSignatureException("The component "
					+ component.identifier() + " applies to a response, not a request.");
			case SignatureComponent.SIGNATURE_PARAMS -> throw new HttpSignatureException(
					"The component " + component.identifier() + " must not be covered explicitly.");
			default -> throw new HttpSignatureException(
					"The derived component " + component.identifier() + " is not supported.");
		};
	}

	private static String queryParamValue(SignatureContext context, SignatureComponent component) {
		final String encodedName = component.queryParamName();
		if ( encodedName == null ) {
			throw new HttpSignatureException("The component " + component.identifier()
					+ " requires a 'name' parameter.");
		}
		final List<String> values = context.queryParamValues(URLDecoder.decode(encodedName, UTF_8));
		if ( values.isEmpty() ) {
			throw new HttpSignatureException("The query parameter covered by "
					+ component.identifier() + " is not present in the request.");
		}
		if ( values.size() > 1 ) {
			// RFC 9421 2.2.8: a repeated parameter MUST NOT be covered this way
			throw new HttpSignatureException("The query parameter covered by "
					+ component.identifier() + " occurs more than once in the request.");
		}
		return values.get(0);
	}

	/**
	 * Get the individual lines of the signature base.
	 *
	 * <p>
	 * The last line is always the {@code @signature-params} line.
	 * </p>
	 *
	 * @return the lines, never {@code null}
	 */
	public List<String> lines() {
		return lines;
	}

	/**
	 * Get the complete signature base value.
	 *
	 * @return the value, never {@code null}
	 */
	public String value() {
		return value;
	}

	/**
	 * Get the signature base encoded as ASCII, ready to sign or verify.
	 *
	 * @return the encoded value, never {@code null}
	 */
	public byte[] bytes() {
		return value.getBytes(US_ASCII);
	}

	@Override
	public String toString() {
		return value;
	}

}

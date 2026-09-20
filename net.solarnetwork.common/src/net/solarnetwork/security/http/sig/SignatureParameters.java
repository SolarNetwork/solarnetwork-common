/* ==================================================================
 * SignatureParameters.java - 19/09/2026 9:22:31 am
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.greenbytes.http.sfv.InnerList;
import org.greenbytes.http.sfv.IntegerItem;
import org.greenbytes.http.sfv.Item;
import org.greenbytes.http.sfv.Parameters;
import org.greenbytes.http.sfv.SfDataType;
import org.greenbytes.http.sfv.StringItem;
import org.jspecify.annotations.Nullable;

/**
 * The signature parameters of a single message signature, as defined in RFC
 * 9421 section 2.3.
 *
 * <p>
 * This is the ordered set of covered components together with the signature
 * metadata parameters. Both orderings are significant: the serialized form is
 * what appears as the {@code @signature-params} line of the signature base, and
 * it must match the {@code Signature-Input} field value exactly. When parsed
 * from a message, the original structured field value is retained and re-used
 * for serialization, so that no re-serialization difference can invalidate an
 * otherwise valid signature.
 * </p>
 *
 * @author matt
 * @version 1.0
 */
public final class SignatureParameters {

	/** The {@code created} signature parameter. */
	public static final String PARAM_CREATED = "created";

	/** The {@code expires} signature parameter. */
	public static final String PARAM_EXPIRES = "expires";

	/** The {@code nonce} signature parameter. */
	public static final String PARAM_NONCE = "nonce";

	/** The {@code alg} signature parameter. */
	public static final String PARAM_ALG = "alg";

	/** The {@code keyid} signature parameter. */
	public static final String PARAM_KEYID = "keyid";

	/** The {@code tag} signature parameter. */
	public static final String PARAM_TAG = "tag";

	private final List<SignatureComponent> components;
	private final InnerList innerList;

	private SignatureParameters(List<SignatureComponent> components, InnerList innerList) {
		this.components = Collections.unmodifiableList(components);
		this.innerList = innerList;
	}

	/**
	 * Create parameters from a parsed {@code Signature-Input} member value.
	 *
	 * @param innerList
	 *        the inner list
	 * @return the parameters
	 * @throws HttpSignatureException
	 *         if any component identifier is malformed
	 */
	public static SignatureParameters forInnerList(InnerList innerList) {
		final List<Item<?>> items = innerList.get();
		final List<SignatureComponent> components = new ArrayList<>(items.size());
		for ( Item<?> item : items ) {
			components.add(SignatureComponent.forItem(item));
		}
		return new SignatureParameters(components, innerList);
	}

	/**
	 * Create parameters from components and metadata.
	 *
	 * @param components
	 *        the covered components, in signing order
	 * @param params
	 *        the signature metadata parameters, in signing order; values may be
	 *        {@link Instant}, {@link Long}, {@link Integer}, or {@link String},
	 *        and {@code null} values are skipped
	 * @return the parameters
	 */
	public static SignatureParameters of(List<SignatureComponent> components,
			Map<String, @Nullable Object> params) {
		final List<Item<?>> items = new ArrayList<>(components.size());
		for ( SignatureComponent c : components ) {
			items.add(c.item());
		}
		final Map<String, Object> meta = new LinkedHashMap<>(params.size());
		for ( Map.Entry<String, @Nullable Object> e : params.entrySet() ) {
			Object v = e.getValue();
			if ( v == null ) {
				continue;
			}
			if ( v instanceof Instant ts ) {
				v = IntegerItem.of(ts.getEpochSecond());
			} else if ( v instanceof String s ) {
				v = StringItem.of(s);
			}
			meta.put(e.getKey(), v);
		}
		final InnerList innerList = InnerList.of(items).withParams(Parameters.of(meta));
		return new SignatureParameters(new ArrayList<>(components), innerList);
	}

	/**
	 * Get the covered components.
	 *
	 * @return the components, in signing order, never {@code null}
	 */
	public List<SignatureComponent> components() {
		return components;
	}

	/**
	 * Get the serialized signature parameters value.
	 *
	 * <p>
	 * This is the value of the {@code @signature-params} line in the signature
	 * base, and also the {@code Signature-Input} member value.
	 * </p>
	 *
	 * @return the serialized value, never {@code null}
	 */
	public String serialize() {
		return innerList.serialize();
	}

	/**
	 * Get the {@code created} parameter.
	 *
	 * @return the creation time, or {@code null} if not present
	 * @throws HttpSignatureException
	 *         if the parameter is not an Integer
	 */
	public @Nullable Instant created() {
		final Long v = integerParameter(PARAM_CREATED);
		return (v != null ? Instant.ofEpochSecond(v) : null);
	}

	/**
	 * Get the {@code expires} parameter.
	 *
	 * @return the expiration time, or {@code null} if not present
	 * @throws HttpSignatureException
	 *         if the parameter is not an Integer
	 */
	public @Nullable Instant expires() {
		final Long v = integerParameter(PARAM_EXPIRES);
		return (v != null ? Instant.ofEpochSecond(v) : null);
	}

	/**
	 * Get the {@code nonce} parameter.
	 *
	 * @return the nonce, or {@code null} if not present
	 */
	public @Nullable String nonce() {
		return stringParameter(PARAM_NONCE);
	}

	/**
	 * Get the {@code alg} parameter.
	 *
	 * @return the algorithm name, or {@code null} if not present
	 */
	public @Nullable String alg() {
		return stringParameter(PARAM_ALG);
	}

	/**
	 * Get the {@code keyid} parameter.
	 *
	 * @return the key identifier, or {@code null} if not present
	 */
	public @Nullable String keyid() {
		return stringParameter(PARAM_KEYID);
	}

	/**
	 * Get the {@code tag} parameter.
	 *
	 * @return the application tag, or {@code null} if not present
	 */
	public @Nullable String tag() {
		return stringParameter(PARAM_TAG);
	}

	private @Nullable String stringParameter(String name) {
		final Item<?> p = innerList.params().get(name);
		if ( p == null ) {
			return null;
		}
		if ( p.getType() != SfDataType.STRING ) {
			throw new HttpSignatureException("Signature parameter [" + name
					+ "] must be a String, but is a " + p.getType() + ".");
		}
		return p.stringValue();
	}

	private @Nullable Long integerParameter(String name) {
		final Item<?> p = innerList.params().get(name);
		if ( p == null ) {
			return null;
		}
		if ( p.getType() != SfDataType.INTEGER ) {
			throw new HttpSignatureException("Signature parameter [" + name
					+ "] must be an Integer, but is a " + p.getType() + ".");
		}
		return p.longValue();
	}

	@Override
	public String toString() {
		return serialize();
	}

}

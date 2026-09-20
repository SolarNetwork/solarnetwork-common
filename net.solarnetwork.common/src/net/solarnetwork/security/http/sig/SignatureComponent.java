/* ==================================================================
 * SignatureComponent.java - 19/09/2026 9:14:03 am
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

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.greenbytes.http.sfv.BooleanItem;
import org.greenbytes.http.sfv.Item;
import org.greenbytes.http.sfv.Parameters;
import org.greenbytes.http.sfv.SfDataType;
import org.greenbytes.http.sfv.StringItem;
import org.jspecify.annotations.Nullable;

/**
 * A single covered component identifier, as defined in RFC 9421 section 2.
 *
 * <p>
 * A component identifier is a name, which is either an HTTP field name in
 * lower case or a derived component name starting with {@code @}, together
 * with any of the parameters defined in section 2.1.
 * </p>
 *
 * @author matt
 * @version 1.0
 */
public final class SignatureComponent {

	/** The {@code @method} derived component name. */
	public static final String METHOD = "@method";

	/** The {@code @target-uri} derived component name. */
	public static final String TARGET_URI = "@target-uri";

	/** The {@code @authority} derived component name. */
	public static final String AUTHORITY = "@authority";

	/** The {@code @scheme} derived component name. */
	public static final String SCHEME = "@scheme";

	/** The {@code @request-target} derived component name. */
	public static final String REQUEST_TARGET = "@request-target";

	/** The {@code @path} derived component name. */
	public static final String PATH = "@path";

	/** The {@code @query} derived component name. */
	public static final String QUERY = "@query";

	/** The {@code @query-param} derived component name. */
	public static final String QUERY_PARAM = "@query-param";

	/** The {@code @status} derived component name (responses only). */
	public static final String STATUS = "@status";

	/** The {@code @signature-params} special component name. */
	public static final String SIGNATURE_PARAMS = "@signature-params";

	/** The {@code sf} strict structured field serialization parameter. */
	public static final String PARAM_SF = "sf";

	/** The {@code key} dictionary member parameter. */
	public static final String PARAM_KEY = "key";

	/** The {@code bs} byte sequence wrapping parameter. */
	public static final String PARAM_BS = "bs";

	/** The {@code tr} trailer parameter. */
	public static final String PARAM_TR = "tr";

	/** The {@code req} related request parameter. */
	public static final String PARAM_REQ = "req";

	/** The {@code name} query parameter name parameter. */
	public static final String PARAM_NAME = "name";

	private final String name;
	private final Item<?> item;

	private SignatureComponent(String name, Item<?> item) {
		this.name = name;
		this.item = item;
	}

	/**
	 * Create a component from a parsed structured field item.
	 *
	 * @param item
	 *        the item, whose bare value must be a String
	 * @return the component
	 * @throws HttpSignatureException
	 *         if the item is not a String item
	 */
	public static SignatureComponent forItem(Item<?> item) {
		if ( item.getType() != SfDataType.STRING ) {
			throw new HttpSignatureException(
					"Component identifier must be a String, but is a " + item.getType() + ".");
		}
		// component names are always lower case; a signer that uses any other case has
		// produced a name that can never match a field or derived component
		final String value = item.stringValue();
		if ( !value.equals(value.toLowerCase(Locale.ROOT)) ) {
			throw new HttpSignatureException(
					"Component identifier [" + value + "] must be in lower case.");
		}
		return new SignatureComponent(value, item);
	}

	/**
	 * Create a component with no parameters.
	 *
	 * @param name
	 *        the component name, which will be lower-cased
	 * @return the component
	 */
	public static SignatureComponent of(String name) {
		final String lc = name.toLowerCase(Locale.ROOT);
		return new SignatureComponent(lc, StringItem.of(lc));
	}

	/**
	 * Create a component with parameters.
	 *
	 * @param name
	 *        the component name, which will be lower-cased
	 * @param params
	 *        the parameters, in the order they should be serialized; boolean
	 *        values are serialized as flags
	 * @return the component
	 */
	public static SignatureComponent of(String name, Map<String, Object> params) {
		final String lc = name.toLowerCase(Locale.ROOT);
		final Map<String, Object> items = new LinkedHashMap<>(params.size());
		for ( Map.Entry<String, Object> e : params.entrySet() ) {
			final Object v = e.getValue();
			items.put(e.getKey(), v instanceof Boolean b ? BooleanItem.of(b)
					: v instanceof String s ? StringItem.of(s) : v);
		}
		return new SignatureComponent(lc, StringItem.of(lc).withParams(Parameters.of(items)));
	}

	/**
	 * Get the component name.
	 *
	 * @return the name, in lower case, never {@code null}
	 */
	public String name() {
		return name;
	}

	/**
	 * Test if this component is a derived component, rather than an HTTP field.
	 *
	 * @return {@code true} if the name starts with {@code @}
	 */
	public boolean isDerived() {
		return name.startsWith("@");
	}

	/**
	 * Get the underlying structured field item.
	 *
	 * @return the item, never {@code null}
	 */
	Item<?> item() {
		return item;
	}

	/**
	 * Get the serialized component identifier, including any parameters.
	 *
	 * <p>
	 * This is the value that prefixes the component's line in the signature
	 * base.
	 * </p>
	 *
	 * @return the serialized identifier, never {@code null}
	 */
	public String identifier() {
		return item.serialize();
	}

	/**
	 * Get a boolean parameter value.
	 *
	 * @param paramName
	 *        the parameter name
	 * @return the value, or {@code false} if not present
	 * @throws HttpSignatureException
	 *         if the parameter is present but is not a Boolean
	 */
	public boolean booleanParameter(String paramName) {
		final Item<?> p = item.params().get(paramName);
		if ( p == null ) {
			return false;
		}
		if ( p.getType() != SfDataType.BOOLEAN ) {
			throw new HttpSignatureException("Component [" + name + "] parameter [" + paramName
					+ "] must be a Boolean, but is a " + p.getType() + ".");
		}
		return p.booleanValue();
	}

	/**
	 * Get a string parameter value.
	 *
	 * @param paramName
	 *        the parameter name
	 * @return the value, or {@code null} if not present
	 * @throws HttpSignatureException
	 *         if the parameter is present but is not a String
	 */
	public @Nullable String stringParameter(String paramName) {
		final Item<?> p = item.params().get(paramName);
		if ( p == null ) {
			return null;
		}
		if ( p.getType() != SfDataType.STRING ) {
			throw new HttpSignatureException("Component [" + name + "] parameter [" + paramName
					+ "] must be a String, but is a " + p.getType() + ".");
		}
		return p.stringValue();
	}

	/**
	 * Get the names of all parameters present on this component.
	 *
	 * @return the parameter names, in serialization order, never {@code null}
	 */
	public Iterable<String> parameterNames() {
		return item.params().keySet();
	}

	/**
	 * Test if the {@code sf} flag is set.
	 *
	 * @return {@code true} if strict structured field serialization is wanted
	 */
	public boolean isSf() {
		return booleanParameter(PARAM_SF);
	}

	/**
	 * Get the {@code key} dictionary member parameter.
	 *
	 * @return the dictionary key, or {@code null}
	 */
	public @Nullable String key() {
		return stringParameter(PARAM_KEY);
	}

	/**
	 * Test if the {@code bs} flag is set.
	 *
	 * @return {@code true} if byte sequence wrapping is wanted
	 */
	public boolean isBs() {
		return booleanParameter(PARAM_BS);
	}

	/**
	 * Test if the {@code tr} flag is set.
	 *
	 * @return {@code true} if the value is to be taken from the trailers
	 */
	public boolean isTr() {
		return booleanParameter(PARAM_TR);
	}

	/**
	 * Test if the {@code req} flag is set.
	 *
	 * @return {@code true} if the value is to be taken from the related request
	 */
	public boolean isReq() {
		return booleanParameter(PARAM_REQ);
	}

	/**
	 * Get the {@code name} query parameter name parameter.
	 *
	 * @return the query parameter name, or {@code null}
	 */
	public @Nullable String queryParamName() {
		return stringParameter(PARAM_NAME);
	}

	@Override
	public String toString() {
		return identifier();
	}

	@Override
	public int hashCode() {
		return identifier().hashCode();
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		return (obj instanceof SignatureComponent other && identifier().equals(other.identifier()));
	}

}

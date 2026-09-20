/* ==================================================================
 * FieldCanonicalizer.java - 19/09/2026 9:44:19 am
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

import static java.nio.charset.StandardCharsets.UTF_8;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import org.greenbytes.http.sfv.ByteSequenceItem;
import org.greenbytes.http.sfv.Dictionary;
import org.greenbytes.http.sfv.Item;
import org.greenbytes.http.sfv.ListElement;
import org.greenbytes.http.sfv.ParseException;
import org.greenbytes.http.sfv.SfList;

/**
 * Computes HTTP field component values, per RFC 9421 section 2.1.
 *
 * @author matt
 * @version 1.0
 */
public final class FieldCanonicalizer {

	/**
	 * The structured field data types, for the {@code sf} component parameter.
	 */
	public enum StructuredType {
		/** A Dictionary. */
		Dictionary,

		/** A List. */
		List,

		/** An Item. */
		Item;
	}

	/** Obsolete line folding: a line break followed by whitespace. */
	private static final Pattern OBS_FOLD = Pattern.compile("\\r?\\n[ \\t]+");

	private final Map<String, StructuredType> structuredTypes = new ConcurrentHashMap<>(
			defaultStructuredTypes());

	private static Map<String, StructuredType> defaultStructuredTypes() {
		// the structured fields an API request is likely to carry; the sf and key
		// parameters need to know a field's type, which is a property of the field
		// definition rather than of the message
		return Map.ofEntries(Map.entry("accept", StructuredType.List),
				Map.entry("accept-encoding", StructuredType.List),
				Map.entry("accept-language", StructuredType.List),
				Map.entry("cache-control", StructuredType.Dictionary),
				Map.entry("content-digest", StructuredType.Dictionary),
				Map.entry("content-length", StructuredType.Item),
				Map.entry("content-type", StructuredType.Item),
				Map.entry("priority", StructuredType.Dictionary),
				Map.entry("repr-digest", StructuredType.Dictionary),
				Map.entry("signature", StructuredType.Dictionary),
				Map.entry("signature-input", StructuredType.Dictionary),
				Map.entry("want-content-digest", StructuredType.Dictionary),
				Map.entry("want-repr-digest", StructuredType.Dictionary));
	}

	/**
	 * Constructor.
	 */
	public FieldCanonicalizer() {
		super();
	}

	/**
	 * Register the structured field type of a field.
	 *
	 * @param fieldName
	 *        the field name, which will be lower-cased
	 * @param type
	 *        the structured field type
	 * @return this instance, to allow method chaining
	 */
	public FieldCanonicalizer withStructuredType(String fieldName, StructuredType type) {
		structuredTypes.put(fieldName.toLowerCase(Locale.ROOT), type);
		return this;
	}

	/**
	 * Compute the component value for an HTTP field.
	 *
	 * @param component
	 *        the component, which must not be a derived component
	 * @param values
	 *        the field values, one per instance of the field in the message, in
	 *        message order
	 * @return the component value
	 * @throws HttpSignatureException
	 *         if the field is not present, the component parameters are
	 *         incompatible, or a structured field value cannot be parsed
	 */
	public String canonicalize(SignatureComponent component, List<String> values) {
		final String name = component.name();
		if ( values.isEmpty() ) {
			throw new HttpSignatureException(
					"The [" + name + "] HTTP field is covered by the signature but is not present"
							+ " in the request.");
		}

		final boolean bs = component.isBs();
		final boolean sf = component.isSf();
		final String key = component.key();

		if ( bs && (sf || key != null) ) {
			// RFC 9421 2.1: bs needs the raw bytes, sf and key need the parsed structure
			throw new HttpSignatureException("The [" + name
					+ "] HTTP field cannot combine the 'bs' parameter with 'sf' or 'key'.");
		}

		if ( bs ) {
			return byteSequenceValue(values);
		}

		final String combined = combine(values);

		if ( key != null ) {
			return dictionaryMemberValue(name, combined, key);
		}
		if ( sf ) {
			return strictValue(name, combined);
		}
		return combined;
	}

	private static String combine(List<String> values) {
		final StringBuilder buf = new StringBuilder();
		for ( String value : values ) {
			if ( !buf.isEmpty() ) {
				buf.append(", ");
			}
			buf.append(normalize(value));
		}
		return buf.toString();
	}

	private static String normalize(String value) {
		return OBS_FOLD.matcher(value.strip()).replaceAll(" ");
	}

	private static String byteSequenceValue(List<String> values) {
		final List<ListElement<?>> items = new ArrayList<>(values.size());
		for ( String value : values ) {
			items.add(ByteSequenceItem.valueOf(normalize(value).getBytes(UTF_8)));
		}
		return SfList.of(items).serialize();
	}

	private String dictionaryMemberValue(String name, String value, String key) {
		final Dictionary dict;
		try {
			dict = Dictionary.parse(value);
		} catch ( ParseException e ) {
			throw new HttpSignatureException("The [" + name
					+ "] HTTP field could not be parsed as a Dictionary: " + e.getMessage(), e);
		}
		final ListElement<?> member = dict.get().get(key);
		if ( member == null ) {
			throw new HttpSignatureException("The [" + name + "] HTTP field does not have a ["
					+ key + "] member, which the signature covers.");
		}
		return member.serialize();
	}

	private String strictValue(String name, String value) {
		final StructuredType type = structuredTypes.get(name);
		if ( type == null ) {
			throw new HttpSignatureException("The [" + name
					+ "] HTTP field is covered with the 'sf' parameter, but its structured field"
					+ " type is not known.");
		}
		try {
			return switch (type) {
				case Dictionary -> Dictionary.parse(value).serialize();
				case List -> SfList.parse(value).serialize();
				case Item -> Item.parse(value).serialize();
			};
		} catch ( ParseException e ) {
			throw new HttpSignatureException("The [" + name + "] HTTP field could not be parsed as a "
					+ type + ": " + e.getMessage(), e);
		}
	}

}

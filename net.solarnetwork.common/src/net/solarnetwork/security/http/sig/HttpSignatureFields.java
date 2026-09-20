/* ==================================================================
 * HttpSignatureFields.java - 19/09/2026 10:09:55 am
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

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.greenbytes.http.sfv.ByteSequenceItem;
import org.greenbytes.http.sfv.Dictionary;
import org.greenbytes.http.sfv.InnerList;
import org.greenbytes.http.sfv.Item;
import org.greenbytes.http.sfv.ListElement;
import org.greenbytes.http.sfv.ParseException;
import org.greenbytes.http.sfv.SfDataType;

/**
 * The {@code Signature-Input} and {@code Signature} HTTP fields, per RFC 9421
 * section 4.
 *
 * @author matt
 * @version 1.0
 */
public final class HttpSignatureFields {

	/** The {@code Signature-Input} HTTP field name. */
	public static final String SIGNATURE_INPUT_HEADER = "Signature-Input";

	/** The {@code Signature} HTTP field name. */
	public static final String SIGNATURE_HEADER = "Signature";

	/** The {@code Accept-Signature} HTTP field name. */
	public static final String ACCEPT_SIGNATURE_HEADER = "Accept-Signature";

	private HttpSignatureFields() {
		// not available
	}

	/**
	 * Parse a {@code Signature-Input} field value.
	 *
	 * @param values
	 *        the field values, one per instance of the field in the message
	 * @return the signature parameters, by signature label, in field order
	 * @throws HttpSignatureException
	 *         if the field cannot be parsed
	 */
	public static Map<String, SignatureParameters> parseSignatureInput(List<String> values) {
		final Dictionary dict = parseDictionary(SIGNATURE_INPUT_HEADER, values);
		final Map<String, SignatureParameters> result = new LinkedHashMap<>(dict.get().size());
		for ( Map.Entry<String, ListElement<?>> e : dict.get().entrySet() ) {
			final ListElement<?> member = e.getValue();
			if ( !(member instanceof InnerList innerList) ) {
				throw new HttpSignatureException("The " + SIGNATURE_INPUT_HEADER + " [" + e.getKey()
						+ "] member must be an Inner List, but is a " + member.getType() + ".");
			}
			result.put(e.getKey(), SignatureParameters.forInnerList(innerList));
		}
		return result;
	}

	/**
	 * Parse a {@code Signature} field value.
	 *
	 * @param values
	 *        the field values, one per instance of the field in the message
	 * @return the signature values, by signature label, in field order
	 * @throws HttpSignatureException
	 *         if the field cannot be parsed
	 */
	public static Map<String, byte[]> parseSignature(List<String> values) {
		final Dictionary dict = parseDictionary(SIGNATURE_HEADER, values);
		final Map<String, byte[]> result = new LinkedHashMap<>(dict.get().size());
		for ( Map.Entry<String, ListElement<?>> e : dict.get().entrySet() ) {
			final ListElement<?> member = e.getValue();
			if ( !(member instanceof Item<?> item) || item.getType() != SfDataType.BYTESEQUENCE ) {
				throw new HttpSignatureException("The " + SIGNATURE_HEADER + " [" + e.getKey()
						+ "] member must be a Byte Sequence, but is a " + member.getType() + ".");
			}
			final ByteBuffer buf = item.byteBufferValue();
			final byte[] sig = new byte[buf.remaining()];
			buf.duplicate().get(sig);
			result.put(e.getKey(), sig);
		}
		return result;
	}

	private static Dictionary parseDictionary(String fieldName, List<String> values) {
		if ( values.isEmpty() ) {
			throw new HttpSignatureException("The " + fieldName + " HTTP field is missing.");
		}
		try {
			return Dictionary.parse(values);
		} catch ( ParseException e ) {
			throw new HttpSignatureException(
					"The " + fieldName + " HTTP field could not be parsed: " + e.getMessage(), e);
		}
	}

	/**
	 * Create a {@code Signature-Input} field value for a single signature.
	 *
	 * @param label
	 *        the signature label
	 * @param parameters
	 *        the signature parameters
	 * @return the field value, never {@code null}
	 */
	public static String signatureInputValue(String label, SignatureParameters parameters) {
		return label + "=" + parameters.serialize();
	}

	/**
	 * Create a {@code Signature} field value for a single signature.
	 *
	 * @param label
	 *        the signature label
	 * @param signature
	 *        the signature bytes
	 * @return the field value, never {@code null}
	 */
	public static String signatureValue(String label, byte[] signature) {
		return label + "=" + ByteSequenceItem.valueOf(signature).serialize();
	}

}

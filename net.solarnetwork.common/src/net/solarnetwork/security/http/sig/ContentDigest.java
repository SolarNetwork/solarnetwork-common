/* ==================================================================
 * ContentDigest.java - 19/09/2026 10:37:26 am
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

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.greenbytes.http.sfv.ByteSequenceItem;
import org.greenbytes.http.sfv.Dictionary;
import org.greenbytes.http.sfv.Item;
import org.greenbytes.http.sfv.ListElement;
import org.greenbytes.http.sfv.ParseException;
import org.greenbytes.http.sfv.SfDataType;
import org.jspecify.annotations.Nullable;

/**
 * The {@code Content-Digest} HTTP field, per RFC 9530.
 *
 * <p>
 * RFC 9421 has no notion of message content: a signature covers the body only
 * by covering a field that digests it. This is the field to use for that, and
 * it supersedes the RFC 3230 {@code Digest} field the SNWS2 scheme accepts.
 * </p>
 *
 * @author matt
 * @version 1.0
 */
public final class ContentDigest {

	/** The {@code Content-Digest} HTTP field name. */
	public static final String CONTENT_DIGEST_HEADER = "Content-Digest";

	/** The {@code sha-256} algorithm key. */
	public static final String SHA_256 = "sha-256";

	/** The {@code sha-512} algorithm key. */
	public static final String SHA_512 = "sha-512";

	private ContentDigest() {
		// not available
	}

	/**
	 * Get the JCA digest algorithm name for a {@code Content-Digest} key.
	 *
	 * @param algorithmKey
	 *        the dictionary key, such as {@code sha-256}
	 * @return the JCA algorithm name, or {@code null} if not supported
	 */
	public static @Nullable String digestAlgorithm(String algorithmKey) {
		return switch (algorithmKey) {
			case SHA_256 -> "SHA-256";
			case SHA_512 -> "SHA-512";
			default -> null;
		};
	}

	/**
	 * Parse a {@code Content-Digest} field value.
	 *
	 * @param values
	 *        the field values, one per instance of the field in the message
	 * @return the digest values, by algorithm key, in field order
	 * @throws HttpSignatureException
	 *         if the field cannot be parsed
	 */
	public static Map<String, byte[]> parse(List<String> values) {
		if ( values.isEmpty() ) {
			throw new HttpSignatureException("The " + CONTENT_DIGEST_HEADER + " HTTP field is missing.");
		}
		final Dictionary dict;
		try {
			dict = Dictionary.parse(values);
		} catch ( ParseException e ) {
			throw new HttpSignatureException("The " + CONTENT_DIGEST_HEADER
					+ " HTTP field could not be parsed: " + e.getMessage(), e);
		}
		final Map<String, byte[]> result = new LinkedHashMap<>(dict.get().size());
		for ( Map.Entry<String, ListElement<?>> e : dict.get().entrySet() ) {
			final ListElement<?> member = e.getValue();
			if ( !(member instanceof Item<?> item) || item.getType() != SfDataType.BYTESEQUENCE ) {
				throw new HttpSignatureException("The " + CONTENT_DIGEST_HEADER + " [" + e.getKey()
						+ "] member must be a Byte Sequence, but is a " + member.getType() + ".");
			}
			final ByteBuffer buf = item.byteBufferValue();
			final byte[] digest = new byte[buf.remaining()];
			buf.duplicate().get(digest);
			result.put(e.getKey(), digest);
		}
		return result;
	}

	/**
	 * Create a {@code Content-Digest} field value.
	 *
	 * @param algorithmKey
	 *        the algorithm key, such as {@code sha-256}
	 * @param content
	 *        the message content to digest
	 * @return the field value, never {@code null}
	 * @throws HttpSignatureException
	 *         if the algorithm is not supported
	 */
	public static String fieldValue(String algorithmKey, byte[] content) {
		final String alg = digestAlgorithm(algorithmKey);
		if ( alg == null ) {
			throw new HttpSignatureException("The " + CONTENT_DIGEST_HEADER + " algorithm ["
					+ algorithmKey + "] is not supported.");
		}
		final MessageDigest digest;
		try {
			digest = MessageDigest.getInstance(alg);
		} catch ( NoSuchAlgorithmException e ) {
			throw new HttpSignatureException("The " + alg + " digest algorithm is not available.", e);
		}
		return algorithmKey + "=" + ByteSequenceItem.valueOf(digest.digest(content)).serialize();
	}

	/**
	 * Create a {@code Content-Digest} field value.
	 *
	 * @param algorithmKey
	 *        the algorithm key, such as {@code sha-256}
	 * @param content
	 *        the message content to digest; the stream will not be closed by
	 *        this method
	 * @return the field value, never {@code null}
	 * @throws HttpSignatureException
	 *         if the algorithm is not supported
	 */
	public static String fieldValue(String algorithmKey, InputStream content) {
		final String alg = digestAlgorithm(algorithmKey);
		if ( alg == null ) {
			throw new HttpSignatureException("The " + CONTENT_DIGEST_HEADER + " algorithm ["
					+ algorithmKey + "] is not supported.");
		}
		final MessageDigest digest;
		try {
			digest = MessageDigest.getInstance(alg);
			final byte[] buffer = new byte[4096];
			int bytesRead;
			while ( (bytesRead = content.read(buffer)) != -1 ) {
				digest.update(buffer, 0, bytesRead);
			}

		} catch ( NoSuchAlgorithmException e ) {
			throw new HttpSignatureException("The " + alg + " digest algorithm is not available.", e);
		} catch ( IOException e ) {
			throw new HttpSignatureException("Error digesting content input: " + e.getMessage(), e);
		}
		return algorithmKey + "=" + ByteSequenceItem.valueOf(digest.digest()).serialize();
	}

}

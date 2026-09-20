/* ==================================================================
 * TokenSecretKeyDeriverTests.java - 19/09/2026 3:28:14 pm
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

package net.solarnetwork.security.http.sig.test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;
import java.time.LocalDate;
import java.util.HexFormat;
import org.junit.Test;
import net.solarnetwork.security.http.sig.HttpSignatureException;
import net.solarnetwork.security.http.sig.TokenSecretKeyDeriver;

/**
 * Test cases for the {@link TokenSecretKeyDeriver} class.
 *
 * @author matt
 * @version 1.0
 */
public class TokenSecretKeyDeriverTests {

	private static final String TEST_TOKEN_ID = "abc123";
	private static final String TEST_SECRET = "s3cr3t";

	@Test
	public void keyId_tokenOnly() {
		// GIVEN
		// WHEN
		final TokenSecretKeyDeriver deriver = TokenSecretKeyDeriver.forKeyId(TEST_TOKEN_ID);

		// THEN
		// @formatter:off
		then(deriver.tokenId())
			.as("Token ID extracted")
			.isEqualTo(TEST_TOKEN_ID)
			;
		then(deriver.signingDate())
			.as("No signing date without a delimiter")
			.isNull()
			;
		then(deriver.isDerived())
			.as("The token secret is used directly")
			.isFalse()
			;
		then(deriver.hmacKey(TEST_SECRET))
			.as("The signing key is the UTF-8 token secret")
			.isEqualTo(TEST_SECRET.getBytes(UTF_8))
			;
		// @formatter:on
	}

	@Test
	public void keyId_withSigningDate() {
		// GIVEN
		// WHEN
		final TokenSecretKeyDeriver deriver = TokenSecretKeyDeriver
				.forKeyId(TEST_TOKEN_ID + ":20210420");

		// THEN
		// @formatter:off
		then(deriver.tokenId())
			.as("Token ID extracted from before the delimiter")
			.isEqualTo(TEST_TOKEN_ID)
			;
		then(deriver.signingDate())
			.as("Signing date parsed from after the delimiter")
			.isEqualTo(LocalDate.of(2021, 4, 20))
			;
		then(deriver.isDerived())
			.as("A signing key is derived")
			.isTrue()
			;
		then(deriver.keyId())
			.as("The key ID round-trips")
			.isEqualTo(TEST_TOKEN_ID + ":20210420")
			;
		// @formatter:on
	}

	@Test
	public void derivedKey_matchesJavaScriptImplementation() {
		// GIVEN
		// this value is computed by the solarnetwork-api-core HttpMessageSignatureBuilder,
		// and pins the two implementations of the SolarNetwork key derivation together;
		// nothing in RFC 9421 covers it, since the derivation is ours
		final String expected = "e6c372aac14814ef99f6e10e38d6a05bffad2665dc7de66b5f6c67ee9d33579f";
		final TokenSecretKeyDeriver deriver = TokenSecretKeyDeriver.forToken(TEST_TOKEN_ID,
				LocalDate.of(2021, 4, 20));

		// WHEN
		final byte[] key = deriver.hmacKey(TEST_SECRET);

		// THEN
		// @formatter:off
		then(HexFormat.of().formatHex(key))
			.as("Derived signing key matches the JavaScript implementation")
			.isEqualTo(expected)
			;
		// @formatter:on
	}

	@Test
	public void forToken_withoutDate() {
		// GIVEN
		// WHEN
		final TokenSecretKeyDeriver deriver = TokenSecretKeyDeriver.forToken(TEST_TOKEN_ID, null);

		// THEN
		// @formatter:off
		then(deriver.keyId())
			.as("The key ID is the token ID alone")
			.isEqualTo(TEST_TOKEN_ID)
			;
		// @formatter:on
	}

	@Test
	public void error_emptyKeyId() {
		// GIVEN
		// WHEN
		// THEN
		// @formatter:off
		thenThrownBy(() -> TokenSecretKeyDeriver.forKeyId(""))
			.as("An empty key ID is rejected")
			.isInstanceOf(HttpSignatureException.class)
			;
		// @formatter:on
	}

	@Test
	public void error_missingTokenId() {
		// GIVEN
		// WHEN
		// THEN
		// @formatter:off
		thenThrownBy(() -> TokenSecretKeyDeriver.forKeyId(":20210420"))
			.as("A key ID without a token ID is rejected")
			.isInstanceOf(HttpSignatureException.class)
			.hasMessageContaining("token ID")
			;
		// @formatter:on
	}

	@Test
	public void error_invalidSigningDate() {
		// GIVEN
		// WHEN
		// THEN
		// @formatter:off
		thenThrownBy(() -> TokenSecretKeyDeriver.forKeyId(TEST_TOKEN_ID + ":notadate"))
			.as("An unparsable signing date is rejected")
			.isInstanceOf(HttpSignatureException.class)
			.hasMessageContaining("YYYYMMDD")
			;
		// @formatter:on
	}

}

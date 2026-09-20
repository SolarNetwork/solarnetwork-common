/* ==================================================================
 * HttpSignatureSettings.java - 19/09/2026 12:31:19 pm
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

package net.solarnetwork.web.jakarta.security;

import java.util.EnumSet;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import net.solarnetwork.security.http.sig.HttpSignatureAlgorithm;

/**
 * Settings for the SolarNetwork RFC 9421 HTTP Message Signatures profile.
 *
 * @author matt
 * @version 1.0
 * @since 4.53
 */
public class HttpSignatureSettings {

	/** The {@code enabled} property default value. */
	public static final boolean DEFAULT_ENABLED = true;

	/** The {@code tag} property default value. */
	public static final String DEFAULT_TAG = "solarnetwork";

	/** The {@code requireTag} property default value. */
	public static final boolean DEFAULT_REQUIRE_TAG = false;

	/** The {@code allowDirectSecretKey} property default value. */
	public static final boolean DEFAULT_ALLOW_DIRECT_SECRET_KEY = true;

	/** The {@code allowDerivedSigningKey} property default value. */
	public static final boolean DEFAULT_ALLOW_DERIVED_SIGNING_KEY = true;

	/** The {@code derivedSigningKeyMaxDays} property default value. */
	public static final int DEFAULT_DERIVED_SIGNING_KEY_MAX_DAYS = 7;

	private boolean enabled = DEFAULT_ENABLED;
	private String tag = DEFAULT_TAG;
	private boolean requireTag = DEFAULT_REQUIRE_TAG;
	private Set<HttpSignatureAlgorithm> algorithms = EnumSet.allOf(HttpSignatureAlgorithm.class);
	private boolean allowDirectSecretKey = DEFAULT_ALLOW_DIRECT_SECRET_KEY;
	private boolean allowDerivedSigningKey = DEFAULT_ALLOW_DERIVED_SIGNING_KEY;
	private int derivedSigningKeyMaxDays = DEFAULT_DERIVED_SIGNING_KEY_MAX_DAYS;

	/**
	 * Constructor.
	 */
	public HttpSignatureSettings() {
		super();
	}

	/**
	 * Test if the scheme is enabled.
	 *
	 * @return {@code true} if RFC 9421 signatures are accepted
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Set if the scheme is enabled.
	 *
	 * @param enabled
	 *        {@code true} to accept RFC 9421 signatures
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Get the application tag.
	 *
	 * <p>
	 * When a request carries more than one signature, this is the {@code tag}
	 * signature parameter value that identifies the one meant for
	 * authentication.
	 * </p>
	 *
	 * @return the tag, never {@code null}
	 */
	public String getTag() {
		return tag;
	}

	/**
	 * Set the application tag.
	 *
	 * @param tag
	 *        the tag to set
	 */
	public void setTag(String tag) {
		this.tag = (tag != null ? tag : DEFAULT_TAG);
	}

	/**
	 * Test if a matching tag is required.
	 *
	 * @return {@code true} if a signature must carry the configured tag; when
	 *         {@code false}, a request carrying exactly one signature is
	 *         accepted without a tag
	 */
	public boolean isRequireTag() {
		return requireTag;
	}

	/**
	 * Set if a matching tag is required.
	 *
	 * @param requireTag
	 *        {@code true} to require the configured tag
	 */
	public void setRequireTag(boolean requireTag) {
		this.requireTag = requireTag;
	}

	/**
	 * Get the acceptable signature algorithms.
	 *
	 * @return the algorithms, never {@code null}
	 */
	public Set<HttpSignatureAlgorithm> getAlgorithms() {
		return algorithms;
	}

	/**
	 * Set the acceptable signature algorithms.
	 *
	 * @param algorithms
	 *        the algorithms to accept
	 */
	public void setAlgorithms(@Nullable Set<HttpSignatureAlgorithm> algorithms) {
		this.algorithms = (algorithms != null && !algorithms.isEmpty() ? algorithms
				: EnumSet.allOf(HttpSignatureAlgorithm.class));
	}

	/**
	 * Test if using the token secret directly as the signing key is allowed.
	 *
	 * @return {@code true} if a key ID of just a token ID is accepted
	 */
	public boolean isAllowDirectSecretKey() {
		return allowDirectSecretKey;
	}

	/**
	 * Set if using the token secret directly as the signing key is allowed.
	 *
	 * @param allowDirectSecretKey
	 *        {@code true} to accept a key ID of just a token ID
	 */
	public void setAllowDirectSecretKey(boolean allowDirectSecretKey) {
		this.allowDirectSecretKey = allowDirectSecretKey;
	}

	/**
	 * Test if a date-derived signing key is allowed.
	 *
	 * @return {@code true} if a key ID with a signing date is accepted
	 */
	public boolean isAllowDerivedSigningKey() {
		return allowDerivedSigningKey;
	}

	/**
	 * Set if a date-derived signing key is allowed.
	 *
	 * @param allowDerivedSigningKey
	 *        {@code true} to accept a key ID with a signing date
	 */
	public void setAllowDerivedSigningKey(boolean allowDerivedSigningKey) {
		this.allowDerivedSigningKey = allowDerivedSigningKey;
	}

	/**
	 * Get the maximum age of a derived signing key.
	 *
	 * @return the maximum number of days a derived signing key remains valid
	 */
	public int getDerivedSigningKeyMaxDays() {
		return derivedSigningKeyMaxDays;
	}

	/**
	 * Set the maximum age of a derived signing key.
	 *
	 * @param derivedSigningKeyMaxDays
	 *        the maximum number of days a derived signing key remains valid
	 */
	public void setDerivedSigningKeyMaxDays(int derivedSigningKeyMaxDays) {
		this.derivedSigningKeyMaxDays = derivedSigningKeyMaxDays;
	}

}

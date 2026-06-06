/* ==================================================================
 * DatumAuxiliaryRecord.java - 30/05/2026 7:45:05 am
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

package net.solarnetwork.domain.datum;

import java.time.Instant;
import org.jspecify.annotations.Nullable;
import net.solarnetwork.domain.Differentiable;

/**
 * Metadata about a specific datum.
 *
 * @author matt
 * @version 1.1
 * @since 4.38
 */
public interface DatumAuxiliaryRecord extends Differentiable<DatumAuxiliaryRecord> {

	/**
	 * Get the type of auxiliary datum this instance represents.
	 *
	 * @return the type
	 */
	DatumAuxiliaryType getType();

	/**
	 * Get the datum identifier.
	 *
	 * @return the identifier
	 */
	DatumIdentity datumIdent();

	/**
	 * Get the object datum kind.
	 *
	 * @return the kind
	 */
	default ObjectDatumKind getKind() {
		return datumIdent().getKind();
	}

	/**
	 * Get the object ID.
	 *
	 * @return the object ID
	 */
	default Long getObjectId() {
		return datumIdent().getObjectId();
	}

	/**
	 * Get the source ID.
	 *
	 * @return the source ID
	 */
	default String getSourceId() {
		return datumIdent().getSourceId();
	}

	/**
	 * Get the timestamp.
	 *
	 * @return the timestamp
	 */
	default Instant getTimestamp() {
		return datumIdent().getTimestamp();
	}

	/**
	 * Get a set of datum properties that represent a "final" values to assume
	 * the datum stream had before {@link #getTimestamp()}.
	 *
	 * @return the final sample values
	 */
	@Nullable
	DatumSamples getSamplesFinal();

	/**
	 * Get a set of datum properties that represent the "start" values to assume
	 * the datum stream has starting at {@link #getTimestamp()}.
	 *
	 * @return the start sample values
	 */
	@Nullable
	DatumSamples getSamplesStart();

	/**
	 * Get optional notes or comments about this auxiliary record.
	 *
	 * @return the notes
	 */
	@Nullable
	String getNotes();

	/**
	 * Get optional metadata about this auxiliary record.
	 *
	 * @return the metadata
	 */
	@Nullable
	GeneralDatumMetadata getMetadata();

}

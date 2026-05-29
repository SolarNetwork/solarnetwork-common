/* ==================================================================
 * BasicDatumAuxiliaryRecord.java - 30/05/2026 7:57:18 am
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

import static net.solarnetwork.util.ObjectUtils.requireNonNullArgument;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import net.solarnetwork.domain.datum.DatumId.DatumIdent;

/**
 * Basic implementation of {@link DatumAuxiliaryRecord}.
 *
 * @author matt
 * @version 1.1
 * @since 4.38
 */
public class BasicDatumAuxiliaryRecord
		implements DatumAuxiliaryRecord, Comparable<BasicDatumAuxiliaryRecord> {

	private final DatumAuxiliaryType type;
	private final DatumIdent datumIdent;
	private final @Nullable String notes;
	private final @Nullable DatumSamples samplesFinal;
	private final @Nullable DatumSamples samplesStart;
	private final @Nullable GeneralDatumMetadata metadata;

	/**
	 * Create a {@code Mark} record.
	 *
	 * @param datumIdent
	 *        the identity
	 * @param notes
	 *        the notes
	 * @param metadata
	 *        the metadata
	 * @return the new instance
	 * @throws IllegalArgumentException
	 *         if {@code type} or {@code datumIdent} is {@code null}
	 */
	public static BasicDatumAuxiliaryRecord createMark(DatumIdentity datumIdent, @Nullable String notes,
			GeneralDatumMetadata metadata) {
		return new BasicDatumAuxiliaryRecord(DatumAuxiliaryType.Mark, datumIdent, notes, null, null,
				metadata);
	}

	/**
	 * Create a {@code Reset} record.
	 *
	 * @param datumIdent
	 *        the identity
	 * @param notes
	 *        the notes
	 * @param samplesFinal
	 *        the final samples
	 * @param samplesStart
	 *        the start samples
	 * @param metadata
	 *        the metadata
	 * @return the new instance
	 * @throws IllegalArgumentException
	 *         if {@code type} or {@code datumIdent} is {@code null}
	 */
	public static BasicDatumAuxiliaryRecord createReset(DatumIdentity datumIdent, @Nullable String notes,
			DatumSamples samplesFinal, DatumSamples samplesStart,
			@Nullable GeneralDatumMetadata metadata) {
		return new BasicDatumAuxiliaryRecord(DatumAuxiliaryType.Reset, datumIdent, notes, samplesFinal,
				samplesStart, metadata);
	}

	/**
	 * Constructor.
	 *
	 * @param type
	 *        the type
	 * @param datumIdent
	 *        the identity; if this is a {@link DatumIdent} instance it will be
	 *        used as-is, otherwise a new {@code DatumIdent} instance will be
	 *        derived from this value
	 * @param notes
	 *        the notes
	 * @param samplesFinal
	 *        the final samples
	 * @param samplesStart
	 *        the start samples
	 * @param metadata
	 *        the metadata
	 * @throws IllegalArgumentException
	 *         if {@code type} or {@code datumIdent} is {@code null}
	 */
	public BasicDatumAuxiliaryRecord(DatumAuxiliaryType type, DatumIdentity datumIdent,
			@Nullable String notes, @Nullable DatumSamples samplesFinal,
			@Nullable DatumSamples samplesStart, @Nullable GeneralDatumMetadata metadata) {
		super();
		this.type = requireNonNullArgument(type, "type");

		var ident = requireNonNullArgument(datumIdent, "datumIdent");
		if ( ident instanceof DatumIdent di ) {
			this.datumIdent = di;
		} else {
			this.datumIdent = new DatumIdent(datumIdent.getKind(), datumIdent.getObjectId(),
					datumIdent.getSourceId(), datumIdent.getTimestamp());
		}

		this.notes = notes;
		this.samplesFinal = samplesFinal;
		this.samplesStart = samplesStart;
		this.metadata = metadata;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BasicDatumAuxiliaryRecord{");
		if ( type != null ) {
			builder.append("type=");
			builder.append(type);
			builder.append(", ");
		}
		if ( datumIdent != null ) {
			builder.append("datumIdent=");
			builder.append(datumIdent);
			builder.append(", ");
		}
		if ( notes != null ) {
			builder.append("notes=");
			builder.append(notes);
			builder.append(", ");
		}
		if ( samplesFinal != null ) {
			builder.append("samplesFinal=");
			builder.append(samplesFinal);
			builder.append(", ");
		}
		if ( samplesStart != null ) {
			builder.append("samplesStart=");
			builder.append(samplesStart);
			builder.append(", ");
		}
		if ( metadata != null ) {
			builder.append("metadata=");
			builder.append(metadata);
		}
		builder.append("}");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, datumIdent);
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( !(obj instanceof BasicDatumAuxiliaryRecord other) ) {
			return false;
		}
		return type == other.type && Objects.equals(datumIdent, other.datumIdent);
	}

	/**
	 * Compare auxiliary record instances.
	 *
	 * <p>
	 * This compares the {@code datumIdent} followed by the {@code type}.
	 * </p>
	 *
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(BasicDatumAuxiliaryRecord o) {
		if ( this == o ) {
			return 0;
		}
		if ( o == null ) {
			return -1;
		}
		int result = datumIdent.compareTo(o.datumIdent);
		if ( result != 0 ) {
			return result;
		}
		return type.compareTo(o.type);
	}

	@Override
	public final DatumAuxiliaryType getType() {
		return type;
	}

	@Override
	public final DatumIdent datumIdent() {
		return datumIdent;
	}

	@Override
	public final @Nullable String getNotes() {
		return notes;
	}

	@Override
	public final @Nullable DatumSamples getSamplesFinal() {
		return samplesFinal;
	}

	@Override
	public final @Nullable DatumSamples getSamplesStart() {
		return samplesStart;
	}

	@Override
	public final @Nullable GeneralDatumMetadata getMetadata() {
		return metadata;
	}

}

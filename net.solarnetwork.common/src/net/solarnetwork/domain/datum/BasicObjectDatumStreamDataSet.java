/* ==================================================================
 * BasicObjectDatumStreamDataSet.java - 30/04/2022 9:18:23 am
 * 
 * Copyright 2022 SolarNetwork.net Dev Team
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
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

/**
 * Basic implementation of {@link ObjectDatumStreamDataSet}.
 * 
 * @author matt
 * @version 1.0
 * @since 2.4
 */
public class BasicObjectDatumStreamDataSet implements ObjectDatumStreamDataSet {

	private final ObjectDatumStreamMetadataProvider provider;
	private final Iterable<StreamDatum> data;
	private final Long totalResultCount;
	private final Integer returnedResultCount;
	private final Integer startingOffset;

	/**
	 * Create a new data set instance out of a set of metadata.
	 * 
	 * @param metadatas
	 *        the metadata
	 * @param data
	 *        the data
	 * @return the new instance
	 */
	public static final BasicObjectDatumStreamDataSet dataSet(
			Iterable<ObjectDatumStreamMetadata> metadatas, Iterable<StreamDatum> data) {
		return dataSet(metadatas, data, null, null, null);
	}

	/**
	 * Create a new data set instance out of a set of metadata.
	 * 
	 * @param metadatas
	 *        the metadata
	 * @param data
	 *        the data
	 * @param totalResultCount
	 *        the total result count, or {@literal null} if not known
	 * @param startingOffset
	 *        the starting offset within the total result set, or
	 *        {@literal null} if not known
	 * @param returnedResultCount
	 *        the number of results available in {@code data} or {@literal null}
	 *        if not known
	 * @return the new instance
	 */
	public static final BasicObjectDatumStreamDataSet dataSet(
			Iterable<ObjectDatumStreamMetadata> metadatas, Iterable<StreamDatum> data,
			Long totalResultCount, Integer startingOffset, Integer returnedResultCount) {
		return new BasicObjectDatumStreamDataSet(
				ObjectDatumStreamMetadataProvider.staticProvider(metadatas), data, totalResultCount,
				startingOffset, returnedResultCount);
	}

	/**
	 * Constructor.
	 * 
	 * @param provider
	 *        the provider to delegate to
	 * @param data
	 *        the data
	 * @throws IllegalArgumentException
	 *         if any argument is {@literal null}
	 */
	public BasicObjectDatumStreamDataSet(ObjectDatumStreamMetadataProvider provider,
			Iterable<StreamDatum> data) {
		this(provider, data, null, null, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param provider
	 *        the provider to delegate to
	 * @param data
	 *        the data
	 * @param totalResultCount
	 *        the total result count, or {@literal null} if not known
	 * @param startingOffset
	 *        the starting offset within the total result set, or
	 *        {@literal null} if not known
	 * @param returnedResultCount
	 *        the number of results available in {@code data} or {@literal null}
	 *        if not known
	 * @throws IllegalArgumentException
	 *         if either {@code provider} or {@code data} is {@literal null}
	 */
	public BasicObjectDatumStreamDataSet(ObjectDatumStreamMetadataProvider provider,
			Iterable<StreamDatum> data, Long totalResultCount, Integer startingOffset,
			Integer returnedResultCount) {
		super();
		this.provider = requireNonNullArgument(provider, "provider");
		this.data = requireNonNullArgument(data, "data");
		this.totalResultCount = totalResultCount;
		this.startingOffset = startingOffset;
		this.returnedResultCount = returnedResultCount;
	}

	@Override
	public Collection<UUID> metadataStreamIds() {
		return provider.metadataStreamIds();
	}

	@Override
	public ObjectDatumStreamMetadata metadataForStreamId(UUID streamId) {
		return provider.metadataForStreamId(streamId);
	}

	@Override
	public ObjectDatumStreamMetadata metadataForObjectSource(Long objectId, String sourceId) {
		return provider.metadataForObjectSource(objectId, sourceId);
	}

	@Override
	public Iterator<StreamDatum> iterator() {
		return data.iterator();
	}

	@Override
	public Iterable<StreamDatum> getResults() {
		return data;
	}

	@Override
	public Long getTotalResultCount() {
		return totalResultCount;
	}

	@Override
	public Integer getStartingOffset() {
		return startingOffset;
	}

	@Override
	public Integer getReturnedResultCount() {
		return returnedResultCount;
	}

}

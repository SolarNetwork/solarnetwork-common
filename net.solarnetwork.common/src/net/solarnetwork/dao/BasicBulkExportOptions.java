/* ==================================================================
 * BasicBulkExportOptions.java - 3/12/2020 9:42:31 am
 *
 * Copyright 2020 SolarNetwork.net Dev Team
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

package net.solarnetwork.dao;

import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * Basic implementation of {@link BulkExportingDao.ExportOptions}.
 *
 * @author matt
 * @version 1.0
 * @since 1.67
 */
public class BasicBulkExportOptions implements BulkExportingDao.ExportOptions {

	private final @Nullable String name;
	private final @Nullable Integer batchSize;
	private final @Nullable Map<String, Object> parameters;

	/**
	 * Constructor.
	 *
	 * @param name
	 *        the name
	 * @param parameters
	 *        export parameters
	 */
	public BasicBulkExportOptions(@Nullable String name, @Nullable Map<String, Object> parameters) {
		this(name, null, parameters);
	}

	/**
	 * Constructor.
	 *
	 * @param name
	 *        the name
	 * @param batchSize
	 *        the batch size hint
	 * @param parameters
	 *        export parameters
	 */
	public BasicBulkExportOptions(@Nullable String name, @Nullable Integer batchSize,
			@Nullable Map<String, Object> parameters) {
		super();
		this.name = name;
		this.batchSize = batchSize;
		this.parameters = parameters;
	}

	@Override
	public @Nullable String getName() {
		return name;
	}

	@Override
	public @Nullable Integer getBatchSize() {
		return batchSize;
	}

	@Override
	public @Nullable Map<String, Object> getParameters() {
		return parameters;
	}

}

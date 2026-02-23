/* ==================================================================
 * SimpleBulkLoadingOptions.java - 2/12/2020 5:19:25 pm
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

import static net.solarnetwork.util.ObjectUtils.requireNonNullArgument;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import net.solarnetwork.dao.BulkLoadingDao.LoadingOptions;
import net.solarnetwork.dao.BulkLoadingDao.LoadingTransactionMode;

/**
 * Basic immutable implementation of {@link LoadingOptions}.
 *
 * @author matt
 * @version 1.0
 * @since 1.67
 */
public class BasicBulkLoadingOptions implements LoadingOptions {

	private final @Nullable String name;
	private final @Nullable Integer batchSize;
	private final LoadingTransactionMode transactionMode;
	private final @Nullable Map<String, ?> parameters;

	/**
	 * Constructor.
	 *
	 * @param name
	 *        the name
	 * @param batchSize
	 *        the batch size hint
	 * @param transactionMode
	 *        the transaction mode
	 * @param parameters
	 *        the parameters
	 * @throws IllegalArgumentException
	 *         if {@code transactionMode} is {@code null}
	 */
	public BasicBulkLoadingOptions(@Nullable String name, @Nullable Integer batchSize,
			LoadingTransactionMode transactionMode, @Nullable Map<String, ?> parameters) {
		super();
		this.name = name;
		this.batchSize = batchSize;
		this.transactionMode = requireNonNullArgument(transactionMode, "transactionMode");
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
	public LoadingTransactionMode getTransactionMode() {
		return transactionMode;
	}

	@Override
	public @Nullable Map<String, ?> getParameters() {
		return parameters;
	}

}

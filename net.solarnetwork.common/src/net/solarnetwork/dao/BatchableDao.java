/* ===================================================================
 * BatchableDao.java
 * 
 * Created Nov 30, 2009 4:56:25 PM
 * 
 * Copyright 2007-2009 SolarNetwork.net Dev Team
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
 * ===================================================================
 */

package net.solarnetwork.dao;

import java.util.Map;

/**
 * An API for batch processing domain objects.
 * 
 * @param <T>
 *        the domain object type
 * @author matt
 * @version 1.0
 * @since 1.74
 */
public interface BatchableDao<T> {

	/**
	 * Batch processing options.
	 */
	interface BatchOptions {

		/**
		 * Get a unique name for this batch operation.
		 * 
		 * @return a name
		 */
		String getName();

		/**
		 * Get a batch size hint.
		 * 
		 * @return a batch size
		 */
		int getBatchSize();

		/**
		 * If {@literal true} the batch should be updatable.
		 * 
		 * @return boolean
		 */
		boolean isUpdatable();

		/**
		 * Get optional additional parameters, implementation specific.
		 * 
		 * @return parameters
		 */
		Map<String, Object> getParameters();

	}

	/**
	 * Handler for batch processing.
	 * 
	 * @param <T>
	 *        the domain object type
	 */
	interface BatchCallback<T> {

		/**
		 * Handle a single domain instance batch operation.
		 * 
		 * @param domainObject
		 *        the domain object
		 * @return the operation results
		 */
		BatchCallbackResult handle(T domainObject);
	}

	/**
	 * The result for a single batch operation.
	 */
	enum BatchCallbackResult {

		/** Continue processing. */
		CONTINUE,

		/** The domain object was updated. */
		UPDATE,

		/** The domain object should be deleted. */
		DELETE,

		/** We should stop processing immediately. */
		STOP,

		/** Stop after updating the domain object. */
		UPDATE_STOP,
	}

	/**
	 * The result of the entire batch processing.
	 */
	interface BatchResult {

		/**
		 * Return the number of domain objects processed.
		 * 
		 * @return the number of objects processed
		 */
		int numProcessed();

	}

	/**
	 * Process a set of domain objects in batch.
	 * 
	 * @param callback
	 *        the batch callback handler
	 * @param options
	 *        the batch processing options
	 * @return the batch results
	 */
	BatchResult batchProcess(BatchCallback<T> callback, BatchOptions options);
}

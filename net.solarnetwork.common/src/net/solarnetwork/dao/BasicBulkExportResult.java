/* ==================================================================
 * BasicBulkExportResult.java - 3/12/2020 10:26:12 am
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

import net.solarnetwork.dao.BulkExportingDao.ExportResult;

/**
 * Basic implementation of {@link ExportResult}.
 * 
 * @author matt
 * @version 1.0
 * @since 1.67
 */
public class BasicBulkExportResult implements BulkExportingDao.ExportResult {

	private final long numProcessed;

	/**
	 * Constructor.
	 * 
	 * @param numProcessed
	 *        the number of processed items
	 */
	public BasicBulkExportResult(long numProcessed) {
		super();
		this.numProcessed = numProcessed;
	}

	@Override
	public long getNumProcessed() {
		return numProcessed;
	}

}

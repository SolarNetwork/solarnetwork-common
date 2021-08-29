/* ==================================================================
 * PurgePostedChargeSessionsTask.java - 16/02/2020 8:08:25 am
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

package net.solarnetwork.ocpp.dao;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.solarnetwork.service.OptionalService;

/**
 * Job to delete old charge sessions that have been uploaded.
 * 
 * @author matt
 * @version 2.0
 */
public class PurgePostedChargeSessionsTask implements Callable<Integer>, Runnable {

	/** The default {@code expirationHours} property value. */
	public static final int DEFAULT_EXPIRATION_HOURS = 4;

	private OptionalService<ChargeSessionDao> chargeSessionDao;
	private int expirationHours = DEFAULT_EXPIRATION_HOURS;

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Override
	public void run() {
		try {
			call();
		} catch ( RuntimeException e ) {
			throw e;
		} catch ( Exception e ) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Integer call() throws Exception {
		ChargeSessionDao dao = (chargeSessionDao != null ? chargeSessionDao.service() : null);
		if ( chargeSessionDao == null ) {
			log.debug("No ChargeSessionDao available, cannot purge posted charge sessions.");
			return 0;
		}
		Instant ts = Instant.now().minus(expirationHours, ChronoUnit.HOURS);
		log.debug(
				"Looking for OCPP posted charge sessions older than {} hours to purge (older that {})...",
				expirationHours, ts);
		int result = dao.deletePostedChargeSessions(ts);
		log.info("Purged {} posted OCPP charge sessions more than {} hours old.", result,
				expirationHours);
		return result;
	}

	/**
	 * Set the charge session DAO to use.
	 * 
	 * @param chargeSessionDao
	 *        the DAO to use
	 */
	public void setChargeSessionDao(OptionalService<ChargeSessionDao> chargeSessionDao) {
		this.chargeSessionDao = chargeSessionDao;
	}

	/**
	 * Get the posted charge session expiration hours.
	 * 
	 * @return the expiration hours
	 */
	public int getExpirationHours() {
		return expirationHours;
	}

	/**
	 * Set the posted charge session expiration hours.
	 * 
	 * @param expirationHours
	 *        the minimum number of hours the {@code posted} date a session must
	 *        have before qualifying for being purged
	 */
	public void setExpirationHours(int expirationHours) {
		this.expirationHours = expirationHours;
	}

}

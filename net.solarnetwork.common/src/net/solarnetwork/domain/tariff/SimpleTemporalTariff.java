/* ==================================================================
 * SimpleTemporalTariff.java - 12/05/2021 10:39:53 AM
 *
 * Copyright 2021 SolarNetwork.net Dev Team
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

package net.solarnetwork.domain.tariff;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

/**
 * A simple implementation of {@link TemporalTariff} that delegates to another
 * {@link Tariff}.
 *
 * @author matt
 * @version 1.1
 * @since 1.71
 */
public class SimpleTemporalTariff implements TemporalTariff {

	private final LocalDate date;
	private final LocalTime time;
	private final Tariff delegate;

	/**
	 * Constructor.
	 *
	 * @param dateTime
	 *        a date time
	 * @param delegate
	 *        the tariff
	 */
	public SimpleTemporalTariff(LocalDateTime dateTime, Tariff delegate) {
		super();
		this.date = (dateTime != null ? dateTime.toLocalDate() : null);
		this.time = (dateTime != null ? dateTime.toLocalTime() : null);
		this.delegate = delegate;
	}

	@Override
	public Map<String, Rate> getRates() {
		return delegate.getRates();
	}

	@Override
	public LocalDate getDate() {
		return date;
	}

	@Override
	public LocalTime getTime() {
		return time;
	}

	@Override
	public <T extends Tariff> T unwrap(Class<T> tariffType) {
		T result = TemporalTariff.super.unwrap(tariffType);
		if ( result == null ) {
			result = delegate.unwrap(tariffType);
		}
		return result;
	}

}

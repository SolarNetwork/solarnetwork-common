/* ==================================================================
 * SimpleTemporalTariffSchedule.java - 12/05/2021 9:30:30 AM
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

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * A simple time-based tariff schedule based on a list of time-based rules.
 * 
 * @author matt
 * @version 1.0
 * @since 1.71
 */
public class SimpleTemporalTariffSchedule implements TariffSchedule {

	/** The {@code evaluator} default value. */
	public static final TemporalRangesTariffEvaluator DEFAULT_EVALUATOR = new SimpleTemporalRangesTariffEvaluator();

	/** The {@code firstMatchOnly} default value. */
	public static final boolean DEFAULT_FIRST_MATCH_ONLY = true;

	static final ChronoField[] FIELDS = new ChronoField[] { ChronoField.MONTH_OF_YEAR,
			ChronoField.DAY_OF_MONTH, ChronoField.DAY_OF_WEEK, ChronoField.MINUTE_OF_DAY };

	private final List<TemporalRangesTariff> rules;
	private final TemporalRangesTariffEvaluator evaluator;
	private boolean firstMatchOnly = DEFAULT_FIRST_MATCH_ONLY;

	/**
	 * Constructor.
	 * 
	 * @param rules
	 *        the schedule rules
	 */
	public SimpleTemporalTariffSchedule(Iterable<TemporalRangesTariff> rules) {
		this(rules, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param rules
	 *        the schedule rules
	 * @param evaluator
	 *        the evaluator
	 */
	public SimpleTemporalTariffSchedule(Iterable<TemporalRangesTariff> rules,
			TemporalRangesTariffEvaluator evaluator) {
		super();
		if ( rules == null ) {
			throw new IllegalArgumentException("The rules argument must not be null.");
		}
		this.rules = StreamSupport.stream(rules.spliterator(), false).collect(Collectors.toList());
		this.evaluator = (evaluator != null ? evaluator : DEFAULT_EVALUATOR);
	}

	@Override
	public Tariff resolveTariff(LocalDateTime dateTime, Map<String, ?> parameters) {
		final boolean firstOnly = isFirstMatchOnly();
		List<Tariff> matches = (firstOnly ? null : new ArrayList<>(rules.size()));
		for ( TemporalRangesTariff rule : rules ) {
			if ( rule.applies(evaluator, dateTime, parameters) ) {
				TemporalTariff t = rule.toTemporalTariff(dateTime);
				if ( firstOnly ) {
					return t;
				}
				matches.add(t);
			}
		}
		if ( matches == null || matches.isEmpty() ) {
			return null;
		}
		return (matches.size() == 1 ? matches.get(0)
				: new CompositeTariff(matches).toTemporalTariff(dateTime));
	}

	/**
	 * Get the first-match-only flag.
	 * 
	 * @return {@literal true} if only the first tariff rule that matches should
	 *         be returned, {@literal false} to return a composite rule of all
	 *         matches; defaults to {@link #DEFAULT_FIRST_MATCH_ONLY}
	 */
	public boolean isFirstMatchOnly() {
		return firstMatchOnly;
	}

	/**
	 * Set the first-match-only flag.
	 * 
	 * @param firstMatchOnly
	 *        {@literal true} if only the first tariff rule that matches should
	 *        be returned
	 */
	public void setFirstMatchOnly(boolean firstMatchOnly) {
		this.firstMatchOnly = firstMatchOnly;
	}

}

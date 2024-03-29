/* ==================================================================
 * BasicEvaluationConfiguration.java - 6/02/2019 7:13:13 am
 * 
 * Copyright 2019 SolarNetwork.net Dev Team
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

package net.solarnetwork.service.support;

import java.util.Map;
import net.solarnetwork.service.ExpressionService.EvaluationConfiguration;

/**
 * Basic implementation of {@link EvaluationConfiguration}.
 * 
 * @author matt
 * @version 2.0
 * @since 1.49
 */
public class BasicEvaluationConfiguration implements EvaluationConfiguration {

	private final Map<String, Object> options;

	/**
	 * Constructor.
	 * 
	 * @param options
	 *        the options
	 */
	public BasicEvaluationConfiguration(Map<String, Object> options) {
		super();
		this.options = options;
	}

	@Override
	public Map<String, Object> getOptions() {
		return options;
	}

}

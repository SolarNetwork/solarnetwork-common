/* ==================================================================
 * RestrictedEvaluationContext.java - 6/02/2019 7:15:48 am
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

package net.solarnetwork.common.expr.spel;

import static java.util.Collections.emptyList;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * {@link org.springframework.expression.EvaluationContext} that restricts some
 * runtime features.
 *
 * @author matt
 * @version 1.1
 */
public class RestrictedEvaluationContext extends StandardEvaluationContext {

	/**
	 * Default constructor.
	 */
	public RestrictedEvaluationContext() {
		super();

		// prevent construction of objects
		setConstructorResolvers(emptyList());
	}

	/**
	 * Constructor.
	 *
	 * @param rootObject
	 *        the optional root object
	 */
	public RestrictedEvaluationContext(Object rootObject) {
		super(rootObject);

		// prevent construction of objects
		setConstructorResolvers(emptyList());
	}

}

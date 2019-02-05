/* ==================================================================
 * ExpressionService.java - 5/02/2019 4:11:08 pm
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

package net.solarnetwork.support;

import java.util.Map;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionException;
import net.solarnetwork.domain.Identifiable;

/**
 * API for a service that uses the {@link Expression} API for evaluating dynamic
 * expressions.
 * 
 * <p>
 * This API extends {@link Identifiable}; each implementation must define their
 * own unique identifier so they can be differentiated at runtime.
 * </p>
 * 
 * @author matt
 * @version 1.0
 * @since 1.9
 */
public interface ExpressionService extends Identifiable {

	/**
	 * Get a unique identifier for the language this expression service
	 * supports.
	 * 
	 * <p>
	 * This identifier is meant to be stored alongside an expressions managed by
	 * users, so they might be able to choose from all languages supported by
	 * the system.
	 * </p>
	 * 
	 * @return a unique identifier
	 */
	@Override
	String getUid();

	/**
	 * Create a reusable evaluation context.
	 * 
	 * <p>
	 * Creating a context can be an expensive operation, so this method can be
	 * called to create a thread-safe and reusable context instance.
	 * </p>
	 * 
	 * @param root
	 *        an optional root object to use during expression evaluation
	 * @return the newly created context
	 */
	EvaluationContext createEvaluationContext(Object root);

	/**
	 * Evaluate an expression.
	 * 
	 * @param expression
	 *        the expression to evaluate
	 * @param variables
	 *        optional variables to pass into the evaluation
	 * @param root
	 *        optional "root" object to set for the evaluation
	 * @param context
	 *        a context, such as one returned from
	 *        {@link #createEvaluationContext(Object)}, or {@literal null} to
	 *        create a new context
	 * @param resultClass
	 *        the expected result object type
	 * @return the expression result
	 * @throws ExpressionException
	 *         if any error occurs
	 */
	<T> T evaluateExpression(String expression, Map<String, Object> variables, Object root,
			EvaluationContext context, Class<T> resultClass);

}

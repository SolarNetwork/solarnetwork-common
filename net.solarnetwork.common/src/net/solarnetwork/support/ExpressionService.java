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
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
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
 * @since 1.49
 */
public interface ExpressionService extends Identifiable {

	/**
	 * API for configuration of the expression runtime.
	 * 
	 * @version 1.0
	 */
	interface EvaluationConfiguration {

		/**
		 * Get custom implementation-specific options.
		 * 
		 * @return the options
		 */
		Map<String, Object> getOptions();
	}

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
	 * @param configuration
	 *        optional configuration to apply to the context
	 * @param root
	 *        an optional root object to use during expression evaluation
	 * @return the newly created context
	 */
	EvaluationContext createEvaluationContext(EvaluationConfiguration configuration, Object root);

	/**
	 * Parse an expression.
	 * 
	 * @param expression
	 *        the expression to parse
	 * @return the parsed expression
	 */
	Expression parseExpression(String expression);

	/**
	 * Evaluate an expression.
	 * 
	 * <p>
	 * Calling this method, instead of
	 * {@link #evaluateExpression(String, Map, Object, EvaluationContext, Class)},
	 * can result in faster evaluation times if the same {@code expression} is
	 * passed multiple times. If an {@code expression} is only used once, then
	 * calling
	 * {@link #evaluateExpression(String, Map, Object, EvaluationContext, Class)}
	 * can be more convenient.
	 * </p>
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
	 * @throws EvaluationException
	 *         if any error occurs
	 */
	<T> T evaluateExpression(Expression expression, Map<String, Object> variables, Object root,
			EvaluationContext context, Class<T> resultClass);

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
	 * @throws EvaluationException
	 *         if any error occurs
	 */
	<T> T evaluateExpression(String expression, Map<String, Object> variables, Object root,
			EvaluationContext context, Class<T> resultClass);

}

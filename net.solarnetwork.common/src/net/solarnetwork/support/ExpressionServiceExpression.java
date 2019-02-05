/* ==================================================================
 * ExpressionServiceExpression.java - 6/02/2019 8:22:25 am
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

import org.springframework.expression.Expression;

/**
 * An {@link Expression} paired with the {@link ExpressionService} it can be
 * used with.
 * 
 * @author matt
 * @version 1.0
 * @since 1.49
 */
public class ExpressionServiceExpression {

	private final ExpressionService service;
	private final Expression expression;

	/**
	 * Constructor.
	 * 
	 * @param service
	 *        the service
	 * @param expression
	 *        the expression
	 */
	public ExpressionServiceExpression(ExpressionService service, Expression expression) {
		super();
		if ( service == null || expression == null ) {
			throw new IllegalArgumentException("An ExpressionService and Expression are both required");
		}
		this.service = service;
		this.expression = expression;
	}

	public ExpressionService getService() {
		return service;
	}

	public Expression getExpression() {
		return expression;
	}

}

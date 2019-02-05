/* ==================================================================
 * SpelExpressionService.java - 5/02/2019 3:22:32 pm
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

import java.util.Map;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import net.solarnetwork.support.ExpressionService;

/**
 * Spring Expression Language implementation of {@link ExpressionService}.
 * 
 * @author matt
 * @version 1.0
 */
public class SpelExpressionService implements ExpressionService {

	private final ExpressionParser parser;
	private String groupUid;

	public SpelExpressionService() {
		this(new SpelParserConfiguration());
	}

	public SpelExpressionService(SpelParserConfiguration configuration) {
		this.parser = new SpelExpressionParser(configuration);
	}

	@Override
	public EvaluationContext createEvaluationContext(Object root) {
		return new StandardEvaluationContext(root);
	}

	@Override
	public <T> T evaluateExpression(String expression, Map<String, Object> variables, Object root,
			EvaluationContext context, Class<T> resultClass) {
		if ( context == null ) {
			context = createEvaluationContext(root);
		}

		if ( variables != null && !variables.isEmpty() ) {
			for ( Map.Entry<String, Object> me : variables.entrySet() ) {
				context.setVariable(me.getKey(), me.getValue());
			}
		}

		Expression expr = parser.parseExpression(expression);
		return expr.getValue(context, root, resultClass);
	}

	@Override
	public String getDisplayName() {
		return "Spel";
	}

	@Override
	public String getUid() {
		return this.getClass().getName();
	}

	@Override
	public String getGroupUid() {
		return groupUid;
	}

	public void setGroupUid(String groupUid) {
		this.groupUid = groupUid;
	}

}

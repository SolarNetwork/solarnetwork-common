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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionException;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.ReflectivePropertyAccessor;
import net.solarnetwork.service.ExpressionService;

/**
 * Spring Expression Language implementation of {@link ExpressionService}.
 * 
 * @author matt
 * @version 1.1
 */
public class SpelExpressionService implements ExpressionService {

	/** The default value for the {@code languageReferenceLink} property. */
	public static final URI DEFAULT_LANG_REF_LINK = defaultLangRefLink();

	private final ExpressionParser parser;
	private String groupUid;
	private URI languageReferenceLink = DEFAULT_LANG_REF_LINK;

	private static final URI defaultLangRefLink() {
		try {
			return new URI(
					"https://github.com/SolarNetwork/solarnetwork/wiki/Spring-Expression-Language");
		} catch ( URISyntaxException e ) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Default constructor.
	 * 
	 * <p>
	 * The expression compiler mode can be set via the
	 * <code>spring.expression.compiler.mode</code> system property, set to one
	 * of the {@link SpelCompilerMode} values, e.g. <code>OFF</code>,
	 * <code>IMMEDIATE</code>, or <code>MIXED</code>.
	 * </p>
	 */
	public SpelExpressionService() {
		this(new SpelParserConfiguration(null, SpelExpressionService.class.getClassLoader()));
	}

	/**
	 * Constructor.
	 * 
	 * @param configuration
	 *        the configuration to use
	 */
	public SpelExpressionService(SpelParserConfiguration configuration) {
		this.parser = new SpelExpressionParser(configuration);
	}

	/**
	 * Create a reusable evaluation context.
	 * 
	 * <p>
	 * This creates a {@link RestrictedEvaluationContext}, with both
	 * {@link MapAccessor} and {@link ReflectivePropertyAccessor} property
	 * accessors.
	 * </p>
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public EvaluationContext createEvaluationContext(EvaluationConfiguration configuration,
			Object root) {
		RestrictedEvaluationContext ctx = new RestrictedEvaluationContext(root);
		ctx.addPropertyAccessor(new MapAccessor());
		ctx.addPropertyAccessor(new ReflectivePropertyAccessor());
		return ctx;
	}

	@Override
	public Expression parseExpression(String expression) {
		try {
			return parser.parseExpression(expression);
		} catch ( NullPointerException e ) {
			throw new ExpressionException(
					"NullPointerException parsing expression `" + expression + "`");
		} catch ( IllegalStateException e ) {
			throw new ExpressionException(
					"IllegalStateException parsing expression `" + expression + "`");
		}
	}

	private static final Pattern VAR_NAME_RESERVED = Pattern.compile("([^a-zA-Z_$])");

	private String safeVariableName(String varName) {
		Matcher m = VAR_NAME_RESERVED.matcher(varName);
		StringBuffer buf = new StringBuffer();
		while ( m.find() ) {
			char ch = m.group(1).charAt(0);
			m.appendReplacement(buf, Integer.toString(ch, 16));
		}
		m.appendTail(buf);
		return buf.toString();
	}

	@Override
	public <T> T evaluateExpression(Expression expression, Map<String, Object> variables, Object root,
			EvaluationContext context, Class<T> resultClass) {
		if ( context == null ) {
			context = createEvaluationContext(null, root);
		}

		if ( variables != null && !variables.isEmpty() ) {
			for ( Map.Entry<String, Object> me : variables.entrySet() ) {
				String varName = safeVariableName(me.getKey());
				context.setVariable(varName, me.getValue());
			}
		}

		return expression.getValue(context, root, resultClass);
	}

	@Override
	public <T> T evaluateExpression(String expression, Map<String, Object> variables, Object root,
			EvaluationContext context, Class<T> resultClass) {
		return evaluateExpression(parseExpression(expression), variables, root, context, resultClass);
	}

	@Override
	public URI getLanguageReferenceLink() {
		return languageReferenceLink;
	}

	/**
	 * Set the language reference link.
	 * 
	 * @param languageReferenceLink
	 *        the link to set
	 */
	public void setLanguageReferenceLink(URI languageReferenceLink) {
		this.languageReferenceLink = languageReferenceLink;
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

	/**
	 * Set the group UID.
	 * 
	 * @param groupUid
	 *        the group UID to set
	 */
	public void setGroupUid(String groupUid) {
		this.groupUid = groupUid;
	}

}

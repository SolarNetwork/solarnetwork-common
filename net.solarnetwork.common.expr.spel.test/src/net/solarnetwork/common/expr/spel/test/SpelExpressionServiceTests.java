/* ==================================================================
 * SpelExpressionServiceTests.java - 5/02/2019 3:55:35 pm
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

package net.solarnetwork.common.expr.spel.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.fail;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelMessage;
import net.solarnetwork.common.expr.spel.SpelExpressionService;

/**
 * Test cases for the {@link SpelExpressionService} class.
 *
 * @author matt
 * @version 1.0
 */
public class SpelExpressionServiceTests {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private SpelExpressionService service;

	@Before
	public void setup() {
		service = new SpelExpressionService();
	}

	@Test
	public void bigDecimalMathWithVariables() {
		Map<String, Object> vars = new HashMap<>(2);
		vars.put("a", new BigDecimal("1.1"));
		vars.put("b", new BigDecimal("2.2"));
		BigDecimal result = service.evaluateExpression("#a * #b", vars, null, null, BigDecimal.class);
		assertThat("Decimal result", result, equalTo(new BigDecimal("2.42")));
	}

	@Test
	public void bigDecimalMathWithRootMap() {
		Map<String, Object> root = new HashMap<>(2);
		root.put("a", new BigDecimal("1.1"));
		root.put("b", new BigDecimal("2.2"));
		BigDecimal result = service.evaluateExpression("#root['a'] * #root['b']", null, root, null,
				BigDecimal.class);
		assertThat("Decimal result", result, equalTo(new BigDecimal("2.42")));
	}

	@Test
	public void bigDecimalAndBigIntegerMath() {
		Map<String, Object> vars = new HashMap<>(2);
		vars.put("a", new BigInteger("3"));
		vars.put("b", new BigDecimal("2.2"));
		BigDecimal result = service.evaluateExpression("#a * #b", vars, null, null, BigDecimal.class);
		assertThat("Decimal result", result, equalTo(new BigDecimal("6.6")));
	}

	@Test
	public void repeatCallsWithRootMap() {
		// this test is to confirm AST compilation functions as expected. The expression must
		// be "compilable", and BigDecimal math operations are NOT, so using doubles here
		Map<String, Object> root = new HashMap<>(2);
		root.put("a", 1.1);
		root.put("b", 2.2);
		List<Long> times = new ArrayList<>(10);
		EvaluationContext ctx = service.createEvaluationContext(null, null);
		Expression expression = service.parseExpression("#root['a'] * #root['b']");
		for ( int i = 0; i < 10; i++ ) {
			long start = System.currentTimeMillis();
			Double result = service.evaluateExpression(expression, null, root, ctx, Double.class);
			times.add(System.currentTimeMillis() - start);
			assertThat("Decimal result", result, Matchers.closeTo(2.42, 0.001));
		}
		log.debug("Evaluation times: {}", times);
	}

	@Test
	public void constructObjectRestricted() {
		try {
			service.evaluateExpression("new String('foobar')", null, null, null, String.class);
			fail("Exception expected");
		} catch ( SpelEvaluationException e ) {
			assertThat("Constructor not found exception", e.getMessageCode(),
					equalTo(SpelMessage.CONSTRUCTOR_NOT_FOUND));
		}
	}

	@Test
	public void classNotFound() {
		try {
			service.evaluateExpression("T(net.solarnetwork.pidfile.PidFileCreator).SETTING_PID_FILE",
					null, null, null, String.class);
			fail("Exception expected");
		} catch ( SpelEvaluationException e ) {
			assertThat("Type not found exception", e.getMessageCode(),
					equalTo(SpelMessage.TYPE_NOT_FOUND));
		}
	}

	@Test
	public void funkyVariableName() {
		Map<String, Object> vars = new HashMap<>(2);
		vars.put("a+", new BigInteger("3"));
		vars.put("b-", new BigDecimal("2.2"));
		BigDecimal result = service.evaluateExpression("#a2b * #b2d", vars, null, null,
				BigDecimal.class);
		assertThat("Decimal result", result, equalTo(new BigDecimal("6.6")));

	}

	@Test
	public void stringConcat() {
		Map<String, Object> vars = new HashMap<>(2);
		vars.put("a", "3");
		vars.put("b", "2.2");
		String result = service.evaluateExpression("#a + ' ' + #b", vars, null, null, String.class);
		assertThat("Decimal result", result, equalTo("3 2.2"));
	}

	@Test
	public void rootObject() {
		Map<String, Object> vars = new HashMap<>(2);
		vars.put("a", new BigInteger("3"));
		vars.put("b", new BigDecimal("2.2"));
		TestExpressionRoot root = new TestExpressionRoot(vars, 42);
		BigDecimal result = service.evaluateExpression("data['a'] + data['b'] * foo", null, root, null,
				BigDecimal.class);
		assertThat("Result", result, equalTo(new BigDecimal("95.4")));
	}

	@Test
	public void rootMapObject() {
		Map<String, Object> vars = new HashMap<>(2);
		vars.put("a", new BigInteger("3"));
		vars.put("b", new BigDecimal("2.2"));
		BigDecimal result = service.evaluateExpression("a * b", null, vars, null, BigDecimal.class);
		assertThat("Result", result, equalTo(new BigDecimal("6.6")));
	}

	@Test
	public void listLiteral() {
		Map<String, Object> vars = new HashMap<>(2);
		vars.put("a", new BigInteger("3"));
		vars.put("b", new BigDecimal("2.2"));
		@SuppressWarnings("unchecked")
		List<Number> result = service.evaluateExpression("{a,b}", null, vars, null, List.class);
		assertThat("Result", result, contains(new BigInteger("3"), new BigDecimal("2.2")));
	}

	@Test
	public void arrayLiteral() {
		Map<String, Object> vars = new HashMap<>(2);
		vars.put("a", new BigInteger("3"));
		vars.put("b", new BigDecimal("2.2"));
		Number[] result = service.evaluateExpression("new Number[]{a,b}", null, vars, null,
				Number[].class);
		assertThat("Result", result, arrayContaining(new BigInteger("3"), new BigDecimal("2.2")));
	}

	@Test
	public void reuseContext() {
		// GIVEN
		EvaluationContext ctx = service.createEvaluationContext(null, null);
		Map<String, Object> vars = new HashMap<>(2);
		vars.put("a", new BigInteger("3"));
		vars.put("b", new BigDecimal("2.2"));

		// WHEN
		BigDecimal result1 = service.evaluateExpression("data['a'] + data['b'] * foo", null,
				new TestExpressionRoot(vars, 42), ctx, BigDecimal.class);

		vars.put("a", new BigInteger("30"));
		BigDecimal result2 = service.evaluateExpression("data['a'] + data['b'] * foo", null,
				new TestExpressionRoot(vars, 420), ctx, BigDecimal.class);

		// THEN
		assertThat("Result 1", result1, equalTo(new BigDecimal("95.4")));

		assertThat("Result 2", result2, equalTo(new BigDecimal("954.0")));
	}

}

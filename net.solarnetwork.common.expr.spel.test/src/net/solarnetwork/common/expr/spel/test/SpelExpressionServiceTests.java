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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import net.solarnetwork.common.expr.spel.SpelExpressionService;

/**
 * Test cases for the {@link SpelExpressionService} class.
 * 
 * @author matt
 * @version 1.0
 */
public class SpelExpressionServiceTests {

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
}

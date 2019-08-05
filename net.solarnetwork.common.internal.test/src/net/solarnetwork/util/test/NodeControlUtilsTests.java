/* ==================================================================
 * NodeControlUtilsTests.java - 30/07/2019 4:23:22 pm
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

package net.solarnetwork.util.test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import java.math.BigDecimal;
import org.junit.Test;
import net.solarnetwork.util.NodeControlUtils;

/**
 * Test cases for the {@link NodeControlUtils} class.
 * 
 * @author Matt Magoffin
 * @version 1.0
 */
public class NodeControlUtilsTests {

	@Test
	public void booleanControlValue_nullObject() {
		String r = NodeControlUtils.booleanControlValue(null);
		assertThat("Converted result", r, equalTo("false"));
	}

	@Test
	public void booleanControlValue_nonNullObject() {
		String r = NodeControlUtils.booleanControlValue(Object.class);
		assertThat("Converted result", r, equalTo("false"));
	}

	@Test
	public void booleanControlValue_nonNullObjectToStringTrue() {
		String r = NodeControlUtils.booleanControlValue(new Object() {

			@Override
			public String toString() {
				return "true";
			}

		});
		assertThat("Converted result", r, equalTo("true"));
	}

	@Test
	public void booleanControlValue_booleanTrue() {
		String r = NodeControlUtils.booleanControlValue(Boolean.TRUE);
		assertThat("Converted result", r, equalTo("true"));
	}

	@Test
	public void booleanControlValue_booleanFalse() {
		String r = NodeControlUtils.booleanControlValue(Boolean.FALSE);
		assertThat("Converted result", r, equalTo("false"));
	}

	@Test
	public void booleanControlValue_0() {
		String r = NodeControlUtils.booleanControlValue(0);
		assertThat("Converted result", r, equalTo("false"));
	}

	@Test
	public void booleanControlValue_1() {
		String r = NodeControlUtils.booleanControlValue(1);
		assertThat("Converted result", r, equalTo("true"));
	}

	@Test
	public void booleanControlValue_stringFalse() {
		String r = NodeControlUtils.booleanControlValue("false");
		assertThat("Converted result", r, equalTo("false"));
	}

	@Test
	public void booleanControlValue_stringTrue() {
		String r = NodeControlUtils.booleanControlValue("true");
		assertThat("Converted result", r, equalTo("true"));
	}

	@Test
	public void booleanControlValue_string0() {
		String r = NodeControlUtils.booleanControlValue("0");
		assertThat("Converted result", r, equalTo("false"));
	}

	@Test
	public void booleanControlValue_string1() {
		String r = NodeControlUtils.booleanControlValue("1");
		assertThat("Converted result", r, equalTo("true"));
	}

	@Test
	public void booleanControlValue_stringNo() {
		String r = NodeControlUtils.booleanControlValue("no");
		assertThat("Converted result", r, equalTo("false"));
	}

	@Test
	public void booleanControlValue_stringYes() {
		String r = NodeControlUtils.booleanControlValue("yes");
		assertThat("Converted result", r, equalTo("true"));
	}

	@Test
	public void floatControlValue_nullObject() {
		String r = NodeControlUtils.floatControlValue(null);
		assertThat("Converted result", r, nullValue());
	}

	@Test
	public void floatControlValue_nonNullObject() {
		String r = NodeControlUtils.floatControlValue(Object.class);
		assertThat("Converted result", r, nullValue());
	}

	@Test
	public void floatControlValue_float() {
		String r = NodeControlUtils.floatControlValue(123.456f);
		assertThat("Converted result", r, equalTo("123.456"));
	}

	@Test
	public void floatControlValue_double() {
		String r = NodeControlUtils.floatControlValue(123.456789123);
		assertThat("Converted result", r, equalTo("123.456789123"));
	}

	@Test
	public void floatControlValue_int() {
		String r = NodeControlUtils.floatControlValue(123);
		assertThat("Converted result", r, equalTo("123"));
	}

	@Test
	public void floatControlValue_long() {
		String r = NodeControlUtils.floatControlValue(12345678912345567L);
		assertThat("Converted result", r, equalTo("12345678912345567"));
	}

	@Test
	public void floatControlValue_bigDecimal() {
		String s = "123.4567891230987654321";
		String r = NodeControlUtils.floatControlValue(new BigDecimal(s));
		assertThat("Converted result", r, equalTo(s));
	}

	@Test
	public void integerControlValue_nullObject() {
		String r = NodeControlUtils.integerControlValue(null);
		assertThat("Converted result", r, nullValue());
	}

	@Test
	public void integerControlValue_nonNullObject() {
		String r = NodeControlUtils.integerControlValue(Object.class);
		assertThat("Converted result", r, nullValue());
	}

	@Test
	public void integerControlValue_float() {
		String r = NodeControlUtils.integerControlValue(123.456f);
		assertThat("Converted result", r, equalTo("123"));
	}

	@Test
	public void integerControlValue_floatRounded() {
		String r = NodeControlUtils.integerControlValue(123.999f);
		assertThat("Converted result", r, equalTo("124"));
	}

	@Test
	public void integerControlValue_double() {
		String r = NodeControlUtils.integerControlValue(123.456789123);
		assertThat("Converted result", r, equalTo("123"));
	}

	@Test
	public void integerControlValue_doubleRounded() {
		String r = NodeControlUtils.integerControlValue(123.999789123);
		assertThat("Converted result", r, equalTo("124"));
	}

	@Test
	public void integerControlValue_int() {
		String r = NodeControlUtils.integerControlValue(123);
		assertThat("Converted result", r, equalTo("123"));
	}

	@Test
	public void integerControlValue_long() {
		String r = NodeControlUtils.integerControlValue(12345678912345567L);
		assertThat("Converted result", r, equalTo("12345678912345567"));
	}

	@Test
	public void integerControlValue_bigDecimal() {
		String s = "123.4567891230987654321";
		String r = NodeControlUtils.integerControlValue(new BigDecimal(s));
		assertThat("Converted result", r, equalTo("123"));
	}

	@Test
	public void percentControlValue_nullObject() {
		String r = NodeControlUtils.percentControlValue(null);
		assertThat("Converted result", r, nullValue());
	}

	@Test
	public void percentControlValue_nonNullObject() {
		String r = NodeControlUtils.percentControlValue(Object.class);
		assertThat("Converted result", r, nullValue());
	}

	@Test
	public void percentControlValue_float() {
		String r = NodeControlUtils.percentControlValue(0.456f);
		assertThat("Converted result", r, equalTo("0.456"));
	}

	@Test
	public void percentControlValue_double() {
		String r = NodeControlUtils.percentControlValue(0.456789123);
		assertThat("Converted result", r, equalTo("0.456789123"));
	}

	@Test
	public void percentControlValue_int() {
		String r = NodeControlUtils.percentControlValue(23);
		assertThat("Converted result", r, equalTo("0.23"));
	}

	@Test
	public void percentControlValue_long() {
		String r = NodeControlUtils.percentControlValue(99L);
		assertThat("Converted result", r, equalTo("0.99"));
	}

	@Test
	public void percentControlValue_bigDecimal() {
		String s = "0.4567891230987654321";
		String r = NodeControlUtils.percentControlValue(new BigDecimal(s));
		assertThat("Converted result", r, equalTo(s));
	}

	@Test
	public void stringControlValue_nullObject() {
		String r = NodeControlUtils.stringControlValue(null);
		assertThat("Converted result", r, nullValue());
	}

	@Test
	public void stringControlValue_nonNullObject() {
		String r = NodeControlUtils.stringControlValue(new Object() {

			@Override
			public String toString() {
				return "Hello, world.";
			}

		});
		assertThat("Converted result", r, equalTo("Hello, world."));
	}

	@Test
	public void stringControlValue_string() {
		String r = NodeControlUtils.stringControlValue("abc123");
		assertThat("Converted result", r, equalTo("abc123"));
	}
}

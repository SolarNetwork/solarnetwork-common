/* ==================================================================
 * NodeControlUtils.java - 30/07/2019 3:52:10 pm
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

package net.solarnetwork.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import net.solarnetwork.domain.NodeControlInfo;
import net.solarnetwork.domain.NodeControlPropertyType;

/**
 * Utilities for working with {@link NodeControlInfo}.
 * 
 * @author Matt Magoffin
 * @version 1.0
 * @since 1.52
 */
public final class NodeControlUtils {

	/**
	 * Convert an arbitrary object into a string suitable for using as a control
	 * value.
	 * 
	 * @param type
	 *        the desired control property type
	 * @param value
	 *        the value
	 * @return a string value derived from {@code value}, or {@literal null} if
	 *         {@code value} cannot be converted to an appropriate value
	 */
	public static String controlValue(NodeControlPropertyType type, Object value) {
		String controlValue = null;
		switch (type) {
			case Boolean:
				controlValue = booleanControlValue(value);
				break;

			case Float:
				controlValue = floatControlValue(value);
				break;

			case Integer:
				controlValue = integerControlValue(value);
				break;

			case Percent:
				controlValue = percentControlValue(value);
				break;

			case String:
				controlValue = stringControlValue(value);
				break;
		}
		return controlValue;
	}

	/**
	 * Convert an object to a boolean control value.
	 * 
	 * <p>
	 * This method converts {@code value} to a boolean value using the following
	 * rules:
	 * </p>
	 * 
	 * <ol>
	 * <li>if {@code value} is a {@link Boolean} then use directly</li>
	 * <li>if {@code value} is a {@link String} then return the result of
	 * {@link StringUtils#parseBoolean(String)}</li>
	 * <li>if {@code value} is a {@link Number} then if
	 * {@link Number#intValue()} is {@literal 0} then {@literal false} otherwise
	 * {@literal true}</li>
	 * <li>if {@code value} is {@literal null} then {@literal false} otherwise
	 * {@literal true}</li>
	 * <li>otherwise the result of passing the value's {@link Object#toString()}
	 * to {@link StringUtils#parseBoolean(String)} is returned</li>
	 * </ol>
	 * 
	 * <p>
	 * The resulting boolean value is then returned as a string, using
	 * {@literal "true"} or {@literal "false"}.
	 * </p>
	 * 
	 * @param value
	 *        the value to convert to a boolean control value
	 * @return either {@literal "true"} or {@literal "false"}, never
	 *         {@literal null}
	 */
	public static String booleanControlValue(Object value) {
		Boolean b = null;
		if ( value instanceof Boolean ) {
			b = (Boolean) value;
		} else if ( value instanceof String ) {
			b = StringUtils.parseBoolean((String) value);
		} else if ( value instanceof Number ) {
			b = ((Number) value).intValue() == 0 ? Boolean.FALSE : Boolean.TRUE;
		} else {
			b = (value == null ? Boolean.FALSE : StringUtils.parseBoolean(value.toString()));
		}
		return b.toString();
	}

	/**
	 * Convert an object to a floating point control value.
	 * 
	 * <p>
	 * This method converts {@code value} to a {@link BigDecimal} value using
	 * the following rules:
	 * </p>
	 * 
	 * <ol>
	 * <li>if {@code value} is a {@link BigDecimal} then use directly</li>
	 * <li>if {@code value} is a {@link Number} then create a {@link BigDecimal}
	 * from the string value of the number</li>
	 * <li>otherwise {@literal null} is returned</li>
	 * </ol>
	 * 
	 * <p>
	 * The resulting {@link BigDecimal} value is then returned as a string.
	 * </p>
	 * 
	 * @param value
	 *        the value to convert to a floating point control value
	 * @return a decimal string representation of a float, or {@literal null} if
	 *         {@code value} is not a number
	 */
	public static String floatControlValue(Object value) {
		BigDecimal n = null;
		if ( value instanceof BigDecimal ) {
			n = (BigDecimal) value;
		} else if ( value instanceof Number ) {
			n = new BigDecimal(value.toString());
		}
		return n != null ? n.toString() : null;
	}

	/**
	 * Convert an object to an integer control value.
	 * 
	 * <p>
	 * This method converts {@code value} to a {@link BigInteger} value using
	 * the following rules:
	 * </p>
	 * 
	 * <ol>
	 * <li>if {@code value} is a {@link BigInteger} then use directly</li>
	 * <li>if {@code value} is a {@link Number} then create a {@link BigDecimal}
	 * from the string value of the number and convert that to a rounded
	 * {@link BigInteger} value</li>
	 * <li>otherwise {@literal null} is returned</li>
	 * </ol>
	 * 
	 * <p>
	 * The resulting {@link BigInteger} value is then returned as a string.
	 * </p>
	 * 
	 * @param value
	 *        the value to convert to an integer control value
	 * @return a decimal string representation of an integer, or {@literal null}
	 *         if {@code value} is not a number
	 */
	public static String integerControlValue(Object value) {
		BigInteger n = null;
		if ( value instanceof BigInteger ) {
			n = (BigInteger) value;
		} else if ( value instanceof BigDecimal ) {
			n = ((BigDecimal) value).setScale(0, RoundingMode.HALF_UP).toBigInteger();
		} else if ( value instanceof Number ) {
			n = new BigDecimal(value.toString()).setScale(0, RoundingMode.HALF_UP).toBigInteger();
		}
		return n != null ? n.toString() : null;
	}

	/**
	 * Convert an object to a percentage control value.
	 * 
	 * <p>
	 * This method converts {@code value} to a {@link BigDecimal} value using
	 * the following rules:
	 * </p>
	 * 
	 * <ol>
	 * <li>if {@code value} is a {@link BigDecimal} then use directly</li>
	 * <li>if {@code value} is a {@link BigDecimal}, {@link Integer}, or
	 * {@link Long} then convert to a {@link BigDecimal} and shift the decimal
	 * point left by 2</li>
	 * <li>if {@code value} is a {@link Number} then create a {@link BigDecimal}
	 * from the string value of the number</li>
	 * <li>otherwise {@literal null} is returned</li>
	 * </ol>
	 * 
	 * <p>
	 * The resulting {@link BigDecimal} value is then returned as a string.
	 * </p>
	 * 
	 * @param value
	 *        the value to convert to a percentage control value
	 * @return a decimal string representation of a float, or {@literal null} if
	 *         {@code value} is not a number
	 */
	public static String percentControlValue(Object value) {
		BigDecimal d = null;
		if ( value instanceof BigDecimal ) {
			d = (BigDecimal) value;
		} else if ( value instanceof BigInteger ) {
			d = new BigDecimal((BigInteger) value).movePointLeft(2);
		} else if ( value instanceof Integer ) {
			d = new BigDecimal(((Integer) value).intValue()).movePointLeft(2);
		} else if ( value instanceof Long ) {
			d = new BigDecimal(((Long) value).longValue()).movePointLeft(2);
		} else if ( value instanceof Number ) {
			d = new BigDecimal(value.toString());
		}
		return d != null ? d.toString() : null;
	}

	/**
	 * Convert an object to a string control value.
	 * 
	 * @param value
	 *        the value to convert to an integer control value
	 * @return a decimal string representation of an integer, or {@literal null}
	 *         if {@code value} is not a number
	 */
	public static String stringControlValue(Object value) {
		return (value != null ? value.toString() : null);
	}

}

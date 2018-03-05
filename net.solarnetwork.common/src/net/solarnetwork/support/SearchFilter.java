/* ==================================================================
 * SearchFilter.java - Aug 8, 2010 8:15:59 PM
 * 
 * Copyright 2007-2010 SolarNetwork.net Dev Team
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

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

/**
 * Generic search filter supporting LDAP-style search queries.
 * 
 * <p>
 * This filter supports a group of key-value pairs joined by a common logical
 * operator. The key-value pairs are provided by a {@code Map}.
 * </p>
 * 
 * @author matt
 * @version 1.0
 */
public class SearchFilter {

	/**
	 * A filter logic qualifier for multiple filters.
	 */
	public enum LogicOperator {

		/**
		 * Combine filters with a logical AND (the default operator).
		 */
		AND,

		/** Combine filters with a logical OR. */
		OR,

		/** Combine filters with a logical NOT. */
		NOT;

		@Override
		public String toString() {
			switch (this) {
				case AND:
					return "&";
				case OR:
					return "|";
				case NOT:
					return "!";
				default:
					throw new AssertionError(this);
			}
		}

	}

	/**
	 * A comparison operator for a single filter.
	 */
	public enum CompareOperator {

		/** Match exactly this attribute value. */
		EQUAL,

		/** Match anything but exactly this attribute value. */
		NOT_EQUAL,

		/** Match attribute values less than this attribute value. */
		LESS_THAN,

		/**
		 * Match attribute values less than or equal to this attribute value.
		 */
		LESS_THAN_EQUAL,

		/** Match attribute values greater than this attribute value. */
		GREATER_THAN,

		/**
		 * Match attribute values greater than or equal to this attribute value.
		 */
		GREATER_THAN_EQUAL,

		/** Match a substring (this attribute value) within attribute values. */
		SUBSTRING,

		/**
		 * Match a substring (this attribute value) at the start of an attribute
		 * value.
		 */
		SUBSTRING_AT_START,

		/** Match if the attribute name is present, regardless of its value. */
		PRESENT,

		/** Approximately match the attribute value to this attribute value. */
		APPROX,

		/** For array comparison, an overlap operator. */
		OVERLAP;

		@Override
		public String toString() {
			switch (this) {
				case EQUAL:
					return "=";
				case NOT_EQUAL:
					return "<>";
				case LESS_THAN:
					return "<";
				case LESS_THAN_EQUAL:
					return "<=";
				case GREATER_THAN:
					return ">";
				case GREATER_THAN_EQUAL:
					return ">=";
				case SUBSTRING:
					return "**";
				case SUBSTRING_AT_START:
					return "*";
				case PRESENT:
					return "?";
				case APPROX:
					return "~";
				case OVERLAP:
					return "&&";
				default:
					throw new AssertionError(this);
			}
		}

	}

	private final Map<String, ?> filter;
	private final CompareOperator compareOp;
	private final LogicOperator logicOp;

	/**
	 * Construct with a filter. Uses {@link CompareOperator#EQUAL} and
	 * {@link LogicOperator#AND}.
	 */
	public SearchFilter(Map<String, ?> filter) {
		this(filter, CompareOperator.EQUAL, LogicOperator.AND);
	}

	/**
	 * Construct with values.
	 * 
	 * @param compareOp
	 *        the comparison operator
	 * @param logicOp
	 *        the logical operator
	 */
	public SearchFilter(Map<String, ?> filter, CompareOperator compareOp, LogicOperator logicOp) {
		super();
		this.filter = filter;
		this.compareOp = compareOp;
		this.logicOp = logicOp;
	}

	/**
	 * Construct with a single key-value pair.
	 * 
	 * @param key
	 *        the key
	 * @param value
	 *        the value
	 * @param compareOp
	 *        the comparison operator
	 */
	public SearchFilter(String key, Object value, CompareOperator compareOp) {
		this(Collections.singletonMap(key, value), compareOp, LogicOperator.AND);
	}

	/**
	 * Construct with a filter and logic operator and
	 * {@link CompareOperator#EQUAL} comparison operator.
	 * 
	 * @param filter
	 *        the filter
	 * @param logicOp
	 *        the logic operator
	 */
	public SearchFilter(Map<String, ?> filter, LogicOperator logicOp) {
		this(filter, CompareOperator.EQUAL, logicOp);
	}

	/**
	 * Appends this filter as a LDAP query string to a StringBuilder. If any
	 * value in the {@code filter} is itself a {@code SearchFilter}, the
	 * associated key is ignored and the {@code SearchFilter} is itself appended
	 * to the buffer. If the {@code filter} has only one key-value pair, the
	 * {@code logicOp} is ignored and {@bold not} appended to the buffer. If the
	 * {@code filter} has more than one key-value pair and the {@code logicOp}
	 * is {@link LogicOperator#NOT}, the filter will automatically be written as
	 * {@code NOT(AND((x)(y)))}.
	 * 
	 * @param buf
	 *        the buffer to append to
	 */
	public void appendLDAPSearchFilter(StringBuilder buf) {
		if ( filter == null ) {
			return;
		}
		if ( filter == null || filter.size() < 1 ) {
			return;
		}
		if ( filter.size() > 1 || logicOp == LogicOperator.NOT ) {
			buf.append('(').append(logicOp.toString());
			if ( filter.size() > 1 && logicOp == LogicOperator.NOT ) {
				// automatically becomes NOT(AND(x))
				buf.append("(&");
			}
		}
		for ( Map.Entry<String, ?> me : filter.entrySet() ) {
			String attributeName = me.getKey();
			Object value = me.getValue();
			if ( value instanceof SearchFilter ) {
				((SearchFilter) value).appendLDAPSearchFilter(buf);
				continue;
			}
			buf.append('(');
			buf.append(attributeName);
			switch (compareOp) {
				case GREATER_THAN:
					buf.append(">");
					break;

				case GREATER_THAN_EQUAL:
					buf.append(">=");
					break;

				case LESS_THAN:
					buf.append("<");
					break;

				case LESS_THAN_EQUAL:
					buf.append("<=");
					break;

				case PRESENT:
					buf.append("=*");
					break;

				case APPROX:
					buf.append("~=");
					break;

				default:
					buf.append("=");
					break;

			}

			if ( compareOp == CompareOperator.SUBSTRING ) {
				if ( value == null ) {
					buf.append("*");
				} else {
					buf.append("*");
					buf.append(value);
					buf.append("*");
				}
			} else if ( compareOp == CompareOperator.SUBSTRING_AT_START ) {
				if ( value != null ) {
					buf.append(value);
				}
				buf.append("*");
			} else if ( compareOp != CompareOperator.PRESENT ) {
				if ( value == null ) {
					buf.append("*");
				} else if ( value.getClass().isArray() ) {
					buf.append(Arrays.toString((Object[]) value));
				} else {
					buf.append(value);
				}
			}
			buf.append(')');
		}
		if ( filter.size() > 1 || logicOp == LogicOperator.NOT ) {
			buf.append(')');
			if ( filter.size() > 1 && logicOp == LogicOperator.NOT ) {
				buf.append(')');
			}
		}
	}

	/**
	 * Return an LDAP search filter string.
	 * 
	 * @return String
	 * @see #appendLDAPSearchFilter(StringBuilder)
	 */
	public String asLDAPSearchFilterString() {
		StringBuilder buf = new StringBuilder();
		appendLDAPSearchFilter(buf);
		return buf.toString();
	}

	/**
	 * Return an LDAP search filter string. This simply calls
	 * {@link #asLDAPSearchFilterString()}.
	 * 
	 * @return String
	 */
	@Override
	public String toString() {
		return asLDAPSearchFilterString();
	}

	public CompareOperator getCompareOperator() {
		return compareOp;
	}

	public LogicOperator getLogicOperator() {
		return logicOp;
	}

}

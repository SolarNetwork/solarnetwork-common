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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generic search filter supporting LDAP-style search queries.
 * 
 * <p>
 * This filter supports a group of key-value pairs joined by a common logical
 * operator. The key-value pairs are provided by a {@code Map}. Nested
 * {@code SearchFilter} instances can be used as values so that complex logic
 * can be implemented.
 * </p>
 * 
 * @author matt
 * @version 1.1
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

		/**
		 * Get an enum value from a key value.
		 * 
		 * @param key
		 *        the key of the enum to get
		 * @return the enum, or {@literal null} if not supported
		 * @since 1.1
		 */
		public static LogicOperator forKey(char key) {
			switch (key) {
				case '&':
					return AND;

				case '|':
					return OR;

				case '!':
					return NOT;

				default:
					return null;
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

		/**
		 * Get an enum value from a key value.
		 * 
		 * @param key
		 *        the key of the enum to get
		 * @return the enum, or {@literal null} if not supported
		 * @since 1.1
		 */
		public static CompareOperator forKey(String key) {
			switch (key) {
				case "=":
					return EQUAL;

				case "<>":
					return NOT_EQUAL;

				case "<":
					return LESS_THAN;

				case "<=":
					return LESS_THAN_EQUAL;

				case ">":
					return GREATER_THAN;

				case ">=":
					return GREATER_THAN_EQUAL;

				case "**":
					return SUBSTRING;

				case "*":
					return SUBSTRING_AT_START;

				case "?":
					return PRESENT;

				case "~":
				case "~=":
					return APPROX;

				case "&&":
					return OVERLAP;

				default:
					return null;
			}
		}

	}

	/**
	 * API for visiting all filters as a tree.
	 */
	public static interface VisitorCallback {

		/**
		 * Visit a node.
		 * 
		 * @param node
		 *        the filter being visited
		 * @param parentNode
		 *        the node's parent filter, or {@literal null} for the top-level
		 *        node
		 * @return {@literal false} to stop walking
		 */
		boolean visit(SearchFilter node, SearchFilter parentNode);

	}

	protected final Map<String, ?> filter;
	private final CompareOperator compareOp;
	private final LogicOperator logicOp;

	/**
	 * Construct with a filter. Uses {@link CompareOperator#EQUAL} and
	 * {@link LogicOperator#AND}.
	 * 
	 * @param filter
	 *        the filter value
	 */
	public SearchFilter(Map<String, ?> filter) {
		this(filter, CompareOperator.EQUAL, LogicOperator.AND);
	}

	/**
	 * Construct with values.
	 * 
	 * @param filter
	 *        the filter value
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
	 * {@code logicOp} is ignored and <b>not</b> appended to the buffer. If the
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

	/**
	 * Get the comparison operator.
	 * 
	 * @return the comparison
	 */
	public CompareOperator getCompareOperator() {
		return compareOp;
	}

	/**
	 * Get the logic operator.
	 * 
	 * @return the logic
	 */
	public LogicOperator getLogicOperator() {
		return logicOp;
	}

	/**
	 * Get the filter values.
	 * 
	 * @return the filter
	 */
	public Map<String, ?> getFilter() {
		return filter;
	}

	@SuppressWarnings("unchecked")
	private void addChild(SearchFilter n) {
		((Map<String, Object>) filter).put(UUID.randomUUID().toString(), n);
	}

	/**
	 * Test if this filter has any nested filters.
	 * 
	 * @return {@literal true} if any nested filters exist within this filter
	 * @since 1.1
	 */
	public boolean hasNestedFilter() {
		for ( Object o : filter.values() ) {
			if ( o instanceof SearchFilter ) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Walk the filter as a tree.
	 * 
	 * @param callback
	 *        the callback
	 */
	public void walk(VisitorCallback callback) {
		walkNode(this, null, callback);
	}

	private static boolean walkNode(SearchFilter node, SearchFilter parent, VisitorCallback callback) {
		if ( node == null ) {
			return false;
		}
		if ( !callback.visit(node, parent) ) {
			return false;
		}
		if ( node.filter != null ) {
			for ( Map.Entry<String, ?> me : node.filter.entrySet() ) {
				if ( me.getValue() instanceof SearchFilter ) {
					if ( !walkNode((SearchFilter) me.getValue(), node, callback) ) {
						return false;
					}
				}
			}
		}
		return true;
	}

	private static final Pattern TOKEN_PAT = Pattern.compile("\\s*(\\([&|!]|\\(|\\))\\s*");

	private static final Pattern COMP_PAT = Pattern.compile("(.+?)(=|<>|~=?|<=?|>=?|\\?|\\&\\&)(.+)");

	/*-
	private static final boolean isLogicOp(String text) {
		return (text != null && text.length() > 0 && LogicOperator.forKey(text.charAt(0)) != null);
	}
	*/

	private static SearchFilter logicNode(char c) {
		return new SearchFilter(new LinkedHashMap<>(), LogicOperator.forKey(c));
	}

	private static SearchFilter compNode(String token) {
		Matcher m = COMP_PAT.matcher(token);
		if ( m.matches() ) {
			CompareOperator op = CompareOperator.forKey(m.group(2));
			String val = m.group(3);
			if ( op == CompareOperator.EQUAL ) {
				final int len = val.length();
				final char lastChar = val.charAt(len - 1);
				if ( len > 2 && val.charAt(0) == '*' && lastChar == '*' ) {
					op = CompareOperator.SUBSTRING;
					val = val.substring(1, len - 1);
				} else if ( len > 1 && lastChar == '*' ) {
					op = CompareOperator.SUBSTRING_AT_START;
					val = val.substring(0, len - 1);
				} else if ( len == 1 && lastChar == '*' ) {
					op = CompareOperator.PRESENT;
					val = null;
				}
			}
			return new SearchFilter(m.group(1), val, op);
		}
		return null;
	}

	private static SearchFilter parseTokens(List<String> tokens, int start, int end) {
		char c;
		SearchFilter topNode = null;
		SearchFilter node = null;
		String tok = null;
		Deque<SearchFilter> stack = new LinkedList<>();
		for ( int i = start; i < end; i += 1 ) {
			tok = tokens.get(i);
			if ( tok.length() < 1 ) {
				continue;
			}
			c = tok.charAt(0);
			if ( c == '(' ) {
				// starting new item
				if ( tok.length() > 1 ) {
					// starting new logical group
					c = tok.charAt(1);
					node = logicNode(c);
					if ( topNode != null ) {
						topNode.addChild(node);
					}
					stack.push(node);
					topNode = node;
				} else {
					// starting a key/value pair
					if ( i + 1 < end ) {
						node = compNode(tokens.get(i + 1));
					}
					if ( topNode != null ) {
						topNode.addChild(node);
					} else {
						// our top node is not a group node, so only one node is possible and we can return now
						return node;
					}
					i += 2; // skip the comparison token + our assumed closing paren
				}
			} else if ( c == ')' ) {
				if ( stack.size() > 1 ) {
					stack.pop();
					topNode = stack.peek();
				} else {
					return topNode;
				}
			}
		}

		// don't expect to get here, unless badly formed filter
		return (stack.size() > 0 ? stack.peek() : topNode);
	}

	/**
	 * Parse an array of search filter tokens, as created via splitting a string
	 * with the {@link #TOKEN_PAT} regular expression.
	 * 
	 * <p>
	 * For example the simple filter <code>(foo=bar)</code> could be expressed
	 * as the tokens <code>["(", "foo=bar", ")"]</code> while the complex filter
	 * <code>(&(foo=bar)(bim>1))</code> could be expressed as the tokens
	 * <code>["(&", "(", "foo=bar", ")", "(", "bim>1", ")", ")"]</code>.
	 * </p>
	 *
	 * <p>
	 * Note that empty string tokens are ignored.
	 * </p>
	 */
	private static List<String> parseTokens(String s) {
		List<String> tokens = new ArrayList<>();
		Matcher m = TOKEN_PAT.matcher(s);
		int last = 0;
		while ( m.find() ) {
			if ( m.start() > last ) {
				tokens.add(s.substring(last, m.start()));
			}
			tokens.add(m.group(1));
			last = m.end();
		}
		return tokens;
	}

	/**
	 * Parse a LDAP search filter into a {@link SearchFilter} instance.
	 * 
	 * @param s
	 *        the string to parse
	 * @return the filter, or {@literal null} if {@code s} is not in a valid
	 *         format
	 */
	public static SearchFilter forLDAPSearchFilterString(String s) {
		if ( s == null ) {
			return null;
		}
		List<String> tokens = parseTokens(s);
		if ( tokens.isEmpty() ) {
			return null;
		}
		return parseTokens(tokens, 0, tokens.size());
	}

}

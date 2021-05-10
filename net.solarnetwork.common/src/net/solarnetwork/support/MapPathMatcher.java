/* ==================================================================
 * MapPathMatcher.java - 27/11/2020 7:55:21 am
 * 
 * Copyright 2020 SolarNetwork.net Dev Team
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import net.solarnetwork.support.SearchFilter.CompareOperator;
import net.solarnetwork.support.SearchFilter.LogicOperator;
import net.solarnetwork.support.SearchFilter.VisitorCallback;
import net.solarnetwork.util.NumberUtils;

/**
 * Match search filter against a nested map using paths for filter keys.
 * 
 * <p>
 * This utility tries to match a {@link SearchFilter} against a {@link Map}, by
 * treating each search filter key (the left-hand side of each filter
 * comparison) like a URL path that corresponds to an entry in the map. Each
 * path segment represents a map, possibly nested with the root map.
 * 
 * <p>
 * For example, given this JSON representation of a nested map structure:
 * </p>
 * 
 * <pre>
 * <code>{
 *     "foo": {
 *         "bar": 1,
 *         "bim": "bam"
 *     },
 *     "pow": 2
 * }</code>
 * </pre>
 * 
 * <p>
 * then the filters <code>(/foo/bar=1)</code> and <code>(/foo/bim=bam)</code>
 * and <code>(/pow=2)</code> and <code>(&amp;(/foo/bar=1)(/pow&lt;5))</code>
 * would match while <code>(/foo/bar&gt;1)</code> and <code>(/foo/bim=no)</code>
 * and <code>(|(/foo/bar&gt;1)(/pow&gt;2))</code> would not.
 * </p>
 * 
 * <p>
 * Originally ported from the JavaScript <code>objectPathMatcher</code> class.
 * </p>
 * 
 * @author matt
 * @version 1.0
 * @since 1.67
 */
public class MapPathMatcher {

	private final Map<String, ?> root;

	/**
	 * Constructor.
	 * 
	 * @param map
	 *        the map root
	 */
	public MapPathMatcher(Map<String, ?> map) {
		super();
		this.root = map;
	}

	/**
	 * Test if a filter matches a map.
	 * 
	 * @param map
	 *        the map
	 * @param filterText
	 *        the filter in text form
	 * @return {@literal true} if the filter matches
	 * @see SearchFilter#forLDAPSearchFilterString(String)
	 */
	public static boolean matches(Map<String, ?> map, String filterText) {
		return new MapPathMatcher(map).matches(filterText);
	}

	/**
	 * Test if a filter matches the configured map.
	 *
	 * @param map
	 *        the map
	 * @param filter
	 *        the filter to test for matches
	 *
	 * @return {@literal true} if the filter matches
	 */
	public static boolean matches(Map<String, ?> map, SearchFilter filter) {
		return new MapPathMatcher(map).matches(filter);
	}

	/**
	 * Test if a filter matches the configured map.
	 *
	 * @param filterText
	 *        the filter in text form
	 *
	 * @return {@literal true} if the filter matches
	 * @see SearchFilter#forLDAPSearchFilterString(String)
	 */
	public boolean matches(String filterText) {
		if ( root == null ) {
			return false;
		}
		return matches(SearchFilter.forLDAPSearchFilterString(filterText));
	}

	/**
	 * Test if a filter matches the configured map.
	 *
	 * @param filter
	 *        the filter to test for matches
	 *
	 * @return {@literal true} if the filter matches
	 */
	public boolean matches(SearchFilter filter) {
		if ( root == null ) {
			return false;
		}
		Evaluator eval = new Evaluator();
		return eval.evaluateFilter(root, filter);
	}

	private static interface EvalCallback {

		boolean isMatch(List<String> currPath, Object value);
	}

	private static boolean handleCallbackValue(Object value, List<String> currPath,
			EvalCallback callback) {
		if ( value != null && value.getClass().isArray() ) {
			Object[] valueArray = (Object[]) value;
			for ( int j = 0, len = valueArray.length; j < len; j += 1 ) {
				if ( !callback.isMatch(currPath, valueArray[j]) ) {
					return false;
				}
			}
		} else if ( value instanceof Iterable<?> ) {
			Iterable<?> valueCollection = (Iterable<?>) value;
			for ( Object colValue : valueCollection ) {
				if ( !callback.isMatch(currPath, colValue) ) {
					return false;
				}
			}
		} else if ( !callback.isMatch(currPath, value) ) {
			return false;
		}
		return true;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static boolean walkObjectPathValues(Map<String, ?> obj, List<String> pathTokens,
			EvalCallback callback) {
		String pathToken = null;
		List<String> currPath = new ArrayList<>();
		String prop = null;
		Object val = null;
		int currPathIdx = -1;

		for ( int i = 0, end = pathTokens.size() - 1; i <= end; i += 1 ) {
			pathToken = pathTokens.get(i);
			if ( pathToken.length() < 1 ) {
				continue;
			}
			currPath.add(pathToken);
			if ( "*".equals(pathToken) && i == end ) {
				currPathIdx = currPath.size() - 1;
				for ( Map.Entry<String, ?> me : obj.entrySet() ) {
					currPath.set(currPathIdx, me.getKey());
					val = me.getValue();
					if ( !handleCallbackValue(val, currPath, callback) ) {
						return false;
					}
				}
			} else if ( "**".equals(pathToken) && i < end ) {
				currPathIdx = currPath.size() - 1;
				for ( Map.Entry<String, ?> me : obj.entrySet() ) {
					currPath.set(currPathIdx, me.getKey());
					prop = me.getKey();
					val = me.getValue();

					// check if prop after ** exists
					if ( prop.equals(pathTokens.get(i + 1)) && val != null
							&& ((!(val instanceof Map<?, ?>) && i + 1 == end)
									|| ((val instanceof Map<?, ?>) && i + 1 < end)) ) {
						if ( !(val instanceof Map<?, ?>) && i + 1 == end ) {
							// looking for **/X and found X
							if ( !handleCallbackValue(val, currPath, callback) ) {
								return false;
							}
						} else {
							// looking for **/X/Y and found X; start search with new path after **/X
							if ( !walkObjectPathValues((Map) val,
									pathTokens.subList(i + 2, pathTokens.size()), new EvalCallback() {

										@Override
										public boolean isMatch(List<String> nestedPath,
												Object nestedVal) {
											List<String> p = new ArrayList<>(currPath);
											p.addAll(nestedPath);
											return callback.isMatch(p, nestedVal);
										}
									}) ) {
								return false;
							}
						}
					} else if ( (val instanceof Map<?, ?>) ) {
						if ( !walkObjectPathValues((Map) val, pathTokens.subList(i, pathTokens.size()),
								new EvalCallback() {

									@Override
									public boolean isMatch(List<String> nestedPath, Object nestedVal) {
										List<String> p = new ArrayList<>(currPath);
										p.addAll(nestedPath);
										return callback.isMatch(p, nestedVal);
									}
								}) ) {
							return false;
						}
					} else if ( "*".equals(pathTokens.get(i + 1)) ) {
						if ( !handleCallbackValue(val, currPath, callback) ) {
							return false;
						}
					}
				}
				break;
			} else if ( i == end ) {
				if ( !handleCallbackValue(obj.get(pathToken), currPath, callback) ) {
					return false;
				}
			} else if ( obj.get(pathToken) instanceof Map<?, ?> ) {
				obj = (Map) obj.get(pathToken);
			} else {
				// prop pathToken not found on obj
				break;
			}
		}
		return true;
	}

	private static class StackObj {

		private final LogicOperator op;
		private final SearchFilter node;
		private boolean result;

		private StackObj(LogicOperator op, boolean result, SearchFilter node) {
			super();
			this.op = op;
			this.result = result;
			this.node = node;
		}
	}

	private static class Evaluator {

		private final List<StackObj> logicStack = new ArrayList<>();
		private int stackIdx = -1;
		private int logicStackSatisfiedIdx = -1; // index in logicStack of OR that has been satisfied
		private LogicOperator currLogicOp = LogicOperator.AND; // default is AND for simple filters
		private boolean foundMatch = false;

		private boolean shortCircuitIfPossible(boolean match) {
			boolean keepWalking = true;
			if ( currLogicOp == LogicOperator.AND ) {
				if ( !match ) {
					if ( stackIdx >= 0 ) {
						logicStack.get(stackIdx).result = false;
					}
					if ( stackIdx < 1 ) {
						// top-level AND has failed; all done
						foundMatch = false;
						keepWalking = false;
					} else {
						logicStackSatisfiedIdx = stackIdx;
					}
				} else if ( stackIdx < 0 ) {
					foundMatch = true;
				} else {
					logicStack.get(stackIdx).result = true;
				}
			} else if ( currLogicOp == LogicOperator.OR ) {
				if ( match ) {
					if ( stackIdx >= 0 ) {
						logicStack.get(stackIdx).result = true;
					}
					if ( stackIdx < 1 ) {
						// top-level OR has matched; all done
						foundMatch = true;
						keepWalking = false;
					} else {
						logicStackSatisfiedIdx = stackIdx;
					}
				}
			} else if ( currLogicOp == LogicOperator.NOT ) {
				if ( stackIdx >= 0 ) {
					logicStack.get(stackIdx).result = !match;
				}
				if ( stackIdx < 1 ) {
					// top-level NOT has matched; all done
					foundMatch = !match;
					keepWalking = false;
				} else {
					logicStackSatisfiedIdx = stackIdx;
				}

			}
			return !keepWalking;
		}

		private boolean evaluateFilter(Map<String, ?> obj, SearchFilter filter) {
			filter.walk(new VisitorCallback() {

				@Override
				public boolean visit(SearchFilter node, SearchFilter parent) {
					boolean match = false;
					if ( parent != null ) {
						if ( parent != logicStack.get(stackIdx).node ) {
							while ( stackIdx >= 0 ) {
								if ( logicStack.get(stackIdx).node == parent ) {
									currLogicOp = logicStack.get(stackIdx).op;
									if ( shortCircuitIfPossible(
											logicStack.get(logicStack.size() - 1).result) ) {
										return false;
									}
									break;
								}
								stackIdx -= 1;
							}

							// trim the stack to the current parent
							while ( logicStack.size() > stackIdx + 1 ) {
								logicStack.remove(logicStack.size() - 1);
							}

							if ( stackIdx < 0 ) {
								return false;
							}

							// if we've popped back before a satisfied condition, reset that index
							if ( logicStackSatisfiedIdx > stackIdx ) {
								logicStackSatisfiedIdx = -1;
							}
						}
					}
					if ( node.hasNestedFilter() ) {
						// new logic grouping
						logicStack.add(new StackObj(node.getLogicOperator(), false, node));
						currLogicOp = node.getLogicOperator();
						stackIdx += 1;
					} else if ( logicStackSatisfiedIdx == -1 ) {
						for ( final Map.Entry<String, ?> me : node.filter.entrySet() ) {
							boolean r = walkObjectPathValues(obj, Arrays.asList(me.getKey().split("/")),
									new EvalCallback() {

										private boolean isComparableOp(CompareOperator op) {
											return (op == CompareOperator.LESS_THAN
													|| op == CompareOperator.LESS_THAN_EQUAL
													|| op == CompareOperator.GREATER_THAN
													|| op == CompareOperator.GREATER_THAN_EQUAL);
										}

										@SuppressWarnings({ "rawtypes", "unchecked" })
										private boolean matchAsComparable(Object objValue,
												Object pathValue, CompareOperator op) {
											Comparable l = null;
											Comparable r = null;
											try {
												BigDecimal objNumber = NumberUtils
														.bigDecimalForNumber((Number) objValue);
												BigDecimal pathNumber = new BigDecimal(
														pathValue.toString());
												l = objNumber;
												r = pathNumber;
											} catch ( NumberFormatException e ) {
												// compare as simple strings
												l = objValue.toString();
												r = pathValue.toString();
											}
											if ( l != null && r != null ) {
												switch (op) {
													case LESS_THAN:
														return l.compareTo(r) < 0;

													case LESS_THAN_EQUAL:
														return l.compareTo(r) <= 0;

													case GREATER_THAN:
														return l.compareTo(r) > 0;

													case GREATER_THAN_EQUAL:
														return l.compareTo(r) >= 0;

													default:
														// should not be here
												}
											}
											return false;
										}

										@Override
										public boolean isMatch(List<String> currPath, Object value) {
											boolean match = false;
											if ( node.getCompareOperator() == CompareOperator.EQUAL ) {
												if ( value != null && value.equals(me.getValue()) ) {
													match = true;
												}
											} else if ( node
													.getCompareOperator() == CompareOperator.APPROX ) {
												if ( value != null && value.toString()
														.matches(me.getValue().toString()) ) {
													match = true;
												}
											} else if ( isComparableOp(node.getCompareOperator()) ) {
												if ( value != null && matchAsComparable(value,
														me.getValue(), node.getCompareOperator()) ) {
													match = true;
												}
											}
											// in case of wildcard path, only keep looking for more path matches if we haven't found a value match
											return !match;
										}

									});
							match = !r;
							if ( shortCircuitIfPossible(match) ) {
								return false;
							}
						}
					}
					return true;
				}
			});

			if ( logicStack.size() > 0 ) {
				foundMatch = logicStack.get(stackIdx).result;
			}

			return foundMatch;
		}
	}

}

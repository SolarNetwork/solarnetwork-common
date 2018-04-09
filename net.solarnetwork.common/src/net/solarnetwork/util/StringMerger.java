/* ==================================================================
 * StringMerger.java - Jan 14, 2010 9:13:00 AM
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

package net.solarnetwork.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

/**
 * Utility class for performing a simple mail-merge.
 * 
 * <p>
 * This class parses a String source and substitutes variables in the form of
 * <code>${<b>name</b>}</code> for a corresponding property in a specified data
 * object. The data object can be either a <code>java.util.Map</code> or an
 * arbitrary JavaBean. If the data object is a Map, the variable names will be
 * treated as keys in that map, the the corresponding value will be substituted
 * in the output String. Otherwise, reflection will be used to access JavaBean
 * getter methods, using the Struts naming conventions for bean property names
 * and nested names.
 * </p>
 * 
 * @author matt.magoffin
 * @version 1.0
 */
public final class StringMerger {

	private static final Logger LOG = LoggerFactory.getLogger(StringMerger.class);
	private static final Pattern MERGE_VAR_PAT = Pattern.compile("\\$\\{([^}]+)\\}");

	private StringMerger() {
		// do not instantiate
	}

	/**
	 * Merge from a Resource, and return the merged output as a String.
	 * 
	 * <p>
	 * This method calls the {@link #mergeResource(Resource, Object, String)}
	 * method, passing an empty string for <code>nullValue</code>.
	 * </p>
	 * 
	 * @param resource
	 *        the resource
	 * @param data
	 *        the data to merge with
	 * @return merged string
	 * @throws IOException
	 *         if an error occurs
	 */
	public static String mergeResource(Resource resource, Object data) throws IOException {
		return mergeResource(resource, data, "");
	}

	/**
	 * Merge from a Resource, and return the merged output as a String.
	 * 
	 * <p>
	 * This method will read the Resource as character data line by line,
	 * merging each line as it goes.
	 * </p>
	 * 
	 * @param resource
	 *        the resource
	 * @param data
	 *        the data to merge with
	 * @param nullValue
	 *        the value to substitute for null data elements
	 * @return merged string
	 * @throws IOException
	 *         if an error occurs
	 */
	public static String mergeResource(Resource resource, Object data, String nullValue)
			throws IOException {
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("Merging " + resource.getFilename() + " with " + data);
		}
		InputStream in = null;
		try {
			in = resource.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			StringBuilder buf = new StringBuilder();
			String oneLine = null;
			while ( (oneLine = reader.readLine()) != null ) {
				mergeString(oneLine, data, nullValue, buf);
				buf.append("\n");
			}
			return buf.toString();
		} finally {
			if ( in != null ) {
				try {
					in.close();
				} catch ( IOException e ) {
					// ignore this
				}
			}
		}
	}

	/**
	 * Merge from a file source, and return the merged output as a String.
	 * 
	 * @return the merged output
	 * @param filePath
	 *        path to the source file
	 * @param data
	 *        the <code>Map</code> or JavaBean with merge data
	 * @exception java.io.IOException
	 *            if an exception occurs
	 */
	public static String merge(String filePath, Object data) throws java.io.IOException {
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("Merging " + filePath + " with " + data);
		}
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(filePath));
			StringBuilder buf = new StringBuilder();
			String oneLine = null;
			while ( (oneLine = in.readLine()) != null ) {
				mergeString(oneLine, data, "", buf);
				buf.append("\n");
			}
			return buf.toString();
		} finally {
			if ( in != null ) {
				in.close();
			}
		}
	}

	/**
	 * Merge from a String source and return the result.
	 * 
	 * @return java.lang.String
	 * @param src
	 *        java.lang.String
	 * @param nullValue
	 *        the value to substitute for null data
	 * @param data
	 *        java.lang.Object
	 */
	public static String mergeString(String src, String nullValue, Object data) {
		StringBuilder buf = new StringBuilder();
		mergeString(src, data, nullValue, buf);
		return buf.toString();
	}

	/**
	 * Merge from a String source into a StringBuilder.
	 * 
	 * @param src
	 *        the source String to substitute into
	 * @param data
	 *        the data object to substitute with
	 * @param nullValue
	 *        the value to substitute for null data
	 * @param buf
	 *        the StringBuilder to append the output to
	 */
	public static void mergeString(String src, Object data, String nullValue, StringBuilder buf) {
		Matcher matcher = MERGE_VAR_PAT.matcher(src);

		//MatchResult[] matches = MERGE_VAR_PAT.matcher(src);
		//REMatch[] matches = MERGE_VAR_RE.getAllMatches(src);
		if ( !matcher.find() ) {
			buf.append(src);
		} else {
			int endLastMatchIdx = 0;
			do {
				MatchResult matchResult = matcher.toMatchResult();

				// append everything from the end of the last
				// match to the start of this match
				buf.append(src.substring(endLastMatchIdx, matchResult.start()));

				// perform substitution here...
				if ( data != null ) {
					int s = matchResult.start(1);
					int e = matchResult.end(1);
					if ( (s > -1) && (e > -1) ) {
						String varName = src.substring(s, e);
						if ( data instanceof java.util.Map<?, ?> ) {
							Object o = null;
							int sepIdx = varName.indexOf('.');
							if ( sepIdx > 0 ) {
								String varName2 = varName.substring(sepIdx + 1);
								varName = varName.substring(0, sepIdx);
								o = ((Map<?, ?>) data).get(varName);
								if ( o != null ) {
									try {
										o = PropertyUtils.getProperty(o, varName2);
									} catch ( Exception e2 ) {
										LOG.warn("Exception getting property '" + varName2 + "' out of "
												+ o.getClass() + ": " + e2);
									}
								}
							} else {
								// simply check for key
								o = ((Map<?, ?>) data).get(varName);
							}
							if ( o == null || (String.class.isAssignableFrom(o.getClass())
									&& !StringUtils.hasText(o.toString())) ) {
								buf.append(nullValue);
							} else {
								buf.append(o);
							}
						} else {
							// use reflection to get a bean property
							try {
								Object o = PropertyUtils.getProperty(data, varName);
								if ( o == null || (String.class.isAssignableFrom(o.getClass())
										&& !StringUtils.hasText(o.toString())) ) {
									buf.append(nullValue);
								} else {
									buf.append(o);
								}
							} catch ( Exception ex ) {
								LOG.warn("Exception getting property '" + varName + "' out of "
										+ data.getClass() + ": " + ex);
								buf.append(nullValue);
							}
						}
					}
					endLastMatchIdx = matchResult.end();
				}
			} while ( matcher.find() );

			if ( endLastMatchIdx < src.length() ) {
				buf.append(src.substring(endLastMatchIdx));
			}
		}
	}

}

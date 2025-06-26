/* ==================================================================
 * StringLogger.java - 7/08/2021 3:35:11 PM
 *
 * Copyright 2021 SolarNetwork.net Dev Team
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

package net.solarnetwork.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.helpers.MarkerIgnoringBase;

/**
 * A {@link org.slf4j.Logger} that logs to a list of log entries in memory.
 *
 * @author matt
 * @version 1.1
 * @since 1.16
 */
@SuppressWarnings("deprecation")
public final class StringLogger extends MarkerIgnoringBase {

	/** A parameter placeholder. */
	public static final Pattern PLACEHOLDER = Pattern.compile(Pattern.quote("{}"));

	private static final long serialVersionUID = -1977216957279160567L;

	/**
	 * Logging level.
	 */
	public enum Level {
		/** Trace level. */
		TRACE,

		/** Debug level. */
		DEBUG,

		/** Info level. */
		INFO,

		/** Warn level. */
		WARN,

		/** Error level. */
		ERROR,

		/** Off level. */
		OFF;
	}

	/**
	 * A log entry.
	 */
	public static class Entry {

		private final long date;
		private final StringLogger.Level level;
		private final String message;
		private final Object[] args;

		/**
		 * Constructor.
		 *
		 * @param level
		 *        the level
		 * @param message
		 *        the message
		 * @param args
		 *        the arguments
		 */
		public Entry(StringLogger.Level level, String message, Object[] args) {
			super();
			this.date = System.currentTimeMillis();
			this.level = level;
			this.message = message;
			this.args = args;
		}

		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder();
			buf.append(String.format("%d %5s ", date, level));

			Matcher m = PLACEHOLDER.matcher(message);
			int idx = 0;
			int prev = 0;
			while ( m.find() ) {
				int curr = m.start();
				buf.append(message.substring(prev, curr));
				if ( idx < args.length ) {
					buf.append(args[idx++]);
				}
				prev = m.end();
			}
			if ( prev < message.length() ) {
				buf.append(message.substring(prev));
			}

			return buf.toString();
		}

		/**
		 * Get the time stamp.
		 *
		 * @return the ts
		 */
		public long getDate() {
			return date;
		}

		/**
		 * Get the level.
		 *
		 * @return the level
		 */
		public StringLogger.Level getLevel() {
			return level;
		}

		/**
		 * Get the message.
		 *
		 * @return the message
		 */
		public String getMessage() {
			return message;
		}

		/**
		 * Get the arguments.
		 *
		 * @return the args
		 */
		public Object[] getArgs() {
			return args;
		}

	}

	/** The logged entries. */
	private final List<StringLogger.Entry> entries = Collections.synchronizedList(new ArrayList<>());

	/** The level to use. */
	private StringLogger.Level level = Level.TRACE;

	/**
	 * Get the level.
	 *
	 * @return the level
	 */
	public StringLogger.Level getLevel() {
		return level;
	}

	/**
	 * Set the level.
	 *
	 * @param level
	 *        the level to set
	 */
	public void setLevel(StringLogger.Level level) {
		this.level = level;
	}

	/**
	 * Get the entries.
	 *
	 * @return the entries
	 */
	public List<StringLogger.Entry> getEntries() {
		return entries;
	}

	@Override
	public void debug(String arg0) {
		entries.add(new Entry(Level.DEBUG, arg0, null));
	}

	@Override
	public void debug(String arg0, Object arg1) {
		entries.add(new Entry(Level.DEBUG, arg0, new Object[] { arg1 }));
	}

	@Override
	public void debug(String arg0, Object... arg1) {
		entries.add(new Entry(Level.DEBUG, arg0, arg1));
	}

	@Override
	public void debug(String arg0, Throwable arg1) {
		entries.add(new Entry(Level.DEBUG, arg0, new Object[] { arg1 }));
	}

	@Override
	public void debug(String arg0, Object arg1, Object arg2) {
		entries.add(new Entry(Level.DEBUG, arg0, new Object[] { arg1, arg2 }));
	}

	@Override
	public void error(String arg0) {
		entries.add(new Entry(Level.ERROR, arg0, null));
	}

	@Override
	public void error(String arg0, Object arg1) {
		entries.add(new Entry(Level.ERROR, arg0, new Object[] { arg1 }));
	}

	@Override
	public void error(String arg0, Object... arg1) {
		entries.add(new Entry(Level.ERROR, arg0, arg1));
	}

	@Override
	public void error(String arg0, Throwable arg1) {
		entries.add(new Entry(Level.ERROR, arg0, new Object[] { arg1 }));
	}

	@Override
	public void error(String arg0, Object arg1, Object arg2) {
		entries.add(new Entry(Level.ERROR, arg0, new Object[] { arg1, arg2 }));
	}

	@Override
	public void info(String arg0) {
		entries.add(new Entry(Level.INFO, arg0, null));
	}

	@Override
	public void info(String arg0, Object arg1) {
		entries.add(new Entry(Level.INFO, arg0, new Object[] { arg1 }));

	}

	@Override
	public void info(String arg0, Object... arg1) {
		entries.add(new Entry(Level.INFO, arg0, arg1));
	}

	@Override
	public void info(String arg0, Throwable arg1) {
		entries.add(new Entry(Level.INFO, arg0, new Object[] { arg1 }));
	}

	@Override
	public void info(String arg0, Object arg1, Object arg2) {
		entries.add(new Entry(Level.INFO, arg0, new Object[] { arg1, arg2 }));
	}

	@Override
	public boolean isDebugEnabled() {
		return (level.ordinal() <= Level.DEBUG.ordinal());
	}

	@Override
	public boolean isErrorEnabled() {
		return (level.ordinal() <= Level.ERROR.ordinal());
	}

	@Override
	public boolean isInfoEnabled() {
		return (level.ordinal() <= Level.INFO.ordinal());
	}

	@Override
	public boolean isTraceEnabled() {
		return (level.ordinal() <= Level.TRACE.ordinal());
	}

	@Override
	public boolean isWarnEnabled() {
		return (level.ordinal() <= Level.WARN.ordinal());
	}

	@Override
	public void trace(String arg0) {
		entries.add(new Entry(Level.TRACE, arg0, null));

	}

	@Override
	public void trace(String arg0, Object arg1) {
		entries.add(new Entry(Level.TRACE, arg0, new Object[] { arg1 }));
	}

	@Override
	public void trace(String arg0, Object... arg1) {
		entries.add(new Entry(Level.TRACE, arg0, arg1));
	}

	@Override
	public void trace(String arg0, Throwable arg1) {
		entries.add(new Entry(Level.TRACE, arg0, new Object[] { arg1 }));
	}

	@Override
	public void trace(String arg0, Object arg1, Object arg2) {
		entries.add(new Entry(Level.TRACE, arg0, new Object[] { arg1, arg2 }));
	}

	@Override
	public void warn(String arg0) {
		entries.add(new Entry(Level.WARN, arg0, null));
	}

	@Override
	public void warn(String arg0, Object arg1) {
		entries.add(new Entry(Level.WARN, arg0, new Object[] { arg1 }));
	}

	@Override
	public void warn(String arg0, Object... arg1) {
		entries.add(new Entry(Level.WARN, arg0, arg1));
	}

	@Override
	public void warn(String arg0, Throwable arg1) {
		entries.add(new Entry(Level.WARN, arg0, new Object[] { arg1 }));
	}

	@Override
	public void warn(String arg0, Object arg1, Object arg2) {
		entries.add(new Entry(Level.WARN, arg0, new Object[] { arg1, arg2 }));
	}

}

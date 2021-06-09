/* ==================================================================
 * JodaDateTimeDeserializer.java - Oct 22, 2014 10:59:45 AM
 * 
 * Copyright 2007-2014 SolarNetwork.net Dev Team
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

import java.io.IOException;
import java.util.TimeZone;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;

/**
 * JsonDeserializer for {@link DateTime} objects from formatted strings.
 * 
 * <p>
 * This deserializer handles timestamps in the following forms:
 * </p>
 * 
 * <ul>
 * <li><code>yyyy-MM-dd HH:mm:ss.SSS'Z'</code> (UTC time zone)</li>
 * <li><code>yyyy-MM-dd HH:mm:ss.SSS</code> (UTC time zone)</li>
 * <li><code>yyyy-MM-dd'T'HH:mm:ss.SSSZZ</code> (ISO 8601)</li>
 * <li><code>yyyy-MM-dd'T'HH:mm:ssZZ</code> (ISO 8601, no milliseconds)</li>
 * <li>millisecond epoch</li>
 * </ul>
 * 
 * @author matt
 * @version 1.2
 * @deprecated since 1.2, use
 *             {@link net.solarnetwork.codec.JodaDateTimeDeserializer}
 */
@Deprecated
public class JodaDateTimeDeserializer extends JodaBaseJsonDeserializer<DateTime> {

	private static final long serialVersionUID = -4973232210335660362L;

	private static final DateTimeFormatter[] FORMATS = {
			DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS'Z'").withZone(DateTimeZone.UTC),
			DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS").withZone(DateTimeZone.UTC),
			ISODateTimeFormat.dateTime(), ISODateTimeFormat.dateTimeNoMillis(), };

	/**
	 * Default constructor.
	 * 
	 * <p>
	 * Uses the pattern <code>yyyy-MM-dd HH:mm:ss.SSS'Z'</code>.
	 * </p>
	 */
	public JodaDateTimeDeserializer() {
		this("yyyy-MM-dd HH:mm:ss.SSS'Z'", TimeZone.getTimeZone("GMT"));
	}

	/**
	 * Construct with values.
	 * 
	 * @param pattern
	 *        the pattern
	 * @param timeZone
	 *        the time zone
	 */
	public JodaDateTimeDeserializer(String pattern, TimeZone timeZone) {
		super(DateTime.class, pattern, timeZone);
	}

	/**
	 * Construct with a pattern.
	 * 
	 * @param pattern
	 *        the pattern
	 */
	public JodaDateTimeDeserializer(String pattern) {
		super(DateTime.class, pattern);
	}

	@Override
	public DateTime deserialize(JsonParser parser, DeserializationContext context)
			throws IOException, JsonProcessingException {
		String s = parser.getText();
		for ( int i = 0, len = s.length(); i < len; i++ ) {
			if ( !Character.isDigit(s.charAt(i)) ) {
				for ( DateTimeFormatter fmt : FORMATS ) {
					try {
						return fmt.parseDateTime(s);
					} catch ( IllegalArgumentException e ) {
						// try next
					}
				}
				throw new JsonParseException(parser, "Unable to parse timestamp from [" + s + "]");
			}
		}
		try {
			long l = Long.parseLong(s);
			return new DateTime(l);
		} catch ( NumberFormatException e ) {
			throw new JsonParseException(parser, "Unable to parse timestamp from [" + s + "]", e);
		}
	}

}

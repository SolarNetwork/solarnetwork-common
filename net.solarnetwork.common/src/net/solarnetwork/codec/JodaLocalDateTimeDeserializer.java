/* ==================================================================
 * JodaLocalDateTimeDeserializer.java - Oct 22, 2014 10:59:45 AM
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

package net.solarnetwork.codec;

import java.io.IOException;
import java.util.TimeZone;
import org.joda.time.LocalDateTime;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;

/**
 * JsonDeserializer for {@link LocalDateTime} objects from formatted strings.
 * 
 * @author matt
 * @version 1.0
 * @since 1.72
 */
public class JodaLocalDateTimeDeserializer extends JodaBaseJsonDeserializer<LocalDateTime> {

	/** A default instance. */
	public static final JodaLocalDateTimeDeserializer INSTANCE = new JodaLocalDateTimeDeserializer();

	private static final long serialVersionUID = 5709750413856542012L;

	/**
	 * Default constructor.
	 * 
	 * <p>
	 * Uses the pattern <code>yyyy-MM-dd HH:mm</code>.
	 * </p>
	 */
	public JodaLocalDateTimeDeserializer() {
		super(LocalDateTime.class, "yyyy-MM-dd HH:mm");
	}

	/**
	 * Construct with values.
	 * 
	 * @param pattern
	 *        the pattern
	 * @param timeZone
	 *        the time zone
	 */
	public JodaLocalDateTimeDeserializer(String pattern, TimeZone timeZone) {
		super(LocalDateTime.class, pattern, timeZone);
	}

	/**
	 * Construct with a pattern.
	 * 
	 * @param pattern
	 *        the pattern
	 */
	public JodaLocalDateTimeDeserializer(String pattern) {
		super(LocalDateTime.class, pattern);
	}

	@Override
	public LocalDateTime deserialize(JsonParser parser, DeserializationContext context)
			throws IOException, JsonProcessingException {
		return formatter.parseLocalDateTime(parser.getText());
	}

}

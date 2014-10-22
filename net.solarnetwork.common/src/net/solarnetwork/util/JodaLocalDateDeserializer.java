/* ==================================================================
 * JodaLocalDateDeserializer.java - Oct 22, 2014 10:59:45 AM
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
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.joda.time.LocalDate;

/**
 * JsonDeserializer for {@link LocalDate} objects from formatted strings.
 * 
 * @author matt
 * @version 1.0
 */
public class JodaLocalDateDeserializer extends JodaBaseJsonDeserializer<LocalDate> {

	/**
	 * Default constructor.
	 * 
	 * <p>
	 * Uses the pattern <code>yyyy-MM-dd</code>.
	 * </p>
	 */
	public JodaLocalDateDeserializer() {
		super(LocalDate.class, "yyyy-MM-dd");
	}

	/**
	 * Construct with values.
	 * 
	 * @param pattern
	 *        the pattern
	 * @param timeZone
	 *        the time zone
	 */
	public JodaLocalDateDeserializer(String pattern, TimeZone timeZone) {
		super(LocalDate.class, pattern, timeZone);
	}

	/**
	 * Construct with a pattern.
	 * 
	 * @param pattern
	 *        the pattern
	 */
	public JodaLocalDateDeserializer(String pattern) {
		super(LocalDate.class, pattern);
	}

	@Override
	public LocalDate deserialize(JsonParser parser, DeserializationContext context) throws IOException,
			JsonProcessingException {
		return formatter.parseLocalDate(parser.getText());
	}

}

/* ==================================================================
 * JodaLocalDateTimeSerializer.java - Mar 20, 2013 8:01:34 PM
 * 
 * Copyright 2007-2013 SolarNetwork.net Dev Team
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
import org.joda.time.LocalDateTime;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * JsonSerializer for {@link LocalDateTime} into simple strings.
 * 
 * @author matt
 * @version 1.1
 */
public class JodaLocalDateTimeSerializer extends JodaBaseJsonSerializer<LocalDateTime> {

	private static final long serialVersionUID = -2514379393212280543L;

	/**
	 * Default constructor.
	 * 
	 * <p>
	 * Uses the pattern <code>yyyy-MM-dd HH:mm</code>.
	 * </p>
	 */
	public JodaLocalDateTimeSerializer() {
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
	public JodaLocalDateTimeSerializer(String pattern, TimeZone timeZone) {
		super(LocalDateTime.class, pattern, timeZone);
	}

	/**
	 * Construct with a pattern.
	 * 
	 * @param pattern
	 *        the pattern
	 */
	public JodaLocalDateTimeSerializer(String pattern) {
		super(LocalDateTime.class, pattern);
	}

	@Override
	public void serialize(LocalDateTime o, JsonGenerator generator, SerializerProvider provider)
			throws IOException, JsonGenerationException {
		if ( o == null ) {
			return;
		}
		generator.writeString(serializeWithFormatter(o));
	}

}

/* ==================================================================
 * JodaLocalTimeSerializer.java - Mar 20, 2013 5:41:54 PM
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

package net.solarnetwork.codec;

import java.io.IOException;
import java.util.TimeZone;
import org.joda.time.LocalTime;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * JsonSerializer for {@link LocalTime} into simple strings.
 * 
 * @author matt
 * @version 1.0
 * @since 1.72
 */
public class JodaLocalTimeSerializer extends JodaBaseJsonSerializer<LocalTime> {

	/** A default instance. */
	public static final JodaLocalTimeSerializer INSTANCE = new JodaLocalTimeSerializer();

	private static final long serialVersionUID = -3719618691614534679L;

	/**
	 * Default constructor.
	 * 
	 * <p>
	 * Uses the pattern <code>HH:mm</code>.
	 * </p>
	 */
	public JodaLocalTimeSerializer() {
		super(LocalTime.class, "HH:mm");
	}

	/**
	 * Construct with values.
	 * 
	 * @param pattern
	 *        the pattern
	 * @param timeZone
	 *        the time zone
	 */
	public JodaLocalTimeSerializer(String pattern, TimeZone timeZone) {
		super(LocalTime.class, pattern, timeZone);
	}

	/**
	 * Construct with a pattern.
	 * 
	 * @param pattern
	 *        the pattern
	 */
	public JodaLocalTimeSerializer(String pattern) {
		super(LocalTime.class, pattern);
	}

	@Override
	public void serialize(LocalTime o, JsonGenerator generator, SerializerProvider provider)
			throws IOException, JsonProcessingException {
		if ( o == null ) {
			return;
		}
		generator.writeString(serializeWithFormatter(o));
	}

}
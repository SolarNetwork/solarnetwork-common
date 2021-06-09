/* ==================================================================
 * JodaDateTimeEpochSerializer.java - 6/11/2019 7:10:59 am
 * 
 * Copyright 2019 SolarNetwork.net Dev Team
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
import org.joda.time.DateTime;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;

/**
 * Serialize {@link DateTime} objects into millisecond epoch number values.
 * 
 * @author matt
 * @version 1.1
 * @since 1.55
 * @deprecated since 1.1, use
 *             {@link net.solarnetwork.codec.JodaDateTimeEpochSerializer}
 */
@Deprecated
public class JodaDateTimeEpochSerializer extends StdScalarSerializer<DateTime> {

	private static final long serialVersionUID = 5162035716013692593L;

	/**
	 * Constructor.
	 */
	public JodaDateTimeEpochSerializer() {
		super(DateTime.class);
	}

	@Override
	public void serialize(DateTime value, JsonGenerator gen, SerializerProvider provider)
			throws IOException {
		if ( value == null ) {
			return;
		}
		gen.writeNumber(value.getMillis());
	}

}

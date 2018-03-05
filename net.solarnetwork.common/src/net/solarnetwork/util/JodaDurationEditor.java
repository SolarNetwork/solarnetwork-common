/* ==================================================================
 * JodaDurationFormatEditor.java - May 11, 2011 4:40:56 PM
 * 
 * Copyright 2007-2011 SolarNetwork.net Dev Team
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

import java.beans.PropertyEditorSupport;
import org.joda.time.Duration;
import org.joda.time.ReadableDuration;

/**
 * PropertyEditor for Joda Time's Duration objects.
 * 
 * <p>
 * This class has been designed with {@link CloningPropertyEditorRegistrar} in
 * mind, so that one instance of a {@link JodaDurationEditor} can be shared
 * between multiple threads to parse or format Joda date objects.
 * </p>
 * 
 * @author matt
 * @version 1.0
 */
public class JodaDurationEditor extends PropertyEditorSupport implements Cloneable {

	/**
	 * Default constructor.
	 */
	public JodaDurationEditor() {
		super();
	}

	@Override
	public String getAsText() {
		Object val = getValue();
		if ( val == null ) {
			return null;
		}
		if ( val instanceof ReadableDuration ) {
			return val.toString();
		}
		throw new IllegalArgumentException(
				"Unsupported duration object [" + val.getClass() + "]: " + val);
	}

	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		try {
			Long ms = Long.valueOf(text);
			setValue(new Duration(ms));
		} catch ( NumberFormatException e ) {
			throw new IllegalArgumentException("Not a valid ms duration");
		}
	}

	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch ( CloneNotSupportedException e ) {
			// should never get here
			throw new RuntimeException(e);
		}
	}

}

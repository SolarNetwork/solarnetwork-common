/* ==================================================================
 * RecordModelAdaptor.java - 6 Sept 2026 11:00:51 am
 *
 * Copyright 2026 SolarNetwork.net Dev Team
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

package net.solarnetwork.common.tmpl.st4;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import org.stringtemplate.v4.Interpreter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.misc.ObjectModelAdaptor;
import org.stringtemplate.v4.misc.STNoSuchPropertyException;

/**
 * Adaptor to allow using public {@code record} instances within ST4.
 *
 * <p>
 * Adapted from <a href=
 * "https://github.com/antlr/stringtemplate4/issues/313#issuecomment-2192876863">starsm64</a>
 * example.
 * </p>
 *
 * @author starsm64
 * @author matt
 * @version 1.0
 * @since 4.1
 */
public class RecordModelAdaptor extends ObjectModelAdaptor<Record> {

	/**
	 * Constructor.
	 */
	public RecordModelAdaptor() {
		super();
	}

	@Override
	public Object getProperty(Interpreter interpreter, ST self, Record rtype, Object property,
			String propertyName) throws STNoSuchPropertyException {
		RecordComponent[] components = rtype.getClass().getRecordComponents();
		for ( RecordComponent component : components ) {
			if ( component.getName().equals(propertyName) ) {
				try {
					return component.getAccessor().invoke(rtype);
				} catch ( IllegalAccessException | InvocationTargetException e ) {
					throw new STNoSuchPropertyException(e, rtype, propertyName);
				}
			}
		}
		return super.getProperty(interpreter, self, rtype, property, propertyName);
	}

}

/* ==================================================================
 * SimpMessageSendingOperationsFactory.java - 26/09/2016 6:07:47 PM
 * 
 * Copyright 2007-2016 SolarNetwork.net Dev Team
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

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * {@link FactoryBean} where the exposed object is actually configured via
 * auto-wiring on {@link #setObject(Object)}. This is to facilitate exposing
 * services with known bean IDs, for situations where a bean might be injected
 * into the application context with an ID generated at runtime.
 * 
 * @author matt
 * @version 1.0
 */
public class AutowiredPropertyFactoryBean<T> implements FactoryBean<T> {

	private T object;
	private final Class<T> objectType;

	/**
	 * Construct with the object type.
	 * 
	 * @param objectType
	 *        The object type.
	 */
	public AutowiredPropertyFactoryBean(Class<T> objectType) {
		super();
		this.objectType = objectType;
	}

	@Override
	public Class<?> getObjectType() {
		return objectType;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public T getObject() throws Exception {
		return object;
	}

	@Autowired
	public void setObject(T object) {
		this.object = object;
	}

}

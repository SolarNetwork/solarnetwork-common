/* ==================================================================
 * MapBeanProxyTests.java - 9/05/2021 8:54:27 AM
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

package net.solarnetwork.util.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.springframework.beans.NotReadablePropertyException;
import org.springframework.beans.NotWritablePropertyException;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import net.solarnetwork.util.MapBeanProxy;

/**
 * Test cases for the {@link MapBeanProxy} class.
 * 
 * @author matt
 * @version 1.0
 */
public class MapBeanProxyTests {

	private static interface MyInterface {

		String getFoo();

		Integer getBar();

		void setBar(Integer val);

	}

	@Test
	public void get() {
		// GIVEN
		MapBeanProxy bean = new MapBeanProxy(Collections.singletonMap("foo", "bar"));
		MyInterface obj = MapBeanProxy.createProxy(bean, MyInterface.class);

		// WHEN
		String f = obj.getFoo();

		// THEN
		assertThat("Proxied getter to map data", f, equalTo("bar"));
	}

	@Test
	public void get_beanWrapper() {
		// GIVEN
		MapBeanProxy bean = new MapBeanProxy(Collections.singletonMap("foo", "bar"));
		MyInterface obj = MapBeanProxy.createProxy(bean, MyInterface.class);
		PropertyAccessor acc = PropertyAccessorFactory.forBeanPropertyAccess(obj);

		// WHEN
		String f = (String) acc.getPropertyValue("foo");

		// THEN
		assertThat("Proxied getter via PropertyAccessor to map data", f, equalTo("bar"));
	}

	@Test(expected = NotReadablePropertyException.class)
	public void get_beanWrapper_notImplemented() {
		// GIVEN
		MapBeanProxy bean = new MapBeanProxy(Collections.singletonMap("foo", "bar"));
		MyInterface obj = MapBeanProxy.createProxy(bean, MyInterface.class);
		PropertyAccessor acc = PropertyAccessorFactory.forBeanPropertyAccess(obj);

		// WHEN
		acc.getPropertyValue("ohNo");
	}

	@Test
	public void set() {
		// GIVEN
		Map<String, Object> data = new HashMap<>(1);
		data.put("foo", "bar");
		MapBeanProxy bean = new MapBeanProxy(data, false);
		MyInterface obj = MapBeanProxy.createProxy(bean, MyInterface.class);

		// WHEN
		obj.setBar(123);

		// THEN
		assertThat("Proxied setter to map data", data, hasEntry("bar", 123));
	}

	@Test
	public void set_beanWrapper() {
		// GIVEN
		Map<String, Object> data = new HashMap<>(1);
		data.put("foo", "bar");
		MapBeanProxy bean = new MapBeanProxy(data, false);
		MyInterface obj = MapBeanProxy.createProxy(bean, MyInterface.class);
		PropertyAccessor acc = PropertyAccessorFactory.forBeanPropertyAccess(obj);

		// WHEN
		acc.setPropertyValue("bar", 123);

		// THEN
		assertThat("Proxied setter to map data", data, hasEntry("bar", 123));
	}

	@Test(expected = NotWritablePropertyException.class)
	public void set_beanWrapper_notImplemented() {
		// GIVEN
		Map<String, Object> data = new HashMap<>(1);
		data.put("foo", "bar");
		MapBeanProxy bean = new MapBeanProxy(data, false);
		MyInterface obj = MapBeanProxy.createProxy(bean, MyInterface.class);
		PropertyAccessor acc = PropertyAccessorFactory.forBeanPropertyAccess(obj);

		// WHEN
		acc.setPropertyValue("bim", 123);
	}

}

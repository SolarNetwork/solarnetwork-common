/* ==================================================================
 * MapBeanProxy.java - 9/05/2021 8:24:40 AM
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

package net.solarnetwork.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.util.ClassUtils;

/**
 * Proxy {@link InvocationHandler} that treats keys in a {@link Map} as JavaBean
 * getter/setter methods.
 * 
 * @author matt
 * @version 1.0
 * @since 1.71
 */
public class MapBeanProxy implements InvocationHandler {

	/** A pattern used to match a JavaBean style getter method name. */
	public static final Pattern GETTER_REGEX = Pattern.compile("(?:get|is)([A-Z][a-zA-Z0-9_]*)");

	/** A pattern used to match a JavaBean style setter method name. */
	public static final Pattern SETTER_REGEX = Pattern.compile("set([A-Z][a-zA-Z0-9_]*)");

	private final Map<String, ?> data;
	private final boolean readOnly;

	/**
	 * Constructor.
	 * 
	 * @param data
	 *        the map data; a new map instance will be created if
	 *        {@literal null}
	 */
	public MapBeanProxy(Map<String, ?> data) {
		this(data, true);
	}

	/**
	 * Constructor.
	 * 
	 * @param data
	 *        the map data; a new map instance will be created if
	 *        {@literal null}
	 * @param readOnly
	 *        {@literal true} to disallow setter method invocation to the data
	 *        map
	 */
	public MapBeanProxy(Map<String, ?> data, boolean readOnly) {
		super();
		this.data = (data != null ? data : readOnly ? Collections.emptyMap() : new LinkedHashMap<>());
		this.readOnly = readOnly;
	}

	/**
	 * Create a new proxy instance.
	 * 
	 * @param <T>
	 *        the type to cast the result to
	 * @param bean
	 *        the bean to proxy
	 * @param interfaces
	 *        specific interfaces to implement, or {@literal null} to extract
	 *        all interfaces from {@code bean}
	 * @return the proxy, which will implement all interfaces defined by
	 *         {@code interfaces} or {@code bean}
	 */
	@SuppressWarnings("unchecked")
	public static <T> T createProxy(MapBeanProxy bean, Class<?>... interfaces) {
		if ( interfaces == null ) {
			interfaces = ClassUtils.getAllInterfaces(bean);
		}
		Object proxy = Proxy.newProxyInstance(bean.getClass().getClassLoader(), interfaces, bean);
		return (T) proxy;
	}

	/**
	 * Create a new proxy instance.
	 * 
	 * @param <T>
	 *        the type to cast the result to
	 * @param bean
	 *        the bean to proxy
	 * @param classLoader
	 *        the class loader to use, or {@literal null} to use the class
	 *        loader of {@code bean}
	 * @param interfaces
	 *        specific interfaces to implement, or {@literal null} to extract
	 *        all interfaces from {@code bean}
	 * @return the proxy, which will implement all interfaces defined by
	 *         {@code interfaces} or {@code bean}
	 */
	@SuppressWarnings("unchecked")
	public static <T> T createProxy(MapBeanProxy bean, ClassLoader classLoader, Class<?>... interfaces) {
		if ( interfaces == null ) {
			interfaces = ClassUtils.getAllInterfaces(bean);
		}
		if ( classLoader == null ) {
			classLoader = bean.getClass().getClassLoader();
		}
		return (T) Proxy.newProxyInstance(classLoader, interfaces, bean);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		String methodName = method.getName();
		Object result = null;
		if ( args == null ) {
			result = get(keyForMethodName(GETTER_REGEX, methodName));
			if ( result == null ) {
				// if a getter method is not found, fall back to invoking the method on self,
				// to support extending classes that might want to add additional methods
				try {
					Method delegateMethod = getClass().getMethod(methodName, method.getParameterTypes());
					result = delegateMethod.invoke(this, args);
				} catch ( Exception e ) {
					// ignore fall back exceptions
				}
			}
		} else if ( args.length == 1 && !readOnly ) {
			set(keyForMethodName(SETTER_REGEX, methodName), args[0]);
		}
		return result;
	}

	private String keyForMethodName(Pattern pat, String methodName) {
		Matcher m = pat.matcher(methodName);
		String k = (m.matches() ? m.group(1) : null);
		if ( k != null ) {
			if ( k.length() < 2 ) {
				return k.toLowerCase();
			}
			k = k.substring(0, 1).toLowerCase() + k.substring(1);
		}
		return k;
	}

	private Object get(String key) {
		if ( key == null ) {
			return null;
		}
		return data.get(key);
	}

	@SuppressWarnings("unchecked")
	private void set(String key, Object val) {
		if ( key == null ) {
			return;
		}
		((Map<String, Object>) data).put(key, val);
	}

	/**
	 * Get the data map.
	 * 
	 * @return the data
	 */
	public Map<String, ?> getData() {
		return data;
	}

}

/* ===================================================================
 * ClassUtils.java
 * 
 * Created Jul 15, 2008 8:20:38 AM
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
 * ===================================================================
 */

package net.solarnetwork.util;

import java.beans.PropertyDescriptor;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.PropertyBatchUpdateException;
import org.springframework.util.StringUtils;

/**
 * Utility methods for dealing with classes at runtime.
 *
 * @author matt
 * @version 1.2
 */
public final class ClassUtils {

	/**
	 * A set of package name prefix values representing built-in Java classes.
	 * 
	 * <p>
	 * This can be useful as an exclusion set in the
	 * {@link #getAllInterfacesForClassAsSet(Class, Set)} method.
	 * </p>
	 * 
	 * @since 1.3
	 */
	public static final Set<String> JAVA_PACKAGE_PREFIXES = Collections
			.unmodifiableSet(new HashSet<String>(Arrays.asList("java.", "javax.")));

	private static final Set<String> DEFAULT_BEAN_PROP_NAME_IGNORE = new HashSet<String>(
			Arrays.asList(new String[] { "class" }));
	private static final Logger LOG = LoggerFactory.getLogger(ClassUtils.class);

	/* Do not instantiate me. */
	private ClassUtils() {
		super();
	}

	/**
	 * Instantiate a class of a specific interface type.
	 * 
	 * @param <T>
	 *        the desired interface type
	 * @param className
	 *        the class name that implements the interface
	 * @param type
	 *        the desired interface
	 * @return new instance of the desired type
	 */
	public static <T> T instantiateClass(String className, Class<T> type) {
		Class<? extends T> clazz = loadClass(className, type);
		try {
			T o = clazz.newInstance();
			return o;
		} catch ( Exception e ) {
			throw new RuntimeException("Unable to instantiate class [" + className + ']', e);
		}
	}

	/**
	 * Load a class of a particular type.
	 * 
	 * <p>
	 * This uses the {@code type}'s ClassLoader to load the class. If that is
	 * not available, it will use the current thread's context class loader.
	 * </p>
	 * 
	 * @param <T>
	 *        the desired interface type
	 * @param className
	 *        the class name that implements the interface
	 * @param type
	 *        the desired interface
	 * @return the class
	 */
	public static <T> Class<? extends T> loadClass(String className, Class<T> type) {
		try {
			ClassLoader loader = type.getClassLoader();
			if ( loader == null ) {
				loader = Thread.currentThread().getContextClassLoader();
			}
			Class<?> clazz = loader.loadClass(className);
			if ( !type.isAssignableFrom(clazz) ) {
				throw new RuntimeException("Class [" + clazz + "] is not a [" + type + ']');
			}
			return clazz.asSubclass(type);
		} catch ( ClassNotFoundException e ) {
			throw new RuntimeException("Unable to load class [" + className + ']', e);
		}
	}

	/**
	 * Set bean property values on an object from a Map.
	 * 
	 * @param o
	 *        the bean to set JavaBean properties on
	 * @param values
	 *        a Map of JavaBean property names and their corresponding values to
	 *        set
	 */
	public static void setBeanProperties(Object o, Map<String, ?> values) {
		BeanWrapper bean = PropertyAccessorFactory.forBeanPropertyAccess(o);
		bean.setAutoGrowNestedPaths(true);
		bean.setPropertyValues(values);
	}

	/**
	 * Set bean property values on an object from a Map.
	 * 
	 * @param o
	 *        The bean to set JavaBean properties on.
	 * @param values
	 *        A Map of JavaBean property names and their corresponding values to
	 *        set.
	 * @param ignoreErrors
	 *        Flag to ignore unknown and invalid properties.
	 * @since 1.1
	 */
	public static void setBeanProperties(Object o, Map<String, ?> values, boolean ignoreErrors) {
		if ( o == null || values == null ) {
			return;
		}
		BeanWrapper bean = PropertyAccessorFactory.forBeanPropertyAccess(o);
		bean.setAutoGrowNestedPaths(true);
		MutablePropertyValues pvs = new MutablePropertyValues(values);
		try {
			bean.setPropertyValues(pvs, ignoreErrors, ignoreErrors);
		} catch ( PropertyBatchUpdateException e ) {
			if ( ignoreErrors == false ) {
				throw e;
			}
		}
	}

	/**
	 * Get a Map of non-null bean properties for an object.
	 * 
	 * @param o
	 *        the object to inspect
	 * @param ignore
	 *        a set of property names to ignore (optional)
	 * @return Map (never null)
	 */
	public static Map<String, Object> getBeanProperties(Object o, Set<String> ignore) {
		if ( ignore == null ) {
			ignore = DEFAULT_BEAN_PROP_NAME_IGNORE;
		}
		Map<String, Object> result = new LinkedHashMap<String, Object>();
		BeanWrapper bean = PropertyAccessorFactory.forBeanPropertyAccess(o);
		PropertyDescriptor[] props = bean.getPropertyDescriptors();
		for ( PropertyDescriptor prop : props ) {
			if ( prop.getReadMethod() == null ) {
				continue;
			}
			String propName = prop.getName();
			if ( ignore != null && ignore.contains(propName) ) {
				continue;
			}
			Object propValue = bean.getPropertyValue(propName);
			if ( propValue == null ) {
				continue;
			}
			result.put(propName, propValue);
		}
		return result;
	}

	/**
	 * Get a Map of non-null <em>simple</em> bean properties for an object.
	 * 
	 * @param o
	 *        the object to inspect
	 * @param ignore
	 *        a set of property names to ignore (optional)
	 * @return Map (never <em>null</em>)
	 * @since 1.1
	 */
	public static Map<String, Object> getSimpleBeanProperties(Object o, Set<String> ignore) {
		if ( ignore == null ) {
			ignore = DEFAULT_BEAN_PROP_NAME_IGNORE;
		}
		Map<String, Object> result = new LinkedHashMap<String, Object>();
		BeanWrapper bean = PropertyAccessorFactory.forBeanPropertyAccess(o);
		PropertyDescriptor[] props = bean.getPropertyDescriptors();
		for ( PropertyDescriptor prop : props ) {
			if ( prop.getReadMethod() == null ) {
				continue;
			}
			String propName = prop.getName();
			if ( ignore != null && ignore.contains(propName) ) {
				continue;
			}
			Class<?> propType = bean.getPropertyType(propName);
			if ( !(propType.isPrimitive() || propType.isEnum() || String.class.isAssignableFrom(propType)
					|| Number.class.isAssignableFrom(propType)
					|| Character.class.isAssignableFrom(propType)
					|| Byte.class.isAssignableFrom(propType)
					|| Date.class.isAssignableFrom(propType)) ) {
				continue;
			}
			Object propValue = bean.getPropertyValue(propName);
			if ( propValue == null ) {
				continue;
			}
			if ( propType.isEnum() ) {
				propValue = propValue.toString();
			} else if ( Date.class.isAssignableFrom(propType) ) {
				propValue = ((Date) propValue).getTime();
			}
			result.put(propName, propValue);
		}
		return result;
	}

	/**
	 * Copy non-null bean properties from one object to another.
	 * 
	 * @param src
	 *        the object to copy values from
	 * @param dest
	 *        the object to copy values to
	 * @param ignore
	 *        a set of property names to ignore (optional) where <em>null</em>
	 */
	public static void copyBeanProperties(Object src, Object dest, Set<String> ignore) {
		copyBeanProperties(src, dest, ignore, false);
	}

	/**
	 * Copy non-null bean properties from one object to another.
	 * 
	 * @param src
	 *        the object to copy values from
	 * @param dest
	 *        the object to copy values to
	 * @param ignore
	 *        a set of property names to ignore (optional)
	 * @param emptyStringToNull
	 *        if <em>true</em> then String values that are empty or contain only
	 *        whitespace will be treated as if they where <em>null</em>
	 */
	public static void copyBeanProperties(Object src, Object dest, Set<String> ignore,
			boolean emptyStringToNull) {
		if ( ignore == null ) {
			ignore = DEFAULT_BEAN_PROP_NAME_IGNORE;
		}
		BeanWrapper bean = PropertyAccessorFactory.forBeanPropertyAccess(src);
		BeanWrapper to = PropertyAccessorFactory.forBeanPropertyAccess(dest);
		PropertyDescriptor[] props = bean.getPropertyDescriptors();
		for ( PropertyDescriptor prop : props ) {
			if ( prop.getReadMethod() == null ) {
				continue;
			}
			String propName = prop.getName();
			if ( ignore != null && ignore.contains(propName) ) {
				continue;
			}
			Object propValue = bean.getPropertyValue(propName);
			if ( propValue == null || (emptyStringToNull && (propValue instanceof String)
					&& !StringUtils.hasText((String) propValue)) ) {
				continue;
			}
			if ( to.isWritableProperty(propName) ) {
				to.setPropertyValue(propName, propValue);
			}
		}
	}

	/**
	 * Get a Map of non-null bean properties for an object.
	 * 
	 * @param o
	 *        the object to inspect
	 * @param ignore
	 *        a set of property names to ignore (optional)
	 * @param serializeIgnore
	 *        if <em>true</em> test for the {@link SerializeIgnore} annotation
	 *        for ignoring properties
	 * @return Map (never null)
	 */
	public static Map<String, Object> getBeanProperties(Object o, Set<String> ignore,
			boolean serializeIgnore) {
		if ( o == null ) {
			return null;
		}
		if ( ignore == null ) {
			ignore = DEFAULT_BEAN_PROP_NAME_IGNORE;
		}
		Map<String, Object> result = new LinkedHashMap<String, Object>();
		BeanWrapper bean = PropertyAccessorFactory.forBeanPropertyAccess(o);
		PropertyDescriptor[] props = bean.getPropertyDescriptors();
		for ( PropertyDescriptor prop : props ) {
			if ( prop.getReadMethod() == null ) {
				continue;
			}
			String propName = prop.getName();
			if ( ignore != null && ignore.contains(propName) ) {
				continue;
			}
			Object propValue = bean.getPropertyValue(propName);
			if ( propValue == null ) {
				continue;
			}
			if ( serializeIgnore ) {
				Method getter = prop.getReadMethod();
				if ( getter != null && getter.isAnnotationPresent(SerializeIgnore.class) ) {
					continue;
				}
			}
			result.put(propName, propValue);
		}
		return result;
	}

	/**
	 * Load a textual classpath resource into a String.
	 * 
	 * @param resourceName
	 *        the resource to load
	 * @param clazz
	 *        the Class to load the resource from
	 * @return the String
	 */
	public static String getResourceAsString(String resourceName, Class<?> clazz) {
		InputStream in = clazz.getResourceAsStream(resourceName);
		if ( in == null ) {
			throw new RuntimeException("Resource [" + resourceName + "] not found");
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			byte[] buffer = new byte[4096];
			int bytesRead = -1;
			while ( (bytesRead = in.read(buffer)) != -1 ) {
				out.write(buffer, 0, bytesRead);
			}
			out.flush();
			return out.toString();
		} catch ( IOException e ) {
			throw new RuntimeException("Error reading resource [" + resourceName + ']', e);
		} finally {
			try {
				in.close();
			} catch ( IOException ex ) {
				LOG.warn("Could not close InputStream", ex);
			}
			try {
				out.close();
			} catch ( IOException ex ) {
				LOG.warn("Could not close OutputStream", ex);
			}
		}
	}

	private static void addClassesToSetUnlessExcludedByPackagePrefix(Collection<Class<?>> classes,
			Set<Class<?>> set, Set<String> excluding) {
		if ( classes == null ) {
			return;
		}
		if ( excluding != null ) {
			for ( Class<?> clazz : classes ) {
				String fqn = clazz.getName();
				for ( String prefix : excluding ) {
					if ( fqn.startsWith(prefix) ) {
						return;
					}
				}
			}
		}
		set.addAll(classes);
	}

	/**
	 * Get all interfaces implemented by a class, excluding those in the
	 * {@link #JAVA_PACKAGE_PREFIXES} package set.
	 * 
	 * @param clazz
	 *        the class to get all implemented interfaces for
	 * @return the set of interfaces
	 * @since 1.1
	 */
	public static Set<Class<?>> getAllNonJavaInterfacesForClassAsSet(Class<?> clazz) {
		return getAllInterfacesForClassAsSet(clazz, JAVA_PACKAGE_PREFIXES);
	}

	/**
	 * Get a set of interfaces implemented by a class and any superclasses or
	 * extended interfaces, optionally excluding based on a set of name prefix
	 * values.
	 * 
	 * <p>
	 * The iteration order of the returned set will be equal to the order
	 * returned by {@link Class#getInterfaces()} method, in reverse class
	 * hierarchy order.
	 * </p>
	 * 
	 * @param clazz
	 *        the class to get all implemented interfaces for
	 * @param excluding
	 *        a set of class name prefix values to exclude from the results
	 * @return the set of interfaces
	 * @since 1.1
	 */
	public static Set<Class<?>> getAllInterfacesForClassAsSet(Class<?> clazz, Set<String> excluding) {
		Set<Class<?>> interfaces = new LinkedHashSet<Class<?>>();
		while ( clazz != null ) {
			if ( clazz.isInterface() ) {
				Set<Class<?>> classes = Collections.<Class<?>> singleton(clazz);
				addClassesToSetUnlessExcludedByPackagePrefix(classes, interfaces, excluding);
			}
			Class<?>[] ifcs = clazz.getInterfaces();
			for ( Class<?> ifc : ifcs ) {
				addClassesToSetUnlessExcludedByPackagePrefix(
						getAllInterfacesForClassAsSet(ifc, excluding), interfaces, excluding);
			}
			clazz = clazz.getSuperclass();
		}
		return interfaces;
	}

}

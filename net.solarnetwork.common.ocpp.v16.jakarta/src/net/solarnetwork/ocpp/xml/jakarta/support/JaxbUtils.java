/* ==================================================================
 * JaxbUtils.java - 4/02/2020 11:47:28 am
 * 
 * Copyright 2020 SolarNetwork.net Dev Team
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

package net.solarnetwork.ocpp.xml.jakarta.support;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.annotation.XmlRegistry;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Utility methods for JAXB tasks.
 * 
 * @author matt
 * @version 1.0
 */
public final class JaxbUtils {

	private JaxbUtils() {
		// not available
	}

	/**
	 * Create a new {@link JAXBContext} for the JAXB classes in an
	 * {@link XmlRegistry}.
	 * 
	 * <p>
	 * This method can be used as an alternative to
	 * {@link JAXBContext#newInstance(String)}, when class loading issues might
	 * prevent the built-in discovery process from working (e.g. in OSGi with
	 * competing JAXB implementations present). This method will call
	 * {@link #jaxbClassesForRegistry(Class)} and pass the result to
	 * {@link JAXBContext#newInstance(Class...)}.
	 * </p>
	 * 
	 * @param registryClass
	 *        the class, annotated with {@link XmlRegistry}, to create a context
	 *        for
	 * @return the new JAXB context
	 * @throws JAXBException
	 *         if any JAXB error occurs
	 * @throws IllegalArgumentException
	 *         if {@code registryClass} is not annotated with
	 *         {@link XmlRegistry}
	 */
	public static JAXBContext jaxbContextForRegistry(Class<?> registryClass) throws JAXBException {
		return JAXBContext.newInstance(jaxbClassesForRegistry(registryClass));
	}

	/**
	 * Discover the JAXB classes associated with an {@link XmlRegistry} class.
	 * 
	 * <p>
	 * This method will look for all zero-argument methods starting with the
	 * name {@literal create} whose return type is annotated with
	 * {@link XmlType} or {@link XmlRootElement}.
	 * </p>
	 * 
	 * @param registryClass
	 *        the class, annotated with {@link XmlRegistry}, to create a context
	 *        for
	 * @return the array of JAXB classes discovered; never {@literal null}
	 */
	public static Class<?>[] jaxbClassesForRegistry(Class<?> registryClass) {
		if ( !registryClass.isAnnotationPresent(XmlRegistry.class) ) {
			throw new IllegalArgumentException("Class must be annotated with @XmlRegistry.");
		}
		List<Class<?>> results = new ArrayList<>(16);
		for ( Method m : registryClass.getDeclaredMethods() ) {
			if ( !(m.getParameterCount() == 0 && m.getName().startsWith("create")
					&& (m.getReturnType().isAnnotationPresent(XmlType.class)
							|| m.getReturnType().isAnnotationPresent(XmlRootElement.class))) ) {
				// skip this one, doesn't look like a JAXB class
				continue;
			}
			results.add(m.getReturnType());
		}
		return results.toArray(new Class<?>[results.size()]);
	}

}

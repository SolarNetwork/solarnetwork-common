/* ==================================================================
 * AbstractView.java - Feb 11, 2012 3:07:31 PM
 * 
 * Copyright 2007-2012 SolarNetwork.net Dev Team
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
 * $Id$
 * ==================================================================
 */

package net.solarnetwork.web.support;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.solarnetwork.util.PropertySerializerRegistrar;

/**
 * Extension of {@link org.springframework.web.servlet.view.AbstractView} that
 * preserves model attribute ordering and supports serializing model properties
 * with a {@link PropertySerializerRegistrar}.
 * 
 * <p>
 * The configurable properties of this class are:
 * </p>
 * 
 * <dl>
 * <dt>propertySerializerRegistrar</dt>
 * <dd>An optional registrar of PropertySerializer instances that can be used to
 * serialize specific objects into String values. This can be useful for
 * formatting Date objects into strings, for example.</dd>
 * 
 * <dt>modelObjectIgnoreTypes</dt>
 * <dd>A set of class types to ignore from the model, and never output in the
 * response. Defaults to an empty array.</dd>
 * 
 * <dt>javaBeanIgnoreProperties</dt>
 * <dd>A set of JavaBean properties to ignore for all JavaBeans. This is useful
 * for omitting things like <code>class</code> which is not usually desired.
 * Defaults to {@link #DEFAULT_JAVA_BEAN_IGNORE_PROPERTIES}.</dd>
 * 
 * <dt>javaBeanTreatAsStringValues</dt>
 * <dd>A set of JavaBean property object types to treat as Strings for all
 * JavaBeans. This is useful for omitting object values such as Class objects,
 * which is not usually desired. Defaults to
 * {@link #DEFAULT_JAVA_BEAN_STRING_VALUES}.</dd>
 * 
 * </dl>
 * 
 * @author matt
 * @version $Revision$
 */
public abstract class AbstractView extends org.springframework.web.servlet.view.AbstractView {

	/**
	 * The default value for the <code>javaBeanIgnoreProperties</code> property.
	 */
	public static final String[] DEFAULT_JAVA_BEAN_IGNORE_PROPERTIES = new String[] { "class", };
	/**
	 * The default value for the <code>javaBeanTreatAsStringValues</code>
	 * property.
	 */
	public static final Class<?>[] DEFAULT_JAVA_BEAN_STRING_VALUES = new Class<?>[] { Class.class, };

	private static final Class<?>[] DEFAULT_MODEL_OBJECT_IGNORE_TYPES = new Class<?>[] {};
	private static final Pattern CHARSET_PATTERN = Pattern.compile("charset\\s*=\\s*([^\\s]+)",
			Pattern.CASE_INSENSITIVE);

	private PropertySerializerRegistrar propertySerializerRegistrar = null;
	private Set<Class<?>> modelObjectIgnoreTypes = new LinkedHashSet<Class<?>>(
			Arrays.asList(DEFAULT_MODEL_OBJECT_IGNORE_TYPES));
	private Set<String> javaBeanIgnoreProperties = new LinkedHashSet<String>(
			Arrays.asList(DEFAULT_JAVA_BEAN_IGNORE_PROPERTIES));
	private Set<Class<?>> javaBeanTreatAsStringValues = new LinkedHashSet<Class<?>>(
			Arrays.asList(DEFAULT_JAVA_BEAN_STRING_VALUES));

	/**
	 * This method performs the same functions as
	 * {@link AbstractView#render(Map, HttpServletRequest, HttpServletResponse)}
	 * except that it uses a LinkedHashMap to preserve model order rather than a
	 * HashMap.
	 */
	@Override
	public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("Rendering view with name '" + getBeanName() + "' with model " + model
					+ " and static attributes " + getStaticAttributes());
		}

		// Consolidate static and dynamic model attributes.
		Map<String, Object> mergedModel = new LinkedHashMap<String, Object>(getStaticAttributes().size()
				+ (model != null ? model.size() : 0));
		mergedModel.putAll(getStaticAttributes());
		if ( model != null ) {
			mergedModel.putAll(model);
		}

		// Expose RequestContext?
		if ( getRequestContextAttribute() != null ) {
			mergedModel.put(getRequestContextAttribute(),
					createRequestContext(request, response, mergedModel));
		}

		// remove objects that should be ignored
		if ( modelObjectIgnoreTypes != null && modelObjectIgnoreTypes.size() > 0 ) {
			Iterator<Object> objects = mergedModel.values().iterator();
			while ( objects.hasNext() ) {
				Object o = objects.next();
				if ( o == null ) {
					objects.remove();
					continue;
				}
				for ( Class<?> clazz : modelObjectIgnoreTypes ) {
					if ( clazz.isAssignableFrom(o.getClass()) ) {
						if ( logger.isTraceEnabled() ) {
							logger.trace("Ignoring model type [" + o.getClass() + ']');
						}
						objects.remove();
						break;
					}
				}
			}
		}

		// remove objects that serialize to null
		if ( propertySerializerRegistrar != null ) {
			for ( Iterator<Map.Entry<String, Object>> itr = mergedModel.entrySet().iterator(); itr
					.hasNext(); ) {
				Map.Entry<String, Object> me = itr.next();
				Object o = propertySerializerRegistrar.serializeProperty(me.getKey(), me.getValue()
						.getClass(), me.getValue(), me.getValue());
				if ( o == null ) {
					if ( logger.isDebugEnabled() ) {
						logger.debug("Removing model entry [" + me.getKey()
								+ "] because PropertySerializerRegistrar serialized to null");
					}
					itr.remove();
				}

			}
		}

		prepareResponse(request, response);
		renderMergedOutputModel(mergedModel, request, response);
	}

	/**
	 * Get the configured character encoding to use for the response.
	 * 
	 * <p>
	 * This method will extract the character encoding specified in
	 * {@link #getContentType()}, or default to {@code UTF-8} if none available.
	 * </p>
	 * 
	 * @return character encoding name
	 */
	protected String getResponseCharacterEncoding() {
		String charset = "UTF-8";
		Matcher m = CHARSET_PATTERN.matcher(getContentType());
		if ( m.find() ) {
			charset = m.group(1);
		}
		return charset;
	}

	public PropertySerializerRegistrar getPropertySerializerRegistrar() {
		return propertySerializerRegistrar;
	}

	public void setPropertySerializerRegistrar(PropertySerializerRegistrar propertySerializerRegistrar) {
		this.propertySerializerRegistrar = propertySerializerRegistrar;
	}

	public Set<Class<?>> getModelObjectIgnoreTypes() {
		return modelObjectIgnoreTypes;
	}

	public void setModelObjectIgnoreTypes(Set<Class<?>> modelObjectIgnoreTypes) {
		this.modelObjectIgnoreTypes = modelObjectIgnoreTypes;
	}

	public Set<String> getJavaBeanIgnoreProperties() {
		return javaBeanIgnoreProperties;
	}

	public void setJavaBeanIgnoreProperties(Set<String> javaBeanIgnoreProperties) {
		this.javaBeanIgnoreProperties = javaBeanIgnoreProperties;
	}

	public Set<Class<?>> getJavaBeanTreatAsStringValues() {
		return javaBeanTreatAsStringValues;
	}

	public void setJavaBeanTreatAsStringValues(Set<Class<?>> javaBeanTreatAsStringValues) {
		this.javaBeanTreatAsStringValues = javaBeanTreatAsStringValues;
	}

}

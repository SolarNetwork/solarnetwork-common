/* ==================================================================
 * SimpleCsvHttpMessageConverter.java - Dec 3, 2013 2:50:27 PM
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

package net.solarnetwork.web.support;

import java.beans.PropertyEditor;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;
import net.solarnetwork.codec.PropertySerializerRegistrar;
import net.solarnetwork.domain.Result;
import net.solarnetwork.util.ClassUtils;

/**
 * {@link HttpMessageConverter} that marshals objects into CSV documents.
 * 
 * @author matt
 * @version 1.3
 */
public class SimpleCsvHttpMessageConverter extends AbstractHttpMessageConverter<Object> {

	/**
	 * The default value for the <code>javaBeanIgnoreProperties</code> property.
	 */
	public static final String[] DEFAULT_JAVA_BEAN_IGNORE_PROPERTIES = new String[] { "class", };

	/**
	 * The default value for the <code>javaBeanTreatAsStringValues</code>
	 * property.
	 */
	public static final Class<?>[] DEFAULT_JAVA_BEAN_STRING_VALUES = new Class<?>[] { Class.class, };

	private PropertySerializerRegistrar propertySerializerRegistrar = null;
	private Set<String> javaBeanIgnoreProperties = new LinkedHashSet<String>(
			Arrays.asList(DEFAULT_JAVA_BEAN_IGNORE_PROPERTIES));
	private Set<Class<?>> javaBeanTreatAsStringValues = new LinkedHashSet<Class<?>>(
			Arrays.asList(DEFAULT_JAVA_BEAN_STRING_VALUES));
	private boolean includeHeader = true;

	/**
	 * Default constructor.
	 */
	public SimpleCsvHttpMessageConverter() {
		super(new MediaType("text", "csv", Charset.forName("UTF-8")));
	}

	@Override
	protected boolean supports(Class<?> clazz) {
		return true;
	}

	@Override
	protected Object readInternal(Class<? extends Object> clazz, HttpInputMessage inputMessage)
			throws IOException, HttpMessageNotReadableException {
		throw new UnsupportedOperationException("Reading CSV is not supported.");
	}

	@Override
	protected void writeInternal(Object t, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {
		Iterable<?> rows = null;
		if ( t instanceof Result<?> ) {
			// for Result objects that are successful, treat the `data` property as our desired output
			// if that is available, instead of the Result wrapper itself
			Result<?> r = (Result<?>) t;
			if ( r.getSuccess() != null && r.getSuccess().booleanValue() ) {
				Object d = r.getData();
				if ( d != null ) {
					t = d;
				}
			}
		}
		if ( t instanceof Iterable ) {
			rows = (Iterable<?>) t;
		} else {
			// see if object has an Iterable property on it
			Map<String, Object> props = ClassUtils.getBeanProperties(t, javaBeanIgnoreProperties);
			for ( Object o : props.values() ) {
				if ( o instanceof Iterable ) {
					rows = (Iterable<?>) o;
					break;
				}
			}
			if ( rows == null ) {
				List<Object> tmpList = new ArrayList<Object>(1);
				tmpList.add(t);
				rows = tmpList;
			}
		}

		Object row = null;
		Iterator<?> rowIterator = rows.iterator();
		if ( !rowIterator.hasNext() ) {
			return;
		}

		// get first row, to use for fields
		row = rowIterator.next();
		if ( row == null ) {
			return;
		}

		final List<String> fieldList = getCSVFields(row, null);
		final String[] fields = fieldList.toArray(new String[fieldList.size()]);

		if ( fields == null || fields.length < 1 ) {
			// could happen with empty Map, for example
			return;
		}

		final ICsvMapWriter writer = new CsvMapWriter(
				new OutputStreamWriter(outputMessage.getBody(), "UTF-8"),
				CsvPreference.EXCEL_PREFERENCE);
		try {
			// output header
			if ( includeHeader ) {
				writer.writeHeader(fields);
			}

			// output first row
			writeCSV(writer, fields, row);

			// output remainder rows
			while ( rowIterator.hasNext() ) {
				row = rowIterator.next();
				writeCSV(writer, fields, row);
			}
		} finally {
			if ( writer != null ) {
				try {
					writer.flush();
					writer.close();
				} catch ( IOException e ) {
					// ignore these
				}
			}
		}
	}

	private List<String> getCSVFields(Object row, final Collection<String> fieldOrder) {
		assert row != null;
		List<String> result = new ArrayList<String>();
		if ( row instanceof Map ) {
			Map<?, ?> map = (Map<?, ?>) row;
			if ( fieldOrder != null ) {
				for ( String key : fieldOrder ) {
					result.add(key);
				}
			} else {
				for ( Object key : map.keySet() ) {
					result.add(key.toString());
				}
			}
		} else {
			// use bean properties
			if ( propertySerializerRegistrar != null ) {
				// try whole-bean serialization first
				Object o = propertySerializerRegistrar.serializeProperty("row", row.getClass(), row,
						row);
				if ( o != row ) {
					if ( o != null ) {
						result = getCSVFields(o, fieldOrder);
						return result;
					}
				}
			}
			Map<String, Object> props = ClassUtils.getBeanProperties(row, javaBeanIgnoreProperties,
					true);
			result = getCSVFields(props, fieldOrder);
		}
		return result;
	}

	// this method exists so we don't have to add @SuppressWarnings to other (real) methods
	@SuppressWarnings("unchecked")
	private <T> T cast(Object o) {
		return (T) o;
	}

	private void writeCSV(ICsvMapWriter writer, String[] fields, Object row) throws IOException {
		if ( row instanceof Map ) {
			@SuppressWarnings("unchecked")
			Map<String, ?> map = (Map<String, ?>) row;
			writer.write(map, fields);
		} else if ( row != null ) {
			Map<String, Object> map = new HashMap<String, Object>(fields.length);

			// use bean properties
			if ( propertySerializerRegistrar != null ) {
				// try whole-bean serialization first
				row = propertySerializerRegistrar.serializeProperty("row", row.getClass(), row, row);
				if ( row == null ) {
					return;
				}
			}

			if ( row instanceof Map ) {
				Map<String, ?> rowMap = cast(row);
				for ( Map.Entry<String, ?> me : rowMap.entrySet() ) {
					Object val = getRowPropertyValue(row, me.getKey(), me.getValue(), null);
					if ( val != null ) {
						map.put(me.getKey(), val);
					}
				}
			} else {
				BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(row);
				for ( String name : fields ) {
					Object val = wrapper.getPropertyValue(name);
					val = getRowPropertyValue(row, name, val, wrapper);
					if ( val != null ) {
						map.put(name, val);
					}
				}
			}

			writer.write(map, fields);
		}
	}

	private Object getRowPropertyValue(Object row, String name, Object val, BeanWrapper wrapper) {
		if ( val != null ) {
			if ( getPropertySerializerRegistrar() != null ) {
				val = getPropertySerializerRegistrar().serializeProperty(name, val.getClass(), row, val);
			} else if ( wrapper != null ) {
				// Spring does not apply PropertyEditors on read methods, so manually handle
				PropertyEditor editor = wrapper.findCustomEditor(null, name);
				if ( editor != null ) {
					editor.setValue(val);
					val = editor.getAsText();
				}
			}
			if ( val instanceof Enum<?> || javaBeanTreatAsStringValues != null
					&& javaBeanTreatAsStringValues.contains(val.getClass()) ) {
				val = val.toString();
			}
		}
		return val;
	}

	/**
	 * Get the property serializer registrar.
	 * 
	 * @return the registrar
	 */
	public PropertySerializerRegistrar getPropertySerializerRegistrar() {
		return propertySerializerRegistrar;
	}

	/**
	 * Set the property serializer registrar.
	 * 
	 * @param propertySerializerRegistrar
	 *        the registrar to set
	 */
	public void setPropertySerializerRegistrar(PropertySerializerRegistrar propertySerializerRegistrar) {
		this.propertySerializerRegistrar = propertySerializerRegistrar;
	}

	/**
	 * Get the JavaBean properties to ignore.
	 * 
	 * @return the properties
	 */
	public Set<String> getJavaBeanIgnoreProperties() {
		return javaBeanIgnoreProperties;
	}

	/**
	 * Set the JavaBean properties to ignore.
	 * 
	 * @param javaBeanIgnoreProperties
	 *        the properties
	 */
	public void setJavaBeanIgnoreProperties(Set<String> javaBeanIgnoreProperties) {
		this.javaBeanIgnoreProperties = javaBeanIgnoreProperties;
	}

	/**
	 * Get the JavaBean classes to treat as strings.
	 * 
	 * @return the class set
	 */
	public Set<Class<?>> getJavaBeanTreatAsStringValues() {
		return javaBeanTreatAsStringValues;
	}

	/**
	 * Set the JavaBean classes to treat as strings.
	 * 
	 * @param javaBeanTreatAsStringValues
	 *        the class set
	 */
	public void setJavaBeanTreatAsStringValues(Set<Class<?>> javaBeanTreatAsStringValues) {
		this.javaBeanTreatAsStringValues = javaBeanTreatAsStringValues;
	}

	/**
	 * Get the "include header" option.
	 * 
	 * @return {@literal true} to include a header row in the output; defaults
	 *         to {@literal true}
	 */
	public boolean isIncludeHeader() {
		return includeHeader;
	}

	/**
	 * Set the "include header" option.
	 * 
	 * @param includeHeader
	 *        {@literal true} to include a header row in the output
	 */
	public void setIncludeHeader(boolean includeHeader) {
		this.includeHeader = includeHeader;
	}

}

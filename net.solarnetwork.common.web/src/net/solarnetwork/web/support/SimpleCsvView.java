/* ==================================================================
 * SimpleCsvView.java - Feb 11, 2012 3:03:32 PM
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

import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.solarnetwork.util.SerializeIgnore;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

/**
 * Spring {@link org.springframework.web.servlet.View} for turning objects into
 * CSV through JavaBean introspection.
 * 
 * <p>
 * The character encoding of the output must be specified in the
 * {@link #setContentType(String)} (e.g. {@literal text/csv;charset=UTF-8}).
 * </p>
 * 
 * <p>
 * The configurable properties of this class are:
 * </p>
 * 
 * <dl>
 * <dt>dataModelKey</dt>
 * <dd>If not <em>null</em>, then use this model key as the data object to
 * render as CSV. Otherwise, export just the first available key's associated
 * object. Defaults to {@link #DEFAULT_DATA_MODEL_KEY}.</dd>
 * 
 * <dt>fieldOrderKey</dt>
 * <dd>If not <em>null</em>, then use this model key as an ordered Collection of
 * exported field names, such that the CSV columns will be exported in the
 * specified order. If not specified, then for Map objects the output order will
 * be determined by the natural iteration order of the Map keys, and for
 * JavaBean objects the bean properties will be exported in case-insensitive
 * alphabetical order. Defaults to {@link #DEFAULT_FIELD_ORDER_KEY}.</dd>
 * </dl>
 * 
 * 
 * @author matt
 * @version $Revision$
 */
public class SimpleCsvView extends AbstractView {

	/** Default content type. */
	public static final String DEFAULT_CSV_CONTENT_TYPE = "text/csv;charset=UTF-8";

	/** The default value for the {@code dataModelKey} property. */
	public static final String DEFAULT_DATA_MODEL_KEY = "data";

	/** The default value for the {@code fieldOrderKey} property. */
	public static final String DEFAULT_FIELD_ORDER_KEY = "fieldOrder";

	private String dataModelKey = DEFAULT_DATA_MODEL_KEY;
	private String fieldOrderKey = DEFAULT_FIELD_ORDER_KEY;

	/**
	 * Default constructor.
	 */
	public SimpleCsvView() {
		super();
		setContentType(DEFAULT_CSV_CONTENT_TYPE);
	}

	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		if ( model.isEmpty() ) {
			return;
		}

		final String charset = getResponseCharacterEncoding();
		response.setCharacterEncoding(charset);
		response.setContentType(getContentType());

		final Object data = (dataModelKey != null && model.containsKey(dataModelKey) ? model
				.get(dataModelKey) : model);

		if ( data == null ) {
			return;
		}

		@SuppressWarnings("unchecked")
		final Collection<String> fieldOrder = (fieldOrderKey != null
				&& model.get(fieldOrderKey) instanceof Collection ? (Collection<String>) model
				.get(fieldOrderKey) : null);

		Iterable<?> rows = null;
		if ( data instanceof Iterable ) {
			rows = (Iterable<?>) data;
		} else {
			List<Object> tmpList = new ArrayList<Object>(1);
			tmpList.add(data);
			rows = tmpList;
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

		final List<String> fieldList = getCSVFields(row, fieldOrder);
		final String[] fields = fieldList.toArray(new String[fieldList.size()]);

		final ICsvMapWriter writer = new CsvMapWriter(response.getWriter(),
				CsvPreference.STANDARD_PREFERENCE);

		// output header
		if ( true ) { // TODO make configurable property
			Map<String, String> headerMap = new HashMap<String, String>(fields.length);
			for ( String field : fields ) {
				headerMap.put(field, field);
			}
			writeCSV(writer, fields, headerMap);
		}

		// output first row
		writeCSV(writer, fields, row);

		// output remainder rows
		while ( rowIterator.hasNext() ) {
			row = rowIterator.next();
			writeCSV(writer, fields, row);
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
			if ( getPropertySerializerRegistrar() != null ) {
				// try whole-bean serialization first
				Object o = getPropertySerializerRegistrar().serializeProperty("row", row.getClass(),
						row, row);
				if ( o != row ) {
					if ( o != null ) {
						result = getCSVFields(o, fieldOrder);
						return result;
					}
				}
			}
			BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(row);
			PropertyDescriptor[] props = wrapper.getPropertyDescriptors();
			Set<String> resultSet = new LinkedHashSet<String>();
			for ( PropertyDescriptor prop : props ) {
				String name = prop.getName();
				if ( getJavaBeanIgnoreProperties() != null
						&& getJavaBeanIgnoreProperties().contains(name) ) {
					continue;
				}
				if ( wrapper.isReadableProperty(name) ) {
					// test for SerializeIgnore
					Method getter = prop.getReadMethod();
					if ( getter != null && getter.isAnnotationPresent(SerializeIgnore.class) ) {
						continue;
					}
					resultSet.add(name);
				}
			}
			if ( fieldOrder != null && fieldOrder.size() > 0 ) {
				for ( String key : fieldOrder ) {
					if ( resultSet.contains(key) ) {
						result.add(key);
					}
				}
			} else {
				result.addAll(resultSet);
			}

		}
		return result;
	}

	private void writeCSV(ICsvMapWriter writer, String[] fields, Object row) throws IOException {
		if ( row instanceof Map ) {
			@SuppressWarnings("unchecked")
			Map<String, ?> map = (Map<String, ?>) row;
			writer.write(map, fields);
		} else if ( row != null ) {
			Map<String, Object> map = new HashMap<String, Object>(fields.length);

			// use bean properties
			if ( getPropertySerializerRegistrar() != null ) {
				// try whole-bean serialization first
				row = getPropertySerializerRegistrar()
						.serializeProperty("row", row.getClass(), row, row);
				if ( row == null ) {
					return;
				}
			}

			BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(row);
			for ( String name : fields ) {
				Object val = wrapper.getPropertyValue(name);
				if ( val != null ) {
					if ( getPropertySerializerRegistrar() != null ) {
						val = getPropertySerializerRegistrar().serializeProperty(name, val.getClass(),
								row, val);
					} else {
						// Spring does not apply PropertyEditors on read methods, so manually handle
						PropertyEditor editor = wrapper.findCustomEditor(null, name);
						if ( editor != null ) {
							editor.setValue(val);
							val = editor.getAsText();
						}
					}
					if ( val instanceof Enum<?> || getJavaBeanTreatAsStringValues() != null
							&& getJavaBeanTreatAsStringValues().contains(val.getClass()) ) {
						val = val.toString();
					}
					if ( val != null ) {
						map.put(name, val);
					}
				}
			}

			writer.write(map, fields);
		}
	}

	public String getDataModelKey() {
		return dataModelKey;
	}

	public void setDataModelKey(String dataModelKey) {
		this.dataModelKey = dataModelKey;
	}

	public String getFieldOrderKey() {
		return fieldOrderKey;
	}

	public void setFieldOrderKey(String fieldOrderKey) {
		this.fieldOrderKey = fieldOrderKey;
	}

}

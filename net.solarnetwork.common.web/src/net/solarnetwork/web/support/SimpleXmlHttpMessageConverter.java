/* ==================================================================
 * SimpleXmlHttpMessageConverter.java - Dec 3, 2013 11:58:01 AM
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

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import net.solarnetwork.codec.PropertySerializerRegistrar;
import net.solarnetwork.util.ClassUtils;

/**
 * {@link HttpMessageConverter} that marshals objects into XML documents.
 * 
 * @author matt
 * @version 1.1
 */
public class SimpleXmlHttpMessageConverter extends AbstractHttpMessageConverter<Object> {

	private static final ThreadLocal<SimpleDateFormat> SDF = new ThreadLocal<SimpleDateFormat>() {

		@Override
		protected SimpleDateFormat initialValue() {
			TimeZone tz = TimeZone.getTimeZone("GMT");
			SimpleDateFormat sdf = new SimpleDateFormat();
			sdf.setTimeZone(tz);
			sdf.applyPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			return sdf;
		}

	};

	/** Default content type. */
	public static final String DEFAULT_XML_CONTENT_TYPE = "text/xml;charset=UTF-8";

	private XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
	private Set<String> classNamesAllowedForNesting = null;
	private PropertySerializerRegistrar propertySerializerRegistrar = null;

	/**
	 * Default constructor.
	 */
	public SimpleXmlHttpMessageConverter() {
		super(MediaType.APPLICATION_XML, MediaType.TEXT_XML);
	}

	@Override
	protected boolean supports(Class<?> clazz) {
		return true;
	}

	@Override
	protected Object readInternal(Class<? extends Object> clazz, HttpInputMessage inputMessage)
			throws IOException, HttpMessageNotReadableException {
		throw new UnsupportedOperationException("Mapping from XML is not supported yet.");
	}

	@Override
	protected void writeInternal(Object t, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {
		OutputStream out = outputMessage.getBody();
		try {
			XMLStreamWriter writer = xmlOutputFactory.createXMLStreamWriter(out, "UTF-8");
			writer.writeStartDocument("UTF-8", "1.0");
			String rootName;
			if ( t.getClass().isArray() ) {
				rootName = "array";
			} else {
				rootName = t.getClass().getSimpleName();
			}
			outputObject(t, rootName, writer);
			writer.flush();
		} catch ( XMLStreamException e ) {
			throw new HttpMessageConversionException("Error creating XMLStreamWriter", e);
		}
	}

	private void outputObject(Object o, String name, XMLStreamWriter writer)
			throws IOException, XMLStreamException {
		if ( o != null && o.getClass().isArray() ) {
			o = Arrays.asList((Object[]) o);
		}
		if ( o instanceof Collection ) {
			Collection<?> col = (Collection<?>) o;
			outputCollection(col, name, writer);
		} else if ( o instanceof Map ) {
			Map<?, ?> map = (Map<?, ?>) o;
			outputMap(map, name, writer);
		} else if ( o instanceof String || o instanceof Number ) {
			// for simple types, write as unified <value type="String" value="foo"/>
			// this happens often in collections / maps of simple data types
			Map<String, Object> params = new LinkedHashMap<String, Object>(2);
			params.put("type", org.springframework.util.ClassUtils.getShortName(o.getClass()));
			params.put("value", o);
			writeElement("value", params, writer, true);
		} else {
			String elementName = (o == null ? name
					: org.springframework.util.ClassUtils.getShortName(o.getClass()));
			writeElement(elementName, o, writer, true);
		}
	}

	private void outputMap(Map<?, ?> map, String name, XMLStreamWriter writer)
			throws IOException, XMLStreamException {
		writeElement(name, null, writer, false);

		// for each entry, write an <entry> element
		for ( Map.Entry<?, ?> me : map.entrySet() ) {
			String entryName = me.getKey().toString();
			writer.writeStartElement("entry");
			writer.writeAttribute("key", entryName);

			Object value = me.getValue();
			if ( value != null && value.getClass().isArray() ) {
				value = Arrays.asList((Object[]) value);
			}
			if ( value instanceof Collection ) {
				// special collection case, we don't add nested element
				for ( Object o : (Collection<?>) value ) {
					outputObject(o, "value", writer);
				}
			} else {
				outputObject(value, null, writer);
			}

			writer.writeEndElement();
		}

		writer.writeEndElement();
	}

	private void outputCollection(Collection<?> col, String name, XMLStreamWriter writer)
			throws IOException, XMLStreamException {
		writeElement(name, null, writer, false);
		for ( Object o : col ) {
			outputObject(o, null, writer);
		}
		writer.writeEndElement();
	}

	private void writeElement(String name, Map<?, ?> props, XMLStreamWriter writer, boolean close)
			throws IOException, XMLStreamException {
		writer.writeStartElement(name);
		Map<String, Object> nested = null;
		if ( props != null ) {
			for ( Map.Entry<?, ?> me : props.entrySet() ) {
				String key = me.getKey().toString();
				Object val = me.getValue();
				if ( propertySerializerRegistrar != null ) {
					val = propertySerializerRegistrar.serializeProperty(name, val.getClass(), props,
							val);
				}
				if ( val != null && val.getClass().isArray() ) {
					val = Arrays.asList((Object[]) val);
				}
				if ( val instanceof Date ) {
					SimpleDateFormat sdf = SDF.get();
					Date date = (Date) val;
					val = sdf.format(date);
				} else if ( val instanceof Collection ) {
					if ( nested == null ) {
						nested = new LinkedHashMap<String, Object>(5);
					}
					nested.put(key, val);
					val = null;
				} else if ( val instanceof Map<?, ?> ) {
					if ( nested == null ) {
						nested = new LinkedHashMap<String, Object>(5);
					}
					nested.put(key, val);
					val = null;
				} else if ( classNamesAllowedForNesting != null && !(val instanceof Enum<?>) ) {
					for ( String prefix : classNamesAllowedForNesting ) {
						if ( val.getClass().getName().startsWith(prefix) ) {
							if ( nested == null ) {
								nested = new LinkedHashMap<String, Object>(5);
							}
							nested.put(key, val);
							val = null;
							break;
						}
					}
				}

				if ( val != null ) {
					writer.writeAttribute(key, val.toString());
				}
			}
		}
		if ( nested != null ) {
			for ( Map.Entry<String, Object> me : nested.entrySet() ) {
				outputObject(me.getValue(), me.getKey(), writer);
			}
		}
		if ( close ) {
			writer.writeEndElement();
		}
	}

	private void writeElement(String name, Object bean, XMLStreamWriter writer, boolean close)
			throws IOException, XMLStreamException {
		if ( propertySerializerRegistrar != null && bean != null ) { // try whole-bean serialization first
			Object o = propertySerializerRegistrar.serializeProperty(name, bean.getClass(), bean, bean);
			if ( o != bean ) {
				if ( o != null ) {
					outputObject(o, name, writer);
				}
				return;
			}
		}

		Map<String, Object> props = ClassUtils.getBeanProperties(bean, null, true);
		writeElement(name, props, writer, close);
	}

	public Set<String> getClassNamesAllowedForNesting() {
		return classNamesAllowedForNesting;
	}

	public void setClassNamesAllowedForNesting(Set<String> classNamesAllowedForNesting) {
		this.classNamesAllowedForNesting = classNamesAllowedForNesting;
	}

	public void setXmlOutputFactory(XMLOutputFactory xmlOutputFactory) {
		this.xmlOutputFactory = xmlOutputFactory;
	}

	public XMLOutputFactory getXmlOutputFactory() {
		return xmlOutputFactory;
	}

	public PropertySerializerRegistrar getPropertySerializerRegistrar() {
		return propertySerializerRegistrar;
	}

	public void setPropertySerializerRegistrar(PropertySerializerRegistrar propertySerializerRegistrar) {
		this.propertySerializerRegistrar = propertySerializerRegistrar;
	}

}

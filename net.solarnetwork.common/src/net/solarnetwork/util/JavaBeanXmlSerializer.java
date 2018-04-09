/* ==================================================================
 * JavaBeanXmlSerializer.java - Sep 6, 2011 9:00:16 PM
 * 
 * Copyright 2007-2011 SolarNetwork.net Dev Team
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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PropertySerializer that serializes JavaBean objects into XML strings.
 * 
 * <p>
 * This class can also be used in a stand-alone manner, calling the public
 * methods exposed for generating XML from JavaBean objects.
 * </p>
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
 * </dl>
 * 
 * @author matt
 * @version 1.0
 */
public class JavaBeanXmlSerializer implements PropertySerializer {

	private static final ThreadLocal<SimpleDateFormat> SDF = new ThreadLocal<SimpleDateFormat>();

	private final String rootElementName = "root";
	private final boolean singleBeanAsRoot = true;
	private final boolean useModelTimeZoneForDates = true;
	private final String modelKey = null;
	private final Set<String> classNamesAllowedForNesting = null;
	private final PropertySerializerRegistrar propertySerializerRegistrar = null;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public Object serialize(Object data, String propertyName, Object propertyValue) {
		setupDateFormat(null);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		XMLStreamWriter writer = startXml(out);
		try {
			outputObject(propertyValue, propertyName, writer);
		} catch ( XMLStreamException e ) {
			throw new RuntimeException(e);
		} finally {
			endXml(writer);
		}
		return out.toString();
	}

	/**
	 * Render a JavaBean object as XML serialized to a given OutputStream.
	 * 
	 * @param bean
	 *        the object to serialize as XML
	 * @param out
	 *        the OutputStream to write the XML to
	 */
	public void renderBean(Object bean, OutputStream out) {
		setupDateFormat(null);
		XMLStreamWriter writer = startXml(out);
		try {
			outputObject(bean, null, writer);
		} catch ( XMLStreamException e ) {
			throw new RuntimeException(e);
		} finally {
			endXml(writer);
		}
	}

	/**
	 * Render a Map as XML serialized to a given OutputStream.
	 * 
	 * @param model
	 *        the data to serialize as XML
	 * @param out
	 *        the OutputStream to write the XML to
	 */
	public void renderMap(Map<String, ?> model, OutputStream out) {
		Map<String, Object> finalModel = setupDateFormat(model);
		XMLStreamWriter writer = startXml(out);
		try {
			Object singleBean = finalModel.size() == 1 && this.singleBeanAsRoot
					? finalModel.values().iterator().next()
					: null;

			if ( singleBean != null ) {
				outputObject(singleBean, finalModel.keySet().iterator().next().toString(), writer);
			} else {
				writeElement(this.rootElementName, null, writer, false);

				for ( Map.Entry<String, Object> me : finalModel.entrySet() ) {
					outputObject(me.getValue(), me.getKey(), writer);
				}

				// end root element
				writer.writeEndElement();
			}
		} catch ( XMLStreamException e ) {
			throw new RuntimeException(e);
		} finally {
			endXml(writer);
		}
	}

	private XMLStreamWriter startXml(OutputStream out) {
		XMLStreamWriter writer = null;
		try {
			writer = XMLOutputFactory.newFactory().createXMLStreamWriter(out);
		} catch ( XMLStreamException e ) {
			throw new RuntimeException(e);
		} catch ( FactoryConfigurationError e ) {
			throw new RuntimeException(e);
		}
		return writer;
	}

	private void endXml(XMLStreamWriter writer) {
		try {
			writer.writeEndDocument();
			writer.flush();
		} catch ( XMLStreamException e ) {
			// ignore this
		} finally {
			SDF.remove();
		}
	}

	/**
	 * Create a {@link SimpleDateFormat} and cache on the {@link #SDF}
	 * ThreadLocal to re-use for all dates within a single response.
	 * 
	 * @param model
	 *        the model, to look for a TimeZone to format the dates in
	 */
	private Map<String, Object> setupDateFormat(Map<String, ?> model) {
		TimeZone tz = TimeZone.getTimeZone("GMT");
		Map<String, Object> result = null;
		if ( model != null ) {
			result = new LinkedHashMap<String, Object>();
			for ( Map.Entry<String, ?> me : model.entrySet() ) {
				Object o = me.getValue();
				if ( useModelTimeZoneForDates && o instanceof TimeZone ) {
					tz = (TimeZone) o;
				} else if ( modelKey != null ) {
					if ( modelKey.equals(me.getKey()) ) {
						result.put(modelKey, o);
					}
				} else {
					result.put(me.getKey(), o);
				}
			}
		}
		SimpleDateFormat sdf = new SimpleDateFormat();
		if ( tz.getRawOffset() == 0 ) {
			sdf.applyPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		} else {
			sdf.applyPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		}
		sdf.setTimeZone(tz);
		if ( logger.isTraceEnabled() ) {
			logger.trace("TZ offset " + tz.getRawOffset());
		}
		SDF.set(sdf);
		return result;
	}

	private void outputObject(Object o, String name, XMLStreamWriter out) throws XMLStreamException {
		if ( o instanceof Collection ) {
			Collection<?> col = (Collection<?>) o;
			outputCollection(col, name, out);
		} else if ( o instanceof Map ) {
			Map<?, ?> map = (Map<?, ?>) o;
			outputMap(map, name, out);
		} else if ( o instanceof String || o instanceof Number ) {
			// for simple types, write as unified <value type="String" value="foo"/>
			// this happens often in collections / maps of simple data types
			Map<String, Object> params = new LinkedHashMap<String, Object>(2);
			params.put("type", org.springframework.util.ClassUtils.getShortName(o.getClass()));
			params.put("value", o);
			writeElement("value", params, out, true);
		} else {
			String elementName = (o == null ? name
					: org.springframework.util.ClassUtils.getShortName(o.getClass()));
			writeElement(elementName, o, out, true);
		}
	}

	private void outputMap(Map<?, ?> map, String name, XMLStreamWriter out) throws XMLStreamException {
		writeElement(name, null, out, false);

		// for each entry, write an <entry> element
		for ( Map.Entry<?, ?> me : map.entrySet() ) {
			String entryName = me.getKey().toString();
			out.writeStartElement("entry");
			out.writeAttribute("key", entryName);

			Object value = me.getValue();
			if ( value instanceof Collection ) {
				// special collection case, we don't add nested element
				for ( Object o : (Collection<?>) value ) {
					outputObject(o, "value", out);
				}
			} else {
				outputObject(value, null, out);
			}
			out.writeEndElement();
		}

		out.writeEndElement();
	}

	private void outputCollection(Collection<?> col, String name, XMLStreamWriter out)
			throws XMLStreamException {
		writeElement(name, null, out, false);
		for ( Object o : col ) {
			outputObject(o, null, out);
		}
		out.writeEndElement();
	}

	private void writeElement(String name, Map<?, ?> props, XMLStreamWriter out, boolean close)
			throws XMLStreamException {
		out.writeStartElement(name);
		Map<String, Object> nested = null;
		if ( props != null ) {
			for ( Map.Entry<?, ?> me : props.entrySet() ) {
				String key = me.getKey().toString();
				Object val = me.getValue();
				if ( propertySerializerRegistrar != null ) {
					val = propertySerializerRegistrar.serializeProperty(name, val.getClass(), props,
							val);
				}
				if ( val instanceof Date ) {
					SimpleDateFormat sdf = SDF.get();
					// SimpleDateFormat has no way to create xs:dateTime with tz,
					// so use trick here to insert required colon for non GMT dates
					Date date = (Date) val;
					StringBuilder buf = new StringBuilder(sdf.format(date));
					if ( buf.charAt(buf.length() - 1) != 'Z' ) {
						buf.insert(buf.length() - 2, ':');
					}
					val = buf.toString();
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
					String attVal = val.toString();
					out.writeAttribute(key, attVal);
				}
			}
		}
		if ( nested != null ) {
			for ( Map.Entry<String, Object> me : nested.entrySet() ) {
				outputObject(me.getValue(), me.getKey(), out);
			}
			if ( close ) {
				out.writeEndElement();
			}
		}
	}

	private void writeElement(String name, Object bean, XMLStreamWriter out, boolean close)
			throws XMLStreamException {
		if ( propertySerializerRegistrar != null && bean != null ) {
			// try whole-bean serialization first
			Object o = propertySerializerRegistrar.serializeProperty(name, bean.getClass(), bean, bean);
			if ( o != bean ) {
				if ( o != null ) {
					outputObject(o, name, out);
				}
				return;
			}
		}
		Map<String, Object> props = ClassUtils.getBeanProperties(bean, null, true);
		writeElement(name, props, out, close);
	}

	/**
	 * Parse XML into a simple Map structure.
	 * 
	 * @param in
	 *        the input stream to parse
	 * @return a Map of the XML
	 */
	public Map<String, Object> parseXml(InputStream in) {
		Deque<Map<String, Object>> stack = new LinkedList<Map<String, Object>>();
		Map<String, Object> result = null;
		XMLStreamReader reader = startParse(in);
		try {
			int eventType;
			boolean parsing = true;
			while ( parsing ) {
				eventType = reader.next();
				switch (eventType) {
					case XMLStreamConstants.END_DOCUMENT:
						parsing = false;
						break;

					case XMLStreamConstants.START_ELEMENT:
						String name = reader.getLocalName();
						if ( stack.isEmpty() ) {
							result = new LinkedHashMap<String, Object>();
							stack.push(result);
						} else {
							Map<String, Object> el = new LinkedHashMap<String, Object>();
							putMapValue(stack.peek(), name, el);
							stack.push(el);
						}
						parseElement(stack.peek(), reader);
						break;

					case XMLStreamConstants.END_ELEMENT:
						stack.pop();
						break;

				}
			}
		} catch ( XMLStreamException e ) {
			throw new RuntimeException(e);
		} finally {
			endParse(reader);
		}
		return result;
	}

	private void parseElement(Map<String, Object> result, XMLStreamReader reader) {
		int attrCount = reader.getAttributeCount();
		for ( int i = 0; i < attrCount; i++ ) {
			String name = reader.getAttributeLocalName(i);
			String val = reader.getAttributeValue(i);
			putMapValue(result, name, val);
		}
	}

	@SuppressWarnings("unchecked")
	private void putMapValue(Map<String, Object> result, String name, Object val) {
		if ( result.containsKey(name) ) {
			Object existingVal = result.get(name);
			if ( existingVal instanceof List ) {
				// add to existing list
				((List<Object>) existingVal).add(val);
			} else {
				// replace existing value with list
				List<Object> list = new ArrayList<Object>();
				list.add(existingVal);
				list.add(val);
				result.put(name, list);
			}
		} else {
			result.put(name, val);
		}
	}

	private XMLStreamReader startParse(InputStream in) {
		XMLStreamReader reader = null;
		try {
			reader = XMLInputFactory.newFactory().createXMLStreamReader(in);
		} catch ( XMLStreamException e ) {
			throw new RuntimeException(e);
		} catch ( FactoryConfigurationError e ) {
			throw new RuntimeException(e);
		}
		return reader;
	}

	private void endParse(XMLStreamReader reader) {
		try {
			reader.close();
		} catch ( XMLStreamException e ) {
			// ignore this
		}
	}
}

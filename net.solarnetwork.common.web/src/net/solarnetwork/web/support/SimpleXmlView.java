/* ===================================================================
 * SimpleXmlView.java
 * 
 * Created Aug 14, 2008 5:07:50 PM
 * 
 * Copyright (c) 2008 Solarnetwork.net Dev Team.
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

package net.solarnetwork.web.support;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.solarnetwork.util.ClassUtils;

/**
 * Spring {@link org.springframework.web.servlet.View} for turning objects into
 * XML through JavaBean introspection.
 * 
 * <p>
 * The character encoding of the output must be specified in the
 * {@link #setContentType(String)} (e.g. {@literal text/xml;charset=UTF-8}).
 * </p>
 * 
 * <p>
 * The configurable properties of this class are:
 * </p>
 * 
 * <dl>
 * <dt>rootElementName</dt>
 * <dd>The name of the root XML element to use.</dd>
 * 
 * <dt>singleBeanAsRoot</dt>
 * <dd>TODO</dd>
 * 
 * <dt>useModelTimeZoneForDates</dt>
 * <dd>TODO</dd>
 * 
 * <dt>modelKey</dt>
 * <dd>TODO</dd>
 * 
 * <dt>rootElementAugmentor</dt>
 * <dd>TODO</dd>
 * 
 * <dt>classNamesAllowedForNesting</dt>
 * <dd>TODO</dd>
 * </dl>
 * 
 * @author matt
 * @version 1.1
 */
public class SimpleXmlView extends AbstractView {

	/** Default content type. */
	public static final String DEFAULT_XML_CONTENT_TYPE = "text/xml;charset=UTF-8";

	private static final ThreadLocal<SimpleDateFormat> SDF = new ThreadLocal<SimpleDateFormat>();

	private static final Pattern AMP = Pattern.compile("&(?!\\w+;)");

	private String rootElementName = "root";
	private boolean singleBeanAsRoot = true;
	private boolean useModelTimeZoneForDates = true;
	private String modelKey = null;
	private ViewResponseAugmentor rootElementAugmentor = null;
	private Set<String> classNamesAllowedForNesting = null;

	/**
	 * Constructor.
	 */
	public SimpleXmlView() {
		setContentType(DEFAULT_XML_CONTENT_TYPE);
	}

	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		Map<String, Object> finalModel = setupDateFormat(model);

		response.setContentType(getContentType());
		String charset = getResponseCharacterEncoding();
		OutputStreamWriter out;
		try {
			out = new OutputStreamWriter(response.getOutputStream(), charset);
		} catch ( UnsupportedEncodingException e ) {
			throw new RuntimeException(e);
		}

		// write XML start
		out.write("<?xml version=\"1.0\" encoding=\"");
		out.write(charset);
		out.write("\"?>\n");

		Object singleBean = finalModel.size() == 1 && this.singleBeanAsRoot ? finalModel.values()
				.iterator().next() : null;

		if ( singleBean != null ) {
			outputObject(singleBean, finalModel.keySet().iterator().next().toString(), out,
					rootElementAugmentor);
		} else {
			writeElement(this.rootElementName, null, out, false, rootElementAugmentor);

			for ( Map.Entry<String, Object> me : finalModel.entrySet() ) {
				outputObject(me.getValue(), me.getKey(), out, null);
			}

			// end root element
			closeElement(this.rootElementName, out);
		}

		out.flush();
		SDF.remove();
	}

	/**
	 * Create a {@link SimpleDateFormat} and cache on the {@link #SDF}
	 * ThreadLocal to re-use for all dates within a single response.
	 * 
	 * @param model
	 *        the model, to look for a TimeZone to format the dates in
	 */
	private Map<String, Object> setupDateFormat(Map<String, Object> model) {
		TimeZone tz = TimeZone.getTimeZone("GMT");
		Map<String, Object> result = new LinkedHashMap<String, Object>();
		for ( Map.Entry<String, Object> me : model.entrySet() ) {
			Object o = me.getValue();
			if ( useModelTimeZoneForDates && o instanceof TimeZone ) {
				tz = (TimeZone) o;
			} else if ( modelKey != null ) {
				if ( modelKey.equals(me.getKey()) ) {
					result.put(modelKey, o);
				}
			} else { //if ( !(o instanceof BindingResult) ) {
				result.put(me.getKey(), o);
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

	private void outputObject(Object o, String name, Writer out, ViewResponseAugmentor augmentor)
			throws IOException {
		if ( o instanceof Collection ) {
			Collection<?> col = (Collection<?>) o;
			outputCollection(col, name, out, augmentor);
		} else if ( o instanceof Map ) {
			Map<?, ?> map = (Map<?, ?>) o;
			outputMap(map, name, out, augmentor);
		} else if ( o instanceof String || o instanceof Number ) {
			// for simple types, write as unified <value type="String" value="foo"/>
			// this happens often in collections / maps of simple data types
			Map<String, Object> params = new LinkedHashMap<String, Object>(2);
			params.put("type", org.springframework.util.ClassUtils.getShortName(o.getClass()));
			params.put("value", o);
			writeElement("value", params, out, true, augmentor);
		} else {
			String elementName = (o == null ? name : org.springframework.util.ClassUtils.getShortName(o
					.getClass()));
			writeElement(elementName, o, out, true, augmentor);
		}
	}

	private void outputMap(Map<?, ?> map, String name, Writer out, ViewResponseAugmentor augmentor)
			throws IOException {
		writeElement(name, null, out, false, augmentor);

		// for each entry, write an <entry> element
		for ( Map.Entry<?, ?> me : map.entrySet() ) {
			String entryName = me.getKey().toString();
			out.write("<entry key=\"");
			out.write(entryName);
			out.write("\">");

			Object value = me.getValue();
			if ( value instanceof Collection ) {
				// special collection case, we don't add nested element
				for ( Object o : (Collection<?>) value ) {
					outputObject(o, "value", out, augmentor);
				}
			} else {
				outputObject(value, null, out, augmentor);
			}

			closeElement("entry", out);
		}

		closeElement(name, out);
	}

	private void outputCollection(Collection<?> col, String name, Writer out,
			ViewResponseAugmentor augmentor) throws IOException {
		writeElement(name, null, out, false, augmentor);
		for ( Object o : col ) {
			outputObject(o, null, out, null);
		}
		closeElement(name, out);
	}

	private void writeElement(String name, Map<?, ?> props, Writer out, boolean close,
			ViewResponseAugmentor augmentor) throws IOException {
		out.write('<');
		out.write(name);
		if ( augmentor != null ) {
			augmentor.augmentResponse(out);
		}
		Map<String, Object> nested = null;
		if ( props != null ) {
			for ( Map.Entry<?, ?> me : props.entrySet() ) {
				String key = me.getKey().toString();
				Object val = me.getValue();
				if ( getPropertySerializerRegistrar() != null ) {
					val = getPropertySerializerRegistrar().serializeProperty(name, val.getClass(),
							props, val);
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
					// replace & with &amp;
					String attVal = val.toString();
					Matcher matcher = AMP.matcher(attVal);
					attVal = matcher.replaceAll("&amp;");
					attVal = attVal.replace("\"", "&quot;");
					out.write(' ');
					out.write(key);
					out.write("=\"");
					out.write(attVal);
					out.write('"');
				}
			}
		}
		if ( close && nested == null ) {
			out.write('/');
		}
		out.write('>');
		if ( nested != null ) {
			for ( Map.Entry<String, Object> me : nested.entrySet() ) {
				outputObject(me.getValue(), me.getKey(), out, augmentor);
			}
			if ( close ) {
				closeElement(name, out);
			}
		}
	}

	private void writeElement(String name, Object bean, Writer out, boolean close,
			ViewResponseAugmentor augmentor) throws IOException {
		if ( getPropertySerializerRegistrar() != null && bean != null ) {
			// try whole-bean serialization first
			Object o = getPropertySerializerRegistrar().serializeProperty(name, bean.getClass(), bean,
					bean);
			if ( o != bean ) {
				if ( o != null ) {
					outputObject(o, name, out, augmentor);
				}
				return;
			}
		}
		Map<String, Object> props = ClassUtils.getBeanProperties(bean, null, true);
		writeElement(name, props, out, close, augmentor);
	}

	private void closeElement(String name, Writer out) throws IOException {
		out.write("</");
		out.write(name);
		out.write('>');
	}

	public String getRootElementName() {
		return rootElementName;
	}

	public void setRootElementName(String rootElementName) {
		this.rootElementName = rootElementName;
	}

	public boolean isSingleBeanAsRoot() {
		return singleBeanAsRoot;
	}

	public void setSingleBeanAsRoot(boolean singleBeanAsRoot) {
		this.singleBeanAsRoot = singleBeanAsRoot;
	}

	public boolean isUseModelTimeZoneForDates() {
		return useModelTimeZoneForDates;
	}

	public void setUseModelTimeZoneForDates(boolean useModelTimeZoneForDates) {
		this.useModelTimeZoneForDates = useModelTimeZoneForDates;
	}

	public String getModelKey() {
		return modelKey;
	}

	public void setModelKey(String modelKey) {
		this.modelKey = modelKey;
	}

	public ViewResponseAugmentor getRootElementAugmentor() {
		return rootElementAugmentor;
	}

	public void setRootElementAugmentor(ViewResponseAugmentor rootElementAugmentor) {
		this.rootElementAugmentor = rootElementAugmentor;
	}

	public Set<String> getClassNamesAllowedForNesting() {
		return classNamesAllowedForNesting;
	}

	public void setClassNamesAllowedForNesting(Set<String> classNamesAllowedForNesting) {
		this.classNamesAllowedForNesting = classNamesAllowedForNesting;
	}

}

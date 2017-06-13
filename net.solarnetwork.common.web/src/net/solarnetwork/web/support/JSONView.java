/* ===================================================================
 * JSONView.java
 * 
 * Created Jan 3, 2007 12:20:21 PM
 * 
 * Copyright (c) 2007 Matt Magoffin (spamsqr@msqr.us)
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
 * $Id$
 * ===================================================================
 */

package net.solarnetwork.web.support;

import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.PropertyEditorRegistrar;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import net.solarnetwork.util.SerializeIgnore;

/**
 * View to return JSON encoded data.
 * 
 * <p>
 * The view model is turned into a complete JSON object. The model keys become
 * JSON object keys, and the model values the corresponding JSON object values.
 * Array and Collection object values will be rendered as JSON array values.
 * Primitive types will render as JSOM primitive values (numbers, strings).
 * Objects will be treated as JavaBeans and the bean properties will be used to
 * render nested JSON objects.
 * </p>
 * 
 * <p>
 * All object values are handled in a recursive fashion, so array, collection,
 * and bean property values will be rendered accordingly.
 * </p>
 * 
 * <p>
 * The JSON encoding is constructed in a streaming fashion, so object graphs of
 * arbitrary size should not cause any memory-related errors.
 * </p>
 * 
 * <p>
 * The configurable properties of this class are:
 * </p>
 * 
 * <dl>
 * <dt>indentAmount</dt>
 * <dd>The number of spaces to indent (pretty print) the JSON output with. If
 * set to zero no indentation will be added (this is the default).</dd>
 * 
 * <dt>includeParentheses</dt>
 * <dd>If true, the entire response will be enclosed in parentheses, required
 * for JSON evaluation support in certain browsers. Defaults to
 * <em>false</em>.</dd>
 * 
 * <dt>propertyEditorRegistrar</dt>
 * <dd>An optional registrar of PropertyEditor instances that can be used to
 * serialize specific objects into String values. This can be useful for
 * formatting Date objects into strings, for example.</dd>
 * 
 * </dl>
 * 
 * @author Matt Magoffin
 * @version $Revision$ $Date$
 */
public class JSONView extends AbstractView {

	/** The default content type: application/json;charset=UTF-8. */
	public static final String JSON_CONTENT_TYPE = "application/json;charset=UTF-8";

	/** The default character encoding used: UTF-8. */
	public static final String UTF8_CHAR_ENCODING = "UTF-8";

	private int indentAmount = 0;
	private boolean includeParentheses = false;
	private PropertyEditorRegistrar propertyEditorRegistrar = null;

	/**
	 * Default constructor.
	 */
	public JSONView() {
		setContentType(JSON_CONTENT_TYPE);
	}

	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		PropertyEditorRegistrar registrar = this.propertyEditorRegistrar;
		Enumeration<String> attrEnum = request.getAttributeNames();
		while ( attrEnum.hasMoreElements() ) {
			String key = attrEnum.nextElement();
			Object val = request.getAttribute(key);
			if ( val instanceof PropertyEditorRegistrar ) {
				registrar = (PropertyEditorRegistrar) val;
				break;
			}
		}

		response.setCharacterEncoding(UTF8_CHAR_ENCODING);
		response.setContentType(getContentType());
		Writer writer = response.getWriter();
		if ( this.includeParentheses ) {
			writer.write('(');
		}
		JsonGenerator json = new JsonFactory().createGenerator(writer);
		json.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
		if ( indentAmount > 0 ) {
			json.useDefaultPrettyPrinter();
		}
		json.writeStartObject();
		for ( String key : model.keySet() ) {
			Object val = model.get(key);
			writeJsonValue(json, key, val, registrar);
		}
		json.writeEndObject();
		json.close();
		if ( this.includeParentheses ) {
			writer.write(')');
		}
	}

	private Collection<?> getPrimitiveCollection(Object array) {
		int len = Array.getLength(array);
		List<Object> result = new ArrayList<Object>(len);
		for ( int i = 0; i < len; i++ ) {
			result.add(Array.get(array, i));
		}
		return result;
	}

	private void writeJsonValue(JsonGenerator json, String key, Object val,
			PropertyEditorRegistrar registrar) throws JsonGenerationException, IOException {
		if ( val instanceof Collection<?> || (val != null && val.getClass().isArray()) ) {
			Collection<?> col;
			if ( val instanceof Collection<?> ) {
				col = (Collection<?>) val;
			} else if ( !val.getClass().getComponentType().isPrimitive() ) {
				col = Arrays.asList((Object[]) val);
			} else {
				// damn you, primitives
				col = getPrimitiveCollection(val);
			}
			if ( key != null ) {
				json.writeFieldName(key);
			}
			json.writeStartArray();
			for ( Object colObj : col ) {
				writeJsonValue(json, null, colObj, registrar);
			}

			json.writeEndArray();
		} else if ( val instanceof Map<?, ?> ) {
			if ( key != null ) {
				json.writeFieldName(key);
			}
			json.writeStartObject();
			for ( Map.Entry<?, ?> me : ((Map<?, ?>) val).entrySet() ) {
				Object propName = me.getKey();
				if ( propName == null ) {
					continue;
				}
				writeJsonValue(json, propName.toString(), me.getValue(), registrar);
			}
			json.writeEndObject();
		} else if ( val instanceof Double ) {
			if ( key == null ) {
				json.writeNumber((Double) val);
			} else {
				json.writeNumberField(key, (Double) val);
			}
		} else if ( val instanceof Integer ) {
			if ( key == null ) {
				json.writeNumber((Integer) val);
			} else {
				json.writeNumberField(key, (Integer) val);
			}
		} else if ( val instanceof Short ) {
			if ( key == null ) {
				json.writeNumber(((Short) val).intValue());
			} else {
				json.writeNumberField(key, ((Short) val).intValue());
			}
		} else if ( val instanceof Float ) {
			if ( key == null ) {
				json.writeNumber((Float) val);
			} else {
				json.writeNumberField(key, (Float) val);
			}
		} else if ( val instanceof Long ) {
			if ( key == null ) {
				json.writeNumber((Long) val);
			} else {
				json.writeNumberField(key, (Long) val);
			}
		} else if ( val instanceof BigDecimal ) {
			if ( key == null ) {
				json.writeNumber((BigDecimal) val);
			} else {
				json.writeNumberField(key, (BigDecimal) val);
			}
		} else if ( val instanceof BigInteger ) {
			if ( key == null ) {
				json.writeNumber((BigInteger) val);
			} else {
				json.writeNumberField(key, new BigDecimal((BigInteger) val));
			}
		} else if ( val instanceof Boolean ) {
			if ( key == null ) {
				json.writeBoolean((Boolean) val);
			} else {
				json.writeBooleanField(key, (Boolean) val);
			}
		} else if ( val instanceof String ) {
			if ( key == null ) {
				json.writeString((String) val);
			} else {
				json.writeStringField(key, (String) val);
			}
		} else {
			// create a JSON object from bean properties
			if ( getPropertySerializerRegistrar() != null && val != null ) {
				// try whole-bean serialization first
				Object o = getPropertySerializerRegistrar().serializeProperty(key, val.getClass(), val,
						val);
				if ( o != val ) {
					if ( o != null ) {
						writeJsonValue(json, key, o, registrar);
					}
					return;
				}
			}
			generateJavaBeanObject(json, key, val, registrar);
		}
	}

	private void generateJavaBeanObject(JsonGenerator json, String key, Object bean,
			PropertyEditorRegistrar registrar) throws JsonGenerationException, IOException {
		if ( key != null ) {
			json.writeFieldName(key);
		}
		if ( bean == null ) {
			json.writeNull();
			return;
		}
		BeanWrapper wrapper = getPropertyAccessor(bean, registrar);
		PropertyDescriptor[] props = wrapper.getPropertyDescriptors();
		json.writeStartObject();
		for ( PropertyDescriptor prop : props ) {
			String name = prop.getName();
			if ( this.getJavaBeanIgnoreProperties() != null
					&& this.getJavaBeanIgnoreProperties().contains(name) ) {
				continue;
			}
			if ( wrapper.isReadableProperty(name) ) {
				Object propVal = wrapper.getPropertyValue(name);
				if ( propVal != null ) {

					// test for SerializeIgnore
					Method getter = prop.getReadMethod();
					if ( getter != null && getter.isAnnotationPresent(SerializeIgnore.class) ) {
						continue;
					}

					if ( getPropertySerializerRegistrar() != null ) {
						propVal = getPropertySerializerRegistrar().serializeProperty(name,
								propVal.getClass(), bean, propVal);
					} else {
						// Spring does not apply PropertyEditors on read methods, so manually handle
						PropertyEditor editor = wrapper.findCustomEditor(null, name);
						if ( editor != null ) {
							editor.setValue(propVal);
							propVal = editor.getAsText();
						}
					}
					if ( propVal instanceof Enum<?> || getJavaBeanTreatAsStringValues() != null
							&& getJavaBeanTreatAsStringValues().contains(propVal.getClass()) ) {
						propVal = propVal.toString();
					}
					writeJsonValue(json, name, propVal, registrar);
				}
			}
		}
		json.writeEndObject();
	}

	private BeanWrapper getPropertyAccessor(Object obj, PropertyEditorRegistrar registrar) {
		BeanWrapper bean = PropertyAccessorFactory.forBeanPropertyAccess(obj);
		if ( registrar != null ) {
			registrar.registerCustomEditors(bean);
		}
		return bean;
	}

	public int getIndentAmount() {
		return indentAmount;
	}

	public void setIndentAmount(int indentAmount) {
		this.indentAmount = indentAmount;
	}

	public boolean isIncludeParentheses() {
		return includeParentheses;
	}

	public void setIncludeParentheses(boolean includeParentheses) {
		this.includeParentheses = includeParentheses;
	}

	public PropertyEditorRegistrar getPropertyEditorRegistrar() {
		return propertyEditorRegistrar;
	}

	public void setPropertyEditorRegistrar(PropertyEditorRegistrar propertyEditorRegistrar) {
		this.propertyEditorRegistrar = propertyEditorRegistrar;
	}

}

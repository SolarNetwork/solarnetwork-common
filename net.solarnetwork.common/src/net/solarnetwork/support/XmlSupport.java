/* ===================================================================
 * XmlSupport.java
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

package net.solarnetwork.support;

import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessor;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A class to support services that use XML.
 * 
 * <p>
 * The configurable properties of this class are:
 * </p>
 * 
 * <dl class="class-properties">
 * <dt>docBuilderFactory</dt>
 * <dd>A JAXP {@link DocumentBuilderFactory} to use. If not configured, the
 * {@link DocumentBuilderFactory#newInstance()} method will be used to create a
 * default one.</p>
 * 
 * <dt>transformerFactory</dt>
 * <dd>A JAXP {@link TransformerFactory} for handling XSLT transformations with.
 * If not configured, the {@link TransformerFactory#newInstance()} method will
 * be used to create a default one.</p>
 * 
 * <dt>xpathFactory</dt>
 * <dd>A JAXP {@link XPathFactory} for handling XPath operations with. If not
 * configured the {@link XPathFactory#newInstance()} method will be used to
 * create a default one.</dd>
 * 
 * <dt>nsContext</dt>
 * <dd>An optional {@link NamespaceContext} to use for proper XML namespace
 * handling in some contexts, such as XPath.</dd>
 * </dl>
 * 
 * @author matt
 * @version 1.2
 */
public class XmlSupport {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private NamespaceContext nsContext = null;
	private DocumentBuilderFactory docBuilderFactory = null;
	private XPathFactory xpathFactory = null;
	private TransformerFactory transformerFactory = null;

	/**
	 * Compile XPathExpression mappings from String XPath expressions.
	 * 
	 * @param xpathMap
	 *        the XPath string expressions
	 * @return the XPathExperssion mapping
	 */
	public Map<String, XPathExpression> getXPathExpressionMap(Map<String, String> xpathMap) {
		Map<String, XPathExpression> datumXPathMap = new LinkedHashMap<String, XPathExpression>();
		for ( Map.Entry<String, String> me : xpathMap.entrySet() ) {
			try {
				datumXPathMap.put(me.getKey(), getXPathExpression(me.getValue()));
			} catch ( XPathExpressionException e ) {
				throw new RuntimeException(e);
			}
		}
		return datumXPathMap;
	}

	/**
	 * Compile a single XPathExpression from a String XPath expression. The
	 * {@link #getNsContext()} will be configured on the resulting
	 * XPathExpression, if available.
	 * 
	 * @param xpath
	 *        the XPath to compile
	 * @return the compiled XPath
	 * @throws XPathExpressionException
	 *         if the XPath cannot be compiled
	 */
	public XPathExpression getXPathExpression(String xpath) throws XPathExpressionException {
		XPath xp = getXpathFactory().newXPath();
		if ( getNsContext() != null ) {
			xp.setNamespaceContext(getNsContext());
		}
		return xp.compile(xpath);
	}

	/**
	 * Get an XSLT Templates object from an XSLT Resource.
	 * 
	 * @param resource
	 *        the XSLT Resource to load
	 * @return the compiled Templates
	 */
	public Templates getTemplates(Resource resource) {
		TransformerFactory tf = TransformerFactory.newInstance();
		try {
			return tf.newTemplates(new StreamSource(resource.getInputStream()));
		} catch ( TransformerConfigurationException e ) {
			throw new RuntimeException("Unable to load XSLT from resource [" + resource + ']');
		} catch ( IOException e ) {
			throw new RuntimeException("Unable to load XSLT from resource [" + resource + ']');
		}
	}

	/**
	 * Turn an object into a simple XML Document, supporting custom property
	 * editors.
	 * 
	 * <p>
	 * The returned XML will be a document with a single element with all
	 * JavaBean properties turned into attributes. For example:
	 * <p>
	 * 
	 * <pre>
	 * &lt;powerDatum
	 *   id="123"
	 *   pvVolts="123.123"
	 *   ... /&gt;
	 * </pre>
	 * 
	 * <p>
	 * {@link PropertyEditor} instances can be registered with the supplied
	 * {@link BeanWrapper} for custom handling of properties, e.g. dates.
	 * </p>
	 * 
	 * @param bean
	 *        the object to turn into XML
	 * @param elementName
	 *        the name of the XML element
	 * @return the element, as an XML DOM Document
	 */
	public Document getDocument(BeanWrapper bean, String elementName) {
		Document dom = null;
		try {
			dom = getDocBuilderFactory().newDocumentBuilder().newDocument();
			dom.appendChild(getElement(bean, elementName, dom));
		} catch ( ParserConfigurationException e ) {
			throw new RuntimeException(e);
		}
		return dom;
	}

	/**
	 * Turn an object into a simple XML Element, supporting custom property
	 * editors.
	 * 
	 * <p>
	 * The returned XML will be a single element with all JavaBean properties
	 * turned into attributes and the element named after the bean object's
	 * class name. For example:
	 * <p>
	 * 
	 * <pre>
	 * &lt;PowerDatum
	 *   id="123"
	 *   pvVolts="123.123"
	 *   ... /&gt;
	 * </pre>
	 * 
	 * <p>
	 * {@link PropertyEditor} instances can be registered with the supplied
	 * {@link BeanWrapper} for custom handling of properties, e.g. dates.
	 * </p>
	 * 
	 * @param bean
	 *        the object to turn into XML
	 * @return the element, as an XML DOM Document
	 */
	public Element getElement(BeanWrapper bean, Document dom) {
		String elementName = bean.getWrappedInstance().getClass().getSimpleName();
		return getElement(bean, elementName, dom);
	}

	/**
	 * Turn an object into a simple XML Element, supporting custom property
	 * editors.
	 * 
	 * <p>
	 * The returned XML will be a single element with all JavaBean properties
	 * turned into attributes. For example:
	 * <p>
	 * 
	 * <pre>
	 * &lt;powerDatum
	 *   id="123"
	 *   pvVolts="123.123"
	 *   ... /&gt;
	 * </pre>
	 * 
	 * <p>
	 * {@link PropertyEditor} instances can be registered with the supplied
	 * {@link BeanWrapper} for custom handling of properties, e.g. dates.
	 * </p>
	 * 
	 * @param bean
	 *        the object to turn into XML
	 * @param elementName
	 *        the name of the XML element
	 * @return the element, as an XML DOM Element
	 */
	public Element getElement(BeanWrapper bean, String elementName, Document dom) {
		PropertyDescriptor[] props = bean.getPropertyDescriptors();
		Element root = null;
		root = dom.createElement(elementName);
		for ( int i = 0; i < props.length; i++ ) {
			PropertyDescriptor prop = props[i];
			if ( prop.getReadMethod() == null ) {
				continue;
			}
			String propName = prop.getName();
			if ( "class".equals(propName) ) {
				continue;
			}
			Object propValue = null;
			PropertyEditor editor = bean.findCustomEditor(prop.getPropertyType(), prop.getName());
			if ( editor != null ) {
				editor.setValue(bean.getPropertyValue(propName));
				propValue = editor.getAsText();
			} else {
				propValue = bean.getPropertyValue(propName);
			}
			if ( propValue == null ) {
				continue;
			}
			if ( log.isTraceEnabled() ) {
				log.trace("attribute name: " + propName + " attribute value: " + propValue);
			}
			root.setAttribute(propName, propValue.toString());
		}
		return root;
	}

	/**
	 * Turn an object into a simple XML Document, supporting custom property
	 * editors.
	 * 
	 * <p>
	 * The returned XML will be a single element with all JavaBean properties
	 * turned into attributed. For example:
	 * <p>
	 * 
	 * <pre>
	 * &lt;powerDatum
	 *   id="123"
	 *   pvVolts="123.123"
	 *   ... /&gt;
	 * </pre>
	 * 
	 * @param bean
	 *        the object to turn into XML
	 * @param elementName
	 *        the name of the XML element
	 * @return the element, as XSLT Source
	 * @see #getDocument(BeanWrapper, String)
	 */
	public Source getSource(BeanWrapper bean, String elementName) {
		Document dom = getDocument(bean, elementName);
		return getSource(dom);
	}

	/**
	 * Turn a Document into a Source.
	 * 
	 * <p>
	 * This method will log the XML document at the FINEST level.
	 * </p>
	 * 
	 * @param dom
	 *        the Document to turn into XSLT source
	 * @return the document, as XSLT Source
	 */
	public Source getSource(Document dom) {
		DOMSource result = new DOMSource(dom);
		if ( log.isDebugEnabled() ) {
			log.debug("XML: " + getXmlAsString(result, true));
		}
		return result;
	}

	/**
	 * Turn an XML Source into a String.
	 * 
	 * @param source
	 *        the XML Source
	 * @param indent
	 *        if <em>true</em> then indent the result
	 * @return the XML, as a String
	 */
	public String getXmlAsString(Source source, boolean indent) {
		ByteArrayOutputStream byos = new ByteArrayOutputStream();
		try {
			Transformer xform = getTransformerFactory().newTransformer();
			if ( indent ) {
				xform.setOutputProperty(OutputKeys.INDENT, "yes");
				xform.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			}
			xform.transform(source, new StreamResult(byos));
		} catch ( TransformerConfigurationException e ) {
			throw new RuntimeException(e);
		} catch ( TransformerException e ) {
			throw new RuntimeException(e);
		}
		return byos.toString();
	}

	/**
	 * Populate JavaBean properties via XPath extraction.
	 * 
	 * @param obj
	 *        the object to set properties on, or a BeanWrapper
	 * @param xml
	 *        the XML
	 * @param xpathMap
	 *        the mapping of JavaBean property names to XPaths
	 */
	public void extractBeanDataFromXml(PropertyAccessor bean, Node xml,
			Map<String, XPathExpression> xpathMap) {
		for ( Map.Entry<String, XPathExpression> me : xpathMap.entrySet() ) {
			String val = extractStringFromXml(xml, me.getValue());
			if ( val != null && !"".equals(val) ) {
				bean.setPropertyValue(me.getKey(), val);
			}
		}
	}

	/**
	 * Extract a String value via an XPath expression.
	 * 
	 * @param xml
	 *        the XML
	 * @param xpath
	 *        the XPath expression
	 * @return the String
	 */
	public String extractStringFromXml(Node xml, XPathExpression xpath) {
		try {
			return (String) xpath.evaluate(xml, XPathConstants.STRING);
		} catch ( XPathExpressionException e ) {
			throw new RuntimeException("Unable to extract string from XPath [" + xpath.toString() + "]",
					e);
		}
	}

	public NamespaceContext getNsContext() {
		return nsContext;
	}

	public void setNsContext(NamespaceContext nsContext) {
		this.nsContext = nsContext;
	}

	public DocumentBuilderFactory getDocBuilderFactory() {
		DocumentBuilderFactory result = this.docBuilderFactory;
		if ( result == null ) {
			result = DocumentBuilderFactory.newInstance();
			result.setNamespaceAware(true);
			this.docBuilderFactory = result;
		}
		return result;
	}

	public void setDocBuilderFactory(DocumentBuilderFactory docBuilderFactory) {
		this.docBuilderFactory = docBuilderFactory;
	}

	public XPathFactory getXpathFactory() {
		XPathFactory result = this.xpathFactory;
		if ( result == null ) {
			// work around Oracle JDK issues loading XPathFactory, see
			// https://java.net/projects/glassfish/lists/users/archive/2012-02/message/371
			final ClassLoader origClassLoader = Thread.currentThread().getContextClassLoader();
			final ClassLoader newClassLoader = XPathFactory.class.getClassLoader();
			if ( newClassLoader != null ) {
				Thread.currentThread().setContextClassLoader(newClassLoader);
			}
			try {
				result = XPathFactory.newInstance();
				this.xpathFactory = result;
			} finally {
				if ( newClassLoader != null ) {
					Thread.currentThread().setContextClassLoader(origClassLoader);
				}
			}
		}
		return result;
	}

	public void setXpathFactory(XPathFactory xpathFactory) {
		this.xpathFactory = xpathFactory;
	}

	public TransformerFactory getTransformerFactory() {
		TransformerFactory result = this.transformerFactory;
		if ( result == null ) {
			// work around Oracle JDK issues loading XPathFactory, see
			// https://java.net/projects/glassfish/lists/users/archive/2012-02/message/371
			final ClassLoader origClassLoader = Thread.currentThread().getContextClassLoader();
			Thread.currentThread().setContextClassLoader(XPathFactory.class.getClassLoader());
			try {
				result = TransformerFactory.newInstance();
				this.transformerFactory = result;
			} finally {
				Thread.currentThread().setContextClassLoader(origClassLoader);
			}
		}
		return result;
	}

	public void setTransformerFactory(TransformerFactory transformerFactory) {
		this.transformerFactory = transformerFactory;
	}

}

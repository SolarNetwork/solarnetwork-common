/* ==================================================================
 * SchemaValidationHelper.java - 3/02/2020 1:34:47 pm
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.MarshalException;
import jakarta.xml.bind.Marshaller;
import net.solarnetwork.ocpp.domain.SchemaValidationException;

/**
 * XML Schema validation helper.
 * 
 * @author matt
 * @version 1.0
 */
public class SchemaValidationHelper {

	private final Schema schema;

	/**
	 * Extract an XML schema that is embedded in a WSDL document.
	 * 
	 * <p>
	 * Adapted from https://stackoverflow.com/a/51132383.
	 * </p>
	 * 
	 * @param wsdlResource
	 *        the WSDL classpath resource to parse
	 * @param classLoader
	 *        the class loader to use for locating the resource
	 * @return the extracted schema
	 * @throws RuntimeException
	 *         if there is any error processing the WSDL
	 */
	public static Schema schemaFromWsdl(String wsdlResource, ClassLoader classLoader) {
		DocumentBuilder dBuilder;
		Document wsdlDoc;
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			dbFactory.setNamespaceAware(true);
			dBuilder = dbFactory.newDocumentBuilder();
			wsdlDoc = dBuilder.parse(classLoader.getResourceAsStream(wsdlResource));
		} catch ( SAXException | IOException | ParserConfigurationException e ) {
			throw new RuntimeException(
					"Error parsing WSDL resource [" + wsdlResource + "]: " + e.toString(), e);
		}

		// read namespace declarations from WSDL document, in case they are referred from a schema
		NamedNodeMap attributes = wsdlDoc.getDocumentElement().getAttributes();
		Map<String, String> namespacesFromWsdlDocument = new HashMap<>();
		for ( int i = 0; i < attributes.getLength(); i++ ) {
			Node n = attributes.item(i);
			if ( n.getNamespaceURI() != null
					&& n.getNamespaceURI().equals("http://www.w3.org/2000/xmlns/") ) {
				namespacesFromWsdlDocument.put(n.getLocalName(), n.getNodeValue());
			}
		}

		// read the schema nodes from the WSDL
		NodeList schemas = wsdlDoc.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema", "schema");
		Map<String, DOMSource> sources = new HashMap<>();
		for ( int i = 0; i < schemas.getLength(); i++ ) {
			// create a document for each schema and copy the source schema
			Document schema = dBuilder.newDocument();
			Element schemaElement = (Element) schema.importNode(schemas.item(i), true);

			// add all non-existing namespace declarations from the wsdl node
			String targetNs = schemaElement.getAttribute("targetNamespace");
			for ( Map.Entry<String, String> ns : namespacesFromWsdlDocument.entrySet() ) {
				String name = ns.getKey();
				String value = ns.getValue();
				if ( schemaElement.getAttributeNodeNS("http://www.w3.org/2000/xmlns/", name) == null ) {
					schemaElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:" + name,
							value);
				}
			}

			// map schemas by their target namespace
			schema.appendChild(schemaElement);
			DOMSource domSource = new DOMSource(schema);
			sources.put(targetNs, domSource);
		}

		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

		// Create a ResourceResolver that can find the correct schema from the map
		DOMImplementationRegistry registry;
		try {
			registry = DOMImplementationRegistry.newInstance();
		} catch ( ClassNotFoundException | InstantiationException | IllegalAccessException
				| ClassCastException e ) {
			throw new RuntimeException(
					"Error creating DOM registry when extracting schema from WSDL resource ["
							+ wsdlResource + "]: " + e.toString(),
					e);
		}

		final DOMImplementationLS domImplementationLS = (DOMImplementationLS) registry
				.getDOMImplementation("LS");
		factory.setResourceResolver(new LSResourceResolver() {

			@Override
			public LSInput resolveResource(String type, String namespaceURI, String publicId,
					String systemId, String baseURI) {
				Source xmlSource = sources.get(namespaceURI);
				if ( xmlSource != null ) {
					LSInput input = domImplementationLS.createLSInput();
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
					Result outputTarget = new StreamResult(outputStream);
					try {
						TransformerFactory.newInstance().newTransformer().transform(xmlSource,
								outputTarget);
					} catch ( TransformerException e ) {
						e.printStackTrace();
					}
					InputStream is = new ByteArrayInputStream(outputStream.toByteArray());
					input.setByteStream(is);
					input.setSystemId(systemId);
					return input;
				} else {
					return null;
				}
			}
		});

		// create the schema object from the sources
		try {
			return factory.newSchema(sources.values().toArray(new DOMSource[sources.size()]));
		} catch ( SAXException e ) {
			throw new RuntimeException(
					"Error parsing schema from WSDL resource [" + wsdlResource + "]: " + e.toString(),
					e);
		}
	}

	/**
	 * Constructor.
	 * 
	 * @param schemaResource
	 *        a classpath resource schema to load
	 * @param classLoader
	 *        the class loader to load the schema resource with
	 */
	public SchemaValidationHelper(String schemaResource, ClassLoader classLoader) {
		super();
		if ( schemaResource == null || schemaResource.isEmpty() ) {
			throw new IllegalArgumentException("The schemaResource parameter must not be null.");
		}
		if ( classLoader == null ) {
			throw new IllegalArgumentException("The classLoader parameter must not be null.");
		}
		SchemaFactory f = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		try {
			schema = f.newSchema(new StreamSource(classLoader.getResourceAsStream(schemaResource)));
		} catch ( SAXException e ) {
			throw new RuntimeException(
					"Error parsing XML schema " + schemaResource + ": " + e.getMessage(), e);
		}
	}

	/**
	 * Constructor.
	 * 
	 * @param schema
	 *        the schema to use for validation
	 */
	public SchemaValidationHelper(Schema schema) {
		super();
		if ( schema == null ) {
			throw new IllegalArgumentException("The schema parameter must not be null.");
		}
		this.schema = schema;
	}

	/**
	 * Validate a JAXB object.
	 * 
	 * <p>
	 * Note if the object is not annotated with
	 * {@code jakarta.xml.bind.annotation.XmlRootElement} then it must be
	 * wrapped in a {@link jakarta.xml.bind.JAXBElement} instance. Often this
	 * can be accomplished via methods in the {@code ObjectFactory} class
	 * created by the JAXB generator.
	 * </p>
	 * 
	 * @param jaxbContext
	 *        the context the JAXB object is from
	 * @param object
	 *        the object to validate
	 * @throws SchemaValidationException
	 *         if validation fails, with {@code object} set as
	 *         {@link SchemaValidationException#getSource()}
	 */
	public void validate(JAXBContext jaxbContext, Object object) {
		try {
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setSchema(schema);
			marshaller.marshal(object, new DefaultHandler());
		} catch ( MarshalException e ) {
			Throwable root = e;
			while ( root.getCause() != null ) {
				root = root.getCause();
			}
			throw new SchemaValidationException(object, root.getMessage(), root);
		} catch ( JAXBException e ) {
			throw new RuntimeException(e);
		}
	}

}

/* ==================================================================
 * BaseActionPayloadDecoder.java - 3/02/2020 3:42:44 pm
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

package net.solarnetwork.ocpp.v16.jakarta.json;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationModule;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.annotation.XmlRegistry;
import net.solarnetwork.ocpp.json.ActionPayloadDecoder;
import net.solarnetwork.ocpp.xml.jakarta.support.JaxbUtils;
import net.solarnetwork.ocpp.xml.jakarta.support.SchemaValidationHelper;

/**
 * Abstract base implementation of {@link ActionPayloadDecoder} with JAXB schema
 * validation support.
 * 
 * @author matt
 * @version 1.0
 */
public abstract class BaseActionPayloadDecoder implements ActionPayloadDecoder {

	/** The JSON object mapper. */
	protected final ObjectMapper mapper;

	/** The schema validation helper. */
	protected final SchemaValidationHelper schemaHelper;

	/** The JAXB context. */
	protected final JAXBContext jaxbContext;

	/**
	 * Get a default {@link ObjectMapper} instance.
	 * 
	 * @return a default mapper
	 */
	public static ObjectMapper defaultObjectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JakartaXmlBindAnnotationModule());
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		mapper.setDateFormat(new StdDateFormat().withColonInTimeZone(true));
		mapper.setSerializationInclusion(Include.NON_EMPTY);
		return mapper;
	}

	/**
	 * Constructor.
	 * 
	 * @param mapper
	 *        the object mapper to use
	 * @param jaxbRegistry
	 *        the {@link XmlRegistry} annotated class to create a new
	 *        {@link JAXBContext} for
	 * @param wsdlResource
	 *        the path to the WSDL resource with the XML Schema to use for
	 *        validation
	 * @param classLoader
	 *        the class loader to use
	 * @see JaxbUtils#jaxbContextForRegistry(Class)
	 */
	public BaseActionPayloadDecoder(ObjectMapper mapper, Class<?> jaxbRegistry, String wsdlResource,
			ClassLoader classLoader) {
		super();
		if ( mapper == null ) {
			throw new IllegalArgumentException("The mapper parameter must not be null.");
		}
		this.mapper = mapper;
		try {
			this.jaxbContext = JaxbUtils.jaxbContextForRegistry(jaxbRegistry);
		} catch ( JAXBException e ) {
			throw new RuntimeException("Error setting up JAXBContext for message validation.", e);
		}
		this.schemaHelper = new SchemaValidationHelper(
				SchemaValidationHelper.schemaFromWsdl(wsdlResource, classLoader));
	}

}

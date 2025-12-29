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
 */

package net.solarnetwork.web.jakarta.support;

import java.util.Map;
import org.springframework.http.server.ServletServerHttpResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.solarnetwork.util.ObjectUtils;

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
 * As of version 1.2 this class delegates all CSV generation to an internal
 * instance of the {@link SimpleCsvHttpMessageConverter} class. Use the
 * {@link #setConverter(SimpleCsvHttpMessageConverter)} method to provide a
 * custom instance.
 * </p>
 *
 * @author matt
 * @version 1.2
 */
public class SimpleCsvView extends AbstractView {

	/** Default content type. */
	public static final String DEFAULT_CSV_CONTENT_TYPE = "text/csv;charset=UTF-8";

	/** The default value for the {@code dataModelKey} property. */
	public static final String DEFAULT_DATA_MODEL_KEY = "data";

	/** The default value for the {@code fieldOrderKey} property. */
	public static final String DEFAULT_FIELD_ORDER_KEY = "fieldOrder";

	private SimpleCsvHttpMessageConverter converter = new SimpleCsvHttpMessageConverter();
	private String dataModelKey = DEFAULT_DATA_MODEL_KEY;

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

		final Object data = (dataModelKey != null && model.containsKey(dataModelKey)
				? model.get(dataModelKey)
				: model);

		if ( data == null ) {
			return;
		}

		converter.write(data, converter.getSupportedMediaTypes().get(0),
				new ServletServerHttpResponse(response));

	}

	/**
	 * Get the data model key.
	 *
	 * @return the key to set
	 */
	public String getDataModelKey() {
		return dataModelKey;
	}

	/**
	 * Set the data model key.
	 *
	 * <p>
	 * If not {@literal null}, then use this model key as the data object to
	 * render as CSV. Otherwise, export just the first available key's
	 * associated object. Defaults to {@link #DEFAULT_DATA_MODEL_KEY}.
	 * </p>
	 *
	 * @param dataModelKey
	 *        the key to set
	 */
	public void setDataModelKey(String dataModelKey) {
		this.dataModelKey = dataModelKey;
	}

	/**
	 * Get the field order key.
	 *
	 * @return the key to set
	 * @deprecated this value is no longer supported
	 */
	@Deprecated
	public String getFieldOrderKey() {
		return null;
	}

	/**
	 * Set the field order key.
	 *
	 * @param fieldOrderKey
	 *        the key to set
	 * @deprecated this value is no longer supported
	 */
	@Deprecated
	public void setFieldOrderKey(String fieldOrderKey) {
		// ignored
	}

	/**
	 * Get the CSV message converter.
	 *
	 * @return the converter, never {@code null}
	 * @since 1.2
	 */
	public SimpleCsvHttpMessageConverter getConverter() {
		return converter;
	}

	/**
	 * Set the CSV message converter.
	 *
	 * @param converter
	 *        the converter to set
	 * @throws IllegalArgumentException
	 *         if any argument is {@code null}
	 * @since 1.2
	 */
	public void setConverter(SimpleCsvHttpMessageConverter converter) {
		this.converter = ObjectUtils.requireNonNullArgument(converter, "converter");
	}

}

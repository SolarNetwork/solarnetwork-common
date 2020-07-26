/* ==================================================================
 * ST4TemplateRenderer.java - 25/07/2020 4:07:33 PM
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

package net.solarnetwork.common.tmpl.st4;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.stringtemplate.v4.AutoIndentWriter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STWriter;
import net.solarnetwork.domain.BasicIdentity;
import net.solarnetwork.support.TemplateRenderer;

/**
 * StringTemplate 4 implementation of {@link TemplateRenderer}.
 * 
 * @author matt
 * @version 1.0
 */
public class ST4TemplateRenderer extends BasicIdentity<String> implements TemplateRenderer {

	/** The UTF-8 character set. */
	public static final Charset UTF8 = Charset.forName("UTF-8");

	/** List of just the HTML MIME type. */
	public static final List<MimeType> HTML = unmodifiableList(asList(MimeTypeUtils.TEXT_HTML));

	/** List of just the text MIME type. */
	public static final List<MimeType> TEXT = unmodifiableList(asList(MimeTypeUtils.TEXT_PLAIN));

	private final STGroup group;
	private final String templateName;
	private final List<MimeType> mimeTypes;
	private final Charset charset;

	/**
	 * Create a renderer for HTML using UTF-8 encoding.
	 * 
	 * @param id
	 *        the identifier
	 * @param group
	 *        the template group to use
	 * @param templateName
	 *        the entry point template name to execute
	 * @return the new instance
	 */
	public static ST4TemplateRenderer html(String id, STGroup group, String templateName) {
		return new ST4TemplateRenderer(id, group, templateName, HTML, UTF8);
	}

	/**
	 * Constructor. v *
	 * 
	 * @param id
	 *        the identifier
	 * @param group
	 *        the template group to use
	 * @param templateName
	 *        the entry point template name to execute
	 * @param mimeTypes
	 *        the supported MIME types
	 * @param charset
	 *        the character set to use; if {@literal null} then {@literal UTF-8}
	 *        will be assumed
	 */
	public ST4TemplateRenderer(String id, STGroup group, String templateName, List<MimeType> mimeTypes,
			Charset charset) {
		super(id);
		this.group = group;
		this.templateName = templateName;
		this.mimeTypes = mimeTypes;
		this.charset = (charset != null ? charset : Charset.forName("UTF-8"));
	}

	@Override
	public boolean supportsMimeType(MimeType mimeType) {
		for ( MimeType type : mimeTypes ) {
			if ( type.isCompatibleWith(mimeType) ) {
				return true;
			}
		}
		return false;
	}

	@Override
	public List<MimeType> supportedMimeTypes() {
		return mimeTypes;
	}

	@Override
	public void render(Locale locale, MimeType mimeType, Map<String, ?> parameters, OutputStream out)
			throws IOException {
		ST st = group.getInstanceOf(templateName);
		if ( st == null ) {
			String msg = String.format("Template %s not available in group %s.", templateName, group);
			throw new RuntimeException(msg);
		}
		if ( parameters != null ) {
			for ( Map.Entry<String, ?> me : parameters.entrySet() ) {
				st.add(me.getKey(), me.getValue());
			}
		}
		try (Writer w = new OutputStreamWriter(out, charset)) {
			STWriter stWriter = new AutoIndentWriter(w);
			st.write(stWriter);
		}
	}

}

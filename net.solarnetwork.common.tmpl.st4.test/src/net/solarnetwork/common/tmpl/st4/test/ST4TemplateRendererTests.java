/* ==================================================================
 * ST4TemplateRendererTests.java - 26/07/2020 12:38:02 PM
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

package net.solarnetwork.common.tmpl.st4.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import org.junit.Test;
import org.stringtemplate.v4.STGroupDir;
import net.solarnetwork.common.tmpl.st4.ST4TemplateRenderer;

/**
 * Test cases for the {@link ST4TemplateRenderer} class.
 * 
 * @author matt
 * @version 1.0
 */
public class ST4TemplateRendererTests {

	@Test
	public void render_hello() throws IOException {
		// GIVEN
		final Properties messages = new Properties();
		messages.put("title", "Yo!");

		STGroupDir group = new STGroupDir("net/solarnetwork/common/tmpl/st4/test/", '$', '$');
		ST4TemplateRenderer t = ST4TemplateRenderer.html("foo", group, "hello");

		// WHEN
		ByteArrayOutputStream byos = new ByteArrayOutputStream();
		Map<String, Object> parameters = new LinkedHashMap<>(4);
		parameters.put("messages", messages);
		t.render(Locale.ENGLISH, ST4TemplateRenderer.HTML.get(0), parameters, byos);

		// THEN
		String output = new String(byos.toByteArray(), ST4TemplateRenderer.UTF8);
		assertThat("Output generated", output,
				equalTo("<html><head><title>Yo!</title></head><body>Hello, world.</body></html>"));
	}

}

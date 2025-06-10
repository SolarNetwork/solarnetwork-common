/* ==================================================================
 * MessageSourceGroupTests.java - 26/07/2020 8:07:01 AM
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
import static org.hamcrest.MatcherAssert.assertThat;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.util.FileCopyUtils;
import org.stringtemplate.v4.ST;
import net.solarnetwork.common.tmpl.st4.MessageSourceGroup;

/**
 * Test cases for the {@link MessageSourceGroup} class.
 * 
 * @author matt
 * @version 1.0
 */
public class MessageSourceGroupTests {

	private MessageSource messageSource;

	private static void populateMessages(StaticMessageSource sms, Class<?> clazz, String... names) {
		try {
			for ( String name : names ) {
				String t = FileCopyUtils
						.copyToString(new InputStreamReader(clazz.getResourceAsStream(name), "UTF-8"));
				String key = name.replaceFirst("^.*/", "/");
				sms.addMessage(key, Locale.getDefault(), t);
			}
		} catch ( IOException e ) {
			throw new RuntimeException(e);
		}
	}

	@Before
	public void setup() {
		StaticMessageSource sms = new StaticMessageSource();
		populateMessages(sms, MessageSourceGroupTests.class, "hello.st", "head.st", "body.st");
		this.messageSource = sms;
	}

	@Test
	public void render() {
		// GIVEN
		final Properties messages = new Properties();
		messages.put("title", "Yo!");

		MessageSourceGroup group = new MessageSourceGroup("/hello", messageSource, '$', '$');

		// WHEN
		Map<String, Object> parameters = new LinkedHashMap<>(4);
		parameters.put("messages", messages);
		ST st = group.getInstanceOf("hello");
		st.add("messages", messages);
		String output = st.render();

		// THEN
		assertThat("Output generated", output,
				equalTo("<html><head><title>Yo!</title></head><body>Hello, world.</body></html>"));

	}

}

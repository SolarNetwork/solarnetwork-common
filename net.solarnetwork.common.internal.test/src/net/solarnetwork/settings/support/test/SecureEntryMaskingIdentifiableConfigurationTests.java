/* ==================================================================
 * SecureEntryMaskingIdentifiableConfigurationTests.java - 15/04/2018 8:30:52 AM
 * 
 * Copyright 2018 SolarNetwork.net Dev Team
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

package net.solarnetwork.settings.support.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import net.solarnetwork.domain.BasicIdentifiableConfiguration;
import net.solarnetwork.service.IdentifiableConfiguration;
import net.solarnetwork.settings.SettingSpecifier;
import net.solarnetwork.settings.support.BasicTextFieldSettingSpecifier;
import net.solarnetwork.settings.support.SecureEntryMaskingIdentifiableConfiguration;

/**
 * Test cases for the {@link SecureEntryMaskingIdentifiableConfiguration} class.
 * 
 * @author matt
 * @version 2.0
 */
public class SecureEntryMaskingIdentifiableConfigurationTests {

	public interface TestIdentifiableConfiguration extends IdentifiableConfiguration {

		public boolean isTest();

		public String getPassword();

	}

	public class BasicTestIdentifiableConfiguration extends BasicIdentifiableConfiguration
			implements TestIdentifiableConfiguration, Serializable {

		private static final long serialVersionUID = -9192963259911215852L;

		@Override
		public boolean isTest() {
			return true;
		}

		@Override
		public String getPassword() {
			return "abc123";
		}

	}

	private BasicIdentifiableConfiguration createTestConfiguration() {
		BasicIdentifiableConfiguration conf = new BasicIdentifiableConfiguration();
		conf.setName("test.name");
		conf.setServiceIdentifier("test.ident");

		Map<String, Object> props = new LinkedHashMap<String, Object>(4);
		props.put("foo", "bar");
		props.put("password", "secret");
		conf.setServiceProps(props);

		return conf;
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void nothingToMask() {
		BasicIdentifiableConfiguration conf = createTestConfiguration();
		List<SettingSpecifier> settings = Collections.emptyList();
		IdentifiableConfiguration secure = SecureEntryMaskingIdentifiableConfiguration.createProxy(conf,
				settings);
		assertThat("Name", secure.getName(), equalTo(conf.getName()));
		assertThat("Service identifier", secure.getServiceIdentifier(),
				equalTo(conf.getServiceIdentifier()));
		assertThat("Service properties", (Map) secure.getServiceProperties(),
				equalTo((Map) conf.getServiceProperties()));
	}

	@Test
	public void allInterfacesProxied() {
		BasicTestIdentifiableConfiguration conf = new BasicTestIdentifiableConfiguration();
		List<SettingSpecifier> settings = Collections.emptyList();
		IdentifiableConfiguration secure = SecureEntryMaskingIdentifiableConfiguration.createProxy(conf,
				settings);
		assertThat("TestIdentifiableConfiguration implemented",
				secure instanceof TestIdentifiableConfiguration, equalTo(true));
		assertThat("Serializable implemented", secure instanceof Serializable, equalTo(true));

		TestIdentifiableConfiguration testSecure = (TestIdentifiableConfiguration) secure;
		assertThat("Proxied method isTest", testSecure.isTest(), equalTo(conf.isTest()));
		assertThat("Proxied method getPassword", testSecure.getPassword(), equalTo(conf.getPassword()));
	}

	@Test
	public void basicPropertyMasked() {
		BasicTestIdentifiableConfiguration conf = new BasicTestIdentifiableConfiguration();
		List<SettingSpecifier> settings = Collections.singletonList(
				(SettingSpecifier) new BasicTextFieldSettingSpecifier("password", "abc123", true));
		TestIdentifiableConfiguration secure = (TestIdentifiableConfiguration) SecureEntryMaskingIdentifiableConfiguration
				.createProxy(conf, settings);
		assertThat("Proxied method isTest", secure.isTest(), equalTo(conf.isTest()));
		assertThat("Proxied method getPassword", secure.getPassword(), startsWith("{SSHA-256}"));
	}

	@Test
	public void servicePropertyMasked() {
		BasicIdentifiableConfiguration conf = createTestConfiguration();
		List<SettingSpecifier> settings = Collections.singletonList(
				(SettingSpecifier) new BasicTextFieldSettingSpecifier("serviceProperties.password", "",
						true));
		IdentifiableConfiguration secure = SecureEntryMaskingIdentifiableConfiguration.createProxy(conf,
				settings);
		assertThat("Name", secure.getName(), equalTo(conf.getName()));
		assertThat("Service identifier", secure.getServiceIdentifier(),
				equalTo(conf.getServiceIdentifier()));
		Map<String, ?> serviceProps = secure.getServiceProperties();
		assertThat(serviceProps.keySet(), hasSize(2));
		assertThat("Proxied service property foo", serviceProps, hasEntry("foo", (Object) "bar"));
		assertThat("Proxied service property password", (String) serviceProps.get("password"),
				startsWith("{SSHA-256}"));
	}

}

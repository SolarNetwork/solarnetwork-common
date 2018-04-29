/* ==================================================================
 * SettingUtilsTests.java - 16/04/2018 9:31:54 AM
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

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import net.solarnetwork.settings.SettingSpecifier;
import net.solarnetwork.settings.support.BasicTextFieldSettingSpecifier;
import net.solarnetwork.settings.support.SettingUtils;

/**
 * Test cases for the {@link SettingUtils} class.
 * 
 * @author matt
 * @version 1.0
 */
public class SettingUtilsTests {

	@Test
	public void secureKeysNullSettings() {
		Set<String> result = SettingUtils.secureKeys(null);
		assertThat(result, hasSize(0));
	}

	@Test
	public void secureKeyEmptySettings() {
		Set<String> result = SettingUtils.secureKeys(Collections.<SettingSpecifier> emptyList());
		assertThat(result, hasSize(0));
	}

	@Test
	public void secureKeyNoSecureSettings() {
		List<SettingSpecifier> settings = Arrays
				.asList((SettingSpecifier) new BasicTextFieldSettingSpecifier("foo", "bar"));
		Set<String> result = SettingUtils.secureKeys(settings);
		assertThat(result, hasSize(0));
	}

	@Test
	public void secureKeyOneSecureSettings() {
		List<SettingSpecifier> settings = Arrays.asList(
				(SettingSpecifier) new BasicTextFieldSettingSpecifier("foo", "bar"),
				(SettingSpecifier) new BasicTextFieldSettingSpecifier("bim", "bam", true));
		Set<String> result = SettingUtils.secureKeys(settings);
		assertThat(result, contains("bim"));
	}

	@Test
	public void secureKeyMultipleSecureSettings() {
		List<SettingSpecifier> settings = Arrays.asList(
				(SettingSpecifier) new BasicTextFieldSettingSpecifier("foo", "bar"),
				(SettingSpecifier) new BasicTextFieldSettingSpecifier("bim", "bam", true),
				(SettingSpecifier) new BasicTextFieldSettingSpecifier("a", "b"),
				(SettingSpecifier) new BasicTextFieldSettingSpecifier("c", "d", true),
				(SettingSpecifier) new BasicTextFieldSettingSpecifier("e", "f", true),
				(SettingSpecifier) new BasicTextFieldSettingSpecifier("g", "h", true),
				(SettingSpecifier) new BasicTextFieldSettingSpecifier("i", "j"),
				(SettingSpecifier) new BasicTextFieldSettingSpecifier("k", "l", true));
		Set<String> result = SettingUtils.secureKeys(settings);
		assertThat(result, contains("bim", "c", "e", "g", "k"));
	}

}

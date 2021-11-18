/* ==================================================================
 * MqttUtilsTests.java - 18/11/2021 3:38:38 PM
 * 
 * Copyright 2021 SolarNetwork.net Dev Team
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

package net.solarnetwork.common.mqtt.test;

import org.junit.Test;
import net.solarnetwork.common.mqtt.MqttUtils;

/**
 * Test cases for the {@link MqttUtils} class.
 * 
 * @author matt
 * @version 1.0
 */
public class MqttUtilsTests {

	@Test
	public void validateTopic_singleCharacter() {
		MqttUtils.validateTopicName("f");
	}

	@Test
	public void validateTopic_singleLevel() {
		MqttUtils.validateTopicName("foo");
	}

	@Test
	public void validateTopic_multiLevel() {
		MqttUtils.validateTopicName("foo/bar/bam");
	}

	@Test(expected = IllegalArgumentException.class)
	public void validateTopic_null() {
		MqttUtils.validateTopicName(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void validateTopic_empty() {
		MqttUtils.validateTopicName("");
	}

	@Test(expected = IllegalArgumentException.class)
	public void validateTopic_singleLevelWildcard() {
		MqttUtils.validateTopicName("foo/+/bar");
	}

	@Test(expected = IllegalArgumentException.class)
	public void validateTopic_multiLevelWildcard() {
		MqttUtils.validateTopicName("foo/#");
	}

	@Test(expected = IllegalArgumentException.class)
	public void validateTopic_nullCharacter() {
		MqttUtils.validateTopicName("foo\0");
	}

	@Test(expected = IllegalArgumentException.class)
	public void validateTopic_tooLong() {
		StringBuilder buf = new StringBuilder();
		for ( int i = 0; i < 65536; i++ ) {
			buf.append("c");
		}
		MqttUtils.validateTopicName(buf.toString());
	}

}

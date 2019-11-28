/* ==================================================================
 * MqttFilePersistenceTests.java - 27/03/2019 7:08:28 am
 * 
 * Copyright 2019 SolarNetwork.net Dev Team
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

package net.solarnetwork.common.mqtt.paho.test;

import static org.apache.commons.lang3.ArrayUtils.toObject;
import static org.hamcrest.Matchers.arrayContaining;
import static org.junit.Assert.assertThat;
import static org.springframework.util.FileCopyUtils.copyToByteArray;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import org.eclipse.paho.client.mqttv3.MqttPersistable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.FileCopyUtils;
import net.solarnetwork.common.mqtt.paho.MqttFilePersistence;
import net.solarnetwork.common.mqtt.paho.MqttPersistentData;

/**
 * Test cases for the {@link MqttFilePersistence} class.
 * 
 * @author matt
 * @version 1.0
 */
public class MqttFilePersistenceTests {

	private static final String CLIENT_ID = "xyz";

	private static final String CONN_ID = "abc123";

	private Path directory;
	private MqttFilePersistence persistence;

	@Before
	public void setup() throws Exception {
		directory = Files.createTempDirectory("mqtt-persistence-");
		persistence = new MqttFilePersistence(directory.toString());
		persistence.open(CLIENT_ID, CONN_ID);
	}

	@After
	public void cleanup() throws Exception {
		try {
			persistence.close();
		} catch ( Exception e ) {
			// ignore
		}
		// @formatter:off
		Files.walk(directory)
	    	.sorted(Comparator.reverseOrder())
	    	.map(Path::toFile)
	    	.peek(System.out::println)
	    	.forEach(File::delete);
		// @formatter:on
	}

	private Path msgPath(String key) {
		return directory.resolve(CLIENT_ID + "-" + CONN_ID).resolve(key + ".msg");
	}

	@Test
	public void saveMessage() throws Exception {
		// given
		String key = "foo";
		byte[] header = new byte[] { 1, 2, 3 };
		byte[] payload = new byte[] { 4, 5, 6 };

		// when
		MqttPersistentData data = new MqttPersistentData(key, header, 0, header.length, payload, 0,
				payload.length);
		persistence.put(key, data);

		// then
		Byte[] persisted = toObject(copyToByteArray(msgPath(key).toFile()));
		assertThat("Persisted bytes", persisted,
				arrayContaining(toObject(new byte[] { 1, 2, 3, 4, 5, 6 })));
	}

	@Test
	public void loadMessage() throws Exception {
		// given
		String key = "foo";
		byte[] persistedData = new byte[] { 1, 2, 3, 4, 5, 6 };
		FileCopyUtils.copy(persistedData, msgPath(key).toFile());

		// when
		MqttPersistable data = persistence.get(key);

		// then
		assertThat("Message header", toObject(data.getHeaderBytes()),
				arrayContaining(toObject(persistedData)));
	}

}

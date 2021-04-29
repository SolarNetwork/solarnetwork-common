/* ==================================================================
 * ProtocProtobufCompilerServiceTests.java - 20/04/2021 1:24:31 PM
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

package net.solarnetwork.common.protobuf.test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;
import net.solarnetwork.common.protobuf.protoc.ProtocProtobufCompilerService;

/**
 * Test cases for the {@link ProtocProtobufCompilerService} class.
 * 
 * @author matt
 * @version 1.0
 */
public class ProtocProtobufCompilerServiceTests extends BaseProtocProtobufCompilerServiceTestSupport {

	@Test
	public void compile() throws Exception {
		// GIVEN
		List<Resource> protos = Arrays.asList(new ClassPathResource("dinosaur.proto", getClass()),
				new ClassPathResource("period.proto", getClass()));
		// WHEN
		ClassLoader cl = protocService.compileProtobufResources(protos, null);

		// THEN
		@SuppressWarnings({ "rawtypes", "unchecked" })
		Class<? extends Message> dinoClass = (Class) cl.loadClass("sn.dinosaurs.Dinosaur");
		Method m = dinoClass.getMethod("newBuilder");
		Message.Builder b = (Message.Builder) m.invoke(null);
		Descriptor desc = b.getDescriptorForType();
		List<FieldDescriptor> fields = desc.getFields();
		for ( FieldDescriptor f : fields ) {
			if ( "name".equals(f.getName()) ) {
				b.setField(f, "Fooasaur");
			}
		}
		Message msg = b.build();
		assertThat("Data created", msg, notNullValue());
		// @formatter:off
		assertThat("TXT message", TextFormat.printToString(msg), equalTo(
				  "name: \"Fooasaur\"\n"
				));
		// @formatter:on
	}

	@Test
	public void compile_nested() throws Exception {
		// GIVEN
		List<Resource> protos = Arrays.asList(new ClassPathResource("my-datum.proto", getClass()));

		// WHEN
		ClassLoader cl = protocService.compileProtobufResources(protos, null);

		// THEN
		@SuppressWarnings({ "rawtypes", "unchecked" })
		Class<? extends Message> msgClass = (Class) cl.loadClass("sn.PowerDatum");
		Method m = msgClass.getMethod("newBuilder");
		Message.Builder b = (Message.Builder) m.invoke(null);
		Descriptor desc = b.getDescriptorForType();
		List<FieldDescriptor> fields = desc.getFields();
		for ( FieldDescriptor f : fields ) {
			String fieldName = f.getName();
			if ( "voltage".equals(fieldName) ) {
				b.setField(f, 1.234);
			}
		}
		Message msg = b.build();
		assertThat("Data created", msg, notNullValue());
		// @formatter:off
		assertThat("TXT message", TextFormat.printToString(msg), equalTo(
				  "voltage: 1.234\n"
				));
		// @formatter:on
	}

}

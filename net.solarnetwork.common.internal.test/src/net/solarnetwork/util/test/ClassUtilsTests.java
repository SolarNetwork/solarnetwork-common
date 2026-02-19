/* ==================================================================
 * ClassUtilsTests.java - 25/09/2017 9:44:13 AM
 *
 * Copyright 2017 SolarNetwork.net Dev Team
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

package net.solarnetwork.util.test;

import static java.util.Map.entry;
import static net.solarnetwork.test.CommonTestUtils.randomLong;
import static net.solarnetwork.test.CommonTestUtils.randomString;
import static org.assertj.core.api.BDDAssertions.then;
import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.Test;
import net.solarnetwork.domain.Location;
import net.solarnetwork.domain.Request;
import net.solarnetwork.domain.SerializeIgnore;
import net.solarnetwork.domain.SimpleLocation;
import net.solarnetwork.domain.datum.DatumSamples;
import net.solarnetwork.domain.datum.GeneralDatum;
import net.solarnetwork.util.ClassUtils;

/**
 * Test cases for the {@link ClassUtils} class.
 *
 * @author matt
 * @version 1.1
 */
public class ClassUtilsTests {

	private static final class SerializableSimpleLocation extends SimpleLocation
			implements Serializable {

		private static final long serialVersionUID = -2811665308323747576L;

	}

	public interface SerializableLocation extends Location, Serializable {

	}

	private static final class SimpleSerializableLocation extends SimpleLocation
			implements SerializableLocation {

		private static final long serialVersionUID = -2811665308323747576L;

	}

	/**
	 * A test bean.
	 */
	public static final class BeanWithIgnore {

		private Long id;
		private String source;
		private Instant timestamp;
		private DatumSamples samples;

		private BeanWithIgnore(Long id, String source, Instant timestamp, DatumSamples samples) {
			super();
			this.id = id;
			this.source = source;
			this.timestamp = timestamp;
			this.samples = samples;
		}

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getSource() {
			return source;
		}

		public void setSource(String source) {
			this.source = source;
		}

		@SerializeIgnore
		public Instant getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(Instant timestamp) {
			this.timestamp = timestamp;
		}

		@SerializeIgnore
		public DatumSamples getSamples() {
			return samples;
		}

		public void setSamples(DatumSamples samples) {
			this.samples = samples;
		}

	}

	@Test
	public void getAllInterfacesExcludingJavaPackagesBasic() {
		final Set<Class<?>> interfaces = ClassUtils
				.getAllNonJavaInterfacesForClassAsSet(SerializableSimpleLocation.class);
		then(interfaces).as("Core Java packages ignored").containsExactly(Location.class);
	}

	@Test
	public void getAllInterfacesExcludingJavaPackagesSuperInterface() {
		final Set<Class<?>> interfaces = ClassUtils
				.getAllNonJavaInterfacesForClassAsSet(SimpleSerializableLocation.class);
		then(interfaces).as("Super interface included in order")
				.containsExactly(SerializableLocation.class, Location.class);
	}

	@Test
	public void getAllInterfacesExcludingJavaPackagesNoInterface() {
		final Set<Class<?>> interfaces = ClassUtils.getAllNonJavaInterfacesForClassAsSet(Request.class);
		then(interfaces).as("No interfaces produces empty set").isEmpty();
	}

	@Test
	public void getResourceAsString() {
		// WHEN
		final String s = ClassUtils.getResourceAsString("test-file.txt", getClass());

		// THEN
		then(s).as("File loaded").isEqualTo("Hello, world.\nGoodbye.");
	}

	/** Regex for a line starting with a {@literal #} comment character. */
	public static final Pattern HASH_COMMENT = Pattern.compile("^\\s*#");

	@Test
	public void getResourceAsString_skipComments() {
		// WHEN
		final String s = ClassUtils.getResourceAsString("test-file-with-comments.txt", getClass(),
				HASH_COMMENT);

		// THEN
		then(s).as("File loaded").isEqualTo("Hello, world.\nGoodbye.");
	}

	@Test
	public void getBeanProperties() {
		// GIVEN
		final var d = new GeneralDatum(randomLong(), randomString(), Instant.now(),
				new DatumSamples(Map.of("a", 1), null, null));

		// WHEN
		final Map<String, Object> result = ClassUtils.getBeanProperties(d, Set.of());

		// THEN
		// @formatter:off
		then(result)
			.as("Map generated")
			.isNotNull()
			.containsOnly(
				entry("class", d.getClass()),
				entry("id", d.getId()),
				entry("objectId", d.getObjectId()),
				entry("sourceId", d.getSourceId()),
				entry("timestamp", d.getTimestamp()),
				entry("samples", d.getSamples()),
				entry("sampleData", d.getSampleData()),
				entry("empty", d.isEmpty())
			)
			;
		// @formatter:on
	}

	@Test
	public void getBeanProperties_defaultIgnoreClass() {
		// GIVEN
		final var d = new GeneralDatum(randomLong(), randomString(), Instant.now(),
				new DatumSamples(Map.of("a", 1), null, null));

		// WHEN
		final Map<String, Object> result = ClassUtils.getBeanProperties(d, null);

		// THEN
		// @formatter:off
		then(result)
			.as("Map generated")
			.isNotNull()
			.containsOnly(
				entry("id", d.getId()),
				entry("objectId", d.getObjectId()),
				entry("sourceId", d.getSourceId()),
				entry("timestamp", d.getTimestamp()),
				entry("samples", d.getSamples()),
				entry("sampleData", d.getSampleData()),
				entry("empty", d.isEmpty())
			)
			;
		// @formatter:on
	}

	@Test
	public void getBeanProperties_ignoreNames() {
		// GIVEN
		final var d = new GeneralDatum(randomLong(), randomString(), Instant.now(),
				new DatumSamples(Map.of("a", 1), null, null));

		// WHEN
		final Map<String, Object> result = ClassUtils.getBeanProperties(d,
				Set.of("class", "id", "sampleData", "empty"));

		// THEN
		// @formatter:off
		then(result)
			.as("Map generated")
			.isNotNull()
			.containsOnly(
				entry("objectId", d.getObjectId()),
				entry("sourceId", d.getSourceId()),
				entry("timestamp", d.getTimestamp()),
				entry("samples", d.getSamples())
			)
			;
		// @formatter:on
	}

	@Test
	public void getBeanProperties_includeSerializeIgnoreAnnotation() {
		// GIVEN
		final var d = new BeanWithIgnore(randomLong(), randomString(), Instant.now(),
				new DatumSamples(Map.of("a", 1), null, null));

		// WHEN
		final Map<String, Object> result = ClassUtils.getBeanProperties(d, null, false);

		// THEN
		// @formatter:off
		then(result)
			.as("Map generated")
			.isNotNull()
			.containsOnly(
				entry("id", d.getId()),
				entry("source", d.getSource()),
				entry("timestamp", d.getTimestamp()),
				entry("samples", d.getSamples())
			)
			;
		// @formatter:on
	}

	@Test
	public void getBeanProperties_ignoreSerializeIgnoreAnnotation() {
		// GIVEN
		final var d = new BeanWithIgnore(randomLong(), randomString(), Instant.now(),
				new DatumSamples(Map.of("a", 1), null, null));

		// WHEN
		final Map<String, Object> result = ClassUtils.getBeanProperties(d, null, true);

		// THEN
		// @formatter:off
		then(result)
			.as("Map generated")
			.isNotNull()
			.containsOnly(
				entry("id", d.getId()),
				entry("source", d.getSource())
			)
			;
		// @formatter:on
	}

	@Test
	public void getBeanProperties_map() {
		// GIVEN
		final var d = Map.of("id", randomLong(), "source", randomString(), "timestamp", Instant.now(),
				"samples", new DatumSamples());

		// WHEN
		final Map<String, Object> result = ClassUtils.getBeanProperties(d, Set.of(), false);

		// THEN
		// @formatter:off
		then(result)
			.as("Map generated")
			.isNotNull()
			.containsOnly(
				entry("id", d.get("id")),
				entry("source", d.get("source")),
				entry("timestamp", d.get("timestamp")),
				entry("samples", d.get("samples"))
			)
			;
		// @formatter:on
	}

	@Test
	public void getBeanProperties_map_ignoreNames() {
		// GIVEN
		final var d = Map.of("id", randomLong(), "source", randomString(), "timestamp", Instant.now(),
				"samples", new DatumSamples());

		// WHEN
		final Map<String, Object> result = ClassUtils.getBeanProperties(d, Set.of("id", "timestamp"),
				false);

		// THEN
		// @formatter:off
		then(result)
			.as("Map generated")
			.isNotNull()
			.containsOnly(
				entry("source", d.get("source")),
				entry("samples", d.get("samples"))
			)
			;
		// @formatter:on
	}

	@Test
	public void getSimpleBeanProperties() {
		// GIVEN
		final var d = new GeneralDatum(randomLong(), randomString(), Instant.now(),
				new DatumSamples(Map.of("a", 1), null, null));

		// WHEN
		final Map<String, Object> result = ClassUtils.getSimpleBeanProperties(d, Set.of(), false);

		// THEN
		// @formatter:off
		then(result)
			.as("Map generated")
			.isNotNull()
			.containsOnly(
				entry("objectId", d.getObjectId()),
				entry("sourceId", d.getSourceId()),
				entry("timestamp", d.getTimestamp().toEpochMilli()),
				entry("empty", d.isEmpty())
			)
			;
		// @formatter:on
	}

	@Test
	public void getSimpleBeanProperties_ignoreNames() {
		// GIVEN
		final var d = new GeneralDatum(randomLong(), randomString(), Instant.now(),
				new DatumSamples(Map.of("a", 1), null, null));

		// WHEN
		final Map<String, Object> result = ClassUtils.getSimpleBeanProperties(d,
				Set.of("objectId", "timestamp"), false);

		// THEN
		// @formatter:off
		then(result)
			.as("Map generated")
			.isNotNull()
			.containsOnly(
				entry("sourceId", d.getSourceId()),
				entry("empty", d.isEmpty())
			)
			;
		// @formatter:on
	}

	@Test
	public void getSimpleBeanProperties_includeSerializeIgnoreAnnotation() {
		// GIVEN
		final BeanWithIgnore d = new BeanWithIgnore(randomLong(), randomString(), Instant.now(),
				new DatumSamples(Map.of("a", 1), null, null));

		// WHEN
		final Map<String, Object> result = ClassUtils.getSimpleBeanProperties(d, Set.of(), false);

		// THEN
		// @formatter:off
		then(result)
			.as("Map generated")
			.isNotNull()
			.containsOnly(
				entry("id", d.getId()),
				entry("source", d.getSource()),
				entry("timestamp", d.getTimestamp().toEpochMilli())
			)
			;
		// @formatter:on
	}

	@Test
	public void getSimpleBeanProperties_ignoreSerializeIgnoreAnnotation() {
		// GIVEN
		final BeanWithIgnore d = new BeanWithIgnore(randomLong(), randomString(), Instant.now(),
				new DatumSamples(Map.of("a", 1), null, null));

		// WHEN
		final Map<String, Object> result = ClassUtils.getSimpleBeanProperties(d, Set.of(), true);

		// THEN
		// @formatter:off
		then(result)
			.as("Map generated")
			.isNotNull()
			.containsOnly(
				entry("id", d.getId()),
				entry("source", d.getSource())
			)
			;
		// @formatter:on
	}

	@Test
	public void getSimpleBeanProperties_map() {
		// GIVEN
		final var d = Map.of("id", randomLong(), "source", randomString(), "timestamp", Instant.now(),
				"samples", new DatumSamples());

		// WHEN
		final Map<String, Object> result = ClassUtils.getSimpleBeanProperties(d, Set.of(), false);

		// THEN
		// @formatter:off
		then(result)
			.as("Map generated")
			.isNotNull()
			.containsOnly(
				entry("id", d.get("id")),
				entry("source", d.get("source")),
				entry("timestamp", ((Instant)d.get("timestamp")).toEpochMilli())
			)
			;
		// @formatter:on
	}

	@Test
	public void getSimpleBeanProperties_map_ignoreNames() {
		// GIVEN
		final var d = Map.of("id", randomLong(), "source", randomString(), "timestamp", Instant.now(),
				"samples", new DatumSamples());

		// WHEN
		final Map<String, Object> result = ClassUtils.getSimpleBeanProperties(d,
				Set.of("id", "timestamp"), false);

		// THEN
		// @formatter:off
		then(result)
			.as("Map generated")
			.isNotNull()
			.containsOnly(
				entry("source", d.get("source"))
			)
			;
		// @formatter:on
	}

}

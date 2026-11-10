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
import static org.assertj.core.api.BDDAssertions.thenExceptionOfType;
import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.Test;
import org.springframework.beans.PropertyBatchUpdateException;
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
 * @version 1.3
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

	public interface TopIfc {

	}

	public interface LeftIfc extends TopIfc {

	}

	public interface RightIfc extends TopIfc {

	}

	public interface DiamondIfc extends LeftIfc, RightIfc {

	}

	private static class DiamondImpl implements DiamondIfc, RightIfc {

	}

	private static final class DiamondSubImpl extends DiamondImpl implements LeftIfc {

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

	@Test
	public void getSimpleBeanProperties_boolean() {
		// GIVEN
		final BeanWithBoolean d = new BeanWithBoolean(Boolean.TRUE, true);

		// WHEN
		final Map<String, Object> result = ClassUtils.getSimpleBeanProperties(d, Set.of(), false);

		// THEN
		// @formatter:off
		then(result)
			.as("Map generated with Boolean and boolean properties")
			.isNotNull()
			.containsOnly(
				entry("enabled", Boolean.TRUE),
				entry("active", true)
			)
			;
		// @formatter:on
	}

	@Test
	public void getSimpleBeanProperties_map_boolean() {
		// GIVEN
		final var d = Map.of("enabled", Boolean.TRUE, "source", randomString());

		// WHEN
		final Map<String, Object> result = ClassUtils.getSimpleBeanProperties(d, Set.of(), false);

		// THEN
		// @formatter:off
		then(result)
			.as("Map generated with Boolean value")
			.isNotNull()
			.containsOnly(
				entry("enabled", Boolean.TRUE),
				entry("source", d.get("source"))
			)
			;
		// @formatter:on
	}

	@Test
	public void getSimpleBeanProperties_map_enumWithBody() {
		// GIVEN
		// a constant with a body is an anonymous subclass, which is not itself an enum
		final var d = Map.of("square", Shape.Square, "circle", Shape.Circle);

		// WHEN
		final Map<String, Object> result = ClassUtils.getSimpleBeanProperties(d, Set.of(), false);

		// THEN
		// @formatter:off
		then(result)
			.as("Map generated with enum values as strings")
			.isNotNull()
			.containsOnly(
				entry("square", "Square"),
				entry("circle", "Circle")
			)
			;
		// @formatter:on
	}

	/**
	 * A test enum with a constant that has a body.
	 */
	public static enum Shape {

		Square,

		Circle {

			@Override
			public boolean isRound() {
				return true;
			}

		};

		public boolean isRound() {
			return false;
		}

	}

	/**
	 * A test bean with boolean properties.
	 */
	public static final class BeanWithBoolean {

		private final Boolean enabled;
		private final boolean active;

		private BeanWithBoolean(Boolean enabled, boolean active) {
			super();
			this.enabled = enabled;
			this.active = active;
		}

		public Boolean getEnabled() {
			return enabled;
		}

		public boolean isActive() {
			return active;
		}

	}

	/**
	 * A test bean for setting and copying properties.
	 */
	public static class CopyBean {

		private String name;
		private Integer count;
		private Child child;
		private String writeOnly;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Integer getCount() {
			return count;
		}

		public void setCount(Integer count) {
			this.count = count;
		}

		public Child getChild() {
			return child;
		}

		public void setChild(Child child) {
			this.child = child;
		}

		public String getReadOnly() {
			return "read-only";
		}

		public void setWriteOnly(String writeOnly) {
			this.writeOnly = writeOnly;
		}

		public String writeOnlyValue() {
			return writeOnly;
		}

	}

	/**
	 * A nested test bean.
	 */
	public static class Child {

		private String value;

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

	}

	@Test
	public void instantiateClass() {
		// WHEN
		Collection<?> result = ClassUtils.instantiateClass(ArrayList.class.getName(), Collection.class);

		// THEN
		then(result).as("Instance of named class created").isInstanceOf(ArrayList.class);
	}

	@Test
	public void instantiateClass_nonBootstrapType() {
		// WHEN
		CopyBean result = ClassUtils.instantiateClass(CopyBean.class.getName(), CopyBean.class);

		// THEN
		then(result).as("Instance of named class created").isInstanceOf(CopyBean.class);
	}

	@Test
	public void instantiateClass_noDefaultConstructor() {
		// @formatter:off
		thenExceptionOfType(RuntimeException.class)
			.isThrownBy(() -> ClassUtils.instantiateClass(Integer.class.getName(), Number.class))
			.as("Instantiation failure wrapped")
			.withMessageContaining("Unable to instantiate class")
			.withCauseInstanceOf(NoSuchMethodException.class)
			;
		// @formatter:on
	}

	@Test
	public void loadClass_notAssignable() {
		// @formatter:off
		thenExceptionOfType(RuntimeException.class)
			.isThrownBy(() -> ClassUtils.loadClass(String.class.getName(), Number.class))
			.as("Class not of requested type")
			.withMessageContaining("is not a")
			;
		// @formatter:on
	}

	@Test
	public void loadClass_notFound() {
		// @formatter:off
		thenExceptionOfType(RuntimeException.class)
			.isThrownBy(() -> ClassUtils.loadClass("not.a.RealClass", Object.class))
			.as("Missing class wrapped")
			.withMessageContaining("Unable to load class")
			.withCauseInstanceOf(ClassNotFoundException.class)
			;
		// @formatter:on
	}

	@Test
	public void setBeanProperties() {
		// GIVEN
		CopyBean bean = new CopyBean();

		// WHEN
		ClassUtils.setBeanProperties(bean, Map.of("name", "foo", "count", 1, "child.value", "bar"));

		// THEN
		then(bean.getName()).as("Property set").isEqualTo("foo");
		then(bean.getCount()).as("Property set").isEqualTo(1);
		then(bean.getChild()).as("Nested path auto-grown").isNotNull().extracting(Child::getValue)
				.as("Nested property set").isEqualTo("bar");
	}

	@Test
	public void setBeanProperties_ignoreErrors() {
		// GIVEN
		CopyBean bean = new CopyBean();

		// WHEN
		ClassUtils.setBeanProperties(bean, Map.of("name", "foo", "count", "not a number", "unknown", 1),
				true);

		// THEN
		then(bean.getName()).as("Valid property set").isEqualTo("foo");
		then(bean.getCount()).as("Invalid property ignored").isNull();
	}

	@Test
	public void setBeanProperties_ignoreUnknown() {
		// GIVEN
		CopyBean bean = new CopyBean();

		// WHEN
		ClassUtils.setBeanProperties(bean, Map.of("name", "foo", "unknown", 1), true);

		// THEN
		then(bean.getName()).as("Valid property set").isEqualTo("foo");
	}

	@Test
	public void setBeanProperties_notIgnoreErrors() {
		// GIVEN
		CopyBean bean = new CopyBean();

		// @formatter:off
		thenExceptionOfType(PropertyBatchUpdateException.class)
			.as("Invalid property value error thrown")
			.isThrownBy(() -> ClassUtils.setBeanProperties(bean, Map.of("count", "not a number"),
					false))
			;
		// @formatter:on
	}

	@Test
	public void setBeanProperties_nullArguments() {
		// GIVEN
		CopyBean bean = new CopyBean();

		// WHEN
		ClassUtils.setBeanProperties(null, Map.of("name", "foo"), false);
		ClassUtils.setBeanProperties(bean, null, false);

		// THEN
		then(bean.getName()).as("Nothing set").isNull();
	}

	@Test
	public void getSimpleBeanProperties_defaultIgnore() {
		// GIVEN
		CopyBean bean = new CopyBean();
		bean.setName("foo");
		bean.setChild(new Child());
		bean.setWriteOnly("secret");

		// WHEN
		Map<String, Object> result = ClassUtils.getSimpleBeanProperties(bean, null);

		// THEN
		then(result).as("Class, non-simple, null, and write-only properties left out")
				.containsOnly(entry("name", "foo"), entry("readOnly", "read-only"));
	}

	@Test
	public void getSimpleBeanProperties_map_nullsAndDateTypes() {
		// GIVEN
		final Instant ts = Instant.parse("2024-01-02T03:04:05.678Z");
		final Map<String, Object> map = new HashMap<>();
		map.put(null, "no key");
		map.put("nothing", null);
		map.put("char", 'c');
		map.put("byte", (byte) 1);
		map.put("date", Date.from(ts));
		map.put("zoned", ts.atZone(ZoneOffset.ofHours(12)));
		map.put("offset", ts.atOffset(ZoneOffset.ofHours(-5)));

		// WHEN
		Map<String, Object> result = ClassUtils.getSimpleBeanProperties(map, null);

		// THEN
		// @formatter:off
		then(result)
			.as("Null keys and values left out, dates converted to epoch milliseconds")
			.containsOnly(
				entry("char", 'c'),
				entry("byte", (byte) 1),
				entry("date", ts.toEpochMilli()),
				entry("zoned", ts.toEpochMilli()),
				entry("offset", ts.toEpochMilli())
			)
			;
		// @formatter:on
	}

	@Test
	public void getBeanProperties_map_nulls() {
		// GIVEN
		final Map<String, Object> map = new HashMap<>();
		map.put(null, "no key");
		map.put("nothing", null);
		map.put("a", 1);

		// WHEN
		Map<String, Object> result = ClassUtils.getBeanProperties(map, null);

		// THEN
		then(result).as("Null keys and values left out").containsOnly(entry("a", 1));
	}

	@Test
	public void getBeanProperties_writeOnly() {
		// GIVEN
		CopyBean bean = new CopyBean();
		bean.setWriteOnly("secret");

		// WHEN
		Map<String, Object> result = ClassUtils.getBeanProperties(bean, null);

		// THEN
		then(result).as("Write-only and null properties left out")
				.containsOnly(entry("readOnly", "read-only"));
	}

	@Test
	public void copyBeanProperties() {
		// GIVEN
		CopyBean src = new CopyBean();
		src.setName("  ");
		src.setCount(2);
		CopyBean dest = new CopyBean();
		dest.setChild(new Child());

		// WHEN
		ClassUtils.copyBeanProperties(src, dest, null);

		// THEN
		then(dest.getName()).as("Blank string copied").isEqualTo("  ");
		then(dest.getCount()).as("Value copied").isEqualTo(2);
		then(dest.getChild()).as("Null value not copied").isNotNull();
	}

	@Test
	public void copyBeanProperties_emptyStringToNull() {
		// GIVEN
		CopyBean src = new CopyBean();
		src.setName("  ");
		src.setCount(2);
		CopyBean dest = new CopyBean();
		dest.setName("keep");

		// WHEN
		ClassUtils.copyBeanProperties(src, dest, null, true);

		// THEN
		then(dest.getName()).as("Blank string treated as null and not copied").isEqualTo("keep");
		then(dest.getCount()).as("Value copied").isEqualTo(2);
	}

	@Test
	public void copyBeanProperties_ignore() {
		// GIVEN
		CopyBean src = new CopyBean();
		src.setName("foo");
		src.setCount(2);
		CopyBean dest = new CopyBean();

		// WHEN
		ClassUtils.copyBeanProperties(src, dest, Set.of("count"), false);

		// THEN
		then(dest.getName()).as("Value copied").isEqualTo("foo");
		then(dest.getCount()).as("Ignored property not copied").isNull();
	}

	@Test
	public void getResourceAsString_allSkipped() {
		// WHEN
		final String s = ClassUtils.getResourceAsString("test-file.txt", getClass(),
				Pattern.compile(".*"));

		// THEN
		then(s).as("Empty string when every line skipped").isEmpty();
	}

	@Test
	public void getResourceAsString_notFound() {
		// @formatter:off
		thenExceptionOfType(RuntimeException.class)
			.isThrownBy(() -> ClassUtils.getResourceAsString("not-a-file.txt", getClass()))
			.as("Missing resource")
			.withMessageContaining("not found")
			;
		// @formatter:on
	}

	@Test
	public void getAllInterfacesForClassAsSet_noExclusions() {
		// WHEN
		final Set<Class<?>> interfaces = ClassUtils
				.getAllInterfacesForClassAsSet(SerializableSimpleLocation.class, null);

		// THEN
		then(interfaces).as("Java interfaces included without exclusions, subclass first")
				.containsExactly(Serializable.class, Location.class, Cloneable.class);
	}

	@Test
	public void getAllInterfacesForClassAsSet_interface() {
		// WHEN
		final Set<Class<?>> interfaces = ClassUtils.getAllInterfacesForClassAsSet(List.class, null);

		// THEN
		then(interfaces).as("Interface itself and super interfaces included")
				.contains(List.class, Collection.class, Iterable.class)
				.doesNotContain(AbstractList.class, RandomAccess.class);
	}

	@Test
	public void getAllInterfacesForClassAsSet_diamond() {
		// WHEN
		final Set<Class<?>> interfaces = ClassUtils.getAllInterfacesForClassAsSet(DiamondImpl.class,
				null);

		// THEN
		then(interfaces).as("Shared super interface included once, depth-first order")
				.containsExactly(DiamondIfc.class, LeftIfc.class, TopIfc.class, RightIfc.class);
	}

	@Test
	public void getAllInterfacesForClassAsSet_diamond_subclass() {
		// WHEN
		final Set<Class<?>> interfaces = ClassUtils.getAllInterfacesForClassAsSet(DiamondSubImpl.class,
				null);

		// THEN
		then(interfaces).as("Subclass interfaces first, then superclass interfaces not already included")
				.containsExactly(LeftIfc.class, TopIfc.class, DiamondIfc.class, RightIfc.class);
	}

	@Test
	public void getAllInterfacesForClassAsSet_diamond_interface() {
		// WHEN
		final Set<Class<?>> interfaces = ClassUtils.getAllInterfacesForClassAsSet(DiamondIfc.class,
				null);

		// THEN
		then(interfaces).as("Interface itself first, then super interfaces depth-first")
				.containsExactly(DiamondIfc.class, LeftIfc.class, TopIfc.class, RightIfc.class);
	}

	@Test
	public void getAllInterfacesForClassAsSet_excludedSuperInterfacesIncluded() {
		// WHEN
		final Set<Class<?>> interfaces = ClassUtils.getAllInterfacesForClassAsSet(DiamondImpl.class,
				Set.of(LeftIfc.class.getName()));

		// THEN
		then(interfaces).as("Excluded interface left out, but its super interface still included")
				.containsExactly(DiamondIfc.class, TopIfc.class, RightIfc.class);
	}

	@Test
	public void getAllInterfacesForClassAsSet_excludedInterfaceItself() {
		// WHEN
		final Set<Class<?>> interfaces = ClassUtils.getAllInterfacesForClassAsSet(DiamondIfc.class,
				Set.of(DiamondIfc.class.getName()));

		// THEN
		then(interfaces).as("Excluded interface left out, but its super interfaces still included")
				.containsExactly(LeftIfc.class, TopIfc.class, RightIfc.class);
	}

	@Test
	public void getAllInterfacesForClassAsSet_null() {
		// WHEN
		final Set<Class<?>> interfaces = ClassUtils.getAllInterfacesForClassAsSet(null, null);

		// THEN
		then(interfaces).as("No class produces empty set").isEmpty();
	}

	@Test
	public void getAllNonJavaInterfacesForClassAsSet_newModifiableSet() {
		// GIVEN
		final Set<Class<?>> interfaces = ClassUtils
				.getAllNonJavaInterfacesForClassAsSet(DiamondImpl.class);

		// WHEN
		interfaces.remove(TopIfc.class);

		// THEN
		then(interfaces).as("Returned set can be modified")
				.containsExactly(DiamondIfc.class, LeftIfc.class, RightIfc.class);
		then(ClassUtils.getAllNonJavaInterfacesForClassAsSet(DiamondImpl.class))
				.as("Each call returns a new set")
				.containsExactly(DiamondIfc.class, LeftIfc.class, TopIfc.class, RightIfc.class);
	}

}

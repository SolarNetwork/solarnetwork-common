/* ==================================================================
 * BasicIdentifiableTests.java - 8/03/2022 8:05:33 AM
 * 
 * Copyright 2022 SolarNetwork.net Dev Team
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

package net.solarnetwork.service.support.test;

import static net.solarnetwork.util.ArrayUtils.arrayWithLength;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import java.math.BigDecimal;
import org.junit.Test;
import net.solarnetwork.domain.KeyValuePair;
import net.solarnetwork.service.support.BasicIdentifiable;

/**
 * Test cases for the {@link BasicIdentifiable} class.
 * 
 * @author matt
 * @version 1.0
 */
public class BasicIdentifiableTests {

	private static final int TEST_INT = 123;
	private static final float TEST_FLOAT = 456.1f;
	private static final double TEST_DOUBLE = 456.7890123456;
	private static final BigDecimal TEST_DECIMAL = new BigDecimal("19023870129381.1092380129");

	@Test
	public void metadataValue_nullArray() {
		// GIVEN
		BasicIdentifiable ident = new BasicIdentifiable();

		// THEN
		assertThat("Null returned from null array", ident.metadataValue("a"), is(nullValue()));
	}

	private KeyValuePair[] testData() {
		// @formatter:off
		return new KeyValuePair[] {
				null,
				new KeyValuePair(),
				new KeyValuePair("a", null),
				new KeyValuePair("b", "bee"),
				new KeyValuePair("int", Integer.toString(TEST_INT)),
				new KeyValuePair("float", Float.toString(TEST_FLOAT)),
				new KeyValuePair("double", Double.toString(TEST_DOUBLE)),
				new KeyValuePair("decimal", TEST_DECIMAL.toPlainString()),
		};
		// @formatter:on
	}

	@Test
	public void metadataValue_notFound() {
		// GIVEN
		BasicIdentifiable ident = new BasicIdentifiable();
		ident.setMetadata(testData());

		// THEN
		assertThat("Null returned from not found", ident.metadataValue("foo"), is(nullValue()));
	}

	@Test
	public void metadataValue_nullValue() {
		// GIVEN
		BasicIdentifiable ident = new BasicIdentifiable();
		ident.setMetadata(testData());

		// THEN
		assertThat("Null returned from matching key", ident.metadataValue("a"), is(nullValue()));
	}

	@Test
	public void metadataValue_value() {
		// GIVEN
		BasicIdentifiable ident = new BasicIdentifiable();
		ident.setMetadata(testData());

		// THEN
		assertThat("Value returned from matching key", ident.metadataValue("b"), is("bee"));
	}

	@Test
	public void smartMetadataValue_nullValue() {
		// GIVEN
		BasicIdentifiable ident = new BasicIdentifiable();
		ident.setMetadata(testData());

		// THEN
		assertThat("Null returned from matching key", ident.smartMetadataValue("a"), is(nullValue()));
	}

	@Test
	public void smartMetadataValue_string() {
		// GIVEN
		BasicIdentifiable ident = new BasicIdentifiable();
		ident.setMetadata(testData());

		// THEN
		assertThat("Value returned from matching key", ident.smartMetadataValue("b"), is("bee"));
	}

	@Test
	public void smartMetadataValue_integer() {
		// GIVEN
		BasicIdentifiable ident = new BasicIdentifiable();
		ident.setMetadata(testData());

		// THEN
		assertThat("Int value returned from matching key", ident.smartMetadataValue("int"),
				is(TEST_INT));
	}

	@Test
	public void smartMetadataValue_float() {
		// GIVEN
		BasicIdentifiable ident = new BasicIdentifiable();
		ident.setMetadata(testData());

		// THEN
		assertThat("Float value returned from matching key", ident.smartMetadataValue("float"),
				is(TEST_FLOAT));
	}

	@Test
	public void smartMetadataValue_double() {
		// GIVEN
		BasicIdentifiable ident = new BasicIdentifiable();
		ident.setMetadata(testData());

		// THEN
		assertThat("Double value returned from matching key", ident.smartMetadataValue("double"),
				is(TEST_DOUBLE));
	}

	@Test
	public void smartMetadataValue_decimal() {
		// GIVEN
		BasicIdentifiable ident = new BasicIdentifiable();
		ident.setMetadata(testData());

		// THEN
		assertThat("Double value returned from matching key", ident.smartMetadataValue("decimal"),
				is(TEST_DECIMAL));
	}

	@Test
	public void numberMetadataValue_nullValue() {
		// GIVEN
		BasicIdentifiable ident = new BasicIdentifiable();
		ident.setMetadata(testData());

		// THEN
		assertThat("Null returned from matching key", ident.numberMetadataValue("a"), is(nullValue()));
	}

	@Test
	public void numberMetadataValue_string() {
		// GIVEN
		BasicIdentifiable ident = new BasicIdentifiable();
		ident.setMetadata(testData());

		// THEN
		assertThat("Null returned from matching key with string value", ident.numberMetadataValue("b"),
				is(nullValue()));
	}

	@Test
	public void numberMetadataValue_integer() {
		// GIVEN
		BasicIdentifiable ident = new BasicIdentifiable();
		ident.setMetadata(testData());

		// THEN
		assertThat("Int value returned from matching key", ident.numberMetadataValue("int"),
				is(TEST_INT));
	}

	@Test
	public void numberMetadataValue_float() {
		// GIVEN
		BasicIdentifiable ident = new BasicIdentifiable();
		ident.setMetadata(testData());

		// THEN
		assertThat("Float value returned from matching key", ident.numberMetadataValue("float"),
				is(TEST_FLOAT));
	}

	@Test
	public void numberMetadataValue_double() {
		// GIVEN
		BasicIdentifiable ident = new BasicIdentifiable();
		ident.setMetadata(testData());

		// THEN
		assertThat("Double value returned from matching key", ident.numberMetadataValue("double"),
				is(TEST_DOUBLE));
	}

	@Test
	public void numberMetadataValue_decimal() {
		// GIVEN
		BasicIdentifiable ident = new BasicIdentifiable();
		ident.setMetadata(testData());

		// THEN
		assertThat("Double value returned from matching key", ident.numberMetadataValue("decimal"),
				is(TEST_DECIMAL));
	}

	@Test
	public void saveMetadataValue_key_nullArray_noCreate() {
		// GIVEN
		BasicIdentifiable ident = new BasicIdentifiable();

		// WHEN
		ident.saveMetadataValue("foo", "bar", false);

		// THEN
		assertThat("Null array stays null without create", ident.getMetadata(), is(nullValue()));
	}

	@Test
	public void saveMetadataValue_key_nullArray_create() {
		// GIVEN
		BasicIdentifiable ident = new BasicIdentifiable();

		// WHEN
		ident.saveMetadataValue("foo", "bar", true);

		// THEN
		assertThat("Array created with values", ident.getMetadata(),
				is(arrayContaining(new KeyValuePair("foo", "bar"))));
	}

	@Test
	public void saveMetadataValue_key_noMatch_noCreate() {
		// GIVEN
		BasicIdentifiable ident = new BasicIdentifiable();
		ident.setMetadata(testData());

		// WHEN
		ident.saveMetadataValue("foo", "bar", false);

		// THEN
		assertThat("Array unchanged without create", ident.getMetadata(),
				is(arrayContaining(testData())));
	}

	@Test
	public void saveMetadataValue_key_noMatch_create() {
		// GIVEN
		BasicIdentifiable ident = new BasicIdentifiable();
		ident.setMetadata(testData());

		// WHEN
		ident.saveMetadataValue("foo", "bar", true);

		// THEN
		KeyValuePair[] expected = testData();
		expected = arrayWithLength(expected, expected.length + 1, KeyValuePair.class, KeyValuePair::new);
		expected[expected.length - 1].setKey("foo");
		expected[expected.length - 1].setValue("bar");

		assertThat("Array expanded with new value", ident.getMetadata(), is(arrayContaining(expected)));
	}

	@Test
	public void saveMetadataValue_key_match() {
		// GIVEN
		BasicIdentifiable ident = new BasicIdentifiable();
		ident.setMetadata(testData());

		// WHEN
		ident.saveMetadataValue("a", "bar", true);

		// THEN
		KeyValuePair[] expected = testData();
		expected[1].setValue("bar");

		assertThat("Array updated with new value", ident.getMetadata(), is(arrayContaining(expected)));
	}

	@Test
	public void saveMetadataValue_index_exists() {
		// GIVEN
		BasicIdentifiable ident = new BasicIdentifiable();
		ident.setMetadata(testData());

		// WHEN
		ident.saveMetadataValue(1, "eh", "bar", true);

		// THEN
		KeyValuePair[] expected = testData();
		expected[1].setKey("eh");
		expected[1].setValue("bar");

		assertThat("Array updated with new value", ident.getMetadata(), is(arrayContaining(expected)));
	}

	@Test
	public void saveMetadataValue_index_notExists_noCreate() {
		// GIVEN
		BasicIdentifiable ident = new BasicIdentifiable();
		ident.setMetadata(testData());

		// WHEN
		ident.saveMetadataValue(ident.getMetadataCount() + 1, "new", "one", false);

		// THEN
		KeyValuePair[] expected = testData();
		assertThat("Array not updated without create", ident.getMetadata(),
				is(arrayContaining(expected)));
	}

	@Test
	public void saveMetadataValue_index_notExists_create() {
		// GIVEN
		BasicIdentifiable ident = new BasicIdentifiable();
		ident.setMetadata(testData());

		// WHEN
		ident.saveMetadataValue(ident.getMetadataCount(), "new", "one", true);

		// THEN
		KeyValuePair[] expected = testData();
		expected = arrayWithLength(expected, expected.length + 1, KeyValuePair.class, KeyValuePair::new);
		expected[expected.length - 1].setKey("new");
		expected[expected.length - 1].setValue("one");

		assertThat("Array expanded with create", ident.getMetadata(), is(arrayContaining(expected)));
	}

}

/* ==================================================================
 * TemporalPropertySerializer.java - 2/10/2021 10:06:16 PM
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

package net.solarnetwork.codec.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import net.solarnetwork.codec.TemporalPropertySerializer;

/**
 * Test cases for the {@link TemporalPropertySerializer} class.
 * 
 * @author matt
 * @version 1.0
 */
public class TemporalPropertySerializerTests {

	@Test
	public void formatInstant() {
		LocalDateTime d = LocalDateTime.of(2021, 10, 2, 22, 10, 23,
				(int) TimeUnit.MILLISECONDS.toNanos(456));
		assertThat(
				"Instant formatted", new TemporalPropertySerializer(DateTimeFormatter.ISO_INSTANT)
						.serialize(null, null, d.toInstant(ZoneOffset.UTC)),
				is("2021-10-02T22:10:23.456Z"));
	}

	@Test
	public void formatInstant_pat() {
		LocalDateTime d = LocalDateTime.of(2021, 10, 2, 22, 10, 23,
				(int) TimeUnit.MILLISECONDS.toNanos(456));
		assertThat(
				"Instant formatted", new TemporalPropertySerializer("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
						.serialize(null, null, d.toInstant(ZoneOffset.UTC)),
				is("2021-10-02T22:10:23.456Z"));
	}

	@Test
	public void formatLocalDateTime() {
		LocalDateTime d = LocalDateTime.of(2021, 10, 2, 22, 10, 23,
				(int) TimeUnit.MILLISECONDS.toNanos(456));
		assertThat("Instant formatted",
				new TemporalPropertySerializer("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").serialize(null, null, d),
				is("2021-10-02T22:10:23.456Z"));
	}

	@Test
	public void formatDate() {
		LocalDateTime d = LocalDateTime.of(2021, 10, 2, 22, 10, 23,
				(int) TimeUnit.MILLISECONDS.toNanos(456));
		assertThat("Instant formatted", new TemporalPropertySerializer("yyyy-MM-dd").serialize(null,
				null, d.toInstant(ZoneOffset.UTC)), is("2021-10-02"));
	}

	@Test
	public void formatTime() {
		LocalDateTime d = LocalDateTime.of(2021, 10, 2, 22, 10, 23,
				(int) TimeUnit.MILLISECONDS.toNanos(456));
		assertThat("Instant formatted", new TemporalPropertySerializer("HH:mm:ss.SSS").serialize(null,
				null, d.toInstant(ZoneOffset.UTC)), is("22:10:23.456"));
	}

}

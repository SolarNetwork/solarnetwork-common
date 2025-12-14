/* ==================================================================
 * BasicSecurityPolicyTests.java - 9/10/2016 8:49:36 AM
 *
 * Copyright 2007-2016 SolarNetwork.net Dev Team
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

package net.solarnetwork.domain.test;

import static net.solarnetwork.codec.JsonUtils.newObjectMapper;
import static org.assertj.core.api.BDDAssertions.from;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenExceptionOfType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.solarnetwork.codec.JsonUtils;
import net.solarnetwork.domain.BasicSecurityPolicy;
import net.solarnetwork.domain.LocationPrecision;
import net.solarnetwork.domain.SecurityPolicy;
import net.solarnetwork.domain.datum.Aggregation;

/**
 * Test cases for the {@link BasicSecurityPolicy} class.
 *
 * @author matt
 * @version 1.1
 */
public class BasicSecurityPolicyTests {

	private static final Instant TEST_DATE = LocalDateTime.of(2018, 5, 30, 10, 30, 0)
			.toInstant(ZoneOffset.UTC);

	@Test
	public void buildNodeIdsPolicy() {
		Set<Long> nodeIds = new HashSet<Long>(Arrays.asList(1L, 2L, 3L));
		BasicSecurityPolicy policy = BasicSecurityPolicy.builder().withNodeIds(nodeIds).build();
		assertThat("Node ID set", policy.getNodeIds(), is(nodeIds));
		thenExceptionOfType(UnsupportedOperationException.class).as("Node ID set should be immutable")
				.isThrownBy(() -> policy.getNodeIds().add(-1L));
	}

	@Test
	public void buildMergedNodeIdsPolicy() {
		Set<Long> nodeIds = new HashSet<Long>(Arrays.asList(1L, 2L, 3L));
		Set<Long> additionalNodeIds = new HashSet<Long>(Arrays.asList(3L, 4L, 5L));
		Set<Long> merged = new HashSet<Long>(Arrays.asList(1L, 2L, 3L, 4L, 5L));
		BasicSecurityPolicy policy = BasicSecurityPolicy.builder().withNodeIds(nodeIds)
				.withMergedNodeIds(additionalNodeIds).build();
		assertThat("Node ID set", policy.getNodeIds(), is(merged));
		thenExceptionOfType(UnsupportedOperationException.class).as("Node ID set should be immutable")
				.isThrownBy(() -> policy.getNodeIds().add(-1L));
	}

	@Test
	public void buildIntersectNodeIdsPolicy() {
		// GIVEN
		Set<Long> nodeIds = new HashSet<Long>(Arrays.asList(1L, 2L, 3L));
		Set<Long> additionalNodeIds = new HashSet<Long>(Arrays.asList(3L, 4L, 5L));
		Set<Long> merged = new HashSet<Long>(Arrays.asList(3L));

		// WHEN
		BasicSecurityPolicy policy = BasicSecurityPolicy.builder().withNodeIds(nodeIds)
				.withIntersectNodeIds(additionalNodeIds).build();

		// THEN
		assertThat("Node ID set", policy.getNodeIds(), is(merged));
		thenExceptionOfType(UnsupportedOperationException.class).as("Node ID set should be immutable")
				.isThrownBy(() -> policy.getNodeIds().add(-1L));
	}

	@Test
	public void buildIntersectNodeIdsPolicy_emptyStart() {
		// GIVEN
		Set<Long> additionalNodeIds = new HashSet<Long>(Arrays.asList(3L, 4L, 5L));

		// WHEN
		BasicSecurityPolicy policy = BasicSecurityPolicy.builder()
				.withIntersectNodeIds(additionalNodeIds).build();

		// THEN
		then(policy.getNodeIds()).as("Node IDs set to additional because no starting restriction")
				.isEqualTo(additionalNodeIds);
		thenExceptionOfType(UnsupportedOperationException.class).as("Node ID set should be immutable")
				.isThrownBy(() -> policy.getNodeIds().add(-1L));
	}

	@Test
	public void buildIntersectNodeIdsPolicy_noOverlap() {
		// GIVEN
		Set<Long> nodeIds = new HashSet<Long>(Arrays.asList(1L, 2L, 3L));
		Set<Long> additionalNodeIds = new HashSet<Long>(Arrays.asList(4L, 5L));

		// THEN
		thenExceptionOfType(IllegalArgumentException.class).as("Disjoint merge not allowed")
				.isThrownBy(() -> BasicSecurityPolicy.builder().withNodeIds(nodeIds)
						.withIntersectNodeIds(additionalNodeIds).build());
	}

	@Test
	public void buildSourceIdsPolicy() {
		Set<String> sourceIds = new HashSet<String>(Arrays.asList("one", "two", "three"));
		BasicSecurityPolicy policy = BasicSecurityPolicy.builder().withSourceIds(sourceIds).build();
		assertThat("Source ID set", policy.getSourceIds(), is(sourceIds));
		thenExceptionOfType(UnsupportedOperationException.class).as("Source ID set should be immutable")
				.isThrownBy(() -> policy.getSourceIds().add("no"));
	}

	@Test
	public void buildMergedSourceIdsPolicy() {
		Set<String> sourceIds = new HashSet<String>(Arrays.asList("one", "two", "three"));
		Set<String> additionalSourceIds = new HashSet<String>(Arrays.asList("three", "four", "five"));
		Set<String> merged = new HashSet<String>(Arrays.asList("one", "two", "three", "four", "five"));
		BasicSecurityPolicy policy = BasicSecurityPolicy.builder().withSourceIds(sourceIds)
				.withMergedSourceIds(additionalSourceIds).build();
		assertThat("Source ID set", policy.getSourceIds(), is(merged));
		thenExceptionOfType(UnsupportedOperationException.class).as("Source ID set should be immutable")
				.isThrownBy(() -> policy.getSourceIds().add("no"));
	}

	@Test
	public void buildIntersectSourceIdsPolicy() {
		// GIVEN
		Set<String> sourceIds = new HashSet<String>(Arrays.asList("one", "two", "three"));
		Set<String> additionalSourceIds = new HashSet<String>(Arrays.asList("three", "four", "five"));
		Set<String> merged = new HashSet<String>(Arrays.asList("three"));

		// WHEN
		BasicSecurityPolicy policy = BasicSecurityPolicy.builder().withSourceIds(sourceIds)
				.withIntersectSourceIds(additionalSourceIds).build();

		// THEN
		assertThat("Node ID set", policy.getSourceIds(), is(merged));
		thenExceptionOfType(UnsupportedOperationException.class).as("Node ID set should be immutable")
				.isThrownBy(() -> policy.getSourceIds().add("o"));
	}

	@Test
	public void buildIntersectSourceIdsPolicy_emptyStart() {
		// GIVEN
		Set<String> additionalSourceIds = new HashSet<String>(Arrays.asList("three", "four", "five"));

		// WHEN
		BasicSecurityPolicy policy = BasicSecurityPolicy.builder()
				.withIntersectSourceIds(additionalSourceIds).build();

		// THEN
		then(policy.getSourceIds()).as("Node IDs set to additional because no starting restriction")
				.isEqualTo(additionalSourceIds);
		thenExceptionOfType(UnsupportedOperationException.class).as("Node ID set should be immutable")
				.isThrownBy(() -> policy.getSourceIds().add("o"));
	}

	@Test
	public void buildIntersectSourceIdsPolicy_noOverlap() {
		// GIVEN
		Set<String> sourceIds = new HashSet<String>(Arrays.asList("one", "two", "three"));
		Set<String> additionalSourceIds = new HashSet<String>(Arrays.asList("four", "five"));

		// THEN
		thenExceptionOfType(IllegalArgumentException.class).as("Disjoint merge not allowed")
				.isThrownBy(() -> BasicSecurityPolicy.builder().withSourceIds(sourceIds)
						.withIntersectSourceIds(additionalSourceIds).build());
	}

	@Test
	public void buildNodeMetadataPathsPolicy() {
		Set<String> paths = new HashSet<String>(Arrays.asList("one", "two", "three"));
		BasicSecurityPolicy policy = BasicSecurityPolicy.builder().withNodeMetadataPaths(paths).build();
		assertThat("Node metadata path set", policy.getNodeMetadataPaths(), is(paths));
		thenExceptionOfType(UnsupportedOperationException.class)
				.as("Node metadata path set should be immutable")
				.isThrownBy(() -> policy.getNodeMetadataPaths().add("no"));
	}

	@Test
	public void buildMergedNodeMetadataPathsPolicy() {
		Set<String> paths = new HashSet<String>(Arrays.asList("one", "two", "three"));
		Set<String> additionalPaths = new HashSet<String>(Arrays.asList("three", "four", "five"));
		Set<String> merged = new HashSet<String>(Arrays.asList("one", "two", "three", "four", "five"));
		BasicSecurityPolicy policy = BasicSecurityPolicy.builder().withNodeMetadataPaths(paths)
				.withMergedNodeMetadataPaths(additionalPaths).build();
		assertThat("Node metadata path set", policy.getNodeMetadataPaths(), is(merged));
		thenExceptionOfType(UnsupportedOperationException.class)
				.as("Node metadata path set should be immutable")
				.isThrownBy(() -> policy.getNodeMetadataPaths().add("no"));
	}

	@Test
	public void buildIntersectNodeMetadataPathsPolicy() {
		// GIVEN
		Set<String> paths = new HashSet<String>(Arrays.asList("one", "two", "three"));
		Set<String> additionalPaths = new HashSet<String>(Arrays.asList("three", "four", "five"));
		Set<String> merged = new HashSet<String>(Arrays.asList("three"));

		// WHEN
		BasicSecurityPolicy policy = BasicSecurityPolicy.builder().withNodeMetadataPaths(paths)
				.withIntersectNodeMetadataPaths(additionalPaths).build();

		// THEN
		then(policy.getNodeMetadataPaths()).as("Api path set to intersection").isEqualTo(merged);
		thenExceptionOfType(UnsupportedOperationException.class)
				.as("Node metadata path set should be immutable")
				.isThrownBy(() -> policy.getNodeMetadataPaths().add("no"));
	}

	@Test
	public void buildIntersectNodeMetadataPathsPolicy_emptyStart() {
		// GIVEN
		Set<String> additionalPaths = new HashSet<String>(Arrays.asList("three", "four", "five"));

		// WHEN
		BasicSecurityPolicy policy = BasicSecurityPolicy.builder()
				.withIntersectNodeMetadataPaths(additionalPaths).build();

		// THEN
		then(policy.getNodeMetadataPaths())
				.as("Node metadata path set to additional because no starting restriction")
				.isEqualTo(additionalPaths);
		thenExceptionOfType(UnsupportedOperationException.class)
				.as("Node metadata path set should be immutable")
				.isThrownBy(() -> policy.getNodeMetadataPaths().add("no"));
	}

	@Test
	public void buildIntersectNodeMetadataPathsPolicy_noOverlap() {
		// GIVEN
		Set<String> paths = new HashSet<String>(Arrays.asList("one", "two", "three"));
		Set<String> additionalPaths = new HashSet<String>(Arrays.asList("four", "five"));

		// THEN
		thenExceptionOfType(IllegalArgumentException.class).as("Disjoint merge not allowed")
				.isThrownBy(() -> BasicSecurityPolicy.builder().withNodeMetadataPaths(paths)
						.withIntersectNodeMetadataPaths(additionalPaths).build());
	}

	@Test
	public void buildUserMetadataPathsPolicy() {
		Set<String> paths = new HashSet<String>(Arrays.asList("one", "two", "three"));
		BasicSecurityPolicy policy = BasicSecurityPolicy.builder().withUserMetadataPaths(paths).build();
		then(policy.getUserMetadataPaths()).as("user metadata path set").isEqualTo(paths);
		thenExceptionOfType(UnsupportedOperationException.class)
				.as("User metadata path set should be immutable")
				.isThrownBy(() -> policy.getUserMetadataPaths().add("no"));
	}

	@Test
	public void buildMergedUserMetadataPathsPolicy() {
		Set<String> paths = new HashSet<String>(Arrays.asList("one", "two", "three"));
		Set<String> additionalPaths = new HashSet<String>(Arrays.asList("three", "four", "five"));
		Set<String> merged = new HashSet<String>(Arrays.asList("one", "two", "three", "four", "five"));
		BasicSecurityPolicy policy = BasicSecurityPolicy.builder().withUserMetadataPaths(paths)
				.withMergedUserMetadataPaths(additionalPaths).build();
		then(policy.getUserMetadataPaths()).as("user metadata path set").isEqualTo(merged);
		thenExceptionOfType(UnsupportedOperationException.class)
				.as("User metadata path set should be immutable")
				.isThrownBy(() -> policy.getUserMetadataPaths().add("no"));
	}

	@Test
	public void buildIntersectUserMetadataPathsPolicy() {
		// GIVEN
		Set<String> paths = new HashSet<String>(Arrays.asList("one", "two", "three"));
		Set<String> additionalPaths = new HashSet<String>(Arrays.asList("three", "four", "five"));
		Set<String> merged = new HashSet<String>(Arrays.asList("three"));

		// WHEN
		BasicSecurityPolicy policy = BasicSecurityPolicy.builder().withUserMetadataPaths(paths)
				.withIntersectUserMetadataPaths(additionalPaths).build();

		// THEN
		then(policy.getUserMetadataPaths()).as("Api path set to intersection").isEqualTo(merged);
		thenExceptionOfType(UnsupportedOperationException.class)
				.as("User metadata path set should be immutable")
				.isThrownBy(() -> policy.getUserMetadataPaths().add("no"));
	}

	@Test
	public void buildIntersectUserMetadataPathsPolicy_emptyStart() {
		// GIVEN
		Set<String> additionalPaths = new HashSet<String>(Arrays.asList("three", "four", "five"));

		// WHEN
		BasicSecurityPolicy policy = BasicSecurityPolicy.builder()
				.withIntersectUserMetadataPaths(additionalPaths).build();

		// THEN
		then(policy.getUserMetadataPaths())
				.as("User metadata path set to additional because no starting restriction")
				.isEqualTo(additionalPaths);
		thenExceptionOfType(UnsupportedOperationException.class)
				.as("User metadata path set should be immutable")
				.isThrownBy(() -> policy.getUserMetadataPaths().add("no"));
	}

	@Test
	public void buildIntersectUserMetadataPathsPolicy_noOverlap() {
		// GIVEN
		Set<String> paths = new HashSet<String>(Arrays.asList("one", "two", "three"));
		Set<String> additionalPaths = new HashSet<String>(Arrays.asList("four", "five"));

		// THEN
		thenExceptionOfType(IllegalArgumentException.class).as("Disjoint merge not allowed")
				.isThrownBy(() -> BasicSecurityPolicy.builder().withUserMetadataPaths(paths)
						.withIntersectUserMetadataPaths(additionalPaths).build());
	}

	@Test
	public void buildApiPathsPolicy() {
		Set<String> paths = new HashSet<String>(Arrays.asList("one", "two", "three"));
		BasicSecurityPolicy policy = BasicSecurityPolicy.builder().withApiPaths(paths).build();
		then(policy.getApiPaths()).as("Api path set").isEqualTo(paths);
		thenExceptionOfType(UnsupportedOperationException.class).as("Api path set should be immutable")
				.isThrownBy(() -> policy.getApiPaths().add("no"));
	}

	@Test
	public void buildMergedApiPathsPolicy() {
		Set<String> paths = new HashSet<String>(Arrays.asList("one", "two", "three"));
		Set<String> additionalPaths = new HashSet<String>(Arrays.asList("three", "four", "five"));
		Set<String> merged = new HashSet<String>(Arrays.asList("one", "two", "three", "four", "five"));
		BasicSecurityPolicy policy = BasicSecurityPolicy.builder().withApiPaths(paths)
				.withMergedApiPaths(additionalPaths).build();
		then(policy.getApiPaths()).as("Api path set").isEqualTo(merged);
		thenExceptionOfType(UnsupportedOperationException.class).as("Api path set should be immutable")
				.isThrownBy(() -> policy.getApiPaths().add("no"));
	}

	@Test
	public void buildIntersectApiPathsPolicy() {
		// GIVEN
		Set<String> paths = new HashSet<String>(Arrays.asList("one", "two", "three"));
		Set<String> additionalPaths = new HashSet<String>(Arrays.asList("three", "four", "five"));
		Set<String> merged = new HashSet<String>(Arrays.asList("three"));

		// WHEN
		BasicSecurityPolicy policy = BasicSecurityPolicy.builder().withApiPaths(paths)
				.withIntersectApiPaths(additionalPaths).build();

		// THEN
		then(policy.getApiPaths()).as("Api path set to intersection").isEqualTo(merged);
		thenExceptionOfType(UnsupportedOperationException.class).as("Api path set should be immutable")
				.isThrownBy(() -> policy.getApiPaths().add("no"));
	}

	@Test
	public void buildIntersectApiPathsPolicy_emptyStart() {
		// GIVEN
		Set<String> additionalPaths = new HashSet<String>(Arrays.asList("three", "four", "five"));

		// WHEN
		BasicSecurityPolicy policy = BasicSecurityPolicy.builder().withIntersectApiPaths(additionalPaths)
				.build();

		// THEN
		then(policy.getApiPaths()).as("Api path set to additional because no starting restriction")
				.isEqualTo(additionalPaths);
		thenExceptionOfType(UnsupportedOperationException.class).as("Api path set should be immutable")
				.isThrownBy(() -> policy.getApiPaths().add("no"));
	}

	@Test
	public void buildIntersectApiPathsPolicy_noOverlap() {
		// GIVEN
		Set<String> paths = new HashSet<String>(Arrays.asList("one", "two", "three"));
		Set<String> additionalPaths = new HashSet<String>(Arrays.asList("four", "five"));

		// THEN
		thenExceptionOfType(IllegalArgumentException.class).as("Disjoint merge not allowed")
				.isThrownBy(() -> BasicSecurityPolicy.builder().withApiPaths(paths)
						.withIntersectApiPaths(additionalPaths).build());
	}

	@Test
	public void buildMinAggregationPolicy() {
		BasicSecurityPolicy policy = BasicSecurityPolicy.builder().withMinAggregation(Aggregation.Month)
				.build();
		assertThat("Minimum aggregation set", policy.getAggregations(),
				contains(Aggregation.Month, Aggregation.Year, Aggregation.RunningTotal));
		thenExceptionOfType(UnsupportedOperationException.class)
				.as("Aggregation set should be immutable")
				.isThrownBy(() -> policy.getAggregations().add(Aggregation.Minute));
	}

	@Test
	public void buildMergedMinAggregationPolicy() {
		BasicSecurityPolicy orig = BasicSecurityPolicy.builder().withMinAggregation(Aggregation.Month)
				.build();
		BasicSecurityPolicy patch = BasicSecurityPolicy.builder().withMinAggregation(Aggregation.Day)
				.build();
		BasicSecurityPolicy policy = BasicSecurityPolicy.builder().withPolicy(orig)
				.withMergedPolicy(patch).build();
		assertThat("Minimum aggregation", policy.getMinAggregation(), is(Aggregation.Day));
		assertThat("Minimum aggregation set", policy.getAggregations(),
				containsInAnyOrder(Aggregation.Day, Aggregation.DayOfYear, Aggregation.DayOfWeek,
						Aggregation.SeasonalDayOfWeek, Aggregation.Week, Aggregation.WeekOfYear,
						Aggregation.Month, Aggregation.Year, Aggregation.RunningTotal));
		thenExceptionOfType(UnsupportedOperationException.class)
				.as("Aggregation set should be immutable")
				.isThrownBy(() -> policy.getAggregations().add(Aggregation.Minute));
	}

	@Test
	public void buildMinAggregationPolicyWithCache() {
		BasicSecurityPolicy policy = BasicSecurityPolicy.builder().withMinAggregation(Aggregation.Month)
				.build();
		BasicSecurityPolicy policy2 = BasicSecurityPolicy.builder().withMinAggregation(Aggregation.Month)
				.build();
		assertThat("Cached minimum aggregation set", policy2.getAggregations(),
				is(sameInstance(policy.getAggregations())));
	}

	@Test
	public void buildIntersecNotAfter_before() {
		// GIVEN
		final Instant expire = Instant.now().truncatedTo(ChronoUnit.MINUTES);
		BasicSecurityPolicy orig = BasicSecurityPolicy.builder().withNotAfter(expire).build();
		BasicSecurityPolicy patch = BasicSecurityPolicy.builder().withNotAfter(expire.minusSeconds(1L))
				.build();

		// WHEN
		BasicSecurityPolicy policy = BasicSecurityPolicy.builder().withPolicy(orig)
				.withIntersectPolicy(patch).build();

		// THEN
		// @formatter:off
		then(policy)
			.as("Expiring earlier than source policy is allowed")
			.returns(patch.getNotAfter(), from(BasicSecurityPolicy::getNotAfter))
			;
		// @formatter:on
	}

	@Test
	public void buildIntersecNotAfter_after() {
		// GIVEN
		final Instant expire = Instant.now().truncatedTo(ChronoUnit.MINUTES);
		BasicSecurityPolicy orig = BasicSecurityPolicy.builder().withNotAfter(expire).build();
		BasicSecurityPolicy patch = BasicSecurityPolicy.builder().withNotAfter(expire.plusSeconds(1L))
				.build();

		// WHEN
		BasicSecurityPolicy policy = BasicSecurityPolicy.builder().withPolicy(orig)
				.withIntersectPolicy(patch).build();

		// THEN
		// @formatter:off
		then(policy)
			.as("Expiring later than source policy is restricted to source")
			.returns(orig.getNotAfter(), from(BasicSecurityPolicy::getNotAfter))
			;
		// @formatter:on
	}

	@Test
	public void buildIntersecRefreshAllowed_yesNo() {
		// GIVEN
		BasicSecurityPolicy orig = BasicSecurityPolicy.builder().withRefreshAllowed(true).build();
		BasicSecurityPolicy patch = BasicSecurityPolicy.builder().withRefreshAllowed(false).build();

		// WHEN
		BasicSecurityPolicy policy = BasicSecurityPolicy.builder().withPolicy(orig)
				.withIntersectPolicy(patch).build();

		// THEN
		// @formatter:off
		then(policy)
			.as("Disabling refresh allowed is allowed")
			.returns(false, from(BasicSecurityPolicy::getRefreshAllowed))
			;
		// @formatter:on
	}

	@Test
	public void buildIntersecRefreshAllowed_yesYes() {
		// GIVEN
		BasicSecurityPolicy orig = BasicSecurityPolicy.builder().withRefreshAllowed(true).build();
		BasicSecurityPolicy patch = BasicSecurityPolicy.builder().withRefreshAllowed(true).build();

		// WHEN
		BasicSecurityPolicy policy = BasicSecurityPolicy.builder().withPolicy(orig)
				.withIntersectPolicy(patch).build();

		// THEN
		// @formatter:off
		then(policy)
			.as("Keeping refresh allowed is allowed")
			.returns(true, from(BasicSecurityPolicy::getRefreshAllowed))
			;
		// @formatter:on
	}

	@Test
	public void buildIntersecRefreshAllowed_noYes() {
		// GIVEN
		BasicSecurityPolicy orig = BasicSecurityPolicy.builder().withRefreshAllowed(false).build();
		BasicSecurityPolicy patch = BasicSecurityPolicy.builder().withRefreshAllowed(true).build();

		// WHEN
		BasicSecurityPolicy policy = BasicSecurityPolicy.builder().withPolicy(orig)
				.withIntersectPolicy(patch).build();

		// THEN
		// @formatter:off
		then(policy)
			.as("Enabling refresh allowed is denied")
			.returns(false, from(BasicSecurityPolicy::getRefreshAllowed))
			;
		// @formatter:on
	}

	@Test
	public void buildIntersecRefreshAllowed_nullNo() {
		// GIVEN
		BasicSecurityPolicy orig = BasicSecurityPolicy.builder().build();
		BasicSecurityPolicy patch = BasicSecurityPolicy.builder().withRefreshAllowed(false).build();

		// WHEN
		BasicSecurityPolicy policy = BasicSecurityPolicy.builder().withPolicy(orig)
				.withIntersectPolicy(patch).build();

		// THEN
		// @formatter:off
		then(policy)
			.as("Keeping refresh not allowed is allowed")
			.returns(false, from(BasicSecurityPolicy::getRefreshAllowed))
			;
		// @formatter:on
	}

	@Test
	public void buildIntersecRefreshAllowed_nullYes() {
		// GIVEN
		BasicSecurityPolicy orig = BasicSecurityPolicy.builder().build();
		BasicSecurityPolicy patch = BasicSecurityPolicy.builder().withRefreshAllowed(true).build();

		// WHEN
		BasicSecurityPolicy policy = BasicSecurityPolicy.builder().withPolicy(orig)
				.withIntersectPolicy(patch).build();

		// THEN
		// @formatter:off
		then(policy)
			.as("Enabling refresh allowed is denied")
			.returns(false, from(BasicSecurityPolicy::getRefreshAllowed))
			;
		// @formatter:on
	}

	@Test
	public void buildIntersectMinAggregationPolicy_narrower() {
		// GIVEN
		BasicSecurityPolicy orig = BasicSecurityPolicy.builder().withMinAggregation(Aggregation.Month)
				.build();
		BasicSecurityPolicy patch = BasicSecurityPolicy.builder().withMinAggregation(Aggregation.Day)
				.build();

		// WHEN
		BasicSecurityPolicy policy = BasicSecurityPolicy.builder().withPolicy(orig)
				.withIntersectPolicy(patch).build();

		// THEN
		// @formatter:off
		then(policy)
			.as("Minimim aggregation is restricted to source policy minimum")
			.returns(Aggregation.Month, from(BasicSecurityPolicy::getMinAggregation))
			;
		// @formatter:on
	}

	@Test
	public void buildIntersectMinAggregationPolicy_wider() {
		// GIVEN
		BasicSecurityPolicy orig = BasicSecurityPolicy.builder().withMinAggregation(Aggregation.Month)
				.build();
		BasicSecurityPolicy patch = BasicSecurityPolicy.builder().withMinAggregation(Aggregation.Year)
				.build();

		// WHEN
		BasicSecurityPolicy policy = BasicSecurityPolicy.builder().withPolicy(orig)
				.withIntersectPolicy(patch).build();

		// THEN
		// @formatter:off
		then(policy)
			.as("Minimim aggregation is allowed to be wider than source policy minimum")
			.returns(Aggregation.Year, from(BasicSecurityPolicy::getMinAggregation))
			;
		// @formatter:on
	}

	@Test
	public void buildMinLocationPrecisionPolicy() {
		BasicSecurityPolicy policy = BasicSecurityPolicy.builder()
				.withMinLocationPrecision(LocationPrecision.PostalCode).build();
		then(policy.getLocationPrecisions()).as("Minimum location precision set").contains(
				LocationPrecision.PostalCode, LocationPrecision.Locality, LocationPrecision.Region,
				LocationPrecision.StateOrProvince, LocationPrecision.TimeZone,
				LocationPrecision.Country);
		thenExceptionOfType(UnsupportedOperationException.class)
				.as("LocationPrecition set should be immutable")
				.isThrownBy(() -> policy.getLocationPrecisions().add(LocationPrecision.LatLong));
	}

	@Test
	public void buildMinLocationPrecisionPolicyWithCache() {
		BasicSecurityPolicy policy = BasicSecurityPolicy.builder()
				.withMinLocationPrecision(LocationPrecision.PostalCode).build();
		BasicSecurityPolicy policy2 = BasicSecurityPolicy.builder()
				.withMinLocationPrecision(LocationPrecision.PostalCode).build();
		assertThat("Cached minimum location precision set", policy2.getLocationPrecisions(),
				is(sameInstance(policy.getLocationPrecisions())));
	}

	@Test
	public void buildExpiringPolicy() {
		BasicSecurityPolicy policy = BasicSecurityPolicy.builder().withNotAfter(TEST_DATE)
				.withRefreshAllowed(true).build();
		assertThat("Not after set", TEST_DATE, equalTo(policy.getNotAfter()));
		assertThat("Refresh allowed set", policy.getRefreshAllowed(), equalTo(true));
		assertThat("Valid before", policy.isValidAt(TEST_DATE.plusMillis(-1)), equalTo(true));
		assertThat("Valid at", policy.isValidAt(TEST_DATE), equalTo(true));
		assertThat("Not valid after", policy.isValidAt(TEST_DATE.plusMillis(1)), equalTo(false));
	}

	@Test
	public void parseJsonNodeIdPolicy() throws Exception {
		SecurityPolicy policy = newObjectMapper().readValue("{\"nodeIds\":[1,2,3]}",
				SecurityPolicy.class);
		assertThat("Node ID set", policy.getNodeIds(), contains(1L, 2L, 3L));
	}

	@Test
	public void parseJsonEmptyNodeIdPolicy() throws Exception {
		SecurityPolicy policy = newObjectMapper().readValue("{\"nodeIds\":[]}", SecurityPolicy.class);
		assertThat("Node ID set null instead of empty set", policy.getNodeIds(), is(nullValue()));
	}

	@Test
	public void parseJsonSourceIdPolicy() throws Exception {
		SecurityPolicy policy = newObjectMapper()
				.readValue("{\"sourceIds\":[\"one\",\"two\",\"three\"]}", SecurityPolicy.class);
		assertThat("Source ID set", policy.getSourceIds(), contains("one", "two", "three"));
	}

	@Test
	public void parseJsonMinAggregationPolicy() throws Exception {
		SecurityPolicy policy = newObjectMapper().readValue("{\"minAggregation\":\"Month\"}",
				SecurityPolicy.class);
		assertThat("Minimum aggregation set", policy.getAggregations(),
				containsInAnyOrder(Aggregation.Month, Aggregation.Year, Aggregation.RunningTotal));
	}

	@Test
	public void parseJsonAggregationPolicy() throws Exception {
		SecurityPolicy policy = newObjectMapper().readValue("{\"aggregations\":[\"Month\"]}",
				SecurityPolicy.class);
		assertThat("Exact aggregation set", policy.getAggregations(), contains(Aggregation.Month));
	}

	@Test
	public void parseJsonMinLocationPrecisionPolicy() throws Exception {
		SecurityPolicy policy = newObjectMapper().readValue("{\"minLocationPrecision\":\"TimeZone\"}",
				SecurityPolicy.class);
		assertThat("Minimum location precision set", policy.getLocationPrecisions(),
				containsInAnyOrder(LocationPrecision.TimeZone, LocationPrecision.Country));
	}

	@Test
	public void parseJsonNotAfterPolicy() throws Exception {
		ObjectMapper mapper = JsonUtils.newObjectMapper();
		mapper.disable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS);
		SecurityPolicy policy = mapper.readValue("{\"notAfter\":" + TEST_DATE.toEpochMilli() + "}",
				SecurityPolicy.class);
		assertThat("Not after set", policy.getNotAfter(), is(equalTo(TEST_DATE)));
	}

	@Test
	public void parseJsonRefreshAllowedPolicy() throws Exception {
		SecurityPolicy policy = newObjectMapper().readValue("{\"refreshAllowed\":true}",
				SecurityPolicy.class);
		assertThat("Refresh allowed set", policy.getRefreshAllowed(), equalTo(true));
	}

	@Test
	public void parseJsonLocationPrecisionPolicy() throws Exception {
		SecurityPolicy policy = newObjectMapper().readValue("{\"locationPrecisions\":[\"TimeZone\"]}",
				SecurityPolicy.class);
		assertThat("Exact location precision set", policy.getLocationPrecisions(),
				contains(LocationPrecision.TimeZone));
	}

}

/* ==================================================================
 * TestServiceReference.java - 7/06/2018 9:00:55 AM
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

package net.solarnetwork.common.osgi.test;

import java.io.Serializable;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

/**
 * {@link ServiceReference} to help with unit tests.
 *
 * @author matt
 * @version 1.2
 */
public class TestServiceReference<S> implements ServiceReference<S> {

	/**
	 * Convenient non-generic reference.
	 */
	public static class SerializableServiceRef extends TestServiceReference<Serializable> {

		public SerializableServiceRef(long id, int rank) {
			super(id, rank);
		}

	}

	private final Map<String, Object> props;

	/**
	 * Constructor.
	 *
	 * @param id
	 *        the service ID
	 * @param rank
	 *        the service rank
	 */
	public TestServiceReference(long id, int rank) {
		super();
		this.props = new HashMap<String, Object>(3);
		this.props.put(Constants.SERVICE_ID, id);
		this.props.put(Constants.SERVICE_RANKING, rank);
	}

	@Override
	public Object getProperty(String key) {
		return props.get(key);
	}

	@Override
	public String[] getPropertyKeys() {
		return props.keySet().toArray(new String[props.size()]);
	}

	@Override
	public Dictionary<String, Object> getProperties() {
		return new Hashtable<String, Object>(props);
	}

	@Override
	public Bundle getBundle() {
		return null;
	}

	@Override
	public Bundle[] getUsingBundles() {
		return null;
	}

	@Override
	public boolean isAssignableTo(Bundle bundle, String className) {
		return false;
	}

	@Override
	public int compareTo(Object reference) {
		// adapted from org.eclipse.osgi.internal.serviceregistry.ServiceReferenceImpl.compareTo(Object)
		ServiceReference<?> other = ((ServiceReference<?>) reference);

		Object thisProp = getProperty(Constants.SERVICE_RANKING);
		Object otherProp = other.getProperty(Constants.SERVICE_RANKING);

		final int thisRanking = (thisProp instanceof Number ? ((Number) thisProp).intValue() : 0);
		final int otherRanking = (otherProp instanceof Number ? ((Number) otherProp).intValue() : 0);
		if ( thisRanking != otherRanking ) {
			if ( thisRanking < otherRanking ) {
				return -1;
			}
			return 1;
		}

		thisProp = getProperty(Constants.SERVICE_ID);
		otherProp = other.getProperty(Constants.SERVICE_ID);

		final long thisId = (thisProp instanceof Number ? ((Number) thisProp).longValue() : 0L);
		final long otherId = (otherProp instanceof Number ? ((Number) otherProp).longValue() : 0L);
		if ( thisId == otherId ) {
			return 0;
		}
		if ( thisId < otherId ) {
			return 1;
		}
		return -1;
	}

	@Override
	public <A> A adapt(Class<A> type) {
		return null;
	}

}

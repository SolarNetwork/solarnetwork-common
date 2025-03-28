/* ==================================================================
 * ChainHttpRequestCustomizerService.java - 2/04/2023 11:42:19 am
 *
 * Copyright 2023 SolarNetwork.net Dev Team
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

package net.solarnetwork.web.service.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import net.solarnetwork.service.DatumFilterService;
import net.solarnetwork.settings.SettingSpecifier;
import net.solarnetwork.settings.support.BasicGroupSettingSpecifier;
import net.solarnetwork.settings.support.BasicTextFieldSettingSpecifier;
import net.solarnetwork.settings.support.SettingUtils;
import net.solarnetwork.util.ArrayUtils;
import net.solarnetwork.util.ByteList;
import net.solarnetwork.util.ObjectUtils;
import net.solarnetwork.util.WeakValueConcurrentHashMap;
import net.solarnetwork.web.service.HttpRequestCustomizerService;

/**
 * A configurable chain of HTTP request customizer services.
 *
 * <p>
 * This class is configured with a list of possible services, and then a list of
 * service UIDs that should be applied when
 * {@link #customize(HttpRequest, ByteList, Map)} is invoked. If a service for a
 * given UID is not available, no error will be generated and the next
 * configured service will be invoked. If any service returns {@literal null}
 * then iteration over the service UIDs will cease and {@literal null} will be
 * returned.
 * </p>
 *
 * @author matt
 * @version 1.1
 */
public class ChainHttpRequestCustomizerService extends AbstractHttpRequestCustomizerService {

	private static final Logger log = LoggerFactory.getLogger(ChainHttpRequestCustomizerService.class);

	private final ConcurrentMap<String, HttpRequestCustomizerService> serviceCache = new WeakValueConcurrentHashMap<>(
			16, 0.9f, 2);

	private final List<HttpRequestCustomizerService> services;
	private String[] serviceUids;

	/**
	 * Constructor.
	 *
	 * @param services
	 *        the list of possible services to choose from
	 * @throws IllegalArgumentException
	 *         if any argument is {@literal null}
	 */
	public ChainHttpRequestCustomizerService(List<HttpRequestCustomizerService> services) {
		super();
		this.services = ObjectUtils.requireNonNullArgument(services, "services");
	}

	@Override
	public void configurationChanged(Map<String, Object> properties) {
		// nothing
	}

	@Override
	public HttpRequest customize(HttpRequest request, ByteList body, Map<String, ?> parameters) {
		HttpRequest result = request;
		final String[] uids = getServiceUids();
		if ( uids != null ) {
			for ( String uid : uids ) {
				// do not allow recursing to self
				if ( uid.equals(getUid()) ) {
					continue;
				}
				HttpRequestCustomizerService s = findService(uid);
				if ( s != null ) {
					result = s.customize(result, body, parameters);
					if ( result == null ) {
						break;
					}
				}
			}
		}
		return result;
	}

	@Override
	public String getSettingUid() {
		return "net.s10k.http.customizer.chain";
	}

	@Override
	public List<SettingSpecifier> getSettingSpecifiers() {
		List<SettingSpecifier> result = new ArrayList<>(8);

		result.addAll(basicIdentifiableSettings(""));

		// list of UIDs
		String[] uids = getServiceUids();
		List<String> uidsList = (uids != null ? Arrays.asList(uids) : Collections.emptyList());
		BasicGroupSettingSpecifier uidsGroup = SettingUtils.dynamicListSettingSpecifier("serviceUids",
				uidsList, new SettingUtils.KeyedListCallback<String>() {

					@Override
					public Collection<SettingSpecifier> mapListSettingKey(String value, int index,
							String key) {
						return Collections.singletonList(new BasicTextFieldSettingSpecifier(key, ""));
					}
				});
		result.add(uidsGroup);

		return result;
	}

	private HttpRequestCustomizerService findService(final String uid) {
		return serviceCache.compute(uid, (k, v) -> {
			// have to re-check UID, as these can change
			try {
				if ( v != null && uid.equals(v.getUid()) ) {
					return v;
				}
				for ( HttpRequestCustomizerService s : services ) {
					String serviceUid = s.getUid();
					if ( uid.equals(serviceUid) ) {
						return s;
					}
				}
			} catch ( Exception e ) {
				log.warn("Discarding cached service [{}] because of exception: {}", uid, e.toString());
			}
			return null;
		});
	}

	/**
	 * Get the service UIDs to use.
	 *
	 * @return the service UIDs.
	 */
	public String[] getServiceUids() {
		return serviceUids;
	}

	/**
	 * Set the service UIDs to use.
	 *
	 * <p>
	 * This list defines the {@link DatumFilterService} instances to apply, from
	 * the list of available services.
	 * </p>
	 *
	 * @param serviceUids
	 *        the UIDs to set
	 */
	public void setServiceUids(String[] serviceUids) {
		this.serviceUids = serviceUids;
	}

	/**
	 * Get the service UIDs count.
	 *
	 * @return the number of UIDs to support
	 */
	public int getServiceUidsCount() {
		String[] uids = getServiceUids();
		return (uids != null ? uids.length : 0);
	}

	/**
	 * Set the service UIDs count.
	 *
	 * @param count
	 *        the number of UIDs to support
	 */
	public void setServiceUidsCount(int count) {
		this.serviceUids = ArrayUtils.arrayWithLength(this.serviceUids, count, String.class, null);
	}

}

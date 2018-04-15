/* ==================================================================
 * SecureEntryMaskingIdentifiableConfiguration.java - 15/04/2018 7:40:21 AM
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

package net.solarnetwork.settings.support;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.util.ClassUtils;
import net.solarnetwork.domain.IdentifiableConfiguration;
import net.solarnetwork.settings.SettingSpecifier;
import net.solarnetwork.settings.TextFieldSettingSpecifier;
import net.solarnetwork.util.StringUtils;

/**
 * Proxy {@link InvocationHandler} that can mask (hide)
 * {@link IdentifiableConfiguration} properties that contain sensitive
 * information based on a set of {@link SettingSpecifier} objects associated
 * with the configuration.
 * 
 * @author matt
 * @version 1.0
 */
public class SecureEntryMaskingIdentifiableConfiguration implements InvocationHandler {

	private static final String SERVICE_PROPERTIES_KEY_PREFIX = "serviceProperties.";
	private static final String SERVICE_PROPERTIES_GETTER_NAME = "getServiceProperties";

	private final IdentifiableConfiguration delegate;
	private final Set<String> secureSettings;
	private final Set<String> secureServiceProperties;

	/**
	 * Constructor.
	 * 
	 * @param delegate
	 *        the configuration to delegate to
	 * @param settings
	 *        the settings that define secure entry properties
	 */
	public SecureEntryMaskingIdentifiableConfiguration(IdentifiableConfiguration delegate,
			List<SettingSpecifier> settings) {
		super();
		this.delegate = delegate;
		if ( settings == null || settings.isEmpty() ) {
			secureSettings = Collections.emptySet();
			secureServiceProperties = Collections.emptySet();
		} else {
			Set<String> secure = null;
			Set<String> secureProps = null;
			for ( SettingSpecifier setting : settings ) {
				if ( setting instanceof TextFieldSettingSpecifier ) {
					TextFieldSettingSpecifier text = (TextFieldSettingSpecifier) setting;
					if ( text.isSecureTextEntry() ) {
						String key = text.getKey();
						if ( key != null && !key.isEmpty() ) {
							if ( key.startsWith(SERVICE_PROPERTIES_KEY_PREFIX) ) {
								String propKey = key.substring(SERVICE_PROPERTIES_KEY_PREFIX.length());
								if ( secureProps == null ) {
									secureProps = new HashSet<String>(4);
								}
								secureProps.add(propKey);
							} else {
								key = "get" + key.substring(0, 1).toUpperCase()
										+ (key.length() > 1 ? key.substring(1) : "");
								if ( secure == null ) {
									secure = new HashSet<String>(4);
								}
								secure.add(key);
							}

						}
					}
				}
			}
			secureSettings = (secure != null ? secure : Collections.<String> emptySet());
			secureServiceProperties = (secureProps != null ? secureProps
					: Collections.<String> emptySet());
		}
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		String methodName = method.getName();
		Method delegateMethod = delegate.getClass().getMethod(methodName, method.getParameterTypes());
		Object res = delegateMethod.invoke(delegate, args);
		if ( SERVICE_PROPERTIES_GETTER_NAME.equals(methodName) && !secureServiceProperties.isEmpty()
				&& res instanceof Map<?, ?> ) {
			Map<?, ?> map = (Map<?, ?>) res;
			for ( String propName : secureServiceProperties ) {
				if ( map.containsKey(propName) ) {
					Map<String, Object> maskedMap = new LinkedHashMap<String, Object>(map.size());
					for ( Map.Entry<?, ?> me : map.entrySet() ) {
						String key = me.getKey().toString();
						Object val = me.getValue();
						if ( val != null && secureServiceProperties.contains(key) ) {
							val = StringUtils.sha256Base64Value(val.toString());
						}
						maskedMap.put(key, val);
					}
					res = maskedMap;
					break;
				}
			}
		} else if ( res != null && secureSettings.contains(methodName) ) {
			res = StringUtils.sha256Base64Value(res.toString());
		}
		return res;
	}

	/**
	 * Create a new proxy instance that masks the secure entry settings of an
	 * existing configuration object.
	 * 
	 * @param configuration
	 *        the configuration to mask the secure settings on
	 * @param settings
	 *        the settings associated with the configuration, that define which
	 *        ones require masking
	 * @return the proxy, which will implement all interfaces defined on
	 *         {@code configuration}
	 */
	public static IdentifiableConfiguration createProxy(IdentifiableConfiguration configuration,
			List<SettingSpecifier> settings) {
		Class<?>[] interfaces = ClassUtils.getAllInterfaces(configuration);
		Object proxy = Proxy.newProxyInstance(configuration.getClass().getClassLoader(), interfaces,
				new SecureEntryMaskingIdentifiableConfiguration(configuration, settings));
		return (IdentifiableConfiguration) proxy;
	}

}

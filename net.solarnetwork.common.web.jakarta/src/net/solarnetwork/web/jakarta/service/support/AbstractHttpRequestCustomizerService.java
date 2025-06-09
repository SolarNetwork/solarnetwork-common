/* ==================================================================
 * AbstractHttpRequestCustomizerService.java - 2/04/2023 11:40:29 am
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

package net.solarnetwork.web.jakarta.service.support;

import net.solarnetwork.service.ServiceLifecycleObserver;
import net.solarnetwork.service.support.BasicIdentifiable;
import net.solarnetwork.settings.SettingSpecifierProvider;
import net.solarnetwork.settings.SettingsChangeObserver;
import net.solarnetwork.web.jakarta.service.HttpRequestCustomizerService;

/**
 * Base class for HTTP request customizer implementations.
 * 
 * @author matt
 * @version 1.0
 */
public abstract class AbstractHttpRequestCustomizerService extends BasicIdentifiable
		implements HttpRequestCustomizerService, SettingSpecifierProvider, SettingsChangeObserver,
		ServiceLifecycleObserver {

	/**
	 * Constructor.
	 */
	public AbstractHttpRequestCustomizerService() {
		super();
	}

	@Override
	public void serviceDidStartup() {
		configurationChanged(null);
	}

	@Override
	public void serviceDidShutdown() {
		// nothing
	}

}

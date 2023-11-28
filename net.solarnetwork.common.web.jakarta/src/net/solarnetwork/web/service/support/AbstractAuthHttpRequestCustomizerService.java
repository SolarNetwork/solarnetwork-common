/* ==================================================================
 * AbstractAuthHttpRequestCustomizerService.java - 2/04/2023 7:11:56 am
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

import net.solarnetwork.web.service.HttpRequestCustomizerService;

/**
 * Base class for authorization HTTP request customizer implementations.
 * 
 * @author matt
 * @version 1.0
 */
public abstract class AbstractAuthHttpRequestCustomizerService
		extends AbstractHttpRequestCustomizerService {

	/**
	 * Constructor.
	 * 
	 * <p>
	 * The {@code groupUid} property will be set to
	 * {@link HttpRequestCustomizerService#AUTHORIZATION_GROUP_UID}.
	 * </p>
	 */
	public AbstractAuthHttpRequestCustomizerService() {
		super();
		setGroupUid(AUTHORIZATION_GROUP_UID);
	}

}

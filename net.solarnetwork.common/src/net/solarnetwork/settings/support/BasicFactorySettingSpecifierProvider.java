/* ==================================================================
 * BasicFactorySettingSpecifierProvider.java - Mar 23, 2012 3:08:18 PM
 * 
 * Copyright 2007-2012 SolarNetwork.net Dev Team
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

import java.util.List;
import org.springframework.context.MessageSource;
import net.solarnetwork.settings.FactorySettingSpecifierProvider;
import net.solarnetwork.settings.SettingSpecifier;
import net.solarnetwork.settings.SettingSpecifierProvider;

/**
 * Basic implementation of {@link FactorySettingSpecifierProvider} that
 * delegates all {@link SettingSpecifierProvider} methods to a delegate.
 * 
 * @author matt
 * @version 1.0
 */
public class BasicFactorySettingSpecifierProvider implements FactorySettingSpecifierProvider {

	private final String factoryInstanceUID;
	private final SettingSpecifierProvider delegate;

	public BasicFactorySettingSpecifierProvider(String factoryInstanceUID,
			SettingSpecifierProvider delegate) {
		super();
		this.factoryInstanceUID = factoryInstanceUID;
		this.delegate = delegate;
	}

	@Override
	public String getSettingUID() {
		return delegate.getSettingUID();
	}

	@Override
	public String getDisplayName() {
		return delegate.getDisplayName();
	}

	@Override
	public MessageSource getMessageSource() {
		return delegate.getMessageSource();
	}

	@Override
	public List<SettingSpecifier> getSettingSpecifiers() {
		return delegate.getSettingSpecifiers();
	}

	@Override
	public String getFactoryInstanceUID() {
		return factoryInstanceUID;
	}

}

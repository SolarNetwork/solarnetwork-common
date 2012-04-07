/* ==================================================================
 * Activator.java - Feb 4, 2010 2:31:27 PM
 * 
 * Copyright 2007-2010 SolarNetwork.net Dev Team
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
 * $Id$
 * ==================================================================
 */

package net.solarnetwork.pidfile;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * {@link BundleActivator} to create a {@link PidFileCreator} instance.
 * 
 * @author matt
 * @version $Id$
 */
public class Activator implements BundleActivator {
	
	private PidFileCreator pidFile = null;

	@Override
	public void start(BundleContext context) throws Exception {
		if ( pidFile == null ) {
			pidFile = new PidFileCreator();
			pidFile.createPidFile();
		}
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		// nothing to do
	}

}

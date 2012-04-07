/* ===================================================================
 * ViewResponseAugmentor.java
 * 
 * Created Dec 11, 2009 7:40:02 PM
 * 
 * Copyright 2007-2009 SolarNetwork.net Dev Team
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
 * ===================================================================
 * $Id$
 * ===================================================================
 */

package net.solarnetwork.web.support;

import java.io.IOException;
import java.io.Writer;

/**
 * API for augmenting a view response.
 * 
 * @author matt
 * @version $Revision$ $Date$
 */
public interface ViewResponseAugmentor {

	/**
	 * Augment the response in some way.
	 * 
	 * @param out an output writer
	 * @throws IOException if any IO error occurs
	 */
	void augmentResponse(Writer out) throws IOException;
	
}

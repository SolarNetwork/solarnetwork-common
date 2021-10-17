/* ==================================================================
 * CloseableIterator.java - 11/10/2021 9:20:19 AM
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

package net.solarnetwork.util;

import java.io.Closeable;
import java.util.Iterator;

/**
 * API for an iterator that is also {@link Closeable}.
 * 
 * <p>
 * The expected use pattern of this class is something along these lines:
 * </p>
 * 
 * <pre>
 * <code>
 * try ( CloseableIterator&lt;Object&gt; itr = thing.iterator() ) {
 *   // do something with Iterator
 * }
 * </code>
 * </pre>
 * 
 * @author matt
 * @version 1.0
 */
public interface CloseableIterator<E> extends Iterator<E>, Closeable {

}

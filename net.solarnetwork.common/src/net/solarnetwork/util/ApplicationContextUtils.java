/* ==================================================================
 * ApplicationContextUtils.java - 4/10/2021 3:24:32 PM
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

import static java.util.function.Function.identity;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;

/**
 * Utility methods for dealing with Spring {@link ApplicationContext} instances.
 * 
 * @author matt
 * @version 1.0
 */
public final class ApplicationContextUtils {

	private ApplicationContextUtils() {
		// not available
	}

	/**
	 * Generate a {@literal TRACE} level log entry of the complete list of bean
	 * names available in the given context.
	 * 
	 * @param ctx
	 *        the context to log the bean names for
	 * @param log
	 *        the log to log to, will log at the {@literal TRACE} level
	 */
	public static void traceBeanNames(ApplicationContext ctx, Logger log) {
		if ( log.isTraceEnabled() ) {
			String[] beanNames = ctx.getBeanDefinitionNames();
			String names = Arrays.stream(beanNames)
					.sorted(Comparator.comparing(identity(), String.CASE_INSENSITIVE_ORDER))
					.collect(Collectors.joining(",\n\t"));
			log.trace("Beans available in {}: [\n{\n}", ctx, names);
		}

	}

}

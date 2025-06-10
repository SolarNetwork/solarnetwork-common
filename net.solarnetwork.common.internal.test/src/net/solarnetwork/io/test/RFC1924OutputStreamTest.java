/* ==================================================================
 * RFC1924OutputStreamTest.java - May 15, 2013 6:58:25 AM
 * 
 * Copyright 2007-2013 SolarNetwork.net Dev Team
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

package net.solarnetwork.io.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;
import net.solarnetwork.io.RFC1924InputStream;
import net.solarnetwork.io.RFC1924OutputStream;

/**
 * Test cases for the {@link RFC1924OutputStream} class.
 * 
 * @author matt
 * @version 1.0
 */
public class RFC1924OutputStreamTest {

	private static final String TEST_DATA = "Man is distinguished, not only by his reason, but by this singular passion from other animals, which is a lust of the mind, that by a perseverance of delight in the continued and indefatigable generation of knowledge, exceeds the short vehemence of any carnal pleasure.";
	private static final String TEST_DATA_ENC = "O<`^zX>%ZCX>)XGZfA9Ab7*B`EFf-gbRchTY<VDJc_3(Mb0BhMVRLV8EFfZabRc4RAarPHb0BkRZfA9DVR9gFVRLh7Z*CxFa&K)QZ**v7av))DX>DO_b1WctXlY|;AZc?TVIXXEb95kYW*~HEWgu;7Ze%PVbZB98AYyqSVIXj2a&u*NWpZI|V`U(3W*}r`Y-wj`bRcPNAarPDAY*TCbZKsNWn>^>Ze$>7Ze(R<VRUI{VPb4$AZKN6WpZJ3X>V>IZ)PBCZf|#NWn^b%EFfigV`XJzb0BnRWgv5CZ*p`Xc4cT~ZDnp_Wgu^6AYpEKAY);2ZeeU7aBO8^b9HiM0k";
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Test
	public void encodeStream1() throws IOException {
		ByteArrayOutputStream byos = new ByteArrayOutputStream();
		RFC1924OutputStream out = new RFC1924OutputStream(byos);
		FileCopyUtils.copy(new ByteArrayInputStream(TEST_DATA.getBytes("US-ASCII")), out);
		String result = byos.toString("US-ASCII");
		Assert.assertEquals(TEST_DATA_ENC, result);
		log.debug("Encoded RFC1924: {}", byos.toString("US-ASCII"));

		// test reverse
		result = new String(FileCopyUtils.copyToByteArray(new RFC1924InputStream(
				new ByteArrayInputStream(byos.toByteArray()))), "US-ASCII");
		log.debug("Decoded RFC1924: {}", result);
		Assert.assertEquals(TEST_DATA, result);
	}
}

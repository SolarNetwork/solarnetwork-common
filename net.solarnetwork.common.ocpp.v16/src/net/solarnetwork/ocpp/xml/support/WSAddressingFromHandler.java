/* ==================================================================
 * WSAddressingFromHandler.java - 13/06/2015 12:57:24 pm
 * 
 * Copyright 2007-2015 SolarNetwork.net Dev Team
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

package net.solarnetwork.ocpp.xml.support;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link SOAPHandler} for inserting {@code wsa:From} headers into outbound
 * messages.
 * 
 * @author matt
 * @version 1.0
 */
public class WSAddressingFromHandler implements SOAPHandler<SOAPMessageContext> {

	/** The {@code wsa} namespace. */
	public static final String WSA_NS = "http://www.w3.org/2005/08/addressing";

	/** The {@code wsa:From} element name. */
	public static final QName WSA_FROM = new QName(WSA_NS, "From");

	/** The {@code wsa:Address} element name. */
	public static final QName WSA_ADDRESS = new QName(WSA_NS, "Address");

	private String dynamicFromPath = "/ocpp/v15";
	private String networkInterfaceName;
	private String fromURL;
	private boolean preferIPv4Address = Boolean
			.valueOf(System.getProperty("java.net.preferIPv4Stack", "true"));

	private final Logger log = LoggerFactory.getLogger(getClass());

	// <wsa:From><wsa:Address>fromURL</wsa:Address></wsa:From>

	/**
	 * Constructor.
	 */
	public WSAddressingFromHandler() {
		super();
	}

	@Override
	public boolean handleMessage(SOAPMessageContext context) {
		Boolean outboundProperty = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		if ( outboundProperty != null && outboundProperty.booleanValue() ) {
			final String fromAddressValue = resolveFromURL();
			if ( fromAddressValue != null ) {
				try {
					SOAPHeader header = context.getMessage().getSOAPHeader();
					SOAPElement from = null;
					Node fromAddr = null;
					for ( @SuppressWarnings("unchecked")
					Iterator<Node> itr = header.getChildElements(WSA_FROM); itr.hasNext(); ) {
						Node n = itr.next();
						if ( n instanceof SOAPElement ) {
							from = (SOAPElement) n;
							break;
						}
					}
					if ( from == null ) {
						from = header.addHeaderElement(WSA_FROM);
					}
					for ( @SuppressWarnings("unchecked")
					Iterator<Node> itr = from.getChildElements(WSA_ADDRESS); itr.hasNext(); ) {
						fromAddr = itr.next();
						break;
					}
					if ( fromAddr == null ) {
						fromAddr = from.addChildElement(WSA_ADDRESS);
					}
					fromAddr.setTextContent(fromAddressValue);
				} catch ( SOAPException e ) {
					log.error("Exception generating WS-Addressing From header to {}", fromAddressValue,
							e);
				}
			}
		}
		return true;
	}

	private String resolveFromURL() {
		String fromPath = dynamicFromPath;
		if ( fromPath != null ) {
			InetAddress addr = getHostInetAddress();
			if ( addr != null ) {
				return "http://" + addr.getHostAddress() + fromPath;
			}
			return null;
		}
		return fromURL;
	}

	private InetAddress getHostInetAddress() {
		String nicName = networkInterfaceName;
		try {
			Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
			while ( nics.hasMoreElements() ) {
				NetworkInterface nic = nics.nextElement();
				if ( nicName != null && !nicName.equalsIgnoreCase(nic.getName()) ) {
					continue;
				}
				Enumeration<InetAddress> addrs = nic.getInetAddresses();
				while ( addrs.hasMoreElements() ) {
					InetAddress inet = addrs.nextElement();
					if ( preferIPv4Address && (inet instanceof Inet6Address) ) {
						continue;
					}
					if ( !(inet.isAnyLocalAddress() || inet.isLinkLocalAddress()
							|| inet.isLoopbackAddress() || inet.isMulticastAddress()) ) {
						return inet;
					}
				}
			}
		} catch ( SocketException e ) {
			// ignore
		}
		return null;
	}

	@Override
	public boolean handleFault(SOAPMessageContext context) {
		return true;
	}

	@Override
	public void close(MessageContext context) {
	}

	@Override
	public Set<QName> getHeaders() {
		return Collections.singleton(new QName(WSA_NS, "From"));
	}

	/**
	 * Get the URL to use for the {@code From} address.
	 * 
	 * @return the URL
	 */
	public String getFromURL() {
		return fromURL;
	}

	/**
	 * Set a specific URL to use for the {@code From} address. If
	 * {@code dynamicFromPath} is configured this property is ignored.
	 * 
	 * @param url
	 *        The specific URL to use.
	 */
	public void setFromURL(String url) {
		this.fromURL = url;
	}

	/**
	 * Get the URL path to use for the {@code From} address.
	 * 
	 * @return the URL
	 */
	public String getDynamicFromPath() {
		return dynamicFromPath;
	}

	/**
	 * Set a URL path to use for the {@code From} address. The host is resolved
	 * dynamically at runtime based on the host's IP address. If configured the
	 * {@code fromURL} property is ignored.
	 * 
	 * @param dynamicFromPath
	 *        A URL path to use with a dynamically resolved host.
	 */
	public void setDynamicFromPath(String dynamicFromPath) {
		this.dynamicFromPath = dynamicFromPath;
	}

	/**
	 * Get the network interface name to use if {@code dynamicFromPath} is
	 * configured.
	 * 
	 * @return the network interface name to limit resolving the host IP address
	 *         to
	 */
	public String getNetworkInterfaceName() {
		return networkInterfaceName;
	}

	/**
	 * If {@code dynamicFromPath} is configured, this property can be configured
	 * to restrict which network interface to look at for the resolved IP
	 * address. If not configured then all network interfaces will be inspected,
	 * and the first available non-local IP address will be used at runtime.
	 * 
	 * @param networkInterfaceName
	 *        the network interface name to limit resolving the host IP address
	 *        to
	 */
	public void setNetworkInterfaceName(String networkInterfaceName) {
		this.networkInterfaceName = networkInterfaceName;
	}

	/**
	 * Get the preference to resolve IPv4 address values when
	 * {@code dynamicFromPath} is configured.
	 * 
	 * @return {@literal true} to prefer IPv4 addresses
	 */
	public boolean isPreferIPv4Address() {
		return preferIPv4Address;
	}

	/**
	 * Set the preference to resolve IPv4 address values when
	 * {@code dynamicFromPath} is configured.
	 * 
	 * @param preferIPv4Address
	 *        {@literal true} to prefer IPv4 addresses
	 */
	public void setPreferIPv4Address(boolean preferIPv4Address) {
		this.preferIPv4Address = preferIPv4Address;
	}

}

/* ==================================================================
 * HMACHandler.java - 16/06/2015 9:33:08 am
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

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Pattern;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.namespace.QName;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.Text;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link SOAPHandler} to generate a HMAC-SHA256 signature of a SOAP message.
 * 
 * <p>
 * This handler is designed to be integrated into both the client and server
 * side of an OCPP system. Both sides must be configured to use the same
 * {@code secret} value, which is the key used to generate a HMAC-SHA256 hash.
 * The Base64-encoded hash is added as the content of a new SOAP header element,
 * {@code Authentication}, and the system's reported current time is added as a
 * {@code ts} attribute to that element. For example:
 * </p>
 * 
 * <pre>
 * &lt;Authentication xmlns="urn://SolarNetwork/SolarNode/WS" 
 *    ts="2015-01-01T12:00:00.000Z"&gt;doEIdjlsdkfjsopdifjso==&lt;/Authentication&gt;
 * </pre>
 * 
 * <p>
 * The encrypted HMAC message content is derived from the SOAP message itself,
 * and includes the following content, all delimited by a newline character (
 * {@code \n}):
 * </p>
 * 
 * <ol>
 * <li>The OCPP {@code chargePointIdentity} SOAP header value, or an empty
 * string if not available.</li>
 * <li>The current date, in ISO 8601 format in the UTC time zone.</li>
 * <li>Top-level SOAP header elements, in DOM order. In addition, for any
 * top-level element in the {@code http://www.w3.org/2005/08/addressing}
 * namespace (WS-Addressing) then the first of any child {@code Address} element
 * is included. This is to ensure all WS-Addressing values are included in the
 * digest.</li>
 * <li>Recursive SOAP body elements, in DOM order, including the SOAP body
 * element itself.</li>
 * </ol>
 * 
 * <p>
 * For any SOAP element to be included in the digest, the syntax of the value to
 * add is <code>{uri}localName=value</code> where <code>uri</code> is the URI of
 * the namespace of the element, <code>localName</code> is the element name, and
 * <code>value</code> is the normalized text value of the element (normalized by
 * calling {@link org.w3c.dom.Node#normalize()}). If the text value is only
 * whitespace, however, the <code>=value</code> part is omitted.
 * </p>
 * 
 * <p>
 * For example, a SOAP message like this:
 * </p>
 * 
 * <pre>
 * &lt;S:Envelope xmlns:S="http://www.w3.org/2003/05/soap-envelope"&gt;
	&lt;S:Header&gt;
		&lt;chargeBoxIdentity xmlns="urn://Ocpp/Cs/2012/06/"&gt;UID=1013,O=SolarDev&lt;/chargeBoxIdentity&gt;
		&lt;To xmlns="http://www.w3.org/2005/08/addressing"
			&gt;http://localhost:9000/steve/services/CentralSystemService&lt;/To&gt;
		&lt;Action xmlns="http://www.w3.org/2005/08/addressing"&gt;/BootNotification&lt;/Action&gt;
		&lt;ReplyTo xmlns="http://www.w3.org/2005/08/addressing"&gt;
			&lt;Address&gt;http://www.w3.org/2005/08/addressing/anonymous&lt;/Address&gt;
		&lt;/ReplyTo&gt;
		&lt;MessageID xmlns="http://www.w3.org/2005/08/addressing"
			&gt;uuid:f86a3b23-5db3-4260-ab21-d72348da5ecc&lt;/MessageID&gt;
		&lt;From xmlns="http://www.w3.org/2005/08/addressing"&gt;
			&lt;Address&gt;http://192.168.1.44:8680/ocpp/v15&lt;/Address&gt;
		&lt;/From&gt;
	&lt;/S:Header&gt;
	&lt;S:Body&gt;
		&lt;bootNotificationRequest xmlns="urn://Ocpp/Cs/2012/06/"&gt;
			&lt;chargePointVendor&gt;SolarNetwork&lt;/chargePointVendor&gt;
			&lt;chargePointModel&gt;SolarNode&lt;/chargePointModel&gt;
			&lt;chargePointSerialNumber&gt;155&lt;/chargePointSerialNumber&gt;
			&lt;firmwareVersion&gt;0.1.0&lt;/firmwareVersion&gt;
		&lt;/bootNotificationRequest&gt;
	&lt;/S:Body&gt;
&lt;/S:Envelope&gt;
 * </pre>
 * 
 * <p>
 * would result in a canonical digest value like this:
 * </p>
 * 
 * <pre>
 * UID=1013,O=SolarDev
2015-06-16T06:31:13.492Z
{urn://Ocpp/Cs/2012/06/}chargeBoxIdentity=UID=1013,O=SolarDev
{http://www.w3.org/2005/08/addressing}To=http://localhost:9000/steve/services/CentralSystemService
{http://www.w3.org/2005/08/addressing}Action=/BootNotification
{http://www.w3.org/2005/08/addressing}ReplyTo=http://www.w3.org/2005/08/addressing/anonymous
{http://www.w3.org/2005/08/addressing}MessageID=uuid:f86a3b23-5db3-4260-ab21-d72348da5ecc
{http://www.w3.org/2005/08/addressing}From=http://192.168.1.44:8680/ocpp/v15
{http://www.w3.org/2003/05/soap-envelope}Body
{urn://Ocpp/Cs/2012/06/}bootNotificationRequest
{urn://Ocpp/Cs/2012/06/}chargePointVendor=SolarNetwork
{urn://Ocpp/Cs/2012/06/}chargePointModel=SolarNode
{urn://Ocpp/Cs/2012/06/}chargePointSerialNumber=155
{urn://Ocpp/Cs/2012/06/}firmwareVersion=0.1.0
 * </pre>
 * 
 * <p>
 * The {@link #getMaximumTimeSkew()} value represents the maximum amount of time
 * difference allowed between the system's reported current time and the
 * 
 * @author matt
 * @version 1.2
 */
public class HMACHandler implements SOAPHandler<SOAPMessageContext> {

	/** The {@code sn} namespace. */
	public static final String SN_WS_NS = "urn://SolarNetwork/SolarNode/WS";

	/** The {@code sn:Authentication} element name. */
	public static final QName SN_WS_AUTH = new QName(SN_WS_NS, "Authentication");

	/** The SolarNode webservice timestamp element local name. */
	public static final String SN_WS_TIMESTAMP = "ts";

	/** The OCPP central service charge box identity element name. */
	public static final QName OCPP_CS_CHARGE_BOX_IDENTITY = new QName("urn://Ocpp/Cs/2012/06/",
			"chargeBoxIdentity");

	/** The OCPP charge point charge box identity element name. */
	public static final QName OCPP_CP_CHARGE_BOX_IDENTITY = new QName("urn://Ocpp/Cp/2012/06/",
			"chargeBoxIdentity");

	/** The default secret value. */
	public static final String DEFAULT_SECRET = "changeit";

	private static final Pattern NON_WHITESPACE = Pattern.compile("\\S");

	private String secret = DEFAULT_SECRET;
	private boolean required = true;
	private Mac hmac;
	private long maximumTimeSkew = 5 * 60 * 1000L; // 5 minutes

	private final Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * Constructor.
	 */
	public HMACHandler() {
		super();
	}

	@Override
	public boolean handleMessage(SOAPMessageContext context) {
		Boolean outboundProperty = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		if ( outboundProperty != null && outboundProperty.booleanValue() ) {
			try {
				addAuthenticationHeader(context);
			} catch ( SOAPException e ) {
				log.error("Error adding Authentication SOAP header", e);
			}
		} else {
			try {
				validateAuthenticationHeader(context);
			} catch ( SOAPException e ) {
				log.error("Error validating Authentication SOAP header", e);
			}
		}
		return true;
	}

	private Mac getHMAC() {
		Mac m = hmac;
		if ( m == null ) {
			try {
				m = Mac.getInstance("HmacSHA256");
				SecretKeySpec key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
				m.init(key);
				hmac = m;
			} catch ( NoSuchAlgorithmException e ) {
				throw new RuntimeException(e);
			} catch ( InvalidKeyException e ) {
				throw new RuntimeException(e);
			}
		}
		return m;
	}

	private DateFormat getTimestampDateFormat() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		return sdf;
	}

	private String getTimestampString(long time) {
		DateFormat sdf = getTimestampDateFormat();
		return sdf.format(new Date(time));
	}

	private void addAuthenticationHeader(SOAPMessageContext context) throws SOAPException {
		addAuthenticationHeader(context, System.currentTimeMillis());
	}

	private void addAuthenticationHeader(SOAPMessageContext context, final long date)
			throws SOAPException {
		SOAPMessage msg = context.getMessage();
		SOAPHeader head = msg.getSOAPHeader();

		SOAPElement auth = getAuthenticationHeader(head);
		String hashData = calculateHashData(context, date);
		String hash = hash(hashData);
		if ( auth == null ) {
			auth = head.addHeaderElement(SN_WS_AUTH);
		}
		auth.setTextContent(hash);
		if ( !auth.hasAttribute(SN_WS_TIMESTAMP) ) {
			auth.setAttribute(SN_WS_TIMESTAMP, getTimestampString(date));
		}
	}

	private String calculateHashData(final SOAPMessageContext context, final long date)
			throws SOAPException {
		SOAPMessage msg = context.getMessage();
		SOAPHeader head = msg.getSOAPHeader();
		SOAPBody body = msg.getSOAPBody();

		// our hash will be constructed out of the date, the headers, and the body
		final StringBuilder buf = new StringBuilder("\n"); // leading newline to handle chargeBoxIdentity value later

		final String ts = getTimestampString(date);
		buf.append(ts).append('\n');

		String cbIdent = null;

		for ( @SuppressWarnings("unchecked")
		Iterator<SOAPHeaderElement> itr = head.examineAllHeaderElements(); itr.hasNext(); ) {
			SOAPHeaderElement header = itr.next();
			QName headerName = header.getElementQName();
			String hashKey = headerName.toString(); // e.g. {nsURL}localName
			String hashValue = "";
			if ( WSAddressingFromHandler.WSA_NS.equals(headerName.getNamespaceURI()) ) {
				// if this element has a child Address element, use that content, otherwise this node's content
				Node addr = null;
				for ( @SuppressWarnings("unchecked")
				Iterator<Node> children = header
						.getChildElements(WSAddressingFromHandler.WSA_ADDRESS); children.hasNext(); ) {
					addr = children.next();
					break;
				}
				if ( addr != null ) {
					addr.normalize();
					hashValue = addr.getTextContent();
				} else {
					header.normalize();
					hashValue = header.getTextContent();
				}
			} else if ( SN_WS_AUTH.equals(headerName) ) {
				continue;
			} else {
				header.normalize();
				hashValue = header.getTextContent();
			}
			if ( cbIdent == null && (OCPP_CS_CHARGE_BOX_IDENTITY.equals(headerName)
					|| OCPP_CP_CHARGE_BOX_IDENTITY.equals(headerName)) ) {
				cbIdent = hashValue;
			}
			buf.append(hashKey).append('=').append(hashValue).append('\n');
		}
		appendHashData(body, buf);
		if ( cbIdent != null ) {
			buf.insert(0, cbIdent);
		}
		return buf.toString();
	}

	private SOAPElement getAuthenticationHeader(SOAPHeader head) throws SOAPException {
		SOAPElement auth = null;
		for ( Iterator<?> itr = head.getChildElements(SN_WS_AUTH); itr.hasNext(); ) {
			auth = (SOAPElement) itr.next();
			break;
		}
		return auth;
	}

	private void validateAuthenticationHeader(SOAPMessageContext context) throws SOAPException {
		SOAPMessage msg = context.getMessage();
		SOAPHeader head = msg.getSOAPHeader();

		SOAPElement auth = getAuthenticationHeader(head);
		if ( auth == null ) {
			// TODO: configurable property to 1) ignore or 2) throw exception. Now just ignore.
			return;
		}
		Date timestamp = null;
		try {
			timestamp = (Date) getTimestampDateFormat().parseObject(auth.getAttribute(SN_WS_TIMESTAMP));
		} catch ( ParseException e ) {
			throw new RuntimeException("Invalid date: " + e.getMessage());
		}
		final long skew = Math.abs(timestamp.getTime() - System.currentTimeMillis());
		if ( skew > maximumTimeSkew ) {
			throw new RuntimeException("Time skew too big: " + skew);
		}
		final String calculatedHashData = calculateHashData(context, timestamp.getTime());
		final String calculatedHash = hash(calculatedHashData);
		final String presentedHash = auth.getTextContent();
		if ( !calculatedHash.equals(presentedHash) ) {
			throw new RuntimeException("Invalid Authentication value");
		}
	}

	private String hash(String data) {
		log.debug("HMAC hash input:\n{}", data);
		Mac mac = getHMAC();
		byte[] hash;
		synchronized ( mac ) {
			mac.reset();
			hash = mac.doFinal(data.getBytes());
		}
		return Base64.getEncoder().encodeToString(hash);
	}

	private void appendHashData(SOAPElement root, StringBuilder buf) {
		QName name = root.getElementQName();
		boolean first = true;
		for ( Iterator<?> itr = root.getChildElements(); itr.hasNext(); ) {
			Object o = itr.next();
			if ( o instanceof SOAPElement ) {
				if ( first ) {
					buf.append(name.toString()).append('\n');
					first = false;
				}
				appendHashData((SOAPElement) o, buf);
			} else if ( o instanceof Text ) {
				Text t = (Text) o;
				if ( t.isComment() ) {
					continue;
				}
				t.normalize();

				// only append non-whitespace text
				if ( NON_WHITESPACE.matcher(t.getTextContent()).find() ) {
					buf.append(name.toString()).append('=').append(t.getTextContent()).append('\n');
				}
			}
		}
	}

	@Override
	public boolean handleFault(SOAPMessageContext context) {
		return true;
	}

	@Override
	public void close(MessageContext context) {
		// nadda
	}

	@Override
	public Set<QName> getHeaders() {
		return Collections.singleton(SN_WS_AUTH);
	}

	/**
	 * Set the shared secret value.
	 * 
	 * This value must be shared with the OCPP central system.
	 * 
	 * @param secret
	 *        The secret value to use. If <em>null</em>, an empty string will be
	 *        used.
	 */
	public void setSecret(String secret) {
		if ( secret == null ) {
			secret = "";
		}
		this.secret = secret;
		hmac = null;
	}

	/**
	 * Set the maximum allowed time skew, in milliseconds.
	 * 
	 * The {@code ts} attribute of incoming messages will be compared to the
	 * current system time, and if it differs by more than this amount the
	 * message will be rejected. In order for this check to be effective, the
	 * system's clock must be kept accurate, for example by using a service like
	 * NTP or GPS to synchronize the system's clock.
	 * 
	 * @param maximumTimeSkew
	 *        The maximum time skew allowed.
	 */
	public void setMaximumTimeSkew(long maximumTimeSkew) {
		this.maximumTimeSkew = maximumTimeSkew;
	}

	/**
	 * Get the maximum allowed time skew.
	 * 
	 * @return The configured maximum time skew, in milliseconds.
	 */
	public long getMaximumTimeSkew() {
		return maximumTimeSkew;
	}

	/**
	 * Get the required flag.
	 * 
	 * @return The configured required flag value. Defaults to <em>true</em>.
	 */
	public boolean isRequired() {
		return required;
	}

	/**
	 * Set the required flag.
	 * 
	 * If <em>true</em> then an authentication header is required to be present
	 * (and valid) or else an exception will be throw. If <em>false</em> then a
	 * missing authentication header will not cause any exception to be thrown,
	 * but if provided will still be validated and if not valid an exception
	 * will still be thrown.
	 * 
	 * @param required
	 *        The required flag value to set.
	 */
	public void setRequired(boolean required) {
		this.required = required;
	}

}

/* ==================================================================
 * TestingInterceptHandler.java - 8/06/2018 4:29:18 PM
 * 
 * Copyright 2018 SolarNetwork.net Dev Team
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

package net.solarnetwork.test.mqtt;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.InterceptHandler;
import io.moquette.interception.messages.InterceptAcknowledgedMessage;
import io.moquette.interception.messages.InterceptConnectMessage;
import io.moquette.interception.messages.InterceptConnectionLostMessage;
import io.moquette.interception.messages.InterceptDisconnectMessage;
import io.moquette.interception.messages.InterceptMessage;
import io.moquette.interception.messages.InterceptPublishMessage;
import io.moquette.interception.messages.InterceptSubscribeMessage;
import io.moquette.interception.messages.InterceptUnsubscribeMessage;

/**
 * An {@link InterceptHandler} for unit testing.
 * 
 * @author matt
 * @version 1.0
 */
public class TestingInterceptHandler extends AbstractInterceptHandler {

	private final String id = UUID.randomUUID().toString();

	public final List<InterceptConnectMessage> connectMessages = new CopyOnWriteArrayList<>();
	public final List<InterceptDisconnectMessage> disconnectMessages = new CopyOnWriteArrayList<>();
	public final List<InterceptConnectionLostMessage> connectionLostMessages = new CopyOnWriteArrayList<>();
	public final List<InterceptPublishMessage> publishMessages = new CopyOnWriteArrayList<>();
	public final List<ByteBuffer> publishPayloads = new CopyOnWriteArrayList<>();
	public final List<InterceptSubscribeMessage> subscribeMessages = new CopyOnWriteArrayList<>();
	public final List<InterceptUnsubscribeMessage> unsubscribeMessages = new CopyOnWriteArrayList<>();
	public final List<InterceptAcknowledgedMessage> acknowledgedMessages = new CopyOnWriteArrayList<>();

	public static interface Callback {

		void handleInterceptMessage(InterceptMessage msg);

	}

	private Callback callback;

	@Override
	public final String getID() {
		return id;
	}

	@Override
	public synchronized void onConnect(InterceptConnectMessage msg) {
		connectMessages.add(msg);
		notifiyCallback(msg);
	}

	@Override
	public synchronized void onDisconnect(InterceptDisconnectMessage msg) {
		disconnectMessages.add(msg);
		notifiyCallback(msg);
	}

	@Override
	public synchronized void onConnectionLost(InterceptConnectionLostMessage msg) {
		connectionLostMessages.add(msg);
		notifiyCallback(msg);
	}

	@Override
	public synchronized void onPublish(InterceptPublishMessage msg) {
		byte[] copy = new byte[msg.getPayload().readableBytes()];
		msg.getPayload().duplicate().readBytes(copy);
		publishPayloads.add(ByteBuffer.wrap(copy));
		publishMessages.add(msg);
		notifiyCallback(msg);
	}

	/**
	 * Get a published message.
	 * 
	 * @param index
	 *        the index of the publish message to get
	 * @return the message
	 */
	public synchronized InterceptPublishMessage getPublishMessageAtIndex(int index) {
		return publishMessages.get(index);
	}

	/**
	 * Get a published payload.
	 * 
	 * @param index
	 *        the index of the publish payload to get
	 * @return the payload
	 */
	public synchronized byte[] getPublishPayloadAtIndex(int index) {
		return publishPayloads.get(index).array();
	}

	/**
	 * Get a publish payload as a UTF-8 string.
	 * 
	 * @param index
	 *        the index of the publish payload to get
	 * @return the string
	 */
	public synchronized String getPublishPayloadStringAtIndex(int index) {
		return getPublishPayloadStringAtIndex(index, "UTF-8");
	}

	/**
	 * Get a publish payload as a string.
	 * 
	 * @param index
	 *        the index of the publish payload to get
	 * @param charsetName
	 *        the charset name to treat the payload as
	 * @return the string
	 */
	public synchronized String getPublishPayloadStringAtIndex(int index, String charsetName) {
		try {
			return new String(getPublishPayloadAtIndex(index), charsetName);
		} catch ( UnsupportedEncodingException e ) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public synchronized void onSubscribe(InterceptSubscribeMessage msg) {
		subscribeMessages.add(msg);
		notifiyCallback(msg);
	}

	@Override
	public synchronized void onUnsubscribe(InterceptUnsubscribeMessage msg) {
		unsubscribeMessages.add(msg);
		notifiyCallback(msg);
	}

	@Override
	public synchronized void onMessageAcknowledged(InterceptAcknowledgedMessage msg) {
		acknowledgedMessages.add(msg);
		notifiyCallback(msg);
	}

	private synchronized void notifiyCallback(InterceptMessage msg) {
		if ( callback != null ) {
			callback.handleInterceptMessage(msg);
		}
	}

	/**
	 * Get the configured callback.
	 * 
	 * @return the callback
	 */
	public Callback getCallback() {
		return callback;
	}

	/**
	 * Set a callback.
	 * 
	 * @param callback
	 *        the callback
	 */
	public void setCallback(Callback callback) {
		this.callback = callback;
	}

}

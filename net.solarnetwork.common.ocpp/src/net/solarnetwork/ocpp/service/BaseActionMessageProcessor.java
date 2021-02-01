/* ==================================================================
 * BaseActionMessageProcessor.java - 16/02/2020 7:05:34 pm
 * 
 * Copyright 2020 SolarNetwork.net Dev Team
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

package net.solarnetwork.ocpp.service;

import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.solarnetwork.ocpp.domain.ActionMessage;
import ocpp.domain.Action;

/**
 * An abstract base implementation of {@link ActionMessageProcessor}.
 * 
 * @param <T>
 *        the message type
 * @param <R>
 *        the result type
 * @author matt
 * @version 1.1
 */
public abstract class BaseActionMessageProcessor<T, R> implements ActionMessageProcessor<T, R> {

	private final Class<T> messageType;
	private final Class<R> resultType;
	private final Set<Action> supportedActions;
	private final boolean emptyMessageAllowed;

	/** A class-level logger. */
	protected Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * Constructor.
	 * 
	 * <p>
	 * The {@code emptyMessagesAllowed} property will be set to
	 * {@literal false}.
	 * </p>
	 * 
	 * @param messageType
	 *        the message type
	 * @param resultType
	 *        the result type
	 * @param supportedActions
	 *        the supported actions
	 * @throws IllegalArgumentException
	 *         if {@code supportedActions} is {@literal null}
	 */
	public BaseActionMessageProcessor(Class<T> messageType, Class<R> resultType,
			Set<Action> supportedActions) {
		this(messageType, resultType, supportedActions, false);
	}

	/**
	 * Constructor.
	 * 
	 * @param messageType
	 *        the message type
	 * @param resultType
	 *        the result type
	 * @param supportedActions
	 *        the supported actions
	 * @param emptyMessageAllowed
	 *        {@literal true} if a {@literal null} message is allowed
	 * @throws IllegalArgumentException
	 *         if {@code supportedActions} is {@literal null}
	 * @since 1.1
	 */
	public BaseActionMessageProcessor(Class<T> messageType, Class<R> resultType,
			Set<Action> supportedActions, boolean emptyMessageAllowed) {
		super();
		this.messageType = messageType;
		this.resultType = resultType;
		if ( supportedActions == null ) {
			throw new IllegalArgumentException("The supportedActions parameter must not be null.");
		}
		this.supportedActions = supportedActions;
		this.emptyMessageAllowed = emptyMessageAllowed;
	}

	@Override
	public Set<Action> getSupportedActions() {
		return supportedActions;
	}

	/**
	 * Test if a specific message is supported by this processor.
	 * 
	 * <p>
	 * This implementation returns {@literal true} if either:
	 * </p>
	 * 
	 * <ol>
	 * <li>The {@code messageType} is {@literal null} or
	 * {@code emptyMessageAllowed} is {@literal true} and the
	 * {@code message.getMessage()} is also {@literal null}.</li>
	 * <li>The {@code messageType} is not {@literal null} and is assignable from
	 * {@code message.getMessage().getClass()}.</li>
	 * </ol>
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public boolean isMessageSupported(ActionMessage<?> message) {
		return (message != null && message.getAction() != null
				&& supportedActions.contains(message.getAction())
				&& (((messageType == null || emptyMessageAllowed) && message.getMessage() == null)
						|| (messageType != null && message.getMessage() != null
								&& messageType.isAssignableFrom(message.getMessage().getClass()))));
	}

	/**
	 * Get the message type.
	 * 
	 * @return the message type
	 */
	public Class<T> getMessageType() {
		return messageType;
	}

	/**
	 * Get the result type.
	 * 
	 * @return the result type
	 */
	public Class<R> getResultType() {
		return resultType;
	}

}

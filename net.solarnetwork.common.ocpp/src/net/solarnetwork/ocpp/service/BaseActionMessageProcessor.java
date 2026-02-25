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

import static net.solarnetwork.util.ObjectUtils.requireNonNullArgument;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.solarnetwork.ocpp.domain.Action;
import net.solarnetwork.ocpp.domain.ActionMessage;
import net.solarnetwork.ocpp.domain.ErrorCode;
import net.solarnetwork.ocpp.domain.ErrorCodeException;
import net.solarnetwork.util.ObjectUtils;

/**
 * An abstract base implementation of {@link ActionMessageProcessor}.
 *
 * @param <T>
 *        the message type
 * @param <R>
 *        the result type
 * @author matt
 * @version 1.2
 */
public abstract class BaseActionMessageProcessor<T, R> implements ActionMessageProcessor<T, R> {

	private final @Nullable Class<T> messageType;
	private final @Nullable Class<R> resultType;
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
	 *         if {@code supportedActions} is {@code null}
	 */
	public BaseActionMessageProcessor(@Nullable Class<T> messageType, @Nullable Class<R> resultType,
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
	 *        {@literal true} if a {@code null} message is allowed
	 * @throws IllegalArgumentException
	 *         if {@code supportedActions} is {@code null}
	 * @since 1.1
	 */
	public BaseActionMessageProcessor(@Nullable Class<T> messageType, @Nullable Class<R> resultType,
			Set<Action> supportedActions, boolean emptyMessageAllowed) {
		super();
		this.messageType = messageType;
		this.resultType = resultType;
		this.supportedActions = ObjectUtils.requireNonNullArgument(supportedActions, "supportedActions");
		this.emptyMessageAllowed = emptyMessageAllowed;
	}

	@Override
	public final Set<Action> getSupportedActions() {
		return supportedActions;
	}

	/**
	 * Process a message with a required client identifier.
	 *
	 * <p>
	 * This method can be called from
	 * {@link ActionMessageProcessor#processActionMessage(ActionMessage, ActionMessageResultHandler)}
	 * by extending classes, to simplify handling messages that require a
	 * non-null client identifier value. This method will verify that
	 * {@code message.clientId.identifier} is not {@code null}. If it is
	 * {@code null} the result handler will be invoked, passing an
	 * {@link ErrorCodeException} configured with the given {@code errorCode}.
	 * </p>
	 * <p>
	 * Then, if {@code emptyMessageAllowed} is {@code true}, the
	 * {@link #handleActionMessageWithClientIdentifier(ActionMessage, ActionMessageResultHandler, String)}
	 * will be invoked (which extending classes must implement), passing the
	 * non-{@code null} client identifier.
	 * </p>
	 * <p>
	 * When {@code emptyMessageAllowed} is {@code false}, the
	 * {@code message.message} value is then verified to not be {@code null}. If
	 * it is {@code null} the result handler will be invoked, passing an
	 * {@link ErrorCodeException} configured with the given {@code errorCode}.
	 * Otherwise, the
	 * {@link #handleActionMessageWithClientIdentifier(ActionMessage, ActionMessageResultHandler, String, Object)}
	 * method will be invoked (which extending classes must implement), passing
	 * the non-{@code null} client identifier and message content.
	 * </p>
	 *
	 * @param message
	 *        the message
	 * @param resultHandler
	 *        the result handler
	 * @param errorCode
	 *        the error code to use if either the message or
	 *        {@code message.clientId.identifier} is {@code null}
	 * @see #handleActionMessageWithClientIdentifier(ActionMessage,
	 *      ActionMessageResultHandler, String)
	 * @since 1.2
	 */
	protected void processActionMessageWithClientIdentifier(ActionMessage<T> message,
			ActionMessageResultHandler<T, R> resultHandler, ErrorCode errorCode) {
		final String clientIdent;
		try {
			clientIdent = requireNonNullArgument(message.getClientId(), "clientId").getIdentifier();
		} catch ( IllegalArgumentException e ) {
			ErrorCodeException err = new ErrorCodeException(errorCode, "Missing client identifier.");
			resultHandler.handleActionMessageResult(message, null, err);
			return;
		}
		if ( emptyMessageAllowed ) {
			handleActionMessageWithClientIdentifier(message, resultHandler, clientIdent);
		} else {
			T msg = message.getMessage();
			if ( msg == null ) {
				ErrorCodeException err = new ErrorCodeException(errorCode, "Missing message content.");
				resultHandler.handleActionMessageResult(message, null, err);
				return;
			}
			handleActionMessageWithClientIdentifier(message, resultHandler, clientIdent, msg);
		}
	}

	/**
	 * Handle a message with a verified client identifier and message content.
	 *
	 * @param message
	 *        the message
	 * @param resultHandler
	 *        the result handler
	 * @param clientIdentifier
	 *        the client identifier
	 * @param msg
	 *        the message content
	 * @throws UnsupportedOperationException
	 *         unless overriden by extending classes
	 * @since 1.2
	 * @see #processActionMessageWithClientIdentifier(ActionMessage,
	 *      ActionMessageResultHandler, ErrorCode)
	 */
	protected void handleActionMessageWithClientIdentifier(ActionMessage<T> message,
			ActionMessageResultHandler<T, R> resultHandler, String clientIdentifier, T msg) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Handle a message with a verified client identifier.
	 *
	 * @param message
	 *        the message
	 * @param resultHandler
	 *        the result handler
	 * @param clientIdentifier
	 *        the client identifier
	 * @throws UnsupportedOperationException
	 *         unless overriden by extending classes
	 * @since 1.2
	 * @see #processActionMessageWithClientIdentifier(ActionMessage,
	 *      ActionMessageResultHandler, ErrorCode)
	 */
	protected void handleActionMessageWithClientIdentifier(ActionMessage<T> message,
			ActionMessageResultHandler<T, R> resultHandler, String clientIdentifier) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Test if a specific message is supported by this processor.
	 *
	 * <p>
	 * This implementation returns {@literal true} if either:
	 * </p>
	 *
	 * <ol>
	 * <li>The {@code messageType} is {@code null} or
	 * {@code emptyMessageAllowed} is {@literal true} and the
	 * {@code message.getMessage()} is also {@code null}.</li>
	 * <li>The {@code messageType} is not {@code null} and is assignable from
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
	public final @Nullable Class<T> getMessageType() {
		return messageType;
	}

	/**
	 * Get the result type.
	 *
	 * @return the result type
	 */
	public final @Nullable Class<R> getResultType() {
		return resultType;
	}

}

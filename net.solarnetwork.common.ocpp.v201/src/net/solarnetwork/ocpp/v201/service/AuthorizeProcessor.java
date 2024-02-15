/* ==================================================================
 * AuthorizeProcessor.java - 14/02/2020 11:23:03 am
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

package net.solarnetwork.ocpp.v201.service;

import java.util.Collections;
import java.util.Set;
import net.solarnetwork.ocpp.domain.ActionMessage;
import net.solarnetwork.ocpp.domain.AuthorizationInfo;
import net.solarnetwork.ocpp.domain.ErrorCodeException;
import net.solarnetwork.ocpp.service.ActionMessageResultHandler;
import net.solarnetwork.ocpp.service.AuthorizationService;
import net.solarnetwork.ocpp.service.BaseActionMessageProcessor;
import net.solarnetwork.ocpp.v201.domain.Action;
import net.solarnetwork.ocpp.v201.domain.ActionErrorCode;
import net.solarnetwork.ocpp.v201.util.OcppUtils;
import ocpp.v201.AuthorizeRequest;
import ocpp.v201.AuthorizeResponse;
import ocpp.v201.IdToken;
import ocpp.v201.IdTokenEnum;
import ocpp.v201.IdTokenInfo;

/**
 * Process {@link AuthorizeRequest} action messages.
 * 
 * @author matt
 * @version 1.0
 */
public class AuthorizeProcessor extends BaseActionMessageProcessor<AuthorizeRequest, AuthorizeResponse> {

	/** The supported actions of this processor. */
	public static final Set<net.solarnetwork.ocpp.domain.Action> SUPPORTED_ACTIONS = Collections
			.singleton(Action.Authorize);

	private final AuthorizationService authService;

	/**
	 * Constructor.
	 * 
	 * @param authService
	 *        the authorization service to use
	 */
	public AuthorizeProcessor(AuthorizationService authService) {
		super(AuthorizeRequest.class, AuthorizeResponse.class, SUPPORTED_ACTIONS);
		this.authService = authService;
	}

	@Override
	public void processActionMessage(ActionMessage<AuthorizeRequest> message,
			ActionMessageResultHandler<AuthorizeRequest, AuthorizeResponse> resultHandler) {
		AuthorizeRequest req = message.getMessage();
		if ( req == null ) {
			ErrorCodeException err = new ErrorCodeException(ActionErrorCode.FormatViolation,
					"Missing AuthorizeRequest message.");
			resultHandler.handleActionMessageResult(message, null, err);
			return;
		}
		try {
			AuthorizationInfo info = authService.authorize(message.getClientId(),
					req.getIdToken().getIdToken());

			IdTokenInfo tagInfo = new IdTokenInfo();
			if ( info.getParentId() != null ) {
				tagInfo.setGroupIdToken(new IdToken(info.getParentId(), IdTokenEnum.CENTRAL));
			}
			tagInfo.setStatus(OcppUtils.statusForStatus(info.getStatus()));
			if ( info.getExpiryDate() != null ) {
				tagInfo.setCacheExpiryDateTime(info.getExpiryDate());
			}

			AuthorizeResponse res = new AuthorizeResponse();
			res.setIdTokenInfo(tagInfo);

			resultHandler.handleActionMessageResult(message, res, null);
		} catch ( Throwable t ) {
			ErrorCodeException err = new ErrorCodeException(ActionErrorCode.InternalError,
					"Internal error: " + t.getMessage());
			resultHandler.handleActionMessageResult(message, null, err);
		}
	}

}

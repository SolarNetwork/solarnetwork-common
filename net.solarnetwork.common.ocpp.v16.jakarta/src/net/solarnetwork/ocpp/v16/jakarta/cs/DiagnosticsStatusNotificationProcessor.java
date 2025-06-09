/* ==================================================================
 * DiagnosticsStatusNotificationProcessor.java - 17/02/2020 6:52:11 am
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

package net.solarnetwork.ocpp.v16.jakarta.cs;

import java.util.Collections;
import java.util.Set;
import net.solarnetwork.ocpp.domain.Action;
import net.solarnetwork.ocpp.domain.ActionMessage;
import net.solarnetwork.ocpp.service.ActionMessageResultHandler;
import net.solarnetwork.ocpp.service.BaseActionMessageProcessor;
import net.solarnetwork.ocpp.v16.jakarta.CentralSystemAction;
import ocpp.v16.jakarta.cs.DiagnosticsStatusNotificationRequest;
import ocpp.v16.jakarta.cs.DiagnosticsStatusNotificationResponse;

/**
 * Process {@link DiagnosticsStatusNotificationRequest} action messages.
 * 
 * @author matt
 * @version 1.0
 */
public class DiagnosticsStatusNotificationProcessor extends
		BaseActionMessageProcessor<DiagnosticsStatusNotificationRequest, DiagnosticsStatusNotificationResponse> {

	/** The supported actions of this processor. */
	public static final Set<Action> SUPPORTED_ACTIONS = Collections
			.singleton(CentralSystemAction.DiagnosticsStatusNotification);

	/**
	 * Constructor.
	 */
	public DiagnosticsStatusNotificationProcessor() {
		super(DiagnosticsStatusNotificationRequest.class, DiagnosticsStatusNotificationResponse.class,
				SUPPORTED_ACTIONS);
	}

	@Override
	public void processActionMessage(ActionMessage<DiagnosticsStatusNotificationRequest> message,
			ActionMessageResultHandler<DiagnosticsStatusNotificationRequest, DiagnosticsStatusNotificationResponse> resultHandler) {
		DiagnosticsStatusNotificationRequest req = message.getMessage();
		log.info("OCPP DiagnosticsStatusNotification received from {}: {}", message.getClientId(),
				req.getStatus());
		resultHandler.handleActionMessageResult(message, new DiagnosticsStatusNotificationResponse(),
				null);
	}

}

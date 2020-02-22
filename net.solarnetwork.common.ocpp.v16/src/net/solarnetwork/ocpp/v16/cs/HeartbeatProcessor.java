/* ==================================================================
 * HeartbeatProcessor.java - 5/02/2020 9:37:10 am
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

package net.solarnetwork.ocpp.v16.cs;

import java.util.Collections;
import java.util.Set;
import net.solarnetwork.ocpp.domain.ActionMessage;
import net.solarnetwork.ocpp.service.ActionMessageResultHandler;
import net.solarnetwork.ocpp.service.BaseActionMessageProcessor;
import ocpp.domain.Action;
import ocpp.v16.CentralSystemAction;
import ocpp.v16.cs.HeartbeatRequest;
import ocpp.v16.cs.HeartbeatResponse;
import ocpp.xml.support.XmlDateUtils;

/**
 * Process {@link HeartbeatRequest} action messages.
 * 
 * <p>
 * This very simple processor directly responds with new
 * {@link HeartbeatResponse} instances with the current system time.
 * </p>
 * 
 * @author matt
 * @version 1.0
 */
public class HeartbeatProcessor extends BaseActionMessageProcessor<HeartbeatRequest, HeartbeatResponse> {

	/** The supported actions of this processor. */
	public static final Set<Action> SUPPORTED_ACTIONS = Collections
			.singleton(CentralSystemAction.Heartbeat);

	/**
	 * Constructor.
	 */
	public HeartbeatProcessor() {
		super(HeartbeatRequest.class, HeartbeatResponse.class, SUPPORTED_ACTIONS);
	}

	@Override
	public void processActionMessage(ActionMessage<HeartbeatRequest> message,
			ActionMessageResultHandler<HeartbeatRequest, HeartbeatResponse> resultHandler) {
		HeartbeatResponse res = new HeartbeatResponse();
		res.setCurrentTime(XmlDateUtils.newXmlCalendar());
		resultHandler.handleActionMessageResult(message, res, null);
	}

}

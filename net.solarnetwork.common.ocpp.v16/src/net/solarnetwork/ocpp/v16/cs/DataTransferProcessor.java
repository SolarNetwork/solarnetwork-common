/* ==================================================================
 * DataTransferProcessor.java - 16/02/2020 7:00:56 pm
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
import net.solarnetwork.ocpp.domain.Action;
import net.solarnetwork.ocpp.domain.ActionMessage;
import net.solarnetwork.ocpp.service.ActionMessageResultHandler;
import net.solarnetwork.ocpp.service.BaseActionMessageProcessor;
import net.solarnetwork.ocpp.v16.CentralSystemAction;
import ocpp.v16.cs.DataTransferRequest;
import ocpp.v16.cs.DataTransferResponse;
import ocpp.v16.cs.DataTransferStatus;

/**
 * Process {@link DataTransferRequest} action messages.
 *
 * <p>
 * This handler does not perform any function itself, other than respond with a
 * {@link DataTransferStatus#REJECTED} status.
 * </p>
 *
 * @author matt
 * @version 1.1
 */
public class DataTransferProcessor
		extends BaseActionMessageProcessor<DataTransferRequest, DataTransferResponse> {

	/** The supported actions of this processor. */
	public static final Set<Action> SUPPORTED_ACTIONS = Collections
			.singleton(CentralSystemAction.DataTransfer);

	/**
	 * Constructor.
	 */
	public DataTransferProcessor() {
		super(DataTransferRequest.class, DataTransferResponse.class, SUPPORTED_ACTIONS);
	}

	@Override
	public void processActionMessage(ActionMessage<DataTransferRequest> message,
			ActionMessageResultHandler<DataTransferRequest, DataTransferResponse> resultHandler) {
		DataTransferRequest req = message.getMessage();
		log.debug("OCPP DataTransfer received from {}; message ID = {}; vendor ID = {}; data = {}",
				message.getClientId(), req.getMessageId(), req.getVendorId(), req.getData());
		DataTransferResponse res = new DataTransferResponse();
		res.setStatus(DataTransferStatus.REJECTED);
		resultHandler.handleActionMessageResult(message, res, null);
	}

}

/* ==================================================================
 * AuthorizeProcessorTests.java - 14/02/2020 11:38:11 am
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

package net.solarnetwork.ocpp.v16.cs.test;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import net.solarnetwork.ocpp.domain.ActionMessage;
import net.solarnetwork.ocpp.domain.AuthorizationInfo;
import net.solarnetwork.ocpp.domain.AuthorizationStatus;
import net.solarnetwork.ocpp.domain.BasicActionMessage;
import net.solarnetwork.ocpp.service.AuthorizationService;
import net.solarnetwork.ocpp.v16.cs.AuthorizeProcessor;
import ocpp.v16.CentralSystemAction;
import ocpp.v16.cs.AuthorizeRequest;
import ocpp.v16.cs.IdTagInfo;

/**
 * Test cases for the {@link AuthorizeProcessor} class.
 * 
 * @author matt
 * @version 1.0
 */
public class AuthorizeProcessorTests {

	private AuthorizationService authService;
	private AuthorizeProcessor processor;

	@Before
	public void setup() {
		authService = EasyMock.createMock(AuthorizationService.class);
		processor = new AuthorizeProcessor(authService);
	}

	@After
	public void teardown() {
		EasyMock.verify(authService);
	}

	private void replayAll() {
		EasyMock.replay(authService);
	}

	@Test
	public void auth_ok() throws InterruptedException {
		// given
		CountDownLatch l = new CountDownLatch(1);
		String clientId = UUID.randomUUID().toString();
		String idTag = UUID.randomUUID().toString().substring(0, 20);
		AuthorizationInfo auth = AuthorizationInfo.builder().withId(idTag)
				.withStatus(AuthorizationStatus.Accepted).build();

		expect(authService.authorize(clientId, idTag)).andReturn(auth);

		// when
		replayAll();

		AuthorizeRequest req = new AuthorizeRequest();
		req.setIdTag(idTag);
		ActionMessage<AuthorizeRequest> message = new BasicActionMessage<AuthorizeRequest>(clientId,
				CentralSystemAction.Authorize, req);
		processor.processActionMessage(message, (msg, res, err) -> {
			assertThat("Message passed", msg, sameInstance(message));
			assertThat("Result available", res, notNullValue());
			assertThat("No error", err, nullValue());

			IdTagInfo tagInfo = res.getIdTagInfo();
			assertThat("Result info available", tagInfo, notNullValue());
			assertThat("Result tag status", tagInfo.getStatus(),
					equalTo(ocpp.v16.cs.AuthorizationStatus.ACCEPTED));

			l.countDown();
			return true;
		});

		// then
		assertThat("Result handler invoked", l.await(1, TimeUnit.SECONDS), equalTo(true));
	}

}

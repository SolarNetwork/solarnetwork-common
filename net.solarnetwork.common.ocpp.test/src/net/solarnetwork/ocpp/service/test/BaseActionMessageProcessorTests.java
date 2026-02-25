/* ==================================================================
 * BaseActionMessageProcessorTests.java - 26/02/2026 8:48:04 am
 *
 * Copyright 2026 SolarNetwork.net Dev Team
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

package net.solarnetwork.ocpp.service.test;

import static net.solarnetwork.test.CommonTestUtils.randomString;
import static org.assertj.core.api.BDDAssertions.from;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.InstanceOfAssertFactories.throwable;
import java.util.Set;
import org.junit.Test;
import net.solarnetwork.ocpp.domain.Action;
import net.solarnetwork.ocpp.domain.ActionMessage;
import net.solarnetwork.ocpp.domain.BasicActionMessage;
import net.solarnetwork.ocpp.domain.ChargePointIdentity;
import net.solarnetwork.ocpp.domain.ErrorCode;
import net.solarnetwork.ocpp.domain.ErrorCodeException;
import net.solarnetwork.ocpp.service.ActionMessageResultHandler;
import net.solarnetwork.ocpp.service.BaseActionMessageProcessor;

/**
 * Test cases for the {@link BaseActionMessageProcessor} class.
 *
 * @author matt
 * @version 1.0
 */
public class BaseActionMessageProcessorTests {

	private enum Work implements Action {

		LazeAbout;

		@Override
		public String getName() {
			return name();
		}
	}

	private enum Fail implements ErrorCode {

		Refuse;

		@Override
		public String getName() {
			return name();
		}

	}

	private BaseActionMessageProcessor<Object, Boolean> testProcessor(boolean emptyMessageAllowed) {
		return new BaseActionMessageProcessor<Object, Boolean>(Object.class, Boolean.class,
				Set.of(Work.LazeAbout), emptyMessageAllowed) {

			@Override
			public void processActionMessage(ActionMessage<Object> message,
					ActionMessageResultHandler<Object, Boolean> resultHandler) {
				processActionMessageWithClientIdentifier(message, resultHandler, Fail.Refuse);
			}

			@Override
			protected void handleActionMessageWithClientIdentifier(ActionMessage<Object> message,
					ActionMessageResultHandler<Object, Boolean> resultHandler,
					ChargePointIdentity identity, Object msg) {
				resultHandler.handleActionMessageResult(message, Boolean.TRUE, null);
			}

			@Override
			protected void handleActionMessageWithClientIdentifier(ActionMessage<Object> message,
					ActionMessageResultHandler<Object, Boolean> resultHandler,
					ChargePointIdentity identity) {
				resultHandler.handleActionMessageResult(message, Boolean.FALSE, null);
			}

		};
	}

	@Test
	public void processWithClientIdentifier_noChargePointIdentity() {
		// GIVEN
		final var processor = testProcessor(false);
		final var message = new BasicActionMessage<>(null, Work.LazeAbout, new Object());

		// WHEN
		processor.processActionMessage(message, (msg, result, error) -> {
			// THEN
			// @formatter:off
			then(msg)
				.as("Handler passed original message")
				.isSameAs(message)
				;

			then(result)
				.as("Result not provided because of missing ChargePointIdentity")
				.isNull()
				;

			then(error)
				.as("ErrorCodeException provided")
				.asInstanceOf(throwable(ErrorCodeException.class))
				.hasMessage("Missing identity.")
				.returns(Fail.Refuse, from(ErrorCodeException::getErrorCode))
				;
			// @formatter:on
			return true;
		});
	}

	@Test
	public void processWithClientIdentifier_noContent() {
		// GIVEN
		final var processor = testProcessor(false);
		final var cpIdent = new ChargePointIdentity(randomString(), randomString());
		final var message = new BasicActionMessage<>(cpIdent, Work.LazeAbout, null);

		// WHEN
		processor.processActionMessage(message, (msg, result, error) -> {
			// THEN
			// @formatter:off
			then(msg)
				.as("Handler passed original message")
				.isSameAs(message)
				;

			then(result)
				.as("Result not provided because of missing message content")
				.isNull()
				;

			then(error)
				.as("ErrorCodeException provided")
				.asInstanceOf(throwable(ErrorCodeException.class))
				.hasMessage("Missing message content.")
				.returns(Fail.Refuse, from(ErrorCodeException::getErrorCode))
				;
			// @formatter:on
			return true;
		});
	}

	@Test
	public void processWithClientIdentifier() {
		// GIVEN
		final var processor = testProcessor(false);
		final var cpIdent = new ChargePointIdentity(randomString(), randomString());
		final var message = new BasicActionMessage<>(cpIdent, Work.LazeAbout, new Object());

		// WHEN
		processor.processActionMessage(message, (msg, result, error) -> {
			// THEN
			// @formatter:off
			then(msg)
				.as("Handler passed original message")
				.isSameAs(message)
				;

			then(result)
				.as("Result provided because of provided identifier")
				.isTrue()
				;

			then(error)
				.as("No exception generated")
				.isNull()
				;
			// @formatter:on
			return true;
		});
	}

	@Test
	public void processWithClientIdentifier_optionalContent() {
		// GIVEN
		final var processor = testProcessor(true);
		final var cpIdent = new ChargePointIdentity(randomString(), randomString());
		final var message = new BasicActionMessage<>(cpIdent, Work.LazeAbout, null);

		// WHEN
		processor.processActionMessage(message, (msg, result, error) -> {
			// THEN
			// @formatter:off
			then(msg)
				.as("Handler passed original message")
				.isSameAs(message)
				;

			then(result)
				.as("Result provided because of provided identifier but optional content")
				.isFalse()
				;

			then(error)
				.as("No exception generated")
				.isNull()
				;
			// @formatter:on
			return true;
		});
	}

}

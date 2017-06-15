/* ==================================================================
 * TransactionSynchronizationEventAdminAdapter.java - 15/06/2017 12:13:40 PM
 * 
 * Copyright 2017 SolarNetwork.net Dev Team
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

package net.solarnetwork.support;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * EventAdmin implementation that can synchronize the posting of events to the
 * current thread's active transaction.
 * 
 * <p>
 * This can be useful for event handlers that need to access data related to the
 * active transaction, for example by querying the data after the transaction
 * completes. If no transaction is available when {@link #postEvent(Event)} is
 * called, it will be immediately passed to the configured delegate
 * {@link EventAdmin}. Otherwise the event will be registered to post when the
 * configured transaction phase occurs.
 * </p>
 * 
 * <p>
 * The {@link #sendEvent(Event)} does not synchronize with any transactions.
 * Instead it immediately calls {@link EventAdmin#sendEvent(Event)} on the
 * configured delegate.
 * </p>
 * 
 * @author matt
 * @version 1.0
 * @since 1.36
 */
public class TransactionSynchronizationEventAdminAdapter implements EventAdmin {

	private final EventAdmin delegate;

	private TransactionPhase phase = TransactionPhase.AFTER_COMPLETION;
	private int order = 0;

	public TransactionSynchronizationEventAdminAdapter(EventAdmin delegate) {
		super();
		assert delegate != null;
		this.delegate = delegate;
	}

	@Override
	public void postEvent(Event event) {
		if ( TransactionSynchronizationManager.isSynchronizationActive() ) {
			TransactionSynchronization transactionSynchronization = new TransactionSynchronizationEventAdapter(
					delegate, event, phase, order);
			TransactionSynchronizationManager.registerSynchronization(transactionSynchronization);
		} else {
			delegate.postEvent(event);
		}
	}

	@Override
	public void sendEvent(Event event) {
		delegate.sendEvent(event);
	}

	private static class TransactionSynchronizationEventAdapter
			extends TransactionSynchronizationAdapter {

		private final EventAdmin eventAdmin;
		private final Event event;
		private final TransactionPhase phase;
		private int order = 0;

		public TransactionSynchronizationEventAdapter(EventAdmin eventAdmin, Event event,
				TransactionPhase phase, int order) {
			this.eventAdmin = eventAdmin;
			this.event = event;
			this.phase = phase;
			this.order = order;
		}

		@Override
		public void beforeCommit(boolean readOnly) {
			if ( phase == TransactionPhase.BEFORE_COMMIT ) {
				processEvent();
			}
		}

		@Override
		public void afterCompletion(int status) {
			if ( phase == TransactionPhase.AFTER_COMPLETION ) {
				processEvent();
			} else if ( phase == TransactionPhase.AFTER_COMMIT && status == STATUS_COMMITTED ) {
				processEvent();
			} else if ( phase == TransactionPhase.AFTER_ROLLBACK && status == STATUS_ROLLED_BACK ) {
				processEvent();
			}
		}

		@Override
		public int getOrder() {
			return order;
		}

		protected void processEvent() {
			eventAdmin.postEvent(event);
		}
	}

	/**
	 * Set the transaction phase to associate posting events with.
	 * 
	 * <p>
	 * This defaults to {@link TransactionPhase#AFTER_COMPLETION} so events are
	 * posted always, after commit or rollback.
	 * </p>
	 * 
	 * @param phase
	 *        the phase to set
	 */
	public void setPhase(TransactionPhase phase) {
		assert phase != null;
		this.phase = phase;
	}

	/**
	 * Set the order to attach to synchronized events.
	 * 
	 * <p>
	 * When events are synchronized to a transaction phase, this order will be
	 * used to rank all registered tasks for that phase. That includes events
	 * posted by this service as well as any other tasks registered with the
	 * transaction.
	 * </p>
	 * 
	 * @param order
	 *        the order to set
	 */
	public void setOrder(int order) {
		this.order = order;
	}

}

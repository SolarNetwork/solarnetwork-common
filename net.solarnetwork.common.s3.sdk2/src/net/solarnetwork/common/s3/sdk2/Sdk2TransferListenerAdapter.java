/* ==================================================================
 * Sdk2TransferListenerAdapter.java - 17/06/2024 8:48:43 am
 *
 * Copyright 2024 SolarNetwork.net Dev Team
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

package net.solarnetwork.common.s3.sdk2;

import java.util.OptionalDouble;
import net.solarnetwork.service.ProgressListener;
import net.solarnetwork.util.ObjectUtils;
import software.amazon.awssdk.transfer.s3.progress.TransferListener;
import software.amazon.awssdk.transfer.s3.progress.TransferListener.Context.BytesTransferred;
import software.amazon.awssdk.transfer.s3.progress.TransferListener.Context.TransferComplete;
import software.amazon.awssdk.transfer.s3.progress.TransferListener.Context.TransferFailed;
import software.amazon.awssdk.transfer.s3.progress.TransferListener.Context.TransferInitiated;

/**
 * Adapts {@link ProgressListener} to the AWS SDK V2 {@code TransferListener}.
 *
 * @param <T>
 *        the listener type
 *
 * @author matt
 * @version 1.0
 */
public class Sdk2TransferListenerAdapter<T> implements TransferListener {

	private final ProgressListener<T> delegate;
	private final T context;

	/**
	 * Constructor.
	 *
	 * @param delegate
	 *        the listener delegate
	 * @param context
	 *        the listener context
	 */
	public Sdk2TransferListenerAdapter(ProgressListener<T> delegate, T context) {
		super();
		this.delegate = ObjectUtils.requireNonNullArgument(delegate, "delegate");
		this.context = ObjectUtils.requireNonNullArgument(context, "context");
	}

	private void handleTransfer(TransferInitiated context) {
		OptionalDouble amt = context.progressSnapshot().ratioTransferred();
		if ( amt.isPresent() ) {
			delegate.progressChanged(this.context, amt.getAsDouble());
		}
	}

	@Override
	public void transferInitiated(TransferInitiated context) {
		handleTransfer(context);
	}

	@Override
	public void bytesTransferred(BytesTransferred context) {
		handleTransfer(context);
	}

	@Override
	public void transferComplete(TransferComplete context) {
		handleTransfer(context);
	}

	@Override
	public void transferFailed(TransferFailed context) {
		// nothing
	}

}

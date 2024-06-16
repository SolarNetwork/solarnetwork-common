/* ==================================================================
 * SdkTransferProgressListenerAdapter.java - 15/10/2019 2:21:39 pm
 *
 * Copyright 2019 SolarNetwork.net Dev Team
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

package net.solarnetwork.common.s3.sdk;

import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressEventType;
import net.solarnetwork.service.ProgressListener;

/**
 * Adapts {@link ProgressListener} to the AWS SDK {@code ProgressListener}.
 *
 * @param <T>
 *        the listener type
 * @author matt
 * @version 2.0
 */
public class SdkTransferProgressListenerAdapter<T> implements com.amazonaws.event.ProgressListener {

	private final ProgressListener<T> delegate;
	private final T context;
	private final boolean trackRequest;
	private long totalBytes;
	private long totalBytesTransferred;

	/**
	 * Constructor.
	 *
	 * @param delegate
	 *        the listener delegate
	 * @param context
	 *        the listener context
	 * @param trackRequest
	 *        {@literal true} to track upload (request) progress,
	 *        {@literal false} to track download (response) progress
	 */
	public SdkTransferProgressListenerAdapter(ProgressListener<T> delegate, T context,
			boolean trackRequest) {
		super();
		this.delegate = delegate;
		this.context = context;
		this.trackRequest = trackRequest;
		this.totalBytes = 0;
		this.totalBytesTransferred = 0;
	}

	@Override
	public void progressChanged(ProgressEvent progressEvent) {
		if ( delegate == null ) {
			return;
		}
		ProgressEventType type = progressEvent.getEventType();
		if ( !type.isByteCountEvent() ) {
			return;
		}
		switch (type) {
			case REQUEST_CONTENT_LENGTH_EVENT:
				if ( trackRequest ) {
					this.totalBytes = progressEvent.getBytes();
				}
				return;

			case RESPONSE_CONTENT_LENGTH_EVENT:
				if ( !trackRequest ) {
					this.totalBytes = progressEvent.getBytes();
				}
				return;

			default:
				this.totalBytesTransferred += progressEvent.getBytesTransferred();
		}
		double progress = totalBytes > 0 ? (double) totalBytesTransferred / (double) totalBytes : 0.0;
		delegate.progressChanged(context, progress);
	}

}

/* ==================================================================
 * ProvidedOutputStream.java - 19/03/2024 7:19:04 am
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

package net.solarnetwork.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Supplier;
import net.solarnetwork.util.ObjectUtils;

/**
 * Output stream that uses a {@link Supplier} to obtain a delegate
 * {@link OutputStream} instance.
 *
 * <p>
 * This can be useful in web response processing, where the response stream can
 * only be obtained once before throwing an exception. If some other exception
 * occurs before any writing to the output stream has started, generating an
 * error response is difficult when accessing the response writer a second time
 * throws an exception.
 * </p>
 *
 * <p>
 * The {@link #flush()} and {@link #close()} methods will not attempt to acquire
 * the delegate {@link OutputStream} from the given {@link Supplier} if it has
 * not already been acquired. All other methods will cause the delegate to be
 * acquired from the supplier once, reusing the resulting writer on all
 * subsequent method calls.
 * </p>
 *
 * <p>
 * If an {@link IllegalStateException} is thrown by the supplier, or the
 * supplier returns {@code null}, then a {@link NullOutputStream} will be used
 * as the delegate writer.
 * </p>
 *
 * @author matt
 * @version 1.0
 * @since 3.8
 */
public class ProvidedOutputStream extends OutputStream {

	private final Supplier<OutputStream> provider;
	private OutputStream delegate;

	/**
	 * Constructor.
	 *
	 * @param provider
	 *        the writer provider
	 */
	public ProvidedOutputStream(Supplier<OutputStream> provider) {
		super();
		this.provider = ObjectUtils.requireNonNullArgument(provider, "provider");
	}

	private OutputStream delegate() {
		if ( this.delegate != null ) {
			return this.delegate;
		}
		OutputStream delegate = null;
		try {
			delegate = provider.get();
		} catch ( IllegalStateException e ) {
			// ignore and continue
		}
		if ( delegate == null ) {
			delegate = NullOutputStream.INSTANCE;
		}
		this.delegate = delegate;
		return delegate;
	}

	@Override
	public void write(int b) throws IOException {
		final OutputStream delegate = delegate();
		delegate.write(b);
	}

	@Override
	public void flush() throws IOException {
		final OutputStream delegate = this.delegate;
		if ( delegate != null ) {
			delegate.flush();
		}
	}

	@Override
	public void close() throws IOException {
		final OutputStream delegate = this.delegate;
		if ( delegate != null ) {
			delegate.close();
		}
	}

	@Override
	public void write(byte[] b) throws IOException {
		final OutputStream delegate = delegate();
		delegate.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		final OutputStream delegate = delegate();
		delegate.write(b, off, len);
	}

}

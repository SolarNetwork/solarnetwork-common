/* ==================================================================
 * ProvidedWriter.java - 19/03/2024 6:44:53 am
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
import java.io.Writer;
import java.util.function.Supplier;
import net.solarnetwork.util.ObjectUtils;

/**
 * Writer that uses a {@link Supplier} to obtain a delegate {@link Writer}
 * instance.
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
 * the delegate {@link Writer} from the given {@link Supplier} if it has not
 * already been acquired. All other methods will cause the delegate to be
 * acquired from the supplier once, reusing the resulting writer on all
 * subsequent method calls.
 * </p>
 *
 * <p>
 * If an {@link IllegalStateException} is thrown by the supplier, or the
 * supplier returns {@code null}, then a {@link NullWriter} will be used as the
 * delegate writer.
 * </p>
 *
 * @author matt
 * @version 1.0
 * @since 3.8
 */
public class ProvidedWriter extends Writer {

	private final Supplier<Writer> provider;
	private Writer delegate;

	/**
	 * Constructor.
	 *
	 * @param provider
	 *        the writer provider
	 */
	public ProvidedWriter(Supplier<Writer> provider) {
		super();
		this.provider = ObjectUtils.requireNonNullArgument(provider, "provider");
	}

	/**
	 * Constructor.
	 *
	 * @param provider
	 *        the writer provider
	 * @param lock
	 *        the object to synchronize on
	 */
	public ProvidedWriter(Supplier<Writer> provider, Object lock) {
		super(lock);
		this.provider = ObjectUtils.requireNonNullArgument(provider, "provider");
	}

	private Writer delegate() {
		if ( this.delegate != null ) {
			return this.delegate;
		}
		Writer delegate = null;
		try {
			delegate = provider.get();
		} catch ( IllegalStateException e ) {
			// ignore and continue
		}
		if ( delegate == null ) {
			delegate = NullWriter.INSTANCE;
		}
		this.delegate = delegate;
		return delegate;
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		Writer delegate = delegate();
		delegate.write(cbuf, off, len);
	}

	@Override
	public void flush() throws IOException {
		final Writer delegate = this.delegate;
		if ( delegate != null ) {
			delegate.flush();
		}
	}

	@Override
	public void close() throws IOException {
		final Writer delegate = this.delegate;
		if ( delegate != null ) {
			delegate.close();
		}
	}

	@Override
	public void write(int c) throws IOException {
		Writer delegate = delegate();
		delegate.write(c);
	}

	@Override
	public void write(char[] cbuf) throws IOException {
		Writer delegate = delegate();
		delegate.write(cbuf);
	}

	@Override
	public void write(String str) throws IOException {
		Writer delegate = delegate();
		delegate.write(str);
	}

	@Override
	public void write(String str, int off, int len) throws IOException {
		Writer delegate = delegate();
		delegate.write(str, off, len);
	}

	@Override
	public Writer append(CharSequence csq) throws IOException {
		Writer delegate = delegate();
		return delegate.append(csq);
	}

	@Override
	public Writer append(CharSequence csq, int start, int end) throws IOException {
		Writer delegate = delegate();
		return delegate.append(csq, start, end);
	}

	@Override
	public Writer append(char c) throws IOException {
		Writer delegate = delegate();
		return delegate.append(c);
	}

}

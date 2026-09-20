/* ==================================================================
 * package-info.java - 19/09/2026 9:00:00 am
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

/**
 * Support for HTTP Message Signatures, as defined in RFC 9421.
 *
 * <p>
 * This package is protocol-agnostic with respect to the runtime HTTP stack: a
 * message is adapted to the {@link net.solarnetwork.security.http.sig.SignatureContext}
 * interface, and everything else is derived from that. It must not gain any
 * servlet dependency.
 * </p>
 *
 * <p>
 * <b>Note</b> this package is a stand-in for a {@code net.solarnetwork.security}
 * package in the {@code solarnetwork-common} project, and is expected to move
 * there once that library is released.
 * </p>
 */

@NullMarked
package net.solarnetwork.security.http.sig;

import org.jspecify.annotations.NullMarked;

/* ==================================================================
 * SimpleTariffRate.java - 12/05/2021 9:36:45 AM
 * 
 * Copyright 2021 SolarNetwork.net Dev Team
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

package net.solarnetwork.domain.tariff;

import java.math.BigDecimal;
import java.util.Objects;
import net.solarnetwork.util.StringUtils;

/**
 * Simple implementation of {@link Tariff.Rate}.
 * 
 * @author matt
 * @version 1.0
 * @since 1.71
 */
public class SimpleTariffRate implements Tariff.Rate {

	private final String id;
	private final String description;
	private final BigDecimal amount;

	/**
	 * Constructor.
	 * 
	 * <p>
	 * This method will derive the {@code id} from the {@code description} via
	 * {@link StringUtils.simpleIdValue(String)}.
	 * </p>
	 * 
	 * @param description
	 *        the description
	 * @param amount
	 *        the amount
	 * @throws IllegalArgumentException
	 *         if any argument is {@literal null}
	 */
	public SimpleTariffRate(String description, BigDecimal amount) {
		this(StringUtils.simpleIdValue(description), description, amount);
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *        the ID
	 * @param description
	 *        the description
	 * @param amount
	 *        the amount
	 * @throws IllegalArgumentException
	 *         if any argument is {@literal null}
	 */
	public SimpleTariffRate(String id, String description, BigDecimal amount) {
		super();
		if ( id == null ) {
			throw new IllegalArgumentException("The id argument must not be null.");
		}
		this.id = id;
		if ( description == null ) {
			throw new IllegalArgumentException("The description argument must not be null.");
		}
		this.description = description;
		if ( amount == null ) {
			throw new IllegalArgumentException("The amount argument must not be null.");
		}
		this.amount = amount;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SimpleTariffRate{");
		if ( id != null ) {
			builder.append("id=");
			builder.append(id);
			builder.append(", ");
		}
		if ( amount != null ) {
			builder.append("amount=");
			builder.append(amount);
		}
		builder.append("}");
		return builder.toString();
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public BigDecimal getAmount() {
		return amount;
	}

	@Override
	public int hashCode() {
		return Objects.hash(amount, id);
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( !(obj instanceof SimpleTariffRate) ) {
			return false;
		}
		SimpleTariffRate other = (SimpleTariffRate) obj;
		return Objects.equals(amount, other.amount) && Objects.equals(id, other.id);
	}

}

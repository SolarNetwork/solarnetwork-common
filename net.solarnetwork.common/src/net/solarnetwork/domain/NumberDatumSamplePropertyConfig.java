/* ==================================================================
 * NumberDatumSamplePropertyConfig.java - 27/09/2019 1:18:30 pm
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

package net.solarnetwork.domain;

import static net.solarnetwork.util.NumberUtils.maximumDecimalScale;
import static net.solarnetwork.util.NumberUtils.multiplied;
import static net.solarnetwork.util.NumberUtils.offset;
import java.math.BigDecimal;

/**
 * Extension of {@link GeneralDatumSamplePropertyConfig} specifically designed
 * to help with number property values.
 * 
 * <p>
 * This class contains two pairs of linear equation property pairs that can be
 * used to transform raw data values <i>x</i> into appropriate datum values
 * <i>y</i>. Generally just one pair needs to be used; sometimes it is more
 * convenient to use one pair over the other.
 * </p>
 * 
 * <p>
 * First, the {@code unitSlope} <i>M</i> and {@code unitIntercept} <i>B</i>
 * properties are calculated using the equation {@literal y = M * (x + B)}.
 * Second, the {@code slope} <i>m</i> and {@code intercept} <i>b</i> properties
 * are used with an equation like {@literal y = (m * x) + b}. If both are
 * specified, the overall equation is thus
 * {@literal y = (m * (M * (x + B))) + b}.
 * </p>
 * 
 * @author matt
 * @version 1.0
 * @since 1.54
 */
public class NumberDatumSamplePropertyConfig<V> extends GeneralDatumSamplePropertyConfig<V> {

	/** The default value for the {@code propertyType} property. */
	public static final GeneralDatumSamplesType DEFAULT_PROPERTY_TYPE = GeneralDatumSamplesType.Instantaneous;

	/**
	 * The default value for the {@code slope} and {@code unitSlope} properties.
	 */
	public static final BigDecimal DEFAULT_SLOPE = BigDecimal.ONE;

	/**
	 * The default value for the {@code intercept} and {@code unitIntercept}
	 * properties.
	 */
	public static final BigDecimal DEFAULT_INTERCEPT = BigDecimal.ZERO;

	/** The default value for the {@code decimalScale} property. */
	public static final int DEFAULT_DECIMAL_SCALE = 5;

	private BigDecimal slope = DEFAULT_SLOPE; // m
	private BigDecimal intercept = DEFAULT_INTERCEPT; // b 
	private BigDecimal unitSlope = DEFAULT_SLOPE; // M
	private BigDecimal unitIntercept = DEFAULT_INTERCEPT; // B
	private int decimalScale = DEFAULT_DECIMAL_SCALE;

	/**
	 * Default constructor.
	 */
	public NumberDatumSamplePropertyConfig() {
		super(null, DEFAULT_PROPERTY_TYPE, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param propertyKey
	 *        the datum property name to assign
	 * @param propertyType
	 *        the datum property type
	 * @param config
	 *        the configuration value
	 */
	public NumberDatumSamplePropertyConfig(String propertyKey, GeneralDatumSamplesType propertyType,
			V config) {
		super(propertyKey, propertyType != null ? propertyType : DEFAULT_PROPERTY_TYPE, config);
	}

	/**
	 * Apply the configured slope, intercept, unit slope, unit intercept, and
	 * decimal scale to a number value.
	 * 
	 * <p>
	 * This executes the equation {@literal y = (m * (M * (x + B))) + b} and
	 * applys {@link #getDecimalScale()} to the result. The variables are:
	 * </p>
	 * 
	 * <ul>
	 * <li><i>x</i> - the {@code value} passed to this method</li>
	 * <li><i>M</i> - the {@link #getUnitSlope()} value</li>
	 * <li><i>B</i> - the {@link #getUnitIntercept()} value</li>
	 * <li><i>m</i> - the {@link #getSlope()} value</li>
	 * <li><i>b</i> - the {@link #getIntercept()} value</li>
	 * </ul>
	 * 
	 * @param value
	 *        the number to apply the transform properties to
	 * @return the result, or {@literal null} if {@code value} is
	 *         {@literal null}
	 */
	public Number applyTransformations(Number value) {
		if ( value == null ) {
			return null;
		}
		return maximumDecimalScale(
				offset(multiplied(multiplied(offset(value, unitIntercept), unitSlope), slope),
						intercept),
				decimalScale);
	}

	/**
	 * Get the slope multiplier.
	 * 
	 * <p>
	 * This value represents <i>m</i> in the equation {@code y = mx + b}. For
	 * example, a power meter might report power as <i>killowatts</i>, in which
	 * case {@code slope} can be configured as {@literal .001} to convert the
	 * value to <i>watts</i>.
	 * </p>
	 * 
	 * @return the slope multiplier, never {@literal null}; defaults to
	 *         {@link #DEFAULT_SLOPE}
	 */
	public BigDecimal getSlope() {
		return slope;
	}

	/**
	 * Set the slope multiplier.
	 * 
	 * @param slope
	 *        the slope multiplier to set; if {@literal null}
	 *        {@link #DEFAULT_SLOPE} will be used instead
	 */
	public void setSlope(BigDecimal slope) {
		this.slope = slope != null ? slope : DEFAULT_SLOPE;
	}

	/**
	 * Get the y-intercept offset.
	 * 
	 * <p>
	 * This value represents <i>b</i> in the equation {@code y = mx + b}. For
	 * example, a sensor might report values in the range {@literal -10..10}
	 * which should be interpreted as values in the range {@literal 0..20}, in
	 * which case {@code intercept} can be configured as {@literal 10} to
	 * convert the value appropriately.
	 * </p>
	 * 
	 * @return the y-intercept, never {@literal null}; defaults to
	 *         {@link #DEFAULT_INTERCEPT}
	 */
	public BigDecimal getIntercept() {
		return intercept;
	}

	/**
	 * Set the y-intercept offset.
	 * 
	 * @param intercept
	 *        the intercept offset to set; if {@literal null}
	 *        {@link #DEFAULT_INTERCEPT} will be used instead
	 */
	public void setIntercept(BigDecimal intercept) {
		this.intercept = intercept != null ? intercept : DEFAULT_INTERCEPT;
	}

	/**
	 * Get the unit slope multiplier.
	 * 
	 * <p>
	 * This value represents <i>m</i> in the equation {@code y = m(x + b)}. For
	 * example, a power meter might report power as <i>killowatts</i>, in which
	 * case {@code slope} can be configured as {@literal .001} to convert the
	 * value to <i>watts</i>.
	 * </p>
	 * 
	 * @return the unit slope multiplier, never {@literal null}; defaults to
	 *         {@link #DEFAULT_SLOPE}
	 */
	public BigDecimal getUnitSlope() {
		return unitSlope;
	}

	/**
	 * Set the unit slope multiplier.
	 * 
	 * @param unitSlope
	 *        the unit slope multiplier to set; if {@literal null}
	 *        {@link #DEFAULT_SLOPE} will be used instead
	 */
	public void setUnitSlope(BigDecimal unitSlope) {
		this.unitSlope = unitSlope != null ? unitSlope : DEFAULT_SLOPE;
	}

	/**
	 * Get the unit y-intercept offset.
	 * 
	 * <p>
	 * This value represents <i>b</i> in the equation {@code y = m(x + b)}. For
	 * example, a sensor might report values in the range {@literal -10..10}
	 * which should be interpreted as values in the range {@literal 0..20}, in
	 * which case {@code intercept} can be configured as {@literal 10} to
	 * convert the value appropriately.
	 * </p>
	 * 
	 * @return the unit y-intercept, never {@literal null}; defaults to
	 *         {@link #DEFAULT_INTERCEPT}
	 */
	public BigDecimal getUnitIntercept() {
		return unitIntercept;
	}

	/**
	 * Set the unit y-intercept offset.
	 * 
	 * @param unitIntercept
	 *        the unit intercept offset to set; if {@literal null}
	 *        {@link #DEFAULT_INTERCEPT} will be used instead
	 */
	public void setUnitIntercept(BigDecimal unitIntercept) {
		this.unitIntercept = unitIntercept != null ? unitIntercept : DEFAULT_INTERCEPT;
	}

	/**
	 * Get the decimal scale to round decimal numbers to.
	 * 
	 * @return the decimal scale; defaults to {@link #DEFAULT_DECIMAL_SCALE}
	 */
	public int getDecimalScale() {
		return decimalScale;
	}

	/**
	 * Set the decimal scale to round decimal numbers to.
	 * 
	 * <p>
	 * This is a <i>maximum</i> scale value that decimal values should be
	 * rounded to. A scale of {@literal 0} would round all decimals to integer
	 * values.
	 * </p>
	 * 
	 * @param decimalScale
	 *        the maximum scale to set, or {@literal -1} to disable rounding
	 *        completely
	 */
	public void setDecimalScale(int decimalScale) {
		this.decimalScale = decimalScale;
	}

}

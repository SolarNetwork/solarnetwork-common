/* ==================================================================
 * ExpressionConfiguration.java - 9/07/2026 12:22:41 pm
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

package net.solarnetwork.service.support;

import static net.solarnetwork.service.ExpressionService.getGeneralExpressionReferenceLink;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.jspecify.annotations.Nullable;
import org.springframework.expression.Expression;
import net.solarnetwork.service.ExpressionService;
import net.solarnetwork.settings.SettingSpecifier;
import net.solarnetwork.settings.support.BasicMultiValueSettingSpecifier;
import net.solarnetwork.settings.support.BasicTextAreaSettingSpecifier;

/**
 * A general purpose expression configuration.
 *
 * @author matt
 * @version 1.0
 * @since 4.43
 */
public class ExpressionConfiguration {

	private @Nullable String expression;
	private @Nullable String expressionServiceId;

	private @Nullable Expression cachedExpression;

	/**
	 * Constructor.
	 */
	public ExpressionConfiguration() {
		super();
	}

	/**
	 * Get settings suitable for configuring an instance of this class.
	 *
	 * <p>
	 * An assumed {@code ExpressionConfig.properties} resource will be used.
	 * </p>
	 *
	 * @param clazz
	 *        the class to get the relative resource from
	 *
	 * @param prefix
	 *        a setting key prefix to use
	 * @param expressionServices
	 *        the available expression services
	 * @return the settings, never {@code null}
	 */
	public static List<SettingSpecifier> settings(final Class<?> clazz, final String prefix,
			final @Nullable Iterable<ExpressionService> expressionServices) {
		return settings(clazz, "ExpressionConfig.properties", prefix, expressionServices);
	}

	/**
	 * Get settings suitable for configuring an instance of this class.
	 *
	 * @param clazz
	 *        the class to get the relative resource from
	 * @param propertiesResource
	 *        the message properties resource
	 * @param prefix
	 *        a setting key prefix to use
	 * @param expressionServices
	 *        the available expression services
	 * @return the settings, never {@code null}
	 */
	public static List<SettingSpecifier> settings(final Class<?> clazz, final String propertiesResource,
			final String prefix, final @Nullable Iterable<ExpressionService> expressionServices) {
		final List<SettingSpecifier> results = new ArrayList<>(3);

		// the expression text area
		final var exprSetting = new BasicTextAreaSettingSpecifier(prefix + "expression", "", true);
		exprSetting.setDescriptionArguments(new Object[] { getGeneralExpressionReferenceLink(),
				expressionReferenceLink(clazz, propertiesResource) });
		results.add(exprSetting);

		// the expression drop-down menu
		final var expressionServiceId = new BasicMultiValueSettingSpecifier(
				prefix + "expressionServiceId", "");
		Map<String, String> exprServiceTitles = new LinkedHashMap<>();
		exprServiceTitles.put("", "");
		if ( expressionServices != null ) {
			for ( ExpressionService service : expressionServices ) {
				exprServiceTitles.put(service.getUid(), service.getDisplayName());
			}
		}
		expressionServiceId.setValueTitles(exprServiceTitles);
		results.add(expressionServiceId);

		return results;
	}

	/**
	 * Get a link to an expression service guide.
	 *
	 * @param clazz
	 *        the class to get the relative resource from
	 * @param propertiesResource
	 *        the message properties resource
	 * @return a link to an expression guide
	 */
	public static @Nullable URI expressionReferenceLink(final Class<?> clazz,
			final String propertiesResource) {
		String result = null;
		Properties props = new Properties();
		try (InputStream in = clazz.getResourceAsStream(propertiesResource)) {
			if ( in != null ) {
				props.load(in);
				if ( props.containsKey("expressions.url") ) {
					result = props.getProperty("expressions.url");
				}
			}
		} catch ( IOException e ) {
			// ignore this
		}
		URI uri = null;
		if ( result != null ) {
			try {
				uri = new URI(result);
			} catch ( URISyntaxException e ) {
				throw new RuntimeException(e);
			}
		} else {
			uri = getGeneralExpressionReferenceLink();
		}
		return uri;
	}

	/**
	 * Get the appropriate {@link Expression} to use based on the
	 * {@link #getExpression()} value, if {@link #getExpressionServiceId()} is
	 * configured and the matching service is available.
	 *
	 * <p>
	 * The parsed expression will be cached and re-used over the life of this
	 * instance.
	 * </p>
	 *
	 * @param services
	 *        the available services
	 * @return the expression instance, or {@code null} if no expression is
	 *         configured or the appropriate service is not found
	 */
	public synchronized @Nullable ExpressionServiceExpression expression(
			final @Nullable Iterable<ExpressionService> services) {
		final String serviceId = getExpressionServiceId();
		if ( serviceId == null || services == null ) {
			return null;
		}
		for ( ExpressionService service : services ) {
			if ( service != null && serviceId.equalsIgnoreCase(service.getUid()) ) {
				Expression expr = cachedExpression;
				if ( expr == null ) {
					final String value = getExpression();
					if ( value != null ) {
						expr = service.parseExpression(value);
						if ( expr != null ) {
							cachedExpression = expr;
						}
					}
				}
				if ( expr != null ) {
					return new ExpressionServiceExpression(service, expr);
				}
			}
		}
		return null;
	}

	/**
	 * Get the expression.
	 *
	 * @return the expression
	 */
	public final @Nullable String getExpression() {
		return expression;
	}

	/**
	 * Set the expression.
	 *
	 * @param expression
	 *        the expression to set
	 */
	public final synchronized void setExpression(@Nullable String expression) {
		this.expression = expression;
		this.cachedExpression = null;
	}

	/**
	 * Get the {@link ExpressionService} ID to use when evaluating
	 * {@link #getExpression()}.
	 *
	 * @return the service ID
	 */
	public final @Nullable String getExpressionServiceId() {
		return expressionServiceId;
	}

	/**
	 * Set the {@link ExpressionService} ID to use when evaluating
	 * {@link #getExpression()}.
	 *
	 * @param expressionServiceId
	 *        the service ID to set
	 */
	public final synchronized void setExpressionServiceId(@Nullable String expressionServiceId) {
		this.expressionServiceId = expressionServiceId;
		this.cachedExpression = null;
	}

}

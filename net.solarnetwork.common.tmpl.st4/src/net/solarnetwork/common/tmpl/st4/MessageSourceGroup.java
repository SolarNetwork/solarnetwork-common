/* ==================================================================
 * MessageSourceGroup.java - 26/07/2020 7:31:13 AM
 * 
 * Copyright 2020 SolarNetwork.net Dev Team
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

package net.solarnetwork.common.tmpl.st4;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.compiler.CompiledST;
import org.stringtemplate.v4.misc.ErrorType;
import org.stringtemplate.v4.misc.Misc;

/**
 * {@link STGroup} that loads templates from a {@link MessageSource}.
 * 
 * @author matt
 * @version 1.0
 */
public class MessageSourceGroup extends STGroup {

	private static final Logger log = LoggerFactory.getLogger(MessageSourceGroup.class);

	private final String groupName;
	private final MessageSource messageSource;

	/**
	 * Constructor.
	 * 
	 * @param groupName
	 *        a group name to use
	 * @param messageSource
	 *        the message source
	 * @throws IllegalArgumentException
	 *         if {@code messageSource} is {@literal null}
	 */
	public MessageSourceGroup(String groupName, MessageSource messageSource) {
		this(groupName, messageSource, '<', '>');
	}

	/**
	 * Constructor.
	 * 
	 * @param groupName
	 *        a group name to use
	 * @param messageSource
	 *        the message source
	 * @param delimiterStartChar
	 *        the start delimiter
	 * @param delimiterStopChar
	 *        the end delimiter
	 * @throws IllegalArgumentException
	 *         if {@code groupName} or {@code messageSource} are {@literal null}
	 */
	public MessageSourceGroup(String groupName, MessageSource messageSource, char delimiterStartChar,
			char delimiterStopChar) {
		super(delimiterStartChar, delimiterStopChar);
		if ( groupName == null ) {
			throw new IllegalArgumentException("The groupName argument must be provided.");
		}
		this.groupName = groupName;
		if ( messageSource == null ) {
			throw new IllegalArgumentException("The messageSource argument must be provided.");
		}
		this.messageSource = messageSource;
	}

	@Override
	public void importTemplates(Token fileNameToken) {
		String msg = "import illegal in group files embedded in MessageSourceGroup; import "
				+ fileNameToken.getText() + " in MessageSourceGroup " + this.getName();
		throw new UnsupportedOperationException(msg);
	}

	@Override
	protected CompiledST load(String name) {
		log.debug("MessageSourceGroup.load({})", name);
		String prefix = Misc.getPrefix(name);
		String unqualifiedName = Misc.getFileName(name);
		return loadTemplateFile(prefix, unqualifiedName + TEMPLATE_FILE_EXTENSION);
	}

	private CompiledST loadTemplateFile(String prefix, String unqualifiedFileName) {
		log.debug("loadTemplateFile({}) in MessageSourceGroup {} from prefix={}", unqualifiedFileName,
				groupName, prefix);
		String t;
		try {
			t = messageSource.getMessage(unqualifiedFileName, null, null);
		} catch ( NoSuchMessageException e ) {
			errMgr.runTimeError(null, null, ErrorType.INVALID_TEMPLATE_NAME, e, unqualifiedFileName);
			return null;
		}

		ANTLRStringStream s = new ANTLRStringStream(t);
		s.name = unqualifiedFileName;
		return loadTemplateFile(prefix, unqualifiedFileName, s);
	}

	@Override
	public String getName() {
		return groupName;
	}

}
